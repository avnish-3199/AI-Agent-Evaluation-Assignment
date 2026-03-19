package com.swiggy.assignment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Represents multi-layered feedback for a conversation.
 * It includes user ratings, ops reviews, and detailed annotations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {
    private Integer userRating;
    private OpsReview opsReview;
    private List<Annotation> annotations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OpsReview {
        private String quality;
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Annotation {
        private String type;
        private String label;
        private String annotatorId;
        private Double confidence; // For meta-evaluation weighting
    }

    /**
     * Get the consolidated rating (user rating normalized to 1.0)
     */
    public double getRating() {
        return userRating != null ? userRating / 5.0 : 0.0;
    }
}
