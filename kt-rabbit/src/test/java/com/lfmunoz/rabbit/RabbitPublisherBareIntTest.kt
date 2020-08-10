package com.lfmunoz.rabbit

import com.lfmunoz.utils.*
import com.rabbitmq.client.ConnectionFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.junit.jupiter.api.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Integration Test:  Rabbit Consumer
 *  Depends on RabbitMQ
 *  http://localhost:15672/#/queues
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RabbitPublisherBareIntTest {

  // DEPENDENCIES
  private val testThreadPool = newFixedThreadPoolContext(4, "tThread")
  private lateinit var consumerJob: Job
  private lateinit var scope : CoroutineScope
  private lateinit var connectionFactory: ConnectionFactory

  private val atomicInteger = AtomicInteger()
  private val messageCount = 1_000

  private val rabbitConfig = RabbitConfig().apply {
    queue.name = "publisher.test.queue"
    exchange.name = "publisher.test.exchange"
  }

  //________________________________________________________________________________
  // BEFORE ALL / AFTER ALL
  //________________________________________________________________________________
  @BeforeEach
  fun before() {
//    changeLogLevel("com.lfmunoz.rabbit.RabbitPublisherBare", Level.DEBUG)
    atomicInteger.set(0)
    consumerJob = Job()
    connectionFactory = RabbitAdminBare.buildConnectionFactory(rabbitConfig.amqp)
    scope = CoroutineScope(consumerJob + testThreadPool)
    scope.launch {
      startRabbitConsumer(rabbitConfig).collect {
        atomicInteger.getAndIncrement()
      }
    }
  }

  @AfterEach
  fun after() {
    consumerJob.cancel()
  }

  //________________________________________________________________________________
  // Tests
  //________________________________________________________________________________
  @Test
  fun `simple publish`() {
    runBlocking {
      val publisher = RabbitPublishBare(connectionFactory, rabbitConfig.exchange)
      repeat(messageCount) {
        val aGenericData = genericDataGenerator("$it")
        val ourByteArr = mapper.writeValueAsBytes(aGenericData)
        publisher.publish(ourByteArr)
      }
      await.timeout(8, TimeUnit.SECONDS).untilAsserted {
        assertThat(atomicInteger.get()).isEqualTo(messageCount)
      }
      publisher.shutdown()
    }
  }

  @Test
  fun `multi-thread publish`() {
    val start = System.currentTimeMillis()
    val numberOfThreads = 4
    runBlocking {
      val publisher = RabbitPublishBare(connectionFactory, rabbitConfig.exchange)
      repeat(numberOfThreads) { _ ->
        Thread() {
          repeat(messageCount/numberOfThreads) {
            val aGenericData = genericDataGenerator("key=$it", 1_000)
            val ourByteArr = mapper.writeValueAsBytes(aGenericData)
            publisher.publish(ourByteArr)
          }
        }.start()
      }
      await.timeout(10, TimeUnit.SECONDS).untilAsserted {
        assertThat(atomicInteger.get()).isEqualTo(messageCount)
      }
      printResults(messageCount, System.currentTimeMillis() - start)
      publisher.shutdown()
    }
  }
  //________________________________________________________________________________
  // Helper methods
  //________________________________________________________________________________
  private fun startRabbitConsumer(rabbitConfig: RabbitConfig) : Flow<GenericData> {
    val consumer = RabbitConsumerBare(
      "testBasicConsumer",
      connectionFactory,
      rabbitConfig.queue
    )
    consumer.queueBind(rabbitConfig.exchange)
    return consumer.consumeFlow().map {
      return@map mapper.readValue(it.body, GenericData::class.java)
    }
  }

}



