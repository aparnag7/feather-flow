package com.featherflow.featherflow.models;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WorkflowDefinition {

    private String name;
    private List<JobDefinition> jobs;

}
