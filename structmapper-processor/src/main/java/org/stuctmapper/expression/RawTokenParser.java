package org.stuctmapper.expression;

import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.stuctmapper.utils.CollectionUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class RawTokenParser implements IMatcherContext, IRawTokenParser {
    private static final Set<MatcherType> INITIAL_MATCHERS = EnumSet.of(MatcherType.ID, MatcherType.WILDCARD, MatcherType.WILDCARD_INDEX_START);
    private final Map<MatcherType, IExpressionMatcher> matchers = buildMatchers();
    private final Deque<Set<MatcherType>> matcherQueue = new LinkedList<>();
    private final Deque<List<IExpressionToken>> tokenQueue = new LinkedList<>();

    @Override
    public void push(final Set<MatcherType> fallbackMatchers) {
        Preconditions.checkArgument(matcherQueue.size() == tokenQueue.size() - 1);
        matcherQueue.push(fallbackMatchers);
        pushTokenQueue();
    }

    private static Map<MatcherType, IExpressionMatcher> buildMatchers() {
        final MatcherType[] values = MatcherType.values();
        final Map<MatcherType, IExpressionMatcher> map = new EnumMap<>(MatcherType.class);
        for (final MatcherType value : values) {
            final Supplier<? extends IExpressionMatcher> constructor = value.getConstructor();
            final IExpressionMatcher matcher = constructor.get();
            CollectionUtils.addNew(map, value, matcher);
        }
        final Map<MatcherType, IExpressionMatcher> result = ImmutableMap.copyOf(map);
        return result;
    }

    @Override
    public <T extends IExpressionToken> List<T> pop() {
        @SuppressWarnings("unchecked")
        final List<T> result = (List<T>) tokenQueue.pop();
        return result;
    }

    @Override
    public List<IExpressionToken> getTokens() {
        final List<IExpressionToken> result = tokenQueue.peek();
        return result;
    }
    
    private void pushTokenQueue() {
        final List<IExpressionToken> tokenList = new ArrayList<>();
        tokenQueue.push(tokenList);
    }
    
    private void reset() {
        matcherQueue.clear();
        tokenQueue.clear();
        for (final IExpressionMatcher matcher : matchers.values()) {
            matcher.reset();
        }
        pushTokenQueue();
    }

    @Override
    public List<IExpressionToken> parse(final String expression) {
        if (expression == null || expression.isEmpty()) {
            return null;
        }
        reset();
        final int size = expression.length();
        IExpressionMatcher previousMatcher = null;
        Set<MatcherType> matcherTypes = INITIAL_MATCHERS;
        int i = 0;
        while (i < size) {
            final char ch = expression.charAt(i);
            i++;
            while (true) {
                boolean matchFound = false;
                for (final MatcherType matcherType : matcherTypes) {
                    final IExpressionMatcher matcher = matchers.get(matcherType);
                    Preconditions.checkNotNull(matcher);
                    if (matcher.matches(ch)) {
                        if (previousMatcher != null && previousMatcher != matcher) {
                            previousMatcher.sequenceEnd(this);
                        }
                        previousMatcher = matcher;
                        matcherTypes = matcher.nextMatchers(this);
                        matchFound = true;
                        break;
                    }
                }
                if (matchFound) {
                    break;
                } else {
                    if (previousMatcher != null) {
                        previousMatcher.sequenceEnd(this);
                        previousMatcher = null;
                    }
                    if (matcherQueue.isEmpty()) {
                        return null;
                    } else {
                        matcherTypes = matcherQueue.pop();
                    }
                }
            }
        }
        if (previousMatcher == null) {
            return null;
        }
        if (!previousMatcher.isTerminal()) {
            return null;
        }
        previousMatcher.sequenceEnd(this);
        final List<IExpressionToken> list = tokenQueue.pop();
        Preconditions.checkArgument(matcherQueue.isEmpty());
        Preconditions.checkArgument(tokenQueue.isEmpty());
        final List<IExpressionToken> result = ImmutableList.copyOf(list);
        return result;
    }
}
