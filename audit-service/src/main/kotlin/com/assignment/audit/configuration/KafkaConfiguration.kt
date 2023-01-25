package com.assignment.audit.configuration

import com.assignment.audit.AccountTransactionAddedEvent
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.util.StdDateFormat
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import mu.KotlinLogging
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.UUIDDeserializer
import org.apache.kafka.common.serialization.UUIDSerializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.CommonErrorHandler
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.util.backoff.FixedBackOff
import java.time.Duration
import java.util.*


@Configuration
class KafkaConfiguration(private val kafkaProperties: KafkaProperties) {
    private val logger = KotlinLogging.logger {}

    @Bean(name = [ACCOUNT_TRANSACTION_ADDED_CONTAINER_FACTORY])
    fun accountTransactionAddedContainerFactory(): ConcurrentKafkaListenerContainerFactory<UUID, AccountTransactionAddedEvent> {
        return ConcurrentKafkaListenerContainerFactory<UUID, AccountTransactionAddedEvent>().apply {
            consumerFactory = consumerFactory()
            setCommonErrorHandler(errorHandler())
            containerProperties.ackMode = ContainerProperties.AckMode.RECORD
            afterPropertiesSet()
        }
    }

    private fun consumerFactory(): ConsumerFactory<UUID, AccountTransactionAddedEvent> {
        val jsonDeserializer = JsonDeserializer(AccountTransactionAddedEvent::class.java, KAFKA_OBJECT_MAPPER).apply {
            this.addTrustedPackages("com.assignment.*")
            ignoreTypeHeaders()
        }
        val errorHandlingDeserializer = ErrorHandlingDeserializer(jsonDeserializer)
        return DefaultKafkaConsumerFactory(kafkaProperties.buildConsumerProperties(), UUIDDeserializer(), errorHandlingDeserializer)
    }

    private fun errorHandler(): CommonErrorHandler {
        val backoff = FixedBackOff(Duration.ofSeconds(2).toMillis(), 2)

        val dltByteArrayKafkaTemplate = KafkaTemplate(dltByteArrayProducerFactory())
        val dltJsonKafkaTemplate = KafkaTemplate(dltJsonProducerFactory())
        val templates: Map<Class<*>, KafkaOperations<*, *>> = linkedMapOf(
            ByteArray::class.java to dltByteArrayKafkaTemplate,
            Any::class.java to dltJsonKafkaTemplate
        )

        val recoverer = DeadLetterPublishingRecoverer(templates) { consumerRecord, exception ->
            logger.error(exception) { "Exception occurred while handling a message with key '${consumerRecord.key()}'" }
            val deadLetterTopic = "${consumerRecord.topic()}.${kafkaProperties.consumer.groupId}.DLT"
            TopicPartition(deadLetterTopic, consumerRecord.partition())
        }
        return DefaultErrorHandler(recoverer, backoff)
    }

    private fun dltByteArrayProducerFactory(): DefaultKafkaProducerFactory<UUID, ByteArray> {
        val serializer = ByteArraySerializer()
        val producerProperties = kafkaProperties.buildProducerProperties()
        return DefaultKafkaProducerFactory(producerProperties, UUIDSerializer(), serializer)
    }

    private fun dltJsonProducerFactory(): DefaultKafkaProducerFactory<UUID, Any> {
        val jsonSerializer = JsonSerializer<Any>(KAFKA_OBJECT_MAPPER).apply {
            isAddTypeInfo = false
        }
        val producerProperties = kafkaProperties.buildProducerProperties()
        return DefaultKafkaProducerFactory(producerProperties, UUIDSerializer(), jsonSerializer)
    }

    companion object {
        val KAFKA_OBJECT_MAPPER: ObjectMapper = jacksonObjectMapper()
            .findAndRegisterModules()
            .apply { dateFormat = StdDateFormat() }
        const val ACCOUNT_TRANSACTION_ADDED_CONTAINER_FACTORY = "accountTransactionAddedContainerFactory"
    }
}