package org.stuctmapper.processor;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Completion;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;

import org.stuctmapper.exceptions.ProcessorException;
import org.stuctmapper.log.Logger;

import com.google.common.base.Preconditions;

public abstract class AbstractProcessor implements Processor {
    private ProcessingEnvironment processingEnv;
    private Logger logger;

    @Override
    public Set<String> getSupportedOptions() {
        final Set<String> result = new HashSet<>();
        final Class<?> thisClass = getClass();
        final SupportedOptions annotation = thisClass.getAnnotation(SupportedOptions.class);
        if (annotation != null) {
            final String[] values = annotation.value();
            if (values != null) {
                final List<String> valuesList = Arrays.asList(values);
                result.addAll(valuesList);
            }
        }
        return result;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        final Set<String> result = new HashSet<>();
        final Class<?> thisClass = getClass();
        final SupportedAnnotationTypes annotation = thisClass.getAnnotation(SupportedAnnotationTypes.class);
        if (annotation != null) {
            final String[] values = annotation.value();
            if (values != null) {
                final List<String> valuesList = Arrays.asList(values);
                result.addAll(valuesList);
            }
        }
        final Iterable<Class<?>> classes = getSupportedAnnotationClasses();
        for (final Class<?> annotationClass : classes) {
            if (annotationClass == null) {
                result.add("*");
            } else {
                final String className = annotationClass.getName();
                result.add(className);
            }
        }
        return result;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        final Class<?> thisClass = getClass();
        final SupportedSourceVersion annotation = thisClass.getAnnotation(SupportedSourceVersion.class);
        if (annotation != null) {
            final SourceVersion value = annotation.value();
            if (value != null) {
                return value;
            }
        }
        final SourceVersion result = SourceVersion.latest();
        return result;
    }
    
    protected Collection<Class<?>> getSupportedAnnotationClasses() {
        final Set<Class<?>> result = new HashSet<>();
        final Class<?> thisClass = getClass();
        final SupportedAnnotationClasses annotation = thisClass.getAnnotation(SupportedAnnotationClasses.class);
        if (annotation != null) {
            final Class<?>[] values = annotation.value();
            if (values != null) {
                final List<Class<?>> valuesList = Arrays.asList(values);
                result.addAll(valuesList);
            }
        }
        return result;
    }

    @Override
    public void init(final ProcessingEnvironment processingEnv) {
        Preconditions.checkArgument(this.processingEnv == null);
        Preconditions.checkNotNull(processingEnv);
        this.processingEnv = processingEnv;
        final Messager messager = processingEnv.getMessager();
        this.logger = new Logger(messager);
        try {
            init();
        } catch (ProcessorException e) {
            logger.handleException(e);
        }
    }
    
    protected void init() {
    }

    @Override
    public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation, ExecutableElement member, String userText) {
        final List<? extends Completion> result = new ArrayList<>();
        return result;
    }
    
    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        try {
            if (roundEnv.processingOver()) {
                return true;
            }
            processOnce(annotations, roundEnv);
            return true;
        } catch (ProcessorException e) {
            logger.handleException(e);
            return false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    protected void processOnce(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) throws IOException, ProcessorException {
    }
    
    protected static Set<? extends Element> getElementsAnnotatedWith(Class<? extends Annotation> annotationClass, 
            final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        final TypeElement annotationElement = get(annotationClass, annotations);
        if (annotationElement == null) {
            return null;
        } else {
            final Set<? extends Element> result = roundEnv.getElementsAnnotatedWith(annotationElement);
            return result;
        }
    }
    
    protected static TypeElement get(Class<? extends Annotation> annotationClass, final Set<? extends TypeElement> annotations) {
        final String searchName = annotationClass.getName();
        for (final TypeElement annotation : annotations) {
            final Name annotationName = annotation.getQualifiedName();
            if (searchName.contentEquals(annotationName)) {
                return annotation;
            }
        }
        return null;
    }

    protected ProcessingEnvironment getProcessingEnv() {
        return processingEnv;
    }
    

    protected Filer getFiler() {
        final Filer result = processingEnv.getFiler();
        return result;
    }

    protected String getOption(final String name, final String defaultValue) {
        final Map<String, String> options = processingEnv.getOptions();
        final String result = options.get(name);
        if (result != null) {
            return result;
        } else {
            return defaultValue;
        }
    }
    
    protected Logger getLogger() {
        return logger;
    }
}
