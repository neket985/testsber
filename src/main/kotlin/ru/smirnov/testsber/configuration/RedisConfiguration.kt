package ru.smirnov.testsber.configuration

import io.lettuce.core.RedisClient
import io.lettuce.core.api.async.RedisAsyncCommands
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.smirnov.testsber.common.IntCodec


@Configuration
class RedisConfiguration() {

    @Bean
    fun getClient(@Value("\${redis.url}") redisUrl: String): RedisAsyncCommands<Int, Int> {
        val redisClient = RedisClient.create(redisUrl)
        val connection = redisClient.connect<Int, Int>(IntCodec())
        return connection.async()
    }


}