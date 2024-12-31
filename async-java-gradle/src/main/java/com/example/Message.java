package com.example;

import java.util.UUID;

public record Message(String id) {
    public static Message create() {
        return new Message(UUID.randomUUID().toString());
    }
} 