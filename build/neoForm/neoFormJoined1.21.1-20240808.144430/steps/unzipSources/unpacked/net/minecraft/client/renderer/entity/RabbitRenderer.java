package net.minecraft.client.renderer.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.client.model.RabbitModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Rabbit;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RabbitRenderer extends MobRenderer<Rabbit, RabbitModel<Rabbit>> {
    private static final ResourceLocation RABBIT_BROWN_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/rabbit/brown.png");
    private static final ResourceLocation RABBIT_WHITE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/rabbit/white.png");
    private static final ResourceLocation RABBIT_BLACK_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/rabbit/black.png");
    private static final ResourceLocation RABBIT_GOLD_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/rabbit/gold.png");
    private static final ResourceLocation RABBIT_SALT_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/rabbit/salt.png");
    private static final ResourceLocation RABBIT_WHITE_SPLOTCHED_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/rabbit/white_splotched.png");
    private static final ResourceLocation RABBIT_TOAST_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/rabbit/toast.png");
    private static final ResourceLocation RABBIT_EVIL_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/rabbit/caerbannog.png");

    public RabbitRenderer(EntityRendererProvider.Context p_174360_) {
        super(p_174360_, new RabbitModel<>(p_174360_.bakeLayer(ModelLayers.RABBIT)), 0.3F);
    }

    /**
     * Returns the location of an entity's texture.
     */
    public ResourceLocation getTextureLocation(Rabbit entity) {
        String s = ChatFormatting.stripFormatting(entity.getName().getString());
        if ("Toast".equals(s)) {
            return RABBIT_TOAST_LOCATION;
        } else {
            return switch (entity.getVariant()) {
                case BROWN -> RABBIT_BROWN_LOCATION;
                case WHITE -> RABBIT_WHITE_LOCATION;
                case BLACK -> RABBIT_BLACK_LOCATION;
                case GOLD -> RABBIT_GOLD_LOCATION;
                case SALT -> RABBIT_SALT_LOCATION;
                case WHITE_SPLOTCHED -> RABBIT_WHITE_SPLOTCHED_LOCATION;
                case EVIL -> RABBIT_EVIL_LOCATION;
            };
        }
    }
}
