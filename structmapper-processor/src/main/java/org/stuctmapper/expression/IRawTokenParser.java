package org.stuctmapper.expression;

import java.util.List;

public interface IRawTokenParser {
    List<IExpressionToken> parse(String expression);
}
