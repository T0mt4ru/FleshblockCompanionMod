package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ZombieVillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ZombieVillagerRenderer extends HumanoidMobRenderer<ZombieVillager, ZombieVillagerModel<ZombieVillager>> {
    private static final ResourceLocation ZOMBIE_VILLAGER_LOCATION = ResourceLocation.withDefaultNamespace(
        "textures/entity/zombie_villager/zombie_villager.png"
    );

    public ZombieVillagerRenderer(EntityRendererProvider.Context p_174463_) {
        super(p_174463_, new ZombieVillagerModel<>(p_174463_.bakeLayer(ModelLayers.ZOMBIE_VILLAGER)), 0.5F);
        this.addLayer(
            new HumanoidArmorLayer<>(
                this,
                new ZombieVillagerModel(p_174463_.bakeLayer(ModelLayers.ZOMBIE_VILLAGER_INNER_ARMOR)),
                new ZombieVillagerModel(p_174463_.bakeLayer(ModelLayers.ZOMBIE_VILLAGER_OUTER_ARMOR)),
                p_174463_.getModelManager()
            )
        );
        this.addLayer(new VillagerProfessionLayer<>(this, p_174463_.getResourceManager(), "zombie_villager"));
    }

    /**
     * Returns the location of an entity's texture.
     */
    public ResourceLocation getTextureLocation(ZombieVillager entity) {
        return ZOMBIE_VILLAGER_LOCATION;
    }

    protected boolean isShaking(ZombieVillager entity) {
        return super.isShaking(entity) || entity.isConverting();
    }
}
