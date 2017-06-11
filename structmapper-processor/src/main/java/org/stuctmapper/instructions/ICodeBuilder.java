package org.stuctmapper.instructions;

import org.stuctmapper.model.instructions.TargetBuildResult;

public interface ICodeBuilder {
    void build(BuildMethodContext context, TargetBuildResult targetBuildResult);
}
