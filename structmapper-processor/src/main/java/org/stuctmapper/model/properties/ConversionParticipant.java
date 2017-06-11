package org.stuctmapper.model.properties;

import org.stuctmapper.model.holder.AbstractTypeHolder;

public final class ConversionParticipant {
    final int index;
    private final String id;
    private final AbstractTypeHolder type;
    private final ParticipantKind kind;
    private final boolean returning;
    
    public ConversionParticipant(final int index, final String id, final AbstractTypeHolder type, final ParticipantKind kind, final boolean returning) {
        super();
        this.index = index;
        this.id = id;
        this.type = type;
        this.kind = kind;
        this.returning = returning;
    }
    
    public boolean isReturning() {
        return returning;
    }

    public int getIndex() {
        return index;
    }
    
    public String getId() {
        return id;
    }
    
    public AbstractTypeHolder getType() {
        return type;
    }
    
    public ParticipantKind getKind() {
        return kind;
    }
    
    @Override
    public int hashCode() {
        return index;
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
            final ConversionParticipant that = (ConversionParticipant) obj;
            return this.index == that.index;
        }
    }
}