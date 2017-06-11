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
import com.squareup.javapoet.TypeName;

public class ReturningMethodCall extends AbstractTargetInitializer {
    
    private final MatchedConversionSignature match;
    private final List<ISourceExpression> sourceExpressions;
    private final List<TargetPathMatchResult> targetMatches;
    private final boolean expectingTargets;
    private ITargetPropertyInitializer[] targetInitializers;

    public ReturningMethodCall(final MatchedConversionSignature match, final List<ISourceExpression> sourceExpressions, 
            final List<TargetPathMatchResult> targetMatches) {
        super(buildType(match));
        final Integer excludedIndex = match.getReturningParameterIndex();
        Preconditions.checkNotNull(excludedIndex);
        this.expectingTargets = PropertyAccessorAggregator.buildAccessors(targetMatches, this::targetVariablesReady, excludedIndex);
        this.match = match;
        this.sourceExpressions = sourceExpressions;
        this.targetMatches = targetMatches;
    }
    
    @Override
    public String getPredefinedVariableName() {
        final Integer index = match.getReturningParameterIndex();
        Preconditions.checkNotNull(index);
        final TargetPathMatchResult returningMatch = targetMatches.get(index);
        Preconditions.checkNotNull(returningMatch);
        final String variableNamePrefix = returningMatch.getVariableNamePrefix();
        Preconditions.checkNotNull(variableNamePrefix);
        return variableNamePrefix;
    }
    
    private void targetVariablesReady(final ITargetPropertyInitializer[] targetInitializers, final BuildMethodContext context) {
        Preconditions.checkArgument(this.targetInitializers == null);
        this.targetInitializers = targetInitializers;
    }
    

    private static AbstractTypeHolder buildType(final MatchedConversionSignature match) {
        final AbstractTypeHolder result = match.getReturnType();
        return result;
    }

    @Override
    public Boolean isNull() {
        final Boolean result = match.isNull();
        return result;
    }
    
    @Override
    protected void appendInline(BuildMethodContext context, StatementConcatenator concatenator) {
        final Integer excludedIndex = match.getReturningParameterIndex();
        final int targetMatchesSize = targetMatches.size();
        final int sourceExpressionsSize = sourceExpressions.size();
        final int size = sourceExpressionsSize + targetMatchesSize;
        if (expectingTargets) {
            Preconditions.checkNotNull(targetInitializers);
            Preconditions.checkArgument(targetInitializers.length == targetMatchesSize);
        }
        final Runnable[] appenders = new Runnable[size];
        for (int i = 0; i < targetMatchesSize; i++) {
            if (excludedIndex != null && i == excludedIndex) {
                continue;
            }
            final ITargetPropertyInitializer targetInitializer = targetInitializers[i];
            final int signatureIndex = match.targetToSignatureIndex(i);
            final Runnable appender = () -> {
                targetInitializer.append(context, concatenator);
            };
            appenders[signatureIndex] = appender;
        }
        for (int i = 0; i < sourceExpressionsSize; i++) {
            final ISourceExpression sourceExpression = sourceExpressions.get(i);
            sourceExpression.prepare(context);
            final Runnable appender = () -> {
                sourceExpression.append(context, concatenator);
            };
            final int signatureIndex = match.sourceToSignatureIndex(i);
            appenders[signatureIndex] = appender;
        }
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
        final Integer signatureExcludedIndex;
        if (excludedIndex != null) {
            signatureExcludedIndex = match.targetToSignatureIndex(excludedIndex);
        } else {
            signatureExcludedIndex = null;
        }
        boolean first = true;
        for (int i = 0; i < size; i++) {
            final Runnable appender = appenders[i];
            if (signatureExcludedIndex != null && i == signatureExcludedIndex) {
                Preconditions.checkArgument(appender == null);
                continue;
            }
            if (first) {
                first = false;
            } else {
                concatenator.append(", ");
            }
            Preconditions.checkNotNull(appender);
            appender.run();
        }
        concatenator.append(")");
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
            final ReturningMethodCall that = (ReturningMethodCall) obj;
            final boolean result = Objects.equals(this.match, that.match);
            return result;
        }
    }
}
