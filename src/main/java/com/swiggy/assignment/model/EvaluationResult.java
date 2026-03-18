package com.swiggy.assignment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the result of a conversation evaluation performed by a specific evaluator.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationResult {
    private String evaluatorName;
    private double score; // 0.0 to 1.0
    private String comments;
    private long timestamp;
}
