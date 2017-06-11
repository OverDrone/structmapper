package org.stuctmapper.properties;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

import org.structmapper.annotations.Initialized;
import org.structmapper.annotations.Mapper;
import org.structmapper.annotations.Mappings;
import org.structmapper.annotations.SourceRef;
import org.structmapper.annotations.TargetRef;
import org.stuctmapper.instructions.IConstParser;
import org.stuctmapper.instructions.LoggableService;
import org.stuctmapper.log.Logger;
import org.stuctmapper.model.holder.AbstractElementHolder;
import org.stuctmapper.model.holder.AbstractTypeHolder;
import org.stuctmapper.model.holder.EnclosedElementKind;
import org.stuctmapper.model.holder.IAnnotationSource;
import org.stuctmapper.model.holder.TypeHolderCache;
import org.stuctmapper.model.instructions.IConstExpression;
import org.stuctmapper.model.properties.ConstructorType;
import org.stuctmapper.model.properties.ConversionParticipant;
import org.stuctmapper.model.properties.ConverterConstructorInfo;
import org.stuctmapper.model.properties.ConverterConstructorMethodInfo;
import org.stuctmapper.model.properties.ConverterInfo;
import org.stuctmapper.model.properties.ConverterMethodInfo;
import org.stuctmapper.model.properties.ParsedType;
import org.stuctmapper.model.properties.ParticipantKind;
import org.stuctmapper.model.properties.Property;
import org.stuctmapper.utils.CollectionUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

public class PropertiesReader extends LoggableService implements IPropertiesReader {
    public static final Set<Modifier> EMPTY_MODIFIERS = Collections.emptySet();
    public static final Set<Modifier> PUBLIC_MODIFIERS = ImmutableSet.of(Modifier.PUBLIC);
    public static final Set<Modifier> STATIC_MODIFIERS = ImmutableSet.of(Modifier.STATIC);
    private static final Set<String> SIMPLE_TYPE_PACKAGE_PREFIXES = ImmutableSet.of("java"); 
    private final Map<AbstractTypeHolder, ParsedType> typeMap = new HashMap<>();
    private final Map<AbstractTypeHolder, ConverterInfo> converterMap = new HashMap<>();
    private final Map<AbstractTypeHolder, ConverterConstructorInfo> constructorMap = new HashMap<>();
    private final IConstParser constParser;

    public PropertiesReader(final Logger logger, final IConstParser constParser) {
        super(logger);
        this.constParser = constParser;
    }
    
    @Override
    public ConverterInfo getConverter(final AbstractTypeHolder type) {
        final ConverterInfo existing = converterMap.get(type);
        if (existing != null) {
            return existing;
        } else {
            final ConverterInfo result = buildConverter(type);
            CollectionUtils.addNew(converterMap, type, result);
            return result;
        }
    }
    
    @Override
    public ParsedType getType(final AbstractTypeHolder type) {
        final ParsedType existing = typeMap.get(type);
        if (existing != null) {
            return existing;
        } else {
            final ParsedType result = buildType(type);
            CollectionUtils.addNew(typeMap, type, result);
            return result;
        }
    }
    
    @Override
    public ConverterConstructorInfo getConstructors(final AbstractTypeHolder type) {
        final ConverterConstructorInfo existing = constructorMap.get(type);
        if (existing != null) {
            return existing;
        } else {
            final ConverterConstructorInfo result = buildConstructors(type);
            CollectionUtils.addNew(constructorMap, type, result);
            return result;
        }
    }
    
