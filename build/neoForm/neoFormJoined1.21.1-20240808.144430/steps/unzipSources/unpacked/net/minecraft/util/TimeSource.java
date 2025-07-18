package net.minecraft.util;

import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

@FunctionalInterface
public interface TimeSource {
    long get(TimeUnit unit);

    public interface NanoTimeSource extends TimeSource, LongSupplier {
        @Override
        default long get(TimeUnit p_239379_) {
            return p_239379_.convert(this.getAsLong(), TimeUnit.NANOSECONDS);
        }
    }
}
