package com.featherflow.featherflow.service.orchestrator;

import com.featherflow.featherflow.models.Job;
import com.featherflow.featherflow.models.JobDependency;
import com.featherflow.featherflow.models.JobStatus;
import com.featherflow.featherflow.repository.JobDependencyRepository;
import com.featherflow.featherflow.repository.JobRepository;
import com.featherflow.featherflow.service.JobService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class WorkflowRunner {

    private final JobRepository jobRepository;
    private final JobDependencyRepository dependencyRepository;
    private final JobRunner jobRunner;
    private final JobService jobService;

    public WorkflowRunner(JobRepository jobRepository, JobDependencyRepository dependencyRepository, JobRunner jobRunner, JobService jobService) {
        this.jobRepository = jobRepository;
        this.dependencyRepository = dependencyRepository;
        this.jobRunner = jobRunner;
        this.jobService = jobService;
    }

    public void runWorkflow(UUID workflowId) throws InterruptedException {
        // get all jobs and dependencies for the workflow
        List<Job> jobs = jobRepository.findByWorkflowId(workflowId);
        HashMap<UUID, Job> jobMap = new HashMap<>();
        HashMap<UUID, List<UUID>> adjacencyList = new HashMap<>();
        ConcurrentHashMap<UUID, Integer> inDegree = new ConcurrentHashMap<>();
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
        BlockingQueue<UUID> readyQueue = new LinkedBlockingQueue<>();
        AtomicInteger jobsRemaining = new AtomicInteger(jobMap.size());

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
                Job job = jobMap.get(jobId); // use cached job for logic
                try {
                    jobService.updateStatus(jobId, JobStatus.RUNNING);
                    System.out.println("Job " + job.getName() + " is RUNNING...");

                    jobRunner.runJob(job);

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
                    jobService.updateStatus(jobId, JobStatus.FAILED);
                    System.out.println("Job " + job.getName() + " FAILED: " + e.getMessage());
                    skipDependents(jobId, adjacencyList);
                }
            });
        }

        executor.shutdown(); //Tells the executor to stop accepting new tasks. Continue running already submitted tasks until they finish.
        executor.awaitTermination(1, TimeUnit.HOURS); // Waits until either all tasks finish (after shutdown()), OR Timeout expires (1 hour in this case).
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