    private ConverterConstructorInfo buildConstructors(final AbstractTypeHolder type) {
        if (isSimpleType(type)) {
            final ConverterConstructorInfo result = new ConverterConstructorInfo(type, Collections.emptyList());
            return result;
        }
        final List<ConverterConstructorMethodInfo> list = new ArrayList<>();
        final BiConsumer<AbstractTypeHolder, AbstractElementHolder> consumer = (visitedType, constructor) -> {
            final List<? extends AbstractTypeHolder> parameters = constructor.getParameters();
            if (parameters.isEmpty()) {
                return;
            }
            final Element errorElement = constructor.getErrorElement();
            final ConversionParticipant target = buildParticipant(constructor, 0, visitedType, ParticipantKind.TARGET, true, errorElement);
            Logger.checkArgument(target.getKind() == ParticipantKind.TARGET, "return type is always target", errorElement);
            
            final List<ConversionParticipant> sources = new ArrayList<>();
            final int parametersSize = parameters.size();
            for (int i = 0; i < parametersSize; i++) {
                final AbstractTypeHolder parameter = parameters.get(i);
                final ParticipantKind defaultKind = ParticipantKind.SOURCE;
                final ConversionParticipant source = buildParticipant(parameter, i + 1, parameter, defaultKind, false, errorElement);
                Logger.checkArgument(source.getKind() == ParticipantKind.SOURCE, "parameter type is always source", errorElement);
                sources.add(source);
            }
            final ConverterConstructorMethodInfo constructorInfo = new ConverterConstructorMethodInfo(constructor, target, sources);
            list.add(constructorInfo);
        };
        enumerateConstructors(type, consumer, Modifier.PUBLIC);
        final ConverterConstructorInfo result = new ConverterConstructorInfo(type, list);
        return result;
    }

    private static boolean isSimpleType(final AbstractTypeHolder type) {
        final TypeName typeName = type.getTypeName();
        if (typeName.isPrimitive()) {
            return true;
        } else if (!(typeName instanceof ClassName)) {
            return false;
        } else {
            final ClassName className = (ClassName) typeName;
            final String packageName = className.packageName();
            for (final String prefix : SIMPLE_TYPE_PACKAGE_PREFIXES) {
                if (packageName.startsWith(prefix)) {
                    return true;
                }
            }
        }
        return false;
    }

    private ParsedType buildType(final AbstractTypeHolder type) {
        if (isSimpleType(type)) {
            final ParsedType result = new ParsedType(type, Collections.emptyMap(), Collections.emptySet(), true);
            return result;
        } else {
            final Set<String> propertyNames = new LinkedHashSet<>();
            final Map<String, Accessor> setters = new HashMap<>();
            final Map<String, Accessor> getters = new HashMap<>();
            final Map<String, Accessor> fields = new HashMap<>();
            final BiConsumer<AbstractTypeHolder, AbstractElementHolder> fieldsVisitor = (visitedType, element) -> {
                final String name = element.getName();
                propertyNames.add(name);
                final AbstractTypeHolder fieldType = element.getReturnType();
                final Accessor accessor = new Accessor(element, fieldType, name);
                CollectionUtils.addNew(fields, name, accessor);
            };
            final BiConsumer<AbstractTypeHolder, AbstractElementHolder> methodsVisitor = (visitedType, element) -> {
                final Accessor setterAccessor = getSetterAccessor(element);
                if (setterAccessor != null) {
                    final String name = setterAccessor.name;
                    propertyNames.add(name);
                    CollectionUtils.addNew(setters, name, setterAccessor);
                }
                final Accessor getterAccessor = getGetterAccessor(element);
                if (getterAccessor != null) {
                    final String name = getterAccessor.name;
                    propertyNames.add(name);
                    CollectionUtils.addNew(getters, name, getterAccessor);
                }
            };
            enumerateProperties(type, fieldsVisitor, methodsVisitor, PUBLIC_MODIFIERS, STATIC_MODIFIERS);
            final Map<String, Property> map = new LinkedHashMap<String, Property>();
            for (final String propertyName : propertyNames) {
                final Accessor setter = setters.get(propertyName);
                final Accessor getter = getters.get(propertyName);
                final Accessor field = fields.get(propertyName);
                final boolean initialized = hasAnnotation(Initialized.class, field, getter, setter);
                final AbstractTypeHolder propertyType = mergeType(field, getter, setter);
                final AbstractElementHolder setterHolder = toHolder(setter);
                final AbstractElementHolder getterHolder = toHolder(getter);
                final AbstractElementHolder fieldHolder = toHolder(field);
                final Property property = new Property(initialized, propertyName, propertyType, fieldHolder, getterHolder, setterHolder);
                CollectionUtils.addNew(map, propertyName, property);
            }
            final boolean[] hasImmutableArray = new boolean[1];
            final boolean[] hasConstructorsArray = new boolean[1];
            final boolean[] hasDefaultArray = new boolean[1];
            final BiConsumer<AbstractTypeHolder, AbstractElementHolder> constructorConsumer = (visitedType, constructor) -> {
                hasConstructorsArray[0] = true;
                final Set<Modifier> modifiers = constructor.getModifiers();
                if (!modifiers.contains(Modifier.PUBLIC)) {
                    return;
                }
                if (isImmutableConstructor(constructor, map)) {
                    hasImmutableArray[0] = true;
                } else if (isDefaultConstructor(constructor)) {
                    hasDefaultArray[0] = true;
                }
            };
            enumerateConstructors(type, constructorConsumer, Modifier.PUBLIC);
            final boolean hasImmutable = hasImmutableArray[0];
            final boolean hasConstructors = hasConstructorsArray[0];
            final boolean hasDefault = hasDefaultArray[0];
            final Set<ConstructorType> constructors = new HashSet<>();
            if (hasDefault) {
                constructors.add(ConstructorType.DEFAULT);
            }
            if (hasImmutable) {
                constructors.add(ConstructorType.IMMUTABLE);
            }
            if (constructors.isEmpty() && !hasConstructors) {
                constructors.add(ConstructorType.DEFAULT);
            }
            final ParsedType result = new ParsedType(type, map, constructors, false);
            return result;
        }
    }

