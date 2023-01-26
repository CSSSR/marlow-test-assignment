package com.assignment.atm.builders

import com.assignment.atm.account.Account
import com.assignment.atm.account.AccountTransaction
import com.assignment.atm.account.AccountTransactionTable
import com.assignment.atm.account.AccountTransactionType
import com.assignment.atm.nextValue
import com.assignment.atm.randomString
import java.time.LocalDateTime
import java.util.*

class AccountBuilder : TestBuilder {
    var id: UUID? = null
    var balance: Long = 0L

    val transactions = mutableListOf<AccountTransactionBuilder>()
    fun accountTransaction(init: AccountTransactionBuilder.() -> Unit) {
        transactions.add(initBuilder(init))
    }
}

fun account(init: AccountBuilder.() -> Unit) = createAccount(initBuilder(init))

fun createAccount(builder: AccountBuilder): Account {
    val account = Account.new(builder.id) {
        balance = builder.balance
    }
    builder.transactions.forEach { createAccountTransaction(account, it) }
    return account
}

class AccountTransactionBuilder : TestBuilder {
    var id: UUID? = null
    var amount: Long = 1L
    lateinit var type: AccountTransactionType
    var order: Long? = null
    var occurredAt: LocalDateTime = LocalDateTime.now()
    var idempotencyKey: String = randomString()
    var eventSentAt: LocalDateTime? = null
}

fun createAccountTransaction(account: Account, builder: AccountTransactionBuilder): AccountTransaction {
    return AccountTransaction.new(builder.id) {
        this.account = account
        this.amount = builder.amount
        this.type = builder.type
        this.order = builder.order ?: AccountTransactionTable.orderSequence.nextValue()
        this.occurredAt = builder.occurredAt
        this.idempotencyKey = builder.idempotencyKey
        this.eventSentAt = builder.eventSentAt
    }
}