package org.stuctmapper.model.instructions;

import java.util.ArrayList;
import java.util.List;

import org.stuctmapper.instructions.BuildMethodContext;
import org.stuctmapper.instructions.StatementConcatenator;
import org.stuctmapper.model.holder.AbstractTypeHolder;

import com.google.common.base.Preconditions;
import com.squareup.javapoet.CodeBlock;

public abstract class AbstractSourceExpression extends AbstractVariableHolder implements ISourceExpression {
    private final List<ISourceExpression> prepareChain = new ArrayList<>();
    private boolean prepared;
    private int referenceCount;
    
    protected AbstractSourceExpression(final AbstractTypeHolder type) {
        super(type);
    }
    
    @Override
    public void prepare(BuildMethodContext context, String variableName) {
        if (!prepared) {
            prepared = true;
            final boolean variableDeclared = variableName != null; 
            if (variableDeclared) {
                setVariableName(variableName);
            }
            prepareOnce(context, variableDeclared);
            for (final ISourceExpression item : prepareChain) {
                item.prepare(context);
            }
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
    public void append(final BuildMethodContext context, final StatementConcatenator concatenator) {
        final String variableName = getVariableName();
        if (variableName != null) {
            concatenator.appendArgs("$N", variableName);
        } else {
            appendInline(context, concatenator);
        }
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
    protected CodeBlock.Builder getVariableBuilder(final BuildMethodContext context) {
        final CodeBlock.Builder result = context.getGettersBuilder();
        return result;
    }

    protected abstract void appendInline(BuildMethodContext context, StatementConcatenator concatenator);

    @Override
    public void addAppendReference(final int count) {
        Preconditions.checkArgument(count > 0);
        referenceCount += count;
    }
    
    @Override
    public void appendPrepareChain(final ISourceExpression expression) {
        prepareChain.add(expression);
    }
    
    @Override
    public Boolean isNull() {
        final Boolean result = type.isNull();
        return result;
    }
}
