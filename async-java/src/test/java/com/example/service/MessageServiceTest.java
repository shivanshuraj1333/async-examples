package com.example.service;

import com.example.config.AppConfig;
import com.example.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class MessageServiceTest {
    private MessageService messageService;
    private AppConfig config;

    @BeforeEach
    void setUp() {
        config = AppConfig.builder().build();
        messageService = new MessageService(config);
    }

    @Test
    void testRecordMessage() {
        Message message = new Message("test-id");
        messageService.recordMessage(message);
        
        assertEquals(message, messageService.getMessageRecord().get("test-id"));
    }

    @Test
    void testAddConsumedMessage() {
        Message message = new Message("test-id");
        messageService.addConsumedMessage(message);
        
        assertTrue(messageService.getConsumed().contains(message));
        assertEquals(1, messageService.getConsumed().size());
    }

    @Test
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void testChannelOperations() throws InterruptedException {
        Message message = new Message("test-id");
        messageService.getChannel().put(message);
        
        Message received = messageService.getChannel().take();
        assertEquals(message, received);
    }

    @Test
    void testChannelCapacity() {
        assertEquals(config.getChannelCapacity(), messageService.getChannel().remainingCapacity());
    }

    @Test
    void testConcurrentMessageRecording() throws InterruptedException {
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        
        for (int i = 0; i < threadCount; i++) {
            final int id = i;
            threads[i] = new Thread(() -> {
                Message message = new Message("test-" + id);
                messageService.recordMessage(message);
            });
            threads[i].start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        assertEquals(threadCount, messageService.getMessageRecord().size());
    }
} 