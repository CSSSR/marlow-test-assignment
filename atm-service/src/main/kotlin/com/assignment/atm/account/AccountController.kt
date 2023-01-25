package com.assignment.atm.account

import com.assignment.atm.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/accounts")
class AccountController(private val accountService: AccountService) {

    @PostMapping("/{accountId}/deposits")
    @Operation(summary = "Add deposit")
    @ApiResponse(responseCode = "201", description = "Deposit added successfully")
    @ResponseStatus(HttpStatus.CREATED)
    fun addDeposit(
        @Parameter(description = ACCOUNT_ID_DESCRIPTION, example = UUID_EXAMPLE)
        @PathVariable accountId: UUID,
        @Parameter(description = IDEMPOTENCY_KEY_DESCRIPTION, example = IDEMPOTENCY_KEY_EXAMPLE)
        @RequestHeader(IDEMPOTENCY_KEY_HEADER) idempotencyKey: String,
        @RequestBody @Valid request: DepositRequest
    ): AccountTransactionView {
        return accountService.addDeposit(accountId, idempotencyKey, request)
    }

    @PostMapping("/{accountId}/withdrawals")
    @Operation(summary = "Add withdrawal")
    @ApiResponse(responseCode = "201", description = "Withdrawal added successfully")
    @ResponseStatus(HttpStatus.CREATED)
    fun addWithdrawal(
        @Parameter(description = ACCOUNT_ID_DESCRIPTION, example = UUID_EXAMPLE)
        @PathVariable accountId: UUID,
        @Parameter(description = IDEMPOTENCY_KEY_DESCRIPTION, example = IDEMPOTENCY_KEY_EXAMPLE)
        @RequestHeader(IDEMPOTENCY_KEY_HEADER) idempotencyKey: String,
        @RequestBody @Valid request: WithdrawalRequest
    ): AccountTransactionView {
        return accountService.addWithdrawal(accountId, idempotencyKey, request)
    }
}