    private static AbstractElementHolder toHolder(final Accessor accessor) {
        if (accessor == null) {
            return null;
        } else {
            final AbstractElementHolder result = accessor.holder;
            return result;
        }
    }

    private static Accessor getGetterAccessor(final AbstractElementHolder element) {
        final String name = element.getName();
        final String propertyName = getPropertyNameFromGetter(name);
        if (propertyName == null) {
            return null;
        }
        final AbstractTypeHolder returnType = element.getReturnType();
        if (returnType == null) {
            return null;
        }
        final List<? extends AbstractTypeHolder> parameters = element.getParameters();
        if (!parameters.isEmpty()) {
            return null;
        }
        final Accessor result = new Accessor(element, returnType, propertyName);
        return result;
    }

    private static Accessor getSetterAccessor(final AbstractElementHolder element) {
        final String name = element.getName();
        final String propertyName = getPropertyNameFromSetter(name);
        if (propertyName == null) {
            return null;
        }
        final AbstractTypeHolder returnType = element.getReturnType();
        if (returnType != null) {
            return null;
        }
        final List<? extends AbstractTypeHolder> parameters = element.getParameters();
        final int size = parameters.size();
        if (size != 1) {
            return null;
        }
        final AbstractTypeHolder type = parameters.get(0);
        final Accessor result = new Accessor(element, type, propertyName);
        return result;
    }

    private boolean isDefaultConstructor(final AbstractElementHolder constructor) {
        final List<? extends AbstractTypeHolder> parameters = constructor.getParameters();
        final boolean result = parameters == null || parameters.isEmpty();
        return result; 
    }
    
    private boolean isImmutableConstructor(final AbstractElementHolder constructor, final Map<String, Property> properties) {
        final List<? extends AbstractTypeHolder> parameters = constructor.getParameters();
        if (parameters == null || parameters.size() != properties.size()) {
            return false;
        }
        final Collection<Property> values = properties.values();
        int index = 0;
        for (final Property property : values) {
            final AbstractTypeHolder parameter = parameters.get(index);
            final AbstractTypeHolder propertyType = property.getType();
            if (!Objects.equals(propertyType, parameter)) {
                return false;
            }
            index++;
        }
        return true;
    }

