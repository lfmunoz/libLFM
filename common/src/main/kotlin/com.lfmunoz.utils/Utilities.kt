package com.lfmunoz.utils

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.common.io.Resources
import com.google.common.util.concurrent.ThreadFactoryBuilder
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.reflect.KClass

//________________________________________________________________________________
// JSON
//________________________________________________________________________________
val mapper: ObjectMapper = jacksonObjectMapper()
  .registerModule(Jdk8Module())
  .registerModule(JavaTimeModule()) // new module, NOT JSR310Module
  .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  .setSerializationInclusion(JsonInclude.Include.NON_NULL)

val ListOfIntType = mapper.typeFactory.constructCollectionType(List::class.java, Int::class.java)
val ListOfByteArrayType = mapper.typeFactory.constructCollectionType(List::class.java, ByteArray::class.java)
val MapOfStringStringType = mapper.typeFactory.constructMapType(Map::class.java, String::class.java, String::class.java)

fun <T: Any> String.toKotlinObject(c: KClass<T>): T {
  return mapper.readValue(this, c.java)
}

//________________________________________________________________________________
// RESULT TYPES
//________________________________________________________________________________
sealed class GenericResult<R> {
  data class Success<R>(val result: R): GenericResult<R>()
  data class Failure<R>(val message: String, val cause: Exception? = null) : GenericResult<R>()
}

//________________________________________________________________________________
// I/O
//________________________________________________________________________________
fun readTextFile(fileName: String): String {
  try {
    return Resources.getResource(fileName).readText()
  } catch (ioe: IOException) {
    throw IllegalStateException(ioe)
  }
}

//________________________________________________________________________________
// LOGGING
//________________________________________________________________________________
fun toWarning(path: String) {
  val logger = LoggerFactory.getLogger(path) as Logger
  logger.level = Level.WARN
}

fun changeLogLevel(path: String, level: Level = Level.WARN) {
  val logger = LoggerFactory.getLogger(path) as Logger
  logger.setLevel(level)
}

//________________________________________________________________________________
// CONCURRENCY
//________________________________________________________________________________
fun createSingleThreadExecutor(name: String): ExecutorService {
  val threadFactory = ThreadFactoryBuilder()
    .setNameFormat(name).setDaemon(true).build()
  return Executors.newSingleThreadExecutor(threadFactory)
}

