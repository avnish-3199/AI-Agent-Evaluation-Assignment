package com.swiggy.assignment.service;

import com.swiggy.assignment.model.Conversation;
import com.swiggy.assignment.model.ImprovementSuggestion;
import com.swiggy.assignment.model.Turn;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for generating improvement suggestions for conversations based on evaluation results and heuristics.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImprovementService {

    private final ConversationService conversationService;

    /**
     * Generates a list of improvement suggestions for a specific conversation.
     * Heuristics include checking for high latency, tool execution failures, and short responses.
     *
     * @param conversationId The ID of the conversation to analyze
     * @return A list of ImprovementSuggestion objects
     * @throws IllegalArgumentException if the conversation with the given ID is not found
     */
    public List<ImprovementSuggestion> generateSuggestions(String conversationId) {
        log.info("Generating improvement suggestions for conversation: {}", conversationId);
        Optional<Conversation> conversationOpt = conversationService.getConversation(conversationId);
        
        if (conversationOpt.isEmpty()) {
            throw new IllegalArgumentException("Conversation not found: " + conversationId);
        }

        Conversation conversation = conversationOpt.get();
        List<ImprovementSuggestion> suggestions = new ArrayList<>();

        // 1. Check for high latency
        boolean hasHighLatency = conversation.getTurns().stream()
                .anyMatch(t -> t.getLatencyMs() > 2000);
        
        if (hasHighLatency) {
            suggestions.add(ImprovementSuggestion.builder()
                    .type("LATENCY_OPTIMIZATION")
                    .suggestion("Consider optimizing model inference or reducing context length.")
                    .rationale("Multiple turns exceeded the 2000ms latency threshold.")
                    .confidenceScore(0.85)
                    .build());
        }

        // 2. Check for tool failures
        long toolFailures = conversation.getTurns().stream()
                .filter(t -> t.getToolCalls() != null)
                .flatMap(t -> t.getToolCalls().stream())
                .filter(tc -> !tc.isExecutionSuccess())
                .count();

        if (toolFailures > 0) {
            suggestions.add(ImprovementSuggestion.builder()
                    .type("TOOL_VALIDATION")
                    .suggestion("Implement stricter input validation before tool execution.")
                    .rationale(String.format("Found %d tool execution failures.", toolFailures))
                    .confidenceScore(0.95)
                    .build());
        }

        // 3. Check for short responses (from LLM-as-Judge logic)
        long shortResponses = conversation.getTurns().stream()
                .filter(t -> "assistant".equalsIgnoreCase(t.getRole()))
                .filter(t -> t.getContent() != null && t.getContent().length() < 20)
                .count();

        if (shortResponses > 0) {
            suggestions.add(ImprovementSuggestion.builder()
                    .type("PROMPT_OPTIMIZATION")
                    .suggestion("Update system prompt to encourage more detailed and informative responses.")
                    .rationale(String.format("Found %d assistant turns with very short content.", shortResponses))
                    .confidenceScore(0.75)
                    .build());
        }

        return suggestions;
    }
}
