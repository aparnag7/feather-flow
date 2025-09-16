package com.featherflow.featherflow.parser;

import com.featherflow.featherflow.models.WorkflowDefinition;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;

public class YamlParser {

    public WorkflowDefinition parseWorkflow(String filePath) {
        LoaderOptions options = new LoaderOptions();
        Yaml yaml = new Yaml(new Constructor(WorkflowDefinition.class, options));
        try (InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream(filePath)) {

            if (inputStream == null) {
                throw new RuntimeException("File not found: " + filePath);
            }
            return yaml.load(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse YAML: " + filePath, e);
        }
    }
}
