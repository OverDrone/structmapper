package org.stuctmapper.processor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

import org.stuctmapper.exceptions.ProcessorException;
import org.stuctmapper.expression.IRawTokenParser;
import org.stuctmapper.expression.ITokenParser;
import org.stuctmapper.expression.RawTokenParser;
import org.stuctmapper.expression.TokenParser;
import org.stuctmapper.instructions.BuildMethodContext;
import org.stuctmapper.instructions.ClassContext;
import org.stuctmapper.instructions.CodeBuilder;
import org.stuctmapper.instructions.ConstParser;
import org.stuctmapper.instructions.ICodeBuilder;
import org.stuctmapper.instructions.IConstParser;
import org.stuctmapper.instructions.ITargetBuilder;
import org.stuctmapper.instructions.TargetBuilder;
import org.stuctmapper.instructions.VariableNameGenerator;
import org.stuctmapper.log.Logger;
import org.stuctmapper.mapping.IMappingBuilder;
import org.stuctmapper.mapping.MappingBuilder;
import org.stuctmapper.model.TypeQualifier;
import org.stuctmapper.model.holder.AbstractElementHolder;
import org.stuctmapper.model.holder.AbstractTypeHolder;
import org.stuctmapper.model.instructions.IConstExpression;
import org.stuctmapper.model.instructions.SuperCallInitializer;
import org.stuctmapper.model.instructions.TargetBuildResult;
import org.stuctmapper.model.instructions.TargetInfo;
import org.stuctmapper.model.mapping.MethodMapping;
import org.stuctmapper.model.properties.ConversionParticipant;
import org.stuctmapper.model.properties.ConverterInfo;
import org.stuctmapper.model.properties.ConverterMethodInfo;
import org.stuctmapper.plugins.GeneratorPlugin;
import org.stuctmapper.plugins.SingletonPlugin;
import org.stuctmapper.plugins.SpringComponentPlugin;
import org.stuctmapper.properties.IPropertiesReader;
import org.stuctmapper.properties.PropertiesReader;
import org.stuctmapper.utils.CollectionUtils;
import org.stuctmapper.utils.JavaBuilderUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class MainBuilder {
    private static final Map<String, Supplier<GeneratorPlugin>> PLUGIN_MAP = ImmutableMap.of(
            Consts.COMPONENTS_SPRING, SpringComponentPlugin::new,
            Consts.COMPONENTS_SINGLETON, SingletonPlugin::new
            );
    private static final Set<Modifier> INHERITED_MODIFIERS = EnumSet.of(Modifier.PUBLIC, Modifier.PROTECTED);
    
    private final IPropertiesReader propertiesReader;
    private final IMappingBuilder mappingBuilder;
    private final ITargetBuilder targetBuilder;
    private final ICodeBuilder codeBuilder;
    private final VariableNameGenerator generator;
    private final Iterable<GeneratorPlugin> plugins;
    private final Logger logger;
    private final ThrowingConsumer<JavaFile> writer;
    private final IRawTokenParser rawTokenParser = new RawTokenParser();
    private final ITokenParser tokenParser = new TokenParser();

    public MainBuilder(final String components, final Logger logger, final ThrowingConsumer<JavaFile> writer) {
        this.logger = logger;
        final Supplier<GeneratorPlugin> supplier = PLUGIN_MAP.get(components);
        Logger.checkNotNull(supplier, "Unexpected " + components);
        final GeneratorPlugin plugin = supplier.get();
        this.plugins = ImmutableList.of(plugin);
        final IConstParser constParser = new ConstParser();
        this.propertiesReader = new PropertiesReader(logger, constParser);
        this.mappingBuilder = new MappingBuilder(tokenParser, rawTokenParser);
        this.targetBuilder = new TargetBuilder(propertiesReader, logger);
        this.generator = new VariableNameGenerator();
        this.codeBuilder = new CodeBuilder(logger);
        this.writer = writer;
    }
    
    public void processType(final AbstractTypeHolder type) throws IOException, ProcessorException {
        final boolean isInterface = type.isInterface();
        final TypeQualifier qualifier = type.getQualifier();
        final Element errorElement = type.getErrorElement();
        final String typeName = qualifier.getName();
        final String implementationName = JavaBuilderUtils.getImplementationName(typeName, isInterface, errorElement);
        final String packageName = qualifier.getPackageName();
        final String prefix = qualifier.getPrefix();
        final String qualifiedTypeName = prefix + typeName;
        final TypeSpec.Builder javaTypeBuilder = TypeSpec.classBuilder(implementationName);
        javaTypeBuilder.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        final ClassName baseTypeName = ClassName.get(packageName, qualifiedTypeName);
        final ClassName implementationTypeName = ClassName.get(packageName, implementationName);
        if (isInterface) {
            javaTypeBuilder.addSuperinterface(baseTypeName);
        } else {
            javaTypeBuilder.superclass(baseTypeName);
        }
        for (final GeneratorPlugin plugin : plugins) {
            plugin.javaType(javaTypeBuilder, baseTypeName, implementationTypeName, type);
        }
        final AbstractTypeHolder registeredInterfaceHolder;
        if (isInterface) {
            registeredInterfaceHolder = type;
        } else {
            registeredInterfaceHolder = null;
        }
        
        final ConverterInfo converterInfo = propertiesReader.getConverter(type);
        final Map<String, IConstExpression> constsMap = converterInfo.getConstsMap();
        final List<ConverterMethodInfo> converterMethods = converterInfo.getMethods();
        final ClassContext classContext = new ClassContext(type);
        final List<MethodSpec> methods = buildMethods(classContext, converterMethods, converterInfo, registeredInterfaceHolder);
        for (final GeneratorPlugin plugin : plugins) {
            plugin.references(javaTypeBuilder, classContext);
        }
        for (final IConstExpression expression : constsMap.values()) {
            final FieldSpec.Builder builder = expression.getAssignment();
            final FieldSpec assignment = builder.build();
            javaTypeBuilder.addField(assignment);
        }
        for (final MethodSpec method : methods) {
            javaTypeBuilder.addMethod(method);
        }
        final TypeSpec javaType = javaTypeBuilder.build();
        final JavaFile.Builder javaFileBuilder = JavaFile.builder(packageName, javaType);
        final JavaFile javaFile = javaFileBuilder.build();
        writer.accept(javaFile);
    }
    
    private List<MethodSpec> buildMethods(final ClassContext classContext,  
            final List<ConverterMethodInfo> list, final ConverterInfo converterInfo, final AbstractTypeHolder typeInterface) {
        final List<MethodSpec> result = new ArrayList<>();
        final Map<String, IConstExpression> constsMap = converterInfo.getConstsMap();
        for (final ConverterMethodInfo info : list) {
            if (!info.isImplementable()) {
                continue;
            }
            final Map<String, TypeName> parameters = buildParameters(info);
            final Set<String> parameterNames = parameters.keySet();
            final AbstractElementHolder element = info.getElement();
            final String methodName = element.getName();
            final MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName);
            final Set<Modifier> elementModifiers = element.getModifiers();
            for (final Modifier inheritedModifier : INHERITED_MODIFIERS) {
                if (elementModifiers.contains(inheritedModifier)) {
                    builder.addModifiers(inheritedModifier);
                }
            }
            builder.addAnnotation(Override.class);
            for (final Entry<String, TypeName> entry : parameters.entrySet()) {
                final String parameterName = entry.getKey();
                final TypeName parameterType = entry.getValue();
                final ParameterSpec.Builder parameterBuilder = ParameterSpec.builder(parameterType, parameterName, Modifier.FINAL);
                final ParameterSpec parameterSpec = parameterBuilder.build();
                builder.addParameter(parameterSpec);
            }
            final BuildMethodContext context = new BuildMethodContext(classContext, builder, generator, logger);
            final MethodMapping mapping = mappingBuilder.build(info, constsMap);
            final TargetBuildResult targetBuildResult = targetBuilder.build(context, mapping, converterInfo);
            
            final Map<Integer, ConversionParticipant> indexMap = info.getIndexMap();
            final ConversionParticipant returnParticipant = indexMap.get(0);
            final boolean isAbstract = elementModifiers.contains(Modifier.ABSTRACT); 
            if (returnParticipant != null) {
                final AbstractTypeHolder returnType = returnParticipant.getType();
                final TypeName returnTypeName = returnType.getTypeName();
                builder.returns(returnTypeName);
                if (!isAbstract) {
                    final SuperCallInitializer superInitializer = new SuperCallInitializer(typeInterface, element, returnType, parameterNames);
                    final Map<ConversionParticipant, TargetInfo> targets = targetBuildResult.getTargetMap();
                    final TargetInfo returnTarget = targets.get(returnParticipant);
                    Preconditions.checkNotNull(returnTarget);
                    returnTarget.registerPropertyInitializer(superInitializer);
                }
            } else if (!isAbstract) {
                final CodeBlock.Builder superCallBuilder = context.getSuperCallBuilder();
                JavaBuilderUtils.buildSuperCall(parameterNames, superCallBuilder);
            }
            codeBuilder.build(context, targetBuildResult);
            final MethodSpec methodSpec = context.finish();
            result.add(methodSpec);
        }
        return result;
    }
    
    private static Map<String, TypeName> buildParameters(final ConverterMethodInfo info) {
        final Map<String, TypeName> map = new LinkedHashMap<>();
        final int maxIndex = info.getMaxIndex();
        final Map<Integer, ConversionParticipant> indexMap = info.getIndexMap();
        for (int i = 1; i <= maxIndex; i++) {
            final ConversionParticipant participant = indexMap.get(i);
            Preconditions.checkNotNull(participant);
            final AbstractTypeHolder participantType = participant.getType();
            final TypeName participantTypeName = participantType.getTypeName();
            final String participantId = participant.getId();
            CollectionUtils.addNew(map, participantId, participantTypeName);
        }
        final Map<String, TypeName> result = ImmutableMap.copyOf(map);
        return result;
    }
}
