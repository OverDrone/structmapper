package org.stuctmapper.instructions;

import java.util.Map;

import org.stuctmapper.model.holder.AbstractTypeHolder;
import org.stuctmapper.model.instructions.IConstExpression;

public interface IConstParser {
    Map<String, IConstExpression> build(AbstractTypeHolder type);
}
