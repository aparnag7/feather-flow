package com.featherflow.featherflow.models;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class JobDefinition {
    private String name;
    private String serviceName;
    private String endpoint;
    private List<String> dependsOn;

}
