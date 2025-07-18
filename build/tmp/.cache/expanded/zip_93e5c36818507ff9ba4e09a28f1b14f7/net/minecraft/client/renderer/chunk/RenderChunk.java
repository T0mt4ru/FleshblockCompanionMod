package net.minecraft.client.renderer.chunk;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
class RenderChunk {
    private final Map<BlockPos, BlockEntity> blockEntities;
    @Nullable
    private final List<PalettedContainer<BlockState>> sections;
    private final boolean debug;
    final LevelChunk wrapped;

    RenderChunk(LevelChunk wrapped) {
        this.wrapped = wrapped;
        this.debug = wrapped.getLevel().isDebug();
        this.blockEntities = ImmutableMap.copyOf(wrapped.getBlockEntities());
        if (wrapped instanceof EmptyLevelChunk) {
            this.sections = null;
        } else {
            LevelChunkSection[] alevelchunksection = wrapped.getSections();
            this.sections = new ArrayList<>(alevelchunksection.length);

            for (LevelChunkSection levelchunksection : alevelchunksection) {
                this.sections.add(levelchunksection.hasOnlyAir() ? null : levelchunksection.getStates().copy());
            }
        }
    }

    @Nullable
    public BlockEntity getBlockEntity(BlockPos pos) {
        return this.blockEntities.get(pos);
    }

    public BlockState getBlockState(BlockPos pos) {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        if (this.debug) {
            BlockState blockstate = null;
            if (j == 60) {
                blockstate = Blocks.BARRIER.defaultBlockState();
            }

            if (j == 70) {
                blockstate = DebugLevelSource.getBlockStateFor(i, k);
            }

            return blockstate == null ? Blocks.AIR.defaultBlockState() : blockstate;
        } else if (this.sections == null) {
            return Blocks.AIR.defaultBlockState();
        } else {
            try {
                int l = this.wrapped.getSectionIndex(j);
                if (l >= 0 && l < this.sections.size()) {
                    PalettedContainer<BlockState> palettedcontainer = this.sections.get(l);
                    if (palettedcontainer != null) {
                        return palettedcontainer.get(i & 15, j & 15, k & 15);
                    }
                }

                return Blocks.AIR.defaultBlockState();
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Getting block state");
                CrashReportCategory crashreportcategory = crashreport.addCategory("Block being got");
                crashreportcategory.setDetail("Location", () -> CrashReportCategory.formatLocation(this.wrapped, i, j, k));
                throw new ReportedException(crashreport);
            }
        }
    }
}
