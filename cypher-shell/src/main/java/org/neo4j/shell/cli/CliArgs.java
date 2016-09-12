package org.neo4j.shell.cli;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class CliArgs {
    private String host = "localhost";
    private int port = 7687;
    private String username = "";
    private String password = "";
    private FailBehavior failBehavior = FailBehavior.FAIL_FAST;
    private Format format = Format.VERBOSE;
    private Optional<String> cypher = Optional.empty();
    private boolean encryption;
    private boolean debugMode;

    /**
     * Set the host to the primary value, or if null, the fallback value.
     */
    void setHost(@Nullable String primary, @Nonnull String fallback) {
        host = primary == null ? fallback : primary;
    }

    /**
     * Set the port to the value.
     */
    void setPort(int port) {
        this.port = port;
    }

    /**
     * Set the username to the primary value, or if null, the fallback value.
     */
    void setUsername(@Nullable String primary, @Nonnull String fallback) {
        username = primary == null ? fallback : primary;
    }

    /**
     * Set the password to the primary value, or if null, the fallback value.
     */
    void setPassword(@Nullable String primary, @Nonnull String fallback) {
        password = primary == null ? fallback : primary;
    }

    /**
     * Set the desired fail behavior
     */
    void setFailBehavior(@Nonnull FailBehavior failBehavior) {
        this.failBehavior = failBehavior;
    }

    /**
     * Set the desired format
     */
    public void setFormat(@Nonnull Format format) {
        this.format = format;
    }

    /**
     * Set the specified cypher string to execute
     */
    public void setCypher(@Nullable String cypher) {
        this.cypher = Optional.ofNullable(cypher);
    }

    /**
     * Set whether the connection should be encrypted
     */
    public void setEncryption(boolean encryption) {
        this.encryption = encryption;
    }

    /**
     * Enable/disable debug mode
     */
    void setDebugMode(boolean enabled) {
        this.debugMode = enabled;
    }

    @Nonnull
    public String getHost() {
        return host;
    }

    @Nonnull
    public int getPort() {
        return port;
    }

    @Nonnull
    public String getUsername() {
        return username;
    }

    @Nonnull
    public String getPassword() {
        return password;
    }

    @Nonnull
    public FailBehavior getFailBehavior() {
        return failBehavior;
    }

    @Nonnull
    public Optional<String> getCypher() {
        return cypher;
    }

    @Nonnull
    public Format getFormat() {
        return format;
    }

    public boolean getEncryption() {
        return encryption;
    }

    public boolean getDebugMode() {
        return debugMode;
    }
}
