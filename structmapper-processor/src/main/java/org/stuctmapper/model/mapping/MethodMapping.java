package org.stuctmapper.model.mapping;

import java.util.List;

import org.stuctmapper.model.properties.ConverterMethodInfo;

import com.google.common.collect.ImmutableList;

public class MethodMapping {
    private final ConverterMethodInfo method;
    private final List<MappingPaths> mappings;
    
    public MethodMapping(final ConverterMethodInfo method, final List<MappingPaths> mappings) {
        super();
        this.method = method;
        this.mappings = ImmutableList.copyOf(mappings);
    }
    
    public ConverterMethodInfo getMethod() {
        return method;
    }
    
    public List<MappingPaths> getMappings() {
        return mappings;
    }
}
