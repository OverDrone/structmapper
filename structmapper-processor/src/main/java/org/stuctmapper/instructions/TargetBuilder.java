package org.stuctmapper.instructions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import javax.lang.model.element.Element;

import org.stuctmapper.log.Logger;
import org.stuctmapper.model.holder.AbstractTypeHolder;
import org.stuctmapper.model.instructions.ISourceExpression;
import org.stuctmapper.model.instructions.ITargetPropertyAccessor;
import org.stuctmapper.model.instructions.ITargetPropertyInitializer;
import org.stuctmapper.model.instructions.NotNullExpressionWrapper;
import org.stuctmapper.model.instructions.PropertyGetterInitializer;
import org.stuctmapper.model.instructions.ReturningMethodCall;
import org.stuctmapper.model.instructions.SourcePropertyInitializer;
import org.stuctmapper.model.instructions.SourceRootInitializer;
import org.stuctmapper.model.instructions.TargetBuildResult;
import org.stuctmapper.model.instructions.TargetInfo;
import org.stuctmapper.model.instructions.TargetPathMatchResult;
import org.stuctmapper.model.instructions.TargetPropertyMappingInitializer;
import org.stuctmapper.model.instructions.VoidMethodCall;
import org.stuctmapper.model.mapping.ConversionSignature;
import org.stuctmapper.model.mapping.ConversionSignatureItem;
import org.stuctmapper.model.mapping.MappingPath;
import org.stuctmapper.model.mapping.MappingPaths;
import org.stuctmapper.model.mapping.MatchedConversionSignature;
import org.stuctmapper.model.mapping.MethodMapping;
import org.stuctmapper.model.properties.ConversionParticipant;
import org.stuctmapper.model.properties.ConverterConstructorInfo;
import org.stuctmapper.model.properties.ConverterConstructorMethodInfo;
import org.stuctmapper.model.properties.ConverterInfo;
import org.stuctmapper.model.properties.ConverterMethodInfo;
import org.stuctmapper.model.properties.ParsedType;
import org.stuctmapper.model.properties.ParticipantKind;
import org.stuctmapper.model.properties.Property;
import org.stuctmapper.properties.IPropertiesReader;
import org.stuctmapper.utils.CollectionUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

public class TargetBuilder extends LoggableService implements ITargetBuilder {
    
    private final IPropertiesReader propertiesReader;

    public TargetBuilder(final IPropertiesReader propertiesReader, final Logger logger) {
        super(logger);
        this.propertiesReader = propertiesReader;
    }
    
    @Override
    public TargetBuildResult build(final BuildMethodContext context, final MethodMapping info, final ConverterInfo converterInfo) {
        final ICodeBlockCache cache = context.getCache();
        final ConverterMethodInfo method = info.getMethod();
        final Map<String, ConversionParticipant> rootSourcesMap = method.getSources();
        final Collection<ConversionParticipant> rootSources = rootSourcesMap.values();
        final Map<ConversionParticipant, ISourceExpression> sourceExpressionMap = buildExpressionMap(cache, rootSources);
        final List<MappingPaths> mappings = info.getMappings();
        final Map<ConversionParticipant, TargetInfo> targetMap = buildMap(method);
        for (final MappingPaths mapping : mappings) {
            final List<MappingPath> sources = mapping.getSources();
            final List<MappingPath> targets = mapping.getTargets();
            build(context, sources, targets, method, sourceExpressionMap, targetMap, converterInfo);
        }
        final TargetBuildResult result = new TargetBuildResult(targetMap, sourceExpressionMap);
        return result;
    }

    private Map<ConversionParticipant, ISourceExpression> buildExpressionMap(final ICodeBlockCache cache, final Collection<ConversionParticipant> list) {
        final int listSize = list.size();
        Map<ConversionParticipant, ISourceExpression> result = new HashMap<>(listSize);
        for (final ConversionParticipant source : list) {
            final ISourceExpression expression = getSourceRootExpression(cache, source);
            CollectionUtils.addNew(result, source, expression);
        }
        return result;
    }

