package org.stuctmapper.model.mapping;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import org.stuctmapper.log.Logger;
import org.stuctmapper.model.holder.AbstractTypeHolder;
import org.stuctmapper.model.properties.ConversionParticipant;
import org.stuctmapper.model.properties.ConverterConstructorMethodInfo;
import org.stuctmapper.model.properties.ConverterMethodInfo;

import com.google.common.base.Preconditions;

public class MatchedConversionSignature {
    private final int[] parameterIndexes;
    private final Integer returningParameterIndex;
    private final AbstractTypeHolder referencedType;
    private final ConverterMethodInfo method;
    private final ConverterConstructorMethodInfo constructor;

    public MatchedConversionSignature(final int[] parameterIndexes, final ConverterConstructorMethodInfo constructor) {
        this(parameterIndexes, 0, null, null, constructor);
    }

    public MatchedConversionSignature(final int[] parameterIndexes, final Integer returningParameterIndex, 
            final AbstractTypeHolder referencedType, final ConverterMethodInfo method) {
        this(parameterIndexes, returningParameterIndex, referencedType, method, null);
    }
    
    public MatchedConversionSignature(final int[] parameterIndexes, final Integer returningParameterIndex, 
            final AbstractTypeHolder referencedType, final ConverterMethodInfo method, final ConverterConstructorMethodInfo constructor) {
        super();
        this.parameterIndexes = parameterIndexes;
        this.returningParameterIndex = returningParameterIndex;
        this.referencedType = referencedType;
        this.method = method;
        this.constructor = constructor;
    }

    public int[] getParameterIndexes() {
        return parameterIndexes;
    }
    
    public Integer getReturningParameterIndex() {
        return returningParameterIndex;
    }
    
    public AbstractTypeHolder getReferencedType() {
        return referencedType;
    }

    public ConverterMethodInfo getMethod() {
        return method;
    }

    public ConverterConstructorMethodInfo getConstructor() {
        return constructor;
    }

    public int sourceToSignatureIndex(final int sourceIndex) {
        final int searchIndex;
        if (constructor != null) {
            searchIndex = sourceIndex + 1;
        } else if (method != null) {
            final Map<String, ConversionParticipant> targets = method.getTargets();
            final int indexOffset = targets.size();
            searchIndex = sourceIndex + indexOffset;
        } else {
            return Logger.fail("no method and constructor");
        }
        final int reverseIndex = reverseSearch(searchIndex);
        return reverseIndex;
    }

    public int targetToSignatureIndex(final int targetIndex) {
        final int searchIndex;
        if (constructor != null) {
            Preconditions.checkArgument(targetIndex == 0);
            searchIndex = targetIndex;
        } else if (method != null) {
            searchIndex = targetIndex;
        } else {
            return Logger.fail("no method and constructor");
        }
        final int reverseIndex = reverseSearch(searchIndex);
        return reverseIndex;
    }
    
    private int reverseSearch(final int searchIndex) {
        for (int i = 0; i < parameterIndexes.length; i++) {
            if (parameterIndexes[i] == searchIndex) {
                return i;
            }
        }
        throw new RuntimeException("Unable to find " + searchIndex + " in " + Arrays.toString(parameterIndexes));
    }

    public boolean isReturning() {
        if (constructor != null) {
            return true;
        } else if (method != null) {
            final boolean result = returningParameterIndex != null;
            return result;
        } else {
            return Logger.fail("no method and constructor");
        }
    }
    
    public Boolean isNull() {
        if (constructor != null) {
            return false;
        } else if (method != null) {
            final ConversionParticipant returningTarget = method.getReturningTarget();
            final AbstractTypeHolder returnType = returningTarget.getType();
            final Boolean result = returnType.isNull();
            return result;
        } else {
            return Logger.fail("no method and constructor");
        }
    }
    
    public AbstractTypeHolder getReturnType() {
        final ConversionParticipant target;
        if (constructor != null) {
            target = constructor.getTarget();
        } else if (method != null) {
            target = method.getReturningTarget();
        } else {
            return Logger.fail("no method and constructor");
        }
        final AbstractTypeHolder result = target.getType();
        return result;
    }
    
    @Override
    public int hashCode() {
        int result = 0;
        if (constructor != null) {
            result += constructor.hashCode();
        }
        if (method != null) {
            result += method.hashCode();
        }
        return result;
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
            final MatchedConversionSignature that = (MatchedConversionSignature) obj;
            if (!Objects.equals(this.referencedType, that.referencedType)) {
                return false;
            } else if (!Objects.equals(this.returningParameterIndex, that.returningParameterIndex)) {
                return false;
            } else if (!Arrays.equals(this.parameterIndexes, that.parameterIndexes)) {
                return false;
            } else if (!Objects.equals(this.method, that.method)) {
                return false;
            } else if (!Objects.equals(this.constructor, that.constructor)) {
                return false;
            } else {
                return true;
            }
        }
    }
    
    @Override
    public String toString() {
        final Class<?> thisClass = getClass();
        final String className = thisClass.getSimpleName();
        final String result = className + "{method=" + method + ",constructor=" + constructor + "}";
        return result;
    }
}
