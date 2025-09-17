package com.featherflow.featherflow.service.execution;

import com.featherflow.featherflow.models.Job;

public interface JobExecutor {
    void executeJob(Job job);
}
