package org.stuctmapper.utils;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.stuctmapper.instructions.StatementConcatenator;
import org.stuctmapper.log.Logger;
import org.stuctmapper.model.TypeQualifier;

import com.google.common.base.Preconditions;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;

public final class JavaBuilderUtils {
    
    private JavaBuilderUtils() {
    }

    public static ClassName toClassName(final TypeElement type) {
        if (type == null) {
            return null;
        }
        final TypeQualifier qualifier = getQualifier(type);
        final String packageName = qualifier.getPackageName();
        final String prefix = qualifier.getPrefix();
        final String name = qualifier.getName();
        final String qualifiedName = prefix + name;
        final ClassName result = ClassName.get(packageName, qualifiedName);
        return result;
    }
    
    
    public static TypeQualifier getQualifier(final Element type) {
        final TypeQualifier result = getQualifier(type, null, null);
        return result;
    }
    
    private static TypeQualifier getQualifier(final Element type, final String prefix, final String name) {
        final ElementKind kind = type.getKind();
        final TypeQualifier result;
        switch (kind) {
        case PACKAGE:
            final PackageElement packageElement = (PackageElement) type;
            final Name packageName = packageElement.getQualifiedName();
            final String packageNameString = packageName.toString();
            result = new TypeQualifier(packageNameString, prefix, name);
            return result;
        case CLASS:
        case INTERFACE:
        case ENUM:
            final Name typeName = type.getSimpleName();
            final String recurseName;
            final String recursePrefix;
            if (name == null) {
                Logger.checkArgument(prefix == null, type);
                recurseName = typeName.toString();
                recursePrefix = "";
            } else {
                Logger.checkArgument(prefix != null, type);
                recursePrefix = typeName + "." + prefix;
                recurseName = name;
            }
            final Element parent = type.getEnclosingElement();
            result = getQualifier(parent, recursePrefix, recurseName);
            return result;
        default:
            return Logger.fail("Unexpected kind=" + kind + " for type " + type, type);
        }
    }
    
    public static String getImplementationName(final String name, final boolean isInterface, final Element errorElement) {
        Logger.checkArgument(name != null && !name.isEmpty(), errorElement);
        if (isInterface) {
            if (name.startsWith("I") && name.length() > 1 && Character.isUpperCase(name.charAt(1))) {
                final String result = name.substring(1);
                return result;
            } else {
                final String result = name + "Impl";
                return result;
            }
        } else {
            final String abstractPrefix = "Abstract"; 
            if (name.startsWith(abstractPrefix)) {
                final String result = name.substring(abstractPrefix.length());
                return result;
            } else {
                final String result = name + "Impl";
                return result;
            }
        }
    }
    
    public static TypeElement toTypeElement(final TypeMirror mirror) {
        final TypeElement result = toTypeElement(mirror, true);
        return result;
    }
    
    public static TypeElement toTypeElement(final TypeMirror mirror, final boolean expected) {
        final TypeKind mirrorKind = mirror.getKind();
        if (mirrorKind == TypeKind.NONE || mirrorKind == TypeKind.VOID) {
            return null;
        }
        if (mirrorKind != TypeKind.DECLARED) {
            Preconditions.checkArgument(!expected, "unexpected %s", mirrorKind);
            return null;
        }
        final DeclaredType declared = (DeclaredType) mirror;
        final Element element = declared.asElement();
        final TypeElement result = toTypeElement(element, expected);
        return result;
    }
    
    public static TypeElement toTypeElement(final Element element, final boolean expected) {
        final ElementKind kind = element.getKind();
        if (kind != ElementKind.CLASS && kind != ElementKind.INTERFACE) {
            Preconditions.checkArgument(!expected, kind);
            return null;
        }
        final TypeElement result = (TypeElement) element;
        return result;
    }

    public static void buildSuperCall(final Iterable<String> parameters, final CodeBlock.Builder builder) {
        final StatementConcatenator concatenator = new StatementConcatenator();
        JavaBuilderUtils.appendSuperCall(concatenator, parameters);
        final String template = concatenator.getTemplate();
        final Object[] arguments = concatenator.getArguments();
        builder.addStatement(template, arguments);
    }

    public static void appendSuperCall(final StatementConcatenator concatenator, final Iterable<String> parameters) {
        appendSuperCall(concatenator, null, null, parameters);
    }
    
    public static void appendSuperCall(final StatementConcatenator concatenator, final String interfaceName, 
            final String methodName, final Iterable<String> parameters) {
        if (interfaceName != null) {
            concatenator.appendArgs("$N.", interfaceName);
        }
        if (methodName != null) {
            concatenator.appendArgs("super.$N(", methodName);
        } else {
            concatenator.append("super(");
        }
        boolean first = true;
        for (final String parameter : parameters) {
            if (first) {
                first = false;
            } else {
                concatenator.append(", ");
            }
            concatenator.appendArgs("$N", parameter);
        }
        concatenator.append(")");
    }

    public static boolean isInterface(final TypeElement type) {
        final ElementKind kind = type.getKind();
        final boolean result = kind == ElementKind.INTERFACE;
        return result;
    }
}
