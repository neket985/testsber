package ru.smirnov.testsber

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TestsberApplication

fun main(args: Array<String>) {
    runApplication<TestsberApplication>(*args)
}
