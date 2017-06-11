package org.stuctmapper.model.instructions;

import org.stuctmapper.instructions.BuildMethodContext;

public interface ITargetPropertyAccessor extends IConverterCodeBlock {
    void access(final BuildMethodContext context, ITargetPropertyInitializer initializer, final TargetPropertyAccessorStage stage);
}
