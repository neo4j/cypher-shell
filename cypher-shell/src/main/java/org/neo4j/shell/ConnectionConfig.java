package org.neo4j.shell;

import org.neo4j.driver.v1.Config;
import org.neo4j.shell.log.AnsiFormattedText;
import org.neo4j.shell.log.Logger;

import javax.annotation.Nonnull;

public class ConnectionConfig {
    private final String scheme;
    private final String host;
    private final int port;
    private final Config.EncryptionLevel encryption;
    private String username;
    private String password;

    public ConnectionConfig(@Nonnull Logger logger, @Nonnull String scheme, @Nonnull String host, int port,
                            @Nonnull String username, @Nonnull String password, boolean encryption) {
        this.host = host;
        this.port = port;
        this.username = fallbackToEnvVariable(username, "NEO4J_USERNAME");
        this.password = fallbackToEnvVariable(password, "NEO4J_PASSWORD");
        this.encryption = encryption ? Config.EncryptionLevel.REQUIRED : Config.EncryptionLevel.NONE;

        if ("bolt+routing://".equalsIgnoreCase(scheme)) {
            logger.printError(
                    AnsiFormattedText.s()
                                     .colorRed()
                                     .append("Routing is not supported by cypher-shell. Falling back to direct connection.")
                                     .formattedString());
            scheme = "bolt://";
        }
        this.scheme = scheme;
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

    @Nonnull
    public String scheme() {
        return scheme;
    }

    @Nonnull
    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    @Nonnull
    public String username() {
        return username;
    }

    @Nonnull
    public String password() {
        return password;
    }

    @Nonnull
    public String driverUrl() {
        return String.format("%s%s:%d", scheme(), host(), port());
    }

    @Nonnull
    public Config.EncryptionLevel encryption() {
        return encryption;
    }

    public void setUsername(@Nonnull String username) {
        this.username = username;
    }

    public void setPassword(@Nonnull String password) {
        this.password = password;
    }
}
