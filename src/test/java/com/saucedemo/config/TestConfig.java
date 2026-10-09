package com.saucedemo.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Optional;
import java.util.Properties;

/**
 * Test configuration. A key is resolved from (first match wins):
 * system property, environment variable in UPPER_SNAKE_CASE, {@code config.properties}.
 */
public final class TestConfig {

    private static final Properties FILE = loadProperties("config.properties");

    private TestConfig() {
    }

    public static String baseUrl() {
        return get("base.url");
    }

    public static String browser() {
        return get("browser");
    }

    public static boolean headless() {
        return Boolean.parseBoolean(get("headless"));
    }

    public static double slowMo() {
        return Double.parseDouble(get("slow.mo"));
    }

    public static String standardUserPrefix() {
        return get("credentials.standard.user.prefix");
    }

    public static Optional<String> find(String key) {
        String value = System.getProperty(key);
        if (value == null) {
            value = System.getenv(key.toUpperCase().replace('.', '_'));
        }
        if (value == null) {
            value = FILE.getProperty(key);
        }
        return Optional.ofNullable(value).filter(v -> !v.isBlank());
    }

    private static String get(String key) {
        return find(key).orElseThrow(() -> new IllegalStateException("Missing configuration key: " + key));
    }

    public static Properties loadProperties(String resource) {
        try (InputStream in = TestConfig.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("Resource not found on classpath: " + resource);
            }
            Properties properties = new Properties();
            properties.load(in);
            return properties;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
