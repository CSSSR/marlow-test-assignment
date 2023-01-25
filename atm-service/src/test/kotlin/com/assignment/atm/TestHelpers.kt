package com.assignment.atm

import io.kotest.matchers.shouldBe
import org.apache.commons.lang3.RandomStringUtils
import org.jetbrains.exposed.sql.Sequence
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.random.Random

fun randomAmount(from: Long = 1L, until: Long = 999_999L): Long {
    return Random.nextLong(from = from, until = until)
}

fun randomString(stringSize: Int = 10): String {
    return RandomStringUtils.randomAlphabetic(stringSize)
}

fun LocalDateTime?.shouldBeIgnoreNanos(expected: LocalDateTime?) {
    return this?.truncatedTo(ChronoUnit.MILLIS).shouldBe(expected?.truncatedTo(ChronoUnit.MILLIS))
}

fun <T> mapRepeat(times: Int, action: () -> T): List<T> {
    val list = mutableListOf<T>()
    for (index in 0 until times) {
        list.add(action())
    }
    return list
}

fun Sequence.nextValue(): Long {
    val query = "select nextval('${this.identifier}')"
    val transaction = TransactionManager.current()
    return transaction.exec(
        stmt = query,
        transform = { rs ->
            rs.next()
            rs.getLong("nextval")
        }
    )!!
}

fun Sequence.lastValue(): Long {
    val query = "select last_value from ${this.identifier}"
    val transaction = TransactionManager.current()
    return transaction.exec(
        stmt = query,
        transform = { rs ->
            rs.next()
            rs.getLong("last_value")
        }
    )!!
}