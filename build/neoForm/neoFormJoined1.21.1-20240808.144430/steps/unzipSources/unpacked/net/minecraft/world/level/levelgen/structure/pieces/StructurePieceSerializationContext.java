package net.minecraft.world.level.levelgen.structure.pieces;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public record StructurePieceSerializationContext(
    ResourceManager resourceManager, RegistryAccess registryAccess, StructureTemplateManager structureTemplateManager
) {
    public static StructurePieceSerializationContext fromLevel(ServerLevel level) {
        MinecraftServer minecraftserver = level.getServer();
        return new StructurePieceSerializationContext(
            minecraftserver.getResourceManager(), minecraftserver.registryAccess(), minecraftserver.getStructureManager()
        );
    }
}
