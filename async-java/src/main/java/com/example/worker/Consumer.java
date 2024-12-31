package com.example.worker;

import com.example.config.AppConfig;
import com.example.model.Message;
import com.example.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
public class Consumer implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Consumer.class);
    
    private final int id;
    private final MessageService messageService;
    private final AppConfig config;
    
    @Override
    public void run() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        while (true) {
            try {
                Message msg = messageService.getChannel().take();
                if (msg.getId().equals("POISON_PILL")) {
                    messageService.getChannel().put(msg);
                    break;
                }
                
                Thread.sleep(random.nextInt(config.getConsumerDelayMin(), config.getConsumerDelayMax()));
                msg.setEndTime(System.nanoTime());
                messageService.addConsumedMessage(msg);
                
                logger.info("Consumer {} received message {}", id, msg);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Consumer {} interrupted", id, e);
                break;
            }
        }
        logger.info("Consumer {} completed", id);
    }
} 