@file:Suppress("NOTHING_TO_INLINE")

package com.nao20010128nao.Cryptorage.internal

import com.google.common.io.ByteStreams
import java.io.InputStream
import java.io.Writer
import java.util.*

internal inline fun String.decodeHex(): ByteArray {
    require(length % 2 == 0)
    return chunkedSequence(2).map { it.toByte(16) }.toList().toByteArray()
}

internal inline fun ByteArray.encodeHex(): String = this.asSequence().map { "%02x".format(it) }.joinToString("")

internal inline fun String.mustBeEndedWith(str: String): String = if (endsWith(str)) {
    this
} else {
    "$this$str"
}

internal inline fun String.mustBeEndedWith(vararg str: String): String {
    var me = this
    val strMutable = LinkedList(str.asList())
    while (!str.isEmpty()) {
        me = me.mustBeEndedWith(strMutable.pop()!!)
    }
    return me
}

internal inline fun Writer.flushing(f: Writer.() -> Unit) {
    try {
        f()
    } finally {
        flush()
    }
}

internal inline fun InputStream.limit(length: Long): InputStream = ByteStreams.limit(this, length)
