package org.stuctmapper.expression.tokens;

/**
 * Индекс для "*"
 */
public class WildcardIndexToken implements IWildcardIndexedIdTokenItem {
    private final int index;

    public WildcardIndexToken(int index) {
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