    private Map<ConversionParticipant, TargetInfo> buildMap(final ConverterMethodInfo method) {
        final Element errorElement = method.getErrorElement();
        final Map<ConversionParticipant, TargetInfo> result = new HashMap<>();
        final Map<String, ConversionParticipant> targetsMap = method.getTargets();
        final Collection<ConversionParticipant> targetValues = targetsMap.values();
        for (final ConversionParticipant target : targetValues) {
            final TargetInfo info = buildInfo(target, errorElement);
            CollectionUtils.addNew(result, target, info);
        }
        return result;
    }

    private TargetInfo buildInfo(final ConversionParticipant value, final Element errorElement) {
        final boolean initialized = !value.isReturning();
        final AbstractTypeHolder type = value.getType();
        final ParsedType properties = propertiesReader.getType(type);
        final Map<String, TargetInfo> childMap = buildInfoChildren(properties);
        final TargetInfo result = new TargetInfo(null, properties, childMap, initialized, errorElement);
        return result;
    }
    
    private Map<String, TargetInfo> buildInfoChildren(final ParsedType properties) {
        final Map<String, Property> propertiesMap = properties.getMap();
        final int size = propertiesMap.size();
        final Map<String, TargetInfo> map = new LinkedHashMap<>(size);
        for (final Entry<String, Property> entry : propertiesMap.entrySet()) {
            final String key = entry.getKey();
            final Property value = entry.getValue();
            final boolean propertyInitialized = value.isInitialized();
            final AbstractTypeHolder type = value.getType();
            final ParsedType childProperties = propertiesReader.getType(type);
            final Element errorElement = value.getErrorElement();
            final Map<String, TargetInfo> childMap = buildInfoChildren(childProperties);
            final TargetInfo info = new TargetInfo(value, childProperties, childMap, propertyInitialized, errorElement);
            CollectionUtils.addNew(map, key, info);
        }
        final Map<String, TargetInfo> result = ImmutableMap.copyOf(map);
        return result;
    }
    

    private void build(final BuildMethodContext context, final List<MappingPath> sources, final List<MappingPath> targets, final ConverterMethodInfo method, 
            final Map<ConversionParticipant, ISourceExpression> sourceExpressionMap, final Map<ConversionParticipant, TargetInfo> map, 
            final ConverterInfo converterInfo) {
        final ICodeBlockCache cache = context.getCache();
        final int sourcesSize = sources.size();
        int targetsSize = targets.size();
        final Element errorElement = method.getErrorElement();
        final ConversionSignature signature = buildSignature(sources, targets, errorElement);
        final List<ISourceExpression> sourceExpressions = new ArrayList<>(sourcesSize);
        for (final MappingPath source : sources) {
            final ISourceExpression sourceExpression = wrapSource(context, source, sourceExpressionMap);
            sourceExpressions.add(sourceExpression);
        }
        final List<TargetPathMatchResult> targetMatches = new ArrayList<>(targetsSize);
        for (final MappingPath target : targets) {
            final TargetPathMatchResult match = wrapTarget(target, map);
            targetMatches.add(match);
        }
        final MatchedConversionSignature matchedMethod = findConverterBySignature(signature, converterInfo, errorElement);
        if (matchedMethod != null) {
            addMethodCall(cache, sourceExpressions, targetMatches, matchedMethod);
        } else {
            Logger.checkArgument(sourcesSize == 1, "multiple sources require method or constructor call", errorElement);
            Logger.checkArgument(targetsSize == 1, "multiple targets require method or constructor call", errorElement);
            final ISourceExpression expression = sourceExpressions.get(0);
            final TargetPathMatchResult match = targetMatches.get(0);
            addAssignment(match, expression);
        }
    }

