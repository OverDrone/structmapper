package org.stuctmapper.expression;

import java.util.ArrayList;
import java.util.List;

import org.stuctmapper.expression.rawtokens.RawDotToken;
import org.stuctmapper.expression.rawtokens.RawIdToken;
import org.stuctmapper.expression.rawtokens.RawMethodArgumentToken;
import org.stuctmapper.expression.rawtokens.RawMethodArgumentsToken;
import org.stuctmapper.expression.rawtokens.RawMultiplierReceiverToken;
import org.stuctmapper.expression.rawtokens.RawMultiplierSourceToken;
import org.stuctmapper.expression.rawtokens.RawPlusToken;
import org.stuctmapper.expression.rawtokens.RawPropertyIndexToken;
import org.stuctmapper.expression.rawtokens.RawWildcardIndexToken;
import org.stuctmapper.expression.rawtokens.RawWildcardToken;
import org.stuctmapper.expression.tokens.CompositeIdToken;
import org.stuctmapper.expression.tokens.IComplexToken;
import org.stuctmapper.expression.tokens.ICompositeIdTokenItem;
import org.stuctmapper.expression.tokens.IWildcardIdTokenItem;
import org.stuctmapper.expression.tokens.IWildcardIndexedIdTokenItem;
import org.stuctmapper.expression.tokens.IndexedPropertyToken;
import org.stuctmapper.expression.tokens.MethodCallToken;
import org.stuctmapper.expression.tokens.MultiplierReceiverToken;
import org.stuctmapper.expression.tokens.MultiplierSourceToken;
import org.stuctmapper.expression.tokens.PlusToken;
import org.stuctmapper.expression.tokens.SimpleIdToken;
import org.stuctmapper.expression.tokens.TerminalDotToken;
import org.stuctmapper.expression.tokens.WildcardIdToken;
import org.stuctmapper.expression.tokens.WildcardIndexToken;
import org.stuctmapper.expression.tokens.WildcardIndexedIdToken;
import org.stuctmapper.expression.tokens.WildcardPlaceholderToken;

import com.google.common.base.Preconditions;

public class TokenParser implements ITokenParser {
    @Override
    public CompositeIdToken parse(final List<IExpressionToken> tokens) {
        final CompositeIdToken result = parse(tokens, 0);
        return result;
    }
    
    private CompositeIdToken parse(final List<IExpressionToken> tokens, final int startIndex) {
        if (tokens.isEmpty()) {
            return null;
        }
        final List<ICompositeIdTokenItem> items = new ArrayList<>();
        int index = startIndex;
        final int tokensSize = tokens.size();
        while (index < tokensSize) {
            final int compositeIdIndex = matchCompositeId(tokens, index, items);
            if (compositeIdIndex == index) {
                return null;
            } else if (compositeIdIndex == tokensSize) {
                break;
            }
            final int complexIndex = matchComplex(tokens, compositeIdIndex, items);
            if (complexIndex == compositeIdIndex) {
                return null;
            } else if (complexIndex == tokensSize) {
                break;
            }
            final int newDotIndex = matchDot(tokens, complexIndex);
            if (newDotIndex == complexIndex) {
                return null;
            } else if (newDotIndex == tokensSize) {
                final TerminalDotToken dot = new TerminalDotToken();
                items.add(dot);
            }
            index = newDotIndex;
        }
        if (items.size() == 1) {
            final ICompositeIdTokenItem item = items.get(0);
            if (item instanceof CompositeIdToken) {
                final CompositeIdToken result = (CompositeIdToken) item;
                return result;
            }
        }
        final CompositeIdToken result = new CompositeIdToken(items);
        return result;
    }
    
    private int matchComplex(final List<IExpressionToken> tokens, final int startIndex, final List<ICompositeIdTokenItem> result) {
        if (startIndex < tokens.size()) {
            final IExpressionToken token = tokens.get(startIndex);
            IComplexToken complex = null;
            if (complex == null) {
                complex = matchIndexed(token, result);
            }
            if (complex == null) {
                complex = matchMethodCall(token, result);
            }
            if (complex != null) {
                result.clear();
                result.add(complex);
                return startIndex + 1;
            }
        }
        return startIndex;
    }

    private IComplexToken matchMethodCall(final IExpressionToken token, final List<ICompositeIdTokenItem> parts) {
        if (!(token instanceof RawMethodArgumentsToken)) {
            return null;
        }
        final RawMethodArgumentsToken methodArgumentsToken = (RawMethodArgumentsToken) token;
        final List<RawMethodArgumentToken> childTokens = methodArgumentsToken.getTokens();
        final List<CompositeIdToken> arguments = new ArrayList<>(childTokens.size());
        for (final RawMethodArgumentToken childToken : childTokens) {
            final List<IExpressionToken> argumentTokens = childToken.getTokens();
            final CompositeIdToken argument = parse(argumentTokens);
            if (argument == null) {
                return null;
            }
            arguments.add(argument);
        }
        final MethodCallToken result = new MethodCallToken(parts, arguments);
        return result;
    }

    private IComplexToken matchIndexed(final IExpressionToken token, final List<ICompositeIdTokenItem> parts) {
        if (!(token instanceof RawPropertyIndexToken)) {
            return null;
        }
        final RawPropertyIndexToken propertyIndexToken = (RawPropertyIndexToken) token;
        final List<IExpressionToken> childTokens = propertyIndexToken.getTokens();
        final CompositeIdToken index = parse(childTokens);
        if (index == null) {
            return null;
        }
        final IndexedPropertyToken result = new IndexedPropertyToken(parts, index);
        return result;
    }

