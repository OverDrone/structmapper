package org.stuctmapper.expression;

import java.util.Set;

public interface IExpressionMatcher {
    boolean matches(char ch);
    void sequenceEnd(IMatcherContext context);
    Set<MatcherType> nextMatchers(IMatcherContext context);
    boolean isTerminal();
    default void reset() {
    }
}
