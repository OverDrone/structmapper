package org.stuctmapper.expression.tokens;

/**
 * Простой строковый идентификатор
 */
public class SimpleIdToken implements IWildcardIdTokenItem, ICompositeIdTokenItem, IWildcardIndexedIdTokenItem {
    private final String id;

    public SimpleIdToken(final String id) {
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