    private static int matchCompositeId(final List<IExpressionToken> tokens, final int startIndex, final List<ICompositeIdTokenItem> result) {
        int index = startIndex;
        final int tokensSize = tokens.size();
        while (index < tokensSize) {
            final int newSingleIndex = matchSingle(tokens, index, result);
            if (newSingleIndex == index || newSingleIndex == tokensSize) {
                return newSingleIndex;
            }
            final int newDotIndex = matchDot(tokens, newSingleIndex);
            if (newDotIndex == newSingleIndex) {
                return newSingleIndex;
            } else if (newDotIndex == tokensSize) {
                final TerminalDotToken dot = new TerminalDotToken();
                result.add(dot);
            }
            index = newDotIndex;
        }
        return index;
    }
    
    private static int matchDot(final List<IExpressionToken> tokens, final int startIndex) {
        if (startIndex < tokens.size()) {
            final IExpressionToken token = tokens.get(startIndex);
            if (token instanceof RawDotToken) {
                return startIndex + 1;
            }
        }
        return startIndex;
    }
    
    private static int matchSingle(final List<IExpressionToken> tokens, final int startIndex, final List<ICompositeIdTokenItem> result) {
        int index = startIndex;
        index = matchWildcard(tokens, index, result);
        if (index > startIndex) {
            return index;
        }
        index = matchWildcardIndex(tokens, index, result);
        if (index > startIndex) {
            return index;
        }
        index = matchSimple(tokens, index, result);
        return index;
    }
    
    
    private static int matchSimple(final List<IExpressionToken> tokens, final int startIndex, final List<ICompositeIdTokenItem> result) {
        if (startIndex < tokens.size()) {
            final IExpressionToken token = tokens.get(startIndex);
            if (token instanceof RawIdToken) {
                final RawIdToken idToken = (RawIdToken) token;
                final String id = idToken.getId();
                final SimpleIdToken simpleId = new SimpleIdToken(id);
                result.add(simpleId);
                return startIndex + 1;
            } else if (token instanceof RawPlusToken) {
                final PlusToken plus = new PlusToken();
                result.add(plus);
                return startIndex + 1;
            } else if (token instanceof RawMultiplierReceiverToken) {
                final MultiplierReceiverToken multiplierReceiver = new MultiplierReceiverToken();
                result.add(multiplierReceiver);
                return startIndex + 1;
            } else if (token instanceof RawMultiplierSourceToken) {
                final MultiplierSourceToken multiplierSource = new MultiplierSourceToken();
                result.add(multiplierSource);
                return startIndex + 1;
            }
        }
        return startIndex;
    }

    private static int matchWildcard(final List<IExpressionToken> tokens, final int startIndex, final List<ICompositeIdTokenItem> result) {
        int index = startIndex;
        boolean hasWildcard = false;
        while (index < tokens.size()) {
            final IExpressionToken token = tokens.get(index);
            if (token instanceof RawIdToken) {
                index++;
            } else if (token instanceof RawWildcardToken) {
                hasWildcard = true;
                index++;
            } else {
                break;
            }
        }
        if (!hasWildcard) {
            return startIndex;
        }
        final int length = index - startIndex;
        if (length > 0) {
            final List<IWildcardIdTokenItem> wildcardIdItems = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                final IExpressionToken token = tokens.get(i + startIndex);
                if (token instanceof RawIdToken) {
                    final RawIdToken idToken = (RawIdToken) token;
                    final String id = idToken.getId();
                    final SimpleIdToken simpleId = new SimpleIdToken(id);
                    wildcardIdItems.add(simpleId);
                } else if (token instanceof RawWildcardToken) {
                    final WildcardPlaceholderToken wildcardPlaceholder = new WildcardPlaceholderToken();
                    wildcardIdItems.add(wildcardPlaceholder);
                } else {
                    Preconditions.checkArgument(false);
                }
            }
            final WildcardIdToken wildcardId = new WildcardIdToken(wildcardIdItems);
            result.add(wildcardId);
        }
        return index;
    }

    private static int matchWildcardIndex(final List<IExpressionToken> tokens, final int startIndex, final List<ICompositeIdTokenItem> result) {
        int index = startIndex;
        boolean hasWildcardIndex = false;
        while (index < tokens.size()) {
            final IExpressionToken token = tokens.get(index);
            if (token instanceof RawIdToken) {
                index++;
            } else if (token instanceof RawWildcardIndexToken) {
                index++;
                hasWildcardIndex = true;
            } else {
                break;
            }
        }
        if (!hasWildcardIndex) {
            return startIndex;
        }
        final int length = index - startIndex;
        if (length > 0) {
            final List<IWildcardIndexedIdTokenItem> wildcardItems = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                final IExpressionToken token = tokens.get(i + startIndex);
                if (token instanceof RawIdToken) {
                    final RawIdToken idToken = (RawIdToken) token;
                    final String id = idToken.getId();
                    final SimpleIdToken simpleId = new SimpleIdToken(id);
                    wildcardItems.add(simpleId);
                } else if (token instanceof RawWildcardIndexToken) {
                    final RawWildcardIndexToken wildcardIndexToken = (RawWildcardIndexToken) token;
                    final int wildcardTokenIndex = wildcardIndexToken.getIndex();
                    final WildcardIndexToken wildcardIndex = new WildcardIndexToken(wildcardTokenIndex);
                    wildcardItems.add(wildcardIndex);
                } else {
                    Preconditions.checkArgument(false);
                }
            }
            final WildcardIndexedIdToken wildcardIndexedId = new WildcardIndexedIdToken(wildcardItems);
            result.add(wildcardIndexedId);
        }
        return index;
    }
}
