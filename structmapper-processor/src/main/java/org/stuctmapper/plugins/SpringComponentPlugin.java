package org.stuctmapper.plugins;

import java.util.Map;
import java.util.Map.Entry;

import org.stuctmapper.instructions.ClassContext;
import org.stuctmapper.model.holder.AbstractTypeHolder;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;

public class SpringComponentPlugin implements GeneratorPlugin {
    @Override
    public void javaType(final TypeSpec.Builder javaTypeBuilder, final ClassName baseTypeName, 
            final ClassName implementationTypeName, final AbstractTypeHolder type) {
        final ClassName annotationClassName = ClassName.get("org.springframework.stereotype", "Component");
        AnnotationSpec.Builder annotationBuilder = AnnotationSpec.builder(annotationClassName);
        final AnnotationSpec annotation = annotationBuilder.build();
        javaTypeBuilder.addAnnotation(annotation);
    }

    @Override
    public void references(Builder javaTypeBuilder, ClassContext classContext) {
        final Map<AbstractTypeHolder, String> referenceMap = classContext.getReferenceMap();

        for (final Entry<AbstractTypeHolder, String> entry : referenceMap.entrySet()) {
            final AbstractTypeHolder type = entry.getKey();
            final String variableName = entry.getValue();
            final TypeName typeName = type.getTypeName();
            
            final ClassName annotationClassName = ClassName.get("org.springframework.beans.factory.annotation", "Autowired");
            AnnotationSpec.Builder annotationBuilder = AnnotationSpec.builder(annotationClassName);
            final AnnotationSpec annotation = annotationBuilder.build();
            final FieldSpec.Builder fieldBuilder = FieldSpec.builder(typeName, variableName);
            fieldBuilder.addAnnotation(annotation);
            final FieldSpec instanceField = fieldBuilder.build();
            javaTypeBuilder.addField(instanceField);
        }
    }

}
