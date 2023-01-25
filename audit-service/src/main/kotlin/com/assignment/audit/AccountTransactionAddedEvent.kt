package com.assignment.audit

import java.time.LocalDateTime
import java.util.*

class AccountTransactionAddedEvent(
    val amount: Long,
    val type: String,
    val occurredAt: LocalDateTime,
    val transactionId: UUID
)