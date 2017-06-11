package org.structmapper.test;

import org.structmapper.annotations.Const;
import org.structmapper.annotations.Mapper;
import org.structmapper.annotations.Mapping;
import org.structmapper.annotations.Mappings;
import org.structmapper.annotations.Path;
import org.structmapper.model.Bean1;
import org.structmapper.model.Bean2;

@Mapper(references = AnotherMapper.class, consts = @Const(name = "const1", l = AbstractFieldMapper.C))
public abstract class AbstractFieldMapper {
    public static final long C = 123L;
    @Mappings({
        @Mapping(sources = @Path("source1.field*Id.method(source1.anotherMethod(),target2).field."))
    })
    protected abstract Bean2 convert(Bean1 src);
}
