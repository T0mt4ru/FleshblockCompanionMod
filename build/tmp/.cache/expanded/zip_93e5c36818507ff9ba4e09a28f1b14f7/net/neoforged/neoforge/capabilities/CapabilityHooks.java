/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.capabilities;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.neoforged.neoforge.items.ComponentItemHandler;
import net.neoforged.neoforge.items.VanillaHopperItemHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import net.neoforged.neoforge.items.wrapper.EntityArmorInvWrapper;
import net.neoforged.neoforge.items.wrapper.EntityHandsInvWrapper;
import net.neoforged.neoforge.items.wrapper.ForwardingItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.PlayerInvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class CapabilityHooks {
    private static boolean initialized = false;
    static boolean initFinished = false;

    public static void init() {
        if (initialized)
            throw new IllegalArgumentException("CapabilityHooks.init() called twice");
        initialized = true;

        var event = new RegisterCapabilitiesEvent();
        ModLoader.postEventWrapContainerInModOrder(event);

        initFinished = true;
    }

    public static void markProxyableCapabilities(RegisterCapabilitiesEvent event) {
        event.setProxyable(Capabilities.EnergyStorage.BLOCK);
        event.setProxyable(Capabilities.FluidHandler.BLOCK);
        event.setProxyable(Capabilities.ItemHandler.BLOCK);
    }

    public static void registerVanillaProviders(RegisterCapabilitiesEvent event) {
        // Blocks
        var composterBlock = (WorldlyContainerHolder) Blocks.COMPOSTER;
        event.registerBlock(Capabilities.ItemHandler.BLOCK, (level, pos, state, blockEntity, side) -> {
            // Return a wrapper that gets re-evaluated every time it is accessed
            // Invalidation is taken care of by the patches to ComposterBlock

            // Note: re-query the block state everytime instead of using `state` because the state can change at any time!
            if (side == null) {
                return new ForwardingItemHandler(() -> new InvWrapper(composterBlock.getContainer(level.getBlockState(pos), level, pos)));
            } else {
                return new ForwardingItemHandler(() -> new SidedInvWrapper(composterBlock.getContainer(level.getBlockState(pos), level, pos), side));
            }
        }, Blocks.COMPOSTER);

        event.registerBlock(Capabilities.ItemHandler.BLOCK, (level, pos, state, blockEntity, side) -> {
            return new InvWrapper(ChestBlock.getContainer((ChestBlock) state.getBlock(), state, level, pos, true));
        }, Blocks.CHEST, Blocks.TRAPPED_CHEST);

        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, BlockEntityType.HOPPER, (hopper, side) -> {
            // Use custom hopper wrapper that respects cooldown
            return new VanillaHopperItemHandler(hopper);
        });

        var sidedVanillaContainers = List.of(
                BlockEntityType.BLAST_FURNACE,
                BlockEntityType.BREWING_STAND,
                BlockEntityType.FURNACE,
                BlockEntityType.SMOKER,
                BlockEntityType.SHULKER_BOX);
        for (var type : sidedVanillaContainers) {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type, SidedInvWrapper::new);
        }

        var nonSidedVanillaContainers = List.of(
                BlockEntityType.BARREL,
                BlockEntityType.CHISELED_BOOKSHELF,
                BlockEntityType.DISPENSER,
                BlockEntityType.DROPPER,
                BlockEntityType.JUKEBOX,
                BlockEntityType.CRAFTER,
                BlockEntityType.DECORATED_POT);
        for (var type : nonSidedVanillaContainers) {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type, (container, side) -> new InvWrapper(container));
        }

        // Entities
        var containerEntities = List.of(
                EntityType.CHEST_BOAT,
                EntityType.CHEST_MINECART,
                EntityType.HOPPER_MINECART);
        for (var entityType : containerEntities) {
            event.registerEntity(Capabilities.ItemHandler.ENTITY, entityType, (entity, ctx) -> new InvWrapper(entity));
            event.registerEntity(Capabilities.ItemHandler.ENTITY_AUTOMATION, entityType, (entity, ctx) -> new InvWrapper(entity));
        }
        event.registerEntity(Capabilities.ItemHandler.ENTITY, EntityType.PLAYER, (player, ctx) -> new PlayerInvWrapper(player.getInventory()));

        // Items
        event.registerItem(Capabilities.ItemHandler.ITEM, (stack, ctx) -> new ComponentItemHandler(stack, DataComponents.CONTAINER, 27),
                Items.SHULKER_BOX,
                Items.BLACK_SHULKER_BOX,
                Items.BLUE_SHULKER_BOX,
                Items.BROWN_SHULKER_BOX,
                Items.CYAN_SHULKER_BOX,
                Items.GRAY_SHULKER_BOX,
                Items.GREEN_SHULKER_BOX,
                Items.LIGHT_BLUE_SHULKER_BOX,
                Items.LIGHT_GRAY_SHULKER_BOX,
                Items.LIME_SHULKER_BOX,
                Items.MAGENTA_SHULKER_BOX,
                Items.ORANGE_SHULKER_BOX,
                Items.PINK_SHULKER_BOX,
                Items.PURPLE_SHULKER_BOX,
                Items.RED_SHULKER_BOX,
                Items.WHITE_SHULKER_BOX,
                Items.YELLOW_SHULKER_BOX);
    }

    public static void registerFallbackVanillaProviders(RegisterCapabilitiesEvent event) {
        // Entities
        // Register to all entity types to make sure we support all living entity subclasses.
        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
            event.registerEntity(Capabilities.ItemHandler.ENTITY, entityType, (entity, ctx) -> {
                if (entity instanceof AbstractHorse horse)
                    return new InvWrapper(horse.getInventory());
                else if (entity instanceof LivingEntity livingEntity)
                    return new CombinedInvWrapper(new EntityHandsInvWrapper(livingEntity), new EntityArmorInvWrapper(livingEntity));

                return null;
            });
        }

        // Items
        for (Item item : BuiltInRegistries.ITEM) {
            if (item.getClass() == BucketItem.class)
                event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new FluidBucketWrapper(stack), item);
        }

        // We want mods to be able to override our milk cap by default
        if (NeoForgeMod.MILK.isBound()) {
            event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new FluidBucketWrapper(stack), Items.MILK_BUCKET);
        }
    }

    public static void invalidateCapsOnChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel sl) {
            sl.invalidateCapabilities(event.getChunk().getPos());
        }
    }

    public static void invalidateCapsOnChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel sl) {
            sl.invalidateCapabilities(event.getChunk().getPos());
        }
    }

    public static void cleanCapabilityListenerReferencesOnTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel sl) {
            sl.cleanCapabilityListenerReferences();
        }
    }
}
