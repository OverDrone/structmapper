package org.stuctmapper.expression.rawtokens;

import java.util.List;

public class RawMethodArgumentsToken extends RawComplexToken<RawMethodArgumentToken> {
    public RawMethodArgumentsToken(final List<RawMethodArgumentToken> tokens) {
        super(tokens);
    }
}
