package net.minecraft.world.level.storage;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.FileUtil;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtFormatException;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.visitors.FieldSelector;
import net.minecraft.nbt.visitors.SkipFields;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.util.DirectoryLock;
import net.minecraft.util.MemoryReserve;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.validation.ContentValidationException;
import net.minecraft.world.level.validation.DirectoryValidator;
import net.minecraft.world.level.validation.ForbiddenSymlinkInfo;
import net.minecraft.world.level.validation.PathAllowList;
import org.slf4j.Logger;

public class LevelStorageSource {
    static final Logger LOGGER = LogUtils.getLogger();
    static final DateTimeFormatter FORMATTER = FileNameDateFormatter.create();
    private static final String TAG_DATA = "Data";
    private static final PathMatcher NO_SYMLINKS_ALLOWED = p_294087_ -> false;
    public static final String ALLOWED_SYMLINKS_CONFIG_NAME = "allowed_symlinks.txt";
    private static final int UNCOMPRESSED_NBT_QUOTA = 104857600;
    private static final int DISK_SPACE_WARNING_THRESHOLD = 67108864;
    private final Path baseDir;
    private final Path backupDir;
    final DataFixer fixerUpper;
    private final DirectoryValidator worldDirValidator;

    public LevelStorageSource(Path baseDir, Path backupDir, DirectoryValidator worldDirValidator, DataFixer fixerUpper) {
        this.fixerUpper = fixerUpper;

        try {
            FileUtil.createDirectoriesSafe(baseDir);
        } catch (IOException ioexception) {
            throw new UncheckedIOException(ioexception);
        }

        this.baseDir = baseDir;
        this.backupDir = backupDir;
        this.worldDirValidator = worldDirValidator;
    }

    public static DirectoryValidator parseValidator(Path validator) {
        if (Files.exists(validator)) {
            try {
                DirectoryValidator directoryvalidator;
                try (BufferedReader bufferedreader = Files.newBufferedReader(validator)) {
                    directoryvalidator = new DirectoryValidator(PathAllowList.readPlain(bufferedreader));
                }

                return directoryvalidator;
            } catch (Exception exception) {
                LOGGER.error("Failed to parse {}, disallowing all symbolic links", "allowed_symlinks.txt", exception);
            }
        }

        return new DirectoryValidator(NO_SYMLINKS_ALLOWED);
    }

    public static LevelStorageSource createDefault(Path savesDir) {
        DirectoryValidator directoryvalidator = parseValidator(savesDir.resolve("allowed_symlinks.txt"));
        return new LevelStorageSource(savesDir, savesDir.resolve("../backups"), directoryvalidator, DataFixers.getDataFixer());
    }

    public static WorldDataConfiguration readDataConfig(Dynamic<?> dynamic) {
        return WorldDataConfiguration.CODEC.parse(dynamic).resultOrPartial(LOGGER::error).orElse(WorldDataConfiguration.DEFAULT);
    }

    public static WorldLoader.PackConfig getPackConfig(Dynamic<?> dynamic, PackRepository packRepository, boolean safeMode) {
        return new WorldLoader.PackConfig(packRepository, readDataConfig(dynamic), safeMode, false);
    }

    public static LevelDataAndDimensions getLevelDataAndDimensions(
        Dynamic<?> p_dynamic, WorldDataConfiguration dataConfiguration, Registry<LevelStem> levelStemRegistry, RegistryAccess.Frozen registry
    ) {
        Dynamic<?> dynamic = RegistryOps.injectRegistryContext(p_dynamic, registry);
        Dynamic<?> dynamic1 = dynamic.get("WorldGenSettings").orElseEmptyMap();
        WorldGenSettings worldgensettings = WorldGenSettings.CODEC.parse(dynamic1).getOrThrow();
        LevelSettings levelsettings = LevelSettings.parse(dynamic, dataConfiguration);
        WorldDimensions.Complete worlddimensions$complete = worldgensettings.dimensions().bake(levelStemRegistry);
        Lifecycle lifecycle = worlddimensions$complete.lifecycle().add(registry.allRegistriesLifecycle());
        PrimaryLevelData primaryleveldata = PrimaryLevelData.parse(
            dynamic, levelsettings, worlddimensions$complete.specialWorldProperty(), worldgensettings.options(), lifecycle
        );
        return new LevelDataAndDimensions(primaryleveldata, worlddimensions$complete);
    }

