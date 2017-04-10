package org.neo4j.shell.prettyprint;

import org.neo4j.shell.cli.Format;
import org.neo4j.shell.state.BoltResult;

import javax.annotation.Nonnull;

import static java.util.Arrays.asList;

/**
 * Print the result from neo4j in a intelligible fashion.
 */
public class PrettyPrinter {
    private final StatisticsCollector statisticsCollector;
    private final OutputFormatter outputFormatter;

    public PrettyPrinter(@Nonnull Format format) {
        this.statisticsCollector = new StatisticsCollector(format);
        this.outputFormatter = format == Format.VERBOSE ? new TableOutputFormatter() : new SimpleOutputFormatter();
    }

    public String format(@Nonnull final BoltResult result) {
        String infoOutput = outputFormatter.formatInfo(result.getSummary());
        String planOutput = outputFormatter.formatPlan(result.getSummary());
        String statistics = statisticsCollector.collect(result.getSummary());
        String resultOutput = outputFormatter.format(result);
        String footer = outputFormatter.formatFooter(result);
        return OutputFormatter.joinNonBlanks(OutputFormatter.NEWLINE, asList(infoOutput, planOutput, resultOutput, footer, statistics));
    }
}
