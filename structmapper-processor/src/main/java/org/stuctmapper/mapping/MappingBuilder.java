package org.stuctmapper.mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;

import org.structmapper.annotations.Mapping;
import org.structmapper.annotations.Mappings;
import org.structmapper.annotations.Path;
import org.stuctmapper.expression.IExpressionToken;
import org.stuctmapper.expression.IRawTokenParser;
import org.stuctmapper.expression.ITokenParser;
import org.stuctmapper.expression.tokens.CompositeIdToken;
import org.stuctmapper.log.Logger;
import org.stuctmapper.model.holder.AbstractElementHolder;
import org.stuctmapper.model.instructions.IConstExpression;
import org.stuctmapper.model.instructions.ISourceExpression;
import org.stuctmapper.model.mapping.MappingPath;
import org.stuctmapper.model.mapping.MappingPaths;
import org.stuctmapper.model.mapping.MethodMapping;
import org.stuctmapper.model.properties.ConversionParticipant;
import org.stuctmapper.model.properties.ConverterMethodInfo;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class MappingBuilder implements IMappingBuilder {
    
    private final ITokenParser tokenParser;
    private final IRawTokenParser rawTokenParser;

    public MappingBuilder(final ITokenParser tokenParser, final IRawTokenParser rawTokenParser) {
        this.tokenParser = tokenParser;
        this.rawTokenParser = rawTokenParser;
    }
    
    @Override
    public MethodMapping build(final ConverterMethodInfo method, final Map<String, IConstExpression> constsMap) {
        final AbstractElementHolder element = method.getElement();
        final Element errorElement = method.getErrorElement();
        final Map<String, ConversionParticipant> sourcesMap = method.getSources();
        final Map<String, ConversionParticipant> targetsMap = method.getTargets();
        Logger.checkArgument(sourcesMap.size() == 1, "more than one source not implemented", errorElement);
        Logger.checkArgument(targetsMap.size() == 1, "more than one target not implemented", errorElement);
        final ConversionParticipant source = sourcesMap.values().iterator().next();
        Preconditions.checkNotNull(source);
        final ConversionParticipant target = targetsMap.values().iterator().next();
        Preconditions.checkNotNull(target);
        final Mappings mappingsAnnotation = element.getAnnotation(Mappings.class);
        final List<MappingPaths> mappings = new ArrayList<>();
        if (mappingsAnnotation != null) {
            final Mapping[] mappingsValue = mappingsAnnotation.value();
            if (mappingsValue != null) {
                for (final Mapping mapping : mappingsValue) {
                    final Path[] mappingSource = mapping.sources();
                    final List<MappingPath> sourcePaths = getMappingPath(mappingSource, source, errorElement, constsMap);
                    final Path[] mappingTarget = mapping.targets();
                    final List<MappingPath> targetPaths = getMappingPath(mappingTarget, target, errorElement, null);
                    final MappingPaths mappingPaths = new MappingPaths(sourcePaths, targetPaths);
                    mappings.add(mappingPaths);
                }
            }
        }
        final MethodMapping result = new MethodMapping(method, mappings);
        return result;
    }

    private final List<MappingPath> getMappingPath(final Path[] paths, final ConversionParticipant root, 
            final Element errorElement, final Map<String, IConstExpression> constsMap) {
        if (paths == null) {
            final List<MappingPath> result = Collections.emptyList();
            return result;
        }
        final int pathsSize = paths.length;
        final List<MappingPath> list = new ArrayList<>(pathsSize);
        for (final Path path : paths) {
            final MappingPath mappingPath = getMappingPath(path, root, errorElement, constsMap);
            list.add(mappingPath);
        }
        final List<MappingPath> result = ImmutableList.copyOf(list);
        return result;
    }
    
    private final MappingPath getMappingPath(final Path path, final ConversionParticipant root, final Element errorElement, 
            final Map<String, IConstExpression> constsMap) {
        final String pathValue = path.value();
        final List<IExpressionToken> tokenList = rawTokenParser.parse(pathValue);
        final CompositeIdToken compositeId = tokenParser.parse(tokenList);
        Preconditions.checkNotNull(compositeId);
        final String id = path.id();
        Logger.checkArgument(!pathValue.contains("."), "embedded properties (a.b) not implemented", errorElement);
        if (constsMap != null) {
            final ISourceExpression sourceConst = constsMap.get(pathValue);
            if (sourceConst != null) {
                final MappingPath result = new MappingPath(id, sourceConst);
                return result;
            }
        }
        final MappingPath result = new MappingPath(root, id, Arrays.asList(pathValue));
        return result;
    }
}