    public String getName() {
        return "Anvil";
    }

    public LevelStorageSource.LevelCandidates findLevelCandidates() throws LevelStorageException {
        if (!Files.isDirectory(this.baseDir)) {
            throw new LevelStorageException(Component.translatable("selectWorld.load_folder_access"));
        } else {
            try {
                LevelStorageSource.LevelCandidates levelstoragesource$levelcandidates;
                try (Stream<Path> stream = Files.list(this.baseDir)) {
                    List<LevelStorageSource.LevelDirectory> list = stream.filter(p_230839_ -> Files.isDirectory(p_230839_))
                        .map(LevelStorageSource.LevelDirectory::new)
                        .filter(p_230835_ -> Files.isRegularFile(p_230835_.dataFile()) || Files.isRegularFile(p_230835_.oldDataFile()))
                        .toList();
                    levelstoragesource$levelcandidates = new LevelStorageSource.LevelCandidates(list);
                }

                return levelstoragesource$levelcandidates;
            } catch (IOException ioexception) {
                throw new LevelStorageException(Component.translatable("selectWorld.load_folder_access"));
            }
        }
    }

    public CompletableFuture<List<LevelSummary>> loadLevelSummaries(LevelStorageSource.LevelCandidates candidates) {
        List<CompletableFuture<LevelSummary>> list = new ArrayList<>(candidates.levels.size());

        for (LevelStorageSource.LevelDirectory levelstoragesource$leveldirectory : candidates.levels) {
            list.add(CompletableFuture.supplyAsync(() -> {
                boolean flag;
                try {
                    flag = DirectoryLock.isLocked(levelstoragesource$leveldirectory.path());
                } catch (Exception exception) {
                    LOGGER.warn("Failed to read {} lock", levelstoragesource$leveldirectory.path(), exception);
                    return null;
                }

                try {
                    return this.readLevelSummary(levelstoragesource$leveldirectory, flag);
                } catch (OutOfMemoryError outofmemoryerror1) {
                    MemoryReserve.release();
                    System.gc();
                    String s = "Ran out of memory trying to read summary of world folder \"" + levelstoragesource$leveldirectory.directoryName() + "\"";
                    LOGGER.error(LogUtils.FATAL_MARKER, s);
                    OutOfMemoryError outofmemoryerror = new OutOfMemoryError("Ran out of memory reading level data");
                    outofmemoryerror.initCause(outofmemoryerror1);
                    CrashReport crashreport = CrashReport.forThrowable(outofmemoryerror, s);
                    CrashReportCategory crashreportcategory = crashreport.addCategory("World details");
                    crashreportcategory.setDetail("Folder Name", levelstoragesource$leveldirectory.directoryName());

                    try {
                        long i = Files.size(levelstoragesource$leveldirectory.dataFile());
                        crashreportcategory.setDetail("level.dat size", i);
                    } catch (IOException ioexception) {
                        crashreportcategory.setDetailError("level.dat size", ioexception);
                    }

                    throw new ReportedException(crashreport);
                }
            }, Util.backgroundExecutor()));
        }

        return Util.sequenceFailFastAndCancel(list).thenApply(p_230832_ -> p_230832_.stream().filter(Objects::nonNull).sorted().toList());
    }

    private int getStorageVersion() {
        return 19133;
    }

    static CompoundTag readLevelDataTagRaw(Path levelPath) throws IOException {
        return NbtIo.readCompressed(levelPath, NbtAccounter.create(104857600L));
    }

