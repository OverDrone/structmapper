package org.stuctmapper.instructions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.stuctmapper.log.Logger;
import org.stuctmapper.model.holder.AbstractTypeHolder;
import org.stuctmapper.model.instructions.DefaultConstructorInitializer;
import org.stuctmapper.model.instructions.ISourceExpression;
import org.stuctmapper.model.instructions.ITargetPropertyAccessor;
import org.stuctmapper.model.instructions.ITargetPropertyInitializer;
import org.stuctmapper.model.instructions.ImmutableConstructorInitializer;
import org.stuctmapper.model.instructions.InitializerAssignment;
import org.stuctmapper.model.instructions.NullPropertyInitializer;
import org.stuctmapper.model.instructions.PropertyGetterInitializer;
import org.stuctmapper.model.instructions.ReturnStatement;
import org.stuctmapper.model.instructions.TargetBuildResult;
import org.stuctmapper.model.instructions.TargetInfo;
import org.stuctmapper.model.instructions.TargetPropertyAccessorStage;
import org.stuctmapper.model.properties.ConstructorType;
import org.stuctmapper.model.properties.ConversionParticipant;
import org.stuctmapper.model.properties.ParsedType;
import org.stuctmapper.model.properties.Property;

import com.google.common.base.Preconditions;
import com.squareup.javapoet.CodeBlock;

public class CodeBuilder extends LoggableService implements ICodeBuilder {

    public CodeBuilder(final Logger logger) {
        super(logger);
    }
    
    @Override
    public void build(final BuildMethodContext context, final TargetBuildResult targetBuildResult) {
        final Map<ConversionParticipant, TargetInfo> targetMap = targetBuildResult.getTargetMap();
        final Map<ConversionParticipant, ISourceExpression> sourceMap = targetBuildResult.getSourceExpressionMap();
        callRootSources(sourceMap, context);
        final TargetInfo returningTarget = getReturningTarget(targetMap);
        final Collection<TargetInfo> targetValues = targetMap.values();
        processInitializers(targetMap, returningTarget, context);
        initializersToAccessors(context, targetValues, null);
        addRootNotNull(context, sourceMap, targetMap);
        addReturningTarget(context, returningTarget);
        callInitializersAccessors(context, targetValues);
    }
    
    private void addRootNotNull(final BuildMethodContext context, final Map<ConversionParticipant, ISourceExpression> sourceMap,
            final Map<ConversionParticipant, TargetInfo> targetMap) {
        final Set<ConversionParticipant> sources = sourceMap.keySet();
        final Set<ConversionParticipant> targets = targetMap.keySet();
        final Set<ConversionParticipant> participants = new LinkedHashSet<>();
        participants.addAll(sources);
        participants.addAll(targets);
        final ConversionParticipant returningParticipant = getReturningParticipant(targets);
        final boolean isReturning = returningParticipant != null;
        if (isReturning) {
            participants.remove(returningParticipant);
        }
        final StatementConcatenator concatenator = new StatementConcatenator();
        concatenator.append("if (");
        boolean first = true;
        for (final ConversionParticipant participant : participants) {
            final String id = participant.getId();
            if (first) {
                first = false;
            } else {
                concatenator.append(" || ");
            }
            concatenator.appendArgs("$N == null", id);
        }
        concatenator.append(")");
        final String template = concatenator.getTemplate();
        final Object[] arguments = concatenator.getArguments();
        final CodeBlock.Builder builder = context.getPreconditionsBuilder();
        builder.beginControlFlow(template, arguments);
        if (isReturning) {
            builder.addStatement("return null");
        } else { 
            builder.addStatement("return");
        }
        builder.endControlFlow();
    }
    
    private static ConversionParticipant getReturningParticipant(final Set<ConversionParticipant> set) {
        for (final ConversionParticipant participant : set) {
            if (participant.isReturning()) {
                return participant;
            }
        }
        return null;
    }

    private void initializersToAccessors(final BuildMethodContext context, final Collection<TargetInfo> list, final TargetInfo parent) {
        for (final TargetInfo info : list) {
            initializersToAccessors(context, info, parent);
        }
    }

