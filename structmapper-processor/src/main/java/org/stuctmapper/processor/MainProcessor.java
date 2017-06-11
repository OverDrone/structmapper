package org.stuctmapper.processor;

import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import org.structmapper.annotations.Mapper;
import org.stuctmapper.exceptions.ProcessorException;
import org.stuctmapper.log.Logger;
import org.stuctmapper.model.holder.AbstractTypeHolder;
import org.stuctmapper.model.holder.MirrorTypeHolder;
import org.stuctmapper.model.holder.TypeHolderCache;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;

@SupportedAnnotationClasses(Mapper.class)
@AutoService(Processor.class)
@SupportedOptions(Consts.COMPONENTS)
public class MainProcessor extends AbstractProcessor {
    private MainBuilder builder;
    
    @Override
    public void init() {
        super.init();
        final String components = getOption(Consts.COMPONENTS, Consts.COMPONENTS_SINGLETON);
        final Logger logger = getLogger();
        
        final ThrowingConsumer<JavaFile> writer = (javaFile) -> {
            final Filer filer = getFiler();
            javaFile.writeTo(filer);
        };
        builder = new MainBuilder(components, logger, writer);
    }
    
    @Override
    public void processOnce(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) throws IOException {
        final Set<? extends Element> types = getElementsAnnotatedWith(Mapper.class, annotations, roundEnv);
        process(types);
    }
    
    public void process(final Set<? extends Element> types) throws ProcessorException, IOException {
        final TypeHolderCache cache = new TypeHolderCache();
        if (types != null) {
            for (final Element type : types) {
                final TypeMirror typeMirror = type.asType();
                final AbstractTypeHolder holder = new MirrorTypeHolder(typeMirror, cache);
                final AbstractTypeHolder registeredHolder = cache.register(holder);
                builder.processType(registeredHolder);
            }
        }
    }
    
}
