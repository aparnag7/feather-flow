package com.featherflow.featherflow.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "jobs")
@Getter
@Setter
public class Job {

    @Id
    @Column(nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "workflow_id", nullable = false)
    private Workflow workflow;

    @Column(nullable = false)
    private String name;

    private String type;

    @Column(columnDefinition = "jsonb")
    private String config; // store JSON as string

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL)
    private List<JobDependency> dependencies;

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
