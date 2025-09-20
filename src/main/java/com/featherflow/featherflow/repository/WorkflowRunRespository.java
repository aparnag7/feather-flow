package com.featherflow.featherflow.repository;

import com.featherflow.featherflow.models.WorkflowRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WorkflowRunRespository extends JpaRepository<WorkflowRun, UUID> {
}
