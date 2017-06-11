package org.stuctmapper.model.properties;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.stuctmapper.model.holder.AbstractTypeHolder;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class ParsedType {
    private final AbstractTypeHolder type;
    private final Map<String, Property> map;
    private final boolean simple;
    private final Set<ConstructorType> constructors;
    
    public ParsedType(final AbstractTypeHolder type, final Map<String, Property> map, final Collection<ConstructorType> constructors, final boolean simple) {
        this.type = type;
        this.map = ImmutableMap.copyOf(map);
        this.constructors = ImmutableSet.copyOf(constructors);
        this.simple = simple;
    }
    
    public Map<String, Property> getMap() {
        return map;
    }

    public Property get(final String name) {
        final Property result = map.get(name);
        return result;
    }
    
    public boolean hasProperty(final String name) {
        final Property property = get(name);
        final boolean result = property != null;
        return result;
    }

    public Set<ConstructorType> getConstructors() {
        return constructors;
    }

    public boolean hasConstructor(final ConstructorType type) {
        final boolean result = constructors.contains(type);
        return result;
    }

    public boolean isSimple() {
        return simple;
    }
    
    public AbstractTypeHolder getType() {
        return type;
    }
    
    @Override
    public int hashCode() {
        return type.hashCode();
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
            final ParsedType that = (ParsedType) obj;
            final boolean result = Objects.equals(this.type, that.type);
            return result;
        }
    }
    
    @Override
    public String toString() {
        final Class<?> thisClass = getClass();
        final String className = thisClass.getSimpleName();
        return className + "{type=" + type + ",constructors=" + constructors + ",simple=" + simple + ",properties=" + map;
    }
}
