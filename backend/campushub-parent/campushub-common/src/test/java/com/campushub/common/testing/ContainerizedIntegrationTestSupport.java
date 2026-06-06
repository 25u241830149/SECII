package com.campushub.common.testing;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.lifecycle.Startables;

public abstract class ContainerizedIntegrationTestSupport {

    // PostGIS is used to keep integration tests aligned with production SQL features.
    private static final DockerImageName POSTGIS_IMAGE = DockerImageName.parse("postgis/postgis:16-3.4")
            .asCompatibleSubstituteFor("postgres");
    private static final DockerImageName REDIS_IMAGE = DockerImageName.parse("redis:7-alpine");
    private static final String TESTCONTAINERS_ENABLED_PROPERTY = "campushub.testcontainers.enabled";
    private static volatile boolean containersStarted;

    @SuppressWarnings("resource")
    protected static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(POSTGIS_IMAGE)
            .withDatabaseName("campushub_test")
            .withUsername("postgres")
            .withPassword("postgres");

    @SuppressWarnings("resource")
    protected static final GenericContainer<?> REDIS = new GenericContainer<>(REDIS_IMAGE)
            .withExposedPorts(6379);

    protected static void registerContainerProperties(DynamicPropertyRegistry registry) {
        if (!testcontainersEnabled()) {
            registerExternalServiceProperties(registry);
            return;
        }
        ensureContainersStarted();
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

    private static boolean testcontainersEnabled() {
        String configured = System.getProperty(
                TESTCONTAINERS_ENABLED_PROPERTY,
                System.getenv().getOrDefault("CAMPUSHUB_TESTCONTAINERS_ENABLED", "true")
        );
        return Boolean.parseBoolean(configured);
    }

    private static synchronized void ensureContainersStarted() {
        if (containersStarted) {
            return;
        }
        Startables.deepStart(Stream.of(POSTGRES, REDIS)).join();
        containersStarted = true;
    }

    private static void registerExternalServiceProperties(DynamicPropertyRegistry registry) {
        String redisHost = envOrDefault("CAMPUSHUB_TEST_REDIS_HOST", "localhost");
        String redisPort = envOrDefault("CAMPUSHUB_TEST_REDIS_PORT", "6379");
        String redisDatabase = envOrDefault("CAMPUSHUB_TEST_REDIS_DATABASE", "0");
        String redisPassword = envOrDefault("CAMPUSHUB_TEST_REDIS_PASSWORD", "");

        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add(
                "spring.datasource.url",
                () -> envOrDefault("CAMPUSHUB_TEST_DATASOURCE_URL", "jdbc:postgresql://localhost:5432/campushub_test")
        );
        registry.add("spring.datasource.username", () -> envOrDefault("CAMPUSHUB_TEST_DATASOURCE_USERNAME", "postgres"));
        registry.add("spring.datasource.password", () -> envOrDefault("CAMPUSHUB_TEST_DATASOURCE_PASSWORD", "postgres"));

        registry.add("spring.data.redis.host", () -> redisHost);
        registry.add("spring.data.redis.port", () -> Integer.parseInt(redisPort));
        registry.add("spring.data.redis.database", () -> Integer.parseInt(redisDatabase));
        registry.add("spring.data.redis.password", () -> redisPassword);
        registry.add(
                "campushub.redisson.address",
                () -> envOrDefault("CAMPUSHUB_TEST_REDISSON_ADDRESS", "redis://" + redisHost + ":" + redisPort)
        );
        registry.add("campushub.redisson.password", () -> redisPassword);
        registry.add("campushub.redisson.database", () -> Integer.parseInt(redisDatabase));
        registry.add("campushub.audit.sensitive-word.enabled", () -> false);
    }

    private static String envOrDefault(String key, String fallback) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? fallback : value;
    }
}
