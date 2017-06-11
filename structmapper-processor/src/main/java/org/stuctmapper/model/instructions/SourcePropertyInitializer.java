package org.stuctmapper.model.instructions;

import java.util.Objects;

import org.stuctmapper.instructions.BuildMethodContext;
import org.stuctmapper.instructions.ICodeBlockCache;
import org.stuctmapper.instructions.StatementConcatenator;
import org.stuctmapper.log.Logger;
import org.stuctmapper.model.holder.AbstractElementHolder;
import org.stuctmapper.model.holder.AbstractTypeHolder;
import org.stuctmapper.model.properties.Property;

public class SourcePropertyInitializer extends AbstractSourceExpression {
    private final Property property;
    private final ISourceExpression wrapper;
    
    public static ISourceExpression get(final Property property, final ISourceExpression parent, final ICodeBlockCache cache) {
        SourcePropertyInitializer item = new SourcePropertyInitializer(property, parent);
        final ISourceExpression registered = cache.add(item);
        if (registered == item) {
            parent.appendPrepareChain(item);
        }
        return registered;
    }
    
    private SourcePropertyInitializer(final Property property, final ISourceExpression wrapper) {
        super(getPropertyType(property));
        this.property = property;
        this.wrapper = wrapper;
    }

    @Override
    public String getPredefinedVariableName() {
        final String result = property.getName();
        return result;
    }
    
    private static AbstractTypeHolder getPropertyType(final Property property) {
        final AbstractTypeHolder result = property.getType();
        return result;
    }
    
    @Override
    protected void appendInline(final BuildMethodContext context, final StatementConcatenator concatenator) {
        final AbstractElementHolder getter = property.getGetter();
        final AbstractElementHolder field = property.getField();
        wrapper.append(context, concatenator);
        if (getter != null) {
            final String getterName = getter.getName();
            concatenator.appendArgs(".$N()", getterName);
        } else if (field != null) {
            final String fieldName = field.getName();
            concatenator.appendArgs(".$N", fieldName);
        } else {
            Logger.fail("property " + property + " is not readable");
        }
    }

    @Override
    public int hashCode() {
        final int result = property.hashCode();
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
            final SourcePropertyInitializer that = (SourcePropertyInitializer) obj;
            final boolean result = Objects.equals(this.property, that.property);
            return result;
        }
    }
    
}
