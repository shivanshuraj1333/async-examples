package com.example

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scopt.OParser
import org.slf4j.LoggerFactory

object Main {
  private val logger = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    // Define the command-line parser
    val builder = OParser.builder[ProducerConsumerConfig]
    val parser = {
      import builder._
      OParser.sequence(
        programName("async-scala"),
        head("Async Scala Producer-Consumer"),
        opt[Int]('p', "producers")
          .action((x, c) => c.copy(producers = x))
          .text("number of producers")
          .validate(x => if (x > 0) success else failure("Producers must be > 0")),
        opt[Int]('c', "consumers")
          .action((x, c) => c.copy(consumers = x))
          .text("number of consumers")
          .validate(x => if (x > 0) success else failure("Consumers must be > 0")),
        opt[Int]('m', "messages")
          .action((x, c) => c.copy(messagesPerProducer = x))
          .text("messages per producer")
          .validate(x => if (x > 0) success else failure("Messages must be > 0")),
        help("help").text("prints this usage text")
      )
    }

    // Default configuration
    val defaultConfig = ProducerConsumerConfig(
      producers = 10, 
      consumers = 5, 
      messagesPerProducer = 5
    )

    // Parse the arguments
    OParser.parse(parser, args, defaultConfig) match {
      case Some(config) => 
        logger.info(s"Starting with ${config.producers} producers and ${config.consumers} consumers")
        
        val pc = new ProducerConsumer(config)
        try {
          val metricsFuture: Future[ProducerConsumerMetrics] = pc.run()
          val metrics = Await.result(metricsFuture, 1.minute)
          logger.info(s"Total messages processed: ${metrics.totalMessages}")
        } catch {
          case e: Exception =>
            logger.error("Error running producer-consumer", e)
            sys.exit(1)
        }

      case _ => 
        OParser.usage(parser)  // Print usage information
        sys.exit(1)
    }
  }
}
