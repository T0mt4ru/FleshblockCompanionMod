package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;

public record MapDecorations(Map<String, MapDecorations.Entry> decorations) {
    public static final MapDecorations EMPTY = new MapDecorations(Map.of());
    public static final Codec<MapDecorations> CODEC = Codec.unboundedMap(Codec.STRING, MapDecorations.Entry.CODEC)
        .xmap(MapDecorations::new, MapDecorations::decorations);

    public MapDecorations withDecoration(String type, MapDecorations.Entry entry) {
        return new MapDecorations(Util.copyAndPut(this.decorations, type, entry));
    }

    public static record Entry(Holder<MapDecorationType> type, double x, double z, float rotation) {
        public static final Codec<MapDecorations.Entry> CODEC = RecordCodecBuilder.create(
            p_335287_ -> p_335287_.group(
                        MapDecorationType.CODEC.fieldOf("type").forGetter(MapDecorations.Entry::type),
                        Codec.DOUBLE.fieldOf("x").forGetter(MapDecorations.Entry::x),
                        Codec.DOUBLE.fieldOf("z").forGetter(MapDecorations.Entry::z),
                        Codec.FLOAT.fieldOf("rotation").forGetter(MapDecorations.Entry::rotation)
                    )
                    .apply(p_335287_, MapDecorations.Entry::new)
        );
    }
}
