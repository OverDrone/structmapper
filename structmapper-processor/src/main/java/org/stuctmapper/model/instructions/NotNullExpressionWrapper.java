package org.stuctmapper.model.instructions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.stuctmapper.instructions.BuildMethodContext;
import org.stuctmapper.instructions.ICodeBlockCache;
import org.stuctmapper.instructions.StatementConcatenator;
import org.stuctmapper.instructions.VariableNameGenerator;
import org.stuctmapper.model.holder.AbstractTypeHolder;
import org.stuctmapper.properties.PropertiesReader;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

public class NotNullExpressionWrapper implements ISourceExpression {
    private List<ISourceExpression> accessors = new ArrayList<>();
    private final ISourceExpression expression;
    private boolean prepared;
    
    public static NotNullExpressionWrapper get(final ISourceExpression expression, final ICodeBlockCache cache) {
        final NotNullExpressionWrapper item = new NotNullExpressionWrapper(expression);
        final NotNullExpressionWrapper registered = cache.add(item);
        if (registered == item) {
            expression.addAppendReference();
            expression.appendPrepareChain(item);
        }
        return registered;
    }
    
    private NotNullExpressionWrapper(final ISourceExpression expression) {
        this.expression = expression;
    }
    
    @Override
    public String getPredefinedVariableName() {
        final String expressionName = expression.getPredefinedVariableName();
        final String pascalName = PropertiesReader.camelToPascal(expressionName);
        final String result = "notNull" + pascalName;
        return result;
    }
    
    @Override
    public AbstractTypeHolder getType() {
        return expression.getType();
    }
    
    @Override
    public void addAppendReference(int count) {
        expression.addAppendReference(count);
    }
    

    @Override
    public void appendPrepareChain(ISourceExpression expression) {
        accessors.add(expression);
        addAppendReference();
    }
    
    @Override
    public void prepare(BuildMethodContext context, String variableName) {
        if (!prepared) {
            prepared = true;
            final VariableNameGenerator generator = context.getNameGenerator();
            final CodeBlock.Builder builder = context.getGettersBuilder();
            final int accessorsSize = accessors.size();
            final List<String> variables = new ArrayList<>(accessorsSize);
            for (final ISourceExpression accessor : accessors) {
                final String variableNamePrefix = accessor.getPredefinedVariableName();
                final String accessorVariableName = generator.getNext(variableNamePrefix);
                final AbstractTypeHolder accessorType = accessor.getType();
                final StatementConcatenator concatenator = new StatementConcatenator();
                final TypeName typeName = accessorType.getTypeName();
                concatenator.appendArgs("final $T $N", typeName, accessorVariableName);
                final String template = concatenator.getTemplate();
                final Object[] arguments = concatenator.getArguments();
                builder.addStatement(template, arguments);
                variables.add(accessorVariableName);
            }
            final StatementConcatenator concatenator = new StatementConcatenator();
            concatenator.append("if (");
            expression.append(context, concatenator);
            concatenator.append(" != null)");
            final String template = concatenator.getTemplate();
            final Object[] arguments = concatenator.getArguments();
            builder.beginControlFlow(template, arguments);
            for (int i = 0; i < accessorsSize; i++) {
                final ISourceExpression accessor = accessors.get(i);
                final String accessorVariableName = variables.get(i);
                accessor.prepare(context, accessorVariableName);
            }
            builder.nextControlFlow("else");
            for (final String accessorVariableName : variables) {
                builder.addStatement("$N = null", accessorVariableName);
            }
            builder.endControlFlow();
        }
    }
    
    @Override
    public void append(BuildMethodContext context, StatementConcatenator concatenator) {
        expression.append(context, concatenator);
    }
    
    @Override
    public String asVariable(BuildMethodContext context) {
        final String result = expression.asVariable(context);
        return result;
    }
    
    @Override
    public Boolean isNull() {
        final Boolean result = expression.isNull();
        return result;
    }
    
    @Override
    public int hashCode() {
        return expression.hashCode();
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
            final NotNullExpressionWrapper that = (NotNullExpressionWrapper) obj;
            final boolean result = Objects.equals(this.expression, that.expression);
            return result;
        }
    }
}
