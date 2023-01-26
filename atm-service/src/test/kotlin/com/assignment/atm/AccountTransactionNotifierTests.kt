package com.assignment.atm

import com.assignment.atm.account.AccountService
import com.assignment.atm.account.AccountTransaction
import com.assignment.atm.account.DepositRequest
import com.assignment.atm.account.WithdrawalRequest
import com.assignment.atm.builders.account
import com.assignment.atm.exposed.findOrException
import com.assignment.atm.notification.AccountTransactionAddedEvent
import com.assignment.atm.notification.AccountTransactionNotifier
import com.assignment.atm.notification.NotificationConfigurationProperties
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import java.util.*

class AccountTransactionNotifierTests(
    private val notifier: AccountTransactionNotifier,
    private val accountService: AccountService,
    private val kafkaMessageVerifier: KafkaMessageVerifier,
    private val notificationProperties: NotificationConfigurationProperties
) : BaseIntegrationTest() {

    @Test
    fun `should notify about new account transaction`() {
        val accountId = UUID.randomUUID()
        transaction {
            account { id = accountId }
        }

        val depositRequest = DepositRequest(amount = 100)
        val depositResponse = accountService.addDeposit(accountId, randomString(), depositRequest)
        notifier.sendAccountTransactionAddedEvents()

        val depositAddedEvent = kafkaMessageVerifier
            .checkMessageExists<AccountTransactionAddedEvent>(accountId, notificationProperties.transactionAddedTopic)
        transaction {
            val depositTransaction = AccountTransaction.findOrException(depositResponse.id)
            verifyTransactionSent(depositTransaction, depositAddedEvent)
        }

        val withdrawalRequest = WithdrawalRequest(amount = 50)
        val withdrawalResponse = accountService.addWithdrawal(accountId, randomString(), withdrawalRequest)
        notifier.sendAccountTransactionAddedEvents()

        val withdrawalAddedEvent = kafkaMessageVerifier
            .checkMessageExists<AccountTransactionAddedEvent>(accountId, notificationProperties.transactionAddedTopic)
        transaction {
            val withdrawalTransaction = AccountTransaction.findOrException(withdrawalResponse.id)
            verifyTransactionSent(withdrawalTransaction, withdrawalAddedEvent)
        }
    }

    private fun verifyTransactionSent(transaction: AccountTransaction, record: ConsumerRecord<UUID, AccountTransactionAddedEvent>) {
        transaction.eventSentAt.shouldNotBeNull()

        record.key().shouldBe(transaction.account.id.value)
        record.value().run {
            amount.shouldBe(transaction.amount)
            type.shouldBe(transaction.type)
            occurredAt.shouldBe(transaction.occurredAt)
            transactionId.shouldBe(transaction.id.value)
        }
    }
}