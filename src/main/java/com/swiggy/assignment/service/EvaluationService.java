package com.swiggy.assignment.service;

import com.swiggy.assignment.evaluator.Evaluator;
import com.swiggy.assignment.model.Conversation;
import com.swiggy.assignment.model.EvaluationResult;
import com.swiggy.assignment.model.Feedback;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing conversation evaluations and user feedback.
 * Coordinates multiple evaluators and calculates meta-evaluation calibration.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final List<Evaluator> evaluators;
    private final ConversationService conversationService;

    /**
     * Runs all registered evaluators for a specific conversation.
     * Updates the conversation with the evaluation results and performs meta-evaluation calibration.
     *
     * @param conversationId The ID of the conversation to evaluate
     * @return A list of EvaluationResult objects
     * @throws IllegalArgumentException if the conversation with the given ID is not found
     */
    public List<EvaluationResult> runEvaluation(String conversationId) {
        log.info("Starting evaluation for conversation: {}", conversationId);
        Optional<Conversation> conversationOpt = conversationService.getConversation(conversationId);
        
        if (conversationOpt.isEmpty()) {
            throw new IllegalArgumentException("Conversation not found: " + conversationId);
        }

        Conversation conversation = conversationOpt.get();
        List<EvaluationResult> results = new ArrayList<>();

        for (Evaluator evaluator : evaluators) {
            EvaluationResult result = evaluator.evaluate(conversation);
            results.add(result);
        }

        conversation.setEvaluations(results);
        conversationService.updateConversation(conversation);
        
        // Meta-evaluation: Compare with user feedback if available
        performMetaEvaluation(conversation, results);

        return results;
    }

    /**
     * Adds user feedback or rating to a specific conversation.
     *
     * @param conversationId The ID of the conversation
     * @param feedback The feedback object containing ratings and comments
     * @throws IllegalArgumentException if the conversation with the given ID is not found
     */
    public void addFeedback(String conversationId, Feedback feedback) {
        log.info("Adding feedback for conversation: {}", conversationId);
        Optional<Conversation> conversationOpt = conversationService.getConversation(conversationId);
        
        if (conversationOpt.isEmpty()) {
            throw new IllegalArgumentException("Conversation not found: " + conversationId);
        }

        Conversation conversation = conversationOpt.get();
        conversation.setFeedback(feedback);
        conversationService.updateConversation(conversation);
    }

    private void performMetaEvaluation(Conversation conversation, List<EvaluationResult> results) {
        if (conversation.getFeedback() == null) {
            return;
        }

        double userRating = conversation.getFeedback().getRating();
        double avgEvaluatorScore = results.stream()
                .mapToDouble(EvaluationResult::getScore)
                .average()
                .orElse(0.0);

        if (Math.abs(userRating - avgEvaluatorScore) > 0.3) {
            log.warn("META-EVALUATION CALIBRATION WARNING: Conversation {} has high disagreement. " +
                    "Evaluators avg: {}, User rating: {}", conversation.getId(), avgEvaluatorScore, userRating);
        }
    }
}
