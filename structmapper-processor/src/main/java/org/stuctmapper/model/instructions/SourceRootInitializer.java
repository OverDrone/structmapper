package org.stuctmapper.model.instructions;

import java.util.Objects;

import org.stuctmapper.instructions.BuildMethodContext;
import org.stuctmapper.instructions.StatementConcatenator;
import org.stuctmapper.model.holder.AbstractTypeHolder;
import org.stuctmapper.model.properties.ConversionParticipant;

import com.google.common.base.Preconditions;

public class SourceRootInitializer extends AbstractSourceExpression {
    private String variableName;

    public SourceRootInitializer(final ConversionParticipant root) {
        super(getType(root));
        variableName = root.getId();
    }

    private static AbstractTypeHolder getType(final ConversionParticipant root) {
        final AbstractTypeHolder result = root.getType();
        return result;
    }

    @Override
    protected void appendInline(final BuildMethodContext context, final StatementConcatenator concatenator) {
        concatenator.appendArgs("$N", variableName);
    }
    
    @Override
    protected String generateVariableStatement(BuildMethodContext context, boolean variableDeclared) {
        Preconditions.checkArgument(!variableDeclared);
        return variableName;
    }
    
    @Override
    public int hashCode() {
        return variableName.hashCode();
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
            final SourceRootInitializer that = (SourceRootInitializer) obj;
            final boolean result = Objects.equals(this.variableName, that.variableName);
            return result;
        }
    }

}
