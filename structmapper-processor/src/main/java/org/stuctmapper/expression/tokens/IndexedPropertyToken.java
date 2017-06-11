package org.stuctmapper.expression.tokens;

import java.util.List;

/**
 * Свойство с индексом. Содержит композитный идентификатор и один аргумент
 */
public class IndexedPropertyToken extends CompositeIdToken implements IComplexToken {
    private CompositeIdToken index;

    public IndexedPropertyToken(final List<ICompositeIdTokenItem> parts, final CompositeIdToken index) {
        super(parts);
        this.index = index;
    }

    public CompositeIdToken getIndex() {
        return index;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{parts=" + getParts() + ",index=" + index + "}";
    }
}
