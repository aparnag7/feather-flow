package com.featherflow.featherflow.service;

import com.featherflow.featherflow.models.*;
import com.featherflow.featherflow.repository.JobDependencyRepository;
import com.featherflow.featherflow.repository.JobRepository;
import com.featherflow.featherflow.repository.WorkflowRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class WorkflowService {
    private final WorkflowRepository workflowRepository;
    private final JobRepository jobRepository;
    private final JobDependencyRepository jobDependencyRepository;


    public WorkflowService(WorkflowRepository workflowRepository, JobRepository jobRepository, JobDependencyRepository jobDependencyRepository) {
        this.workflowRepository = workflowRepository;
        this.jobRepository = jobRepository;
        this.jobDependencyRepository = jobDependencyRepository;
    }

    // read about Transactional here: https://docs.spring.io/spring-framework/docs/current/reference/html/data-access.html#transaction-declarative-annotations

    @Transactional
    public Workflow saveWorkflow(WorkflowDefinition workflowDefinition) {

        // Step 1: Create Workflow entity
        Workflow workflow = new Workflow();
        workflow.setName(workflowDefinition.getName());
        workflow = workflowRepository.save(workflow);

        // Step 2: Create Job entities and map by name
        HashMap<String, Job> jobMap = new HashMap<>();

        for(JobDefinition jobDefinition: workflowDefinition.getJobs()) {
            Job job = new Job();
            job.setName(jobDefinition.getName());
            job.setEndpoint(jobDefinition.getEndpoint());
            job.setServiceName(jobDefinition.getServiceName());
            job.setWorkflow(workflow);
            job = jobRepository.save(job);
            jobMap.put(job.getName(), job);
        }

        // Step 3: Save dependencies in job dependency table
        for(JobDefinition jobDefinition: workflowDefinition.getJobs()) {
            Job job = jobMap.get(jobDefinition.getName());
            for(String dependencyName: jobDefinition.getDependsOn()) {
                Job dependsOnJob = jobMap.get(dependencyName);
                JobDependency jobDependency = new JobDependency();
                jobDependency.setJob(job);
                jobDependency.setDependsOnJob(dependsOnJob);
                jobDependencyRepository.save(jobDependency);
            }
        }
        System.out.println("Saved to DB!");
        return workflow;
    }
}
