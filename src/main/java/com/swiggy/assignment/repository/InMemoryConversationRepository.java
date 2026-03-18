package com.swiggy.assignment.repository;

import com.swiggy.assignment.model.Conversation;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory repository for storing and retrieving conversations.
 * Uses a ConcurrentHashMap for thread-safe storage.
 */
@Repository
public class InMemoryConversationRepository {
    private final ConcurrentHashMap<String, Conversation> storage = new ConcurrentHashMap<>();

    /**
     * Saves a conversation to the repository.
     *
     * @param conversation The conversation to save
     * @return The saved conversation
     */
    public Conversation save(Conversation conversation) {
        storage.put(conversation.getId(), conversation);
        return conversation;
    }

    /**
     * Finds a conversation by its unique ID.
     *
     * @param id The ID of the conversation to find
     * @return An Optional containing the conversation, or empty if not found
     */
    public Optional<Conversation> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    /**
     * Returns a list of all conversations in the repository.
     *
     * @return A list of all conversations
     */
    public List<Conversation> findAll() {
        return new ArrayList<>(storage.values());
    }

    /**
     * Checks if a conversation with the given ID exists in the repository.
     *
     * @param id The ID to check
     * @return True if it exists, false otherwise
     */
    public boolean existsById(String id) {
        return storage.containsKey(id);
    }
}
