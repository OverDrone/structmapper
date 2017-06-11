package org.stuctmapper.model.instructions;

import org.stuctmapper.instructions.BuildMethodContext;
import org.stuctmapper.instructions.StatementConcatenator;
import org.stuctmapper.instructions.VariableNameGenerator;
import org.stuctmapper.model.holder.AbstractTypeHolder;

import com.google.common.base.Preconditions;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

public abstract class AbstractVariableHolder {
    protected final AbstractTypeHolder type;
    private String variableName;
    
    public AbstractVariableHolder(final AbstractTypeHolder type) {
        this.type = type;
    }
    
    protected void applyVariableDeclaration(final BuildMethodContext context) {
        final StatementConcatenator concatenator = new StatementConcatenator();
        applyVariableDeclaration(concatenator, false);
        final Object[] arguments = concatenator.getArguments();
        final String template = concatenator.getTemplate();
        if (!template.isEmpty()) {
            final CodeBlock.Builder settersBuilder = getVariableBuilder(context);
            settersBuilder.addStatement(template, arguments);
        }
    }
    
    protected abstract CodeBlock.Builder getVariableBuilder(final BuildMethodContext context);
    
    protected void applyVariableDeclaration(final StatementConcatenator concatenator, boolean hasInitializer) {
        final TypeName typeName = type.getTypeName();
        concatenator.appendArgs("final $T ", typeName);
        if (!hasInitializer) {
            final String variableName = getVariableName();
            Preconditions.checkNotNull(variableName);
            concatenator.appendArgs("$N", variableName);
        }
    }
    
    protected String generateVariableName(final BuildMethodContext context, final boolean variableDeclared) {
        Preconditions.checkArgument(variableDeclared == (variableName != null));
        if (variableName == null) {
            final String predefinedVariableName = getPredefinedVariableName();
            final VariableNameGenerator nameGenerator = context.getNameGenerator();
            final String variableName = nameGenerator.getNext(predefinedVariableName);
            setVariableName(variableName);
        }
        return this.variableName;
    }
    
    public String getPredefinedVariableName() {
        return null;
    }
    
    
    protected String getVariableName() {
        return variableName;
    }
    
    protected void setVariableName(final String variableName) {
        Preconditions.checkNotNull(variableName);
        Preconditions.checkArgument(this.variableName == null);
        this.variableName = variableName;
    }
    
    public AbstractTypeHolder getType() {
        return type;
    }
}
