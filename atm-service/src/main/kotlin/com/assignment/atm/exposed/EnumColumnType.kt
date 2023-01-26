package com.assignment.atm.exposed

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

inline fun <reified T : Enum<T>> Table.enum(name: String): Column<T> {
    return enumerationByName(name, 32, T::class)
}
