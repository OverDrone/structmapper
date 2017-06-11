package org.stuctmapper.model.instructions;

import org.stuctmapper.instructions.BuildMethodContext;

public interface IStatement extends IConverterCodeBlock {
    void apply(BuildMethodContext context);
}
