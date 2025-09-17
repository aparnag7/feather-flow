package com.featherflow.featherflow.service.orchestrator;

import com.featherflow.featherflow.models.Job;
import com.featherflow.featherflow.service.execution.JobExecutorFactory;
import org.springframework.stereotype.Service;

@Service
public class JobRunner {

    private final JobExecutorFactory jobExecutorFactory;

    public JobRunner(JobExecutorFactory jobExecutorFactory) {
        this.jobExecutorFactory = jobExecutorFactory;
    }
    public void runJob(Job job) {
        jobExecutorFactory.getExecutor().executeJob(job);
    }
}
