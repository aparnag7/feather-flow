package com.featherflow.featherflow.repository;

import com.featherflow.featherflow.models.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID> {
}
