package com.assignment.audit

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.util.*

object AccountTransactionChangelogTable : UUIDTable("account_transactions_changelog") {
    val accountId = uuid("account_id")
    val transactionId = uuid("transaction_id")
    val amount = long("amount")
    val type = text("type")
    val occurredAt = datetime("occurred_at")
}

class AccountTransactionChangelog(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<AccountTransactionChangelog>(AccountTransactionChangelogTable)

    var accountId by AccountTransactionChangelogTable.accountId
    var transactionId by AccountTransactionChangelogTable.transactionId
    var amount by AccountTransactionChangelogTable.amount
    var type by AccountTransactionChangelogTable.type
    var occurredAt by AccountTransactionChangelogTable.occurredAt
}