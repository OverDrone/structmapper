package org.stuctmapper.model.instructions;

import java.util.Objects;

import org.stuctmapper.instructions.StatementConcatenator;
import org.stuctmapper.model.holder.AbstractTypeHolder;
import org.stuctmapper.model.holder.ClassTypeHolder;
import org.stuctmapper.model.holder.TypeHolderCache;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

public class ConstLiteral extends AbstractConstExpression {
    private Object value;
    
    public ConstLiteral(final Object value, final TypeHolderCache cache, final String name) {
        super(getType(value, cache), name);
        this.value = value;
    }
    
    @Override
    protected void buildInitializer(final StatementConcatenator concatenator) {
        if (value instanceof String) {
            concatenator.appendArgs("$S", value);
        } else if (value instanceof AbstractTypeHolder) {
            final AbstractTypeHolder typeHolder = (AbstractTypeHolder) value;
            final TypeName typeName = typeHolder.getTypeName();
            concatenator.appendArgs("$T", typeName);
        } else if (value instanceof Long) {
            concatenator.appendArgs("$LL", value);
        } else {
            concatenator.appendArgs("$L", value);
        }
    }
    
    
    private static AbstractTypeHolder getType(final Object value, final TypeHolderCache cache) {
        final Class<?> valueClass = value.getClass();
        if (valueClass == AbstractTypeHolder.class) {
            final AbstractTypeHolder result = (AbstractTypeHolder) value;
            return result;
        } else {
            final ClassName className = ClassName.get(valueClass);
            final TypeName unboxed = className.unbox();
            final AbstractTypeHolder holder;
            if (!Objects.equals(unboxed, className)) {
                holder = new ClassTypeHolder(unboxed, cache);
            } else {
                holder = new ClassTypeHolder(valueClass, cache);
            }
            final AbstractTypeHolder result = cache.register(holder);
            return result;
        }
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        } else if (obj == this) {
            return true;
        } else if (obj.getClass() != this.getClass()) {
            return false;
        } else {
            final ConstLiteral that = (ConstLiteral) obj;
            final boolean result = Objects.equals(this.value, that.value);
            return result;
        }
    }

}
