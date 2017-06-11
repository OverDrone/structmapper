package org.stuctmapper.expression.tokens;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Идентификатор с применением "*"
 */
public class WildcardIdToken implements ICompositeIdTokenItem {
    private final List<IWildcardIdTokenItem> items;

    public WildcardIdToken(final List<IWildcardIdTokenItem> items) {
        super();
        this.items = ImmutableList.copyOf(items);
    }
    
    public List<IWildcardIdTokenItem> getItems() {
        return items;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{items=" + items + "}";
    }
}
