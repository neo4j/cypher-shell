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

    public ConnectionConfig(@Nonnull String host, int port, @Nonnull String username, @Nonnull String password, boolean encryption) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.encryption = encryption ? Config.EncryptionLevel.REQUIRED: Config.EncryptionLevel.NONE;
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
