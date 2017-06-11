package org.stuctmapper.expression.matchers;

import java.util.EnumSet;
import java.util.Set;

import org.stuctmapper.expression.IExpressionMatcher;
import org.stuctmapper.expression.IMatcherContext;
import org.stuctmapper.expression.MatcherType;

public class MethodCallStartMatcher implements IExpressionMatcher {
    private static final Set<MatcherType> FALLBACK1_MATCHERS = EnumSet.of(MatcherType.METHOD_CALL_END);
    private static final Set<MatcherType> FALLBACK2_MATCHERS = EnumSet.of(MatcherType.METHOD_CALL_COMMA);
    private static final Set<MatcherType> NEXT_MATCHERS = EnumSet.of(MatcherType.ID, MatcherType.WILDCARD, 
            MatcherType.WILDCARD_INDEX_START, MatcherType.MULTIPLIER_SOURCE, MatcherType.MULTIPLIER_RECEIVER);

    @Override
    public boolean matches(final char ch) {
        final boolean result = ch == '(';
        return result;
    }

    @Override
    public void sequenceEnd(final IMatcherContext context) {
        context.push(FALLBACK1_MATCHERS);
        context.push(FALLBACK2_MATCHERS);
    }

    @Override
    public Set<MatcherType> nextMatchers(final IMatcherContext context) {
        return NEXT_MATCHERS;
    }

    @Override
    public boolean isTerminal() {
        return false;
    }

}
