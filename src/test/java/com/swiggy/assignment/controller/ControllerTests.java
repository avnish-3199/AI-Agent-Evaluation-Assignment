package com.swiggy.assignment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swiggy.assignment.model.Conversation;
import com.swiggy.assignment.model.EvaluationSummary;
import com.swiggy.assignment.model.Feedback;
import com.swiggy.assignment.service.ConversationService;
import com.swiggy.assignment.service.EvaluationService;
import com.swiggy.assignment.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller-level tests using {@link MockMvc}.
 * Tests cover API routing, serialization, and global exception handling for a robust REST layer.
 */
@WebMvcTest(controllers = {ConversationController.class, EvaluationController.class, GlobalExceptionHandler.class})
class ControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConversationService conversationService;

    @MockBean
    private EvaluationService evaluationService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Tests {@link ConversationController#ingestConversation(Conversation)} for single ingestion.
     */
    @Test
    void testIngestConversation() throws Exception {
        Conversation conv = Conversation.builder().conversationId("test-id").build();
        when(conversationService.ingestConversation(any())).thenReturn(conv);

        mockMvc.perform(post("/api/conversations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(conv)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversation_id").value("test-id"));
    }

    /**
     * Tests {@link EvaluationController#runEvaluation(String)} for full summary orchestration.
     */
    @Test
    void testRunEvaluation() throws Exception {
        String convId = "eval-test";
        EvaluationSummary summary = EvaluationSummary.builder().conversationId(convId).build();
        when(evaluationService.getEvaluationSummary(convId)).thenReturn(summary);

        mockMvc.perform(post("/api/evaluations/" + convId + "/run"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").value(convId));
    }

    /**
     * Tests {@link ConversationController#batchIngest(java.util.List)} for multi-conversation support.
     */
    @Test
    void testBatchIngest() throws Exception {
        Conversation conv = Conversation.builder().conversationId("test-id").build();
        when(conversationService.ingestBatch(any())).thenReturn(Collections.singletonList(conv));

        mockMvc.perform(post("/api/conversations/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Collections.singletonList(conv))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].conversation_id").value("test-id"));
    }

    /**
     * Tests {@link EvaluationController#submitFeedback(String, Feedback)} feedback submission.
     */
    @Test
    void testSubmitFeedback() throws Exception {
        Feedback feedback = Feedback.builder().userRating(5).build();

        mockMvc.perform(post("/api/evaluations/test-id/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(feedback)))
                .andExpect(status().isOk());
    }

    /**
     * Tests {@link EvaluationController#getMetaMetrics()} for empty state instructional message.
     */
    @Test
    void testGetMetaMetrics() throws Exception {
        when(evaluationService.getMetaEvaluationMetrics()).thenReturn(Collections.emptyMap());

        mockMvc.perform(get("/api/evaluations/meta-metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.instruction").exists());
    }

    /**
     * Tests {@link GlobalExceptionHandler} for consistent error reporting when resources are missing.
     */
    @Test
    void testNotFoundScenario() throws Exception {
        String convId = "non-existent";
        when(evaluationService.getEvaluationSummary(convId)).thenThrow(new IllegalArgumentException("Conversation not found"));

        mockMvc.perform(post("/api/evaluations/" + convId + "/run"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Conversation not found"));
    }

    /**
     * Tests {@link ConversationController#getAllConversations()} empty state message.
     */
    @Test
    void testEmptyConversationsList() throws Exception {
        when(conversationService.getAllConversations()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/conversations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.instruction").exists());
    }
}
