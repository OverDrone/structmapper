package org.stuctmapper.model.instructions;

import org.stuctmapper.instructions.BuildMethodContext;
import org.stuctmapper.instructions.StatementConcatenator;
import org.stuctmapper.model.holder.AbstractTypeHolder;

import com.squareup.javapoet.TypeName;

public class DefaultConstructorInitializer extends AbstractTargetInitializer {

    private final String predefinedVariableName;

    public DefaultConstructorInitializer(final String predefinedVariableName, final AbstractTypeHolder type) {
        super(type);
        this.predefinedVariableName = predefinedVariableName;
    }
    
    @Override
    public String getPredefinedVariableName() {
        return predefinedVariableName;
    }

    @Override
    public Boolean isNull() {
        return Boolean.FALSE;
    }
    
    @Override
    protected void appendInline(BuildMethodContext context, StatementConcatenator concatenator) {
        final TypeName typeName = type.getTypeName();
        concatenator.appendArgs("new $T()", typeName);
    }
}
