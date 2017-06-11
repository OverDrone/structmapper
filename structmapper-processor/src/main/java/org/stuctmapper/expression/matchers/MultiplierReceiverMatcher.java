package org.stuctmapper.expression.matchers;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.stuctmapper.expression.IExpressionMatcher;
import org.stuctmapper.expression.IExpressionToken;
import org.stuctmapper.expression.IMatcherContext;
import org.stuctmapper.expression.MatcherType;
import org.stuctmapper.expression.rawtokens.RawMultiplierReceiverToken;

public class MultiplierReceiverMatcher implements IExpressionMatcher {
    private static final Set<MatcherType> NEXT_MATCHERS = Collections.emptySet();

    @Override
    public boolean matches(final char ch) {
        final boolean result = ch == '#';
        return result;
    }

    @Override
    public void sequenceEnd(final IMatcherContext context) {
        final RawMultiplierReceiverToken token = new RawMultiplierReceiverToken();
        final List<IExpressionToken> tokens = context.getTokens();
        tokens.add(token);
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
