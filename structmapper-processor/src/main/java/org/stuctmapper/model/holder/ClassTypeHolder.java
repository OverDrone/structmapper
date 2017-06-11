package org.stuctmapper.model.holder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import javax.lang.model.element.Element;

import org.stuctmapper.model.TypeQualifier;
import org.stuctmapper.utils.CollectionUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.TypeName;

public class ClassTypeHolder extends AbstractTypeHolder {
    private Class<?> objectClass;

    public ClassTypeHolder(final TypeName primitiveType, final TypeHolderCache cache) {
        this(primitiveType, null, cache, (annotationClass) -> null);
    }
    
    public ClassTypeHolder(final Class<?> objectClass, final TypeHolderCache cache) {
        this(objectClass, cache, (annotationClass) -> objectClass.getAnnotation(annotationClass));
    }

    public ClassTypeHolder(final Class<?> objectClass, final TypeHolderCache cache, 
            final Function<Class<? extends Annotation>, ? extends Annotation> annotationFunction) {
        this(TypeName.get(objectClass), objectClass, cache, annotationFunction);
    }

    public ClassTypeHolder(final TypeName typeName, final Class<?> objectClass, final TypeHolderCache cache, 
            final Function<Class<? extends Annotation>, ? extends Annotation> annotationFunction) {
        super(typeName, cache, annotationFunction);
        this.objectClass = objectClass;
    }
    
    @Override
    protected AbstractTypeHolder buildSuperclass() {
        if (objectClass == null) {
            return null;
        }
        final Class<?> superclass = objectClass.getSuperclass();
        if (superclass != null) {
            final TypeHolderCache cache = getCache();
            final AbstractTypeHolder superclassHolder = new ClassTypeHolder(superclass, cache);
            final AbstractTypeHolder result = cache.register(superclassHolder);
            return result;
        } else {
            return null;
        }
    }
    
    @Override
    protected Set<? extends AbstractTypeHolder> buildInterfaces() {
        if (objectClass != null) {
            final Class<?>[] interfaces = objectClass.getInterfaces();
            if (interfaces != null) {
                final int interfacesSize = interfaces.length;
                final Set<AbstractTypeHolder> set = new LinkedHashSet<>(interfacesSize);
                final TypeHolderCache cache = getCache();
                for (final Class<?> objectInterface : interfaces) {
                    final AbstractTypeHolder interfaceHolder = new ClassTypeHolder(objectInterface, cache);
                    final AbstractTypeHolder registeredHolder = cache.register(interfaceHolder);
                    CollectionUtils.addNew(set, registeredHolder);
                }
                final Set<? extends AbstractTypeHolder> result = ImmutableSet.copyOf(set);
                return result;
            }
        }
        final Set<? extends AbstractTypeHolder> result = Collections.emptySet();
        return result;
    }

    @Override
    protected List<? extends AbstractElementHolder> buildEnclosedElements() {
        final List<AbstractElementHolder> list = new ArrayList<>();
        convertConstructors(list);
        convertFields(list);
        convertMethods(list);
        final List<? extends AbstractElementHolder> result = ImmutableList.copyOf(list);
        return result;
    }
    
    @Override
    public Element getErrorElement() {
        return null;
    }
    
    @Override
    public boolean isInterface() {
        final boolean result = objectClass.isInterface();
        return result;
    }
    
    @Override
    protected TypeQualifier buildQualifier() {
        final String simpleName = objectClass.getSimpleName();
        Class<?> currentClass = objectClass;
        String prefix = "";
        while (true) {
            currentClass = currentClass.getEnclosingClass();
            if (currentClass == null) {
                break;
            }
            final String currentSimpleName = currentClass.getSimpleName();
            if (prefix.isEmpty()) {
                prefix = currentSimpleName;
            } else {
                prefix = currentSimpleName + "." + prefix;
            }
        }
        final Package objectPackage = objectClass.getPackage();
        final String packageName = objectPackage.getName();
        final TypeQualifier result = new TypeQualifier(packageName, prefix, simpleName);
        return result;
    }
    
    private void convertMethods(final List<AbstractElementHolder> list) {
        final Method[] methods = objectClass.getDeclaredMethods();
        if (methods != null) {
            for (final Method method : methods) {
                final AbstractElementHolder holder = ClassElementHolder.fromMethod(this, method);
                list.add(holder);
            }
        }
    }

    private void convertFields(final List<AbstractElementHolder> list) {
        final Field[] fields = objectClass.getDeclaredFields();
        if (fields != null) {
            for (final Field field : fields) {
                final AbstractElementHolder holder = ClassElementHolder.fromField(this, field);
                list.add(holder);
            }
        }
    }

    private void convertConstructors(final List<AbstractElementHolder> list) {
        final Constructor<?>[] constructors = objectClass.getDeclaredConstructors();
        if (constructors != null) {
            for (final Constructor<?> constructor : constructors) {
                final AbstractElementHolder holder = ClassElementHolder.fromConstructor(this, constructor);
                list.add(holder);
            }
        }
    }
}
