package net.minecraft.client.model.geom;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EntityModelSet implements ResourceManagerReloadListener {
    private Map<ModelLayerLocation, LayerDefinition> roots = ImmutableMap.of();

    public ModelPart bakeLayer(ModelLayerLocation modelLayerLocation) {
        LayerDefinition layerdefinition = this.roots.get(modelLayerLocation);
        if (layerdefinition == null) {
            throw new IllegalArgumentException("No model for layer " + modelLayerLocation);
        } else {
            return layerdefinition.bakeRoot();
        }
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        this.roots = ImmutableMap.copyOf(LayerDefinitions.createRoots());
    }
}
