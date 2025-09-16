package com.featherflow.featherflow.parser;

import com.featherflow.featherflow.models.JobDefinition;
import com.featherflow.featherflow.models.WorkflowDefinition;

import java.util.HashMap;

public class DAGValidator {
    public static void validate(WorkflowDefinition workflowDefinition) {
        HashMap<String, JobDefinition> jobMap = new HashMap<>();
        for (JobDefinition job : workflowDefinition.getJobs()) {
            if(jobMap.containsKey(job)) {
                throw new RuntimeException("Duplicate job name found: " + job.getName());
            }
            jobMap.put(job.getName(), job);
        }

        // check if all dependencies exist
        for (JobDefinition job : workflowDefinition.getJobs()) {
            for (String dependency : job.getDependsOn()) {
                if (!jobMap.containsKey(dependency)) {
                    throw new RuntimeException("Job " + job.getName() + " has a dependency on non-existent job: " + dependency);
                }
            }
        }

        // check for cycles using DFS
        if(hasCycle(workflowDefinition, jobMap)) {
            throw new RuntimeException("Cycle detected in the workflow");
        }
    }

    private static boolean hasCycle(WorkflowDefinition workflowDefinition, HashMap<String, JobDefinition> jobMap) {
        HashMap<String, Boolean> visited = new HashMap<>();
        HashMap<String, Boolean> recStack = new HashMap<>();

        for (JobDefinition job : workflowDefinition.getJobs()) {
            if (isCyclicUtil(job.getName(), visited, recStack, jobMap)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isCyclicUtil(String jobName, HashMap<String, Boolean> visited, HashMap<String, Boolean> recStack, HashMap<String, JobDefinition> jobMap) {
        if (recStack.getOrDefault(jobName, false)) {
            return true;
        }
        if (visited.getOrDefault(jobName, false)) {
            return false;
        }

        visited.put(jobName, true);
        recStack.put(jobName, true);

        JobDefinition job = jobMap.get(jobName);
        for (String dependency : job.getDependsOn()) {
            if (isCyclicUtil(dependency, visited, recStack, jobMap)) {
                return true;
            }
        }

        recStack.put(jobName, false);
        return false;
    }
}
