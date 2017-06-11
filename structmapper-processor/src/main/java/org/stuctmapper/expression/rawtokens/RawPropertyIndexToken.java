package org.stuctmapper.expression.rawtokens;

import java.util.List;

import org.stuctmapper.expression.IExpressionToken;

public class RawPropertyIndexToken extends RawComplexToken<IExpressionToken> {
    public RawPropertyIndexToken(final List<IExpressionToken> tokens) {
        super(tokens);
    }
}
