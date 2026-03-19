package com.swiggy.assignment.service;

import com.swiggy.assignment.evaluator.Evaluator;
import com.swiggy.assignment.model.*;
import com.swiggy.assignment.repository.InMemoryConversationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Service-level tests for orchestration and business logic.
 * Tests cover ingestion, evaluation summary generation, improvement suggestions, and meta-calibration.
 */
class ServiceTests {

    private ConversationService conversationService;
    private EvaluationService evaluationService;
    private ImprovementService improvementService;

    @Mock
    private InMemoryConversationRepository repository;

    @Mock
    private Evaluator mockEvaluator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        conversationService = new ConversationService(repository);
        improvementService = new ImprovementService(conversationService);
        evaluationService = new EvaluationService(
                Collections.singletonList(mockEvaluator),
                conversationService,
                improvementService
        );
    }

    /**
     * Tests real-time and batch ingestion with repository interactions.
     */
    @Test
    void testConversationIngestionAndBatch() {
        Conversation conv = Conversation.builder().conversationId("test-1").build();
        when(repository.save(any())).thenReturn(conv);

        Conversation result = conversationService.ingestConversation(conv);
        assertEquals("test-1", result.getConversationId());

        Conversation convWithoutId = new Conversation();
        conversationService.ingestConversation(convWithoutId);
        assertNotNull(convWithoutId.getConversationId());

        List<Conversation> batch = Arrays.asList(conv, conv);
        List<Conversation> batchResult = conversationService.ingestBatch(batch);
        assertEquals(2, batchResult.size());
        
        when(repository.findAll()).thenReturn(batch);
        assertEquals(2, conversationService.getAllConversations().size());
        
        verify(repository, atLeast(2)).save(any());
    }

    /**
     * Tests {@link EvaluationService} orchestration and issue detection like high latency.
     */
    @Test
    void testEvaluationOrchestrationWithConfidenceFlag() {
        String convId = "test-eval";
        Conversation conv = Conversation.builder()
                .conversationId(convId)
                .turns(Collections.singletonList(Turn.builder().latencyMs(1500L).build()))
                .build();
        
        when(repository.findById(convId)).thenReturn(Optional.of(conv));
        when(mockEvaluator.getName()).thenReturn("Mock-Evaluator");
        when(mockEvaluator.evaluate(any())).thenReturn(new EvaluationResult("Mock-Evaluator", 0.9, "Good", 123L));

        EvaluationSummary summary = evaluationService.getEvaluationSummary(convId);

        assertNotNull(summary);
        assertEquals(0.9, summary.getScores().get("mock"));
        assertTrue(summary.getIssuesDetected().stream().anyMatch(i -> i.getType().equals("latency")));
    }

    /**
     * Tests the CONFIDENCE_FLAG mechanism when evaluators significantly disagree.
     */
    @Test
    void testEvaluationDivergence() {
        String convId = "test-diverge";
        Conversation conv = Conversation.builder().conversationId(convId).turns(new ArrayList<>()).build();
        
        Evaluator e1 = mock(Evaluator.class);
        when(e1.getName()).thenReturn("E1");
        when(e1.evaluate(any())).thenReturn(new EvaluationResult("E1", 0.1, "Low", 0L));
        
        Evaluator e2 = mock(Evaluator.class);
        when(e2.getName()).thenReturn("E2");
        when(e2.evaluate(any())).thenReturn(new EvaluationResult("E2", 0.9, "High", 0L));
        
        EvaluationService service = new EvaluationService(Arrays.asList(e1, e2), conversationService, improvementService);
        
        when(repository.findById(convId)).thenReturn(Optional.of(conv));
        
        EvaluationSummary summary = service.getEvaluationSummary(convId);
        // overall should be average (0.1 + 0.9) / 2 = 0.5
        assertEquals(0.5, summary.getScores().get("overall"), 0.01);
        // High divergence (0.8 difference) should trigger flag
        assertTrue(summary.getIssuesDetected().stream().anyMatch(i -> i.getType().equals("CONFIDENCE_FLAG")));
    }

    /**
     * Tests the {@link ImprovementService} for detecting latency, prompt, and tool patterns.
     */
    @Test
    void testImprovementSuggestionsAllPatterns() {
        String convId = "test-improve-all";
        List<Turn> turns = new ArrayList<>();
        turns.add(Turn.builder().latencyMs(2000L).role("user").content("hi").build());
        turns.add(Turn.builder().role("assistant").content("ok").build());
        turns.add(Turn.builder().role("assistant").toolCalls(Collections.singletonList(
                ToolCall.builder().toolName("t1").executionSuccess(false).build())).build());
        turns.add(Turn.builder().role("assistant").toolCalls(Collections.singletonList(
                ToolCall.builder().toolName("t2").parameters(new HashMap<>()).build())).build());

        Conversation conv = Conversation.builder().conversationId(convId).turns(turns).build();
        when(repository.findById(convId)).thenReturn(Optional.of(conv));

        List<ImprovementSuggestion> suggestions = improvementService.generateSuggestions(convId);
        assertTrue(suggestions.stream().anyMatch(s -> s.getType().equals("latency")));
        assertTrue(suggestions.stream().anyMatch(s -> s.getType().equals("prompt")));
        assertTrue(suggestions.stream().anyMatch(s -> s.getType().equals("tool")));
    }

    /**
     * Tests meta-evaluation flywheel: how evaluator accuracy is tracked against human feedback.
     */
    @Test
    void testMetaEvaluationBlindSpot() {
        String convId = "blind-spot";
        Conversation conv = Conversation.builder()
                .conversationId(convId)
                .evaluations(Collections.singletonList(new EvaluationResult("E1", 0.9, "High", 0L)))
                .build();
        
        when(repository.findById(convId)).thenReturn(Optional.of(conv));

        // Human rates low (0.2), while evaluator scored high (0.9)
        Feedback feedback = Feedback.builder().userRating(1).build(); 
        evaluationService.addFeedback(convId, feedback);

        Map<String, Double> metrics = evaluationService.getMetaEvaluationMetrics();
        assertEquals(0.3, metrics.get("E1"), 0.01); // 1.0 - |0.9 - 0.2| = 0.3 accuracy
    }

    /**
     * Tests meta-evaluation flywheel: feedback provided BEFORE evaluation.
     */
    @Test
    void testMetaEvaluationFeedbackBeforeEvaluation() {
        String convId = "feedback-before";
        Conversation conv = Conversation.builder()
                .conversationId(convId)
                .turns(new ArrayList<>())
                .feedback(Feedback.builder().userRating(1).build()) // 0.2 rating
                .build();
        
        when(repository.findById(convId)).thenReturn(Optional.of(conv));
        when(mockEvaluator.getName()).thenReturn("E1");
        when(mockEvaluator.evaluate(any())).thenReturn(new EvaluationResult("E1", 0.9, "High", 0L));

        // Trigger evaluation
        evaluationService.getEvaluationSummary(convId);

        Map<String, Double> metrics = evaluationService.getMetaEvaluationMetrics();
        assertFalse(metrics.isEmpty(), "Metrics should not be empty");
        assertEquals(0.3, metrics.get("E1"), 0.01); // 1.0 - |0.9 - 0.2| = 0.3 accuracy
    }
}
