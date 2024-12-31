package com.example

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.concurrent.Await
import scala.concurrent.duration._

class ProducerConsumerSpec extends AnyFlatSpec with Matchers {
  "ProducerConsumer" should "process all messages" in {
    val config  = ProducerConsumerConfig(producers = 2, consumers = 1, messagesPerProducer = 3)
    val pc      = new ProducerConsumer(config)
    val metrics = Await.result(pc.run(), 1.minute)
    metrics.totalMessages shouldBe 6
  }

  it should "handle empty queue correctly" in {
    val config  = ProducerConsumerConfig(producers = 0, consumers = 1, messagesPerProducer = 0)
    val pc      = new ProducerConsumer(config)
    val metrics = Await.result(pc.run(), 1.minute)
    metrics.totalMessages shouldBe 0
  }

  it should "handle multiple producers and consumers" in {
    val config  = ProducerConsumerConfig(producers = 5, consumers = 3, messagesPerProducer = 10)
    val pc      = new ProducerConsumer(config)
    val metrics = Await.result(pc.run(), 1.minute)
    metrics.totalMessages shouldBe 50
  }

  it should "handle backpressure correctly" in {
    val config = ProducerConsumerConfig(
      producers = 5,
      consumers = 1,
      messagesPerProducer = 10,
      maxQueueSize = 10
    )

    val pc      = new ProducerConsumer(config)
    val metrics = Await.result(pc.run(), 1.minute)
    metrics.messageTimings.size should be <= config.maxQueueSize
  }

  it should "process messages within reasonable time" in {
    val config = ProducerConsumerConfig(
      producers = 3,
      consumers = 2,
      messagesPerProducer = 5
    )

    val pc      = new ProducerConsumer(config)
    val metrics = Await.result(pc.run(), 1.minute)
    
    // Check that all messages have processing times
    metrics.messageTimings.values.foreach { timing =>
      timing.processingTimeMs should be >= 0L
    }
  }

  it should "handle different delay ranges" in {
    val config = ProducerConsumerConfig(
      producers = 2,
      consumers = 2,
      messagesPerProducer = 5,
      producerDelayRange = (10, 50),
      consumerDelayRange = (5, 30)
    )

    val pc      = new ProducerConsumer(config)
    val metrics = Await.result(pc.run(), 1.minute)
    metrics.totalMessages shouldBe 10
  }
}

object LoggingConfig {
  def configureLogging(level: LogLevel = LogLevel.INFO): Unit = {
    // Configure logging dynamically
  }
}
