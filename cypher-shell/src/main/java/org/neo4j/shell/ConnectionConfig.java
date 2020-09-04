package org.neo4j.shell;

import javax.annotation.Nonnull;

import org.neo4j.shell.cli.Encryption;

public class ConnectionConfig {
    public static final String USERNAME_ENV_VAR = "NEO4J_USERNAME";
    public static final String PASSWORD_ENV_VAR = "NEO4J_PASSWORD";
    public static final String DATABASE_ENV_VAR = "NEO4J_DATABASE";

    private final String scheme;
    private final String host;
    private final int port;
    private final Encryption encryption;
    private String username;
    private String password;
    private String newPassword;
    private String database;

    public ConnectionConfig(@Nonnull String scheme,
                            @Nonnull String host,
                            int port,
                            @Nonnull String username,
                            @Nonnull String password,
                            Encryption encryption,
                            @Nonnull String database) {
        this.host = host;
        this.port = port;
        this.username = fallbackToEnvVariable(username, USERNAME_ENV_VAR);
        this.password = fallbackToEnvVariable(password, PASSWORD_ENV_VAR);
        this.encryption = encryption;
        this.scheme = scheme;
        this.database = fallbackToEnvVariable(database, DATABASE_ENV_VAR);
    }

    /**
     * @return preferredValue if not empty, else the contents of the fallback environment variable
     */
    @Nonnull
    private static String fallbackToEnvVariable(@Nonnull String preferredValue, @Nonnull String fallbackEnvVar) {
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

    public String newPassword() {
        return newPassword;
    }

    @Nonnull
    public String driverUrl() {
        return String.format("%s://%s:%d", scheme(), host(), port());
    }

    @Nonnull
    public Encryption encryption() {
        return encryption;
    }

    @Nonnull
    public String database() {
        return database;
    }

    public void setUsername(@Nonnull String username) {
        this.username = username;
    }

    public void setPassword(@Nonnull String password) {
        this.password = password;
    }

    public void setNewPassword( String password) {
        this.newPassword = password;
    }

    public boolean passwordChangeRequired() {
        return this.newPassword != null;
    }
}
