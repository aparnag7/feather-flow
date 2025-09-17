package com.featherflow.featherflow.service.orchestrator;

import com.featherflow.featherflow.models.Job;
import com.featherflow.featherflow.models.JobDependency;
import com.featherflow.featherflow.repository.JobDependencyRepository;
import com.featherflow.featherflow.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class WorkflowRunner {

    private final JobRepository jobRepository;
    private final JobRunner jobRunner;
    private final JobDependencyRepository dependencyRepository;

    public WorkflowRunner(JobRepository jobRepository, JobDependencyRepository dependencyRepository, JobRunner jobRunner) {
        this.jobRepository = jobRepository;
        this.dependencyRepository = dependencyRepository;
        this.jobRunner = jobRunner;
    }

    public void runWorkflow(UUID workflowId) {
        // get list of jobs for this workflow
        List<Job> jobs = jobRepository.findByWorkflowId(workflowId);
        HashMap<UUID, Job> jobMap = new HashMap<>();
        HashMap<UUID, List<UUID>> adjacencyList = new HashMap<>();
        HashMap<UUID, Integer> inDegree = new HashMap<>();
        for (Job job : jobs) {
            jobMap.put(job.getId(), job);
            List<JobDependency> dependencyList = dependencyRepository.findByJob(job);
            inDegree.put(job.getId(), dependencyList.size());
            for (JobDependency dependency : dependencyList) {
                UUID dependsOnJobId = dependency.getDependsOnJob().getId();
                adjacencyList.computeIfAbsent(dependsOnJobId, k -> new java.util.ArrayList<>()).add(job.getId());
            }
        }
        /*
        1. Start with all jobs where indegree == 0 → ready to run.
        2. Execute them (via JobExecutor).
        3. After completion, decrement indegree of dependent jobs.
        4. If indegree reaches 0 → push to queue.
        5. Repeat until no jobs lef
         */

        Queue<UUID> readyQueue = new LinkedList<>();
        for(UUID jobId : inDegree.keySet()) {
            if(inDegree.get(jobId) == 0) {
                readyQueue.add(jobId);
            }
        }

        while (!readyQueue.isEmpty()) {
            UUID jobId = readyQueue.poll();
            Job job = jobMap.get(jobId);
            jobRunner.runJob(job);
            List<UUID> dependents = adjacencyList.getOrDefault(jobId, new ArrayList<>());
            for(UUID dependentId : dependents) {
                inDegree.put(dependentId, inDegree.get(dependentId) - 1);
                if(inDegree.get(dependentId) == 0) {
                    readyQueue.add(dependentId);
                }
            }
        }
    }
}
