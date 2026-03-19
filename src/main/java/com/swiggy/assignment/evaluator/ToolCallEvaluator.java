package com.swiggy.assignment.evaluator;

import com.swiggy.assignment.model.Conversation;
import com.swiggy.assignment.model.EvaluationResult;
import com.swiggy.assignment.model.ToolCall;
import com.swiggy.assignment.model.Turn;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Evaluator that focuses on the quality and success of tool calls within a conversation.
 * It calculates a score based on success rate, parameter accuracy, and hallucination detection.
 */
@Slf4j
@Component
public class ToolCallEvaluator implements Evaluator {

    @Override
    public String getName() {
        return "Tool-Call-Evaluator";
    }

    @Override
    public EvaluationResult evaluate(Conversation conversation) {
        log.info("Running ToolCallEvaluator for conversation: {}", conversation.getId());
        List<Turn> turns = conversation.getTurns();
        if (turns == null || turns.isEmpty()) {
            return buildResult(1.0, "No turns to evaluate.");
        }

        double totalCalls = 0;
        double successfulCalls = 0;
        double hallucinatedCalls = 0;

        for (int i = 0; i < turns.size(); i++) {
            Turn turn = turns.get(i);
            List<ToolCall> toolCalls = turn.getToolCalls();
            
            if (toolCalls != null && !toolCalls.isEmpty()) {
                totalCalls += toolCalls.size();
                
                // Get previous user content to check for hallucinations
                String context = "";
                if (i > 0) {
                    context = turns.get(i-1).getContent() != null ? turns.get(i-1).getContent().toLowerCase() : "";
                }

                for (ToolCall tc : toolCalls) {
                    if (tc.isExecutionSuccess()) {
                        successfulCalls++;
                    }
                    
                    // Simple hallucination check: are parameter values present in context?
                    if (tc.getParameters() != null) {
                        for (Object value : tc.getParameters().values()) {
                            if (value instanceof String valStr) {
                                if (!context.contains(valStr.toLowerCase()) && valStr.length() > 2) {
                                    hallucinatedCalls += 0.5; // Partial penalty
                                }
                            }
                        }
                    }
                }
            }
        }

        if (totalCalls == 0) {
            return buildResult(1.0, "No tool calls found.");
        }

        double score = (successfulCalls - hallucinatedCalls) / totalCalls;
        return buildResult(Math.max(0.0, score), 
                String.format("Evaluated %d tool calls. Success rate: %.2f. Hallucination penalty: %.2f.", 
                        (int)totalCalls, successfulCalls/totalCalls, hallucinatedCalls/totalCalls));
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
