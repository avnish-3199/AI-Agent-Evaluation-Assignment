package com.swiggy.assignment.model;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Domain Models.
 * Ensures correct behavior of data structures, builders, and custom logic like rating normalization.
 */
class ModelTests {

    /**
     * Tests {@link Conversation} model for ID mapping and basic encapsulation.
     */
    @Test
    void testConversationModel() {
        Conversation conv = new Conversation();
        conv.setConversationId("c1");
        conv.setAgentVersion("v1");
        conv.setMetadata(Map.of("k", "v"));
        conv.setTurns(Collections.singletonList(new Turn()));
        conv.setEvaluations(Collections.singletonList(new EvaluationResult()));
        conv.setFeedback(new Feedback());

        assertEquals("c1", conv.getConversationId());
        assertEquals("c1", conv.getId());
        assertEquals("v1", conv.getAgentVersion());
        assertNotNull(conv.getMetadata());
        assertNotNull(conv.getTurns());
        assertNotNull(conv.getEvaluations());
        assertNotNull(conv.getFeedback());
    }

    /**
     * Tests {@link EvaluationSummary} and its nested components (ToolEvaluation, Issue).
     */
    @Test
    void testEvaluationSummaryModel() {
        EvaluationSummary.ToolEvaluation tool = EvaluationSummary.ToolEvaluation.builder()
                .selectionAccuracy(1.0)
                .parameterAccuracy(0.9)
                .executionSuccess(true)
                .build();
        
        EvaluationSummary summary = EvaluationSummary.builder()
                .evaluationId("e1")
                .scores(Map.of("s", 1.0))
                .toolEvaluation(tool)
                .issuesDetected(Collections.singletonList(new EvaluationSummary.Issue("t", "s", "d")))
                .improvementSuggestions(Collections.singletonList(new ImprovementSuggestion()))
                .build();

        assertEquals("e1", summary.getEvaluationId());
        assertEquals(1.0, summary.getToolEvaluation().getSelectionAccuracy());
        assertEquals("t", summary.getIssuesDetected().get(0).getType());
    }

    /**
     * Tests {@link Feedback} rating normalization logic (5-star to 0.0-1.0).
     */
    @Test
    void testFeedbackModel() {
        Feedback feedback = Feedback.builder()
                .userRating(4)
                .opsReview(new Feedback.OpsReview("good", "notes"))
                .annotations(Collections.singletonList(new Feedback.Annotation("t", "l", "a", 0.9)))
                .build();

        assertEquals(0.8, feedback.getRating(), 0.01);
        assertEquals("good", feedback.getOpsReview().getQuality());
        
        Feedback empty = new Feedback();
        assertEquals(0.0, empty.getRating());
    }

    /**
     * Tests {@link Turn} and {@link ToolCall} encapsulation and helper methods.
     */
    @Test
    void testTurnAndToolCall() {
        ToolCall tc = ToolCall.builder()
                .toolName("tool")
                .parameters(Map.of("p", "v"))
                .result(Map.of("r", "v"))
                .latencyMs(100L)
                .executionSuccess(true)
                .build();
        
        Turn turn = Turn.builder()
                .turnId(1)
                .role("user")
                .content("hi")
                .toolCalls(Collections.singletonList(tc))
                .latencyMs(200L)
                .timestamp("now")
                .build();

        assertEquals("tool", tc.getToolName());
        assertEquals("tool", tc.getName());
        assertEquals(1, turn.getTurnId());
        assertEquals("user", turn.getRole());
    }
}
