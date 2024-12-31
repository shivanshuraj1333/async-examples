package com.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

class MessageProcessorTest {

    @Test
    void testProcessingWithDefaultParameters() {
        MessageProcessor processor = new MessageProcessor(2, 2, 5);
        MessageProcessor.ProcessingResult result = processor.process();
        
        assertNotNull(result);
        assertEquals(10, result.getConsumedMessages().size());
        assertEquals(10, result.getProcessingTimes().size());
    }

    @Test
    void testProcessingWithZeroProducers() {
        assertThrows(IllegalArgumentException.class, () -> 
            new MessageProcessor(0, 2, 5)
        );
    }

    @Test
    void testProcessingWithZeroConsumers() {
        assertThrows(IllegalArgumentException.class, () -> 
            new MessageProcessor(2, 0, 5)
        );
    }

    @Test
    void testProcessingMessageTimes() {
        MessageProcessor processor = new MessageProcessor(1, 1, 5);
        MessageProcessor.ProcessingResult result = processor.process();
        
        Map<Message, Duration> processingTimes = result.getProcessingTimes();
        processingTimes.values().forEach(duration -> {
            assertTrue(duration.getProcessingTimeMs() >= 0, 
                "Processing time should be non-negative");
        });
    }
}