    static Dynamic<?> readLevelDataTagFixed(Path levelPath, DataFixer dataFixer) throws IOException {
        CompoundTag compoundtag = readLevelDataTagRaw(levelPath);
        CompoundTag compoundtag1 = compoundtag.getCompound("Data");
        int i = NbtUtils.getDataVersion(compoundtag1, -1);
        Dynamic<?> dynamic = DataFixTypes.LEVEL.updateToCurrentVersion(dataFixer, new Dynamic<>(NbtOps.INSTANCE, compoundtag1), i);
        dynamic = dynamic.update("Player", p_341581_ -> DataFixTypes.PLAYER.updateToCurrentVersion(dataFixer, p_341581_, i));
        return dynamic.update("WorldGenSettings", p_341584_ -> DataFixTypes.WORLD_GEN_SETTINGS.updateToCurrentVersion(dataFixer, p_341584_, i));
    }

    private LevelSummary readLevelSummary(LevelStorageSource.LevelDirectory levelDirectory, boolean locked) {
        Path path = levelDirectory.dataFile();
        if (Files.exists(path)) {
            try {
                if (Files.isSymbolicLink(path)) {
                    List<ForbiddenSymlinkInfo> list = this.worldDirValidator.validateSymlink(path);
                    if (!list.isEmpty()) {
                        LOGGER.warn("{}", ContentValidationException.getMessage(path, list));
                        return new LevelSummary.SymlinkLevelSummary(levelDirectory.directoryName(), levelDirectory.iconFile());
                    }
                }

                if (readLightweightData(path) instanceof CompoundTag compoundtag) {
                    CompoundTag compoundtag1 = compoundtag.getCompound("Data");
                    int i = NbtUtils.getDataVersion(compoundtag1, -1);
                    Dynamic<?> dynamic = DataFixTypes.LEVEL.updateToCurrentVersion(this.fixerUpper, new Dynamic<>(NbtOps.INSTANCE, compoundtag1), i);
                    return this.makeLevelSummary(dynamic, levelDirectory, locked);
                }

                LOGGER.warn("Invalid root tag in {}", path);
            } catch (Exception exception) {
                LOGGER.error("Exception reading {}", path, exception);
            }
        }

        return new LevelSummary.CorruptedLevelSummary(levelDirectory.directoryName(), levelDirectory.iconFile(), getFileModificationTime(levelDirectory));
    }

    private static long getFileModificationTime(LevelStorageSource.LevelDirectory levelDirectory) {
        Instant instant = getFileModificationTime(levelDirectory.dataFile());
        if (instant == null) {
            instant = getFileModificationTime(levelDirectory.oldDataFile());
        }

        return instant == null ? -1L : instant.toEpochMilli();
    }

    @Nullable
    static Instant getFileModificationTime(Path dataFilePath) {
        try {
            return Files.getLastModifiedTime(dataFilePath).toInstant();
        } catch (IOException ioexception) {
            return null;
        }
    }

    LevelSummary makeLevelSummary(Dynamic<?> dynamic, LevelStorageSource.LevelDirectory levelDirectory, boolean locked) {
        LevelVersion levelversion = LevelVersion.parse(dynamic);
        int i = levelversion.levelDataVersion();
        if (i != 19132 && i != 19133) {
            throw new NbtFormatException("Unknown data version: " + Integer.toHexString(i));
        } else {
            boolean flag = i != this.getStorageVersion();
            Path path = levelDirectory.iconFile();
            WorldDataConfiguration worlddataconfiguration = readDataConfig(dynamic);
            LevelSettings levelsettings = LevelSettings.parse(dynamic, worlddataconfiguration);
            FeatureFlagSet featureflagset = parseFeatureFlagsFromSummary(dynamic);
            boolean flag1 = FeatureFlags.isExperimental(featureflagset);
            return new LevelSummary(levelsettings, levelversion, levelDirectory.directoryName(), flag, locked, flag1, path);
        }
    }

    private static FeatureFlagSet parseFeatureFlagsFromSummary(Dynamic<?> dataDynamic) {
        Set<ResourceLocation> set = dataDynamic.get("enabled_features")
            .asStream()
            .flatMap(p_338115_ -> p_338115_.asString().result().map(ResourceLocation::tryParse).stream())
            .collect(Collectors.toSet());
        return FeatureFlags.REGISTRY.fromNames(set, p_248503_ -> {
        });
    }

