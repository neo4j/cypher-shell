package org.neo4j.shell;

import jline.console.ConsoleReader;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandReader {
    private final ConsoleReader reader;
    private final Shell shell;
    static final Pattern MULTILINE_BREAK = Pattern.compile("\\\\\\s*$");
    static final Pattern COMMENTS = Pattern.compile("//.*$");

    public CommandReader(ConsoleReader reader, Shell shell) {
        this.reader = reader;
        this.shell = shell;
    }

    @Nullable
    public String readCommand() throws IOException {
        StringBuffer stringBuffer = new StringBuffer();
        boolean reading = true;
        while (reading) {
            String line = reader.readLine(shell.prompt());
            if (line == null) {
                reading = false;
                if (stringBuffer.length() == 0) {
                    return null;
                }
            } else {
                Matcher matcher = MULTILINE_BREAK.matcher(commentSubstitutedLine(line));
                if (!matcher.find()) {
                    reading = false;
                }
                stringBuffer.append(matcher.replaceAll("")).append("\n");
            }
        }
        return stringBuffer.toString();
    }

    private String commentSubstitutedLine(String line) {
        Matcher commentsMatcher = COMMENTS.matcher(line);
        return commentsMatcher.replaceAll("");
    }
}
