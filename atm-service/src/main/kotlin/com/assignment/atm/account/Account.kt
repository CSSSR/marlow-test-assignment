package com.assignment.atm.account

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.*

object AccountTable : UUIDTable("accounts") {
    val balance = long("balance")
}

class Account(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Account>(AccountTable)

    var balance by AccountTable.balance
    val transactions by AccountTransaction referrersOn AccountTransactionTable.accountId
}