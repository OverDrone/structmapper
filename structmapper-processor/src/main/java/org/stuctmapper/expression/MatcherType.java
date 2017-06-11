package org.stuctmapper.expression;

import java.util.function.Supplier;

import org.stuctmapper.expression.matchers.DotMatcher;
import org.stuctmapper.expression.matchers.IdMatcher;
import org.stuctmapper.expression.matchers.MethodCallCommaMatcher;
import org.stuctmapper.expression.matchers.MethodCallEndMatcher;
import org.stuctmapper.expression.matchers.MethodCallStartMatcher;
import org.stuctmapper.expression.matchers.MultiplierReceiverMatcher;
import org.stuctmapper.expression.matchers.MultiplierSourceMatcher;
import org.stuctmapper.expression.matchers.PlusMatcher;
import org.stuctmapper.expression.matchers.PropertyIndexEndMatcher;
import org.stuctmapper.expression.matchers.PropertyIndexStartMatcher;
import org.stuctmapper.expression.matchers.WildcardIndexEndMatcher;
import org.stuctmapper.expression.matchers.WildcardIndexStartMatcher;
import org.stuctmapper.expression.matchers.WildcardIndexValueMatcher;
import org.stuctmapper.expression.matchers.WildcardMatcher;

public enum MatcherType {
    WILDCARD(WildcardMatcher::new),
    WILDCARD_INDEX_START(WildcardIndexStartMatcher::new),
    WILDCARD_INDEX_VALUE(WildcardIndexValueMatcher::new),
    WILDCARD_INDEX_END(WildcardIndexEndMatcher::new),
    PROPERTY_INDEX_START(PropertyIndexStartMatcher::new),
    PROPERTY_INDEX_END(PropertyIndexEndMatcher::new),
    METHOD_CALL_START(MethodCallStartMatcher::new),
    METHOD_CALL_COMMA(MethodCallCommaMatcher::new),
    METHOD_CALL_END(MethodCallEndMatcher::new),
    DOT(DotMatcher::new),
    MULTIPLIER_RECEIVER(MultiplierReceiverMatcher::new),
    MULTIPLIER_SOURCE(MultiplierSourceMatcher::new),
    PLUS(PlusMatcher::new),
    ID(IdMatcher::new);
    
    private final Supplier<? extends IExpressionMatcher> constructor;

    MatcherType(final Supplier<? extends IExpressionMatcher> constructor) {
        this.constructor = constructor;
    }
    
    public Supplier<? extends IExpressionMatcher> getConstructor() {
        return constructor;
    }
}
