package org.neo4j.shell;

import org.neo4j.shell.prettyprint.LinePrinter;
import org.neo4j.shell.prettyprint.OutputFormatter;

public class StringLinePrinter implements LinePrinter {

    private StringBuilder sb = new StringBuilder();

    @Override
    public void printOut(String line) {
        sb.append(line).append(OutputFormatter.NEWLINE);
    }

    public void clear() {
        sb.setLength(0);
    }

    public String output() {
        return sb.toString();
    }
}
