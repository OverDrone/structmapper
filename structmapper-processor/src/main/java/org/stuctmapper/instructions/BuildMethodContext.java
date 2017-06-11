package org.stuctmapper.instructions;

import org.stuctmapper.log.Logger;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;

public class BuildMethodContext {
    private final ICodeBlockCache cache = new CodeBlockCache();
    private final VariableNameGenerator nameGenerator;
    private final MethodSpec.Builder methodBuilder; 
    private final CodeBlock.Builder gettersBuilder;
    private final CodeBlock.Builder settersBuilder;
    private final CodeBlock.Builder superCallBuilder;
    private final CodeBlock.Builder returnBuilder;
    private final CodeBlock.Builder preconditionsBuilder;
    private final Logger logger;
    private final ClassContext classContext;
    
    public BuildMethodContext(final ClassContext classContext, final MethodSpec.Builder methodBuilder, 
            final VariableNameGenerator nameGenerator, final Logger logger) {
        super();
        this.gettersBuilder = CodeBlock.builder();
        this.settersBuilder = CodeBlock.builder();
        this.superCallBuilder = CodeBlock.builder();
        this.returnBuilder = CodeBlock.builder();
        this.preconditionsBuilder = CodeBlock.builder();
        this.methodBuilder = methodBuilder;
        this.nameGenerator = nameGenerator;
        this.classContext = classContext;
        this.logger = logger;
    }
    
    public MethodSpec finish() {
        final CodeBlock preconditions = preconditionsBuilder.build();
        final CodeBlock superCallBlock = superCallBuilder.build();
        final CodeBlock gettersBlock = gettersBuilder.build();
        final CodeBlock settersBlock = settersBuilder.build();
        final CodeBlock returnBlock = returnBuilder.build();
        methodBuilder.addCode(preconditions);
        methodBuilder.addCode(superCallBlock);
        methodBuilder.addCode(gettersBlock);
        methodBuilder.addCode(settersBlock);
        methodBuilder.addCode(returnBlock);
        final MethodSpec method = methodBuilder.build();
        return method;
    }
    
    public ClassContext getClassContext() {
        return classContext;
    }
    
    public CodeBlock.Builder getPreconditionsBuilder() {
        return preconditionsBuilder;
    }
    
    public MethodSpec.Builder getMethodBuilder() {
        return methodBuilder;
    }
    
    public CodeBlock.Builder getGettersBuilder() {
        return gettersBuilder;
    }
    
    public CodeBlock.Builder getSettersBuilder() {
        return settersBuilder;
    }
    
    public CodeBlock.Builder getSuperCallBuilder() {
        return superCallBuilder;
    }
    
    public CodeBlock.Builder getReturnBuilder() {
        return returnBuilder;
    }
    
    public VariableNameGenerator getNameGenerator() {
        return nameGenerator;
    }
    
    public Logger getLogger() {
        return logger;
    }
    
    public ICodeBlockCache getCache() {
        return cache;
    }
}
