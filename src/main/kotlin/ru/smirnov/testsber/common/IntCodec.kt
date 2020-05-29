package ru.smirnov.testsber.common

import io.lettuce.core.codec.RedisCodec
import io.lettuce.core.codec.StringCodec
import java.nio.ByteBuffer


class IntCodec: RedisCodec<Int, Int> {
    private val strCodec = StringCodec()

    override fun decodeKey(bytes: ByteBuffer?): Int = strCodec.decodeKey(bytes).toInt()

    override fun encodeValue(value: Int?): ByteBuffer = strCodec.encodeValue(value?.toString())

    override fun encodeKey(key: Int?): ByteBuffer = strCodec.encodeKey(key?.toString())

    override fun decodeValue(bytes: ByteBuffer?): Int  = strCodec.decodeValue(bytes).toInt()


}