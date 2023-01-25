package com.assignment.atm.exposed

import com.assignment.atm.NotFoundException
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass

fun <ID : Comparable<ID>, E : Entity<ID>> EntityClass<ID, E>.findOrException(id: ID): E {
    return this.find { table.id.eq(id) }.firstOrNull()
        ?: throw NotFoundException("Entity with id '$id' is not found")
}
