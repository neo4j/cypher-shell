package org.neo4j.shell;

@FunctionalInterface
public interface TriFunction<IN1, IN2, IN3, OUT> {
    OUT apply(IN1 in1, IN2 in2, IN3 in3);
}
