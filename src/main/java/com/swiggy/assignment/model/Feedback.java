package com.swiggy.assignment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Represents user feedback or rating for a specific conversation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {
    private double rating; // 0.0 to 1.0 (e.g. 5 stars = 1.0)
    private Map<String, String> annotations; // key-value pairs
}
