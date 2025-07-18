package net.minecraft.world.level.chunk.storage;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.world.level.ChunkPos;

public interface ChunkIOErrorReporter {
    void reportChunkLoadFailure(Throwable throwable, RegionStorageInfo regionStorageInfo, ChunkPos chunkPos);

    void reportChunkSaveFailure(Throwable throwable, RegionStorageInfo regionStorageInfo, ChunkPos chunkPos);

    static ReportedException createMisplacedChunkReport(ChunkPos pos, ChunkPos expectedPos) {
        CrashReport crashreport = CrashReport.forThrowable(
            new IllegalStateException("Retrieved chunk position " + pos + " does not match requested " + expectedPos), "Chunk found in invalid location"
        );
        CrashReportCategory crashreportcategory = crashreport.addCategory("Misplaced Chunk");
        crashreportcategory.setDetail("Stored Position", pos::toString);
        return new ReportedException(crashreport);
    }

    default void reportMisplacedChunk(ChunkPos pos, ChunkPos expectedPos, RegionStorageInfo regionStorageInfo) {
        this.reportChunkLoadFailure(createMisplacedChunkReport(pos, expectedPos), regionStorageInfo, expectedPos);
    }
}
