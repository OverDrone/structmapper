package org.stuctmapper.model.properties;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.stuctmapper.model.holder.AbstractTypeHolder;
import org.stuctmapper.model.instructions.IConstExpression;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class ConverterInfo {
    private final AbstractTypeHolder type;
    private final Set<AbstractTypeHolder> references;
    private final List<ConverterMethodInfo> methods;
    private final Map<String, IConstExpression> constsMap;
    
    public ConverterInfo(final AbstractTypeHolder type, final Set<AbstractTypeHolder> references, final List<ConverterMethodInfo> methods,
            final Map<String, IConstExpression> constsMap) {
        super();
        this.type = type;
        this.references = ImmutableSet.copyOf(references);
        this.methods = ImmutableList.copyOf(methods);
        this.constsMap = ImmutableMap.copyOf(constsMap);
    }

    public AbstractTypeHolder getType() {
        return type;
    }

    public Set<AbstractTypeHolder> getReferences() {
        return references;
    }

    public List<ConverterMethodInfo> getMethods() {
        return methods;
    }
    
    public Map<String, IConstExpression> getConstsMap() {
        return constsMap;
    }
    
    @Override
    public String toString() {
        final Class<?> thisClass = getClass();
        final String className = thisClass.getSimpleName();
        final String result = className + "{type=" + type + ",methods=" + methods + ",references=" + references + "}";
        return result;
    }
}
