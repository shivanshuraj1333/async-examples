package com.example;

import com.example.config.AppConfig;
import com.example.model.Message;
import com.example.service.MessageService;
import com.example.worker.Consumer;
import com.example.worker.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Command(name = "async-java", mixinStandardHelpOptions = true, version = "1.0",
        description = "A async-java application")
public class ProducerConsumerApp implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ProducerConsumerApp.class);

    @Option(names = {"-p", "--producers"}, description = "Number of producers")
    private int numProducers = 10;

    @Option(names = {"-c", "--consumers"}, description = "Number of consumers")
    private int numConsumers = 5;

    @Option(names = {"-m", "--messages"}, description = "Number of messages per producer")
    private int numMessagesPerProducer = 5;

    private final MessageService messageService;
    private final AppConfig config;

    public ProducerConsumerApp() {
        this.config = AppConfig.builder()
                .producerCount(numProducers)
                .consumerCount(numConsumers)
                .messagesPerProducer(numMessagesPerProducer)
                .build();
        this.messageService = new MessageService(config);
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ProducerConsumerApp()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        logger.info("Starting application with producers={}, consumers={}, messages_per_producer={}",
                config.getProducerCount(), config.getConsumerCount(), config.getMessagesPerProducer());
        setup();
    }

    private void setup() {
        ExecutorService executorService = Executors.newCachedThreadPool();
        CountDownLatch producersLatch = new CountDownLatch(config.getProducerCount());
        CountDownLatch consumersLatch = new CountDownLatch(config.getConsumerCount());

        // Start producers
        for (int i = 1; i <= config.getProducerCount(); i++) {
            final int producerId = i;
            executorService.submit(() -> {
                try {
                    new Producer(producerId, config.getMessagesPerProducer(), messageService, config).run();
                } finally {
                    producersLatch.countDown();
                }
            });
        }

        // Start consumers
        for (int i = 1; i <= config.getConsumerCount(); i++) {
            final int consumerId = i;
            executorService.submit(() -> {
                try {
                    new Consumer(consumerId, messageService, config).run();
                } finally {
                    consumersLatch.countDown();
                }
            });
        }

        try {
            producersLatch.await();
            messageService.getChannel().put(new Message("POISON_PILL"));
            consumersLatch.await();
            
            logger.info("All producers and consumers have completed");
            logger.info("Consumption summary: messages_consumed={}", messageService.getConsumed().size());
            
            logger.info("################ Printing produce and consume delays ################");
            Thread.sleep(2000);
            printDelay();
            logger.info("################ Printing done ################");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted while waiting for completion", e);
        } finally {
            executorService.shutdown();
        }
    }

    private void printDelay() {
        messageService.getMessageRecord().values().forEach(msg -> {
            if (!msg.getId().equals("POISON_PILL")) {
                logger.info("Message processing time: message={}, processing_time_ms={}",
                        msg.getId(), msg.getProcessingTime());
            }
        });
    }
} 