    private void addMethodCall(final ICodeBlockCache cache, final List<ISourceExpression> sourceExpressions, 
            final List<TargetPathMatchResult> targetMatches, final MatchedConversionSignature matchedMethod) {
        replaceInitializersWithGetters(cache, targetMatches);
        if (matchedMethod.isReturning()) {
            final ITargetPropertyInitializer initializer = new ReturningMethodCall(matchedMethod, sourceExpressions, targetMatches);
            final Integer index = matchedMethod.getReturningParameterIndex();
            final TargetPathMatchResult returningMatch = targetMatches.get(index);
            final TargetInfo returningInfo = returningMatch.getInfo();
            returningInfo.setPropertyInitializer(initializer);
        } else {
            VoidMethodCall.call(matchedMethod, sourceExpressions, targetMatches);
        }
    }

    private void replaceInitializersWithGetters(final ICodeBlockCache cache, final List<TargetPathMatchResult> targetMatches) {
        for (final TargetPathMatchResult matchResult : targetMatches) {
            final TargetInfo info = matchResult.getInfo();
            final TargetInfo parentInfo = matchResult.getParent();
            final String variableNamePrefix = matchResult.getVariableNamePrefix();
            replaceInitializersWithGetters(cache, variableNamePrefix, info, parentInfo);
        }
    }

    private void replaceInitializersWithGetters(final ICodeBlockCache cache, final String variableNamePrefix, final TargetInfo info, final TargetInfo parentInfo) {
        final Map<String, TargetInfo> map = info.getMap();
        final Collection<TargetInfo> values = map.values();
        for (final TargetInfo childInfo : values) {
            final Property parentProperty = childInfo.getParentProperty();
            Preconditions.checkNotNull(parentProperty);
            final String propertyName = parentProperty.getName();
            final String childPrefix = variableNamePrefix + propertyName;
            replaceInitializersWithGetters(cache, childPrefix, childInfo, info);
        }
        final ITargetPropertyInitializer existingInitializer = info.getInitializer();
        if (existingInitializer != null) {
            final Property parentProperty = info.getParentProperty();
            Preconditions.checkNotNull(parentProperty);
            PropertyGetterInitializer.add(variableNamePrefix, parentProperty, cache, info, parentInfo);
        }
    }

    private TargetPathMatchResult wrapTarget(final MappingPath target, final Map<ConversionParticipant, TargetInfo> map) {
        final ConversionParticipant targetRoot = target.getRoot();
        final String id = targetRoot.getId();
        final TargetInfo targetInfo = map.get(targetRoot);
        Preconditions.checkNotNull(targetInfo);
        final TargetPathMatchResult match = buildOnTargetPath(id, targetInfo, target);
        Preconditions.checkNotNull(match);
        return match;
    }
    
    private ISourceExpression wrapSource(final BuildMethodContext context, final MappingPath source, 
            final Map<ConversionParticipant, ISourceExpression> sourceExpressionMap) {
        final ICodeBlockCache cache = context.getCache();
        final ISourceExpression constExpression = source.getConstExpression();
        if (constExpression != null) {
            final ISourceExpression registeredExpression = cache.add(constExpression);
            return registeredExpression;
        }
        final ConversionParticipant sourceRoot = source.getRoot();
        final ISourceExpression sourceRootExpression = sourceExpressionMap.get(sourceRoot);
        Preconditions.checkNotNull(sourceRootExpression);
        final ISourceExpression sourceExpression = getSource(cache, source, sourceRoot, sourceRootExpression);
        Preconditions.checkNotNull(sourceExpression);
        return sourceExpression;
    }
    
    private void addAssignment(final TargetPathMatchResult match, final ISourceExpression expression) {
        final TargetInfo info = match.getInfo();
        final ITargetPropertyInitializer existingInitializer = info.getInitializer();
        final Set<ITargetPropertyAccessor> existingAccessors = info.getAccessors();
        Preconditions.checkArgument(existingInitializer == null);
        Preconditions.checkArgument(existingAccessors.isEmpty());
        
        final ITargetPropertyInitializer initializer = new TargetPropertyMappingInitializer(expression);
        info.setPropertyInitializer(initializer);
    }

