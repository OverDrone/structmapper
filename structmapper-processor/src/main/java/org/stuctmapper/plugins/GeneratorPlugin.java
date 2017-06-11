package org.stuctmapper.plugins;

import org.stuctmapper.instructions.ClassContext;
import org.stuctmapper.model.holder.AbstractTypeHolder;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;

public interface GeneratorPlugin {
    default void javaType(final TypeSpec.Builder javaTypeBuilder, final ClassName baseTypeName, 
            final ClassName implementationTypeName, AbstractTypeHolder type) {
    }

    void references(Builder javaTypeBuilder, ClassContext classContext);
}
