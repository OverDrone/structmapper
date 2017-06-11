package org.stuctmapper.expression.rawtokens;

import org.stuctmapper.expression.IExpressionToken;

public class RawDotToken implements IExpressionToken {
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
