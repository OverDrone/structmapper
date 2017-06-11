package org.stuctmapper.instructions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StatementConcatenator {
    private StringBuilder template = new StringBuilder();
    private List<Object> arguments = new ArrayList<>();

    public void append(final String template) {
        this.template.append(template);
    }
    
    public void appendArgs(final String template, final Object... args) {
        this.template.append(template);
        final List<Object> argsList = Arrays.asList(args);
        arguments.addAll(argsList);
    }
    
    public String getTemplate() {
        final String result = template.toString();
        return result;
    }
    
    public Object[] getArguments() {
        final Object[] result = arguments.toArray();
        return result;
    }
}
