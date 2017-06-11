package org.stuctmapper.model.instructions;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.lang.model.element.Element;

import org.stuctmapper.log.Logger;
import org.stuctmapper.model.properties.ConstructorType;
import org.stuctmapper.model.properties.ParsedType;
import org.stuctmapper.model.properties.Property;
import org.stuctmapper.utils.CollectionUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

public class TargetInfo {
    private final ParsedType properties;
    private final Map<String, TargetInfo> map;
    private final Element errorElement;
    private ITargetPropertyInitializer initializer;
    private final Set<ITargetPropertyAccessor> accessors = new LinkedHashSet<>();
    private final Property parentProperty;
    private final boolean initialized;

    public TargetInfo(final Property parentProperty, final ParsedType properties, final Map<String, TargetInfo> map, 
            final boolean initialized, final Element errorElement) {
        this.parentProperty = parentProperty;
        this.properties = properties;
        this.errorElement = errorElement;
        this.initialized = initialized;
        this.map = ImmutableMap.copyOf(map);
    }
    
    public boolean isInitialized() {
        return initialized;
    }
    
    public Property getParentProperty() {
        return parentProperty;
    }
    
    public ParsedType getProperties() {
        return properties;
    }
    
    public Map<String, TargetInfo> getMap() {
        return map;
    }
    
    public TargetInfo get(final String propertyName) {
        final TargetInfo result = map.get(propertyName);
        return result;
    }
    
    public void registerPropertyAccessor(final ITargetPropertyAccessor accessor) {
        CollectionUtils.addNew(accessors, accessor);
    }
    
    public void registerPropertyInitializer(final ITargetPropertyInitializer initializer) {
        Preconditions.checkNotNull(initializer);
        Logger.checkArgument(this.initializer == null, "multiple initializers", errorElement);
        this.initializer = initializer;
    }

    public void setPropertyInitializer(final ITargetPropertyInitializer initializer) {
        Preconditions.checkNotNull(initializer);
        this.initializer = initializer;
    }
    

    public ITargetPropertyInitializer getInitializer() {
        return initializer;
    }
    
    public Set<ITargetPropertyAccessor> getAccessors() {
        return accessors;
    }
    
    public Set<ConstructorType> getConstructors() {
        final Set<ConstructorType> result = properties.getConstructors();
        return result;
    }
    
    public boolean hasConstructor(final ConstructorType type) {
        final boolean result = properties.hasConstructor(type);
        return result;
    }
     
    @Override
    public int hashCode() {
        final int hashCode = properties.hashCode();
        return hashCode;
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
            final TargetInfo that = (TargetInfo) obj;
            final boolean result = Objects.equals(this.properties, that.properties);
            return result;
        }
    }
    
    @Override
    public String toString() {
        final Class<?> thisClass = getClass();
        final String className = thisClass.getSimpleName();
        final String result = className + "{type=" + properties.getType() + ",initializer=" + initializer + ",accessors=" + accessors + ",map=" + map + "}";
        return result;
    }
}
