package com.featherflow.featherflow.service.orchestrator;

import com.featherflow.featherflow.models.*;
import com.featherflow.featherflow.repository.*;
import com.featherflow.featherflow.service.JobService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class WorkflowRunner {

    private final JobRepository jobRepository;
    private final JobDependencyRepository dependencyRepository;
    private final JobRunner jobRunner;
    private final JobService jobService;
    private final WorkflowRepository workflowRepository;
    private final JobRunRepository jobRunRepository;
    private final WorkflowRunRespository workflowRunRespository;

    public WorkflowRunner(JobRepository jobRepository, JobDependencyRepository dependencyRepository, JobRunner jobRunner, JobService jobService, WorkflowRepository workflowRepository, JobRunRepository jobRunRepository, WorkflowRunRespository workflowRunRespository) {
        this.jobRepository = jobRepository;
        this.dependencyRepository = dependencyRepository;
        this.jobRunner = jobRunner;
        this.jobService = jobService;
        this.workflowRepository = workflowRepository;
        this.jobRunRepository = jobRunRepository;
        this.workflowRunRespository = workflowRunRespository;
    }

    public void runWorkflow(UUID workflowId) throws InterruptedException {
        Workflow workflow = workflowRepository.findById(workflowId).orElseThrow();

        // create a new workflow run entry in the database
        WorkflowRun workflowRun = new WorkflowRun();
        workflowRun.setWorkflow(workflow);
        workflowRun.setStatus(String.valueOf(JobStatus.RUNNING));
        workflowRunRespository.save(workflowRun);

        // get all jobs and dependencies for the workflow
        List<Job> jobs = jobRepository.findByWorkflowId(workflowId);
        HashMap<UUID, Job> jobMap = new HashMap<>();
        HashMap<UUID, List<UUID>> adjacencyList = new HashMap<>(); // each job knows which jobs depend on it.
        ConcurrentHashMap<UUID, Integer> inDegree = new ConcurrentHashMap<>();
        // count how many dependencies each job has
        // multiple threads will update inDegree, so use ConcurrentHashMap

        // Build DAG
        for (Job job : jobs) {
            jobMap.put(job.getId(), job);
            List<JobDependency> dependencyList = dependencyRepository.findByJob(job);
            inDegree.put(job.getId(), dependencyList.size());
            for (JobDependency dependency : dependencyList) {
                UUID dependsOnJobId = dependency.getDependsOnJob().getId();
                adjacencyList.computeIfAbsent(dependsOnJobId, k -> new java.util.ArrayList<>()).add(job.getId());
            }
        }

        ExecutorService executor = Executors.newFixedThreadPool(4);
        AtomicInteger jobsRemaining = new AtomicInteger(jobMap.size());
        BlockingQueue<UUID> readyQueue = new LinkedBlockingQueue<>();
        // thread-safe queue for jobs ready to run
        // if thread tries to take() an element but the queue is empty,
        // the thread waits (blocks) until an element is available.

        // add all jobs with no dependencies to the ready queue
        for (UUID jobId : inDegree.keySet()) {
            if (inDegree.get(jobId) == 0) {
                readyQueue.add(jobId);
            }
        }
        System.out.println("Initial ready queue: " + readyQueue.size());
        System.out.println("Total jobs to run: " + jobsRemaining.get());

        while (jobsRemaining.get() > 0) {
            UUID jobId = readyQueue.poll(10, TimeUnit.SECONDS);
            if (jobId == null) continue;

            executor.submit(() -> {
                Job job = jobMap.get(jobId);
                // create JobRun for this execution
                JobRun jobRun = new JobRun();
                jobRun.setJob(job);
                jobRun.setWorkflowRun(workflowRun);
                jobRun.setStatus(String.valueOf(JobStatus.RUNNING));
                jobRun.setStartedAt( LocalDateTime.now());
                jobRunRepository.save(jobRun);

                try {
                    jobService.updateStatus(jobId, JobStatus.RUNNING);
                    System.out.println("Job " + job.getName() + " is RUNNING...");

                    jobRunner.runJob(job);
                    jobRun.setStatus(String.valueOf(JobStatus.SUCCESS));
                    jobRun.setEndedAt(LocalDateTime.now());
                    jobRunRepository.save(jobRun);

                    jobService.updateStatus(jobId, JobStatus.SUCCESS);
                    System.out.println("Job " + job.getName() + " SUCCESS");

                    List<UUID> dependents = adjacencyList.getOrDefault(jobId, new ArrayList<>());
                    for (UUID dependantJobId : dependents) {
                        int newVal = inDegree.compute(dependantJobId, (k, v) -> v - 1);
                        if (newVal == 0) {
                            readyQueue.add(dependantJobId);
                        }
                    }
                    jobsRemaining.decrementAndGet();
                } catch (Exception e) {
                    jobRun.setStatus(String.valueOf(JobStatus.FAILED));
                    jobRun.setEndedAt(LocalDateTime.now());
                    jobRunRepository.save(jobRun);
                    jobService.updateStatus(jobId, JobStatus.FAILED);
                    System.out.println("Job " + job.getName() + " FAILED: " + e.getMessage());
                    skipDependents(jobId, adjacencyList);
                }
            });
        }

        executor.shutdown(); //Tells the executor to stop accepting new tasks. Continue running already submitted tasks until they finish.
        executor.awaitTermination(1, TimeUnit.HOURS); // Waits until either all tasks finish (after shutdown()), OR Timeout expires (1 hour in this case).

        List<JobRun> allJobRuns = jobRunRepository.findByWorkflowRun(workflowRun);
        boolean anyFailed = allJobRuns.stream().anyMatch(jobRun -> jobRun.getStatus().equals(String.valueOf(JobStatus.FAILED)));
        boolean allSkipped = allJobRuns.stream().allMatch( jobRun -> jobRun.getStatus().equals(String.valueOf(JobStatus.SKIPPED)));
        boolean allSuccess = allJobRuns.stream().allMatch( jobRun -> jobRun.getStatus().equals(String.valueOf(JobStatus.SUCCESS)));
        if (anyFailed) {
            workflowRun.setStatus(String.valueOf(JobStatus.FAILED));
        } else if (allSkipped) {
            workflowRun.setStatus(String.valueOf(JobStatus.SKIPPED));
        } else if(allSuccess) {
            workflowRun.setStatus(String.valueOf(JobStatus.SUCCESS));
        }
        workflowRun.setEndedAt(LocalDateTime.now());
        workflowRunRespository.save(workflowRun);
        // workflow status does not need to be persisted, it can be queried from job status.
        System.out.println("Workflow " + workflow.getName() + " completed with status: " + workflowRun.getStatus());
    }

    private void skipDependents(UUID jobId, HashMap<UUID, List<UUID>> adjacencyList) {
        List<UUID> dependents = adjacencyList.getOrDefault(jobId, new ArrayList<>());
        for (UUID dependantJobId : dependents) {
            jobService.updateStatus(dependantJobId, JobStatus.SKIPPED);
            System.out.println("Job " + dependantJobId + " SKIPPED due to dependency failure.");
            // recursively skip their dependents
            skipDependents(dependantJobId, adjacencyList);
        }
    }
}
