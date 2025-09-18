package com.featherflow.featherflow.service;

import com.featherflow.featherflow.models.Job;
import com.featherflow.featherflow.models.JobStatus;
import com.featherflow.featherflow.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Service
public class JobService {
    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Transactional(propagation = REQUIRES_NEW)
    public void updateStatus(UUID jobId, JobStatus status){
        Job job = jobRepository.findById(jobId).orElseThrow();
        job.setStatus(status.name());
        jobRepository.save(job);
    }
}
