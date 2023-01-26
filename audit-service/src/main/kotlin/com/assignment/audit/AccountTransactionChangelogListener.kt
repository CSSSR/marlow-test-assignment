package com.assignment.audit

import com.assignment.audit.configuration.KafkaConfiguration.Companion.ACCOUNT_TRANSACTION_ADDED_CONTAINER_FACTORY
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component
import java.util.*

@Component
class AccountTransactionChangelogListener {
    private val logger = KotlinLogging.logger {}

    @KafkaListener(containerFactory = ACCOUNT_TRANSACTION_ADDED_CONTAINER_FACTORY, topics = ["\${service.account-transaction-added-topic}"])
    fun receiveAccountTransactionAddedEvent(@Header(KafkaHeaders.RECEIVED_KEY) key: UUID, event: AccountTransactionAddedEvent) {
        logger.info { "Trying to process AccountTransactionAddedEvent with transaction id '${event.transactionId}'" }
        transaction {
            val eventAlreadySaved = AccountTransactionChangelog
                .find { AccountTransactionChangelogTable.transactionId.eq(event.transactionId) }
                .empty().not()
            if (eventAlreadySaved) {
                logger.info { "AccountTransactionAddedEvent with transaction id '${event.transactionId}' was already processed" }
                return@transaction
            }

            val changelogEntry = AccountTransactionChangelog.new {
                accountId = key
                transactionId = event.transactionId
                amount = event.amount
                type = event.type
                occurredAt = event.occurredAt
            }
            logger.info { "Saved AccountTransactionAddedEvent with transactionId '${changelogEntry.transactionId}'" }
        }
    }
}