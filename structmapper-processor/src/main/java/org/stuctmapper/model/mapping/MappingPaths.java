package org.stuctmapper.model.mapping;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;

public class MappingPaths {
    private List<MappingPath> sources;
    private List<MappingPath> targets;
    
    public MappingPaths(final Collection<MappingPath> sources, final Collection<MappingPath> targets) {
        this.sources = ImmutableList.copyOf(sources);
        this.targets = ImmutableList.copyOf(targets);
    }

    public List<MappingPath> getSources() {
        return sources;
    }
    
    public List<MappingPath> getTargets() {
        return targets;
    }
}
