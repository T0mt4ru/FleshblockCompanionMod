package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public class BastionPieces {
    public static final ResourceKey<StructureTemplatePool> START = Pools.createKey("bastion/starts");

    public static void bootstrap(BootstrapContext<StructureTemplatePool> context) {
        HolderGetter<StructureProcessorList> holdergetter = context.lookup(Registries.PROCESSOR_LIST);
        Holder<StructureProcessorList> holder = holdergetter.getOrThrow(ProcessorLists.BASTION_GENERIC_DEGRADATION);
        HolderGetter<StructureTemplatePool> holdergetter1 = context.lookup(Registries.TEMPLATE_POOL);
        Holder<StructureTemplatePool> holder1 = holdergetter1.getOrThrow(Pools.EMPTY);
        context.register(
            START,
            new StructureTemplatePool(
                holder1,
                ImmutableList.of(
                    Pair.of(StructurePoolElement.single("bastion/units/air_base", holder), 1),
                    Pair.of(StructurePoolElement.single("bastion/hoglin_stable/air_base", holder), 1),
                    Pair.of(StructurePoolElement.single("bastion/treasure/big_air_full", holder), 1),
                    Pair.of(StructurePoolElement.single("bastion/bridge/starting_pieces/entrance_base", holder), 1)
                ),
                StructureTemplatePool.Projection.RIGID
            )
        );
        BastionHousingUnitsPools.bootstrap(context);
        BastionHoglinStablePools.bootstrap(context);
        BastionTreasureRoomPools.bootstrap(context);
        BastionBridgePools.bootstrap(context);
        BastionSharedPools.bootstrap(context);
    }
}
