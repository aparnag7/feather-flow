package com.featherflow.featherflow.repository;

import com.featherflow.featherflow.models.Job;
import com.featherflow.featherflow.models.JobDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JobDependencyRepository extends JpaRepository<JobDependency, UUID> {
    List<JobDependency> findByJob(Job job);
}
