package com.lfmunoz.fdb


import com.apple.foundationdb.Database
import com.apple.foundationdb.Range
import com.apple.foundationdb.Transaction
import com.apple.foundationdb.subspace.Subspace
import com.apple.foundationdb.tuple.Tuple
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.suspendCancellableCoroutine
import org.fissore.slf4j.FluentLoggerFactory
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


import com.lfmunoz.utils.mapper

/**
 * https://apple.github.io/foundationdb/javadoc/index.html
 */
class FdbRepository<T>(
  private val database: Database,
  private val clazz: Class<T>,
  private val subspace: String = "tableName",
  private val namespace: String = "databaseName"
) {

  private val dispatcher = database.executor.asCoroutineDispatcher()
  private val path = Subspace(Tuple.from(namespace, subspace))

  companion object {
    private val log = FluentLoggerFactory.getLogger(FdbRepository::class.java)
  }

  suspend fun save(key: String, obj: T): T {
    val  aByteArray = serialize(obj)
    assert(aByteArray.size < 10_000) {"[key=${key}] - Can't save more than 10 Kbytes into FoundationDB"}
    return suspendCoroutine { cont ->
      database.runAsync { tr ->
        tr[path.pack(key)] = aByteArray
        CompletableFuture.completedFuture(aByteArray)
      }.thenAccept { result ->
        cont.resume(deserialize(result))
      }
    } // end of suspendCoroutine
  }

  suspend fun delete(key: String): Unit {
    return suspendCoroutine { cont ->
      database.runAsync { tr ->
        tr.clear(path.pack(key))
        CompletableFuture.completedFuture(Unit)
      }.thenAccept { result ->
        cont.resume(result)
      }
    } // end of suspendCoroutine
  }

  suspend fun findByKey(key: String): T? {
    return return suspendCoroutine { cont ->
      database.readAsync { tr ->
        tr[path.pack(key)].thenAccept { result ->
          if (result == null || result.isEmpty()) {
            cont.resume(null)
          } else {
            cont.resume(deserialize(result))
          }
        } // end of thenAccept
      }
    } // end of suspendCoroutine
  }

  suspend fun clear(): Unit {
    return return suspendCoroutine { cont ->
      database.runAsync { tr ->
        tr.clear(Range.startsWith(path.key))
        CompletableFuture.completedFuture(Unit)
      }.thenAccept {
        cont.resume(Unit)
      }
    } // end of suspendCoroutine
  }


  fun readRange(): Flow<T> {
    return callbackFlow<T> {
      database.readAsync { tr ->
        val itr = tr.getRange(path.range()).iterator()
        while (itr.hasNext() && isActive) {
          sendBlocking(deserialize(itr.next().value))
        }
        channel.close()
        CompletableFuture.completedFuture(Unit)
      }
      awaitClose { }
    }.flowOn(dispatcher)
  }

  fun confirmDbIsActive(): Boolean {
    database.run<Any?> { tr: Transaction ->
      tr[Tuple.from("hello").pack()] = Tuple.from("world").pack()
      null
    }
    val world = database.run { tr: Transaction ->
      val result = tr[Tuple.from("hello").pack()].join()
      Tuple.fromBytes(result).getString(0)
    }
    return world == "world"
  }

  private suspend fun watchForChange(key: String)  : Unit {
    return suspendCancellableCoroutine<Unit> { cont ->
      database.run{ tr ->
        tr.watch(path.pack(key))
      }.thenAccept {
        cont.resume(Unit)
      }
    } // end of suspendCoroutine
  }

  suspend fun watch(key: String) : T? {
    watchForChange(key)
    return findByKey(key)
  }


  private fun deserialize(objAsByteArray: ByteArray) : T {
    return mapper.readValue(objAsByteArray, clazz)
  }

  private fun serialize(obj: T) : ByteArray {
    return mapper.writeValueAsBytes(obj)
  }

} // end of FdbRepository


