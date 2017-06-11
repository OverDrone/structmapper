package org.stuctmapper.expression.matchers;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.stuctmapper.expression.IExpressionMatcher;
import org.stuctmapper.expression.IExpressionToken;
import org.stuctmapper.expression.IMatcherContext;
import org.stuctmapper.expression.MatcherType;
import org.stuctmapper.expression.rawtokens.RawMethodArgumentToken;
import org.stuctmapper.expression.rawtokens.RawMethodArgumentsToken;

public class MethodCallEndMatcher implements IExpressionMatcher {
    private static final Set<MatcherType> NEXT_MATCHERS = EnumSet.of(MatcherType.DOT);

    @Override
    public boolean matches(final char ch) {
        final boolean result = ch == ')';
        return result;
    }

    @Override
    public void sequenceEnd(final IMatcherContext context) {
        final List<IExpressionToken> tokenList = context.pop();
        final List<RawMethodArgumentToken> complexTokenList = context.pop();
        final List<IExpressionToken> tokens = context.getTokens();
        if (!tokenList.isEmpty()) {
            final RawMethodArgumentToken complexToken = new RawMethodArgumentToken(tokenList);
            complexTokenList.add(complexToken);
        }
        final RawMethodArgumentsToken argumentsToken = new RawMethodArgumentsToken(complexTokenList);
        tokens.add(argumentsToken);
    }

    @Override
    public Set<MatcherType> nextMatchers(IMatcherContext context) {
        return NEXT_MATCHERS;
    }

    @Override
    public boolean isTerminal() {
        return true;
    }
    
}
