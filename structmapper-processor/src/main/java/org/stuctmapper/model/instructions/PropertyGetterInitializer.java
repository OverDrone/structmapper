package org.stuctmapper.model.instructions;

import java.util.Objects;

import org.stuctmapper.instructions.BuildMethodContext;
import org.stuctmapper.instructions.ICodeBlockCache;
import org.stuctmapper.instructions.StatementConcatenator;
import org.stuctmapper.log.Logger;
import org.stuctmapper.model.holder.AbstractElementHolder;
import org.stuctmapper.model.holder.AbstractTypeHolder;
import org.stuctmapper.model.properties.Property;

import com.google.common.base.Preconditions;
import com.squareup.javapoet.CodeBlock;

public class PropertyGetterInitializer extends AbstractTargetInitializer implements ITargetPropertyAccessor {
    private final Property property;
    private ITargetPropertyInitializer parentInitializer;
    private final String predefinedVariableName;

    public static PropertyGetterInitializer add(final String predefinedVariableName, final Property property, final ICodeBlockCache cache, 
            final TargetInfo info, final TargetInfo parentInfo) {
        PropertyGetterInitializer newValue = new PropertyGetterInitializer(predefinedVariableName, property);
        final PropertyGetterInitializer registered = cache.add(newValue);
        if (registered == newValue) {
            parentInfo.registerPropertyAccessor(newValue);
            info.setPropertyInitializer(newValue);
        }
        return registered;
    }
    
    private PropertyGetterInitializer(final String predefinedVariableName, final Property property) { 
        super(buildType(property));
        this.property = property;
        this.predefinedVariableName = predefinedVariableName;
    }
    
    @Override
    public String getPredefinedVariableName() {
        return predefinedVariableName;
    }
    
    private static AbstractTypeHolder buildType(final Property property) {
        final AbstractTypeHolder result = property.getType();
        return result;
    }
    
    @Override
    protected void appendInline(final BuildMethodContext context, final StatementConcatenator concatenator) {
        Preconditions.checkNotNull(parentInitializer);
        parentInitializer.append(context, concatenator);
        concatenator.append(".");
        final AbstractElementHolder field = property.getField();
        final AbstractElementHolder getter = property.getGetter();
        if (getter != null) {
            final String getterName = getter.getName();
            concatenator.appendArgs("$N()", getterName);
        } else if (field != null) {
            final String fieldName = field.getName();
            concatenator.appendArgs("$N", fieldName);
        } else {
            Logger.fail("property " + property + " is not readable");
        }
        final String template = concatenator.getTemplate();
        final Object[] arguments = concatenator.getArguments();
        final CodeBlock.Builder builder = getVariableBuilder(context);
        builder.addStatement(template, arguments);
    }

    @Override
    public void access(final BuildMethodContext context, final ITargetPropertyInitializer parentInitializer, final TargetPropertyAccessorStage stage) {
        this.parentInitializer = parentInitializer;
        
        switch (stage) {
        case DECLARE:
            generateVariableName(context, false);
            applyVariableDeclaration(context);
            break;
            
        case IF_NOT_NULL:
            prepare(context);
            break;
            
        case IF_NULL:
            final String variableName = getVariableName();
            final CodeBlock.Builder builder = getVariableBuilder(context);
            builder.addStatement("$N = null", variableName);
            break;

        case NOT_NULL:
            prepare(context);
            break;
            
        default:
            break;
        }
    }
    
    @Override
    public boolean isFake() {
        return true;
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
            final PropertyGetterInitializer that = (PropertyGetterInitializer) obj;
            final boolean result = Objects.equals(this.property, that.property);
            return result;
        }
    }
}