    private MatchedConversionSignature findConverterBySignature(final ConversionSignature signature, 
            final ConverterInfo converterInfo, final Element errorElement) {
        final Set<AbstractTypeHolder> searchedTypesSet = new HashSet<>();
        final MatchedConversionSignature result = findConverterBySignature(signature, converterInfo, errorElement, true, searchedTypesSet);
        return result;
    }
    
    private MatchedConversionSignature findConverterBySignature(final ConversionSignature signature, final ConverterInfo converterInfo, 
            final Element errorElement, final boolean sameType, final Set<AbstractTypeHolder> searchedTypesSet) {
        final AbstractTypeHolder converterType = converterInfo.getType();
        if (searchedTypesSet.contains(converterType)) {
            return null;
        }
        final List<ConverterMethodInfo> methods = converterInfo.getMethods();
        final int signatureParametersCount = signature.size();
        final boolean explicitMapping = isExplicitMapping(signature, errorElement);
        for (final ConverterMethodInfo method : methods) {
            final Map<Integer, ConversionParticipant> indexMap = method.getIndexMap();
            final int methodParametersCount = indexMap.size();
            if (methodParametersCount != signatureParametersCount) {
                continue;
            }
            final int[] parameterIndexes = new int[methodParametersCount]; 
            if (explicitMapping) {
                if (!matchesExplicitly(signature, method, parameterIndexes)) {
                    continue;
                }
            } else {
                if (!matchesImplicitly(signature, method, parameterIndexes)) {
                    continue;
                }
            }
            final AbstractTypeHolder referencedType;
            if (sameType) {
                referencedType = null;
            } else {
                referencedType = converterType;
            }
            final Collection<ConversionParticipant> participants = indexMap.values();
            final Integer returningParticipantIndex = getReturnindIndex(participants);
            final Integer returningIndex;
            if (returningParticipantIndex != null) {
                final ConversionParticipant participant = indexMap.get(returningParticipantIndex);
                returningIndex = calcIndex(method, participant); 
            } else {
                returningIndex = null;
            }
            final MatchedConversionSignature result = new MatchedConversionSignature(parameterIndexes, returningIndex, referencedType, method);
            return result;
        }
        if (sameType) {
            final MatchedConversionSignature constructorMatch = matchConstructor(signature, errorElement, searchedTypesSet);
            if (constructorMatch != null) {
                return constructorMatch;
            }
        }
        searchedTypesSet.add(converterType);
        final Set<AbstractTypeHolder> references = converterInfo.getReferences();
        for (final AbstractTypeHolder reference : references) {
            final ConverterInfo referencedConverter = propertiesReader.getConverter(reference);
            final MatchedConversionSignature referencedMethod = findConverterBySignature(signature, referencedConverter, errorElement, false, searchedTypesSet);
            if (referencedMethod != null) {
                return referencedMethod;
            }
        }
        return null;
    }
    
    private MatchedConversionSignature matchConstructor(final ConversionSignature signature, final Element errorElement, final Set<AbstractTypeHolder> searchedTypesSet) {
        if (!isConstructorSignature(signature)) {
            return null;
        }
        final int signatureSize = signature.size();
        final boolean explicitMapping = isExplicitMapping(signature, errorElement);
        final ConversionSignatureItem lastSignatureTarget = signature.get(signatureSize - 1);
        Preconditions.checkArgument(lastSignatureTarget != null && lastSignatureTarget.getKind() == ParticipantKind.TARGET);
        final AbstractTypeHolder type = lastSignatureTarget.getType();
        if (searchedTypesSet.contains(type)) {
            return null;
        }
        final ConverterConstructorInfo typeInfo = propertiesReader.getConstructors(type);
        final List<ConverterConstructorMethodInfo> constructors = typeInfo.getConstructors();
        for (final ConverterConstructorMethodInfo constructor : constructors) {
            final Map<String, ConversionParticipant> constructorSources = constructor.getSourcesMap();
            final int constructorSourcesCount = constructorSources.size();
            if (constructorSourcesCount != signatureSize - 1) {
                continue;
            }
            final int[] parameterIndexes = new int[signatureSize]; 
            if (explicitMapping) {
                if (!matchesExplicitly(signature, constructor, parameterIndexes)) {
                    continue;
                }
            } else {
                if (!matchesImplicitly(signature, constructor, parameterIndexes)) {
                    continue;
                }
            }
            final MatchedConversionSignature result = new MatchedConversionSignature(parameterIndexes, constructor);
            return result;
        }
        return null;
    }

