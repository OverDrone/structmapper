package org.stuctmapper.model.properties;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;

import org.stuctmapper.model.holder.AbstractElementHolder;

import com.google.common.collect.ImmutableMap;

public class ConverterMethodInfo {
    public static final Integer RETURN_INDEX = 0;
    private final AbstractElementHolder element;
    private final Map<String, ConversionParticipant> sources;
    private final Map<String, ConversionParticipant> targets;
    private final Integer maxIndex;
    private final Map<Integer, ConversionParticipant> indexMap;
    private final boolean implementable;
    
    public ConverterMethodInfo(final AbstractElementHolder element, final List<ConversionParticipant> participants, final boolean implementable) {
        super();
        this.element = element;
        this.sources = toIdMap(participants, ParticipantKind.SOURCE);
        this.targets = toIdMap(participants, ParticipantKind.TARGET);
        this.maxIndex = calcMaxIndex(participants);
        this.indexMap = toIndexMap(participants);
        this.implementable = implementable;
    }
    
    private static Map<String, ConversionParticipant> toIdMap(final List<ConversionParticipant> list, final ParticipantKind kind) {
        final Predicate<ConversionParticipant> filter = (item) -> item.getKind() == kind;
        final Function<ConversionParticipant, ConversionParticipant> valueMapper = (item) -> item;
        final Collector<ConversionParticipant, ?, Map<String, ConversionParticipant>> collector = Collectors.toMap(ConversionParticipant::getId, valueMapper);
        final Map<String, ConversionParticipant> map = list.stream().filter(filter).collect(collector);
        final Map<String, ConversionParticipant> result = ImmutableMap.copyOf(map);
        return result;
    }

    private static Map<Integer, ConversionParticipant> toIndexMap(final List<ConversionParticipant> list) {
        final Map<Integer, ConversionParticipant> map = list.stream().collect(Collectors.toMap((item) -> item.index, (item) -> item));
        final Map<Integer, ConversionParticipant> result = ImmutableMap.copyOf(map);
        return result;
    }
    
    private static Integer calcMaxIndex(final List<ConversionParticipant> list) {
        final Integer result = list.stream().map((item) -> item.index).max(Comparator.naturalOrder()).orElse(null);
        return result;
    }
    
    public Map<String, ConversionParticipant> getSources() {
        return sources;
    }

    public Map<String, ConversionParticipant> getTargets() {
        return targets;
    }

    public AbstractElementHolder getElement() {
        return element;
    }
    
    public Map<Integer, ConversionParticipant> getIndexMap() {
        return indexMap;
    }
    
    public Integer getMaxIndex() {
        return maxIndex;
    }
    
    public boolean isImplementable() {
        return implementable;
    }
    
    public ConversionParticipant getReturningTarget() {
        final ConversionParticipant result = indexMap.get(RETURN_INDEX);
        return result;
    }
    
    public Element getErrorElement() {
        final Element result = element.getErrorElement();
        return result;
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
            final ConverterMethodInfo that = (ConverterMethodInfo) obj;
            final boolean result = Objects.equals(this.element, that.element);
            return result;
        }
    }
    
    @Override
    public String toString() {
        return element.toString(); 
    }
}
