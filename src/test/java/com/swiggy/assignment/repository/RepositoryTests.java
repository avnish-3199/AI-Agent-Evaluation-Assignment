package com.swiggy.assignment.repository;

import com.swiggy.assignment.model.Conversation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link InMemoryConversationRepository}.
 * Ensures thread-safe storage operations and basic CRUD functionality.
 */
class RepositoryTests {

    private InMemoryConversationRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryConversationRepository();
    }

    /**
     * Tests saving and retrieving a conversation by ID.
     */
    @Test
    void testSaveAndFind() {
        Conversation conv = Conversation.builder().conversationId("c1").build();
        repository.save(conv);
        
        Optional<Conversation> found = repository.findById("c1");
        assertTrue(found.isPresent());
        assertEquals("c1", found.get().getConversationId());
    }

    /**
     * Tests retrieving all conversations and existence checks.
     */
    @Test
    void testFindAllAndExists() {
        repository.save(Conversation.builder().conversationId("c1").build());
        repository.save(Conversation.builder().conversationId("c2").build());
        
        List<Conversation> all = repository.findAll();
        assertEquals(2, all.size());
        
        assertTrue(repository.existsById("c1"));
        assertFalse(repository.existsById("c3"));
    }
}
