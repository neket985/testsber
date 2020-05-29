package ru.smirnov.testsber.entity

import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class PaymentRequest(
        val billId: Int,
        @get:Min(0)
        val sum: Long
)

data class MoveRequest(
        val billIdFrom: Int,
        val billIdTo: Int,
        @get:Min(0)
        val sum: Long
)