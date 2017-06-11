package org.stuctmapper.model.mapping;

import org.stuctmapper.model.holder.AbstractTypeHolder;
import org.stuctmapper.model.properties.MultiplierType;
import org.stuctmapper.model.properties.ParticipantKind;

public class ConversionSignatureItem {
    private final AbstractTypeHolder type;
    private final String id;
    private final ParticipantKind kind;
    private final MultiplierType multiplier;
    
    public ConversionSignatureItem(final AbstractTypeHolder type, final String id, final ParticipantKind kind, final MultiplierType multiplier) {
        super();
        this.type = type;
        this.id = id;
        this.kind = kind;
        this.multiplier = multiplier;
    }

    public AbstractTypeHolder getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public ParticipantKind getKind() {
        return kind;
    }
    
    public MultiplierType getMultiplier() {
        return multiplier;
    }
    
    @Override
    public String toString() {
        final Class<?> thisClass = getClass();
        final String className = thisClass.getSimpleName();
        final String result = className + "{type=" + type + ",id=" + id + ",kind=" + kind + "}";
        return result;
    }
}
