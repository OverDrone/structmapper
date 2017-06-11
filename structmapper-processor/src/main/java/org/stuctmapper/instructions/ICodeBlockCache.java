package org.stuctmapper.instructions;

import org.stuctmapper.model.instructions.IConverterCodeBlock;

public interface ICodeBlockCache {
    <T extends IConverterCodeBlock> T add(T value);
}
