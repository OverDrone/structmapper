package org.stuctmapper.log;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;

import org.stuctmapper.exceptions.ProcessorException;

import com.google.common.base.Preconditions;

public class Logger {
    private final Messager messager;

    public Logger(final Messager messager) {
        this.messager = messager;
    }
    
    public void printMessage(final String message) {
        printMessage(Kind.NOTE, message);
    }

    public static void checkArgument(final boolean value, final Element element) {
        if (!value) {
            throw new ProcessorException(element);
        }
    }

    public static void checkNotNull(final Object value, final String message) {
        if (value == null) {
            throw new ProcessorException(message);
        }
    }

    public static void checkNotNull(final Object value, final String message, final Element element) {
        if (value == null) {
            throw new ProcessorException(message, element);
        }
    }
    
    public static void checkArgument(final boolean value, final String message, final Element element) {
        if (!value) {
            throw new ProcessorException(message, element);
        }
    }
    
    public static <T> T fail(final String message, final Element element) {
        throw new ProcessorException(message, element);
    }

    public static <T> T fail(final String message) {
        throw new ProcessorException(message);
    }
    
    private void printMessage(final Kind kind, final String message) {
        messager.printMessage(kind, message);
    }
    
    public void error(final String message, final Element element) {
        Preconditions.checkNotNull(message);
    }
    
    public void handleException(final ProcessorException e) {
        final String message = e.getMessage();
        if (message == null) {
            throw new RuntimeException(e);
        } else {
            final Element element = e.getElement();
            final AnnotationMirror annotationMirror = e.getAnnotationMirror();
            final AnnotationValue annotationValue = e.getAnnotationValue();

            if (element == null) {
                messager.printMessage(Kind.ERROR, message);
            } else if (annotationMirror == null) {
                messager.printMessage(Kind.ERROR, message, element);
            } else if (annotationValue == null) {
                messager.printMessage(Kind.ERROR, message, element, annotationMirror);
            } else {
                messager.printMessage(Kind.ERROR, message, element, annotationMirror, annotationValue);
            }
        }
    }
    
}
