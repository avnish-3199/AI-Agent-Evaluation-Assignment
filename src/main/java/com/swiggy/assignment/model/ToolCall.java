package com.swiggy.assignment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Represents a tool call made by the assistant during a conversation turn.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolCall {
    private String name;
    private Map<String, Object> parameters;
    private boolean executionSuccess;
}
