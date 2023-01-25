package com.assignment.atm.notification

import com.assignment.atm.account.AccountTransactionType
import java.time.LocalDateTime
import java.util.*

class AccountTransactionAddedEvent(
    val transactionId: UUID,
    val amount: Long,
    val type: AccountTransactionType,
    val occurredAt: LocalDateTime
)