package com.featherflow.featherflow.service;

import com.featherflow.featherflow.models.Job;
import com.featherflow.featherflow.models.JobStatus;
import com.featherflow.featherflow.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class JobService {
    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public void updateStatus(UUID jobId, JobStatus status){
        Job job = jobRepository.findById(jobId).orElseThrow();
        job.setStatus(status.name());
        jobRepository.save(job);
    }
}
