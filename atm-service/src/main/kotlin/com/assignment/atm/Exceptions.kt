package com.assignment.atm

import java.util.*

open class NotFoundException(message: String) : RuntimeException(message)
class AccountNotFoundException(accountId: UUID) : NotFoundException("Account with ID '$accountId' is not found")
class InsufficientBalanceException(accountId: UUID) : RuntimeException("Insufficient balance for account with ID '$accountId'")