    @Nullable
    private static Tag readLightweightData(Path file) throws IOException {
        SkipFields skipfields = new SkipFields(
            new FieldSelector("Data", CompoundTag.TYPE, "Player"), new FieldSelector("Data", CompoundTag.TYPE, "WorldGenSettings")
        );
        NbtIo.parseCompressed(file, skipfields, NbtAccounter.create(104857600L));
        return skipfields.getResult();
    }

    public boolean isNewLevelIdAcceptable(String saveName) {
        try {
            Path path = this.getLevelPath(saveName);
            Files.createDirectory(path);
            Files.deleteIfExists(path);
            return true;
        } catch (IOException ioexception) {
            return false;
        }
    }

    /**
     * Return whether the given world can be loaded.
     */
    public boolean levelExists(String saveName) {
        try {
            return Files.isDirectory(this.getLevelPath(saveName));
        } catch (InvalidPathException invalidpathexception) {
            return false;
        }
    }

    public Path getLevelPath(String saveName) {
        return this.baseDir.resolve(saveName);
    }

    public Path getBaseDir() {
        return this.baseDir;
    }

    public Path getBackupPath() {
        return this.backupDir;
    }

    public LevelStorageSource.LevelStorageAccess validateAndCreateAccess(String saveName) throws IOException, ContentValidationException {
        Path path = this.getLevelPath(saveName);
        List<ForbiddenSymlinkInfo> list = this.worldDirValidator.validateDirectory(path, true);
        if (!list.isEmpty()) {
            throw new ContentValidationException(path, list);
        } else {
            return new LevelStorageSource.LevelStorageAccess(saveName, path);
        }
    }

    public LevelStorageSource.LevelStorageAccess createAccess(String saveName) throws IOException {
        Path path = this.getLevelPath(saveName);
        return new LevelStorageSource.LevelStorageAccess(saveName, path);
    }

    public DirectoryValidator getWorldDirValidator() {
        return this.worldDirValidator;
    }

    public static record LevelCandidates(List<LevelStorageSource.LevelDirectory> levels) implements Iterable<LevelStorageSource.LevelDirectory> {
        public boolean isEmpty() {
            return this.levels.isEmpty();
        }

        @Override
        public Iterator<LevelStorageSource.LevelDirectory> iterator() {
            return this.levels.iterator();
        }
    }

    public static record LevelDirectory(Path path) {
        public String directoryName() {
            return this.path.getFileName().toString();
        }

        public Path dataFile() {
            return this.resourcePath(LevelResource.LEVEL_DATA_FILE);
        }

        public Path oldDataFile() {
            return this.resourcePath(LevelResource.OLD_LEVEL_DATA_FILE);
        }

        public Path corruptedDataFile(LocalDateTime dateTime) {
            return this.path.resolve(LevelResource.LEVEL_DATA_FILE.getId() + "_corrupted_" + dateTime.format(LevelStorageSource.FORMATTER));
        }

        public Path rawDataFile(LocalDateTime dateTime) {
            return this.path.resolve(LevelResource.LEVEL_DATA_FILE.getId() + "_raw_" + dateTime.format(LevelStorageSource.FORMATTER));
        }

        public Path iconFile() {
            return this.resourcePath(LevelResource.ICON_FILE);
        }

        public Path lockFile() {
            return this.resourcePath(LevelResource.LOCK_FILE);
        }

        public Path resourcePath(LevelResource resource) {
            return this.path.resolve(resource.getId());
        }
    }

    public class LevelStorageAccess implements AutoCloseable {
        final DirectoryLock lock;
        final LevelStorageSource.LevelDirectory levelDirectory;
        private final String levelId;
        private final Map<LevelResource, Path> resources = Maps.newHashMap();

        LevelStorageAccess(String levelId, Path levelDir) throws IOException {
            this.levelId = levelId;
            this.levelDirectory = new LevelStorageSource.LevelDirectory(levelDir);
            this.lock = DirectoryLock.create(levelDir);
        }

        public long estimateDiskSpace() {
            try {
                return Files.getFileStore(this.levelDirectory.path).getUsableSpace();
            } catch (Exception exception) {
                return Long.MAX_VALUE;
            }
        }

