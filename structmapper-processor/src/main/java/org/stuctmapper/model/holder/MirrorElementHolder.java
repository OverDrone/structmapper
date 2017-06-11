package org.stuctmapper.model.holder;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

public class MirrorElementHolder extends AbstractElementHolder {
    private final Element errorElement;

    private MirrorElementHolder(final AbstractTypeHolder enclosingType, final String name, final AbstractTypeHolder returnType, 
            final List<AbstractTypeHolder> parameters, final EnclosedElementKind kind, final Set<Modifier> modifiers, 
            final Function<Class<? extends Annotation>, ? extends Annotation> annotationFunction,
            final Element errorElement) {
        super(enclosingType, name, returnType, parameters, kind, modifiers, annotationFunction);
        this.errorElement = errorElement;
    }

    public static MirrorElementHolder fromConstructor(final AbstractTypeHolder enclosingType, final ExecutableElement element) {
        final TypeHolderCache cache = enclosingType.getCache();
        final String name = getName(element);
        final List<? extends VariableElement> elementParameters = element.getParameters();
        final List<AbstractTypeHolder> parameters = convertParameters(elementParameters, cache);
        final Set<Modifier> modifiers = element.getModifiers();
        final Function<Class<? extends Annotation>, ? extends Annotation> annotationFunction = (annotationClass) -> element.getAnnotation(annotationClass);
        final MirrorElementHolder result = new MirrorElementHolder(enclosingType, name, enclosingType, parameters, 
                EnclosedElementKind.CONSTRUCTOR, modifiers, annotationFunction, element);
        return result;
    }

    public static MirrorElementHolder fromMethod(final AbstractTypeHolder enclosingType, final ExecutableElement element) {
        final TypeHolderCache cache = enclosingType.getCache();
        final String name = getName(element);
        final List<? extends VariableElement> elementParameters = element.getParameters();
        final List<AbstractTypeHolder> parameters = convertParameters(elementParameters, cache);
        final AbstractTypeHolder returnType = convertReturnType(element, cache);
        final Set<Modifier> modifiers = element.getModifiers();
        final Function<Class<? extends Annotation>, ? extends Annotation> annotationFunction = (annotationClass) -> element.getAnnotation(annotationClass);
        final MirrorElementHolder result = new MirrorElementHolder(enclosingType, name, returnType, parameters, 
                EnclosedElementKind.METHOD, modifiers, annotationFunction, element);
        return result;
    }

    public static MirrorElementHolder fromField(final AbstractTypeHolder enclosingType, final VariableElement element) {
        final TypeHolderCache cache = enclosingType.getCache();
        final String name = getName(element);
        final List<AbstractTypeHolder> parameters = Collections.emptyList();
        final Set<Modifier> modifiers = element.getModifiers();
        final AbstractTypeHolder fieldType = convertFieldType(element, cache);
        final Function<Class<? extends Annotation>, ? extends Annotation> annotationFunction = (annotationClass) -> element.getAnnotation(annotationClass);
        final MirrorElementHolder result = new MirrorElementHolder(enclosingType, name, fieldType, parameters, 
                EnclosedElementKind.FIELD, modifiers, annotationFunction, element);
        return result;
    }
    
    @Override
    public Element getErrorElement() {
        return errorElement;
    }
    
    private static AbstractTypeHolder convertFieldType(final VariableElement element, final TypeHolderCache cache) {
        final TypeMirror typeMirror = element.asType();
        final MirrorTypeHolder fieldTypeHolder = new MirrorTypeHolder(typeMirror, cache);
        final AbstractTypeHolder result = cache.register(fieldTypeHolder);
        return result;
    }

    private static AbstractTypeHolder convertReturnType(final ExecutableElement element, final TypeHolderCache cache) {
        if (element == null) {
            return null;
        } else {
            final TypeMirror returnTypeMirror = element.getReturnType();
            if (returnTypeMirror == null || returnTypeMirror.getKind() == TypeKind.VOID) {
                return null;
            } else {
                final MirrorTypeHolder returnHolder = new MirrorTypeHolder(returnTypeMirror, cache);
                final AbstractTypeHolder result = cache.register(returnHolder);
                return result;
            }
        }
    }

    private static String getName(final Element element) {
        final Name simpleName = element.getSimpleName();
        final String result = simpleName.toString();
        return result;
    }
    
    private static List<AbstractTypeHolder> convertParameters(final List<? extends VariableElement> elementParameters, final TypeHolderCache cache) {
        if (elementParameters == null) {
            final List<AbstractTypeHolder> result = Collections.emptyList();
            return result;
        } else {
            final int elementParametersSize = elementParameters.size();
            final List<AbstractTypeHolder> result = new ArrayList<>(elementParametersSize);
            for (final VariableElement element : elementParameters) {
                final MirrorTypeHolder mirrorTypeHolder = new MirrorTypeHolder(element, cache);
                final AbstractTypeHolder registeredHolder = cache.register(mirrorTypeHolder);
                result.add(registeredHolder);
            }
            return result;
        }
    }
    
}