    private static AbstractTypeHolder mergeType(final Accessor... list) {
        AbstractTypeHolder result = null;
        for (final Accessor accessor : list) {
            if (accessor != null) {
                final AbstractTypeHolder type = accessor.type;
                final AbstractElementHolder holder = accessor.holder;
                final Element errorElement = holder.getErrorElement();
                Preconditions.checkNotNull(type, "element=%s", errorElement);
                if (result == null) {
                    result = type;
                } else {
                    Logger.checkArgument(Objects.equals(result, type), "field/getter/settter type differs", errorElement);
                }
            }
        }
        Preconditions.checkNotNull(result);
        return result;
    }

    private static void enumerateClassHierarchy(final AbstractTypeHolder type, final Consumer<AbstractTypeHolder> consumer) {
        if (type == null) {
            return;
        }
        if (type.isObject()) {
            return;
        }
        final AbstractTypeHolder superclassHolder = type.getSuperclass();
        enumerateClassHierarchy(superclassHolder, consumer);
        final Set<? extends AbstractTypeHolder> interfaces = type.getInterfaces();
        for (final AbstractTypeHolder typeInterface : interfaces) {
            enumerateClassHierarchy(typeInterface, consumer);
        }
        consumer.accept(type);
    }
    
    public static void enumerateProperties(final AbstractTypeHolder type, final BiConsumer<AbstractTypeHolder, AbstractElementHolder> fieldConsumer, 
            final BiConsumer<AbstractTypeHolder, AbstractElementHolder> methodConsumer, final Collection<Modifier> expectedModifiers, 
            final Collection<Modifier> prohibitedModifiers) {
        final Consumer<AbstractTypeHolder> hiearchyConsumer = (visitedType) -> {
            final List<? extends AbstractElementHolder> enclosedElements = visitedType.getEnclosedElements();
            for (final AbstractElementHolder enclosedElement : enclosedElements) {
                final EnclosedElementKind enclosedKind = enclosedElement.getKind();
                final Set<Modifier> enclosedModifiers = enclosedElement.getModifiers();
                if (!enclosedModifiers.containsAll(expectedModifiers)) {
                    continue;
                }
                if (!Collections.disjoint(enclosedModifiers, prohibitedModifiers)) {
                    continue;
                }
                switch (enclosedKind) {
                case FIELD: 
                    if (fieldConsumer != null) {
                        fieldConsumer.accept(visitedType, enclosedElement);
                    }
                    break;
                case METHOD:
                    if (methodConsumer != null) {
                        methodConsumer.accept(visitedType, enclosedElement);
                    }
                    break;
                default:
                    break;
                }
            }
        };
        enumerateClassHierarchy(type, hiearchyConsumer);
    }

    public void enumerateConstructors(final AbstractTypeHolder type, final BiConsumer<AbstractTypeHolder, AbstractElementHolder> consumer, final Modifier... modifiers) {
        final List<Modifier> modifiersList = Arrays.asList(modifiers);
        final Consumer<AbstractTypeHolder> hiearchyConsumer = (visitedType) -> {
            final List<? extends AbstractElementHolder> enclosedElements = visitedType.getEnclosedElements();
            if (enclosedElements == null) {
                return;
            }
            for (final AbstractElementHolder enclosedElement : enclosedElements) {
                final EnclosedElementKind enclosedKind = enclosedElement.getKind();
                final Set<Modifier> enclosedModifiers = enclosedElement.getModifiers();
                if (!enclosedModifiers.containsAll(modifiersList)) {
                    continue;
                }
                switch (enclosedKind) {
                case CONSTRUCTOR:
                  if (consumer != null) {
                      consumer.accept(visitedType, enclosedElement);
                  }
                  break;
                default:
                    break;
                }
            }
        };
        enumerateClassHierarchy(type, hiearchyConsumer);
    }
    
