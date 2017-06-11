package org.stuctmapper.model.instructions;

import org.stuctmapper.instructions.BuildMethodContext;

@FunctionalInterface
public interface ISourceExpressionAccessor {
    public void apply(BuildMethodContext context, final ISourceExpression expression);
}
