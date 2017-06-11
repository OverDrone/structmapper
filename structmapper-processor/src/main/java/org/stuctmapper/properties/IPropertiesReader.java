package org.stuctmapper.properties;

import org.stuctmapper.model.holder.AbstractTypeHolder;
import org.stuctmapper.model.properties.ConverterConstructorInfo;
import org.stuctmapper.model.properties.ConverterInfo;
import org.stuctmapper.model.properties.ParsedType;

public interface IPropertiesReader {
    ConverterInfo getConverter(AbstractTypeHolder type);

    ParsedType getType(AbstractTypeHolder type);

    ConverterConstructorInfo getConstructors(AbstractTypeHolder type);
}
