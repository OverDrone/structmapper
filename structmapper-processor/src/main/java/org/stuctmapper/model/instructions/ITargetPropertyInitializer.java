package org.stuctmapper.model.instructions;

import org.stuctmapper.instructions.BuildMethodContext;
import org.stuctmapper.instructions.StatementConcatenator;
import org.stuctmapper.model.holder.AbstractTypeHolder;

public interface ITargetPropertyInitializer extends IConverterCodeBlock {
    AbstractTypeHolder getType();
    Boolean isNull();

    default void prepare(BuildMethodContext context) {
        prepare(context, null);
    }

    void prepare(BuildMethodContext context, final String variableName);
    
    void append(BuildMethodContext context, StatementConcatenator concatenator);
    
    default void addReference() {
        addReference(1);
    }
    
    void addReference(int count);
    
    default boolean isFake() {
        return false;
    }
    
    String asVariable(BuildMethodContext context);
}
