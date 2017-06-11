package org.stuctmapper.model.instructions;

import org.stuctmapper.instructions.BuildMethodContext;
import org.stuctmapper.instructions.StatementConcatenator;
import org.stuctmapper.model.holder.AbstractTypeHolder;

public interface ISourceExpression extends IConverterCodeBlock {
    default void addAppendReference() {
        addAppendReference(1);
    }
    
    void addAppendReference(final int count);
    
    void appendPrepareChain(ISourceExpression expression);
    
    default void prepare(final BuildMethodContext context) {
        prepare(context, null);
    }

    /**
     * Если ссылок больше одной, то будет создана новая переменная.
     * Туда запишется значение данного выражения.
     * В качестве выражения будет использоваться имя переменной.
     * @param variableName 
     *  Если задано, заставляет явно использовать данное имя переменной. 
     *  И принудительно записать туда значение, даже если ссылка только одна
     */
    void prepare(final BuildMethodContext context, String variableName);
    
    void append(final BuildMethodContext context, final StatementConcatenator concatenator);
    
    String asVariable(final BuildMethodContext context);
    
    AbstractTypeHolder getType();
    
    Boolean isNull();

    String getPredefinedVariableName();
}
