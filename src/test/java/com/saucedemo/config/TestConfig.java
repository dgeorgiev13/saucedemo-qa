package com.saucedemo.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

/**
 * Central test configuration.
 * <p>
 * Resolution order for every key (first match wins):
 * <ol>
 *     <li>JVM system property, e.g. {@code -Dheadless=false}</li>
 *     <li>Environment variable in UPPER_SNAKE_CASE, e.g. {@code HEADLESS=false}</li>
 *     <li>{@code config.properties} on the test classpath</li>
 * </ol>
 */
public final class TestConfig {

    private static final String CONFIG_FILE = "config.properties";
    private static final Properties FILE_PROPERTIES = PropertiesLoader.load(CONFIG_FILE);

    private TestConfig() {
    }

    public static String baseUrl() {
        return required("base.url");
    }

    public static BrowserType browser() {
        return BrowserType.from(required("browser"));
    }

    public static boolean headless() {
        return Boolean.parseBoolean(required("headless"));
    }

    public static double slowMo() {
        return Double.parseDouble(required("slow.mo"));
    }

    public static double defaultTimeoutMs() {
        return Double.parseDouble(required("default.timeout.ms"));
    }

    public static String standardUserPrefix() {
        return required("credentials.standard.user.prefix");
    }

    public static Path artifactsDir() {
        return Path.of(required("artifacts.dir"));
    }

    /** Optional value - used for secrets such as credentials that must never live in the repository. */
    public static Optional<String> optional(String key) {
        String fromSystem = System.getProperty(key);
        if (isPresent(fromSystem)) {
            return Optional.of(fromSystem);
        }
        String fromEnv = System.getenv(toEnvName(key));
        if (isPresent(fromEnv)) {
            return Optional.of(fromEnv);
        }
        return Optional.ofNullable(FILE_PROPERTIES.getProperty(key)).filter(TestConfig::isPresent);
    }

    private static String required(String key) {
        return optional(key).orElseThrow(() -> new IllegalStateException(
                "Missing configuration key '%s' (system property, env %s or %s)"
                        .formatted(key, toEnvName(key), CONFIG_FILE)));
    }

    private static String toEnvName(String key) {
        return key.toUpperCase().replace('.', '_');
    }

    private static boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }

    public enum BrowserType {
        CHROMIUM, FIREFOX, WEBKIT;

        static BrowserType from(String value) {
            try {
                return valueOf(value.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Unsupported browser: " + value, e);
            }
        }
    }

    /** Loads a properties file from the test classpath. */
    public static final class PropertiesLoader {

        private PropertiesLoader() {
        }

        public static Properties load(String resource) {
            Properties properties = new Properties();
            try (InputStream in = TestConfig.class.getClassLoader().getResourceAsStream(resource)) {
                if (in == null) {
                    throw new IllegalStateException("Resource not found on classpath: " + resource);
                }
                properties.load(in);
                return properties;
            } catch (IOException e) {
                throw new UncheckedIOException("Cannot read " + resource, e);
            }
        }
    }
}
