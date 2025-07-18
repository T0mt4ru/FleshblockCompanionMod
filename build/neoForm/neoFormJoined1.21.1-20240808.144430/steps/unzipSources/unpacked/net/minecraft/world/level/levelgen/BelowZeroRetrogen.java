package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.BitSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.LongStream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public final class BelowZeroRetrogen {
    private static final BitSet EMPTY = new BitSet(0);
    private static final Codec<BitSet> BITSET_CODEC = Codec.LONG_STREAM
        .xmap(p_188484_ -> BitSet.valueOf(p_188484_.toArray()), p_188482_ -> LongStream.of(p_188482_.toLongArray()));
    private static final Codec<ChunkStatus> NON_EMPTY_CHUNK_STATUS = BuiltInRegistries.CHUNK_STATUS
        .byNameCodec()
        .comapFlatMap(
            p_330147_ -> p_330147_ == ChunkStatus.EMPTY ? DataResult.error(() -> "target_status cannot be empty") : DataResult.success((ChunkStatus)p_330147_),
            Function.identity()
        );
    public static final Codec<BelowZeroRetrogen> CODEC = RecordCodecBuilder.create(
        p_338091_ -> p_338091_.group(
                    NON_EMPTY_CHUNK_STATUS.fieldOf("target_status").forGetter(BelowZeroRetrogen::targetStatus),
                    BITSET_CODEC.lenientOptionalFieldOf("missing_bedrock")
                        .forGetter(p_188480_ -> p_188480_.missingBedrock.isEmpty() ? Optional.empty() : Optional.of(p_188480_.missingBedrock))
                )
                .apply(p_338091_, BelowZeroRetrogen::new)
    );
    private static final Set<ResourceKey<Biome>> RETAINED_RETROGEN_BIOMES = Set.of(Biomes.LUSH_CAVES, Biomes.DRIPSTONE_CAVES, Biomes.DEEP_DARK);
    public static final LevelHeightAccessor UPGRADE_HEIGHT_ACCESSOR = new LevelHeightAccessor() {
        @Override
        public int getHeight() {
            return 64;
        }

        @Override
        public int getMinBuildHeight() {
            return -64;
        }
    };
    private final ChunkStatus targetStatus;
    private final BitSet missingBedrock;

    private BelowZeroRetrogen(ChunkStatus targetStatus, Optional<BitSet> missingBedrock) {
        this.targetStatus = targetStatus;
        this.missingBedrock = missingBedrock.orElse(EMPTY);
    }

    @Nullable
    public static BelowZeroRetrogen read(CompoundTag tag) {
        ChunkStatus chunkstatus = ChunkStatus.byName(tag.getString("target_status"));
        return chunkstatus == ChunkStatus.EMPTY
            ? null
            : new BelowZeroRetrogen(chunkstatus, Optional.of(BitSet.valueOf(tag.getLongArray("missing_bedrock"))));
    }

    public static void replaceOldBedrock(ProtoChunk chunk) {
        int i = 4;
        BlockPos.betweenClosed(0, 0, 0, 15, 4, 15).forEach(p_188492_ -> {
            if (chunk.getBlockState(p_188492_).is(Blocks.BEDROCK)) {
                chunk.setBlockState(p_188492_, Blocks.DEEPSLATE.defaultBlockState(), false);
            }
        });
    }

    public void applyBedrockMask(ProtoChunk chunk) {
        LevelHeightAccessor levelheightaccessor = chunk.getHeightAccessorForGeneration();
        int i = levelheightaccessor.getMinBuildHeight();
        int j = levelheightaccessor.getMaxBuildHeight() - 1;

        for (int k = 0; k < 16; k++) {
            for (int l = 0; l < 16; l++) {
                if (this.hasBedrockHole(k, l)) {
                    BlockPos.betweenClosed(k, i, l, k, j, l).forEach(p_198219_ -> chunk.setBlockState(p_198219_, Blocks.AIR.defaultBlockState(), false));
                }
            }
        }
    }

    public ChunkStatus targetStatus() {
        return this.targetStatus;
    }

    public boolean hasBedrockHoles() {
        return !this.missingBedrock.isEmpty();
    }

    public boolean hasBedrockHole(int x, int z) {
        return this.missingBedrock.get((z & 15) * 16 + (x & 15));
    }

    public static BiomeResolver getBiomeResolver(BiomeResolver resolver, ChunkAccess access) {
        if (!access.isUpgrading()) {
            return resolver;
        } else {
            Predicate<ResourceKey<Biome>> predicate = RETAINED_RETROGEN_BIOMES::contains;
            return (p_204538_, p_204539_, p_204540_, p_204541_) -> {
                Holder<Biome> holder = resolver.getNoiseBiome(p_204538_, p_204539_, p_204540_, p_204541_);
                return holder.is(predicate) ? holder : access.getNoiseBiome(p_204538_, 0, p_204540_);
            };
        }
    }
}
