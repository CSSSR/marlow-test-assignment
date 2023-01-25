package com.assignment.atm.notification

import com.assignment.atm.account.AccountTransaction
import com.assignment.atm.account.AccountTransactionTable
import com.assignment.atm.exposed.findOrException
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import java.time.LocalDateTime
import java.util.*

class AccountTransactionNotifier(
    private val kafkaTemplate: KafkaTemplate<UUID, AccountTransactionAddedEvent>,
    private val notificationConfigurationProperties: NotificationConfigurationProperties
) {

    // In case of multiple service instances, it's necessary to use distributed scheduler or library like Shedlock/JobRunr/Quartz
    @Scheduled(cron = "\${notification.send-cron}")
    fun sendAccountTransactionAddedEvents() {
        val accountTransactionIds = transaction {
            AccountTransactionTable
                .slice(AccountTransactionTable.id)
                .select { AccountTransactionTable.eventSentAt.isNull() }
                .orderBy(AccountTransactionTable.order to SortOrder.ASC)
                .limit(notificationConfigurationProperties.batchSize)
                .map { it[AccountTransactionTable.id].value }
        }
        accountTransactionIds.forEach { accountTransactionId ->
            transaction {
                val accountTransaction = AccountTransaction.findOrException(accountTransactionId)
                val accountId = accountTransaction.readValues[AccountTransactionTable.accountId].value
                val accountTransactionAddedEvent = AccountTransactionAddedEvent(
                    amount = accountTransaction.amount,
                    type = accountTransaction.type,
                    occurredAt = accountTransaction.occurredAt,
                    transactionId = accountTransactionId
                )
                kafkaTemplate
                    .send(notificationConfigurationProperties.transactionAddedTopic, accountId, accountTransactionAddedEvent)
                    .get()
                accountTransaction.eventSentAt = LocalDateTime.now()
            }
        }
    }
}