package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;

public class MessageProcessor {
    private static final Logger logger = LoggerFactory.getLogger(MessageProcessor.class);
    private static final int DEFAULT_QUEUE_CAPACITY = 100;
    private static final long SHUTDOWN_TIMEOUT_SECONDS = 60;

    private final int numProducers;
    private final int numConsumers;
    private final int numMessagesPerProducer;
    private final BlockingQueue<Message> channel;
    private final Map<Message, Duration> record;
    private final List<Message> consumed;

    public MessageProcessor(int numProducers, int numConsumers, int numMessagesPerProducer) {
        validateInputs(numProducers, numConsumers, numMessagesPerProducer);
        
        this.numProducers = numProducers;
        this.numConsumers = numConsumers;
        this.numMessagesPerProducer = numMessagesPerProducer;
        this.channel = new ArrayBlockingQueue<>(DEFAULT_QUEUE_CAPACITY);
        this.record = new ConcurrentHashMap<>();
        this.consumed = new CopyOnWriteArrayList<>();
    }

    private void validateInputs(int producers, int consumers, int messagesPerProducer) {
        if (producers <= 0 || consumers <= 0 || messagesPerProducer <= 0) {
            throw new IllegalArgumentException("Producers, consumers, and messages must be positive");
        }
    }

    public ProcessingResult process() {
        logger.info("Starting message processing: producers={}, consumers={}, messages_per_producer={}",
                numProducers, numConsumers, numMessagesPerProducer);

        ExecutorService executorService = Executors.newCachedThreadPool();
        List<Future<?>> futures = new ArrayList<>();

        try {
            // Start producers
            for (int p = 1; p <= numProducers; p++) {
                int producerId = p;
                futures.add(executorService.submit(() -> producer(producerId)));
            }

            // Start consumers
            for (int c = 1; c <= numConsumers; c++) {
                int consumerId = c;
                futures.add(executorService.submit(() -> consumer(consumerId)));
            }

            // Wait for all tasks to complete
            futures.forEach(this::awaitFuture);

            return new ProcessingResult(consumed, record);
        } finally {
            shutdownExecutorService(executorService);
        }
    }

    private void awaitFuture(Future<?> future) {
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error in task execution", e);
            Thread.currentThread().interrupt();
        }
    }

    private void shutdownExecutorService(ExecutorService executorService) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void producer(int id) {
        Random rng = new Random();
        for (int i = 0; i < numMessagesPerProducer; i++) {
            try {
                Message msg = Message.create();
                Thread.sleep(100 + rng.nextInt(400));
                channel.put(msg);
                Duration duration = new Duration();
                duration.setStart(System.nanoTime());
                record.put(msg, duration);
                logger.debug("Message sent by producer_id={}, message={}, message_num={}", 
                        id, msg.id(), i + 1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        logger.info("Producer completed: producer_id={}", id);
    }

    private void consumer(int id) {
        Random rng = new Random();
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Message msg = channel.poll(1, TimeUnit.SECONDS);
                if (msg == null) {
                    if (channel.isEmpty()) break;
                    continue;
                }
                Thread.sleep(200 + rng.nextInt(400));
                consumed.add(msg);
                record.get(msg).setEnd(System.nanoTime());
                logger.debug("Message received by consumer_id={}, message={}", id, msg.id());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        logger.info("Consumer completed: consumer_id={}", id);
    }

    public static class ProcessingResult {
        private final List<Message> consumedMessages;
        private final Map<Message, Duration> processingTimes;

        public ProcessingResult(List<Message> consumedMessages, Map<Message, Duration> processingTimes) {
            this.consumedMessages = consumedMessages;
            this.processingTimes = processingTimes;
        }

        public List<Message> getConsumedMessages() {
            return consumedMessages;
        }

        public Map<Message, Duration> getProcessingTimes() {
            return processingTimes;
        }
    }
}
