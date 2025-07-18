package net.minecraft.server.packs.repository;

import com.google.common.annotations.VisibleForTesting;
import java.nio.file.Path;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.BuiltInMetadata;
import net.minecraft.server.packs.FeatureFlagsMetadataSection;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.VanillaPackResourcesBuilder;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.validation.DirectoryValidator;

public class ServerPacksSource extends BuiltInPackSource {
    private static final PackMetadataSection VERSION_METADATA_SECTION = new PackMetadataSection(
        Component.translatable("dataPack.vanilla.description"), SharedConstants.getCurrentVersion().getPackVersion(PackType.SERVER_DATA), Optional.empty()
    );
    private static final FeatureFlagsMetadataSection FEATURE_FLAGS_METADATA_SECTION = new FeatureFlagsMetadataSection(FeatureFlags.DEFAULT_FLAGS);
    private static final BuiltInMetadata BUILT_IN_METADATA = BuiltInMetadata.of(
        PackMetadataSection.TYPE, VERSION_METADATA_SECTION, FeatureFlagsMetadataSection.TYPE, FEATURE_FLAGS_METADATA_SECTION
    );
    private static final PackLocationInfo VANILLA_PACK_INFO = new PackLocationInfo(
        "vanilla", Component.translatable("dataPack.vanilla.name"), PackSource.BUILT_IN, Optional.of(CORE_PACK_INFO)
    );
    private static final PackSelectionConfig VANILLA_SELECTION_CONFIG = new PackSelectionConfig(false, Pack.Position.BOTTOM, false);
    private static final PackSelectionConfig FEATURE_SELECTION_CONFIG = new PackSelectionConfig(false, Pack.Position.TOP, false);
    private static final ResourceLocation PACKS_DIR = ResourceLocation.withDefaultNamespace("datapacks");

    public ServerPacksSource(DirectoryValidator validator) {
        super(PackType.SERVER_DATA, createVanillaPackSource(), PACKS_DIR, validator);
    }

    private static PackLocationInfo createBuiltInPackLocation(String id, Component title) {
        return new PackLocationInfo(id, title, PackSource.FEATURE, Optional.of(KnownPack.vanilla(id)));
    }

    @VisibleForTesting
    public static VanillaPackResources createVanillaPackSource() {
        return new VanillaPackResourcesBuilder()
            .setMetadata(BUILT_IN_METADATA)
            .exposeNamespace("minecraft")
            .applyDevelopmentConfig()
            .pushJarResources()
            .build(VANILLA_PACK_INFO);
    }

    @Override
    protected Component getPackTitle(String id) {
        return Component.literal(id);
    }

    @Nullable
    @Override
    protected Pack createVanillaPack(PackResources resources) {
        return Pack.readMetaAndCreate(VANILLA_PACK_INFO, fixedResources(resources), PackType.SERVER_DATA, VANILLA_SELECTION_CONFIG);
    }

    @Nullable
    @Override
    protected Pack createBuiltinPack(String id, Pack.ResourcesSupplier resources, Component title) {
        return Pack.readMetaAndCreate(createBuiltInPackLocation(id, title), resources, PackType.SERVER_DATA, FEATURE_SELECTION_CONFIG);
    }

    public static PackRepository createPackRepository(Path folder, DirectoryValidator validator) {
        final PackRepository packRepository = new PackRepository(new ServerPacksSource(validator), new FolderRepositorySource(folder, PackType.SERVER_DATA, PackSource.WORLD, validator));
        net.neoforged.neoforge.resource.ResourcePackLoader.populatePackRepository(packRepository, PackType.SERVER_DATA, false);
        return packRepository;
    }

    public static PackRepository createVanillaTrustedRepository() {
        final PackRepository packRepository = new PackRepository(new ServerPacksSource(new DirectoryValidator(p_293813_ -> true)));
        net.neoforged.neoforge.resource.ResourcePackLoader.populatePackRepository(packRepository, PackType.SERVER_DATA, true);
        return packRepository;
    }

    public static PackRepository createPackRepository(LevelStorageSource.LevelStorageAccess level) {
        return createPackRepository(level.getLevelPath(LevelResource.DATAPACK_DIR), level.parent().getWorldDirValidator());
    }
}
