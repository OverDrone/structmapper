package org.stuctmapper.instructions;

import java.util.HashMap;
import java.util.Map;

import org.stuctmapper.utils.CollectionUtils;

import com.google.common.base.Preconditions;

public class VariableNameGenerator {
    private static final String DEFAULT_VARIABLE_NAME = "var";
    private final String defaultName;
    private final Map<String, Integer> names = new HashMap<>();
    
    public VariableNameGenerator() {
        this(DEFAULT_VARIABLE_NAME);
    }
    
    public VariableNameGenerator(final String defaultName) {
        Preconditions.checkNotNull(defaultName);
        this.defaultName = defaultName;
    }
    
    public String getNext() {
        final String result = getNext(defaultName);
        return result;
    }
    
    public String getNext(final String variableName) {
        if (variableName == null) {
            final String result = getNext();
            return result;
        } else {
            Integer index = names.get(variableName);
            if (index == null) {
                index = 1;
                CollectionUtils.addNew(names, variableName, index);
            } else {
                index++;
                names.put(variableName, index);
            }
            final String result;
            if (index == 1) {
                result = variableName;
            } else {
                result = variableName + index;
            }
            return result;
        }
    }
}
