package com.example

import scala.concurrent.{Future, Promise, blocking}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.concurrent.TrieMap
import org.slf4j.LoggerFactory
import java.util.UUID
import java.util.concurrent.{LinkedBlockingQueue, TimeUnit}

class ProducerConsumer(config: ProducerConsumerConfig) {
  private val logger = LoggerFactory.getLogger("async_rust")

  // Shared channel for producers and consumers
  private val channel = new LinkedBlockingQueue[String](config.maxQueueSize)
  
  // Metrics tracking
  private val messageMetrics = new TrieMap[String, Long]()
  private val messageStartTimes = new TrieMap[String, Long]()

  def run(): Future[ProducerConsumerMetrics] = {
    val promise = Promise[ProducerConsumerMetrics]()

    // Producers
    val producerFutures = (1 to config.producers).map { producerId =>
      Future {
        blocking {
          (1 to config.messagesPerProducer).foreach { _ =>
            val messageId = UUID.randomUUID().toString
            val startTime = System.currentTimeMillis()
            messageStartTimes.put(messageId, startTime)
            
            // Simulate some work before sending
            Thread.sleep(scala.util.Random.nextInt(config.producerDelayRange._2 - config.producerDelayRange._1) + config.producerDelayRange._1)
            
            channel.offer(messageId)
            logger.info(s"Producer $producerId sent $messageId")
          }
          logger.info(s"Producer $producerId completed")
        }
      }
    }

    // Consumers
    val consumerFutures = (1 to config.consumers).map { consumerId =>
      Future {
        blocking {
          (1 to config.messagesPerProducer).foreach { _ =>
            val messageId = channel.poll(5, TimeUnit.SECONDS)
            if (messageId != null) {
              val startTime = messageStartTimes.getOrElse(messageId, System.currentTimeMillis())
              val processingTime = System.currentTimeMillis() - startTime
              
              logger.info(s"Consumer $consumerId received $messageId")
              
              // Update metrics
              messageMetrics.put(messageId, processingTime)
            }
          }
          logger.info(s"Consumer $consumerId completed")
        }
      }
    }

    // Combine and process results
    Future.sequence(producerFutures ++ consumerFutures).map { _ =>
      // Create metrics
      val metrics = ProducerConsumerMetrics(
        totalMessages = config.producers * config.messagesPerProducer,
        messageTimings = messageMetrics.map { case (id, time) => 
          id -> MessageTiming(processingTimeMs = time)
        }.toMap
      )
      
      logger.info("All producers and consumers have completed.")
      logger.info(s"Number of messages consumed: ${metrics.totalMessages}")
      
      logger.info("################ printing produce and consume delays ################")
      metrics.messageTimings.foreach { case (messageId, timing) =>
        logger.info(s"Message $messageId took ${timing.processingTimeMs} ms")
      }
      logger.info("################ printing done ################")
      
      promise.success(metrics)
      metrics
    }.recover {
      case e: Exception =>
        logger.error("Error in producer-consumer", e)
        promise.failure(e)
        ProducerConsumerMetrics()
    }

    promise.future
  }
}
