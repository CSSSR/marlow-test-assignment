package com.assignment.audit

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@ConfigurationPropertiesScan
@SpringBootApplication
class AuditServiceApplication

fun main(args: Array<String>) {
    runApplication<AuditServiceApplication>(*args)
}
