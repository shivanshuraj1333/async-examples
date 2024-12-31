package com.example.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString(of = "id")
public class Message {
    private final String id;
    @Setter private long startTime;
    @Setter private long endTime;

    public long getProcessingTime() {
        return (endTime - startTime) / 1_000_000; // Convert to milliseconds
    }
} 