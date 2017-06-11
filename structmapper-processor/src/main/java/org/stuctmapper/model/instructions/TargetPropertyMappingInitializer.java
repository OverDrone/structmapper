package org.stuctmapper.model.instructions;

import org.stuctmapper.instructions.BuildMethodContext;
import org.stuctmapper.instructions.StatementConcatenator;
import org.stuctmapper.model.holder.AbstractTypeHolder;

public class TargetPropertyMappingInitializer implements ITargetPropertyInitializer {
    private final ISourceExpression source;

    public TargetPropertyMappingInitializer(final ISourceExpression source) {
        this.source = source;
    }

    @Override
    public AbstractTypeHolder getType() {
        return source.getType();
    }

    @Override
    public Boolean isNull() {
        final Boolean result = source.isNull();
        return result;
    }
    
    @Override
    public void prepare(final BuildMethodContext context, final String variableName) {
        source.prepare(context, variableName);
    }
    
    @Override
    public void append(BuildMethodContext context, StatementConcatenator concatenator) {
        source.append(context, concatenator);
    }
    
    @Override
    public void addReference(int count) {
        source.addAppendReference(count);
    }
    
    @Override
    public String asVariable(BuildMethodContext context) {
        final String result = source.asVariable(context);
        return result;
    }
}
