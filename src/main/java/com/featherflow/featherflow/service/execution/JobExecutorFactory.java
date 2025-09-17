package com.featherflow.featherflow.service.execution;

import org.springframework.stereotype.Service;

@Service
public class JobExecutorFactory {
    public JobExecutor getExecutor(){
        return new PrintJobExecutor();
    }
}
