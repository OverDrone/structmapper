package org.stuctmapper.model.instructions;

import javax.lang.model.element.Modifier;

import org.stuctmapper.instructions.BuildMethodContext;
import org.stuctmapper.instructions.StatementConcatenator;
import org.stuctmapper.model.holder.AbstractTypeHolder;

import com.google.common.base.Preconditions;
import com.squareup.javapoet.FieldSpec;

public abstract class AbstractConstExpression extends AbstractSourceExpression implements IConstExpression {
    private final String name;

    protected AbstractConstExpression(final AbstractTypeHolder type, final String name) {
        super(type);
        this.name = name;
    }
    
    @Override
    protected String generateVariableStatement(final BuildMethodContext context, boolean variableDeclared) {
        Preconditions.checkArgument(!variableDeclared);
        return name;
    }

    @Override
    public FieldSpec.Builder getAssignment() {
        final StatementConcatenator concatenator = new StatementConcatenator();
        buildInitializer(concatenator);
        final FieldSpec.Builder result = FieldSpec.builder(getType().getTypeName(), name, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);
        final String template = concatenator.getTemplate();
        final Object[] arguments = concatenator.getArguments();
        result.initializer(template, arguments);
        return result;
    }
    
    @Override
    protected void appendInline(final BuildMethodContext context, final StatementConcatenator concatenator) {
        concatenator.appendArgs("$N", name);
    }
    
    protected abstract void buildInitializer(StatementConcatenator concatenator);
}
