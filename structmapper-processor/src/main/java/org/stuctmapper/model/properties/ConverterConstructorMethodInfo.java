package org.stuctmapper.model.properties;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.stuctmapper.model.holder.AbstractElementHolder;
import org.stuctmapper.utils.CollectionUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class ConverterConstructorMethodInfo {
    private final AbstractElementHolder element;
    private final ConversionParticipant target;
    private final Map<String, ConversionParticipant> sourcesMap;
    private final List<ConversionParticipant> sourcesList;
    
    public ConverterConstructorMethodInfo(final AbstractElementHolder element, 
            final ConversionParticipant target, final List<ConversionParticipant> sources) {
        super();
        this.element = element;
        this.target = target;
        this.sourcesMap = buildMap(sources);
        final Collection<ConversionParticipant> values = sourcesMap.values();
        this.sourcesList = ImmutableList.copyOf(values);
    }

    private static Map<String, ConversionParticipant> buildMap(final List<ConversionParticipant> list) {
        final Map<String, ConversionParticipant> map = new LinkedHashMap<>();
        for (final ConversionParticipant item : list) {
            final String id = item.getId();
            CollectionUtils.addNew(map, id, item);
        }
        final Map<String, ConversionParticipant> result = ImmutableMap.copyOf(map);
        return result;
    }
    
    public AbstractElementHolder getElement() {
        return element;
    }
    
    public ConversionParticipant getTarget() {
        return target;
    }
    
    public List<ConversionParticipant> getSourcesList() {
        return sourcesList;
    }
    
    public Map<String, ConversionParticipant> getSourcesMap() {
        return sourcesMap;
    }

    public ConversionParticipant byIndex(final int index) {
        if (index == 0) {
            return target;
        } else {
            final ConversionParticipant source = sourcesList.get(index - 1);
            return source;
        }
    }

    @Override
    public int hashCode() {
        final int result = element.hashCode();
        return result;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        } else if (obj == this) {
            return true;
        } else if (obj.getClass() != this.getClass()) {
            return false;
        } else {
            final ConverterConstructorMethodInfo that = (ConverterConstructorMethodInfo) obj;
            final boolean result = Objects.equals(this.element, that.element);
            return result;
        }
    }
}
