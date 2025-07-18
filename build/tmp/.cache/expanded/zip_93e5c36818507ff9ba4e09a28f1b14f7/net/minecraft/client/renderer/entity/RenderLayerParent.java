package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface RenderLayerParent<T extends Entity, M extends EntityModel<T>> {
    M getModel();

    /**
     * Returns the location of an entity's texture.
     */
    ResourceLocation getTextureLocation(T entity);
}
