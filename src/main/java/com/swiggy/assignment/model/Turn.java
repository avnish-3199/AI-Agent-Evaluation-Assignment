package com.swiggy.assignment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Represents a single turn in a multi-turn conversation.
 * A turn includes the role of the speaker, content, and any tool calls made.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Turn {
    private String role; // user, assistant, system
    private String content;
    private List<ToolCall> toolCalls;
    private long latencyMs;
    private long timestamp;
    private Map<String, Object> metadata;
}
