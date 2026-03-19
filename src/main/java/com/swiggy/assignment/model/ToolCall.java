package com.swiggy.assignment.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Represents a tool call initiated by the AI agent during a turn.
 * Compliant with assignment.md schema.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolCall {
    @JsonProperty("tool_name")
    private String toolName;
    
    private Map<String, Object> parameters;
    private Map<String, Object> result;
    
    @JsonProperty("latency_ms")
    private Long latencyMs;
    
    @JsonProperty("execution_success")
    @Builder.Default
    private boolean executionSuccess = true;

    /**
     * Helper for backward compatibility or ease of use
     */
    public String getName() {
        return toolName;
    }
}
