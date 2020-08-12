package com.lfmunoz.utils

import com.fasterxml.jackson.module.kotlin.readValue
import java.time.Instant

val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

data class GenericData(
  val key: String = "",
  val value: String = "",
  val meataData: String = "",
  val src: String = "n/a",
  val dst: String = "n/a",
  val created: Instant = Instant.now()
) {
  companion object {
    val listType =  mapper.typeFactory.constructCollectionType(List::class.java, GenericData::class.java)
    fun fromJson(json: String) = mapper.readValue<GenericData>(json)
    fun fromByteArray(iBytes: ByteArray) = mapper.readValue<GenericData>(iBytes)
  }

  fun toJson(): String = mapper.writeValueAsString(this)
  fun toByteArray(): ByteArray = mapper.writeValueAsBytes(this)
}


fun genericDataGenerator(key: String, size: Int = 1000) : GenericData {
  val value =  (1..size)
    .map { kotlin.random.Random.nextInt(0, charPool.size) }
    .map(charPool::get)
    .joinToString("")

  return GenericData(key, value)
}
