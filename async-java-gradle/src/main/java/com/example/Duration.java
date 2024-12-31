package com.example;

public class Duration {
    private long start;
    private long end;

    public void setStart(long start) {
        this.start = start;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public long getProcessingTimeMs() {
        return (end - start) / 1_000_000;
    }
} 