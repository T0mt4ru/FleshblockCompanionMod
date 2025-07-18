package net.minecraft.client.resources;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MapDecorationTextureManager extends TextureAtlasHolder {
    public MapDecorationTextureManager(TextureManager textureManager) {
        super(textureManager, ResourceLocation.withDefaultNamespace("textures/atlas/map_decorations.png"), ResourceLocation.withDefaultNamespace("map_decorations"));
    }

    public TextureAtlasSprite get(MapDecoration mapDecoration) {
        return this.getSprite(mapDecoration.getSpriteLocation());
    }
}
