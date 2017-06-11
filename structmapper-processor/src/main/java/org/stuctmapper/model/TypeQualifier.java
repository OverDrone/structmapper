package org.stuctmapper.model;

public class TypeQualifier {
    final String packageName;
    final String prefix;
    final String name;
    
    public TypeQualifier(final String packageName, final String prefix, final String name) {
        super();
        this.packageName = packageName;
        this.prefix = prefix;
        this.name = name;
    }
    
    public String getPackageName() {
        return packageName;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }
}
