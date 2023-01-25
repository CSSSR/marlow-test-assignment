package com.assignment.audit

import com.assignment.audit.configuration.ServiceConfigurationProperties
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.apache.kafka.clients.admin.AdminClient
import org.awaitility.kotlin.await
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.springframework.kafka.core.KafkaAdmin
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.random.Random

class AccountTransactionAddedListenerTests(
    private val kafkaSender: KafkaSender,
    private val serviceProperties: ServiceConfigurationProperties,
    private val kafkaMessageVerifier: KafkaMessageVerifier,
    kafkaAdmin: KafkaAdmin
) : BaseIntegrationTest() {
    private val adminClient = AdminClient.create(kafkaAdmin.configurationProperties)

    @Test
    fun `should write account transaction added event to changelog table`() {
        val event = randomEvent()
        val accountId = UUID.randomUUID()
        kafkaSender.sendMessage(serviceProperties.accountTransactionAddedTopic, accountId, event)

        val changelogEntry = waitForChangelogEntry(event.transactionId)
        changelogEntry.shouldNotBeNull().run {
            this.accountId.shouldBe(accountId)
            this.transactionId.shouldBe(event.transactionId)
            this.amount.shouldBe(event.amount)
            this.type.shouldBe(event.type)
            this.occurredAt.shouldBe(event.occurredAt)
        }
    }

    @Test
    fun `should not process event if it was already processed`() {
        val event = randomEvent()
        val accountId = UUID.randomUUID()

        kafkaSender.sendMessage(serviceProperties.accountTransactionAddedTopic, accountId, event)
        waitForChangelogEntry(event.transactionId)
        val currentOffset = getCurrentOffset()

        kafkaSender.sendMessage(serviceProperties.accountTransactionAddedTopic, accountId, event)
        await.until { getCurrentOffset() == currentOffset + 1 }
        transaction {
            AccountTransactionChangelog
                .find { AccountTransactionChangelogTable.transactionId.eq(event.transactionId) }
                .shouldHaveSize(1)
        }
    }

    @Test
    fun `should send account transaction added event to dead letter topic in case of deserialization failure`() {
        val event = InvalidAccountTransactionAddedEvent(
            amount = 1L,
            occurredAt = LocalDate.now(),
            transactionId = UUID.randomUUID()
        )
        val accountId = UUID.randomUUID()
        kafkaSender.sendMessage(serviceProperties.accountTransactionAddedTopic, accountId, event)
        val dltRecord = kafkaMessageVerifier
            .checkMessageExists<InvalidAccountTransactionAddedEvent>(accountId, "account.transaction.added.audit-service.DLT")
        dltRecord.key().shouldBe(accountId)
        dltRecord.value().run {
            amount.shouldBe(event.amount)
            occurredAt.shouldBe(event.occurredAt)
            transactionId.shouldBe(event.transactionId)
        }
    }

    @Test
    fun `should send account transaction added event to dead letter topic in case of database exception`() {
        runCatching {
            val event = randomEvent()
            val accountId = UUID.randomUUID()
            transaction { exec("alter table account_transactions_changelog rename to unexpected_table_name") }
            kafkaSender.sendMessage(serviceProperties.accountTransactionAddedTopic, accountId, event)
            val dltRecord = kafkaMessageVerifier
                .checkMessageExists<AccountTransactionAddedEvent>(accountId, "account.transaction.added.audit-service.DLT")
            dltRecord.key().shouldBe(accountId)
            dltRecord.value().run {
                amount.shouldBe(event.amount)
                type.shouldBe(event.type)
                occurredAt.shouldBe(event.occurredAt)
                transactionId.shouldBe(event.transactionId)
            }
        }.also { transaction { exec("alter table unexpected_table_name rename to account_transactions_changelog") } }.getOrThrow()
    }

    private fun waitForChangelogEntry(transactionId: UUID): AccountTransactionChangelog {
        var changelogEntry: AccountTransactionChangelog? = null
        await.until {
            transaction {
                changelogEntry = AccountTransactionChangelog
                    .find { AccountTransactionChangelogTable.transactionId.eq(transactionId) }
                    .firstOrNull()
            }
            changelogEntry != null
        }
        return changelogEntry!!
    }

    private fun randomEvent(): AccountTransactionAddedEvent {
        return AccountTransactionAddedEvent(
            amount = Random.nextLong(from = 0, until = 1000L),
            type = listOf("WITHDRAWAL", "DEPOSIT").random(),
            transactionId = UUID.randomUUID(),
            occurredAt = LocalDateTime.now().minusMinutes(1)
        )
    }

    private fun getCurrentOffset(): Long {
        // offset position for our consumer group in first partition in a single topic
        return adminClient.listConsumerGroupOffsets("audit-service").all().get()
            .entries.first()
            .value.entries.first()
            .value.offset()
    }

    private class InvalidAccountTransactionAddedEvent(
        val amount: Long,
        val occurredAt: LocalDate,
        val transactionId: UUID
    )
}