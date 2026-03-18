package com.swiggy.assignment.controller;

import com.swiggy.assignment.model.Conversation;
import com.swiggy.assignment.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for handling conversation-related operations.
 * Provides endpoints for ingesting single and batch conversations, as well as fetching them by ID.
 */
@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
@Tag(name = "Conversation Ingestion", description = "Endpoints for ingesting multi-turn conversation logs")
public class ConversationController {

    private final ConversationService conversationService;

    /**
     * Ingests a single conversation.
     *
     * @param conversation The conversation object to be ingested
     * @return ResponseEntity containing the ingested conversation
     */
    @PostMapping
    @Operation(summary = "Ingest a single conversation")
    public ResponseEntity<Conversation> ingestConversation(@RequestBody Conversation conversation) {
        return ResponseEntity.ok(conversationService.ingestConversation(conversation));
    }

    /**
     * Ingests multiple conversations in a single batch.
     *
     * @param conversations List of conversation objects to be ingested
     * @return ResponseEntity containing the list of ingested conversations
     */
    @PostMapping("/batch")
    @Operation(summary = "Ingest multiple conversations in batch")
    public ResponseEntity<List<Conversation>> ingestBatch(@RequestBody List<Conversation> conversations) {
        return ResponseEntity.ok(conversationService.ingestBatch(conversations));
    }

    /**
     * Retrieves a specific conversation by its unique ID.
     *
     * @param id The unique identifier of the conversation
     * @return ResponseEntity containing the requested conversation, or 404 if not found
     */
    @GetMapping("/{id}")
    @Operation(summary = "Fetch a conversation by ID")
    public ResponseEntity<Conversation> getConversation(@PathVariable String id) {
        return conversationService.getConversation(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
