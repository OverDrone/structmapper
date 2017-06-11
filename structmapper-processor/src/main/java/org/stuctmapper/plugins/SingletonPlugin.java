package org.stuctmapper.plugins;

import java.util.Map;
import java.util.Map.Entry;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

import org.stuctmapper.instructions.ClassContext;
import org.stuctmapper.model.TypeQualifier;
import org.stuctmapper.model.holder.AbstractTypeHolder;
import org.stuctmapper.utils.JavaBuilderUtils;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;

public class SingletonPlugin implements GeneratorPlugin {
    @Override
    public void javaType(final TypeSpec.Builder javaTypeBuilder, final ClassName baseTypeName, 
            final ClassName implementationTypeName, final AbstractTypeHolder type) {
        final CodeBlock.Builder fieldInitBuilder = CodeBlock.builder();
        fieldInitBuilder.add("new $T()", implementationTypeName);
        final CodeBlock fieldInit = fieldInitBuilder.build();
        final FieldSpec.Builder fieldBuilder = FieldSpec.builder(baseTypeName, "INSTANCE", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);
        fieldBuilder.initializer(fieldInit);
        final FieldSpec instanceField = fieldBuilder.build();
        javaTypeBuilder.addField(instanceField);

        final MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder();
        constructorBuilder.addModifiers(Modifier.PRIVATE);
        if (!type.isInterface()) {
            constructorBuilder.addStatement("super()");
        }
        final MethodSpec constructor = constructorBuilder.build();
        javaTypeBuilder.addMethod(constructor);
    }

    @Override
    public void references(final Builder javaTypeBuilder, final ClassContext classContext) {
        final Map<AbstractTypeHolder, String> referenceMap = classContext.getReferenceMap();

        for (final Entry<AbstractTypeHolder, String> entry : referenceMap.entrySet()) {
            final AbstractTypeHolder type = entry.getKey();
            final TypeQualifier qualifier = type.getQualifier();
            final String name = qualifier.getName();
            final String packageName = qualifier.getPackageName();
            final boolean isInterface = type.isInterface();
            final Element errorElement = type.getErrorElement();
            final String implementationName = JavaBuilderUtils.getImplementationName(name, isInterface, errorElement);
            final ClassName implementationClassName = ClassName.get(packageName, implementationName);
            final String variableName = entry.getValue();
            final TypeName typeName = type.getTypeName();
            
            final CodeBlock.Builder fieldInitBuilder = CodeBlock.builder();
            fieldInitBuilder.add("$T.INSTANCE", implementationClassName);
            final CodeBlock fieldInit = fieldInitBuilder.build();
            final FieldSpec.Builder fieldBuilder = FieldSpec.builder(typeName, variableName, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);
            fieldBuilder.initializer(fieldInit);
            final FieldSpec instanceField = fieldBuilder.build();
            javaTypeBuilder.addField(instanceField);
        }
    }
}
