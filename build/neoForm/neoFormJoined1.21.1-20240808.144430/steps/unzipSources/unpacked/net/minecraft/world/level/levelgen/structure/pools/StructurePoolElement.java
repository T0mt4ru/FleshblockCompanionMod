package net.minecraft.world.level.levelgen.structure.pools;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public abstract class StructurePoolElement {
    public static final Codec<StructurePoolElement> CODEC = BuiltInRegistries.STRUCTURE_POOL_ELEMENT
        .byNameCodec()
        .dispatch("element_type", StructurePoolElement::getType, StructurePoolElementType::codec);
    private static final Holder<StructureProcessorList> EMPTY = Holder.direct(new StructureProcessorList(List.of()));
    @Nullable
    private volatile StructureTemplatePool.Projection projection;

    protected static <E extends StructurePoolElement> RecordCodecBuilder<E, StructureTemplatePool.Projection> projectionCodec() {
        return StructureTemplatePool.Projection.CODEC.fieldOf("projection").forGetter(StructurePoolElement::getProjection);
    }

    protected StructurePoolElement(StructureTemplatePool.Projection projection) {
        this.projection = projection;
    }

    public abstract Vec3i getSize(StructureTemplateManager structureTemplateManager, Rotation rotation);

    public abstract List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(
        StructureTemplateManager structureTemplateManager, BlockPos pos, Rotation rotation, RandomSource random
    );

    public abstract BoundingBox getBoundingBox(StructureTemplateManager structureTemplateManager, BlockPos pos, Rotation rotation);

    public abstract boolean place(
        StructureTemplateManager structureTemplateManager,
        WorldGenLevel level,
        StructureManager structureManager,
        ChunkGenerator generator,
        BlockPos offset,
        BlockPos pos,
        Rotation rotation,
        BoundingBox box,
        RandomSource random,
        LiquidSettings liquidSettings,
        boolean keepJigsaws
    );

    public abstract StructurePoolElementType<?> getType();

    public void handleDataMarker(
        LevelAccessor level,
        StructureTemplate.StructureBlockInfo blockInfo,
        BlockPos pos,
        Rotation rotation,
        RandomSource random,
        BoundingBox box
    ) {
    }

    public StructurePoolElement setProjection(StructureTemplatePool.Projection projection) {
        this.projection = projection;
        return this;
    }

    public StructureTemplatePool.Projection getProjection() {
        StructureTemplatePool.Projection structuretemplatepool$projection = this.projection;
        if (structuretemplatepool$projection == null) {
            throw new IllegalStateException();
        } else {
            return structuretemplatepool$projection;
        }
    }

    public int getGroundLevelDelta() {
        return 1;
    }

    public static Function<StructureTemplatePool.Projection, EmptyPoolElement> empty() {
        return p_210525_ -> EmptyPoolElement.INSTANCE;
    }

    public static Function<StructureTemplatePool.Projection, LegacySinglePoolElement> legacy(String id) {
        return p_352020_ -> new LegacySinglePoolElement(Either.left(ResourceLocation.parse(id)), EMPTY, p_352020_, Optional.empty());
    }

    public static Function<StructureTemplatePool.Projection, LegacySinglePoolElement> legacy(String id, Holder<StructureProcessorList> processors) {
        return p_352029_ -> new LegacySinglePoolElement(Either.left(ResourceLocation.parse(id)), processors, p_352029_, Optional.empty());
    }

    public static Function<StructureTemplatePool.Projection, SinglePoolElement> single(String id) {
        return p_352026_ -> new SinglePoolElement(Either.left(ResourceLocation.parse(id)), EMPTY, p_352026_, Optional.empty());
    }

    public static Function<StructureTemplatePool.Projection, SinglePoolElement> single(String id, Holder<StructureProcessorList> processors) {
        return p_352035_ -> new SinglePoolElement(Either.left(ResourceLocation.parse(id)), processors, p_352035_, Optional.empty());
    }

    public static Function<StructureTemplatePool.Projection, SinglePoolElement> single(String id, LiquidSettings liquidSettings) {
        return p_352032_ -> new SinglePoolElement(Either.left(ResourceLocation.parse(id)), EMPTY, p_352032_, Optional.of(liquidSettings));
    }

    public static Function<StructureTemplatePool.Projection, SinglePoolElement> single(
        String id, Holder<StructureProcessorList> processors, LiquidSettings liquidSettings
    ) {
        return p_352024_ -> new SinglePoolElement(Either.left(ResourceLocation.parse(id)), processors, p_352024_, Optional.of(liquidSettings));
    }

    public static Function<StructureTemplatePool.Projection, FeaturePoolElement> feature(Holder<PlacedFeature> feature) {
        return p_210506_ -> new FeaturePoolElement(feature, p_210506_);
    }

    public static Function<StructureTemplatePool.Projection, ListPoolElement> list(
        List<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>> elements
    ) {
        return p_210523_ -> new ListPoolElement(elements.stream().map(p_210482_ -> p_210482_.apply(p_210523_)).collect(Collectors.toList()), p_210523_);
    }
}
