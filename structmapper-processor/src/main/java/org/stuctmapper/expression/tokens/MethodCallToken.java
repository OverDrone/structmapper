package org.stuctmapper.expression.tokens;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Вызов метода. Содержит композитный идентификатор и список аргументов
 */
public class MethodCallToken extends CompositeIdToken implements IComplexToken {
    private final List<CompositeIdToken> arguments;

    public MethodCallToken(final List<ICompositeIdTokenItem> parts, final List<CompositeIdToken> arguments) {
        super(parts);
        this.arguments = ImmutableList.copyOf(arguments);
    }
    
    public List<CompositeIdToken> getArguments() {
        return arguments;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{parts=" + getParts() + ",arguments=" + arguments + "}";
    }
}
