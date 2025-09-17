package com.featherflow.featherflow.repository;

import com.featherflow.featherflow.models.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {
    List<Job> findByWorkflowId(UUID workflowId);
}
