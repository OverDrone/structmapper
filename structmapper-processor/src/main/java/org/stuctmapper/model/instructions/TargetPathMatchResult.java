package org.stuctmapper.model.instructions;

public class TargetPathMatchResult {
    private final String variableNamePrefix;
    private final TargetInfo info;
    private final TargetInfo parent;
    
    public TargetPathMatchResult(final String variableNamePrefix, final TargetInfo info, final TargetInfo parent) {
        super();
        this.variableNamePrefix = variableNamePrefix;
        this.info = info;
        this.parent = parent;
    }

    public String getVariableNamePrefix() {
        return variableNamePrefix;
    }
    
    public TargetInfo getInfo() {
        return info;
    }

    public TargetInfo getParent() {
        return parent;
    }
}