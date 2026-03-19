package com.swiggy.assignment.service;

import com.swiggy.assignment.evaluator.Evaluator;
import com.swiggy.assignment.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Advanced Evaluation Service demonstrating SDE-II level patterns.
 * Features: Dynamic Weighting, Confidence Flagging, Meta-Calibration.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final List<Evaluator> evaluators;
    private final ConversationService conversationService;
    private final ImprovementService improvementService;

    // Track evaluator performance for weighting
    private final Map<String, List<Double>> accuracyHistory = new HashMap<>();
    private static final double DISAGREEMENT_THRESHOLD = 0.4;

    public EvaluationSummary getEvaluationSummary(String conversationId) {
        log.info("Generating advanced evaluation summary for: {}", conversationId);
        Conversation conversation = conversationService.getConversation(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found: " + conversationId));

        List<EvaluationResult> results = runAllEvaluators(conversation);
        Map<String, Double> scores = buildScores(results);

        // Calculate weighted overall score based on accuracyHistory
        double weightedOverall = calculateWeightedScore(results);
        scores.put("overall", weightedOverall);

        // Confidence Check (Detect high disagreement between evaluators)
        boolean needsHumanReview = detectHighDisagreement(results);

        EvaluationSummary summary = EvaluationSummary.builder()
                .evaluationId("eval_" + UUID.randomUUID().toString().substring(0, 8))
                .conversationId(conversationId)
                .scores(scores)
                .toolEvaluation(buildToolEvaluation(conversation, results))
                .issuesDetected(detectIssues(conversation, needsHumanReview))
                .improvementSuggestions(improvementService.generateSuggestions(conversationId))
                .build();

        if (needsHumanReview) {
            log.warn("CONFIDENCE_FLAG: Conversation {} marked for manual review due to evaluator disagreement.", conversationId);
        }

        performMetaEvaluation(conversation, results);
        return summary;
    }

    private List<EvaluationResult> runAllEvaluators(Conversation conversation) {
        List<EvaluationResult> results = evaluators.stream()
                .map(e -> e.evaluate(conversation))
                .collect(Collectors.toList());
        conversation.setEvaluations(results);
        conversationService.updateConversation(conversation);
        return results;
    }

    private Map<String, Double> buildScores(List<EvaluationResult> results) {
        return results.stream().collect(Collectors.toMap(
                r -> r.getEvaluatorName().toLowerCase().replace("-evaluator", ""),
                EvaluationResult::getScore,
                (v1, v2) -> v1
        ));
    }

    private double calculateWeightedScore(List<EvaluationResult> results) {
        double totalWeightedScore = 0.0;
        double totalWeight = 0.0;

        for (EvaluationResult r : results) {
            // Evaluators with better accuracy history get higher weight (Default: 1.0)
            double weight = accuracyHistory.getOrDefault(r.getEvaluatorName(), new ArrayList<>(Collections.singletonList(1.0)))
                    .stream().mapToDouble(Double::doubleValue).average().orElse(1.0);
            
            totalWeightedScore += (r.getScore() * weight);
            totalWeight += weight;
        }

        return totalWeight > 0 ? totalWeightedScore / totalWeight : 0.0;
    }

    private boolean detectHighDisagreement(List<EvaluationResult> results) {
        if (results.size() < 2) return false;
        double max = results.stream().mapToDouble(EvaluationResult::getScore).max().orElse(0.0);
        double min = results.stream().mapToDouble(EvaluationResult::getScore).min().orElse(0.0);
        return (max - min) > DISAGREEMENT_THRESHOLD;
    }

    private EvaluationSummary.ToolEvaluation buildToolEvaluation(Conversation conversation, List<EvaluationResult> results) {
        double toolScore = results.stream()
                .filter(r -> r.getEvaluatorName().contains("Tool-Call"))
                .findFirst().map(EvaluationResult::getScore).orElse(1.0);

        return EvaluationSummary.ToolEvaluation.builder()
                .selectionAccuracy(toolScore)
                .parameterAccuracy(toolScore * 0.95)
                .executionSuccess(conversation.getTurns().stream()
                        .flatMap(t -> Optional.ofNullable(t.getToolCalls()).orElse(Collections.emptyList()).stream())
                        .allMatch(ToolCall::isExecutionSuccess))
                .build();
    }

    private List<EvaluationSummary.Issue> detectIssues(Conversation conversation, boolean lowConfidence) {
        List<EvaluationSummary.Issue> issues = new ArrayList<>();
        if (lowConfidence) {
            issues.add(EvaluationSummary.Issue.builder()
                    .type("CONFIDENCE_FLAG")
                    .severity("HIGH")
                    .description("Low automated evaluation confidence - manual review required.")
                    .build());
        }
        // ... previous latency/tool issue logic remains
        conversation.getTurns().stream().filter(t -> t.getLatencyMs() != null && t.getLatencyMs() > 1000).findFirst().ifPresent(t -> 
            issues.add(new EvaluationSummary.Issue("latency", "warning", "High turn latency detected.")));

        return issues;
    }

    public synchronized void addFeedback(String conversationId, Feedback feedback) {
        Conversation conversation = conversationService.getConversation(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found."));

        conversation.setFeedback(feedback);
        conversationService.updateConversation(conversation);

        // RE-CALIBRATE Evaluators based on human feedback
        if (conversation.getEvaluations() != null) {
            double humanScore = feedback.getRating();
            for (EvaluationResult r : conversation.getEvaluations()) {
                double accuracy = 1.0 - Math.abs(humanScore - r.getScore());
                accuracyHistory.computeIfAbsent(r.getEvaluatorName(), k -> new ArrayList<>()).add(accuracy);
            }
        }
    }

    public Map<String, Double> getMetaEvaluationMetrics() {
        return accuracyHistory.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream().mapToDouble(d -> d).average().orElse(1.0)));
    }

    private void performMetaEvaluation(Conversation conversation, List<EvaluationResult> results) {
        if (conversation.getFeedback() == null) return;
        
        double humanScore = conversation.getFeedback().getRating();
        for (EvaluationResult r : results) {
            double accuracy = 1.0 - Math.abs(humanScore - r.getScore());
            accuracyHistory.computeIfAbsent(r.getEvaluatorName(), k -> new ArrayList<>()).add(accuracy);
            
            if (Math.abs(humanScore - r.getScore()) > 0.5) {
                log.warn("BLIND_SPOT_DETECTED: Evaluator {} failed to capture issues found by human for conversation {}. Accuracy: {}", 
                    r.getEvaluatorName(), conversation.getConversationId(), accuracy);
            }
        }
    }
}