    private boolean isConstructorSignature(final ConversionSignature signature) {
        final List<ConversionSignatureItem> items = signature.getItems();
        int targetsCount = 0;
        int sourcesCount = 0;
        for (final ConversionSignatureItem item : items) {
            final ParticipantKind itemKind = item.getKind();
            switch (itemKind) {
            case TARGET: 
                targetsCount++;
                break;
            case SOURCE:
                sourcesCount++;
                break;
            default:
                throw new RuntimeException("Unexpected " + itemKind);
            }
        }
        final boolean hasSources = sourcesCount > 0;
        final boolean singleTarget = targetsCount == 1;
        final boolean result = singleTarget && hasSources;
        return result;
    }

    private static boolean matchesExplicitly(final ConversionSignature signature, final ConverterMethodInfo method, final int[] parameterIndexes) {
        final Map<String, ConversionParticipant> sources = method.getSources();
        final Map<String, ConversionParticipant> targets = method.getTargets();
        final List<ConversionSignatureItem> signatureItems = signature.getItems();
        int index = -1;
        for (final ConversionSignatureItem signatureItem : signatureItems) {
            index++;
            final String signatureItemId = signatureItem.getId();
            final ParticipantKind kind = signatureItem.getKind();
            final Map<String, ConversionParticipant> searchMap;
            switch (kind) {
            case SOURCE: 
                searchMap = sources;
                break;
            case TARGET:
                searchMap = targets;
                break;
            default:
                throw new RuntimeException("Unexpected " + kind);
            }
            final ConversionParticipant methodItem = searchMap.get(signatureItemId);
            if (!matches(signatureItem, methodItem)) {
                return false;
            }
            parameterIndexes[index] = calcIndex(method, methodItem);
        }
        return true;
    }
    
    private static boolean matchesExplicitly(final ConversionSignature signature, final ConverterConstructorMethodInfo constructor, 
            final int[] parameterIndexes) {
        final Map<String, ConversionParticipant> sourcesMap = constructor.getSourcesMap();
        final List<ConversionSignatureItem> signatureItems = signature.getItems();
        int index = -1;
        for (final ConversionSignatureItem signatureItem : signatureItems) {
            index++;
            final ParticipantKind signatureKind = signatureItem.getKind();
            final ConversionParticipant constructorItem;
            switch (signatureKind) {
            case SOURCE:
                final String signatureId = signatureItem.getId();
                constructorItem = sourcesMap.get(signatureId);
                break;
            case TARGET:
                constructorItem = constructor.getTarget();
                break;
            default:
                throw new RuntimeException("Unexpected " + signatureKind);
            }
            if (!matches(signatureItem, constructorItem)) {
                return false;
            }
            parameterIndexes[index] = constructorItem.getIndex();
        }
        return true;
    }

    private boolean matchesImplicitly(final ConversionSignature signature, final ConverterMethodInfo method, final int[] parameterIndexes) {
        final Map<Integer, ConversionParticipant> indexMap = method.getIndexMap();
        final boolean returning = indexMap.get(0) != null;
        final List<ConversionSignatureItem> signatureItems = signature.getItems();
        final int signatureSize = signatureItems.size();
        int index = -1;
        for (final ConversionSignatureItem signatureItem : signatureItems) {
            index++;
            final int searchIndex;
            if (index == signatureSize - 1 && returning) {
                searchIndex = 0;
            } else {
                searchIndex = index + 1;
            }
            final ConversionParticipant methodItem = indexMap.get(searchIndex);
            if (!matches(signatureItem, methodItem)) {
                return false;
            }
            final int methodItemIndex = calcIndex(method, methodItem);
            parameterIndexes[index] = methodItemIndex;
        }
        return true;
    }
    
