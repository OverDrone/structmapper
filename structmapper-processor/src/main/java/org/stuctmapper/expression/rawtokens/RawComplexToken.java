package org.stuctmapper.expression.rawtokens;

import java.util.List;

import org.stuctmapper.expression.IExpressionToken;

import com.google.common.collect.ImmutableList;

public class RawComplexToken<T extends IExpressionToken> implements IExpressionToken {
    private final List<T> tokens;

    public RawComplexToken(final List<T> tokens) {
        this.tokens = ImmutableList.copyOf(tokens);
    }
    
    public List<T> getTokens() {
        return tokens;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{tokens=" + tokens + "}";
    }
}
