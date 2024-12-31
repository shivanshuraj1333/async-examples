package com.example;

public class Message {
    private final String id;
    private long startTime;
    private long endTime;

    public Message(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getProcessingTime() {
        return (endTime - startTime) / 1_000_000; // Convert to milliseconds
    }

    @Override
    public String toString() {
        return id;
    }
} 