    private void initializersToAccessors(final BuildMethodContext context, final TargetInfo info, final TargetInfo parent) {
        final Map<String, TargetInfo> childMap = info.getMap();
        final Collection<TargetInfo> childValues = childMap.values();
        initializersToAccessors(context, childValues, info);
        
        if (parent != null && !parent.hasConstructor(ConstructorType.IMMUTABLE)) {
            final ITargetPropertyInitializer initializer = info.getInitializer();
            if (initializer != null && !initializer.isFake()) {
                final Property parentProperty = info.getParentProperty();
                Preconditions.checkNotNull(parentProperty);
                final InitializerAssignment assignment = new InitializerAssignment(initializer, parentProperty);
                parent.registerPropertyAccessor(assignment);
            }
        }
    }

    private void callRootSources(final Map<ConversionParticipant, ISourceExpression> map, final BuildMethodContext context) {
        final Collection<ISourceExpression> values = map.values();
        for (final ISourceExpression expression : values) {
            expression.prepare(context);
        }
    }

    private static TargetInfo getReturningTarget(final Map<ConversionParticipant, TargetInfo> targets) {
        TargetInfo result = null;
        for (final Entry<ConversionParticipant, TargetInfo> entry : targets.entrySet()) {
            final ConversionParticipant key = entry.getKey();
            final TargetInfo value = entry.getValue();
            if (key.isReturning()) {
                Preconditions.checkArgument(result == null);
                result = value;
            }
        }
        return result;
    }

    private void addReturningTarget(final BuildMethodContext context, final TargetInfo returningTarget) {
        if (returningTarget != null) {
            final ReturnStatement returnAccessor = new ReturnStatement(); 
            returningTarget.registerPropertyAccessor(returnAccessor);
        }
    }

    private void callInitializersAccessors(final BuildMethodContext context, final Collection<TargetInfo> values) {
        for (final TargetInfo info : values) {
            callInitializersAccessors(context, info);
        }
    }

    private void callInitializersAccessors(final BuildMethodContext context, final TargetInfo info) {
        final Map<String, TargetInfo> childMap = info.getMap();
        final Collection<TargetInfo> childValues = childMap.values();
        callInitializersAccessors(context, childValues);
        
        final Set<ITargetPropertyAccessor> accessors = info.getAccessors();
        if (accessors.isEmpty()) {
            return;
        }
        final ITargetPropertyInitializer initializer = info.getInitializer();
        Preconditions.checkNotNull(initializer, "accessors=" + accessors);
        final int accessorsSize = accessors.size();
        final Boolean isNull = initializer.isNull();
        initializer.addReference(accessorsSize);
        if (isNull == null) {
            initializer.addReference();
        }
        initializer.prepare(context);
        callAccessors(context, initializer, info, isNull);
    }

    private void callAccessors(final BuildMethodContext context, final ITargetPropertyInitializer initializer, final TargetInfo info,  final Boolean isNull) {
        if (isNull == Boolean.TRUE) {
            callAccessors(context, initializer, info, TargetPropertyAccessorStage.DECLARE);
            callAccessors(context, initializer, info, TargetPropertyAccessorStage.IF_NULL);
            callAccessors(context, initializer, info, TargetPropertyAccessorStage.FINISH);
        } else if (isNull == Boolean.FALSE) {
            callAccessors(context, initializer, info, TargetPropertyAccessorStage.NOT_NULL);
            callAccessors(context, initializer, info, TargetPropertyAccessorStage.FINISH);
        } else {
            final CodeBlock.Builder builder = context.getSettersBuilder();
            callAccessors(context, initializer, info, TargetPropertyAccessorStage.DECLARE);
            final StatementConcatenator concatenator = new StatementConcatenator();
            concatenator.append("if (");
            initializer.append(context, concatenator);
            concatenator.append(" != null)");
            final String template = concatenator.getTemplate();
            final Object[] arguments = concatenator.getArguments();
            builder.beginControlFlow(template, arguments);
            callAccessors(context, initializer, info, TargetPropertyAccessorStage.IF_NOT_NULL);
            builder.nextControlFlow("else");
            callAccessors(context, initializer, info, TargetPropertyAccessorStage.IF_NULL);
            builder.endControlFlow();
        }
    }
    
    private void callAccessors(final BuildMethodContext context, final ITargetPropertyInitializer initializer, 
            final TargetInfo info, final TargetPropertyAccessorStage stage) {
        final Set<ITargetPropertyAccessor> accessors = info.getAccessors();
        for (final ITargetPropertyAccessor accessor : accessors) {
            accessor.access(context, initializer, stage);
        }
    }
    
