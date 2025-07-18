package net.minecraft.client.renderer.texture.atlas.sources;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class SingleFile implements SpriteSource {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<SingleFile> CODEC = RecordCodecBuilder.mapCodec(
        p_261903_ -> p_261903_.group(
                    ResourceLocation.CODEC.fieldOf("resource").forGetter(p_261913_ -> p_261913_.resourceId),
                    ResourceLocation.CODEC.optionalFieldOf("sprite").forGetter(p_261615_ -> p_261615_.spriteId)
                )
                .apply(p_261903_, SingleFile::new)
    );
    private final ResourceLocation resourceId;
    private final Optional<ResourceLocation> spriteId;

    public SingleFile(ResourceLocation resourceId, Optional<ResourceLocation> spriteId) {
        this.resourceId = resourceId;
        this.spriteId = spriteId;
    }

    @Override
    public void run(ResourceManager resourceManager, SpriteSource.Output output) {
        ResourceLocation resourcelocation = TEXTURE_ID_CONVERTER.idToFile(this.resourceId);
        Optional<Resource> optional = resourceManager.getResource(resourcelocation);
        if (optional.isPresent()) {
            output.add(this.spriteId.orElse(this.resourceId), optional.get());
        } else {
            LOGGER.warn("Missing sprite: {}", resourcelocation);
        }
    }

    @Override
    public SpriteSourceType type() {
        return SpriteSources.SINGLE_FILE;
    }
}
