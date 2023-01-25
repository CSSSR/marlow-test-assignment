package com.assignment.audit

import com.assignment.audit.configuration.KafkaConfiguration.Companion.KAFKA_OBJECT_MAPPER
import org.apache.kafka.common.serialization.UUIDSerializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.stereotype.Component
import java.util.*

@Component
class KafkaSender(private val kafkaProperties: KafkaProperties) {
    private val kafkaTemplate = kafkaTemplate()

    fun <T> sendMessage(topic: String, key: UUID, value: T) {
        kafkaTemplate.send(topic, key, value).get()
    }

    private fun producerFactory(): DefaultKafkaProducerFactory<UUID, Any> {
        val jsonSerializer = JsonSerializer<Any>(KAFKA_OBJECT_MAPPER).apply { isAddTypeInfo = false }
        val producerProperties = kafkaProperties.buildProducerProperties()
        return DefaultKafkaProducerFactory(producerProperties, UUIDSerializer(), jsonSerializer)
    }

    private fun kafkaTemplate(): KafkaTemplate<UUID, Any> {
        return KafkaTemplate(producerFactory())
    }
}