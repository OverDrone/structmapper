package org.stuctmapper.model.instructions;

import java.util.Map;

import org.stuctmapper.model.properties.ConversionParticipant;

import com.google.common.collect.ImmutableMap;

public class TargetBuildResult {
    private final Map<ConversionParticipant, TargetInfo> targetMap;
    private final Map<ConversionParticipant, ISourceExpression> sourceExpressionMap;
    
    public TargetBuildResult(final Map<ConversionParticipant, TargetInfo> targetMap, 
            final Map<ConversionParticipant, ISourceExpression> sourceExpressionMap) {
        super();
        this.targetMap = ImmutableMap.copyOf(targetMap);
        this.sourceExpressionMap = ImmutableMap.copyOf(sourceExpressionMap);
    }
    
    public Map<ConversionParticipant, TargetInfo> getTargetMap() {
        return targetMap;
    }
    
    public Map<ConversionParticipant, ISourceExpression> getSourceExpressionMap() {
        return sourceExpressionMap;
    }
    
}
