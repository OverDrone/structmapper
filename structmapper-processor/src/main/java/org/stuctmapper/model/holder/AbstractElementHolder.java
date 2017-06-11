package org.stuctmapper.model.holder;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public abstract class AbstractElementHolder implements IAnnotationSource {
    private final AbstractTypeHolder enclosingType;
    private final String name;
    private final AbstractTypeHolder returnType;
    private final List<? extends AbstractTypeHolder> parameters;
    private final EnclosedElementKind kind;
    private final Set<Modifier> modifiers;
    private final Function<Class<? extends Annotation>, ? extends Annotation> annotationFunction;
    private final Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<>();
    
    public AbstractElementHolder(final AbstractTypeHolder enclosingType, final String name, 
            final AbstractTypeHolder returnType, final List<? extends AbstractTypeHolder> parameters, 
            final EnclosedElementKind kind, final Set<Modifier> modifiers, 
            final Function<Class<? extends Annotation>, ? extends Annotation> annotationFunction) {
        super();
        this.enclosingType = enclosingType;
        this.name = name;
        this.returnType = returnType;
        this.parameters = ImmutableList.copyOf(parameters);
        this.kind = kind;
        this.modifiers = ImmutableSet.copyOf(modifiers);
        this.annotationFunction = annotationFunction;
    }
    
    public EnclosedElementKind getKind() {
        return kind;
    }
    
    public String getName() {
        return name;
    }
    
    public AbstractTypeHolder getEnclosingType() {
        return enclosingType;
    }
    
    public AbstractTypeHolder getReturnType() {
        return returnType;
    }
    
    public List<? extends AbstractTypeHolder> getParameters() {
        return parameters;
    }
    
    public Set<Modifier> getModifiers() {
        return modifiers;
    }
    
    @Override
    public <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
        @SuppressWarnings("unchecked")
        final T result = (T) annotations.computeIfAbsent(annotationClass, annotationFunction);
        return result;
    }
    
    public abstract Element getErrorElement();
    
    @Override
    public int hashCode() {
        final int enclosingHashCode = enclosingType.hashCode();
        final int nameHashCode = name.hashCode();
        final int result = enclosingHashCode + nameHashCode;
        return result;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        } else if (obj == this) {
            return true;
        } else if (!(obj instanceof AbstractElementHolder)) {
            return false;
        } else {
            final AbstractElementHolder that = (AbstractElementHolder) obj;
            if (!Objects.equals(this.enclosingType, that.enclosingType)) {
                return false;
            } else if (!Objects.equals(this.name, that.name)) {
                return false;
            } else {
                return true;
            }
        }
    }
    
    @Override
    public String toString() {
        return returnType + " " + enclosingType.toString() + "." + name + "(" + parameters + ")";
    }
    
}
