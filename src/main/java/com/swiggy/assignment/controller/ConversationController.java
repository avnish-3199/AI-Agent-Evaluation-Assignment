package com.swiggy.assignment.controller;

import com.swiggy.assignment.model.Conversation;
import com.swiggy.assignment.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for managing conversations.
 * Supports individual and batch ingestion.
 */
@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    /**
     * Ingests a single conversation.
     *
     * @param conversation The conversation object to ingest
     * @return ResponseEntity containing the ingested conversation
     */
    @PostMapping
    public ResponseEntity<Conversation> ingestConversation(@RequestBody Conversation conversation) {
        Conversation savedConversation = conversationService.ingestConversation(conversation);
        return ResponseEntity.ok(savedConversation);
    }

    /**
     * Batch ingest multiple conversations.
     *
     * @param conversations List of conversations to ingest
     * @return ResponseEntity containing the list of ingested conversations
     */
    @PostMapping("/batch")
    public ResponseEntity<List<Conversation>> batchIngest(@RequestBody List<Conversation> conversations) {
        List<Conversation> savedConversations = conversationService.ingestBatch(conversations);
        return ResponseEntity.ok(savedConversations);
    }

    /**
     * Retrieves a conversation by its ID.
     *
     * @param id The ID of the conversation
     * @return ResponseEntity containing the conversation, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Conversation> getConversation(@PathVariable String id) {
        Optional<Conversation> conversation = conversationService.getConversation(id);
        return conversation.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves all ingested conversations.
     * If no data is present, returns an instructional message.
     *
     * @return ResponseEntity containing a list of all conversations or an instructional message
     */
    @GetMapping
    public ResponseEntity<?> getAllConversations() {
        List<Conversation> conversations = conversationService.getAllConversations();
        if (conversations.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "No conversations found in the system.");
            response.put("instruction", "Please ingest data by hitting the POST /api/conversations or POST /api/conversations/batch endpoint first.");
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.ok(conversations);
    }
}