        public boolean checkForLowDiskSpace() {
            return this.estimateDiskSpace() < 67108864L;
        }

        public void safeClose() {
            try {
                this.close();
            } catch (IOException ioexception) {
                LevelStorageSource.LOGGER.warn("Failed to unlock access to level {}", this.getLevelId(), ioexception);
            }
        }

        public LevelStorageSource parent() {
            return LevelStorageSource.this;
        }

        public LevelStorageSource.LevelDirectory getLevelDirectory() {
            return this.levelDirectory;
        }

        public String getLevelId() {
            return this.levelId;
        }

        public Path getLevelPath(LevelResource folderName) {
            return this.resources.computeIfAbsent(folderName, this.levelDirectory::resourcePath);
        }

        public Path getDimensionPath(ResourceKey<Level> dimensionPath) {
            return DimensionType.getStorageFolder(dimensionPath, this.levelDirectory.path());
        }

        private void checkLock() {
            if (!this.lock.isValid()) {
                throw new IllegalStateException("Lock is no longer valid");
            }
        }

        public void readAdditionalLevelSaveData(boolean fallback) {
            checkLock();
            Path path = fallback ? this.levelDirectory.oldDataFile() : this.levelDirectory.dataFile();
            try {
                var tag = readLightweightData(path);
                if (tag instanceof CompoundTag compoundTag)
                    net.neoforged.neoforge.common.CommonHooks.readAdditionalLevelSaveData(compoundTag, this.levelDirectory);
            } catch (Exception e) {
                LOGGER.error("Exception reading {}", path, e);
            }
        }

        public PlayerDataStorage createPlayerStorage() {
            this.checkLock();
            return new PlayerDataStorage(this, LevelStorageSource.this.fixerUpper);
        }

        public LevelSummary getSummary(Dynamic<?> dynamic) {
            this.checkLock();
            return LevelStorageSource.this.makeLevelSummary(dynamic, this.levelDirectory, false);
        }

        public Dynamic<?> getDataTag() throws IOException {
            return this.getDataTag(false);
        }

        public Dynamic<?> getDataTagFallback() throws IOException {
            return this.getDataTag(true);
        }

        private Dynamic<?> getDataTag(boolean useFallback) throws IOException {
            this.checkLock();
            return LevelStorageSource.readLevelDataTagFixed(
                useFallback ? this.levelDirectory.oldDataFile() : this.levelDirectory.dataFile(), LevelStorageSource.this.fixerUpper
            );
        }

        public void saveDataTag(RegistryAccess registries, WorldData serverConfiguration) {
            this.saveDataTag(registries, serverConfiguration, null);
        }

        public void saveDataTag(RegistryAccess registries, WorldData serverConfiguration, @Nullable CompoundTag hostPlayerNBT) {
            CompoundTag compoundtag = serverConfiguration.createTag(registries, hostPlayerNBT);
            CompoundTag compoundtag1 = new CompoundTag();
            compoundtag1.put("Data", compoundtag);
            net.neoforged.neoforge.common.CommonHooks.writeAdditionalLevelSaveData(serverConfiguration, compoundtag1);
            this.saveLevelData(compoundtag1);
        }

        private void saveLevelData(CompoundTag tag) {
            Path path = this.levelDirectory.path();

            try {
                Path path1 = Files.createTempFile(path, "level", ".dat");
                NbtIo.writeCompressed(tag, path1);
                Path path2 = this.levelDirectory.oldDataFile();
                Path path3 = this.levelDirectory.dataFile();
                Util.safeReplaceFile(path3, path1, path2);
            } catch (Exception exception) {
                LevelStorageSource.LOGGER.error("Failed to save level {}", path, exception);
            }
        }

        public Optional<Path> getIconFile() {
            return !this.lock.isValid() ? Optional.empty() : Optional.of(this.levelDirectory.iconFile());
        }

        public Path getWorldDir() {
            return baseDir;
        }

