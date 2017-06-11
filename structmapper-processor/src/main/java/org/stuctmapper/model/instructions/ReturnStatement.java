package org.stuctmapper.model.instructions;

import org.stuctmapper.instructions.BuildMethodContext;
import org.stuctmapper.instructions.StatementConcatenator;

import com.squareup.javapoet.CodeBlock;

public class ReturnStatement implements ITargetPropertyAccessor {

    @Override
    public void access(final BuildMethodContext context, final ITargetPropertyInitializer initializer, final TargetPropertyAccessorStage stage) {
        switch (stage) {
        case FINISH:
            final CodeBlock.Builder builder = context.getReturnBuilder();
            final StatementConcatenator concatenator = new StatementConcatenator();
            concatenator.append("return ");
            initializer.append(context, concatenator);
            final String template = concatenator.getTemplate();
            final Object[] arguments = concatenator.getArguments();
            builder.addStatement(template, arguments);
            break;
            
        default:
            break;
        }
    }
    

}
