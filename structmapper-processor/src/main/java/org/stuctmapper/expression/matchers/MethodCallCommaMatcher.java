package org.stuctmapper.expression.matchers;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.stuctmapper.expression.IExpressionMatcher;
import org.stuctmapper.expression.IExpressionToken;
import org.stuctmapper.expression.IMatcherContext;
import org.stuctmapper.expression.MatcherType;
import org.stuctmapper.expression.rawtokens.RawMethodArgumentToken;

public class MethodCallCommaMatcher implements IExpressionMatcher {
    private static final Set<MatcherType> FALLBACK_MATCHERS = EnumSet.of(MatcherType.METHOD_CALL_COMMA);
    private static final Set<MatcherType> NEXT_MATCHERS = EnumSet.of(MatcherType.ID, MatcherType.WILDCARD, 
            MatcherType.WILDCARD_INDEX_START, MatcherType.MULTIPLIER_SOURCE, MatcherType.MULTIPLIER_RECEIVER);

    @Override
    public boolean matches(final char ch) {
        final boolean result = ch == ',';
        return result;
    }

    @Override
    public void sequenceEnd(final IMatcherContext context) {
        final List<IExpressionToken> list = context.pop();
        final RawMethodArgumentToken token = new RawMethodArgumentToken(list);
        final List<IExpressionToken> tokens = context.getTokens();
        tokens.add(token);
        context.push(FALLBACK_MATCHERS);
    }

    @Override
    public Set<MatcherType> nextMatchers(IMatcherContext context) {
        return NEXT_MATCHERS;
    }

    @Override
    public boolean isTerminal() {
        return false;
    }
}
