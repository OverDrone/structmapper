package org.stuctmapper.model.instructions;

import java.util.List;
import java.util.function.BiConsumer;

import org.stuctmapper.instructions.BuildMethodContext;

import com.google.common.base.Preconditions;

public class PropertyAccessorAggregator {
    
    public static boolean buildAccessors(final List<TargetPathMatchResult> list, 
            final BiConsumer<ITargetPropertyInitializer[], BuildMethodContext> consumer, 
            final Integer exludedIndex) {
        final int size = list.size();
        final int expectedSize;
        if (exludedIndex != null) {
            expectedSize = size - 1;
        } else {
            expectedSize = size;
        }
        if (expectedSize == 0) {
            return false;
        }
        final ITargetPropertyInitializer[] variableNames = new ITargetPropertyInitializer[size];
        final TriConsumer<ITargetPropertyInitializer, Integer, BuildMethodContext> variableConsumer = 
                new TriConsumer<ITargetPropertyInitializer, Integer, BuildMethodContext>() {
            int count = 0;
            @Override
            public void accept(final ITargetPropertyInitializer initializer, final Integer variableIndex, final BuildMethodContext context) {
                Preconditions.checkArgument(variableNames[variableIndex] == null);
                variableNames[variableIndex] = initializer;
                count++;
                if (count == expectedSize) {
                    consumer.accept(variableNames, context);
                }
            }
        }; 
        int index = 0;
        for (final TargetPathMatchResult item : list) {
            if (exludedIndex == null || index != exludedIndex) {
                final TargetInfo info = item.getInfo();
                final PropertyAccessor accessor = new PropertyAccessor(variableConsumer, index);
                info.registerPropertyAccessor(accessor);
            }
            index++;
        }
        return true;
    }

    private static final class PropertyAccessor implements ITargetPropertyAccessor {
        private final int index;
        private final TriConsumer<ITargetPropertyInitializer, Integer, BuildMethodContext> consumer;

        PropertyAccessor(final TriConsumer<ITargetPropertyInitializer, Integer, BuildMethodContext> consumer, final int index) {
            this.index = index;
            this.consumer = consumer;
        }

        @Override
        public void access(final BuildMethodContext context, final ITargetPropertyInitializer initializer, final TargetPropertyAccessorStage stage) {
            switch (stage) {
            case FINISH:
                consumer.accept(initializer, index, context);
                break;

            default:
                break;
            }
        }
    }
    
    @FunctionalInterface
    private interface TriConsumer<A, B, C> {
        void accept(A a, B b, C c);
    }
}
