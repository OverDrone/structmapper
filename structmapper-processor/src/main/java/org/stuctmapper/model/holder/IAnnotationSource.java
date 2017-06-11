package org.stuctmapper.model.holder;

import java.lang.annotation.Annotation;

public interface IAnnotationSource {
    <T extends Annotation> T getAnnotation(final Class<T> annotationClass);
}
