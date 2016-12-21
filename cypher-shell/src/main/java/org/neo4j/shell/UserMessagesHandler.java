package org.neo4j.shell;

import org.neo4j.shell.commands.Exit;
import org.neo4j.shell.commands.Help;
import org.neo4j.shell.log.AnsiFormattedText;

import javax.annotation.Nonnull;

public class UserMessagesHandler {
    private ConnectionConfig connectionConfig;
    private String serverVersion;

    public UserMessagesHandler(@Nonnull ConnectionConfig connectionConfig, @Nonnull String serverVersion) {
        this.connectionConfig = connectionConfig;
        this.serverVersion = serverVersion;
    }

    @Nonnull
    public String getWelcomeMessage() {
        String neo4j = "Neo4j";
        if (!serverVersion.isEmpty()) {
            neo4j += " " + serverVersion;
        }
        AnsiFormattedText welcomeMessage = AnsiFormattedText.from("Connected to ")
                                                            .append(neo4j)
                                                            .append(" at ")
                                                            .bold().append(connectionConfig.driverUrl()).boldOff();

        if (!connectionConfig.username().isEmpty()) {
            welcomeMessage = welcomeMessage
                    .append(" as user ")
                    .bold().append(connectionConfig.username()).boldOff();
        }

        return welcomeMessage
                .append(".\nType ")
                .bold().append(Help.COMMAND_NAME).boldOff()
                .append(" for a list of available commands or ")
                .bold().append(Exit.COMMAND_NAME).boldOff()
                .append(" to exit the shell.")
                .append("\nNote that Cypher queries must end with a ")
                .bold().append("semicolon.").boldOff().formattedString();
    }

    @Nonnull
    public String getExitMessage() {
        return AnsiFormattedText.s().append("\nBye!").formattedString();
    }
}
