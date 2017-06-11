package org.stuctmapper.expression.tokens;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Составной идентификатор объекта/свойства/метода 
 */
public class CompositeIdToken {
    private final List<ICompositeIdTokenItem> parts;

    public CompositeIdToken(final List<ICompositeIdTokenItem> parts) {
        super();
        this.parts = ImmutableList.copyOf(parts);
    }
    
    public List<ICompositeIdTokenItem> getParts() {
        return parts;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{parts=" + parts + "}";
    }
}
