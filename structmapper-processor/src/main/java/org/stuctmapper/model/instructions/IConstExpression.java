package org.stuctmapper.model.instructions;

import com.squareup.javapoet.FieldSpec;

public interface IConstExpression extends ISourceExpression {
    FieldSpec.Builder getAssignment();
}
