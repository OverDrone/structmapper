package org.stuctmapper.expression.matchers;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.stuctmapper.expression.IExpressionMatcher;
import org.stuctmapper.expression.IExpressionToken;
import org.stuctmapper.expression.IMatcherContext;
import org.stuctmapper.expression.MatcherType;
import org.stuctmapper.expression.rawtokens.RawWildcardIndexToken;

public class WildcardIndexValueMatcher implements IExpressionMatcher {
    private static final Set<MatcherType> NEXT_MATCHERS = EnumSet.of(MatcherType.WILDCARD_INDEX_VALUE, MatcherType.WILDCARD_INDEX_END);
    final StringBuffer stringBuffer = new StringBuffer(1000);

    @Override
    public boolean matches(final char ch) {
        final boolean result = ch >= '0' && ch <= '9' && stringBuffer.length() < 10;
        if (result) {
            stringBuffer.append(ch);
        }
        return result;
    }

    @Override
    public void sequenceEnd(final IMatcherContext context) {
        final String stringValue = stringBuffer.toString();
        final int index = Integer.valueOf(stringValue);
        final RawWildcardIndexToken token = new RawWildcardIndexToken(index);
        final List<IExpressionToken> tokens = context.getTokens();
        tokens.add(token);
        reset();
    }

    @Override
    public Set<MatcherType> nextMatchers(final IMatcherContext context) {
        return NEXT_MATCHERS;
    }

    @Override
    public boolean isTerminal() {
        return false;
    }

    @Override
    public void reset() {
        IExpressionMatcher.super.reset();
        stringBuffer.setLength(0);
    }
}
