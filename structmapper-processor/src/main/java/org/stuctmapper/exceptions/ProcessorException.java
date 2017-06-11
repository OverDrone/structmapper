package org.stuctmapper.exceptions;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;

public class ProcessorException extends RuntimeException {
    private static final long serialVersionUID = -5497385238697749773L;
    private final Element element;
    private final AnnotationMirror annotationMirror;
    private final AnnotationValue annotationValue;
    
    public ProcessorException(final Element element) {
        super();
        this.element = element;
        this.annotationMirror = null;
        this.annotationValue = null;
    }
    
    public ProcessorException(final String message, final Element element) {
        super(message);
        this.element = element;
        this.annotationMirror = null;
        this.annotationValue = null;
    }
    
    public ProcessorException(final Exception e) {
        super(e);
        this.element = null;
        this.annotationMirror = null;
        this.annotationValue = null;
    }

    public ProcessorException(final String message) {
        super(message);
        this.element = null;
        this.annotationMirror = null;
        this.annotationValue = null;
    }

    public Element getElement() {
        return element;
    }
    
    public AnnotationMirror getAnnotationMirror() {
        return annotationMirror;
    }
    
    public AnnotationValue getAnnotationValue() {
        return annotationValue;
    }
}
