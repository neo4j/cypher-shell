package org.neo4j.shell.prettyprint;

import javax.annotation.Nonnull;

public class ToStringLinePrinter implements LinePrinter {

    final StringBuilder sb;

    public ToStringLinePrinter() {
        this.sb = new StringBuilder();
    }

    @Override
    public void printOut(String line) {
        sb.append(line);
        sb.append(OutputFormatter.NEWLINE);
    }

    @Nonnull
    public String result() {
        return sb.toString();
    }
}
