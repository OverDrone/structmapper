package org.stuctmapper.model.instructions;

import java.util.Collection;
import java.util.List;

import org.stuctmapper.instructions.BuildMethodContext;
import org.stuctmapper.instructions.StatementConcatenator;
import org.stuctmapper.model.holder.AbstractTypeHolder;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

public class ImmutableConstructorInitializer extends AbstractTargetInitializer {

    private final List<ITargetPropertyInitializer> list;
    private final String predefinedVariableName;

    public ImmutableConstructorInitializer(final String predefinedVariableName, final AbstractTypeHolder type, 
            final Collection<ITargetPropertyInitializer> list) {
        super(type);
        this.predefinedVariableName = predefinedVariableName;
        this.list = ImmutableList.copyOf(list);
        for (final ITargetPropertyInitializer item : list) {
            item.addReference();
        }
    }
    
    @Override
    public String getPredefinedVariableName() {
        return predefinedVariableName;
    }
    
    @Override
    protected void appendInline(BuildMethodContext context, StatementConcatenator concatenator) {
        final TypeName typeName = type.getTypeName();
        concatenator.appendArgs("new $T(", typeName);
        boolean first = true;
        for (final ITargetPropertyInitializer initializer : list) {
            if (first) {
                first = false;
            } else {
                concatenator.append(", ");
            }
            initializer.append(context, concatenator);
        }
        concatenator.append(")");
        final String template = concatenator.getTemplate();
        final Object[] arguments = concatenator.getArguments();
        final CodeBlock.Builder builder = getVariableBuilder(context);
        builder.addStatement(template, arguments);
    }
    
    @Override
    public Boolean isNull() {
        return Boolean.FALSE;
    }

}
