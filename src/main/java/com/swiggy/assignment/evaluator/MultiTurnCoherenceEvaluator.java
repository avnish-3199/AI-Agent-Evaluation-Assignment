package com.swiggy.assignment.evaluator;

import com.swiggy.assignment.model.Conversation;
import com.swiggy.assignment.model.EvaluationResult;
import com.swiggy.assignment.model.Turn;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Evaluator that assesses the coherence of a multi-turn conversation.
 * It looks for keyword overlap between user and assistant turns as a proxy for coherence.
 */
@Slf4j
@Component
public class MultiTurnCoherenceEvaluator implements Evaluator {

    /**
     * Returns the name of the evaluator.
     *
     * @return "Multi-turn-Coherence-Evaluator"
     */
    @Override
    public String getName() {
        return "Multi-turn-Coherence-Evaluator";
    }

    /**
     * Evaluates the coherence of the conversation by analyzing transitions between user and assistant turns.
     *
     * @param conversation The conversation to evaluate
     * @return EvaluationResult containing the coherence score and comments
     */
    @Override
    public EvaluationResult evaluate(Conversation conversation) {
        log.info("Running MultiTurnCoherenceEvaluator for conversation: {}", conversation.getId());
        List<Turn> turns = conversation.getTurns();
        if (turns == null || turns.size() < 2) {
            return buildResult(1.0, "Not enough turns to evaluate coherence.");
        }

        double coherenceScore = 0.8; // Base score for multi-turn
        int inconsistencies = 0;

        for (int i = 1; i < turns.size(); i++) {
            Turn currentTurn = turns.get(i);
            Turn previousTurn = turns.get(i - 1);

            // Simple heuristic: If assistant follows user and uses some keywords from user's turn
            if ("assistant".equalsIgnoreCase(currentTurn.getRole()) && "user".equalsIgnoreCase(previousTurn.getRole())) {
                String prevContent = previousTurn.getContent() != null ? previousTurn.getContent().toLowerCase() : "";
                String currentContent = currentTurn.getContent() != null ? currentTurn.getContent().toLowerCase() : "";

                // Check for some word overlap as a proxy for coherence
                boolean hasOverlap = false;
                String[] words = prevContent.split("\\s+");
                int overlapCount = 0;
                for (String word : words) {
                    if (word.length() > 3 && currentContent.contains(word)) {
                        overlapCount++;
                        hasOverlap = true;
                    }
                }
                
                if (hasOverlap) coherenceScore += 0.05;
                else coherenceScore -= 0.1;
            }
        }

        return buildResult(Math.max(0.0, Math.min(1.0, coherenceScore)), 
                String.format("Evaluated coherence based on turn transitions and keyword overlap."));
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
