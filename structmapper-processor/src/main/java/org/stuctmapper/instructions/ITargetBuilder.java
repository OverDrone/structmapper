package org.stuctmapper.instructions;

import org.stuctmapper.model.instructions.TargetBuildResult;
import org.stuctmapper.model.mapping.MethodMapping;
import org.stuctmapper.model.properties.ConverterInfo;

public interface ITargetBuilder {
    TargetBuildResult build(BuildMethodContext context, MethodMapping info, final ConverterInfo converterInfo);
}
