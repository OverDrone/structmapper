package org.stuctmapper.instructions;

import java.util.HashMap;
import java.util.Map;

import org.stuctmapper.model.instructions.IConverterCodeBlock;
import org.stuctmapper.utils.CollectionUtils;

public class CodeBlockCache implements ICodeBlockCache {
    private Map<IConverterCodeBlock, IConverterCodeBlock> map = new HashMap<>();
    
    @Override
    public <T extends IConverterCodeBlock> T add(final T value) {
        @SuppressWarnings("unchecked")
        final T existing = (T) map.get(value);
        if (existing != null) {
            return existing;
        } else {
            CollectionUtils.addNew(map, value, value);
            return value;
        }
    }
}
