package com.assignment.atm

import com.assignment.atm.account.DepositRequest
import com.assignment.atm.account.WithdrawalRequest
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import jakarta.validation.ConstraintViolation
import jakarta.validation.Validation
import jakarta.validation.constraints.Min
import org.junit.jupiter.api.Test

class AccountRequestsValidationTests {
    private val validator = Validation
        .buildDefaultValidatorFactory()
        .validator

    @Test
    fun `should validate deposit request`() {
        listOf(
            DepositRequest(0),
            DepositRequest(-1)
        ).forEach { request ->
            val violations = validator.validate(request)
            verifyMinAmountConstraint("amount", violations)
        }
    }

    @Test
    fun `should validate withdrawal request`() {
        listOf(
            WithdrawalRequest(0),
            WithdrawalRequest(-1)
        ).forEach { request ->
            val violations = validator.validate(request)
            verifyMinAmountConstraint("amount", violations)
        }
    }

    private fun verifyMinAmountConstraint(expectedPath: String, violations: Set<ConstraintViolation<*>>) {
        violations.shouldHaveSize(1)
        val violation = violations.first()
        violation.constraintDescriptor.annotation.annotationClass.java.shouldBe(Min::class.java)
        violation.propertyPath.toString().shouldBe(expectedPath)
    }
}