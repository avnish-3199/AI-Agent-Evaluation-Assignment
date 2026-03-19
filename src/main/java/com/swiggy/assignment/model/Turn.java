package com.swiggy.assignment.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a single turn in a conversation.
 * Compliant with assignment.md schema.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Turn {
    @JsonProperty("turn_id")
    private Integer turnId;
    
    private String role; // "user" or "assistant"
    private String content;
    
    @JsonProperty("tool_calls")
    private List<ToolCall> toolCalls;
    
    @JsonProperty("latency_ms")
    private Long latencyMs;
    
    private String timestamp; // ISO-8601 format
}
