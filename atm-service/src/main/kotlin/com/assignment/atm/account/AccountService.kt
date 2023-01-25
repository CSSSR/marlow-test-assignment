package com.assignment.atm.account

import com.assignment.atm.AccountNotFoundException
import com.assignment.atm.InsufficientBalanceException
import mu.KotlinLogging
import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.nextLongVal
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class AccountService {
    private val logger = KotlinLogging.logger {}

    fun addDeposit(accountId: UUID, idempotencyKey: String, request: DepositRequest) = transaction {
        val existingTransaction = AccountTransaction.findByIdempotencyKey(idempotencyKey)
        if (existingTransaction != null) {
            return@transaction existingTransaction.toView()
        }

        getAccountForUpdate(accountId)
        AccountTable.update(
            where = { AccountTable.id.eq(accountId) },
            body = { it.update(balance, balance.plus(request.amount)) }
        )
        val transactionId = AccountTransactionTable.insertAndGetId {
            it[AccountTransactionTable.accountId] = accountId
            it[amount] = request.amount
            it[type] = AccountTransactionType.DEPOSIT
            it[order] = orderSequence.nextLongVal()
            it[occurredAt] = LocalDateTime.now()
            it[AccountTransactionTable.idempotencyKey] = idempotencyKey
        }
        logger.info { "Added deposit with amount ${request.amount} to account with ID '$accountId'" }
        checkNotNull(AccountTransaction.findById(transactionId)).toView()
    }

    fun addWithdrawal(accountId: UUID, idempotencyKey: String, request: WithdrawalRequest) = transaction {
        val existingTransaction = AccountTransaction.findByIdempotencyKey(idempotencyKey)
        if (existingTransaction != null) {
            return@transaction existingTransaction.toView()
        }

        val account = getAccountForUpdate(accountId)
        val balanceAfterWithdrawal = account.balance - request.amount
        if (balanceAfterWithdrawal < 0) throw InsufficientBalanceException(accountId)

        AccountTable.update(
            where = { AccountTable.id.eq(accountId) },
            body = { it.update(balance, balance.minus(request.amount)) }
        )
        val transactionId = AccountTransactionTable.insertAndGetId {
            it[AccountTransactionTable.accountId] = accountId
            it[amount] = request.amount
            it[type] = AccountTransactionType.WITHDRAWAL
            it[order] = orderSequence.nextLongVal()
            it[occurredAt] = LocalDateTime.now()
            it[AccountTransactionTable.idempotencyKey] = idempotencyKey
        }
        logger.info { "Added withdrawal with amount ${request.amount} to account with ID '$accountId'" }
        checkNotNull(AccountTransaction.findById(transactionId)).toView()
    }

    /*
    Strictly speaking, we don't need `FOR UPDATE` clause in case of
    - DEPOSIT: row will be locked anyway because of `balance = (balance + :amount)` statement.
    - WITHDRAW: there is a check constraint on balance, that will not allow to withdraw more money than user has.
    But with `FOR UPDATE` clause we have more clear approach to what's happening in the code, and we can safely check for user balance in case of WITHDRAW.
     */
    private fun getAccountForUpdate(accountId: UUID): Account {
        return Account
            .find { AccountTable.id.eq(accountId) }
            .forUpdate()
            .firstOrNull() ?: throw AccountNotFoundException(accountId)
    }

    private fun AccountTransaction.toView(): AccountTransactionView {
        return AccountTransactionView(
            id = id.value,
            type = type,
            amount = amount,
            occurredAt = occurredAt
        )
    }
}