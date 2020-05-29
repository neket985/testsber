package ru.smirnov.testsber.controller

import io.lettuce.core.RedisFuture
import io.lettuce.core.ScriptOutputType
import io.lettuce.core.api.async.RedisAsyncCommands
import org.springframework.web.bind.annotation.*
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import ru.smirnov.testsber.entity.*
import javax.annotation.PostConstruct
import javax.validation.Valid

@RestController
@RequestMapping("/api/bill")
class BillController(
        private val redis: RedisAsyncCommands<Int, Int>
) {
    @PostConstruct
    fun initTestData() {
        (1..3).toFlux().flatMap {
            redis.set(it, it * 1000).toMono()
        }.subscribe()
    }

    @GetMapping("{id}")
    fun getById(@PathVariable("id") id: Int) = redis.hasKeyOrThrow(id)
            .flatMap {
                redis.get(id).toMono()
                        .map {
                            BillResponse(id, it.toLong())
                        }
            }


    @PostMapping("payin")
    fun payIn(
            @Valid
            @RequestBody
            body: PaymentRequest
    ) = redis.hasKeyOrThrow(body.billId).flatMap {
        redis.incrby(body.billId, body.sum).toMono()
                .map {
                    BillResponse(body.billId, it)
                }
    }

    @PostMapping("payout")
    fun payOut(
            @Valid
            @RequestBody
            body: PaymentRequest
    ) = redis.hasKeyOrThrow(body.billId).flatMap {
        //этой процедурой обеспечивается атомарность операции. так же, из-за сравнения строк внутри редиса приходится разницу сравнивать с 0, так как "990">"1000"
        redis.eval<Long>("""
                local value = redis.call('get', KEYS[1])-${body.sum}
                if value >= 0 then 
                    redis.call('set', KEYS[1], value)
                    return value
                else 
                    return -1
                end 
            """.trimIndent(), ScriptOutputType.INTEGER, body.billId)
                .toMono()
                .map {
                    if (it == -1L) throw ApiException(400, "Bill from balance is lower than sum")
                    BillResponse(body.billId, it)
                }
    }

    @PostMapping("move")
    fun move(
            @Valid
            @RequestBody
            body: MoveRequest
    ) =
            redis.hasKeyOrThrow(body.billIdFrom).flatMap {
                redis.hasKeyOrThrow(body.billIdTo).flatMap {
                    redis.eval<Boolean>("""
                        local value = redis.call('get', KEYS[1])-${body.sum}
                        if value >= 0 then 
                            redis.call('set', KEYS[1], value)
                            redis.call('incrby', KEYS[2], ${body.sum})
                            return true
                        else 
                            return false 
                        end 
                         """.trimIndent(), ScriptOutputType.BOOLEAN, body.billIdFrom, body.billIdTo)
                            .toMono()
                            .map {
                                if (!it) throw ApiException(400, "Bill from balance is lower than sum")
                                SuccessResponse(it)
                            }
                }
            }

    fun <T> RedisFuture<T>.toMono() = toCompletableFuture().toMono()

    private fun <K, V> RedisAsyncCommands<K, V>.hasKey(key: K) = keys(key).toMono().map { it.isNotEmpty() }

    private fun <K, V> RedisAsyncCommands<K, V>.hasKeyOrThrow(id: K) = hasKey(id)
            .map { hasBill ->
                if (!hasBill) throw ApiException(404, "Bill not found")
            }
}