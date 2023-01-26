package com.assignment.atm.builders

@DslMarker
annotation class TestDsl

@TestDsl
interface TestBuilder

inline fun <reified T> initBuilder(init: T.() -> Unit): T {
    return T::class::constructors.get().first().call().also(init)
}