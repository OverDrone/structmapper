package org.stuctmapper.model.instructions;

import org.stuctmapper.instructions.BuildMethodContext;
import org.stuctmapper.instructions.StatementConcatenator;
import org.stuctmapper.model.holder.AbstractElementHolder;
import org.stuctmapper.model.holder.AbstractTypeHolder;
import org.stuctmapper.utils.JavaBuilderUtils;

public class SuperCallInitializer extends AbstractTargetInitializer {
    
    private final Iterable<String> parameters;
    private final AbstractElementHolder element;
    private final AbstractTypeHolder typeInterface;

    public SuperCallInitializer(final AbstractTypeHolder typeInterface, final AbstractElementHolder element, 
            final AbstractTypeHolder type, final Iterable<String> parameters) {
        super(type);
        this.typeInterface = typeInterface;
        this.element = element;
        this.parameters = parameters;
    }
    
    @Override
    public String getPredefinedVariableName() {
        return "super";
    }

    @Override
    protected void appendInline(BuildMethodContext context, StatementConcatenator concatenator) {
        final String methodName = element.getName();
        final String interfaceName;
        if (typeInterface != null) {
            interfaceName = typeInterface.getClassSimpleName();
        } else {
            interfaceName = null;
        }
        JavaBuilderUtils.appendSuperCall(concatenator, interfaceName, methodName, parameters);
    }
}
