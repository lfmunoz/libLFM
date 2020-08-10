package com.lfmunoz.rabbit

import com.lfmunoz.utils.GenericData
import com.lfmunoz.utils.genericDataGenerator
import com.lfmunoz.utils.mapper
import com.rabbitmq.client.ConnectionFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Integration Test:  Rabbit Consumer
 *  Depends on RabbitMQ
 *  http://localhost:15672/#/queues
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RabbitConsumerBareIntTest {

  // Dependencies
  private val testThreadPool = newFixedThreadPoolContext(4, "tThread")
  private lateinit var consumerJob: Job
  private lateinit var scope : CoroutineScope
  private lateinit var connectionFactory: ConnectionFactory

  private val messageCount = 5_000
  private val rabbitConfig = RabbitConfig().apply {
    queue.name = "consumer.test.queue"
    exchange.name = "consumer.test.exchange"
  }


  //________________________________________________________________________________
  // BEFORE ALL / AFTER ALL
  //________________________________________________________________________________
  @BeforeEach
  fun before() {
    // changeLogLevel("com.lfmunoz.rabbit.RabbitPublisherBare", Level.DEBUG)
    consumerJob = Job()
    scope = CoroutineScope(consumerJob + testThreadPool)
    connectionFactory = RabbitAdminBare.buildConnectionFactory(rabbitConfig.amqp)
  }

  @AfterEach
  fun after() {
    consumerJob.cancel()
  }

  //________________________________________________________________________________
  // Tests
  //________________________________________________________________________________
  @Test
  fun `multi-thread consume`() {
    runBlocking {
      // PUBLISHER
      val publisher = RabbitPublishBare(connectionFactory, rabbitConfig.exchange)
      RabbitAdminBare.createExchange(publisher.rabbitChannel, rabbitConfig.exchange)

      // CONSUMER
      val latch = CountDownLatch(messageCount)
      val consumer = RabbitConsumerBare(
        "testBasicConsumer",
        connectionFactory,
        rabbitConfig.queue
      )
      consumer.queueBind(rabbitConfig.exchange)
      consumer.consumeFlow().map {
        return@map mapper.readValue(it.body, GenericData::class.java)
      }.onEach {
        // println("${Thread.currentThread().name}")
        latch.countDown()
      }.launchIn(scope)

      // SEND MESSAGES
      repeat(messageCount) {
        val aGenericData = genericDataGenerator("key=$it", 1_000)
        val ourByteArr = mapper.writeValueAsBytes(aGenericData)
        publisher.publish(ourByteArr)
      }

      assertThat(latch.await(15, TimeUnit.SECONDS)).isTrue()

    } // end of runBlcoking
  } // end of Test Case



}



