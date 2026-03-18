package com.swiggy.assignment.controller;

import com.swiggy.assignment.model.EvaluationResult;
import com.swiggy.assignment.model.Feedback;
import com.swiggy.assignment.model.ImprovementSuggestion;
import com.swiggy.assignment.service.ConversationService;
import com.swiggy.assignment.service.EvaluationService;
import com.swiggy.assignment.service.ImprovementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing conversation evaluations and improvement suggestions.
 * Provides endpoints to trigger evaluations, submit feedback, and retrieve results and suggestions.
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Evaluation & Suggestions", description = "Endpoints for evaluating conversations and generating suggestions")
public class EvaluationController {

    private final EvaluationService evaluationService;
    private final ImprovementService improvementService;
    private final ConversationService conversationService;

    /**
     * Triggers all registered evaluators for a specific conversation.
     *
     * @param conversationId The ID of the conversation to evaluate
     * @return ResponseEntity containing the list of evaluation results
     */
    @PostMapping("/evaluate/{conversationId}")
    @Operation(summary = "Run all evaluators for a conversation")
    public ResponseEntity<List<EvaluationResult>> evaluate(@PathVariable String conversationId) {
        return ResponseEntity.ok(evaluationService.runEvaluation(conversationId));
    }

    /**
     * Adds user feedback or rating to a conversation.
     *
     * @param conversationId The ID of the conversation to provide feedback for
     * @param feedback The feedback object containing ratings and comments
     * @return ResponseEntity with 202 Accepted status
     */
    @PostMapping("/feedback/{conversationId}")
    @Operation(summary = "Submit user feedback/rating for a conversation")
    public ResponseEntity<Void> addFeedback(@PathVariable String conversationId, @RequestBody Feedback feedback) {
        evaluationService.addFeedback(conversationId, feedback);
        return ResponseEntity.accepted().build();
    }

    /**
     * Retrieves the evaluation results for a specific conversation.
     *
     * @param conversationId The ID of the conversation
     * @return ResponseEntity containing the list of evaluation results, or 404 if not found
     */
    @GetMapping("/results/{conversationId}")
    @Operation(summary = "Fetch evaluation results for a conversation")
    public ResponseEntity<List<EvaluationResult>> getResults(@PathVariable String conversationId) {
        return conversationService.getConversation(conversationId)
                .map(conversation -> ResponseEntity.ok(conversation.getEvaluations()))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Generates improvement suggestions for a conversation based on its evaluation results.
     *
     * @param conversationId The ID of the conversation
     * @return ResponseEntity containing the list of improvement suggestions
     */
    @GetMapping("/suggestions/{conversationId}")
    @Operation(summary = "Fetch improvement suggestions based on evaluation results")
    public ResponseEntity<List<ImprovementSuggestion>> getSuggestions(@PathVariable String conversationId) {
        return ResponseEntity.ok(improvementService.generateSuggestions(conversationId));
    }
}
