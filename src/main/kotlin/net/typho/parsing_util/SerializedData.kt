package net.typho.parsing_util

import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path
import kotlin.io.path.*

/**
 * Contains raw serialized data, and can be created from a [String], [ByteArray], [Path], [InputStream], or make a custom implementation.
 *
 * The purpose is to avoid excess boxing of data.
 *
 * For example, if you're reading the text of a file then giving it to a byte format like NBT or Packet, the string will be converted to a byte array when a raw byte array could have been read directly from the file.
 *
 * Note that this class is single-use, and the data can only be read/written a single time (except for [toString] which is separate and repeatable).
 */
abstract class SerializedData {
    var hasBeenUsed = false
        private set

    protected fun <R> use(task: () -> R): R {
        if (hasBeenUsed) {
            throw IllegalStateException("SerializedData has already been used")
        }

        hasBeenUsed = true
        return task()
    }

    abstract fun asString(): String

    abstract fun asBytes(): ByteArray

    abstract fun save(path: Path)

    /**
     * Stream must be closed by caller.
     */
    abstract fun copyTo(output: OutputStream)

    abstract override fun toString(): String

    companion object {
        @JvmStatic
        @JvmName("of")
        fun String.asData() = object : SerializedData() {
            override fun asString() = use { this@asData }

            override fun asBytes() = use { toByteArray() }

            override fun save(path: Path) = use { path.writeText(this@asData) }

            override fun copyTo(output: OutputStream) {
                use {
                    output.bufferedWriter().write(this@asData)
                }
            }

            override fun toString() = this@asData
        }

        @JvmStatic
        @JvmName("of")
        fun ByteArray.asData() = object : SerializedData() {
            override fun asString() = use { String(this@asData) }

            override fun asBytes() = use { this@asData }

            override fun save(path: Path) = use { path.writeBytes(this@asData) }

            override fun copyTo(output: OutputStream) {
                use {
                    output.write(this@asData)
                }
            }

            override fun toString() = joinToString(prefix = "0x") { it.toHexString() }
        }

        @JvmStatic
        @JvmName("of")
        fun Path.asData() = object : SerializedData() {
            override fun asString() = use { readText() }

            override fun asBytes() = use { readBytes() }

            override fun save(path: Path) {
                use {
                    path.outputStream().use { output ->
                        copyTo(output)
                    }
                }
            }

            override fun copyTo(output: OutputStream) {
                use {
                    inputStream().use { input ->
                        input.copyTo(output)
                    }
                }
            }

            override fun toString() = this@asData.toString()
        }

        /**
         * Stream must be closed by caller.
         */
        @JvmStatic
        @JvmOverloads
        @JvmName("of")
        fun InputStream.asData(name: String = "InputStream") = object : SerializedData() {
            override fun asString() = use { reader().readText() }

            override fun asBytes() = use { readBytes() }

            override fun save(path: Path) {
                use {
                    path.outputStream().use { output ->
                        copyTo(output)
                    }
                }
            }

            override fun copyTo(output: OutputStream) {
                use {
                    this@asData.copyTo(output)
                }
            }

            override fun toString() = name
        }
    }
}