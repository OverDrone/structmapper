package org.stuctmapper.model.holder;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.stuctmapper.model.TypeQualifier;
import org.stuctmapper.utils.CollectionUtils;
import org.stuctmapper.utils.JavaBuilderUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.TypeName;

public class MirrorTypeHolder extends AbstractTypeHolder {
    private final Element errorElement;
    private final TypeElement typeElement;
    
    public MirrorTypeHolder(final VariableElement parameter, final TypeHolderCache cache) {
        this(parameter.asType(), cache, parameter, (annotationClass) -> parameter.getAnnotation(annotationClass));
    }
    
    public MirrorTypeHolder(final TypeMirror typeMirror, final TypeHolderCache cache) {
        this(typeMirror, cache, null, null);
    }

    private MirrorTypeHolder(final TypeMirror typeMirror, final TypeHolderCache cache, final Element errorElement,
            final Function<Class<? extends Annotation>, ? extends Annotation> annotationFunction) {
        this(typeMirror, JavaBuilderUtils.toTypeElement(typeMirror, false), cache, errorElement, annotationFunction);
    }

    private MirrorTypeHolder(final TypeMirror typeMirror, final TypeElement typeElement, final TypeHolderCache cache, final Element errorElement,
            final Function<Class<? extends Annotation>, ? extends Annotation> annotationFunction) {
        super(TypeName.get(typeMirror), cache, getAnnotationFunction(annotationFunction, typeElement, typeMirror));
        this.typeElement = typeElement;
        if (errorElement != null) {
            this.errorElement = errorElement;
        } else {
            this.errorElement = typeElement;
        }
    }
    
    private static Function<Class<? extends Annotation>, ? extends Annotation> getAnnotationFunction(
            final Function<Class<? extends Annotation>, ? extends Annotation> annotationFunction, 
            final TypeElement typeElement, final TypeMirror typeMirror) {
        if (annotationFunction != null) {
            return annotationFunction;
        } else if (typeElement != null) {
            return (annotationClass) -> typeElement.getAnnotation(annotationClass);
        } else {
            return (annotationClass) -> typeMirror.getAnnotation(annotationClass);
        }
    }

    @Override
    protected AbstractTypeHolder buildSuperclass() {
        if (typeElement == null) {
            return null;
        }
        final TypeMirror superclass = typeElement.getSuperclass();
        if (superclass != null && superclass.getKind() != TypeKind.NONE) {
            final TypeHolderCache cache = getCache();
            final AbstractTypeHolder superclassHolder = new MirrorTypeHolder(superclass, cache);
            final AbstractTypeHolder result = cache.register(superclassHolder);
            return result;
        }
        return null;
    }

    @Override
    protected Set<? extends AbstractTypeHolder> buildInterfaces() {
        if (typeElement != null) {
            final List<? extends TypeMirror> interfaces = typeElement.getInterfaces();
            if (interfaces != null) {
                final int interfacesSize = interfaces.size();
                final Set<AbstractTypeHolder> set = new LinkedHashSet<>(interfacesSize);
                final TypeHolderCache cache = getCache();
                for (final TypeMirror typeMirror : interfaces) {
                    final AbstractTypeHolder interfaceHolder = new MirrorTypeHolder(typeMirror, cache);
                    final AbstractTypeHolder registeredHolder = cache.register(interfaceHolder);
                    CollectionUtils.addNew(set, registeredHolder);
                }
                final Set<? extends AbstractTypeHolder> result = ImmutableSet.copyOf(set);
                return result;
            }
        }
        final Set<AbstractTypeHolder> result = Collections.emptySet();
        return result;
    }
    
    @Override
    protected List<? extends AbstractElementHolder> buildEnclosedElements() {
        final List<? extends Element> elements = typeElement.getEnclosedElements();
        if (elements == null) {
            final List<AbstractElementHolder> result = Collections.emptyList();
            return result;
        } else {
            final int elementsSize = elements.size();
            final List<AbstractElementHolder> list = new ArrayList<>(elementsSize);
            for (final Element element : elements) {
                final AbstractElementHolder elementHolder = convertElement(element);
                if (elementHolder != null) {
                    list.add(elementHolder);
                }
            }
            final List<? extends AbstractElementHolder> result = ImmutableList.copyOf(list);
            return result;
        }
    }
    
    @Override
    protected TypeQualifier buildQualifier() {
        final TypeQualifier result = JavaBuilderUtils.getQualifier(typeElement);
        return result;
    }
    
    @Override
    public Element getErrorElement() {
        return errorElement;
    }
    
    @Override
    public boolean isInterface() {
        final boolean result = JavaBuilderUtils.isInterface(typeElement);
        return result;
    }
    
    private AbstractElementHolder convertElement(final Element element) {
        final ElementKind kind = element.getKind();
        switch (kind) {
        case METHOD: return convertMethod(element);
        case CONSTRUCTOR: return convertConstructor(element);
        case FIELD: return convertField(element);
        default: return null;
        }
    }

    private AbstractElementHolder convertField(final Element element) {
        final VariableElement fieldElement = (VariableElement) element;
        final MirrorElementHolder result = MirrorElementHolder.fromField(this, fieldElement);
        return result;
    }

    private AbstractElementHolder convertConstructor(final Element element) {
        final ExecutableElement executableElement = (ExecutableElement) element;
        final MirrorElementHolder result = MirrorElementHolder.fromConstructor(this, executableElement);
        return result;
    }

    private AbstractElementHolder convertMethod(final Element element) {
        final ExecutableElement executableElement = (ExecutableElement) element;
        final MirrorElementHolder result = MirrorElementHolder.fromMethod(this, executableElement);
        return result;
    }
}
