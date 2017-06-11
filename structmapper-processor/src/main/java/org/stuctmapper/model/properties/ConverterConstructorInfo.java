package org.stuctmapper.model.properties;

import java.util.List;

import org.stuctmapper.model.holder.AbstractTypeHolder;

import com.google.common.collect.ImmutableList;

public class ConverterConstructorInfo {
    private final AbstractTypeHolder type;
    private final List<ConverterConstructorMethodInfo> constructors;
    
    public ConverterConstructorInfo(final AbstractTypeHolder type, final List<ConverterConstructorMethodInfo> constructors) {
        super();
        this.type = type;
        this.constructors = ImmutableList.copyOf(constructors);
    }

    public AbstractTypeHolder getType() {
        return type;
    }

    public List<ConverterConstructorMethodInfo> getConstructors() {
        return constructors;
    }
}
