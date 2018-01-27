package org.neo4j.shell.prettyprint;

import org.neo4j.shell.cli.Format;
import org.neo4j.shell.state.BoltResult;

import javax.annotation.Nonnull;

import java.util.Set;
import java.util.function.Consumer;

/**
 * Print the result from neo4j in a intelligible fashion.
 */
public class PrettyPrinter {
    private final StatisticsCollector statisticsCollector;
    private final OutputFormatter outputFormatter;

    public PrettyPrinter(@Nonnull Format format, int width, boolean wrap) {
        this.statisticsCollector = new StatisticsCollector(format);
        this.outputFormatter = format == Format.VERBOSE ? new TableOutputFormatter(width, wrap) : new SimpleOutputFormatter();
    }

    public void format(@Nonnull final BoltResult result, Consumer<String> outputConsumer) {
        Set<OutputFormatter.Capablities> capabilities = outputFormatter.capabilities();

        if (capabilities.contains(OutputFormatter.Capablities.result)) outputFormatter.format(result,outputConsumer);

        if (capabilities.contains(OutputFormatter.Capablities.info)) outputConsumer.accept(outputFormatter.formatInfo(result.getSummary()));
        if (capabilities.contains(OutputFormatter.Capablities.plan)) outputConsumer.accept(outputFormatter.formatPlan(result.getSummary()));
        if (capabilities.contains(OutputFormatter.Capablities.footer)) outputConsumer.accept(outputFormatter.formatFooter(result));
        if (capabilities.contains(OutputFormatter.Capablities.statistics)) outputConsumer.accept(statisticsCollector.collect(result.getSummary()));
    }
}
