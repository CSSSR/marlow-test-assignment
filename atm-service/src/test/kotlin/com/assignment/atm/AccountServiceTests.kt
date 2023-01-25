package com.assignment.atm

import com.assignment.atm.account.*
import com.assignment.atm.builders.account
import com.assignment.atm.exposed.findOrException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.common.concurrentHashMap
import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.longs.shouldBeZero
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import java.util.*


class AccountServiceTests(private val accountService: AccountService) : BaseIntegrationTest() {

    @Test
    fun `should add deposit to account`() {
        val accountId = UUID.randomUUID()
        transaction {
            account { id = accountId }
        }

        val request = DepositRequest(amount = randomAmount())
        val idempotencyKey = randomString()
        val view = accountService.addDeposit(accountId, idempotencyKey, request)
        transaction {
            val account = Account.findOrException(accountId)
            account.balance.shouldBe(request.amount)

            val accountTransaction = account.transactions.first()
            accountTransaction.amount.shouldBe(request.amount)
            accountTransaction.type.shouldBe(AccountTransactionType.DEPOSIT)
            accountTransaction.order.shouldBe(AccountTransactionTable.orderSequence.lastValue())
            accountTransaction.idempotencyKey.shouldBe(idempotencyKey)
            accountTransaction.eventSentAt.shouldBeNull()

            view.id.shouldBe(accountTransaction.id.value)
            view.amount.shouldBe(accountTransaction.amount)
            view.type.shouldBe(accountTransaction.type)
            view.occurredAt.shouldBeIgnoreNanos(accountTransaction.occurredAt)
        }
    }

    @Test
    fun `should not process deposit if it was already processed`() {
        val accountId = UUID.randomUUID()
        val processedTransactionId = UUID.randomUUID()
        val processedIdempotencyKey = randomString()
        transaction {
            account {
                id = accountId
                accountTransaction {
                    id = processedTransactionId
                    idempotencyKey = processedIdempotencyKey
                    type = AccountTransactionType.DEPOSIT
                }
            }
        }

        val request = DepositRequest(amount = randomAmount())
        val view = accountService.addDeposit(accountId, processedIdempotencyKey, request)
        view.id.shouldBe(processedTransactionId)
        transaction {
            Account.findOrException(accountId).transactions.shouldHaveSize(1)
        }
    }

    @Test
    fun `should add deposits concurrently`() {
        val accountId = UUID.randomUUID()
        transaction {
            account { id = accountId }
        }

        val requestsSize = 50
        val requestsByIdempotencyKey = mapRepeat(requestsSize) {
            randomString() to DepositRequest(amount = randomAmount())
        }.toMap()
        runBlocking(Dispatchers.IO) {
            requestsByIdempotencyKey.map { (idempotencyKey, request) ->
                async { accountService.addDeposit(accountId, idempotencyKey, request) }
            }.awaitAll()
        }

        transaction {
            val account = Account.findOrException(accountId)
            account.balance.shouldBe(requestsByIdempotencyKey.values.sumOf { it.amount })
            account.transactions.shouldHaveSize(requestsSize)
            account.transactions.forEach { accountTransaction ->
                val idempotencyKey = accountTransaction.idempotencyKey
                val requestAmount = requestsByIdempotencyKey[idempotencyKey].shouldNotBeNull().amount
                accountTransaction.amount.shouldBe(requestAmount)
            }
        }
    }

    @Test
    fun `should throw not found if account is not found on deposit add`() {
        val accountId = UUID.fromString("a6c41823-d473-441a-aed3-662dc6f4d5ea")
        val request = DepositRequest(amount = randomAmount())
        shouldThrow<AccountNotFoundException> { accountService.addDeposit(accountId, randomString(), request) }
            .message.shouldBe("Account with ID 'a6c41823-d473-441a-aed3-662dc6f4d5ea' is not found")
    }

    @Test
    fun `should add withdrawal to account`() {
        val accountId = UUID.randomUUID()
        transaction {
            account { id = accountId; balance = 100 }
        }

        val request = WithdrawalRequest(amount = 10)
        val idempotencyKey = randomString()
        val view = accountService.addWithdrawal(accountId, idempotencyKey, request)
        transaction {
            val account = Account.findOrException(accountId)
            account.balance.shouldBe(90)

            val accountTransaction = account.transactions.first()
            accountTransaction.amount.shouldBe(request.amount)
            accountTransaction.type.shouldBe(AccountTransactionType.WITHDRAWAL)
            accountTransaction.order.shouldBe(AccountTransactionTable.orderSequence.lastValue())
            accountTransaction.idempotencyKey.shouldBe(idempotencyKey)
            accountTransaction.eventSentAt.shouldBeNull()

            view.id.shouldBe(accountTransaction.id.value)
            view.amount.shouldBe(accountTransaction.amount)
            view.type.shouldBe(accountTransaction.type)
            view.occurredAt.shouldBeIgnoreNanos(accountTransaction.occurredAt)
        }
    }

