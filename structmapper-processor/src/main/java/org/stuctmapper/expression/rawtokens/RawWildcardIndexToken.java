package org.stuctmapper.expression.rawtokens;

import org.stuctmapper.expression.IExpressionToken;

public class RawWildcardIndexToken implements IExpressionToken {
    private final int index;

    public RawWildcardIndexToken(final int index) {
        super();
        this.index = index;
    }
    
    public int getIndex() {
        return index;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{index=" + index + "}";
    }
    
}
