package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Command(name = "producer-consumer", description = "A producer-consumer application")
public class ProducerConsumerApp implements Callable<Integer> {
    private static final Logger logger = LoggerFactory.getLogger(ProducerConsumerApp.class);

    @Option(names = {"-p", "--producers"}, description = "Number of producers", defaultValue = "10")
    private int numProducers;

    @Option(names = {"-c", "--consumers"}, description = "Number of consumers", defaultValue = "5")
    private int numConsumers;

    @Option(names = {"-m", "--messages"}, description = "Number of messages per producer", defaultValue = "5")
    private int numMessagesPerProducer;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ProducerConsumerApp()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        logger.info("Starting application with producers={}, consumers={}, messages_per_producer={}",
                numProducers, numConsumers, numMessagesPerProducer);
        
        try {
            MessageProcessor processor = new MessageProcessor(numProducers, numConsumers, numMessagesPerProducer);
            MessageProcessor.ProcessingResult result = processor.process();
            
            logger.info("Total messages consumed: {}", result.getConsumedMessages().size());
            result.getProcessingTimes().forEach((msg, duration) -> 
                logger.info("Message {} processing time: {} ms", 
                    msg.id(), duration.getProcessingTimeMs())
            );
            
            return 0;
        } catch (Exception e) {
            logger.error("Error in producer-consumer application", e);
            return 1;
        }
    }
}
