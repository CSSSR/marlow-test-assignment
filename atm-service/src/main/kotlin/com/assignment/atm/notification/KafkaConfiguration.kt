package com.assignment.atm.notification

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.util.StdDateFormat
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.kafka.common.serialization.UUIDSerializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.serializer.JsonSerializer
import java.util.*

@Configuration
class KafkaConfiguration(
    private val kafkaProperties: KafkaProperties,
    private val notificationConfigurationProperties: NotificationConfigurationProperties
) {

    @Bean
    fun cdcNotificator(): AccountTransactionNotifier {
        return AccountTransactionNotifier(kafkaTemplate(), notificationConfigurationProperties)
    }

    private fun producerFactory(): DefaultKafkaProducerFactory<UUID, AccountTransactionAddedEvent> {
        val jsonSerializer = JsonSerializer<AccountTransactionAddedEvent>(KAFKA_OBJECT_MAPPER).apply {
            isAddTypeInfo = false
        }
        val producerProperties = kafkaProperties.buildProducerProperties()
        return DefaultKafkaProducerFactory(producerProperties, UUIDSerializer(), jsonSerializer)
    }

    private fun kafkaTemplate(): KafkaTemplate<UUID, AccountTransactionAddedEvent> {
        return KafkaTemplate(producerFactory())
    }

    companion object {
        val KAFKA_OBJECT_MAPPER: ObjectMapper = jacksonObjectMapper()
            .findAndRegisterModules()
            .apply { dateFormat = StdDateFormat() }
    }
}