package net.minecraft.util;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import java.util.function.Function;

public interface ToFloatFunction<C> {
    ToFloatFunction<Float> IDENTITY = createUnlimited(p_216474_ -> p_216474_);

    float apply(C object);

    float minValue();

    float maxValue();

    static ToFloatFunction<Float> createUnlimited(final Float2FloatFunction wrapped) {
        return new ToFloatFunction<Float>() {
            public float apply(Float p_216483_) {
                return wrapped.apply(p_216483_);
            }

            @Override
            public float minValue() {
                return Float.NEGATIVE_INFINITY;
            }

            @Override
            public float maxValue() {
                return Float.POSITIVE_INFINITY;
            }
        };
    }

    default <C2> ToFloatFunction<C2> comap(final Function<C2, C> converter) {
        final ToFloatFunction<C> tofloatfunction = this;
        return new ToFloatFunction<C2>() {
            @Override
            public float apply(C2 p_216496_) {
                return tofloatfunction.apply(converter.apply(p_216496_));
            }

            @Override
            public float minValue() {
                return tofloatfunction.minValue();
            }

            @Override
            public float maxValue() {
                return tofloatfunction.maxValue();
            }
        };
    }
}
