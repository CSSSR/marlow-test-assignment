package com.assignment.atm.exposed

import org.jetbrains.exposed.spring.SpringTransactionManager
import org.jetbrains.exposed.sql.DatabaseConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.sql.Connection
import javax.sql.DataSource

@Configuration
class ExposedConfiguration {
    @Bean
    fun springTransactionManager(datasource: DataSource, databaseConfig: DatabaseConfig): SpringTransactionManager {
        return SpringTransactionManager(dataSource = datasource, databaseConfig = databaseConfig, showSql = false)
    }

    @Bean
    fun databaseConfig(): DatabaseConfig {
        return DatabaseConfig { defaultIsolationLevel = Connection.TRANSACTION_READ_COMMITTED }
    }
}
