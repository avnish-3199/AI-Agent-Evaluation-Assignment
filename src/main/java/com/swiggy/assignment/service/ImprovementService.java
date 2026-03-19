package com.swiggy.assignment.service;

import com.swiggy.assignment.model.Conversation;
import com.swiggy.assignment.model.ImprovementSuggestion;
import com.swiggy.assignment.model.Turn;
import com.swiggy.assignment.model.ToolCall;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Collections;

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
     * Analyzes failure patterns in prompts and tools.
     *
     * @param conversationId The ID of the conversation to analyze
     * @return A list of ImprovementSuggestion objects
     */
    public List<ImprovementSuggestion> generateSuggestions(String conversationId) {
        log.info("Generating improvement suggestions for conversation: {}", conversationId);
        Optional<Conversation> conversationOpt = conversationService.getConversation(conversationId);
        
        if (conversationOpt.isEmpty()) {
            throw new IllegalArgumentException("Conversation not found: " + conversationId);
        }

        Conversation conversation = conversationOpt.get();
        List<ImprovementSuggestion> suggestions = new ArrayList<>();

        analyzePrompts(conversation, suggestions);
        analyzeTools(conversation, suggestions);
        analyzePerformance(conversation, suggestions);

        return suggestions;
    }

    private void analyzePrompts(Conversation conversation, List<ImprovementSuggestion> suggestions) {
        // Pattern: Short assistant responses
        long shortResponses = conversation.getTurns().stream()
                .filter(t -> "assistant".equalsIgnoreCase(t.getRole()))
                .filter(t -> t.getContent() != null && t.getContent().length() < 30)
                .count();

        if (shortResponses > 0) {
            suggestions.add(ImprovementSuggestion.builder()
                    .type("prompt")
                    .suggestion("Update system prompt to include 'always provide detailed explanations'.")
                    .rationale("Identified pattern of overly brief responses which may decrease user satisfaction.")
                    .confidence(0.75)
                    .build());
        }

        // Pattern: Repeated user questions (could indicate lack of clarity)
        // This is a simple mock of the logic
        if (conversation.getTurns().size() > 6) {
             suggestions.add(ImprovementSuggestion.builder()
                    .type("prompt")
                    .suggestion("Clarify agent role in the system prompt regarding multi-turn memory.")
                    .rationale("High turn count suggests the agent might be struggling to resolve user intent efficiently.")
                    .confidence(0.65)
                    .build());
        }
    }

    private void analyzeTools(Conversation conversation, List<ImprovementSuggestion> suggestions) {
        List<ToolCall> allToolCalls = conversation.getTurns().stream()
                .flatMap(t -> Optional.ofNullable(t.getToolCalls()).orElse(Collections.emptyList()).stream())
                .toList();

        long failures = allToolCalls.stream().filter(tc -> !tc.isExecutionSuccess()).count();
        
        if (failures > 0) {
            suggestions.add(ImprovementSuggestion.builder()
                    .type("tool")
                    .suggestion("Add stricter validation for 'parameters' schema in tool definition.")
                    .rationale(String.format("Detected %d tool execution failures. Likely due to malformed parameters.", failures))
                    .confidence(0.90)
                    .build());
        }

        // Pattern: Tool calls with empty parameters
        long emptyParams = allToolCalls.stream()
                .filter(tc -> tc.getParameters() == null || tc.getParameters().isEmpty())
                .count();
        
        if (emptyParams > 0) {
            suggestions.add(ImprovementSuggestion.builder()
                    .type("tool")
                    .suggestion("Improve parameter descriptions to help the model extract values from context.")
                    .rationale("Found tool calls with missing parameters. Better descriptions reduce hallucination or omission.")
                    .confidence(0.80)
                    .build());
        }
    }

    private void analyzePerformance(Conversation conversation, List<ImprovementSuggestion> suggestions) {
        boolean hasHighLatency = conversation.getTurns().stream()
                .anyMatch(t -> t.getLatencyMs() != null && t.getLatencyMs() > 1000);
        
        if (hasHighLatency) {
            suggestions.add(ImprovementSuggestion.builder()
                    .type("latency")
                    .suggestion("Consider using a smaller model for simple turns or optimizing tool call latency.")
                    .rationale("Response latency exceeded 1000ms threshold.")
                    .confidence(0.85)
                    .build());
        }
    }
}
