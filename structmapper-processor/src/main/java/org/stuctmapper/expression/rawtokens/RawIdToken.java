package org.stuctmapper.expression.rawtokens;

import org.stuctmapper.expression.IExpressionToken;

public class RawIdToken implements IExpressionToken {
    private final String id;

    public RawIdToken(final String id) {
        super();
        this.id = id;
    }
    
    public String getId() {
        return id;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id=" + id + "}";
    }
}