    private boolean matchesImplicitly(final ConversionSignature signature, final ConverterConstructorMethodInfo constructor, 
            final int[] parameterIndexes) {
        final List<ConversionSignatureItem> signatureItems = signature.getItems();
        final int signatureSize = signatureItems.size();
        int index = -1;
        for (final ConversionSignatureItem signatureItem : signatureItems) {
            index++;
            final int searchIndex;
            if (index == signatureSize - 1) {
                searchIndex = 0;
            } else {
                searchIndex = index + 1;
            }
            final ConversionParticipant constructorItem = constructor.byIndex(searchIndex);
            if (!matches(signatureItem, constructorItem)) {
                return false;
            }
            final int constructorItemIndex = constructorItem.getIndex();
            parameterIndexes[index] = constructorItemIndex;
        }
        return true;
    }
    
    private static boolean matches(final ConversionSignatureItem signatureItem, final ConversionParticipant methodItem) {
        if (signatureItem == null || methodItem == null) {
            return false;
        }
        final ParticipantKind signatureKind = signatureItem.getKind();
        final ParticipantKind methodKind = methodItem.getKind();
        if (signatureKind != methodKind) {
            return false;
        }
        final AbstractTypeHolder signatureType = signatureItem.getType();
        final AbstractTypeHolder methodType = methodItem.getType();
        final boolean result = Objects.equals(signatureType, methodType);
        return result;
    }
    
    private static Integer getReturnindIndex(final Collection<ConversionParticipant> participants) {
        for (final ConversionParticipant participant : participants) {
            if (participant.isReturning()) {
                final int result = participant.getIndex();
                return result;
            }
        }
        return null;
    }

    private static int calcIndex(final ConverterMethodInfo method, final ConversionParticipant item) {
        final Map<String, ConversionParticipant> sourcesMap = method.getSources();
        final Map<String, ConversionParticipant> targetsMap = method.getTargets();
        final Collection<ConversionParticipant> sources = sourcesMap.values();
        final Collection<ConversionParticipant> targets = targetsMap.values();
        int index = 0;
        for (final ConversionParticipant target : targets) {
            if (target == item) {
                return index;
            }
            index++;
        }
        for (final ConversionParticipant source : sources) {
            if (source == item) {
                return index;
            }
            index++;
        }
        throw new RuntimeException("item not found");
    }

    private boolean isExplicitMapping(final ConversionSignature signature, final Element errorElement) {
        Boolean result = null;
        final List<ConversionSignatureItem> items = signature.getItems();
        for (final ConversionSignatureItem item : items) {
            final String id = item.getId();
            final boolean itemExplicitMapping =  id != null;
            if (result == null) {
                result = itemExplicitMapping;
            } else {
                if (itemExplicitMapping) {
                    Logger.checkArgument(result, "all participants should be mapped explicitly (id is set)", errorElement);
                } else {
                    Logger.checkArgument(!result, "all participants should be mapped implicitly (id is not set)", errorElement);
                }
            }
        }
        Preconditions.checkNotNull(result);
        return result;
    }

    private ConversionSignature buildSignature(final List<MappingPath> sources, final List<MappingPath> targets, final Element errorElement) {
        final int sourcesSize = sources.size();
        final int targetsSize = targets.size();
        final int size = sourcesSize + targetsSize;
        final List<ConversionSignatureItem> list = new ArrayList<>(size);
        buildSignatureItems(sources, list, ParticipantKind.SOURCE, errorElement);
        buildSignatureItems(targets, list, ParticipantKind.TARGET, errorElement);
        final ConversionSignature result = new ConversionSignature(list);
        return result;
    }
    
