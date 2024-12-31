package com.example.service;

import com.example.config.AppConfig;
import com.example.model.Message;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Getter
public class MessageService {
    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);
    
    private final Map<String, Message> messageRecord;
    private final BlockingQueue<Message> channel;
    private final List<Message> consumed;
    private final Object lock;
    
    public MessageService(AppConfig config) {
        this.messageRecord = new ConcurrentHashMap<>();
        this.channel = new ArrayBlockingQueue<>(config.getChannelCapacity());
        this.consumed = new ArrayList<>();
        this.lock = new Object();
    }
    
    public void recordMessage(Message message) {
        messageRecord.put(message.getId(), message);
    }
    
    public void addConsumedMessage(Message message) {
        synchronized (lock) {
            consumed.add(message);
        }
    }
} 