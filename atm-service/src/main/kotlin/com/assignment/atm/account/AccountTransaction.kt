package com.assignment.atm.account

import com.assignment.atm.exposed.enum
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Sequence
import org.jetbrains.exposed.sql.javatime.datetime
import java.util.*

object AccountTransactionTable : UUIDTable("account_transactions") {
    val accountId = reference("account_id", AccountTable)
    val amount = long("amount")
    val type = enum<AccountTransactionType>("type")
    val order = long("order")
    val occurredAt = datetime("occurred_at")
    val idempotencyKey = text("idempotency_key")
    val eventSentAt = datetime("event_sent_at").nullable()

    val orderSequence = Sequence("account_transactions_order_sequence")
}

class AccountTransaction(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<AccountTransaction>(AccountTransactionTable) {
        fun findByIdempotencyKey(idempotencyKey: String): AccountTransaction? {
            return AccountTransaction
                .find { AccountTransactionTable.idempotencyKey.eq(idempotencyKey) }
                .firstOrNull()
        }
    }

    var account by Account referencedOn AccountTransactionTable.accountId
    var amount by AccountTransactionTable.amount
    var type by AccountTransactionTable.type
    var order by AccountTransactionTable.order
    var occurredAt by AccountTransactionTable.occurredAt
    var idempotencyKey by AccountTransactionTable.idempotencyKey
    var eventSentAt by AccountTransactionTable.eventSentAt
}

enum class AccountTransactionType {
    DEPOSIT, WITHDRAWAL
}