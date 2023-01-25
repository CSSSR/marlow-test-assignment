package com.assignment.audit.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "service")
data class ServiceConfigurationProperties(val accountTransactionAddedTopic: String)