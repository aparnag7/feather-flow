package com.featherflow.featherflow.repository;

import com.featherflow.featherflow.models.JobRun;
import com.featherflow.featherflow.models.WorkflowRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JobRunRepository extends JpaRepository<JobRun, UUID> {
    List<JobRun> findByWorkflowRun(WorkflowRun workflowRun);
}
