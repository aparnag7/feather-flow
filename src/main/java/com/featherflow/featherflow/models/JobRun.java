package com.featherflow.featherflow.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "job_runs")
@Getter
@Setter
public class JobRun {

    @Id
    @GeneratedValue
    @Column(nullable = false)
    private UUID id;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column
    private String status;

    @ManyToOne
    @JoinColumn(name = "workflow_run_id", nullable = false)
    private WorkflowRun workflowRun;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    @PrePersist
    protected void onCreate() {
        this.status = String.valueOf(JobStatus.RUNNING);
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
