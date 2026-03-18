package com.swiggy.assignment.evaluator;

import com.swiggy.assignment.model.Conversation;
import com.swiggy.assignment.model.EvaluationResult;
import com.swiggy.assignment.model.ToolCall;
import com.swiggy.assignment.model.Turn;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Evaluator that focuses on the quality and success of tool calls within a conversation.
 * It calculates a score based on the success rate of tool executions and the presence of required parameters.
 */
@Slf4j
@Component
public class ToolCallEvaluator implements Evaluator {

    /**
     * Returns the name of the evaluator.
     *
     * @return "Tool-Call-Evaluator"
     */
    @Override
    public String getName() {
        return "Tool-Call-Evaluator";
    }

    /**
     * Evaluates the conversation based on the success and quality of tool calls.
     *
     * @param conversation The conversation to evaluate
     * @return EvaluationResult containing the tool call score and comments
     */
    @Override
    public EvaluationResult evaluate(Conversation conversation) {
        log.info("Running ToolCallEvaluator for conversation: {}", conversation.getId());
        List<Turn> turns = conversation.getTurns();
        if (turns == null || turns.isEmpty()) {
            return buildResult(1.0, "No turns to evaluate.");
        }

        int totalToolCalls = 0;
        int successfulToolCalls = 0;
        int missingParamsCalls = 0;

        for (Turn turn : turns) {
            List<ToolCall> toolCalls = turn.getToolCalls();
            if (toolCalls != null && !toolCalls.isEmpty()) {
                totalToolCalls += toolCalls.size();
                for (ToolCall tc : toolCalls) {
                    if (tc.isExecutionSuccess()) {
                        successfulToolCalls++;
                    }
                    if (tc.getParameters() == null || tc.getParameters().isEmpty()) {
                        missingParamsCalls++;
                    }
                }
            }
        }

        if (totalToolCalls == 0) {
            return buildResult(1.0, "No tool calls found in the conversation.");
        }

        double score = (double) successfulToolCalls / totalToolCalls;
        // Apply penalty for missing params
        score -= (double) missingParamsCalls / totalToolCalls * 0.2;
        
        return buildResult(Math.max(0.0, score), String.format("Found %d tool calls. Successes: %d. Missing params: %d.", totalToolCalls, successfulToolCalls, missingParamsCalls));
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
