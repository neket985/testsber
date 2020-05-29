package ru.smirnov.testsber.entity

class ApiException(val code: Int, val desc: String) : RuntimeException(desc)