package com.featherflow.featherflow.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "workflows")
@Getter
@Setter
public class Workflow {
    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    @OneToMany(mappedBy = "workflow", cascade = CascadeType.ALL)
    private List<Job> jobs;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
