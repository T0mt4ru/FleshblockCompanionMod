package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import java.util.List;
import java.util.Map;
import net.minecraft.Util;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SectionBufferBuilderPack implements AutoCloseable {
    private static final List<RenderType> RENDER_TYPES = RenderType.chunkBufferLayers();
    public static final int TOTAL_BUFFERS_SIZE = RENDER_TYPES.stream().mapToInt(RenderType::bufferSize).sum();
    private final Map<RenderType, ByteBufferBuilder> buffers = Util.make(new Reference2ObjectArrayMap<>(RENDER_TYPES.size()), p_349874_ -> {
        for (RenderType rendertype : RENDER_TYPES) {
            p_349874_.put(rendertype, new ByteBufferBuilder(rendertype.bufferSize()));
        }
    });

    public ByteBufferBuilder buffer(RenderType renderType) {
        return this.buffers.get(renderType);
    }

    public void clearAll() {
        this.buffers.values().forEach(ByteBufferBuilder::clear);
    }

    public void discardAll() {
        this.buffers.values().forEach(ByteBufferBuilder::discard);
    }

    @Override
    public void close() {
        this.buffers.values().forEach(ByteBufferBuilder::close);
    }
}
