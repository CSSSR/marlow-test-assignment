package com.assignment.atm.account

import com.assignment.atm.*
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import java.time.LocalDateTime
import java.util.*

class DepositRequest(
    @get:Min(1)
    @Schema(description = AMOUNT_DESCRIPTION, example = AMOUNT_EXAMPLE)
    val amount: Long
)

class WithdrawalRequest(
    @get:Min(1)
    @Schema(description = AMOUNT_DESCRIPTION, example = AMOUNT_EXAMPLE)
    val amount: Long
)

class AccountTransactionView(
    @Schema(description = ACCOUNT_TRANSACTION_ID_DESCRIPTION, example = UUID_EXAMPLE)
    val id: UUID,
    @Schema(description = AMOUNT_DESCRIPTION, example = AMOUNT_EXAMPLE)
    val amount: Long,
    @Schema(description = ACCOUNT_TRANSACTION_TYPE_DESCRIPTION, example = ACCOUNT_TRANSACTION_TYPE_EXAMPLE)
    val type: AccountTransactionType,
    @Schema(description = ACCOUNT_TRANSACTION_OCCURRED_AT_DESCRIPTION, example = DATE_TIME_EXAMPLE)
    val occurredAt: LocalDateTime
)