    private ConversionSignatureItem buildSignature(final MappingPath path, final ParticipantKind kind, final Element errorElement) {
        final AbstractTypeHolder type = getPathType(path, errorElement);
        final String id = path.getId();
        final ConversionSignatureItem result = new ConversionSignatureItem(type, id, kind, null);
        return result;
    }
    
    private void buildSignatureItems(final List<MappingPath> src, final List<ConversionSignatureItem> dst, 
            final ParticipantKind kind, final Element errorElement) {
        for (final MappingPath srcItem : src) {
            final ConversionSignatureItem dstItem = buildSignature(srcItem, kind, errorElement);
            dst.add(dstItem);
        }
    }
    
    private AbstractTypeHolder getPathType(final MappingPath path, final Element errorElement) {
        final ISourceExpression constExpression = path.getConstExpression();
        if (constExpression != null) {
            final AbstractTypeHolder result = constExpression.getType();
            return result;
        }
        final ConversionParticipant root = path.getRoot();
        final List<String> list = path.getPath();
        AbstractTypeHolder current = root.getType();
        for (final String propertyName : list) {
            final ParsedType parsedType = propertiesReader.getType(current);
            Preconditions.checkNotNull(parsedType);
            final Property property = parsedType.get(propertyName);
            Logger.checkArgument(property != null, "Unknown property " + propertyName, errorElement);
            current = property.getType();
            Preconditions.checkNotNull(current);
        }
        return current;
    }
    
    private TargetPathMatchResult buildOnTargetPath(final String variableNamePrefix, final TargetInfo start, final MappingPath target) {
        final List<String> path = target.getPath();
        TargetInfo parent = null;
        TargetInfo current = start;
        String currentPrefix = variableNamePrefix;
        for (final String propertyName : path) {
            parent = current;
            current = current.get(propertyName);
            currentPrefix = currentPrefix + propertyName;
            Preconditions.checkNotNull(current);
        }
        Preconditions.checkNotNull(parent);
        Preconditions.checkNotNull(current);
        final TargetPathMatchResult result = new TargetPathMatchResult(currentPrefix, current, parent);
        return result;
    }
    
    private ISourceExpression getSourceRootExpression(final ICodeBlockCache cache, final ConversionParticipant root) {
        final ISourceExpression rootInitializer = new SourceRootInitializer(root);
        final ISourceExpression registeredRoot = cache.add(rootInitializer);
        return registeredRoot;
    }
    
    private ISourceExpression getSource(final ICodeBlockCache cache, final MappingPath source, 
            final ConversionParticipant root, final ISourceExpression sourceExpression) {
        final AbstractTypeHolder typeElement = root.getType();
        final ParsedType type = propertiesReader.getType(typeElement);
        final List<String> path = source.getPath();
        final ISourceExpression result = getSource(cache, path, 0, sourceExpression, type, false);
        return result;
    }
    
    private ISourceExpression getSource(final ICodeBlockCache cache, final List<String> path, final int index, 
            final ISourceExpression parent, final ParsedType type, final boolean doWrap) {
        final int size = path.size();
        if (index >= size) {
            Preconditions.checkArgument(index == size);
            return parent;
        } else {
            final String propertyName = path.get(index);
            final Property property = type.get(propertyName);
            final AbstractTypeHolder propertyTypeElement = property.getType();
            final ParsedType propertyType = propertiesReader.getType(propertyTypeElement);
            final ISourceExpression wrappedParent;
            if (doWrap) {
                wrappedParent = NotNullExpressionWrapper.get(parent, cache);
            } else {
                wrappedParent = parent;
            }
            final ISourceExpression propertyInitializer = SourcePropertyInitializer.get(property, wrappedParent, cache);
            final ISourceExpression result = getSource(cache, path, index + 1, propertyInitializer, propertyType, true);
            return result;
        }
    }
}
