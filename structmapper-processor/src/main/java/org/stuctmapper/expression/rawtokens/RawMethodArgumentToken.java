package org.stuctmapper.expression.rawtokens;

import java.util.List;

import org.stuctmapper.expression.IExpressionToken;

public class RawMethodArgumentToken extends RawComplexToken<IExpressionToken> {
    public RawMethodArgumentToken(final List<IExpressionToken> tokens) {
        super(tokens);
    }
    
}
