package org.stuctmapper.processor;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.structmapper.annotations.Mapper;
import org.structmapper.test.AbstractFieldMapper;
import org.structmapper.test.AnotherMapper;
import org.stuctmapper.exceptions.ProcessorException;

import com.google.common.collect.ImmutableSet;

@Mapper
public class BuilderTest {
    private static final Set<Class<?>> annotatedClasses = ImmutableSet.of(AbstractFieldMapper.class, AnotherMapper.class);
    private static final File dir = new File("build/test");

    public static void main(final String[] args) throws ProcessorException, IOException {
        dir.mkdirs();
        ConsoleBuilder.build(dir, annotatedClasses, Consts.COMPONENTS_SINGLETON);
    }
}
