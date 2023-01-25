package com.assignment.atm.notification

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "notification")
data class NotificationConfigurationProperties(val transactionAddedTopic: String, val batchSize: Int)