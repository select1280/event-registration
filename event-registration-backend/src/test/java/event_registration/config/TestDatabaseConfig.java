package event_registration.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestDatabaseConfig {

    /**
     * 建立獨立的 PostgreSQL 測試容器。
     * 容器生命週期由 Spring 管理，連線資訊自動提供給測試環境
     */
    @Bean
    @ServiceConnection
    public PostgreSQLContainer postgresContainer(){
        return new PostgreSQLContainer("postgres:17")
                .withDatabaseName("event_registration_test")
                .withUsername("test")
                .withPassword("test");
    }
}
