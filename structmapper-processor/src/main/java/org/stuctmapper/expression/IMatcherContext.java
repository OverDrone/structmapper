package org.stuctmapper.expression;

import java.util.List;
import java.util.Set;

public interface IMatcherContext {
    void push(Set<MatcherType> fallbackMatchers);
    <T extends IExpressionToken> List<T> pop();
    List<IExpressionToken> getTokens();
}
