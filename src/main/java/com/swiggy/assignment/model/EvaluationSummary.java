package com.swiggy.assignment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Consolidated evaluation output as specified in the assignment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationSummary {
    private String evaluationId;
    private String conversationId;
    private Map<String, Double> scores; // overall, response_quality, tool_accuracy, coherence
    private ToolEvaluation toolEvaluation;
    private List<Issue> issuesDetected;
    private List<ImprovementSuggestion> improvementSuggestions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToolEvaluation {
        private double selectionAccuracy;
        private double parameterAccuracy;
        private boolean executionSuccess;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Issue {
        private String type;
        private String severity;
        private String description;
    }
}