    private ConverterInfo buildConverter(final AbstractTypeHolder type) {
        final List<ConverterMethodInfo> methods = enumerateConverterMethods(type);
        final Set<AbstractTypeHolder> references = getReferences(type);
        final Map<String, IConstExpression> constsMap = constParser.build(type);
        final ConverterInfo result = new ConverterInfo(type, references, methods, constsMap);
        return result;
        
    }
    
    private Set<AbstractTypeHolder> getReferences(final AbstractTypeHolder type) {
        final TypeHolderCache cache = type.getCache();
        final Mapper mapperAnnotation = type.getAnnotation(Mapper.class);
        if (mapperAnnotation != null) {
            final Set<AbstractTypeHolder> result = AbstractTypeHolder.unmirrorClassArray(() -> mapperAnnotation.references(), cache);
            if (result != null) {
                return result;
            }
        }
        final Set<AbstractTypeHolder> result = Collections.emptySet();
        return result;
    }

    private List<ConverterMethodInfo> enumerateConverterMethods(final AbstractTypeHolder type) {
        final List<ConverterMethodInfo> result = new ArrayList<>();
        final BiConsumer<AbstractTypeHolder, AbstractElementHolder> visitor = (visitedType, method) -> {
            final Set<Modifier> modifiers = method.getModifiers();
            final Mappings mappingsAnnotation = method.getAnnotation(Mappings.class);
            final boolean containsMapping = mappingsAnnotation != null;
            final boolean containsFinal = modifiers.contains(Modifier.FINAL);
            final boolean containsPrivate = modifiers.contains(Modifier.PRIVATE);
            final boolean implementable = containsMapping; 
            final Element errorElement = method.getErrorElement();
            Logger.checkArgument(!implementable || !containsFinal, "mapper method cannot be final", errorElement);
            Logger.checkArgument(!implementable || !containsPrivate, "mapper method cannot be private", errorElement);
            if (containsPrivate) {
                return;
            }
            final List<ConversionParticipant> participants = new ArrayList<>();
            final AbstractTypeHolder returnType = method.getReturnType();
            final boolean hasReturnType = returnType != null; 
            if (hasReturnType) {
                final ConversionParticipant participant = buildParticipant(method, 0, returnType, ParticipantKind.TARGET, true, errorElement);
                Logger.checkArgument(participant.getKind() == ParticipantKind.TARGET, "return type is always target", errorElement);
                participants.add(participant);
            }
            final List<? extends AbstractTypeHolder> parameters = method.getParameters();
            final int parametersSize = parameters.size();
            for (int i = 0; i < parametersSize; i++) {
                final AbstractTypeHolder variable = parameters.get(i);
                final ParticipantKind defaultKind;
                if (hasReturnType || i < parametersSize - 1) {
                    defaultKind = ParticipantKind.SOURCE;
                } else {
                    defaultKind = ParticipantKind.TARGET;
                }
                final ConversionParticipant participant = buildParticipant(variable, i + 1, variable, defaultKind, false, errorElement);
                participants.add(participant);
            }
            boolean hasSource = false;
            boolean hasTarget = false;
            for (final ConversionParticipant participant : participants) {
                final ParticipantKind kind = participant.getKind();
                switch (kind) {
                case SOURCE:
                    hasSource = true;
                    break;
                case TARGET:
                    hasTarget = true;
                    break;
                }
            }
            Logger.checkArgument(!implementable || hasSource, "method should have at least one source", errorElement);
            Logger.checkArgument(!implementable || hasTarget, "method should have at least one target", errorElement);
            if (!hasSource || !hasTarget) {
                return;
            }
            final ConverterMethodInfo methodInfo = new ConverterMethodInfo(method, participants, implementable);
            result.add(methodInfo);
        };
        enumerateProperties(type, null, visitor, EMPTY_MODIFIERS, EMPTY_MODIFIERS);
        return result;
    }
    
