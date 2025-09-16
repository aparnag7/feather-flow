package com.featherflow.featherflow.repository;

import com.featherflow.featherflow.models.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WorkflowRepository extends JpaRepository<Workflow, UUID> {

}
