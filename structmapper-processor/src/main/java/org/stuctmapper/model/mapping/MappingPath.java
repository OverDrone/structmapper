package org.stuctmapper.model.mapping;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.stuctmapper.model.instructions.ISourceExpression;
import org.stuctmapper.model.properties.ConversionParticipant;

import com.google.common.collect.ImmutableList;

public class MappingPath {
    private final ConversionParticipant root;
    private final String id;
    private final List<String> path;
    private final ISourceExpression constExpression;

    public MappingPath(final String id, final ISourceExpression constExpression) {
        this.root = null;
        this.id = id;
        this.path = null;
        this.constExpression = constExpression;
                
    }
    
    public MappingPath(final ConversionParticipant root, final String id, final Collection<String> path) {
        this.root = root;
        this.id = id;
        this.path = ImmutableList.copyOf(path);
        this.constExpression = null;
    }
    
    public ISourceExpression getConstExpression() {
        return constExpression;
    }

    public String getId() {
        return id;
    }
    
    public ConversionParticipant getRoot() {
        return root;
    }
    
    public List<String> getPath() {
        return path;
    }
    
    @Override
    public int hashCode() {
        return path.hashCode() + root.hashCode();
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
            final MappingPath that = (MappingPath) obj;
            if (!Objects.equals(this.constExpression, that.constExpression)) {
                return false;
            } else if (!Objects.equals(this.path, that.path)) {
                return false;
            } else if (!Objects.equals(this.root, that.root)) {
                return false;
            } else {
                return true;
            }
        }
    }
}
