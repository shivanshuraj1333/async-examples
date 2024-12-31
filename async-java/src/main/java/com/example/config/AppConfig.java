package com.example.config;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AppConfig {
    @Builder.Default int producerCount = 10;
    @Builder.Default int consumerCount = 5;
    @Builder.Default int messagesPerProducer = 5;
    @Builder.Default int channelCapacity = 100;
    @Builder.Default int producerDelayMin = 100;
    @Builder.Default int producerDelayMax = 500;
    @Builder.Default int consumerDelayMin = 200;
    @Builder.Default int consumerDelayMax = 600;
} 