package com.example

import java.util.UUID
import java.util.concurrent.TimeUnit

case class Message(
  id: String = UUID.randomUUID().toString,
  createdAt: Long = System.nanoTime()
) {
  def processingTimeMs(endTime: Long): Long =
    TimeUnit.NANOSECONDS.toMillis(endTime - createdAt)
}
