package com.featherflow.featherflow;

import com.featherflow.featherflow.models.WorkflowDefinition;
import com.featherflow.featherflow.parser.DAGValidator;
import com.featherflow.featherflow.parser.YamlParser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FeatherflowApplication {

	public static void main(String[] args) {
		SpringApplication.run(FeatherflowApplication.class, args);
        WorkflowDefinition workflowDefinition = new YamlParser().parseWorkflow("assets/user_onboarding_workflow.yaml");
        DAGValidator.validate(workflowDefinition);
        System.out.println("DAG is valid");
	}

}
