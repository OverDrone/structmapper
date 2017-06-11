package org.stuctmapper.mapping;

import java.util.Map;

import org.stuctmapper.model.instructions.IConstExpression;
import org.stuctmapper.model.mapping.MethodMapping;
import org.stuctmapper.model.properties.ConverterMethodInfo;

public interface IMappingBuilder {
    MethodMapping build(ConverterMethodInfo method, Map<String, IConstExpression> constsMap);
}
