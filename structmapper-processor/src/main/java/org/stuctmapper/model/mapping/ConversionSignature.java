package org.stuctmapper.model.mapping;

import java.util.List;

import com.google.common.collect.ImmutableList;

public class ConversionSignature {
    private final List<ConversionSignatureItem> items;

    public ConversionSignature(List<ConversionSignatureItem> items) {
        super();
        this.items = ImmutableList.copyOf(items);
    }
    
    public List<ConversionSignatureItem> getItems() {
        return items;
    }
    
    public ConversionSignatureItem get(final int index) {
        final ConversionSignatureItem result = items.get(index);
        return result;
    }
    
    public int size() {
        final int result = items.size();
        return result;
    }
}
