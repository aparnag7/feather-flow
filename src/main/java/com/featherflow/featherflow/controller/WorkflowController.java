package com.featherflow.featherflow.controller;

import com.featherflow.featherflow.aop.LogExecution;
import com.featherflow.featherflow.service.WorkflowOrchestrator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {

    private final WorkflowOrchestrator orchestrator;

    public WorkflowController(WorkflowOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping("/run")
    @LogExecution
    public ResponseEntity<String> runWorkflow(@RequestParam("yamlPath") String yamlPath) throws InterruptedException {
        orchestrator.runWorkflowFromYaml(yamlPath);
        return ResponseEntity.accepted().body("Workflow started for YAML: " + yamlPath);
    }
}


