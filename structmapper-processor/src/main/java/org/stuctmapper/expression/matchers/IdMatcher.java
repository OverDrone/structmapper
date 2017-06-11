package org.stuctmapper.expression.matchers;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.stuctmapper.expression.IExpressionMatcher;
import org.stuctmapper.expression.IExpressionToken;
import org.stuctmapper.expression.IMatcherContext;
import org.stuctmapper.expression.MatcherType;
import org.stuctmapper.expression.rawtokens.RawIdToken;

public class IdMatcher implements IExpressionMatcher {
    private static final Set<MatcherType> NEXT_MATCHERS = EnumSet.of(MatcherType.ID, MatcherType.WILDCARD, MatcherType.WILDCARD_INDEX_START,
            MatcherType.PROPERTY_INDEX_START, MatcherType.METHOD_CALL_START, MatcherType.DOT);
    private StringBuffer stringBuffer = new StringBuffer(1000);
    
    @Override
    public boolean matches(final char ch) {
        switch (ch) {
        case '{':
        case '}':
        case '#':
        case '(':
        case ')':
        case '[':
        case ']':
        case '.':
        case ',':
        case '?':
        case '$':
        case '+':
        case '*':
        case ' ':
        case '\r':
        case '\n':
        case '\t':
            return false;
        default:
            stringBuffer.append(ch);
            return true;
        }
    }
    
    @Override
    public Set<MatcherType> nextMatchers(final IMatcherContext context) {
        return NEXT_MATCHERS;
    }

    @Override
    public void sequenceEnd(final IMatcherContext context) {
        final String id = stringBuffer.toString();
        final RawIdToken token = new RawIdToken(id);
        final List<IExpressionToken> tokens = context.getTokens();
        tokens.add(token);
        reset();
    }

    @Override
    public boolean isTerminal() {
        return true;
    }
    
    @Override
    public void reset() {
        IExpressionMatcher.super.reset();
        stringBuffer.setLength(0);
    }

}
