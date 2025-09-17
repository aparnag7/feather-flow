package com.featherflow.featherflow;

import com.featherflow.featherflow.service.WorkflowOrchestrator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// read about CommandLineRunner
public class FeatherflowApplication implements CommandLineRunner {

    private final WorkflowOrchestrator orchestrator;

    public FeatherflowApplication(WorkflowOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

	public static void main(String[] args) {
		SpringApplication.run(FeatherflowApplication.class, args);
	}

    public void run(String... args) {
        String yamlPath = "assets/complex_workflow.yaml";
        orchestrator.runWorkflowFromYaml(yamlPath);
    }
}
