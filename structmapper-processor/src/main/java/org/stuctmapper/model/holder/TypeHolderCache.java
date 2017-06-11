package org.stuctmapper.model.holder;

import java.util.HashMap;
import java.util.Map;

import org.stuctmapper.utils.CollectionUtils;

public class TypeHolderCache {
    private Map<AbstractTypeHolder, AbstractTypeHolder> map = new HashMap<>();
    
    public AbstractTypeHolder register(final AbstractTypeHolder type) {
        final AbstractTypeHolder existing = map.get(type);
        if (existing != null) {
            return existing;
        } else {
            CollectionUtils.addNew(map, type, type);
            return type;
        }
    }
}
