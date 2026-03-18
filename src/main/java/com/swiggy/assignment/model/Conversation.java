package com.swiggy.assignment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Represents a multi-turn conversation between a user and an assistant.
 * It contains metadata, turns, evaluation results, and user feedback.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {
    private String id;
    private Map<String, Object> metadata;
    private List<Turn> turns;
    private List<EvaluationResult> evaluations;
    private Feedback feedback;
}
