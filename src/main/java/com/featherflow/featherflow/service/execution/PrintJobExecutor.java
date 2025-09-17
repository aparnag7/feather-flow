package com.featherflow.featherflow.service.execution;

import org.springframework.stereotype.Service;

@Service
public class PrintJobExecutor implements  JobExecutor{
    @Override
    public void executeJob(com.featherflow.featherflow.models.Job job) {
        System.out.println("Executing job: " + job.getName() + " with endpoint: " + job.getEndpoint());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Job execution interrupted", e);
        }
        System.out.println("Finished job: " + job.getName());
    }
}
