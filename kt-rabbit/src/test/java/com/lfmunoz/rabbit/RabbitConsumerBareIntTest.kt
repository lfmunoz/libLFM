package com.lfmunoz.rabbit

import com.fasterxml.jackson.module.kotlin.readValue
import com.lfmunoz.utils.GenericData
import com.lfmunoz.utils.changeLogLevel
import com.lfmunoz.utils.genericDataGenerator
import org.junit.jupiter.api.Assertions.*
import com.lfmunoz.utils.mapper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import java.time.Instant
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * Integration Test:  Rabbit Consumer
 *  Depends on RabbitMQ
 *  http://localhost:15672/#/queues
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RabbitConsumerBareIntTest {

  // Dependencies
  private val testThreadPool = newFixedThreadPoolContext(4, "consumerThread")

  //________________________________________________________________________________
  // BEFORE ALL / AFTER ALL
  //________________________________________________________________________________
  @BeforeAll
  fun before() {
    changeLogLevel("org.apache.kafka.clients.producer.ProducerConfig")
  }

  private val messageCount = 50


  val config = RabbitConfig().apply {
    queue.name = "consumer.test.queue"
    exchange.name = "consumer.test.exchange"
  }
  //________________________________________________________________________________
  // Tests
  //________________________________________________________________________________
  @Test
  fun `simple publish and consume`() {
    runBlocking {
      val latch = CountDownLatch(messageCount)

      // CONSUMER
      val consumer = RabbitConsumerBare(
      "testBasicConsumer",
        config.amqp,
        config.queue,
        config.exchange
      )

      launch(testThreadPool) {
        consumer.consumeChannelWithCallback {
          val listOfGenericData : GenericData = mapper.readValue(it, GenericData::class.java)
          println(listOfGenericData)
          latch.countDown()
        }
      }

      // PUBLISHER
      val publisher = RabbitPublisherBare( config.amqp, config.exchange)

      println("C")
      launch(testThreadPool) {
        repeat(messageCount) {
          val aGenericData = genericDataGenerator("$it")
          println(aGenericData)
          val ourByteArr = mapper.writeValueAsBytes(aGenericData)
          publisher.publish(ourByteArr)
        }
      }

      println("Y")
      // ASSERTIONS
      assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue()
      println("Z")
      consumer.shutdown()
      publisher.shutdown()

    } // end of runBlcoking
  } // end of Test Case

  @Test
  fun `multi-thread publish and consume`() {

  }


  //________________________________________________________________________________
  // Helper methods
  //________________________________________________________________________________



}



