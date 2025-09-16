package com.featherflow.featherflow.models;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "execution_logs")
@Getter
@Setter
public class ExecutionLog {
    @Id
    @Column(nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "job_run_id", nullable = false)
    private JobRun jobRun;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    private String level; // INFO, WARN, ERROR

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
        if(this.level == null) {
            this.level = "INFO";
        }
    }
}
