package com.assignment.atm

import com.assignment.atm.account.AccountTable
import com.assignment.atm.account.AccountTransactionTable
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

class ClearDataExtension : AfterEachCallback {
    override fun afterEach(context: ExtensionContext) {
        val tableNames = listOf(AccountTable, AccountTransactionTable).joinToString(separator = ", ") { it.tableName }
        transaction { exec("truncate $tableNames cascade;") }
    }
}
