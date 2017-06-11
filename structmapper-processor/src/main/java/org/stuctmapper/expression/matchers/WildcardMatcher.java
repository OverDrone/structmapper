package org.stuctmapper.expression.matchers;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.stuctmapper.expression.IExpressionMatcher;
import org.stuctmapper.expression.IExpressionToken;
import org.stuctmapper.expression.IMatcherContext;
import org.stuctmapper.expression.MatcherType;
import org.stuctmapper.expression.rawtokens.RawWildcardToken;

public class WildcardMatcher implements IExpressionMatcher {
    private static final Set<MatcherType> NEXT_MATCHERS = EnumSet.of(MatcherType.ID, MatcherType.PROPERTY_INDEX_START, MatcherType.METHOD_CALL_START,
            MatcherType.DOT);

    @Override
    public boolean matches(final char ch) {
        final boolean result = ch == '*';
        return result;
    }

    @Override
    public void sequenceEnd(final IMatcherContext context) {
        final RawWildcardToken token = new RawWildcardToken();
        final List<IExpressionToken> tokens = context.getTokens();
        tokens.add(token);
    }

    @Override
    public Set<MatcherType> nextMatchers(final IMatcherContext context) {
        return NEXT_MATCHERS;
    }

    @Override
    public boolean isTerminal() {
        return true;
    }
}
