package com.swiggy.assignment.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Represents a multi-turn conversation between a user and an assistant.
 * It contains metadata, turns, evaluation results, and user feedback.
 * Compliant with assignment.md schema.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {
    @JsonProperty("conversation_id")
    private String conversationId;
    
    @JsonProperty("agent_version")
    private String agentVersion;
    
    private Map<String, Object> metadata;
    private List<Turn> turns;
    private List<EvaluationResult> evaluations;
    private Feedback feedback;

    /**
     * Helper for backward compatibility or ease of use
     */
    public String getId() {
        return conversationId;
    }
}