    @Test
    fun `should not process withdrawal if it was already processed`() {
        val accountId = UUID.randomUUID()
        val processedTransactionId = UUID.randomUUID()
        val processedIdempotencyKey = randomString()
        transaction {
            account {
                id = accountId
                accountTransaction {
                    id = processedTransactionId
                    idempotencyKey = processedIdempotencyKey
                    type = AccountTransactionType.WITHDRAWAL
                }
            }
        }

        val request = WithdrawalRequest(amount = randomAmount())
        val view = accountService.addWithdrawal(accountId, processedIdempotencyKey, request)
        view.id.shouldBe(processedTransactionId)
        transaction {
            Account.findOrException(accountId).transactions.shouldHaveSize(1)
        }
    }

    @Test
    fun `should add withdrawals concurrently`() {
        val accountId = UUID.randomUUID()
        val accountInitialBalance = 10000L
        transaction {
            account { id = accountId; balance = accountInitialBalance }
        }

        val requestsSize = 50
        val requestsByIdempotencyKey = mapRepeat(requestsSize) {
            randomString() to WithdrawalRequest(amount = randomAmount(from = 1, until = 99))
        }.toMap()
        runBlocking(Dispatchers.IO) {
            requestsByIdempotencyKey.map { (idempotencyKey, request) ->
                async { accountService.addWithdrawal(accountId, idempotencyKey, request) }
            }.awaitAll()
        }

        transaction {
            val account = Account.findOrException(accountId)
            val requestAmountSum = requestsByIdempotencyKey.values.sumOf { it.amount }
            account.balance.shouldBe(accountInitialBalance - requestAmountSum)
            account.transactions.shouldHaveSize(requestsSize)
            account.transactions.forEach { accountTransaction ->
                val idempotencyKey = accountTransaction.idempotencyKey
                val requestAmount = requestsByIdempotencyKey[idempotencyKey].shouldNotBeNull().amount
                accountTransaction.amount.shouldBe(requestAmount)
            }
        }
    }

    @Test
    fun `should throw insufficient balance exception on withdrawal adding`() {
        val accountId = UUID.fromString("f944fb2f-a3d4-4565-abc9-628e64aa7869")
        transaction {
            account { id = accountId; balance = 10L }
        }

        val request = WithdrawalRequest(amount = 11)
        shouldThrow<InsufficientBalanceException> { accountService.addWithdrawal(accountId, randomString(), request) }
            .message.shouldBe("Insufficient balance for account with ID 'f944fb2f-a3d4-4565-abc9-628e64aa7869'")
    }

    @Test
    fun `should throw insufficient balance exception on concurrent withdrawal adding`() {
        val accountId = UUID.randomUUID()
        val accountInitialBalance = 90L
        transaction {
            account { id = accountId; balance = accountInitialBalance }
        }

        val exceptionsByIdempotencyKey = concurrentHashMap<String, Throwable>()
        val requestsByIdempotencyKey = mapRepeat(100) {
            randomString() to WithdrawalRequest(amount = 1L)
        }.toMap()
        runBlocking(Dispatchers.IO) {
            requestsByIdempotencyKey.map { (idempotencyKey, request) ->
                async {
                    runCatching { accountService.addWithdrawal(accountId, idempotencyKey, request) }
                        .onFailure { exceptionsByIdempotencyKey[idempotencyKey] = it }
                }
            }.awaitAll()
        }

        exceptionsByIdempotencyKey.values.forAll { it.shouldBeInstanceOf<InsufficientBalanceException>() }
        exceptionsByIdempotencyKey.shouldHaveSize(10)
        transaction {
            val account = Account.findOrException(accountId)
            account.balance.shouldBeZero()
            val accountTransactions = account.transactions.toList()
            accountTransactions.shouldHaveSize(90)
            accountTransactions.forAll { it.amount.shouldBe(1) }
        }
    }

    @Test
    fun `should throw not found if account is not found on withdrawal add`() {
        val accountId = UUID.fromString("52f5cc0f-9881-43ea-9bde-10d91c4c7975")
        val request = WithdrawalRequest(amount = randomAmount())
        shouldThrow<AccountNotFoundException> { accountService.addWithdrawal(accountId, randomString(), request) }
            .message.shouldBe("Account with ID '52f5cc0f-9881-43ea-9bde-10d91c4c7975' is not found")
    }
}