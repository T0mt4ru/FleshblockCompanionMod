package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.IOException;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.VillagerHeadModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.resources.metadata.animation.VillagerMetaDataSection;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VillagerProfessionLayer<T extends LivingEntity & VillagerDataHolder, M extends EntityModel<T> & VillagerHeadModel> extends RenderLayer<T, M> {
    private static final Int2ObjectMap<ResourceLocation> LEVEL_LOCATIONS = Util.make(new Int2ObjectOpenHashMap<>(), p_349909_ -> {
        p_349909_.put(1, ResourceLocation.withDefaultNamespace("stone"));
        p_349909_.put(2, ResourceLocation.withDefaultNamespace("iron"));
        p_349909_.put(3, ResourceLocation.withDefaultNamespace("gold"));
        p_349909_.put(4, ResourceLocation.withDefaultNamespace("emerald"));
        p_349909_.put(5, ResourceLocation.withDefaultNamespace("diamond"));
    });
    private final Object2ObjectMap<VillagerType, VillagerMetaDataSection.Hat> typeHatCache = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectMap<VillagerProfession, VillagerMetaDataSection.Hat> professionHatCache = new Object2ObjectOpenHashMap<>();
    private final ResourceManager resourceManager;
    private final String path;

    public VillagerProfessionLayer(RenderLayerParent<T, M> renderer, ResourceManager resourceManager, String path) {
        super(renderer);
        this.resourceManager = resourceManager;
        this.path = path;
    }

    public void render(
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        T livingEntity,
        float limbSwing,
        float limbSwingAmount,
        float partialTicks,
        float ageInTicks,
        float netHeadYaw,
        float headPitch
    ) {
        if (!livingEntity.isInvisible()) {
            VillagerData villagerdata = livingEntity.getVillagerData();
            VillagerType villagertype = villagerdata.getType();
            VillagerProfession villagerprofession = villagerdata.getProfession();
            VillagerMetaDataSection.Hat villagermetadatasection$hat = this.getHatData(this.typeHatCache, "type", BuiltInRegistries.VILLAGER_TYPE, villagertype);
            VillagerMetaDataSection.Hat villagermetadatasection$hat1 = this.getHatData(
                this.professionHatCache, "profession", BuiltInRegistries.VILLAGER_PROFESSION, villagerprofession
            );
            M m = this.getParentModel();
            m.hatVisible(
                villagermetadatasection$hat1 == VillagerMetaDataSection.Hat.NONE
                    || villagermetadatasection$hat1 == VillagerMetaDataSection.Hat.PARTIAL && villagermetadatasection$hat != VillagerMetaDataSection.Hat.FULL
            );
            ResourceLocation resourcelocation = this.getResourceLocation("type", BuiltInRegistries.VILLAGER_TYPE.getKey(villagertype));
            renderColoredCutoutModel(m, resourcelocation, poseStack, buffer, packedLight, livingEntity, -1);
            m.hatVisible(true);
            if (villagerprofession != VillagerProfession.NONE && !livingEntity.isBaby()) {
                ResourceLocation resourcelocation1 = this.getResourceLocation("profession", BuiltInRegistries.VILLAGER_PROFESSION.getKey(villagerprofession));
                renderColoredCutoutModel(m, resourcelocation1, poseStack, buffer, packedLight, livingEntity, -1);
                if (villagerprofession != VillagerProfession.NITWIT) {
                    ResourceLocation resourcelocation2 = this.getResourceLocation(
                        "profession_level", LEVEL_LOCATIONS.get(Mth.clamp(villagerdata.getLevel(), 1, LEVEL_LOCATIONS.size()))
                    );
                    renderColoredCutoutModel(m, resourcelocation2, poseStack, buffer, packedLight, livingEntity, -1);
                }
            }
        }
    }

    private ResourceLocation getResourceLocation(String folder, ResourceLocation location) {
        return location.withPath(p_247944_ -> "textures/entity/" + this.path + "/" + folder + "/" + p_247944_ + ".png");
    }

    public <K> VillagerMetaDataSection.Hat getHatData(
        Object2ObjectMap<K, VillagerMetaDataSection.Hat> cache, String folder, DefaultedRegistry<K> villagerTypeRegistry, K key
    ) {
        return cache.computeIfAbsent(
            key, p_258159_ -> this.resourceManager.getResource(this.getResourceLocation(folder, villagerTypeRegistry.getKey(key))).flatMap(p_234875_ -> {
                    try {
                        return p_234875_.metadata().getSection(VillagerMetaDataSection.SERIALIZER).map(VillagerMetaDataSection::getHat);
                    } catch (IOException ioexception) {
                        return Optional.empty();
                    }
                }).orElse(VillagerMetaDataSection.Hat.NONE)
        );
    }
}
