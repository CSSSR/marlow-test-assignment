package com.assignment.atm

import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.TestConstructor
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@SpringBootTest(
    classes = [AtmServiceApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ExtendWith(value = [ClearDataExtension::class])
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class BaseIntegrationTest {

    companion object {
        private val postgreSqlContainer = postgreSqlContainer().also { it.start() }
        private val kafka = kafkaContainer().also { it.start() }

        private fun postgreSqlContainer(): KPostgreSQLContainer {
            return KPostgreSQLContainer("15.1")
                .withUsername("postgresUsername")
                .withPassword("postgresPassword")
                .withTmpFs(mapOf("/var/lib/postgresql/data" to "rw"))
        }

        private fun kafkaContainer(): KafkaContainer {
            return KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.1.9"))
        }

        @JvmStatic
        @DynamicPropertySource
        fun datasourceProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { postgreSqlContainer.jdbcUrl }
            registry.add("spring.datasource.username", postgreSqlContainer::getUsername)
            registry.add("spring.datasource.password", postgreSqlContainer::getPassword)
        }

        @JvmStatic
        @DynamicPropertySource
        fun kafkaProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.kafka.bootstrap-servers") { kafka.bootstrapServers }
        }

        class KPostgreSQLContainer(version: String) : PostgreSQLContainer<KPostgreSQLContainer>("postgres:$version")
    }
}