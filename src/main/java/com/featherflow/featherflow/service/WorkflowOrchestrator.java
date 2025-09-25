package com.featherflow.featherflow.service;

import com.featherflow.featherflow.models.Workflow;
import com.featherflow.featherflow.models.WorkflowDefinition;
import com.featherflow.featherflow.parser.DAGValidator;
import com.featherflow.featherflow.parser.YamlParser;
import com.featherflow.featherflow.service.orchestrator.WorkflowRunner;
import org.springframework.stereotype.Component;
import com.featherflow.featherflow.aop.LogExecution;

@Component
public class WorkflowOrchestrator {
    private final WorkflowService workflowService;
    private final WorkflowRunner workflowRunner;

    public WorkflowOrchestrator(WorkflowService workflowService, WorkflowRunner workflowRunner) {
        this.workflowService = workflowService;
        this.workflowRunner = workflowRunner;
    }

    @LogExecution
    public void runWorkflowFromYaml(String yamlFilePath) throws InterruptedException {
        // 1. Parse YAML
        WorkflowDefinition workflowDefinition = new YamlParser().parseWorkflow(yamlFilePath);

        // 2. Validate DAG
        DAGValidator.validate(workflowDefinition);

        // 3. Persist workflow and jobs
        Workflow workflow = workflowService.saveWorkflow(workflowDefinition);

        System.out.println("Workflow persisted successfully: " + workflow.getId());

        // 4. Execute workflows
        workflowRunner.runWorkflow(workflow.getId());
    }
}
