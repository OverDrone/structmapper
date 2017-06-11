package org.stuctmapper.model.properties;

import java.util.Objects;

import javax.lang.model.element.Element;

import org.stuctmapper.log.Logger;
import org.stuctmapper.model.holder.AbstractElementHolder;
import org.stuctmapper.model.holder.AbstractTypeHolder;

public class Property {
    private final boolean initialized;
    private final String name;
    private final AbstractTypeHolder type;
    private final AbstractElementHolder field;
    private final AbstractElementHolder getter;
    private final AbstractElementHolder setter;
    
    public Property(final boolean initialized, final String name, final AbstractTypeHolder type, 
            final AbstractElementHolder field, final AbstractElementHolder getter, final AbstractElementHolder setter) {
        super();
        this.initialized = initialized;
        this.name = name;
        this.type = type;
        this.field = field;
        this.getter = getter;
        this.setter = setter;
    }
    
    public String getName() {
        return name;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public AbstractTypeHolder getType() {
        return type;
    }

    public AbstractElementHolder getField() {
        return field;
    }

    public AbstractElementHolder getGetter() {
        return getter;
    }

    public AbstractElementHolder getSetter() {
        return setter;
    }
    
    public Element getErrorElement() {
        if (field != null) {
            return field.getErrorElement();
        } else if (getter != null) {
            return getter.getErrorElement();
        } else if (setter != null) {
            return setter.getErrorElement();
        } else {
            return Logger.fail("Unexpected null all");
        }
    }

    @Override
    public int hashCode() {
        int result = 0;
        if (field != null) {
            result += field.hashCode();
        }
        if (setter != null) {
            result += setter.hashCode();
        }
        if (getter != null) {
            result += getter.hashCode();
        }
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
            final Property that = (Property) obj;
            if (!Objects.equals(this.field, that.field)) {
                return false;
            } else if (!Objects.equals(this.getter, that.getter)) {
                return false;
            } else if (!Objects.equals(this.setter, that.setter)) {
                return false;
            } else {
                return true;
            }
        }
    }
    
    @Override
    public String toString() {
        final Class<?> thisClass = getClass();
        final String className = thisClass.getSimpleName();
        final String result = className + "{name=" + name + ",type=" + type + ",initialized=" + initialized + ",field=" + field + ",getter=" + getter
                + ",setter=" + setter + "}";
        return result;
    }

    public boolean isWriteable() {
        final boolean result = field != null || setter != null;
        return result;
    }
    
    
}
