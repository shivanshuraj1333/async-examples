package com.example.integration;

import com.example.config.AppConfig;
import com.example.model.Message;
import com.example.service.MessageService;
import com.example.worker.Consumer;
import com.example.worker.Producer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProducerConsumerIntegrationTest {
    
    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testProducerConsumerIntegration() throws InterruptedException {
        // Given
        AppConfig config = AppConfig.builder()
                .producerCount(3)
                .consumerCount(2)
                .messagesPerProducer(5)
                .producerDelayMax(100)
                .consumerDelayMax(200)
                .build();
        
        MessageService messageService = new MessageService(config);
        ExecutorService executorService = Executors.newCachedThreadPool();
        CountDownLatch producersLatch = new CountDownLatch(config.getProducerCount());
        CountDownLatch consumersLatch = new CountDownLatch(config.getConsumerCount());
        
        // When
        for (int i = 1; i <= config.getProducerCount(); i++) {
            executorService.submit(() -> {
                try {
                    new Producer(i, config.getMessagesPerProducer(), messageService, config).run();
                } finally {
                    producersLatch.countDown();
                }
            });
        }
        
        for (int i = 1; i <= config.getConsumerCount(); i++) {
            executorService.submit(() -> {
                try {
                    new Consumer(i, messageService, config).run();
                } finally {
                    consumersLatch.countDown();
                }
            });
        }
        
        // Then
        producersLatch.await();
        messageService.getChannel().put(new Message("POISON_PILL"));
        consumersLatch.await();
        
        int expectedMessages = config.getProducerCount() * config.getMessagesPerProducer();
        assertEquals(expectedMessages, messageService.getConsumed().size());
        
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
    }
} 