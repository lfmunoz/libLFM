package com.lfmunoz.utils

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.GZIPInputStream
import kotlin.experimental.and

import net.jpountz.lz4.LZ4BlockInputStream
import net.jpountz.lz4.LZ4BlockOutputStream
//import org.xerial.snappy.SnappyFramedInputStream
//import org.xerial.snappy.SnappyFramedOutputStream
//import com.github.luben.zstd.ZstdInputStream
//import com.github.luben.zstd.ZstdOutputStream


/**
 * Supported compression formats and string parser.
 */
enum class Compression {
  SNAPPY, LZ4, GZIP, ZSTD, ZSTD_FASTEST, NONE;
}


/**
 * Simple compression utility class.
 */
object CompressionUtil {
  /**
   * Wrap the [OutputStream] in a compression based output stream.
   *
   * @param compression <null></null> is consider no compression.
   */
  @Throws(IOException::class)
  fun wrap(compression: Compression?, os: OutputStream): OutputStream {
    when (compression) {
      Compression.LZ4 -> return net.jpountz.lz4.LZ4BlockOutputStream(os)
//      Compression.GZIP -> return OurGZIPOutputStream(os)
//      Compression.SNAPPY -> return org.xerial.snappy.SnappyFramedOutputStream(os)
//      Compression.ZSTD -> return com.github.luben.zstd.ZstdOutputStream(os)
//      Compression.ZSTD_FASTEST -> return com.github.luben.zstd.ZstdOutputStream(os, 1)
    }
    return os
  }

  /**
   * Wrap the [InputStream] in a compression based output stream.
   *
   * @param compression <null></null> is consider no compression.
   */
  @Throws(IOException::class)
  fun wrap(compression: Compression?, ins: InputStream): InputStream {
    when (compression) {
      Compression.LZ4 -> return net.jpountz.lz4.LZ4BlockInputStream(ins)
      Compression.GZIP -> return GZIPInputStream(ins)
//      Compression.SNAPPY -> return org.xerial.snappy.SnappyFramedInputStream(ins)
//      Compression.ZSTD, Compression.ZSTD_FASTEST -> return com.github.luben.zstd.ZstdInputStream(ins)
    }
    return ins
  }

  /**
   * Simple compress this byte array.
   */
  @Throws(IOException::class)
  fun compress(compression: Compression?, data: ByteArray?): ByteArray {
    val baos = FastByteArrayOutputStream()
    val os = wrap(compression, baos)
    os.write(data)
    os.close()
    return baos.toByteArray()
  }

  /**
   * Simple de-compress this byte array.
   */
  @Throws(IOException::class)
  fun decompress(compression: Compression?, data: ByteArray?): ByteArray {
    val bais = wrap(compression, FastByteArrayInputStream(data!!))
    val baos = FastByteArrayOutputStream()
    val buffer = ByteArray(256)
    var len = bais.read(buffer)
    while (len != -1) {
      baos.write(buffer, 0, len)
      len = bais.read(buffer)
    }
    baos.flush()
    return baos.toByteArray()
  }
}


/**
 * Simple non-synchronized fast output stream.
 */
class FastByteArrayOutputStream @JvmOverloads constructor(initialCapacity: Int = 1024, increment: Int = 256) : OutputStream() {
  private val increment: Int
  private var array: ByteArray
  private var length = 0
  override fun write(b: Int) {
    if (length >= array.size) {
      array = grow(array, length + increment, length)
    }
    array[length++] = b.toByte()
  }

  @Throws(IOException::class)
  override fun write(b: ByteArray, off: Int, len: Int) {
    if (length + len > array.size) {
      array = grow(array, length + len, length)
    }
    System.arraycopy(b, off, array, length, len)
    length += len
  }

  /**
   * @return important for some input streams to have a trimmed copy.
   */
  fun toByteArray(): ByteArray {
    val ret = ByteArray(length)
    System.arraycopy(array, 0, ret, 0, length)
    return ret
  }

  /**
   * Reset the stream for re-use.
   */
  fun reset() {
    length = 0
  }

  companion object {
    fun grow(array: ByteArray?, length: Int, preserve: Int): ByteArray {
      val t = ByteArray(length)
      System.arraycopy(array, 0, t, 0, preserve)
      return t
    }
  }

  init {
    array = ByteArray(initialCapacity)
    this.increment = increment
  }
}


/**
 * Non-synchronized version of byte array input stream.
 */
class FastByteArrayInputStream private constructor(
  private val array: ByteArray, private val offset: Int, private val length: Int
) : InputStream() {
  private var position = 0
  private var mark = 0

  constructor(array: ByteArray) : this(array, 0, array.size) {}

  override fun markSupported(): Boolean {
    return true
  }

  override fun reset() {
    position = mark
  }

  override fun close() {}
  override fun mark(dummy: Int) {
    mark = position
  }

  override fun available(): Int {
    return length - position
  }

  override fun skip(n: Long): Long {
    var n = n
    return if (n <= (length - position).toLong()) {
      position += n.toInt()
      n
    } else {
      n = (length - position).toLong()
      position = length
      n
    }
  }

  override fun read(): Int {
    return if (length == position) -1 else ( array[offset + position++].and(255.toByte())).toInt()
  }

  override fun read(b: ByteArray, offset: Int, length: Int): Int {
    return if (this.length == position) {
      if (length == 0) 0 else -1
    } else {
      val n = Math.min(length, this.length - position)
      System.arraycopy(array, this.offset + position, b, offset, n)
      position += n
      n
    }
  }

}
