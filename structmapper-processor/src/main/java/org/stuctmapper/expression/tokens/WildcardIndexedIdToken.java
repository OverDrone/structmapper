package org.stuctmapper.expression.tokens;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Идентификатор с применением индексов для "*"
 */
public class WildcardIndexedIdToken implements ICompositeIdTokenItem {
    private final List<IWildcardIndexedIdTokenItem> items;

    public WildcardIndexedIdToken(final List<IWildcardIndexedIdTokenItem> items) {
        super();
        this.items = ImmutableList.copyOf(items);
    }
    
    public List<IWildcardIndexedIdTokenItem> getItems() {
        return items;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{items=" + items + "}";
    }
}
