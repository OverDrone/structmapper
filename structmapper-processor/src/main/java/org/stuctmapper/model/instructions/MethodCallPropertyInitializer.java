package org.stuctmapper.model.instructions;

import java.util.Collection;

import org.stuctmapper.instructions.BuildMethodContext;
import org.stuctmapper.instructions.StatementConcatenator;
import org.stuctmapper.model.holder.AbstractTypeHolder;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.CodeBlock;

public class MethodCallPropertyInitializer extends AbstractTargetInitializer {
    
    private final ImmutableList<ITargetPropertyInitializer> list;
    private final String methodName;
    private final ITargetPropertyInitializer objectInitializer;

    public MethodCallPropertyInitializer(final AbstractTypeHolder type, final ITargetPropertyInitializer objectInitializer, 
            final String methodName, final Collection<ITargetPropertyInitializer> list) {
        super(type);
        this.list = ImmutableList.copyOf(list);
        this.methodName = methodName;
        this.objectInitializer = objectInitializer;
        objectInitializer.addReference();
        for (final ITargetPropertyInitializer item : list) {
            item.addReference();
        }
    }

    @Override
    protected void appendInline(BuildMethodContext context, StatementConcatenator concatenator) {
        if (objectInitializer != null) {
            objectInitializer.append(context, concatenator);
            concatenator.append(".");
        }
        concatenator.appendArgs("$N(", methodName);
        boolean first = true;
        for (final ITargetPropertyInitializer item : list) {
            if (first) {
                first = false;
            } else {
                concatenator.append(", ");
            }
            item.append(context, concatenator);
        }
        concatenator.append(")");
        final String template = concatenator.getTemplate();
        final Object[] arguments = concatenator.getArguments();
        final CodeBlock.Builder builder = getVariableBuilder(context);
        builder.addStatement(template, arguments);
    }
    
}
