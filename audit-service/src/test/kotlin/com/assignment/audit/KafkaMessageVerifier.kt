package com.assignment.audit

import com.assignment.audit.configuration.KafkaConfiguration
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.UUIDDeserializer
import org.awaitility.kotlin.await
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.listener.MessageListener
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.stereotype.Component
import java.util.*
import kotlin.reflect.KClass

@Component
class KafkaMessageVerifier(private val kafkaProperties: KafkaProperties) {
    private val objectMapper = KafkaConfiguration.KAFKA_OBJECT_MAPPER

    final inline fun <reified T> checkMessageExists(key: UUID, topic: String): ConsumerRecord<UUID, T> {
        var producerRecord: ConsumerRecord<UUID, T>? = null
        val messageListener = MessageListener { record: ConsumerRecord<UUID, T> ->
            if (record.key() == key) {
                producerRecord = record
            }
        }

        startContainer(topic, messageListener, T::class)
        await.until { producerRecord != null }
        return producerRecord!!
    }

    fun startContainer(topic: String, messageListener: MessageListener<UUID, *>, clazz: KClass<*>) {
        val consumerProperties = kafkaProperties.buildConsumerProperties()
        val jsonDeserializer = ErrorHandlingDeserializer(JsonDeserializer(clazz.java, objectMapper).apply {
            this.addTrustedPackages("com.assignment.*")
        })
        val consumerFactory = DefaultKafkaConsumerFactory(consumerProperties, UUIDDeserializer(), jsonDeserializer)
        val containerProperties = ContainerProperties(topic).apply {
            this.setGroupId(UUID.randomUUID().toString())
            this.messageListener = messageListener
        }
        val container = KafkaMessageListenerContainer(consumerFactory, containerProperties).also { it.start() }
        container.start()
    }
}