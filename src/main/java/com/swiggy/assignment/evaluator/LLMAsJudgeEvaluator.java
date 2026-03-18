package com.swiggy.assignment.evaluator;

import com.swiggy.assignment.model.Conversation;
import com.swiggy.assignment.model.EvaluationResult;
import com.swiggy.assignment.model.Turn;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Evaluator that mocks an "LLM-as-Judge" approach using heuristics.
 * It assesses conversation quality based on assistant response length and specific keywords.
 */
@Slf4j
@Component
public class LLMAsJudgeEvaluator implements Evaluator {

    /**
     * Returns the name of the evaluator.
     *
     * @return "LLM-as-Judge"
     */
    @Override
    public String getName() {
        return "LLM-as-Judge";
    }

    /**
     * Evaluates the conversation using heuristics to simulate an LLM judge.
     *
     * @param conversation The conversation to be evaluated
     * @return EvaluationResult containing a score between 0.0 and 1.0 and comments
     */
    @Override
    public EvaluationResult evaluate(Conversation conversation) {
        log.info("Running LLM-as-Judge evaluator for conversation: {}", conversation.getId());
        List<Turn> turns = conversation.getTurns();
        if (turns == null || turns.isEmpty()) {
            return buildResult(0.0, "No turns found in conversation.");
        }

        // Mock heuristic: Reward longer assistant responses that don't just repeat user input
        double totalScore = 0.0;
        int assistantTurns = 0;

        for (int i = 0; i < turns.size(); i++) {
            Turn turn = turns.get(i);
            if ("assistant".equalsIgnoreCase(turn.getRole())) {
                assistantTurns++;
                double turnScore = 0.5; // Base score

                String content = turn.getContent() != null ? turn.getContent() : "";
                
                // Heuristic: Quality based on length
                if (content.length() > 50) turnScore += 0.2;
                if (content.length() > 150) turnScore += 0.2;
                
                // Heuristic: Penalty for too short
                if (content.length() < 10) turnScore -= 0.3;

                // Heuristic: Check for common "I don't know" or "I'm sorry" (mocking quality)
                if (content.toLowerCase().contains("sorry") || content.toLowerCase().contains("don't know")) {
                    turnScore -= 0.2;
                }

                totalScore += Math.max(0.0, Math.min(1.0, turnScore));
            }
        }

        double finalScore = assistantTurns > 0 ? totalScore / assistantTurns : 0.5;
        
        return buildResult(finalScore, String.format("Evaluated %d assistant turns using length and keyword heuristics.", assistantTurns));
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
