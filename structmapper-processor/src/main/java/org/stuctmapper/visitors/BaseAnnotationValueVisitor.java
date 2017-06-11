package org.stuctmapper.visitors;

import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

public class BaseAnnotationValueVisitor<R> implements AnnotationValueVisitor<R, Object> {

    @Override
    public R visit(final AnnotationValue value, final Object param) {
        throw new UnsupportedOperationException();
    }

    @Override
    public R visit(final AnnotationValue value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public R visitBoolean(final boolean value, final Object param) {
        throw new UnsupportedOperationException();
    }

    @Override
    public R visitByte(final byte value, final Object param) {
        throw new UnsupportedOperationException();
    }

    @Override
    public R visitChar(final char value, final Object param) {
        throw new UnsupportedOperationException();
    }

    @Override
    public R visitDouble(final double value, final Object param) {
        throw new UnsupportedOperationException();
    }

    @Override
    public R visitFloat(final float value, final Object param) {
        throw new UnsupportedOperationException();
    }

    @Override
    public R visitInt(final int value, final Object param) {
        throw new UnsupportedOperationException();
    }

    @Override
    public R visitLong(final long value, final Object param) {
        throw new UnsupportedOperationException();
    }

    @Override
    public R visitShort(final short value, final Object param) {
        throw new UnsupportedOperationException();
    }

    @Override
    public R visitString(final String value, final Object param) {
        throw new UnsupportedOperationException();
    }

    @Override
    public R visitType(final TypeMirror value, final Object param) {
        throw new UnsupportedOperationException();
    }

    @Override
    public R visitEnumConstant(final VariableElement value, final Object param) {
        throw new UnsupportedOperationException();
    }

    @Override
    public R visitAnnotation(final AnnotationMirror value, final Object param) {
        throw new UnsupportedOperationException();
    }

    @Override
    public R visitArray(final List<? extends AnnotationValue> value, final Object param) {
        throw new UnsupportedOperationException();
    }

    @Override
    public R visitUnknown(final AnnotationValue value, final Object param) {
        throw new UnsupportedOperationException();
    }

}
