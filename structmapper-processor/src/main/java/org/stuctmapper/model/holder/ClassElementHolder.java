package org.stuctmapper.model.holder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

import org.stuctmapper.utils.CollectionUtils;

import com.google.common.base.Preconditions;

public class ClassElementHolder extends AbstractElementHolder {
    private ClassElementHolder(final AbstractTypeHolder enclosingType, final String name, final AbstractTypeHolder returnType, 
            final List<AbstractTypeHolder> parameters, final EnclosedElementKind kind, final Set<Modifier> modifiers, 
            final Function<Class<? extends Annotation>, ? extends Annotation> annotationFunction) {
        super(enclosingType, name, returnType, parameters, kind, modifiers, annotationFunction);
    }
    
    public static ClassElementHolder fromConstructor(final AbstractTypeHolder enclosingType, final Constructor<?> constructor) {
        final TypeHolderCache cache = enclosingType.getCache();
        final String name = constructor.getName();
        final int modifiers = constructor.getModifiers();
        final Set<Modifier> modifiersSet = convertModifiers(modifiers);
        final Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
        final Class<?>[] parameterTypes = constructor.getParameterTypes();
        final List<AbstractTypeHolder> parameters = convertParameters(parameterTypes, parameterAnnotations, cache);
        final Function<Class<? extends Annotation>, ? extends Annotation> annotationFunction = (annotationClass) -> constructor.getAnnotation(annotationClass);
        final ClassElementHolder result = new ClassElementHolder(enclosingType, name, enclosingType, parameters, 
                EnclosedElementKind.CONSTRUCTOR, modifiersSet, annotationFunction);
        return result;
    }

    public static ClassElementHolder fromField(final AbstractTypeHolder enclosingType, final Field field) {
        final TypeHolderCache cache = enclosingType.getCache();
        final String name = field.getName();
        final int modifiers = field.getModifiers();
        final Set<Modifier> modifiersSet = convertModifiers(modifiers);
        final List<AbstractTypeHolder> parameters = Collections.emptyList();
        final AbstractTypeHolder registeredTypeHolder = convertFieldType(field, cache);
        final Function<Class<? extends Annotation>, ? extends Annotation> annotationFunction = (annotationClass) -> field.getAnnotation(annotationClass);
        final ClassElementHolder result = new ClassElementHolder(enclosingType, name, registeredTypeHolder, parameters, 
                EnclosedElementKind.FIELD, modifiersSet, annotationFunction);
        return result;
    }

    public static ClassElementHolder fromMethod(final AbstractTypeHolder enclosingType, final Method method) {
        final TypeHolderCache cache = enclosingType.getCache();
        final String name = method.getName();
        final int modifiers = method.getModifiers();
        final Set<Modifier> modifiersSet = convertModifiers(modifiers);
        final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        final Class<?>[] parameterTypes = method.getParameterTypes();
        final List<AbstractTypeHolder> parameters = convertParameters(parameterTypes, parameterAnnotations, cache);
        final AbstractTypeHolder returnType = convertReturnType(method, cache);
        final Function<Class<? extends Annotation>, ? extends Annotation> annotationFunction = (annotationClass) -> method.getAnnotation(annotationClass);
        final ClassElementHolder result = new ClassElementHolder(enclosingType, name, returnType, parameters, 
                EnclosedElementKind.METHOD, modifiersSet, annotationFunction);
        return result;
    }
    
    @Override
    public Element getErrorElement() {
        return null;
    }
    
    private static AbstractTypeHolder convertFieldType(final Field field, final TypeHolderCache cache) {
        final Class<?> fieldType = field.getType();
        final AbstractTypeHolder fieldTypeHolder = new ClassTypeHolder(fieldType, cache);
        final AbstractTypeHolder registeredTypeHolder = cache.register(fieldTypeHolder);
        return registeredTypeHolder;
    }

    private static AbstractTypeHolder convertReturnType(final Method method, final TypeHolderCache cache) {
        final Class<?> returnType = method.getReturnType();
        if (returnType == null || returnType == Void.TYPE) {
            return null;
        } else {
            final AbstractTypeHolder returnHolder = new ClassTypeHolder(returnType, cache);
            final AbstractTypeHolder result = cache.register(returnHolder);
            return result;
        }
    }

    private static List<AbstractTypeHolder> convertParameters(final Class<?>[] parameterTypes, 
            final Annotation[][] parameterAnnotations, final TypeHolderCache cache) {
        if (parameterTypes == null) {
            final List<AbstractTypeHolder> result = Collections.emptyList();
            return result;
        } else {
            final int size = parameterTypes.length;
            Preconditions.checkArgument(parameterAnnotations == null || parameterAnnotations.length == size);
            final List<AbstractTypeHolder> result = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                final Class<?> parameterType = parameterTypes[i];
                final Annotation[] annotations;
                if (parameterAnnotations != null) {
                    annotations = parameterAnnotations[i];
                } else {
                    annotations = null;
                }
                final Function<Class<? extends Annotation>, ? extends Annotation> annotationFunction = (annotationClass) -> {
                    if (annotations == null) {
                        return null;
                    } else {
                        for (final Annotation annotation : annotations) {
                            if (annotationClass.isInstance(annotation)) {
                                return annotation;
                            }
                        }
                        return null;
                    }
                };
                final ClassTypeHolder parameter = new ClassTypeHolder(parameterType, cache, annotationFunction);
                final AbstractTypeHolder registeredParameter = cache.register(parameter);
                result.add(registeredParameter);
            }
            return result;
        }
    }
    

    private static Set<Modifier> convertModifiers(final int modifiers) {
        final Set<Modifier> set = new HashSet<>();
        if (java.lang.reflect.Modifier.isPublic(modifiers)) {
            CollectionUtils.addNew(set, Modifier.PUBLIC);
        } else if (java.lang.reflect.Modifier.isPrivate(modifiers)) {
            CollectionUtils.addNew(set, Modifier.PRIVATE);
        } else if (java.lang.reflect.Modifier.isProtected(modifiers)) {
            CollectionUtils.addNew(set, Modifier.PROTECTED);
        } else {
            CollectionUtils.addNew(set, Modifier.DEFAULT);
        }
        if (java.lang.reflect.Modifier.isAbstract(modifiers)) {
            CollectionUtils.addNew(set, Modifier.ABSTRACT);
        }
        if (java.lang.reflect.Modifier.isStatic(modifiers)) {
            CollectionUtils.addNew(set, Modifier.STATIC);
        }
        if (java.lang.reflect.Modifier.isFinal(modifiers)) {
            CollectionUtils.addNew(set, Modifier.FINAL);
        }
        if (java.lang.reflect.Modifier.isTransient(modifiers)) {
            CollectionUtils.addNew(set, Modifier.TRANSIENT);
        }
        if (java.lang.reflect.Modifier.isVolatile(modifiers)) {
            CollectionUtils.addNew(set, Modifier.VOLATILE);
        }
        if (java.lang.reflect.Modifier.isSynchronized(modifiers)) {
            CollectionUtils.addNew(set, Modifier.SYNCHRONIZED);
        }
        if (java.lang.reflect.Modifier.isNative(modifiers)) {
            CollectionUtils.addNew(set, Modifier.NATIVE);
        }
        if (java.lang.reflect.Modifier.isStrict(modifiers)) {
            CollectionUtils.addNew(set, Modifier.STRICTFP);
        }
        return set;
    }
}
