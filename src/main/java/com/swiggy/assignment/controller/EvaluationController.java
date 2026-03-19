package com.swiggy.assignment.controller;

import com.swiggy.assignment.model.EvaluationSummary;
import com.swiggy.assignment.model.Feedback;
import com.swiggy.assignment.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for handling conversation evaluations and feedback.
 */
@RestController
@RequestMapping("/api/evaluations")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    /**
     * Triggers the evaluation of a conversation and returns a consolidated summary.
     *
     * @param conversationId The ID of the conversation to evaluate
     * @return ResponseEntity containing the EvaluationSummary
     */
    @PostMapping("/{conversationId}/run")
    public ResponseEntity<EvaluationSummary> runEvaluation(@PathVariable String conversationId) {
        EvaluationSummary summary = evaluationService.getEvaluationSummary(conversationId);
        return ResponseEntity.ok(summary);
    }

    /**
     * Submits user feedback or human annotations for a conversation.
     *
     * @param conversationId The ID of the conversation
     * @param feedback The feedback object
     * @return ResponseEntity with status 200 OK
     */
    @PostMapping("/{conversationId}/feedback")
    public ResponseEntity<Void> submitFeedback(@PathVariable String conversationId, @RequestBody Feedback feedback) {
        evaluationService.addFeedback(conversationId, feedback);
        return ResponseEntity.ok().build();
    }

    /**
     * Retrieves meta-evaluation metrics (evaluator accuracy against feedback).
     * If no metrics are available, returns an instructional message.
     *
     * @return ResponseEntity containing a map of evaluator names or an instructional message
     */
    @GetMapping("/meta-metrics")
    public ResponseEntity<?> getMetaMetrics() {
        Map<String, Double> metrics = evaluationService.getMetaEvaluationMetrics();
        if (metrics.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "No meta-evaluation metrics available.");
            response.put("instruction", "Meta-metrics are generated after submitting feedback using POST /api/evaluations/{id}/feedback and running an evaluation.");
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.ok(metrics);
    }
}
