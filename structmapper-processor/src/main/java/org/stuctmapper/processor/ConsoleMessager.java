package org.stuctmapper.processor;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;

public class ConsoleMessager implements Messager {

    @Override
    public void printMessage(final Kind kind, final CharSequence msg) {
        System.out.println(kind + " " + msg);
    }

    @Override
    public void printMessage(Kind kind, CharSequence msg, Element e) {
        printMessage(kind, msg);
    }

    @Override
    public void printMessage(Kind kind, CharSequence msg, Element e, AnnotationMirror a) {
        printMessage(kind, msg);
    }

    @Override
    public void printMessage(Kind kind, CharSequence msg, Element e, AnnotationMirror a, AnnotationValue v) {
        printMessage(kind, msg);
    }

}
