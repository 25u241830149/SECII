package com.campushub.common.testing;

import java.nio.file.Path;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers(disabledWithoutDocker = true)
public abstract class ContainerizedIntegrationTestSupport {

    // PostGIS is used to keep integration tests aligned with production SQL features.
    private static final DockerImageName POSTGIS_IMAGE = DockerImageName.parse("postgis/postgis:16-3.4")
            .asCompatibleSubstituteFor("postgres");
    private static final DockerImageName REDIS_IMAGE = DockerImageName.parse("redis:7-alpine");

    @Container
    protected static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(POSTGIS_IMAGE)
            .withDatabaseName("campushub_test")
            .withUsername("postgres")
            .withPassword("postgres");

    @Container
    protected static final GenericContainer<?> REDIS = new GenericContainer<>(REDIS_IMAGE)
            .withExposedPorts(6379);

    protected static void registerContainerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);

        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
        registry.add("spring.data.redis.database", () -> 0);
        registry.add("campushub.redisson.address",
                () -> "redis://" + REDIS.getHost() + ":" + REDIS.getMappedPort(6379));
        registry.add("campushub.redisson.database", () -> 0);
        registry.add("campushub.audit.sensitive-word.enabled", () -> false);
    }

    protected static void registerSchemaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.sql.init.mode", () -> "always");
        registry.add("spring.sql.init.schema-locations", ContainerizedIntegrationTestSupport::schemaLocation);
    }

    private static String schemaLocation() {
        Path schemaPath = Path.of("..", "campushub-bootstrap", "src", "main", "resources", "db", "schema.sql")
                .toAbsolutePath()
                .normalize();
        return "file:" + schemaPath;
    }
}
