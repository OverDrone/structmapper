package org.stuctmapper.model.instructions;

import org.stuctmapper.instructions.BuildMethodContext;
import org.stuctmapper.instructions.StatementConcatenator;
import org.stuctmapper.model.holder.AbstractTypeHolder;

public class NullPropertyInitializer extends AbstractTargetInitializer {

    private final String predefinedVariableName;

    public NullPropertyInitializer(final String predefinedVariableName, final AbstractTypeHolder type) {
        super(type);
        this.predefinedVariableName = predefinedVariableName;
    }
    
    @Override
    public String getPredefinedVariableName() {
        return predefinedVariableName;
    }
    
    @Override
    protected void appendInline(BuildMethodContext context, StatementConcatenator concatenator) {
        concatenator.appendArgs("null");
    }
    
    @Override
    public Boolean isNull() {
        return Boolean.TRUE;
    }

}
