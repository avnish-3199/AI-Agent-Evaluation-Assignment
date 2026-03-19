package com.swiggy.assignment.evaluator;

import com.swiggy.assignment.model.Conversation;
import com.swiggy.assignment.model.EvaluationResult;
import com.swiggy.assignment.model.Turn;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Evaluator that uses simple heuristics like latency and response content to score conversations.
 */
@Slf4j
@Component
public class HeuristicEvaluator implements Evaluator {

    private static final long LATENCY_THRESHOLD_MS = 2000;

    /**
     * Returns the name of the evaluator.
     *
     * @return "Heuristic-Evaluator"
     */
    @Override
    public String getName() {
        return "Heuristic-Evaluator";
    }

    /**
     * Evaluates the conversation based on latency and content emptiness of assistant turns.
     *
     * @param conversation The conversation to evaluate
     * @return EvaluationResult containing the heuristic score and comments
     */
    @Override
    public EvaluationResult evaluate(Conversation conversation) {
        log.info("Running HeuristicEvaluator for conversation: {}", conversation.getId());
        List<Turn> turns = conversation.getTurns();
        if (turns == null || turns.isEmpty()) {
            return buildResult(1.0, "No turns found.");
        }

        int slowTurns = 0;
        int assistantTurns = 0;
        int emptyTurns = 0;

        for (Turn turn : turns) {
            if ("assistant".equalsIgnoreCase(turn.getRole())) {
                assistantTurns++;
                if (turn.getLatencyMs() != null && turn.getLatencyMs() > LATENCY_THRESHOLD_MS) {
                    slowTurns++;
                }
                if (turn.getContent() == null || turn.getContent().trim().isEmpty()) {
                    emptyTurns++;
                }
            }
        }

        double score = 1.0;
        if (assistantTurns > 0) {
            score -= (double) slowTurns / assistantTurns * 0.4;
            score -= (double) emptyTurns / assistantTurns * 0.5;
        }

        return buildResult(Math.max(0.0, score), String.format("Analyzed %d assistant turns. Slow turns: %d, Empty turns: %d.", assistantTurns, slowTurns, emptyTurns));
    }

    private EvaluationResult buildResult(double score, String comments) {
        return EvaluationResult.builder()
                .evaluatorName(getName())
                .score(score)
                .comments(comments)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
