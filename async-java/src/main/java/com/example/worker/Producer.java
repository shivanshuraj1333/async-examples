package com.example.worker;

import com.example.config.AppConfig;
import com.example.model.Message;
import com.example.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
public class Producer implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Producer.class);
    
    private final int id;
    private final int messageCount;
    private final MessageService messageService;
    private final AppConfig config;
    
    @Override
    public void run() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < messageCount; i++) {
            try {
                Message msg = new Message(UUID.randomUUID().toString());
                Thread.sleep(random.nextInt(config.getProducerDelayMin(), config.getProducerDelayMax()));
                msg.setStartTime(System.nanoTime());
                messageService.getChannel().put(msg);
                messageService.recordMessage(msg);
                logger.info("Producer {} sent message {} ({}/{})", id, msg, i + 1, messageCount);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Producer {} interrupted", id, e);
                break;
            }
        }
        logger.info("Producer {} completed", id);
    }
} 