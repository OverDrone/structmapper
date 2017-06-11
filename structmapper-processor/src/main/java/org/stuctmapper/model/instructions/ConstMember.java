package org.stuctmapper.model.instructions;

import java.util.Objects;

import org.stuctmapper.instructions.StatementConcatenator;
import org.stuctmapper.model.holder.AbstractTypeHolder;

import com.squareup.javapoet.TypeName;

public class ConstMember extends AbstractConstExpression {

    private final AbstractTypeHolder enclosingType;
    private final String fieldName;
    private final boolean isMethod;

    public ConstMember(final String name, final AbstractTypeHolder fieldType, final AbstractTypeHolder enclosingType, 
            final String fieldName, final boolean isMethod) {
        super(fieldType, name);
        this.enclosingType = enclosingType;
        this.fieldName = fieldName;
        this.isMethod = isMethod;
    }
    
    @Override
    protected void buildInitializer(final StatementConcatenator concatenator) {
        if (enclosingType != null) {
            final TypeName typeName = enclosingType.getTypeName();
            concatenator.appendArgs("$T.", typeName);
        }
        concatenator.appendArgs("$N", fieldName);
        if (isMethod) {
            concatenator.appendArgs("()");
        }
    }

    @Override
    public int hashCode() {
        final int result = fieldName.hashCode();
        return result;
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
            final ConstMember that = (ConstMember) obj;
            if (!Objects.equals(this.enclosingType, that.enclosingType)) {
                return false;
            } else if (!Objects.equals(this.fieldName, that.fieldName)) {
                return false;
            } else if (this.isMethod != that.isMethod) {
                return false;
            } else {
                return true;
            }
        }
    }

}
