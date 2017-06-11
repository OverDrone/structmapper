package org.stuctmapper.expression.matchers;

import java.util.EnumSet;
import java.util.Set;

import org.stuctmapper.expression.IExpressionMatcher;
import org.stuctmapper.expression.IMatcherContext;
import org.stuctmapper.expression.MatcherType;

public class WildcardIndexStartMatcher implements IExpressionMatcher {
    private static final Set<MatcherType> NEXT_MATCHERS = EnumSet.of(MatcherType.WILDCARD_INDEX_VALUE);

    @Override
    public boolean matches(final char ch) {
        final boolean result = ch == '{';
        return result;
    }

    @Override
    public void sequenceEnd(final IMatcherContext context) {
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
