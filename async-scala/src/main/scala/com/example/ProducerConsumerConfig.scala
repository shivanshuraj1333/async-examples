package com.example

case class ProducerConsumerConfig(
  producers: Int = 10,
  consumers: Int = 5,
  messagesPerProducer: Int = 5,
  maxQueueSize: Int = 100,
  producerDelayRange: (Int, Int) = (10, 100),
  consumerDelayRange: (Int, Int) = (10, 100)
) 