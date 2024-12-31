package com.example

case class ProducerConsumerMetrics(
  totalMessages: Int = 0,
  messageTimings: Map[String, MessageTiming] = Map.empty
)

case class MessageTiming(
  processingTimeMs: Long = 0
)
