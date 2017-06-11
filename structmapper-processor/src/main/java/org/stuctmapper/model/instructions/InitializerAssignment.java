package org.stuctmapper.model.instructions;

import org.stuctmapper.instructions.BuildMethodContext;
import org.stuctmapper.instructions.StatementConcatenator;
import org.stuctmapper.log.Logger;
import org.stuctmapper.model.holder.AbstractElementHolder;
import org.stuctmapper.model.holder.AbstractTypeHolder;
import org.stuctmapper.model.properties.Property;

import com.squareup.javapoet.CodeBlock;

public class InitializerAssignment implements ITargetPropertyAccessor {
    private final ITargetPropertyInitializer initializer;
    private final Property property;
    private final boolean wrapNotNull;

    public InitializerAssignment(final ITargetPropertyInitializer initializer, final Property property) {
        this.initializer = initializer;
        this.property = property;
        final AbstractTypeHolder targetType = property.getType();
        initializer.addReference();
        this.wrapNotNull = targetType.isNull() == Boolean.FALSE && initializer.isNull() != Boolean.FALSE;
        if (this.wrapNotNull) {
            initializer.addReference();
        }
    }

    @Override
    public void access(final BuildMethodContext context, final ITargetPropertyInitializer parentInitializer, final TargetPropertyAccessorStage stage) {
        switch (stage) {
        
        case DECLARE:
            initializer.asVariable(context);
            break;
            
        case NOT_NULL:
            initializer.prepare(context);
            wrapWrite(context, parentInitializer);
            break;
            
        case IF_NOT_NULL:
            wrapWrite(context, parentInitializer);
            break;
            
        default:
            break;
        }
    }
    

    private void wrapWrite(final BuildMethodContext context, final ITargetPropertyInitializer parentInitializer) {
        if (wrapNotNull) {
            initializer.asVariable(context);
            final StatementConcatenator concatenator = new StatementConcatenator();
            concatenator.append("if (");
            initializer.append(context, concatenator);
            concatenator.append(" != null)");
            final String template = concatenator.getTemplate();
            final Object[] arguments = concatenator.getArguments();
            final CodeBlock.Builder builder = context.getSettersBuilder();
            builder.beginControlFlow(template, arguments);
            write(context, parentInitializer);
            builder.endControlFlow();
        } else {
            write(context, parentInitializer);
        }
    }
    
    private void write(final BuildMethodContext context, final ITargetPropertyInitializer parentInitializer) {
        final AbstractElementHolder field = property.getField();
        final AbstractElementHolder setter = property.getSetter();
        final CodeBlock.Builder builder = context.getSettersBuilder();
        final StatementConcatenator concatenator = new StatementConcatenator();
        parentInitializer.append(context, concatenator);
        concatenator.appendArgs(".");
        if (setter != null) {
            final String setterName = setter.getName();
            concatenator.appendArgs("$N(", setterName);
            initializer.append(context, concatenator);
            concatenator.append(")");
        } else if (field != null) {
            final String fieldName = field.getName();
            concatenator.appendArgs("$N =", fieldName);
            initializer.append(context, concatenator);
        } else {
            Logger.fail("property " + property + " not writeable in initializer");
        }
        final String template = concatenator.getTemplate();
        final Object[] arguments = concatenator.getArguments();
        builder.addStatement(template, arguments);
    }

}
