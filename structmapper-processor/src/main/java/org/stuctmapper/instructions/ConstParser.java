package org.stuctmapper.instructions;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

import org.structmapper.annotations.Const;
import org.structmapper.annotations.Mapper;
import org.stuctmapper.log.Logger;
import org.stuctmapper.model.holder.AbstractElementHolder;
import org.stuctmapper.model.holder.AbstractTypeHolder;
import org.stuctmapper.model.holder.TypeHolderCache;
import org.stuctmapper.model.instructions.ConstLiteral;
import org.stuctmapper.model.instructions.ConstMember;
import org.stuctmapper.model.instructions.IConstExpression;
import org.stuctmapper.properties.PropertiesReader;
import org.stuctmapper.utils.CollectionUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;

public class ConstParser implements IConstParser {
    
    private static List<Function<Const, Object>> literalList = buildList();
    private static List<TriFunction<Const, AbstractTypeHolder, String, IConstExpression>> functionList = ImmutableList.of(
            ConstParser::buildFromMember, ConstParser::buildFromLiteral, ConstParser::buildFromClassLiteral);

    @Override
    public Map<String, IConstExpression> build(final AbstractTypeHolder type) {
        final Map<String, IConstExpression> result = new HashMap<>();
        final Mapper mapperAnnotation = type.getAnnotation(Mapper.class);
        if (mapperAnnotation != null) {
            final Const[] consts = mapperAnnotation.consts();
            if (consts != null) {
                for (final Const constAnnotation : consts) {
                    final String name = constAnnotation.name();
                    final IConstExpression constExpression = build(constAnnotation, type, name);
                    CollectionUtils.addNew(result, name, constExpression);
                }
            }
        }
        return result;
    }

    private IConstExpression build(final Const constAnnotation, final AbstractTypeHolder type, final String name) {
        IConstExpression result = null;
        for (final TriFunction<Const, AbstractTypeHolder, String, IConstExpression> function : functionList) {
            final IConstExpression expression = function.apply(constAnnotation, type, name);
            if (expression != null) {
                Logger.checkArgument(result == null, "only one expression allowed", type.getErrorElement());
                result = expression;
            }
        }
        return result;
    }
    
    private static List<Function<Const, Object>> buildList() {
        final Builder<Function<Const, Object>> builder = ImmutableList.builder();
        builder.add((c) -> c.bool());
        builder.add((c) -> c.b());
        builder.add((c) -> c.c());
        builder.add((c) -> c.sh());
        builder.add((c) -> c.i());
        builder.add((c) -> c.l());
        builder.add((c) -> c.f());
        builder.add((c) -> c.d());
        builder.add((c) -> c.s());
        final List<Function<Const, Object>> result = builder.build();
        return result;
    }
    
    private static IConstExpression buildFromLiteral(final Const constAnnotation, final AbstractTypeHolder type, final String name) {
        final Element errorElement = type.getErrorElement();
        IConstExpression result = null;
        final TypeHolderCache cache = type.getCache();
        for (final Function<Const, Object> function : literalList) {
            final Object array = function.apply(constAnnotation);
            if (array == null) {
                continue;
            }
            final int length = Array.getLength(array);
            if (length == 0) {
                continue;
            }
            Logger.checkArgument(length == 1, "only one literal in array allowed", errorElement);
            final Object value = Array.get(array, 0);
            if (value != null) {
                Logger.checkArgument(result == null, "only one literal allowed", errorElement);
                result = new ConstLiteral(value, cache, name);
            }
        }
        return result;
    }

    private static IConstExpression buildFromClassLiteral(final Const constAnnotation, final AbstractTypeHolder type, final String name) {
        final TypeHolderCache cache = type.getCache();
        final Set<AbstractTypeHolder> set = AbstractTypeHolder.unmirrorClassArray(() -> constAnnotation.cl(), cache);
        final AbstractTypeHolder holder = getSingleFromSet(set, type);
        if (holder == null) {
            return null;
        } else {
            final ConstLiteral result = new ConstLiteral(holder, cache, name);
            return result;
        }
    }
    
