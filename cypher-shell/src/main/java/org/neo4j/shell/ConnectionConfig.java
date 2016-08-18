package org.neo4j.shell;

import javax.annotation.Nonnull;

public class ConnectionConfig {
    @Nonnull
    private final String host;
    private final int port;
    @Nonnull
    private final String username;
    @Nonnull
    private final String password;

    public ConnectionConfig(@Nonnull String host, int port, @Nonnull String username, @Nonnull String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
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
}
