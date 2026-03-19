package com.swiggy.assignment.evaluator;

import com.swiggy.assignment.model.Conversation;
import com.swiggy.assignment.model.EvaluationResult;
import com.swiggy.assignment.model.ToolCall;
import com.swiggy.assignment.model.Turn;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the modular evaluation framework.
 * Tests cover Heuristic, LLM-as-Judge, Tool-Call, and Multi-turn Coherence evaluators.
 */
class EvaluatorTests {

    /**
     * Tests {@link HeuristicEvaluator} for latency thresholds and empty response detection.
     */
    @Test
    void testHeuristicEvaluator() {
        HeuristicEvaluator evaluator = new HeuristicEvaluator();
        
        // Scenario 1: Empty turns (should handle gracefully)
        Conversation conv1 = Conversation.builder().conversationId("1").turns(new ArrayList<>()).build();
        assertEquals(1.0, evaluator.evaluate(conv1).getScore());

        // Scenario 2: Slow and empty turns (should penalize score)
        List<Turn> turns = new ArrayList<>();
        turns.add(Turn.builder().role("assistant").latencyMs(3000L).content("").build()); 
        Conversation conv2 = Conversation.builder().conversationId("2").turns(turns).build();
        
        EvaluationResult result = evaluator.evaluate(conv2);
        assertTrue(result.getScore() < 0.5);
        assertTrue(result.getComments().contains("Slow turns: 1"));
    }

    /**
     * Tests {@link LLMAsJudgeEvaluator} using response length and keyword heuristics.
     */
    @Test
    void testLLMAsJudgeEvaluator() {
        LLMAsJudgeEvaluator evaluator = new LLMAsJudgeEvaluator();

        // Scenario 1: Detailed response (High score)
        List<Turn> turns = new ArrayList<>();
        turns.add(Turn.builder().role("assistant").content("This is a very detailed and helpful response that should score highly based on its length and lack of negative keywords.").build());
        Conversation conv = Conversation.builder().turns(turns).build();

        EvaluationResult result = evaluator.evaluate(conv);
        assertTrue(result.getScore() > 0.6);

        // Scenario 2: Poor response with apology keywords (Low score)
        turns.clear();
        turns.add(Turn.builder().role("assistant").content("sorry").build());
        result = evaluator.evaluate(conv);
        assertTrue(result.getScore() < 0.4);
    }

    /**
     * Tests {@link ToolCallEvaluator} for parameter accuracy and hallucination detection.
     */
    @Test
    void testToolCallEvaluator() {
        ToolCallEvaluator evaluator = new ToolCallEvaluator();

        // Scenario: Hallucination detection (Parameter value not present in previous user turn)
        List<Turn> turns = new ArrayList<>();
        turns.add(Turn.builder().role("user").content("Book flight to NYC").build());
        
        ToolCall tc = ToolCall.builder()
                .toolName("book")
                .parameters(Map.of("date", "2024-05-01")) 
                .executionSuccess(true)
                .build();
        
        turns.add(Turn.builder().role("assistant").toolCalls(Collections.singletonList(tc)).build());
        Conversation conv = Conversation.builder().turns(turns).build();

        EvaluationResult result = evaluator.evaluate(conv);
        // Score should be penalized for hallucination
        assertEquals(0.5, result.getScore(), 0.01);
    }

    /**
     * Tests {@link MultiTurnCoherenceEvaluator} for context maintenance using keyword overlap.
     */
    @Test
    void testMultiTurnCoherenceEvaluator() {
        MultiTurnCoherenceEvaluator evaluator = new MultiTurnCoherenceEvaluator();

        // Scenario 1: Coherent turns with overlapping context
        List<Turn> turns = new ArrayList<>();
        turns.add(Turn.builder().role("user").content("I want pizza").build());
        turns.add(Turn.builder().role("assistant").content("I can help you order pizza.").build());
        Conversation conv = Conversation.builder().turns(turns).build();

        EvaluationResult result = evaluator.evaluate(conv);
        assertTrue(result.getScore() > 0.8);

        // Scenario 2: Incoherent turns (no context maintenance)
        turns.set(1, Turn.builder().role("assistant").content("The weather is nice.").build());
        result = evaluator.evaluate(conv);
        assertTrue(result.getScore() < 0.8);
    }
}