    private ConversionParticipant buildParticipant(final IAnnotationSource annotationSource, final int index, 
            final AbstractTypeHolder type, final ParticipantKind defaultKind, final boolean initializing, final Element errorElement) {
        final ParticipantKind participantKind;
        final String id;
        final TargetRef targetAnnotation = annotationSource.getAnnotation(TargetRef.class);
        final SourceRef sourceAnnotation = annotationSource.getAnnotation(SourceRef.class);
        if (targetAnnotation != null) {
            Logger.checkArgument(sourceAnnotation == null, "Cannot have both TargetRef and SourceRef annotations", errorElement);
            id = targetAnnotation.value();
            participantKind = ParticipantKind.TARGET;
        } else if (sourceAnnotation != null) {
            id = sourceAnnotation.value();
            participantKind = ParticipantKind.SOURCE;
        } else {
            participantKind = defaultKind;
            id = "";
        }
        final String effectiveId;
        if (id.isEmpty()) {
            final String prefix = prefixByKind(participantKind);
            effectiveId = prefix + Integer.toString(index);
        } else {
            effectiveId = id;
        }
        final ConversionParticipant result = new ConversionParticipant(index, effectiveId, type, participantKind, initializing);
        return result;
    }
    
    private static String prefixByKind(final ParticipantKind kind) {
        switch (kind) {
        case SOURCE: return "source";
        case TARGET: return "target";
        default: throw new RuntimeException("Unexpected kind=" + kind);
        }
    }
    
    private static final String getPropertyNameFromSetter(final String name) {
        final String result = pascalToCamel(name, "set");
        return result;
    }

    private static final String getPropertyNameFromGetter(final String name) {
        final String getResult = pascalToCamel(name, "get");
        if (getResult != null) {
            return getResult;
        } else {
            final String isResult = pascalToCamel(name, "is");
            return isResult;
        }
    }
    
    private static final String pascalToCamel(final String value, final String prefix) {
        if (value == null || prefix == null) {
            return null;
        }
        if (!value.startsWith(prefix)) {
            return null;
        } else {
            final int prefixLength = prefix.length();
            return pascalToCamel(value, prefixLength);
        }
    }

    public static final String pascalToCamel(final String value) {
        return pascalToCamel(value, 0);
    }
    
    private static final String pascalToCamel(final String value, final int startIndex) {
        if (value.length() < startIndex + 1) {
            return null;
        }
        final char firstChar = value.charAt(startIndex);
        if (!Character.isUpperCase(firstChar)) {
            return null;
        }
        final char firstCharLower = Character.toLowerCase(firstChar);
        final String noFirstChar = value.substring(startIndex + 1);
        final String result = firstCharLower + noFirstChar;
        return result;
    }

    public static final String camelToPascal(final String value) {
        if (value.length() < 1) {
            return null;
        }
        final char firstChar = value.charAt(0);
        if (!Character.isLowerCase(firstChar)) {
            return null;
        }
        final char firstCharUpper = Character.toUpperCase(firstChar);
        final String noFirstChar = value.substring(1);
        final String result = firstCharUpper + noFirstChar;
        return result;
    }
    
    public static boolean hasAnnotation(final Class<? extends Annotation> annotationClass, final Accessor... list) {
        for (final Accessor accessor : list) {
            if (accessor != null) {
                final AbstractElementHolder holder = accessor.holder;
                final Annotation annotation = holder.getAnnotation(annotationClass);
                if (annotation != null) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private static class Accessor {
        final AbstractElementHolder holder;
        final AbstractTypeHolder type;
        final String name;
        
        public Accessor(final AbstractElementHolder holder, final AbstractTypeHolder type, final String name) {
            super();
            this.holder = holder;
            this.type = type;
            this.name = name;
        }
        
    }
}
