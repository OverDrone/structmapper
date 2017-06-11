package org.stuctmapper.model.holder;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.lang.model.element.Element;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;

import org.stuctmapper.model.TypeQualifier;
import org.stuctmapper.utils.CollectionUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

public abstract class AbstractTypeHolder implements IAnnotationSource {
    private final TypeName typeName;
    private final TypeHolderCache cache;
    private final Function<Class<? extends Annotation>, ? extends Annotation> annotationFunction;
    private final Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<>();
    private AbstractTypeHolder superclass;
    private Set<? extends AbstractTypeHolder> interfaces;
    private List<? extends AbstractElementHolder> enclosedElements;
    private TypeQualifier qualifier;
    
    protected AbstractTypeHolder(final TypeName typeName, final TypeHolderCache cache, 
            final Function<Class<? extends Annotation>, ? extends Annotation> annotationFunction) {
        this.typeName = typeName;
        this.cache = cache;
        this.annotationFunction = annotationFunction;
    }
    
    public TypeHolderCache getCache() {
        return cache;
    }
    
    protected abstract AbstractTypeHolder buildSuperclass();
    
    protected abstract Set<? extends AbstractTypeHolder> buildInterfaces();

    protected abstract List<? extends AbstractElementHolder> buildEnclosedElements();

    protected abstract TypeQualifier buildQualifier();

    public TypeName getTypeName() {
        return typeName;
    }
    
    public String getClassSimpleName() {
        final ClassName className = (ClassName) typeName;
        final String result = className.simpleName();
        return result;
    }
    
    public AbstractTypeHolder getSuperclass() {
        if (superclass == null) {
            superclass = buildSuperclass();
        }
        return superclass;
    }
    
    public Set<? extends AbstractTypeHolder> getInterfaces() {
        if (interfaces == null) {
            interfaces = buildInterfaces();
        }
        return interfaces;
    }
    
    public List<? extends AbstractElementHolder> getEnclosedElements() {
        if (enclosedElements == null) {
            enclosedElements = buildEnclosedElements();
        }
        return enclosedElements;
    }
    
    public TypeQualifier getQualifier() {
        if (qualifier == null) {
            qualifier = buildQualifier();
        }
        return qualifier;
    }
    
    @Override
    public <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
        @SuppressWarnings("unchecked")
        final T result = (T) annotations.computeIfAbsent(annotationClass, annotationFunction);
        return result;
    }
    
    public abstract Element getErrorElement();
    
    public abstract boolean isInterface();
    
    public Boolean isNull() {
        if (typeName.isPrimitive()) {
            return Boolean.FALSE; 
        } else {
            return null;
        }
    }
    
    public boolean isObject() {
        final boolean result = TypeName.OBJECT.equals(typeName);
        return result;
    }
    
    public static Set<AbstractTypeHolder> unmirrorClassArray(final Supplier<Class<?>[]> supplier, final TypeHolderCache cache) {
        final Set<AbstractTypeHolder> set = new LinkedHashSet<>();
        try {
            final Class<?>[] classes = supplier.get();
            if (classes != null) {
                for (final Class<?> objectClass : classes) {
                    final ClassTypeHolder holder = new ClassTypeHolder(objectClass, cache);
                    final AbstractTypeHolder registeredHolder = cache.register(holder);
                    CollectionUtils.addNew(set, registeredHolder);
                }
            }
        } catch (final MirroredTypesException e) {
            Preconditions.checkArgument(set.isEmpty());
            final List<? extends TypeMirror> list = e.getTypeMirrors();
            if (list != null) {
                for (final TypeMirror typeMirror : list) {
                    final MirrorTypeHolder holder = new MirrorTypeHolder(typeMirror, cache);
                    final AbstractTypeHolder registeredHolder = cache.register(holder);
                    CollectionUtils.addNew(set, registeredHolder);
                }
            }
        }
        final Set<AbstractTypeHolder> result = ImmutableSet.copyOf(set);
        return result;
    }

    public static AbstractTypeHolder unmirrorClass(final Supplier<Class<?>> supplier, final TypeHolderCache cache) {
        try {
            final Class<?> objectClass = supplier.get();
            if (objectClass == null) {
                return null;
            } else {
                final ClassTypeHolder holder = new ClassTypeHolder(objectClass, cache);
                final AbstractTypeHolder result = cache.register(holder);
                return result;
            }
        } catch (final MirroredTypeException e) {
            final TypeMirror typeMirror = e.getTypeMirror();
            if (typeMirror == null) {
                return null;
            } else {
                final MirrorTypeHolder holder = new MirrorTypeHolder(typeMirror, cache);
                final AbstractTypeHolder result = cache.register(holder);
                return result;
            }
        }
    }
    
    @Override
    public int hashCode() {
        final int hashCode = typeName.hashCode();
        return hashCode;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        } else if (obj == this) {
            return true;
        } else if (!(obj instanceof AbstractTypeHolder)) {
            return false;
        } else {
            final AbstractTypeHolder that = (AbstractTypeHolder) obj;
            final boolean result = Objects.equals(this.typeName, that.typeName);
            return result;
        }
    }
    
    @Override
    public String toString() {
        return typeName.toString();
    }
}
