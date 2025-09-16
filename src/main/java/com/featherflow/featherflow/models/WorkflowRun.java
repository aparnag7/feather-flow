package com.featherflow.featherflow.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "workflow_runs")
@Getter
@Setter
public class WorkflowRun {
    @Id
    @Column(nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "workflow_id", nullable = false)
    private Workflow workflow;

    private String status;

    @OneToMany(mappedBy = "workflowRun", cascade = CascadeType.ALL)
    private List<JobRun> jobRuns;

    @Column
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime EndedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
