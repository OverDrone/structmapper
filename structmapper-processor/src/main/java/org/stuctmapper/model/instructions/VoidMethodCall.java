package org.stuctmapper.model.instructions;

import java.util.List;
import java.util.Objects;

import org.stuctmapper.instructions.BuildMethodContext;
import org.stuctmapper.instructions.ClassContext;
import org.stuctmapper.instructions.StatementConcatenator;
import org.stuctmapper.model.holder.AbstractElementHolder;
import org.stuctmapper.model.holder.AbstractTypeHolder;
import org.stuctmapper.model.mapping.MatchedConversionSignature;
import org.stuctmapper.model.properties.ConverterConstructorMethodInfo;
import org.stuctmapper.model.properties.ConverterMethodInfo;

import com.google.common.base.Preconditions;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

public class VoidMethodCall {
    private final MatchedConversionSignature match;
    private final List<ISourceExpression> sourceExpressions;
    private final List<TargetPathMatchResult> targetMatches;
    private boolean readyCalled;
    
    public static void call(final MatchedConversionSignature match, final List<ISourceExpression> sourceExpressions, 
            final List<TargetPathMatchResult> targetMatches) {
        new VoidMethodCall(match, sourceExpressions, targetMatches);
    }

    private VoidMethodCall(final MatchedConversionSignature match, final List<ISourceExpression> sourceExpressions, 
            final List<TargetPathMatchResult> targetMatches) {
        final Integer excludedIndex = match.getReturningParameterIndex();
        Preconditions.checkArgument(excludedIndex == null);
        final boolean expectingTargets = PropertyAccessorAggregator.buildAccessors(targetMatches, this::targetVariablesReady, null);
        Preconditions.checkArgument(expectingTargets);
        this.match = match;
        this.sourceExpressions = sourceExpressions;
        this.targetMatches = targetMatches;
    }
    
    private void targetVariablesReady(final ITargetPropertyInitializer[] initializers, final BuildMethodContext context) {
        Preconditions.checkArgument(!readyCalled);
        readyCalled = true;
        final Integer excludedIndex = match.getReturningParameterIndex();
        final int targetMatchesSize = targetMatches.size();
        final int sourceExpressionsSize = sourceExpressions.size();
        final int size = sourceExpressionsSize + targetMatchesSize; 
        Preconditions.checkNotNull(initializers);
        Preconditions.checkArgument(initializers.length == targetMatchesSize);
        final IAppender[] appenders = new IAppender[size];
        for (int i = 0; i < sourceExpressionsSize; i++) {
            final ISourceExpression sourceExpression = sourceExpressions.get(i);
            sourceExpression.prepare(context);
            final IAppender appender = (concatenator) -> {
                sourceExpression.append(context, concatenator);
            };
            final int signatureIndex = match.sourceToSignatureIndex(i);
            appenders[signatureIndex] = appender;
        }
        for (int i = 0; i < targetMatchesSize; i++) {
            if (excludedIndex != null && i == excludedIndex) {
                continue;
            }
            final ITargetPropertyInitializer initializer = initializers[i];
            final int signatureIndex = match.targetToSignatureIndex(i);
            final IAppender appender = (concatenator) -> {
                initializer.append(context, concatenator);
            };
            appenders[signatureIndex] = appender;
        }
        final StatementConcatenator concatenator = new StatementConcatenator();
        final ConverterConstructorMethodInfo constructor = match.getConstructor();
        final ConverterMethodInfo method = match.getMethod();
        if (constructor != null) {
            final AbstractElementHolder constructorElement = constructor.getElement();
            final AbstractTypeHolder classElement = constructorElement.getEnclosingType();
            final TypeName typeName = classElement.getTypeName();
            concatenator.appendArgs("new $T(", typeName);
        } else if (method != null) {
            final AbstractElementHolder methodElement = method.getElement();
            final AbstractTypeHolder methodTypeElement = methodElement.getEnclosingType();
            final ClassContext classContext = context.getClassContext();
            final AbstractTypeHolder thisType = classContext.getType();
            if (!Objects.equals(methodTypeElement, thisType)) {
                final String reference = classContext.addReference(methodTypeElement);
                concatenator.appendArgs("$N.", reference);
            }
            final String methodName = methodElement.getName();
            concatenator.appendArgs("$N(", methodName);
        } else {
            throw new RuntimeException("Unexpected method == null and constructor == null");
        }
        boolean first = true;
        for (int i = 0; i < size; i++) {
            final IAppender appender = appenders[i];
            if (excludedIndex != null && i == excludedIndex) {
                Preconditions.checkArgument(appender == null);
                continue;
            }
            if (first) {
                first = false;
            } else {
                concatenator.append(", ");
            }
            Preconditions.checkNotNull(appender);
            appender.append(concatenator);
        }
        concatenator.append(")");
        final String template = concatenator.getTemplate();
        final Object[] arguments = concatenator.getArguments();
        final CodeBlock.Builder builder = context.getSettersBuilder();
        builder.addStatement(template, arguments);
    }
    

    @Override
    public int hashCode() {
        return match.hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        } else if (obj == this) {
            return true;
        } else if (obj.getClass() != this.getClass()) {
            return false;
        } else {
            final VoidMethodCall that = (VoidMethodCall) obj;
            final boolean result = Objects.equals(this.match, that.match);
            return result;
        }
    }

    @FunctionalInterface
    private interface IAppender {
        void append(final StatementConcatenator concatenator);
    }
}
