package org.structmapper.test;

import org.structmapper.annotations.Mapper;

@Mapper
public interface AnotherMapper {
    
    default Integer convert(final Integer value) {
        return value;
    }
}
