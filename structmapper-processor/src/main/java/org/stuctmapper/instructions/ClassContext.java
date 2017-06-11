package org.stuctmapper.instructions;

import java.util.HashMap;
import java.util.Map;

import org.stuctmapper.model.holder.AbstractTypeHolder;
import org.stuctmapper.properties.PropertiesReader;
import org.stuctmapper.utils.CollectionUtils;

public class ClassContext {
    private final VariableNameGenerator referenceNameGenerator = new VariableNameGenerator("ref");
    private final Map<AbstractTypeHolder, String> referenceMap = new HashMap<>();
    private final AbstractTypeHolder type;
    
    public ClassContext(final AbstractTypeHolder type) {
        this.type = type;
    }
    
    public AbstractTypeHolder getType() {
        return type;
    }

    public String addReference(final AbstractTypeHolder type) {
        final String existing = referenceMap.get(type);
        if (existing != null) {
            return existing;
        } else {
            final String typeName = type.getClassSimpleName();
            final String camelName = PropertiesReader.pascalToCamel(typeName);
            final String name = referenceNameGenerator.getNext(camelName);
            CollectionUtils.addNew(referenceMap, type, name);
            return name;
        }
    }
    
    public Map<AbstractTypeHolder, String> getReferenceMap() {
        return referenceMap;
    }
}