        public void deleteLevel() throws IOException {
            this.checkLock();
            final Path path = this.levelDirectory.lockFile();
            LevelStorageSource.LOGGER.info("Deleting level {}", this.levelId);

            for (int i = 1; i <= 5; i++) {
                LevelStorageSource.LOGGER.info("Attempt {}...", i);

                try {
                    Files.walkFileTree(this.levelDirectory.path(), new SimpleFileVisitor<Path>() {
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            if (!file.equals(path)) {
                                LevelStorageSource.LOGGER.debug("Deleting {}", file);
                                Files.delete(file);
                            }

                            return FileVisitResult.CONTINUE;
                        }

                        public FileVisitResult postVisitDirectory(Path dir, @Nullable IOException exception) throws IOException {
                            if (exception != null) {
                                throw exception;
                            } else {
                                if (dir.equals(LevelStorageAccess.this.levelDirectory.path())) {
                                    LevelStorageAccess.this.lock.close();
                                    Files.deleteIfExists(path);
                                }

                                Files.delete(dir);
                                return FileVisitResult.CONTINUE;
                            }
                        }
                    });
                    break;
                } catch (IOException ioexception) {
                    if (i >= 5) {
                        throw ioexception;
                    }

                    LevelStorageSource.LOGGER.warn("Failed to delete {}", this.levelDirectory.path(), ioexception);

                    try {
                        Thread.sleep(500L);
                    } catch (InterruptedException interruptedexception) {
                    }
                }
            }
        }

        public void renameLevel(String saveName) throws IOException {
            this.modifyLevelDataWithoutDatafix(p_307270_ -> p_307270_.putString("LevelName", saveName.trim()));
        }

        public void renameAndDropPlayer(String saveName) throws IOException {
            this.modifyLevelDataWithoutDatafix(p_307287_ -> {
                p_307287_.putString("LevelName", saveName.trim());
                p_307287_.remove("Player");
            });
        }

        private void modifyLevelDataWithoutDatafix(Consumer<CompoundTag> modifier) throws IOException {
            this.checkLock();
            CompoundTag compoundtag = LevelStorageSource.readLevelDataTagRaw(this.levelDirectory.dataFile());
            modifier.accept(compoundtag.getCompound("Data"));
            this.saveLevelData(compoundtag);
        }

        public long makeWorldBackup() throws IOException {
            this.checkLock();
            String s = LocalDateTime.now().format(LevelStorageSource.FORMATTER) + "_" + this.levelId;
            Path path = LevelStorageSource.this.getBackupPath();

            try {
                FileUtil.createDirectoriesSafe(path);
            } catch (IOException ioexception) {
                throw new RuntimeException(ioexception);
            }

            Path path1 = path.resolve(FileUtil.findAvailableName(path, s, ".zip"));

            try (final ZipOutputStream zipoutputstream = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(path1)))) {
                final Path path2 = Paths.get(this.levelId);
                Files.walkFileTree(this.levelDirectory.path(), new SimpleFileVisitor<Path>() {
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (file.endsWith("session.lock")) {
                            return FileVisitResult.CONTINUE;
                        } else {
                            String s1 = path2.resolve(LevelStorageAccess.this.levelDirectory.path().relativize(file)).toString().replace('\\', '/');
                            ZipEntry zipentry = new ZipEntry(s1);
                            zipoutputstream.putNextEntry(zipentry);
                            com.google.common.io.Files.asByteSource(file.toFile()).copyTo(zipoutputstream);
                            zipoutputstream.closeEntry();
                            return FileVisitResult.CONTINUE;
                        }
                    }
                });
            }

            return Files.size(path1);
        }

        public boolean hasWorldData() {
            return Files.exists(this.levelDirectory.dataFile()) || Files.exists(this.levelDirectory.oldDataFile());
        }

        @Override
        public void close() throws IOException {
            this.lock.close();
        }

        public boolean restoreLevelDataFromOld() {
            return Util.safeReplaceOrMoveFile(
                this.levelDirectory.dataFile(), this.levelDirectory.oldDataFile(), this.levelDirectory.corruptedDataFile(LocalDateTime.now()), true
            );
        }

        @Nullable
        public Instant getFileModificationTime(boolean useFallback) {
            return LevelStorageSource.getFileModificationTime(useFallback ? this.levelDirectory.oldDataFile() : this.levelDirectory.dataFile());
        }
    }
}
