package com.swiggy.assignment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a suggestion for improving the conversation quality or system performance.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImprovementSuggestion {
    private String type; // e.g., "PROMPT_OPTIMIZATION", "TOOL_VALIDATION", "LATENCY_FIX"
    private String suggestion;
    private String rationale;
    private double confidence; // 0.0 to 1.0
}
