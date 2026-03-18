package com.swiggy.assignment.service;

import com.swiggy.assignment.model.Conversation;
import com.swiggy.assignment.repository.InMemoryConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for handling conversation-related operations such as ingestion, retrieval, and updates.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationService {

    private final InMemoryConversationRepository repository;

    /**
     * Ingests a single conversation. Assigns a unique ID if one is not provided.
     *
     * @param conversation The conversation to ingest
     * @return The ingested conversation with its ID
     */
    public Conversation ingestConversation(Conversation conversation) {
        if (conversation.getId() == null || conversation.getId().isEmpty()) {
            conversation.setId(UUID.randomUUID().toString());
        }
        log.info("Ingesting conversation with ID: {}", conversation.getId());
        return repository.save(conversation);
    }

    /**
     * Ingests a batch of conversations.
     *
     * @param conversations The list of conversations to ingest
     * @return The list of ingested conversations
     */
    public List<Conversation> ingestBatch(List<Conversation> conversations) {
        log.info("Ingesting batch of {} conversations", conversations.size());
        conversations.forEach(this::ingestConversation);
        return conversations;
    }

    /**
     * Retrieves a conversation by its unique ID.
     *
     * @param id The ID of the conversation
     * @return An Optional containing the conversation, or empty if not found
     */
    public Optional<Conversation> getConversation(String id) {
        return repository.findById(id);
    }
    
    /**
     * Updates an existing conversation in the repository.
     *
     * @param conversation The conversation to update
     */
    public void updateConversation(Conversation conversation) {
        repository.save(conversation);
    }
}
