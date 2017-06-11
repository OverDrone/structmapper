package org.stuctmapper.expression;

import java.util.List;

import org.stuctmapper.expression.tokens.CompositeIdToken;

public interface ITokenParser {
    CompositeIdToken parse(List<IExpressionToken> tokens);
}
