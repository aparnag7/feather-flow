package com.featherflow.featherflow.service;

import com.featherflow.featherflow.models.Workflow;
import com.featherflow.featherflow.models.WorkflowDefinition;
import com.featherflow.featherflow.parser.DAGValidator;
import com.featherflow.featherflow.parser.YamlParser;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

@Component
public class WorkflowOrchestrator {
    private final WorkflowService workflowService;

    public WorkflowOrchestrator(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @Transactional
    public Workflow runWorkflowFromYaml(String yamlFilePath) {
        // 1. Parse YAML
        WorkflowDefinition workflowDefinition = new YamlParser().parseWorkflow("assets/user_onboarding_workflow.yaml");

        // 2. Validate DAG
        DAGValidator.validate(workflowDefinition);

        // 3. Persist workflow and jobs
        Workflow workflow = workflowService.saveWorkflow(workflowDefinition);

        System.out.println("Workflow persisted successfully: " + workflow.getId());
        return workflow;
    }
}