    private static IConstExpression buildFromMember(final Const constAnnotation, final AbstractTypeHolder type, final String name) {
        final TypeHolderCache cache = type.getCache();
        final Set<AbstractTypeHolder> classSet = AbstractTypeHolder.unmirrorClassArray(() -> constAnnotation.cl(), cache); 
        final AbstractTypeHolder singleHolder = getSingleFromSet(classSet, type);
        final AbstractTypeHolder registeredHolder = cache.register(singleHolder);
        final Set<Modifier> expectedModifiers;
        final Set<Modifier> prohibitedModifiers;
        if (singleHolder == null) {
            expectedModifiers = PropertiesReader.EMPTY_MODIFIERS;
            prohibitedModifiers = PropertiesReader.EMPTY_MODIFIERS;
        } else {
            expectedModifiers = PropertiesReader.STATIC_MODIFIERS;
            prohibitedModifiers = ImmutableSet.of(Modifier.PRIVATE);
        }
        final String[] members = constAnnotation.member();
        final String member = getSingleFromArray(members, type);
        if (member == null) {
            return null;
        }
        final IConstExpression[] arr = new IConstExpression[1];
        final BiConsumer<AbstractTypeHolder, AbstractElementHolder> fieldConsumer = (visitedType, field) -> {
            Preconditions.checkArgument(arr[0] == null);
            arr[0] = buildFromField(registeredHolder, field, name);
        };
        final BiConsumer<AbstractTypeHolder, AbstractElementHolder> methodConsumer = (visitedType, method) -> {
            final IConstExpression expression = buildFromMethod(registeredHolder, method, name);
            if (expression != null) {
                Preconditions.checkArgument(arr[0] == null);
                arr[0] = expression;
            }
        };
        PropertiesReader.enumerateProperties(type, fieldConsumer, methodConsumer, expectedModifiers, prohibitedModifiers);
        final IConstExpression result = arr[0];
        Logger.checkNotNull(result, "no member found", type.getErrorElement());
        return result;
    }


    private static IConstExpression buildFromMethod(final AbstractTypeHolder enclosingType, final AbstractElementHolder method, final String name) {
        final AbstractTypeHolder returnType = method.getReturnType();
        if (returnType == null) {
            return null;
        }
        final List<? extends AbstractTypeHolder> parameters = method.getParameters();
        if (!parameters.isEmpty()) {
            return null;
        }
        final String methodName = method.getName();
        final IConstExpression result = new ConstMember(name, returnType, enclosingType, methodName, true);
        return result;
    }

    private static IConstExpression buildFromField(final AbstractTypeHolder enclosingType, final AbstractElementHolder field, final String name) {
        final AbstractTypeHolder fieldType = field.getReturnType();
        final String fieldName = field.getName();
        final IConstExpression result = new ConstMember(name, fieldType, enclosingType, fieldName, false);
        return result;
    }

    private static <V> V getSingleFromSet(final Collection<V> set, final AbstractTypeHolder type) {
        if (set == null || set.isEmpty()) {
            return null;
        }
        final int size = set.size();
        final Element errorElement = type.getErrorElement();
        Logger.checkArgument(size == 1, "unexpected more than one element", errorElement);
        final Iterator<V> iterator = set.iterator();
        final V result = iterator.next();
        return result;
    }
    
    private static <V> V getSingleFromArray(final Object array, final AbstractTypeHolder type) {
        if (array == null) {
            return null;
        }
        final int length = Array.getLength(array);
        if (length == 0) {
            return null;
        }
        final Element errorElement = type.getErrorElement();
        Logger.checkArgument(length == 1, "unexpected more than one element", errorElement);
        @SuppressWarnings("unchecked")
        final V result = (V) Array.get(array, 0);
        return result;
    }
    
    
    @FunctionalInterface
    private interface TriFunction<A, B, C, D> {
        D apply(A a, B b, C c);
    }
}
