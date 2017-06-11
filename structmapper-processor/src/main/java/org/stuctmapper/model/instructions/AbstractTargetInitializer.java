package org.stuctmapper.model.instructions;

import org.stuctmapper.instructions.BuildMethodContext;
import org.stuctmapper.instructions.StatementConcatenator;
import org.stuctmapper.model.holder.AbstractTypeHolder;

import com.squareup.javapoet.CodeBlock;

public abstract class AbstractTargetInitializer extends AbstractVariableHolder implements ITargetPropertyInitializer {
    private boolean prepared = false;
    private int referenceCount = 0;
    
    protected AbstractTargetInitializer(final AbstractTypeHolder type) {
        super(type);
    }
    
    @Override
    public void addReference(final int count) {
        referenceCount += count;
    }

    @Override
    public void prepare(final BuildMethodContext context, final String variableName) {
        if (!prepared) {
            prepared = true;
            final boolean variableDeclared = variableName != null;
            if (variableDeclared) {
                setVariableName(variableName);
            }
            prepareOnce(context, variableDeclared);
        }
    }
    
    protected void prepareOnce(final BuildMethodContext context, final boolean variableDeclared) {
        if (variableDeclared || referenceCount > 1) {
            generateVariableStatement(context, variableDeclared);
        }
    }
    
    protected String generateVariableStatement(final BuildMethodContext context, final boolean variableDeclared) {
        final String variableName = generateVariableName(context, variableDeclared);
        final StatementConcatenator concatenator = new StatementConcatenator();
        if (!variableDeclared) {
            applyVariableDeclaration(concatenator, true);
        }
        concatenator.appendArgs("$N = ", variableName);
        appendInline(context, concatenator);
        final String template = concatenator.getTemplate();
        final Object[] arguments = concatenator.getArguments();
        final CodeBlock.Builder builder = getVariableBuilder(context);
        builder.addStatement(template, arguments);
        return variableName;
    }
    
    @Override
    public String asVariable(BuildMethodContext context) {
        final String variableName = getVariableName();
        if (variableName != null) {
            return variableName;
        } else {
            final String result = generateVariableStatement(context, false);
            return result;
        }
    }

    @Override
    public void append(final BuildMethodContext context, final StatementConcatenator concatenator) {
        final String variableName = getVariableName();
        if (variableName != null) {
            concatenator.appendArgs("$N", variableName);
        } else {
            appendInline(context, concatenator);
        }
    }
    
    protected abstract void appendInline(BuildMethodContext context, StatementConcatenator concatenator);

    @Override
    protected CodeBlock.Builder getVariableBuilder(final BuildMethodContext context) {
        final CodeBlock.Builder result = context.getSettersBuilder();
        return result;
    }
    
    @Override
    public Boolean isNull() {
        final Boolean result = type.isNull();
        return result;
    }
}