    private void processInitializers(final Map<ConversionParticipant, TargetInfo> targetsMap, final TargetInfo returningTarget, 
            final BuildMethodContext context) {
        for (final Entry<ConversionParticipant, TargetInfo> entry : targetsMap.entrySet()) {
            final ConversionParticipant root = entry.getKey();
            final TargetInfo info = entry.getValue();
            final String id = root.getId();
            processInitializers(info, root, null, null, id, context);
        }
    }
    
    private void processInitializers(final TargetInfo info, final ConversionParticipant root, final TargetInfo parentInfo, 
            final Property parentProperty, final String variableNamePrefix, final BuildMethodContext context) {
        final ICodeBlockCache cache = context.getCache();
        final ParsedType properties = info.getProperties();
        final Set<ConstructorType> constructors = properties.getConstructors();
        final boolean parentHasImmutable = parentInfo != null && parentInfo.hasConstructor(ConstructorType.IMMUTABLE);
        final boolean hasImmutable = constructors.contains(ConstructorType.IMMUTABLE);
        final boolean hasDefault = constructors.contains(ConstructorType.DEFAULT);
        final boolean isRoot = root != null;
        final boolean isReturningTarget = isRoot && root.isReturning();
        final boolean writeable = parentProperty == null || parentProperty.isWriteable();
        final Map<String, TargetInfo> map = info.getMap();
        for (final Entry<String, TargetInfo> entry : map.entrySet()) {
            final String propertyName = entry.getKey();
            final TargetInfo value = entry.getValue();
            final Property property = properties.get(propertyName);
            Preconditions.checkNotNull(property);
            final String childVariablePrefix = variableNamePrefix + propertyName;
            processInitializers(value, null, info, property, childVariablePrefix, context);
        }
        final Collection<TargetInfo> mapValues = map.values();
        final boolean initialized = info.isInitialized();
        final AbstractTypeHolder typeHolder = properties.getType();
        if (initialized) {
            final ITargetPropertyInitializer existingInitializer = info.getInitializer();
            Preconditions.checkArgument(existingInitializer == null);
            Preconditions.checkNotNull(parentInfo);
            Preconditions.checkNotNull(parentProperty);
            PropertyGetterInitializer.add(variableNamePrefix, parentProperty, cache, info, parentInfo);
        } else {
            if (writeable || parentHasImmutable) {
                if (properties.isSimple()) {
                    if (info.getInitializer() == null) {
                        final ITargetPropertyInitializer initializer = new NullPropertyInitializer(variableNamePrefix, typeHolder);
                        info.setPropertyInitializer(initializer);
                    }
                } else {
                    if (info.getInitializer() == null && hasImmutable) {
                        final int mapValuesSize = mapValues.size();
                        final List<ITargetPropertyInitializer> propertyInitializers = new ArrayList<>(mapValuesSize);
                        boolean hasNotNull = false;
                        for (final TargetInfo propertyInfo : mapValues) {
                            if (!propertyInfo.isInitialized()) {
                                final ITargetPropertyInitializer propertyInitializer = propertyInfo.getInitializer();
                                Preconditions.checkNotNull(propertyInitializer);
                                final Boolean propertyNull = propertyInitializer.isNull();
                                if (propertyNull != Boolean.TRUE) {
                                    hasNotNull = true;
                                }
                                propertyInitializers.add(propertyInitializer);
                            }
                        }
                        if (hasNotNull || (isReturningTarget && !hasDefault)) {
                            final ITargetPropertyInitializer initializer = new ImmutableConstructorInitializer(variableNamePrefix, 
                                    typeHolder, propertyInitializers);
                            info.setPropertyInitializer(initializer);
                        }
                    }
                    if (info.getInitializer() == null && hasDefault) {
                        final ITargetPropertyInitializer initializer = new DefaultConstructorInitializer(variableNamePrefix, typeHolder);
                        info.setPropertyInitializer(initializer);
                    }
                    if (info.getInitializer() == null && parentHasImmutable) {
                        final ITargetPropertyInitializer initializer = new NullPropertyInitializer(variableNamePrefix, typeHolder);
                        info.setPropertyInitializer(initializer);
                    }
                }
            }
        }
    }
}
