package org.stuctmapper.expression.tokens;

/**
 * Наличие данного объекта в списке говорит о том, что в составном идентификаторе в конце ".", после чего ничего не указано 
 */
public class TerminalDotToken implements ICompositeIdTokenItem {
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
