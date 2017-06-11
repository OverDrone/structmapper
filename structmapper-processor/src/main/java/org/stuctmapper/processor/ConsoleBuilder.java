package org.stuctmapper.processor;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.Messager;

import org.stuctmapper.exceptions.ProcessorException;
import org.stuctmapper.log.Logger;
import org.stuctmapper.model.holder.AbstractTypeHolder;
import org.stuctmapper.model.holder.ClassTypeHolder;
import org.stuctmapper.model.holder.TypeHolderCache;

import com.squareup.javapoet.JavaFile;

public final class ConsoleBuilder {
    private ConsoleBuilder() {
    }
    
    public static void build(final File dir, final Set<Class<?>> annotatedClasses, final String components) throws ProcessorException, IOException {
        final Messager messager = new ConsoleMessager();
        final Logger logger = new Logger(messager);
        final ThrowingConsumer<JavaFile> writer = (javaFile) -> {
            javaFile.writeTo(dir);
        };
        final MainBuilder builder = new MainBuilder(components, logger, writer);
        final TypeHolderCache cache = new TypeHolderCache();
        for (final Class<?> annotatedClass : annotatedClasses) {
            final AbstractTypeHolder holder = new ClassTypeHolder(annotatedClass, cache);
            final AbstractTypeHolder registeredHolder = cache.register(holder);
            builder.processType(registeredHolder);
        }
    }
}
