package org.neo4j.shell;

import org.neo4j.driver.v1.Config;

import javax.annotation.Nonnull;

public class ConnectionConfig {
    @Nonnull
    private final String host;
    private final int port;
    @Nonnull
    private final String username;
    @Nonnull
    private final String password;
    @Nonnull
    private final Config.EncryptionLevel encryption;

    public ConnectionConfig(@Nonnull String host, int port, @Nonnull String username, @Nonnull String password,
                            boolean encryption) {
        this.host = host;
        this.port = port;
        this.username = fallbackToEnvVariable(username, "NEO4J_USERNAME");
        this.password = fallbackToEnvVariable(password, "NEO4J_PASSWORD");
        this.encryption = encryption ? Config.EncryptionLevel.REQUIRED : Config.EncryptionLevel.NONE;
    }

    /**
     * @return preferredValue if not empty, else the contents of the fallback environment variable
     */
    @Nonnull
    static String fallbackToEnvVariable(@Nonnull String preferredValue, @Nonnull String fallbackEnvVar) {
        String result = System.getenv(fallbackEnvVar);
        if (result == null || !preferredValue.isEmpty()) {
            result = preferredValue;
        }
        return result;
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }

    public String driverUrl() {
        return String.format("bolt://%s:%d", host(), port());
    }

    public Config.EncryptionLevel encryption() {
        return encryption;
    }
}
