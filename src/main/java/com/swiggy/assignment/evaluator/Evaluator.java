package com.swiggy.assignment.evaluator;

import com.swiggy.assignment.model.Conversation;
import com.swiggy.assignment.model.EvaluationResult;

/**
 * Interface for conversation evaluators.
 * Implementations of this interface provide different metrics or methods to evaluate a conversation.
 */
public interface Evaluator {
    /**
     * Returns the unique name of the evaluator.
     *
     * @return The name of the evaluator
     */
    String getName();

    /**
     * Evaluates a given conversation and returns the result.
     *
     * @param conversation The conversation to be evaluated
     * @return The evaluation result containing score and comments
     */
    EvaluationResult evaluate(Conversation conversation);
}
