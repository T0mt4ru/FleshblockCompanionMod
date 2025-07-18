/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.common.DataMapHooks;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.enums.BubbleColumnDirection;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.common.world.AuxiliaryLightManager;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public interface IBlockExtension {
    private Block self() {
        return (Block) this;
    }

    /**
     * Gets the slipperiness at the given location at the given state. Normally
     * between 0 and 1.
     * <p>
     * Note that entities may reduce slipperiness by a certain factor of their own;
     * for {@link LivingEntity}, this is {@code .91}.
     * {@link ItemEntity} uses {@code .98}, and
     * {@link FishingHook} uses {@code .92}.
     *
     * @param state  state of the block
     * @param level  the level
     * @param pos    the position in the level
     * @param entity the entity in question
     * @return the factor by which the entity's motion should be multiplied
     */
    default float getFriction(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        return self().getFriction();
    }

    /**
     * Whether this block has dynamic light emission which is not solely based on the {@link BlockState} and instead
     * uses the {@link BlockPos}, the {@link AuxiliaryLightManager} or another external data source to determine its
     * light value in {@link #getLightEmission(BlockState, BlockGetter, BlockPos)}
     *
     * @param state the block state being checked
     * @return true if this block cannot determine its light emission solely based on the block state, false otherwise
     */
    default boolean hasDynamicLightEmission(BlockState state) {
        return false;
    }

    /**
     * Get a light value for this block, taking into account the given state and coordinates, normal ranges are between 0 and 15
     *
     * @param state The state of this block
     * @param level The level this block is in, may be {@link EmptyBlockGetter#INSTANCE}, see implementation notes
     * @param pos   The position of this block in the level, may be {@link BlockPos#ZERO}, see implementation notes
     * @return The light value
     * @implNote <ul>
     *           <li>
     *           If the given state of this block may emit light but requires position context to determine the light
     *           value, then it must return {@code true} from {@link #hasDynamicLightEmission(BlockState)}, otherwise
     *           this method will be called with {@link EmptyBlockGetter#INSTANCE} and {@link BlockPos#ZERO} during
     *           chunk generation or loading to determine whether a chunk may contain a light-emitting block,
     *           resulting in erroneous data if it's determined with the given level and/or the given position.
     *           </li>
     *           <li>
     *           The given {@link BlockGetter} may be a chunk. Block, fluid or block entity accesses outside of its bounds
     *           will cause issues such as wrapping coordinates returning values from the opposing chunk edge
     *           </li>
     *           <li>
     *           If the light value depends on data from a {@link BlockEntity} then the light level must be stored in
     *           the {@link AuxiliaryLightManager} by the {@code BlockEntity} and retrieved from the
     *           {@code AuxiliaryLightManager} in this method. This is to ensure thread-safety and availability of
     *           the data during chunk load from disk.
     *           </li>
     *           </ul>
     */
    default int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getLightEmission();
    }

    /**
     * Checks if a player or entity can use this block to 'climb' like a ladder.
     *
     * @param state  The current state
     * @param level  The current level
     * @param pos    Block position in level
     * @param entity The entity trying to use the ladder, CAN be null.
     * @return True if the block should act like a ladder
     */
    default boolean isLadder(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
        return state.is(BlockTags.CLIMBABLE);
    }

    /**
     * Checks if this block makes an open trapdoor above it climbable.
     *
     * @param state         The current state
     * @param level         The current level
     * @param pos           Block position in level
     * @param trapdoorState The current state of the open trapdoor above
     * @return True if the block should act like a ladder
     */
    default boolean makesOpenTrapdoorAboveClimbable(BlockState state, LevelReader level, BlockPos pos, BlockState trapdoorState) {
        return state.getBlock() instanceof LadderBlock && state.getValue(LadderBlock.FACING) == trapdoorState.getValue(TrapDoorBlock.FACING);
    }

    /**
     * Determines if this block should set fire and deal fire damage
     * to entities coming into contact with it.
     *
     * @param level The current level
     * @param pos   Block position in level
     * @return True if the block should deal damage
     */
    default boolean isBurning(BlockState state, BlockGetter level, BlockPos pos) {
        return this == Blocks.FIRE || this == Blocks.LAVA;
    }

    /**
     * Determines if the player can harvest this block, obtaining it's drops when the block is destroyed.
     *
     * @param level  The current level
     * @param pos    The block's current position
     * @param player The player damaging the block
     * @return True to spawn the drops
     */
    default public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
        return EventHooks.doPlayerHarvestCheck(player, state, level, pos);
    }

    /**
     * Called when a player removes a block. This is responsible for
     * actually destroying the block, and the block is intact at time of call.
     * This is called regardless of whether the player can harvest the block or
     * not.
     *
     * Return true if the block is actually destroyed.
     *
     * This function is called on both the logical client and logical server.
     *
     * @param state       The current state.
     * @param level       The current level
     * @param player      The player damaging the block, may be null
     * @param pos         Block position in level
     * @param willHarvest The result of {@link #canHarvestBlock}, if called on the server by a non-creative player, otherwise always false.
     * @param fluid       The current fluid state at current position
     * @return True if the block is actually destroyed.
     */
    default boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (level.isClientSide()) {
            // On the client, vanilla calls Level#setBlock, per MultiPlayerGameMode#destroyBlock
            return level.setBlock(pos, fluid.createLegacyBlock(), 11);
        } else {
            // On the server, vanilla calls Level#removeBlock, per ServerPlayerGameMode#destroyBlock
            return level.removeBlock(pos, false);
        }
    }

    /**
     * Called when a block is removed by {@link PushReaction#DESTROY}. This is responsible for
     * actually destroying the block, and the block is intact at time of call.
     * <p>
     * Will only be called if {@link BlockState#getPistonPushReaction} returns {@link PushReaction#DESTROY}.
     * <p>
     * Note: When used in multiplayer, this is called on both client and
     * server sides!
     *
     * @param state         The current state.
     * @param level         The current level
     * @param pos           Block position in level
     * @param pushDirection The direction of block movement
     * @param fluid         The current fluid state at current position
     */
    default void onDestroyedByPushReaction(BlockState state, Level level, BlockPos pos, Direction pushDirection, FluidState fluid) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_CLIENTS);
        level.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(state));
    }

    /**
     * Determines if this block is classified as a bed, replacing <code>instanceof BedBlock</code> checks.
     * <p>
     * If true, players may sleep in it, though the block must manually put the player to sleep
     * by calling {@link Player#startSleepInBed} from {@link BlockBehaviour#useWithoutItem} or similar.
     * <p>
     * If you want players to be able to respawn at your bed, you also need to override {@link #getRespawnPosition}.
     *
     * @param state   The current state
     * @param level   The current level
     * @param pos     Block position in level
     * @param sleeper The sleeping entity.
     * @return True to treat this as a bed
     */
    default boolean isBed(BlockState state, BlockGetter level, BlockPos pos, LivingEntity sleeper) {
        return self() instanceof BedBlock;
    }

    /**
     * Returns the position that the entity is moved to upon respawning at this block.
     *
     * @param state       The current state
     * @param type        The entity type used when checking if a dismount blockstate is dangerous. Currently always PLAYER.
     * @param levelReader The current level
     * @param pos         Block position in level
     * @param orientation The angle the entity had when setting the respawn point
     * @return The spawn position or the empty optional if respawning here is not possible
     */
    default Optional<ServerPlayer.RespawnPosAngle> getRespawnPosition(BlockState state, EntityType<?> type, LevelReader levelReader, BlockPos pos, float orientation) {
        return Optional.empty();
    }

    /**
     * Called when a user either starts or stops sleeping in the bed.
     *
     * @param level    The current level
     * @param pos      Block position in level
     * @param sleeper  The sleeper or camera entity, null in some cases.
     * @param occupied True if we are occupying the bed, or false if they are stopping use of the bed
     */
    default void setBedOccupied(BlockState state, Level level, BlockPos pos, LivingEntity sleeper, boolean occupied) {
        level.setBlock(pos, state.setValue(BedBlock.OCCUPIED, occupied), 3);
    }

    /**
     * Returns the direction of the block. Same values that
     * are returned by BlockDirectional. Called every frame tick for every living entity. Be VERY fast.
     *
     * @param state The current state
     * @param level The current level
     * @param pos   Block position in level
     * @return Bed direction
     */
    default Direction getBedDirection(BlockState state, LevelReader level, BlockPos pos) {
        return state.getValue(HorizontalDirectionalBlock.FACING);
    }

    /**
     * Location sensitive version of getExplosionResistance
     *
     * @param level     The current level
     * @param pos       Block position in level
     * @param explosion The explosion
     * @return The amount of the explosion absorbed.
     */
    default float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        return self().getExplosionResistance();
    }

    /**
     *
     * Called when A user uses the creative pick block button on this block
     *
     * @param target The full target the player is looking at
     * @return A ItemStack to add to the player's inventory, empty itemstack if nothing should be added.
     */
    default ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        return self().getCloneItemStack(level, pos, state);
    }

    /**
     * Allows a block to override the standard EntityLivingBase.updateFallState
     * particles, this is a server side method that spawns particles with
     * WorldServer.spawnParticle.
     *
     * @param level             The current server level
     * @param pos               The position of the block.
     * @param state2            The state at the specific level/pos
     * @param entity            The entity that hit landed on the block
     * @param numberOfParticles That vanilla level have spawned
     * @return True to prevent vanilla landing particles from spawning
     */
    default boolean addLandingEffects(BlockState state1, ServerLevel level, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
        return false;
    }

    /**
     * Allows a block to override the standard vanilla running particles.
     * This is called from Entity.spawnSprintParticle and is called both,
     * Client and server side, it's up to the implementor to client check / server check.
     * By default vanilla spawns particles only on the client and the server methods no-op.
     *
     * @param state  The BlockState the entity is running on.
     * @param level  The level.
     * @param pos    The position at the entities feet.
     * @param entity The entity running on the block.
     * @return True to prevent vanilla running particles from spawning.
     */
    default boolean addRunningEffects(BlockState state, Level level, BlockPos pos, Entity entity) {
        return false;
    }

    /**
     * Determines if this block either force allow or force disallow a plant from being placed on it. (Or pass and let the plant's decision win)
     * This will be called in plant's canSurvive method and/or mayPlace method.
     *
     * @param state        The current state
     * @param level        The current level
     * @param soilPosition The current position of the block that will sustain the plant
     * @param facing       The direction relative to the given position the plant wants to be, typically its UP
     * @param plant        The plant that is checking survivability
     * @return {@link TriState#TRUE} to allow the plant to be planted/stay. {@link TriState#FALSE} to disallow the plant from placing. {@link TriState#DEFAULT} to allow the plant to make the decision to stay or not.
     */
    default TriState canSustainPlant(BlockState state, BlockGetter level, BlockPos soilPosition, Direction facing, BlockState plant) {
        return TriState.DEFAULT;
    }

    /**
     * Called when a tree grows on top of this block and tries to set it to dirt by the trunk placer.
     * An override that returns true is responsible for using the place function to
     * set blocks in the world properly during generation. A modded grass block might override this method
     * to ensure it turns into the corresponding modded dirt instead of regular dirt when a tree grows on it.
     * For modded grass blocks, returning true from this method is NOT a substitute for adding your block
     * to the #minecraft:dirt tag, rather for changing the behaviour to something other than setting to dirt.
     *
     * NOTE: This happens DURING world generation, the generation may be incomplete when this is called.
     * Use the placeFunction when modifying the level.
     *
     * @param state         The current state
     * @param level         The current level
     * @param placeFunction Function to set blocks in the level for the tree, use this instead of the level directly
     * @param randomSource  The random source
     * @param pos           Position of the block to be set to dirt
     * @param config        Configuration of the trunk placer. Consider azalea trees, which should place rooted dirt instead of regular dirt.
     * @return True to ignore vanilla behaviour
     */
    default boolean onTreeGrow(BlockState state, LevelReader level, BiConsumer<BlockPos, BlockState> placeFunction, RandomSource randomSource, BlockPos pos, TreeConfiguration config) {
        return false;
    }

    /**
     * Checks if this soil is fertile, typically this means that growth rates
     * of plants on this soil will be slightly sped up.
     * Only vanilla case is tilledField when it is within range of water.
     *
     * @param level The current level
     * @param pos   Block position in level
     * @return True if the soil should be considered fertile.
     */
    default boolean isFertile(BlockState state, BlockGetter level, BlockPos pos) {
        if (state.getBlock() instanceof FarmBlock)
            return state.getValue(FarmBlock.MOISTURE) > 0;

        return false;
    }

    /**
     * Determines if this block can be used as the frame of a conduit.
     *
     * @param level   The current level
     * @param pos     Block position in level
     * @param conduit Conduit position in level
     * @return True, to support the conduit, and make it active with this block.
     */
    default boolean isConduitFrame(BlockState state, LevelReader level, BlockPos pos, BlockPos conduit) {
        return state.getBlock() == Blocks.PRISMARINE ||
                state.getBlock() == Blocks.PRISMARINE_BRICKS ||
                state.getBlock() == Blocks.SEA_LANTERN ||
                state.getBlock() == Blocks.DARK_PRISMARINE;
    }

    /**
     * Determines if this block can be used as part of a frame of a nether portal.
     *
     * @param state The current state
     * @param level The current level
     * @param pos   Block position in level
     * @return True, to support being part of a nether portal frame, false otherwise.
     */
    default boolean isPortalFrame(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.OBSIDIAN);
    }

    /**
     * Returns how many experience points this block drops when broken, before application of {@linkplain EnchantmentEffectComponents#BLOCK_EXPERIENCE enchantments}.
     *
     * @param state       The state of the block being broken
     * @param level       The level
     * @param pos         The position of the block being broken
     * @param blockEntity The block entity, if any
     * @param breaker     The entity who broke the block, if known
     * @param tool        The item stack used to break the block. May be empty
     * @return The amount of experience points dropped by this block
     */
    default int getExpDrop(BlockState state, LevelAccessor level, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Entity breaker, ItemStack tool) {
        return 0;
    }

    default BlockState rotate(BlockState state, LevelAccessor level, BlockPos pos, Rotation direction) {
        return state.rotate(direction);
    }

    /**
     * Determines the amount of enchanting power this block can provide to an enchanting table.
     *
     * @param level The level
     * @param pos   Block position in level
     * @return The amount of enchanting power this block produces.
     */
    default float getEnchantPowerBonus(BlockState state, LevelReader level, BlockPos pos) {
        return state.is(BlockTags.ENCHANTMENT_POWER_PROVIDER) ? 1 : 0;
    }

    /**
     * Called when a block entity on a side of this block changes, is created, or is destroyed.
     *
     * <p>This method is not suitable for listening to capability invalidations.
     * For capability invalidations specifically, use {@link BlockCapabilityCache} instead.
     *
     * @param level    The level
     * @param pos      Block position in level
     * @param neighbor Block position of neighbor
     */
    default void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {}

    /**
     * Called to determine whether to allow the block to handle its own indirect power rather than using the default rules.
     *
     * @param level The level
     * @param pos   Block position in level
     * @param side  The INPUT side of the block to be powered - ie the opposite of this block's output side
     * @return Whether Block#isProvidingWeakPower should be called when determining indirect power
     */
    default boolean shouldCheckWeakPower(BlockState state, SignalGetter level, BlockPos pos, Direction side) {
        return state.isRedstoneConductor(level, pos);
    }

    /**
     * If this block should be notified of weak changes.
     * Weak changes are changes 1 block away through a solid block.
     * Similar to comparators.
     *
     * @param level The current level
     * @param pos   Block position in level
     * @return true To be notified of changes
     */
    default boolean getWeakChanges(BlockState state, LevelReader level, BlockPos pos) {
        return false;
    }

    /**
     * Sensitive version of getSoundType
     *
     * @param state  The state
     * @param level  The level
     * @param pos    The position. Note that the level may not necessarily have {@code state} here!
     * @param entity The entity that is breaking/stepping on/placing/hitting/falling on this block, or null if no entity is in this context
     * @return A SoundType to use
     */
    default SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        return state.getSoundType();
    }

    /**
     * @param state     The state
     * @param level     The level
     * @param pos       The position of this state
     * @param beaconPos The position of the beacon
     * @return An Integer ARGB value to be averaged with a beacon's existing beam color, or null to do nothing to the beam
     */
    @Nullable
    default Integer getBeaconColorMultiplier(BlockState state, LevelReader level, BlockPos pos, BlockPos beaconPos) {
        if (self() instanceof BeaconBeamBlock)
            return ((BeaconBeamBlock) self()).getColor().getTextureDiffuseColor();
        return null;
    }

    /**
     * Used to determine the state 'viewed' by an entity (see
     * {@link Camera#getBlockAtCamera()}).
     * Can be used by fluid blocks to determine if the viewpoint is within the fluid or not.
     *
     * @param state     the state
     * @param level     the level
     * @param pos       the position
     * @param viewpoint the viewpoint
     * @return the block state that should be 'seen'
     */
    default BlockState getStateAtViewpoint(BlockState state, BlockGetter level, BlockPos pos, Vec3 viewpoint) {
        return state;
    }

    /**
     * Gets the path type of this block when an entity is pathfinding. When
     * {@code null}, uses vanilla behavior.
     *
     * @param state the state of the block
     * @param level the level which contains this block
     * @param pos   the position of the block
     * @param mob   the mob currently pathfinding, may be {@code null}
     * @return the path type of this block
     */
    @Nullable
    default PathType getBlockPathType(BlockState state, BlockGetter level, BlockPos pos, @Nullable Mob mob) {
        return state.getBlock() == Blocks.LAVA ? PathType.LAVA : state.isBurning(level, pos) ? PathType.DAMAGE_FIRE : null;
    }

    /**
     * Gets the path type of the adjacent block to a pathfinding entity.
     * Path types with a negative malus are not traversable for the entity.
     * Pathfinding entities will favor paths consisting of a lower malus.
     * When {@code null}, uses vanilla behavior.
     *
     * @param state        the state of the block
     * @param level        the level which contains this block
     * @param pos          the position of the block
     * @param mob          the mob currently pathfinding, may be {@code null}
     * @param originalType the path type of the source the entity is on
     * @return the path type of this block
     */
    @Nullable
    default PathType getAdjacentBlockPathType(BlockState state, BlockGetter level, BlockPos pos, @Nullable Mob mob, PathType originalType) {
        if (state.is(Blocks.SWEET_BERRY_BUSH)) return PathType.DANGER_OTHER;
        else if (WalkNodeEvaluator.isBurningBlock(state)) return PathType.DANGER_FIRE;
        else return null;
    }

    /**
     * @param state The state
     * @return true if the block is sticky block which used for pull or push adjacent blocks (use by piston)
     */
    default boolean isSlimeBlock(BlockState state) {
        return state.getBlock() == Blocks.SLIME_BLOCK;
    }

    /**
     * @param state The state
     * @return true if the block is sticky block which used for pull or push adjacent blocks (use by piston)
     */
    default boolean isStickyBlock(BlockState state) {
        return state.getBlock() == Blocks.SLIME_BLOCK || state.getBlock() == Blocks.HONEY_BLOCK;
    }

    /**
     * Determines if this block can stick to another block when pushed by a piston.
     *
     * @param state My state
     * @param other Other block
     * @return True to link blocks
     */
    default boolean canStickTo(BlockState state, BlockState other) {
        if (state.getBlock() == Blocks.HONEY_BLOCK && other.getBlock() == Blocks.SLIME_BLOCK) return false;
        if (state.getBlock() == Blocks.SLIME_BLOCK && other.getBlock() == Blocks.HONEY_BLOCK) return false;
        return state.isStickyBlock() || other.isStickyBlock();
    }

    /**
     * Chance that fire will spread and consume this block.
     * 300 being a 100% chance, 0, being a 0% chance.
     *
     * @param state     The current state
     * @param level     The current level
     * @param pos       Block position in level
     * @param direction The direction that the fire is coming from
     * @return A number ranging from 0 to 300 relating used to determine if the block will be consumed by fire
     */
    default int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return ((FireBlock) Blocks.FIRE).getBurnOdds(state);
    }

    /**
     * Called when fire is updating, checks if a block face can catch fire.
     *
     *
     * @param state     The current state
     * @param level     The current level
     * @param pos       Block position in level
     * @param direction The direction that the fire is coming from
     * @return True if the face can be on fire, false otherwise.
     */
    default boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getFlammability(level, pos, direction) > 0;
    }

    /**
     * If the block is flammable, this is called when it gets lit on fire.
     *
     * @param state     The current state
     * @param level     The current level
     * @param pos       Block position in level
     * @param direction The direction that the fire is coming from
     * @param igniter   The entity that lit the fire
     */
    default void onCaughtFire(BlockState state, Level level, BlockPos pos, @Nullable Direction direction, @Nullable LivingEntity igniter) {}

    /**
     * Called when fire is updating on a neighbor block.
     * The higher the number returned, the faster fire will spread around this block.
     *
     * @param state     The current state
     * @param level     The current level
     * @param pos       Block position in level
     * @param direction The direction that the fire is coming from
     * @return A number that is used to determine the speed of fire growth around the block
     */
    default int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return ((FireBlock) Blocks.FIRE).getIgniteOdds(state);
    }

    /**
     * Currently only called by fire when it is on top of this block.
     * Returning true will prevent the fire from naturally dying during updating.
     * Also prevents firing from dying from rain.
     *
     * @param state     The current state
     * @param level     The current level
     * @param pos       Block position in level
     * @param direction The direction that the fire is coming from
     * @return True if this block sustains fire, meaning it will never go out.
     */
    default boolean isFireSource(BlockState state, LevelReader level, BlockPos pos, Direction direction) {
        return state.is(level.dimensionType().infiniburn());
    }

    /**
     * Determines if this block is can be destroyed by the specified entities normal behavior.
     *
     * @param state The current state
     * @param level The current level
     * @param pos   Block position in level
     * @return True to allow the ender dragon to destroy this block
     */
    default boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        if (entity instanceof EnderDragon) {
            return !this.self().defaultBlockState().is(BlockTags.DRAGON_IMMUNE);
        } else if ((entity instanceof WitherBoss) ||
                (entity instanceof WitherSkull)) {
                    return state.isAir() || WitherBoss.canDestroy(state);
                }

        return true;
    }

    /**
     * Determines if this block should drop loot when exploded.
     */
    default boolean canDropFromExplosion(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        return state.getBlock().dropFromExplosion(explosion);
    }

    /**
     * Called when the block is destroyed by an explosion.
     * Useful for allowing the block to take into account tile entities,
     * state, etc. when exploded, before it is removed.
     *
     * @param level     The current level
     * @param pos       Block position in level
     * @param explosion The explosion instance affecting the block
     */
    default void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        self().wasExploded(level, pos, explosion);
    }

    /**
     * Determines if this block's collision box should be treated as though it can extend above its block space.
     * Use this to replicate fence and wall behavior.
     */
    default boolean collisionExtendsVertically(BlockState state, BlockGetter level, BlockPos pos, Entity collidingEntity) {
        return state.is(BlockTags.FENCES) || state.is(BlockTags.WALLS) || self() instanceof FenceGateBlock;
    }

    /**
     * Called to determine whether this block should use the fluid overlay texture or flowing texture when it is placed under the fluid.
     *
     * @param state      The current state
     * @param level      The level
     * @param pos        Block position in level
     * @param fluidState The state of the fluid
     * @return Whether the fluid overlay texture should be used
     */
    default boolean shouldDisplayFluidOverlay(BlockState state, BlockAndTintGetter level, BlockPos pos, FluidState fluidState) {
        return state.getBlock() instanceof HalfTransparentBlock || state.getBlock() instanceof LeavesBlock;
    }

    /**
     * Returns the state that this block should transform into when right-clicked by a tool.
     * For example: Used to determine if {@link ItemAbilities#AXE_STRIP an axe can strip},
     * {@link ItemAbilities#SHOVEL_FLATTEN a shovel can path}, or {@link ItemAbilities#HOE_TILL a hoe can till}.
     * Returns {@code null} if nothing should happen.
     *
     * @param state       The current state
     * @param context     The use on context that the action was performed in
     * @param itemAbility The action being performed by the tool
     * @param simulate    If {@code true}, no actions that modify the world in any way should be performed. If {@code false}, the world may be modified.
     * @return The resulting state after the action has been performed
     */
    @Nullable
    default BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility itemAbility, boolean simulate) {
        ItemStack itemStack = context.getItemInHand();
        if (!itemStack.canPerformAction(itemAbility))
            return null;

        if (ItemAbilities.AXE_STRIP == itemAbility) {
            return AxeItem.getAxeStrippingState(state);
        } else if (ItemAbilities.AXE_SCRAPE == itemAbility) {
            return WeatheringCopper.getPrevious(state).orElse(null);
        } else if (ItemAbilities.AXE_WAX_OFF == itemAbility) {
            Block waxOffBlock = DataMapHooks.getBlockUnwaxed(state.getBlock());
            return Optional.ofNullable(waxOffBlock).map(block -> block.withPropertiesOf(state)).orElse(null);
        } else if (ItemAbilities.SHOVEL_FLATTEN == itemAbility) {
            return ShovelItem.getShovelPathingState(state);
        } else if (ItemAbilities.HOE_TILL == itemAbility) {
            // Logic copied from HoeItem#TILLABLES; needs to be kept in sync during updating
            Block block = state.getBlock();
            if (block == Blocks.ROOTED_DIRT) {
                if (!simulate && !context.getLevel().isClientSide) {
                    Block.popResourceFromFace(context.getLevel(), context.getClickedPos(), context.getClickedFace(), new ItemStack(Items.HANGING_ROOTS));
                }
                return Blocks.DIRT.defaultBlockState();
            } else if ((block == Blocks.GRASS_BLOCK || block == Blocks.DIRT_PATH || block == Blocks.DIRT || block == Blocks.COARSE_DIRT) &&
                    context.getLevel().getBlockState(context.getClickedPos().above()).isAir()) {
                        return block == Blocks.COARSE_DIRT ? Blocks.DIRT.defaultBlockState() : Blocks.FARMLAND.defaultBlockState();
                    }
        } else if (ItemAbilities.SHEARS_TRIM == itemAbility) {
            if (state.getBlock() instanceof GrowingPlantHeadBlock growingPlant && !growingPlant.isMaxAge(state)) {
                if (!simulate)
                    context.getLevel().playSound(context.getPlayer(), context.getClickedPos(), SoundEvents.GROWING_PLANT_CROP, SoundSource.BLOCKS, 1.0F, 1.0F);
                return growingPlant.getMaxAgeState(state);
            }
        } else if (ItemAbilities.SHOVEL_DOUSE == itemAbility) {
            if (state.getBlock() instanceof CampfireBlock && state.getValue(CampfireBlock.LIT)) {
                if (!simulate) {
                    CampfireBlock.dowse(context.getPlayer(), context.getLevel(), context.getClickedPos(), state);
                }
                return state.setValue(CampfireBlock.LIT, Boolean.valueOf(false));
            }
        } else if (ItemAbilities.FIRESTARTER_LIGHT == itemAbility) {
            if (CampfireBlock.canLight(state) || CandleBlock.canLight(state) || CandleCakeBlock.canLight(state)) {
                return state.setValue(BlockStateProperties.LIT, Boolean.valueOf(true));
            }
        }

        return null;
    }

    /**
     * Checks if a player or entity handles movement on this block like scaffolding.
     *
     * @param state  The current state
     * @param level  The current level
     * @param pos    The block position in level
     * @param entity The entity on the scaffolding
     * @return True if the block should act like scaffolding
     */
    default boolean isScaffolding(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
        return state.is(Blocks.SCAFFOLDING);
    }

    /**
     * Whether redstone dust should visually connect to this block on a given side
     * <p>
     * The default implementation is identical to
     * {@code RedStoneWireBlock#shouldConnectTo(BlockState, Direction)}
     *
     * <p>
     * {@link RedStoneWireBlock} updates its visual connection when
     * {@link BlockState#updateShape(Direction, BlockState, LevelAccessor, BlockPos, BlockPos)}
     * is called, this callback is used during the evaluation of its new shape.
     *
     * @param state     The current state
     * @param level     The level
     * @param pos       The block position in level
     * @param direction The coming direction of the redstone dust connection (with respect to the block at pos)
     * @return True if redstone dust should visually connect on the side passed
     *         <p>
     *         If the return value is evaluated based on level and pos (e.g. from BlockEntity), then the implementation of
     *         this block should notify its neighbors to update their shapes when necessary. Consider using
     *         {@link BlockState#updateNeighbourShapes(LevelAccessor, BlockPos, int, int)} or
     *         {@link BlockState#updateShape(Direction, BlockState, LevelAccessor, BlockPos, BlockPos)}.
     *         <p>
     *         Example:
     *         <p>
     *         1. {@code yourBlockState.updateNeighbourShapes(level, yourBlockPos, UPDATE_ALL);}
     *         <p>
     *         2. {@code neighborState.updateShape(fromDirection, stateOfYourBlock, level, neighborBlockPos, yourBlockPos)},
     *         where {@code fromDirection} is defined from the neighbor block's point of view.
     */
    default boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        if (state.is(Blocks.REDSTONE_WIRE)) {
            return true;
        } else if (state.is(Blocks.REPEATER)) {
            Direction facing = state.getValue(RepeaterBlock.FACING);
            return facing == direction || facing.getOpposite() == direction;
        } else if (state.is(Blocks.OBSERVER)) {
            return direction == state.getValue(ObserverBlock.FACING);
        } else {
            return state.isSignalSource() && direction != null;
        }
    }

    /**
     * Whether this block hides the neighbors face pointed towards by the given direction.
     * <p>
     * This method should only be used for blocks you don't control, for your own blocks override
     * {@link Block#skipRendering(BlockState, BlockState, Direction)} on the respective block instead
     * <p>
     * <b>Note that this method may be called on any of the client's meshing threads.</b><br/>
     * As such, if you need any data from your {@link BlockEntity}, you should put it in {@link ModelData} to guarantee
     * safe concurrent access to it on the client.<br/>
     * {@link IBlockGetterExtension#getModelData(BlockPos)} will return the {@link ModelData} for the queried block,
     * or {@link ModelData#EMPTY} if none is present.
     *
     * @param level         The world
     * @param pos           The blocks position in the world
     * @param state         The blocks {@link BlockState}
     * @param neighborState The neighboring blocks {@link BlockState}
     * @param dir           The direction towards the neighboring block
     */
    default boolean hidesNeighborFace(BlockGetter level, BlockPos pos, BlockState state, BlockState neighborState, Direction dir) {
        return false;
    }

    /**
     * Whether this block allows a neighboring block to hide the face of this block it touches.
     * If this returns true, {@link IBlockStateExtension#hidesNeighborFace(BlockGetter, BlockPos, BlockState, Direction)}
     * will be called on the neighboring block.
     */
    default boolean supportsExternalFaceHiding(BlockState state) {
        if (FMLEnvironment.dist.isClient()) {
            return !ClientHooks.isBlockInSolidLayer(state);
        }
        return true;
    }

    /**
     * Called after the {@link BlockState} at the given {@link BlockPos} was changed and neighbors were updated.
     * This method is called on the server and client side.
     * Modifying the level is disallowed in this method.
     * Useful for calculating additional data based on the new state and the neighbor's reactions to the state change.
     *
     * @param level    The level the state was modified in
     * @param pos      The blocks position in the level
     * @param oldState The previous state of the block at the given position, may be a different block than this one
     * @param newState The new state of the block at the given position
     */
    default void onBlockStateChange(LevelReader level, BlockPos pos, BlockState oldState, BlockState newState) {}

    /**
     * Returns whether the block can be hydrated by a fluid.
     *
     * <p>Hydration is an arbitrary word which depends on the block.
     * <ul>
     * <li>A farmland has moisture</li>
     * <li>A sponge can soak up the liquid</li>
     * <li>A coral can live</li>
     * </ul>
     *
     * @param state    the state of the block being hydrated
     * @param getter   the getter which can get the block
     * @param pos      the position of the block being hydrated
     * @param fluid    the state of the fluid
     * @param fluidPos the position of the fluid
     * @return {@code true} if the block can be hydrated, {@code false} otherwise
     */
    default boolean canBeHydrated(BlockState state, BlockGetter getter, BlockPos pos, FluidState fluid, BlockPos fluidPos) {
        return fluid.canHydrate(getter, fluidPos, state, pos);
    }

    /**
     * Returns the {@link MapColor} shown on the map.
     *
     * @param state        The state of this block
     * @param level        The level this block is in
     * @param pos          The blocks position in the level
     * @param defaultColor The {@code MapColor} configured for the given {@code BlockState} in the {@link BlockBehaviour.Properties}
     */
    default MapColor getMapColor(BlockState state, BlockGetter level, BlockPos pos, MapColor defaultColor) {
        return defaultColor;
    }

    /**
     * Returns the {@link BlockState} that this block reports to look like on the given side, for querying by other mods.
     * Note: Overriding this does not change how this block renders. That must still be handled in the block's model.
     * <p>
     * Common implementors would be covers and facades, or any other mimic blocks that proxy another block's model.
     * Common consumers would be models with connected textures that wish to seamlessly connect to mimic blocks.
     * <p>
     * <b>Note that this method may be called on the server, or on any of the client's meshing threads.</b><br/>
     * As such, if you need any data from your {@link BlockEntity}, you should put it in {@link ModelData} to guarantee
     * safe concurrent access to it on the client.<br/>
     * Calling {@link ILevelExtension#getModelDataManager()} will return {@code null} if in a server context, where it is
     * safe to query your {@link BlockEntity} directly. Otherwise, {@link IBlockGetterExtension#getModelData(BlockPos)} will return
     * the {@link ModelData} for the queried block, or {@link ModelData#EMPTY} if none is present.
     *
     * @param state      The state of this block
     * @param level      The level this block is in
     * @param pos        The block's position in the level
     * @param side       The side of the block that is being queried
     * @param queryState The state of the block that is querying the appearance, or {@code null} if not applicable
     * @param queryPos   The position of the block that is querying the appearance, or {@code null} if not applicable
     * @return The appearance of this block on the given side. By default, the current state
     * @see IBlockStateExtension#getAppearance(BlockAndTintGetter, BlockPos, Direction, BlockState, BlockPos)
     */
    default BlockState getAppearance(BlockState state, BlockAndTintGetter level, BlockPos pos, Direction side, @Nullable BlockState queryState, @Nullable BlockPos queryPos) {
        return state;
    }

    /**
     * Returns the reaction of the block when pushed or pulled by a piston. This method should be not called directly, instead via {@link BlockState#getPistonPushReaction()}.
     * <ul>
     * <li>NORMAL: is pushable and pullable by sticky pistons</li>
     * <li>DESTROY: is being destroyed on pushing and pulling</li>
     * <li>BLOCK: is not being able to be moved</li>
     * <li>IGNORE: only usable by entities</li>
     * <li>PUSH_ONLY: can only be pushed, blocks on trying to be pulled</li>
     * <li>{@code null}: use the PistonPushReaction from the BlockBehaviour.Properties passed into the Block Constructor</li>
     * </ul>
     *
     * @param state The state of this block
     * @return the PushReaction of this state or {@code null} if the one passed into the block properties should be used
     */
    @Nullable
    default PushReaction getPistonPushReaction(BlockState state) {
        return null;
    }

    /**
     * Return true if the state is able to be replaced with Blocks.AIR in chunk sections that is entirely made of blocks that return true for isEmpty
     *
     * @param state The current state
     * @return True if the block should be allowed to be optimized away into Blocks.AIR
     */
    default boolean isEmpty(BlockState state) {
        return state.is(Blocks.AIR) || state.is(Blocks.CAVE_AIR) || state.is(Blocks.VOID_AIR);
    }

    /**
     * Determines if this block can spawn Bubble Columns and if so, what direction the column flows.
     * <p>
     * NOTE: The block itself will still need to call {@link net.minecraft.world.level.block.BubbleColumnBlock#updateColumn(LevelAccessor, BlockPos, BlockState)} in their tick method and schedule a block tick in the block's onPlace.
     * Also, schedule a fluid tick in updateShape method if update direction is up. Both are needed in order to get the Bubble Columns to function properly. See {@link net.minecraft.world.level.block.SoulSandBlock} and {@link net.minecraft.world.level.block.MagmaBlock} for example.
     *
     * @param state The current state
     * @return BubbleColumnDirection.NONE for no Bubble Column. Otherwise, will spawn Bubble Column flowing with specified direction
     */
    default BubbleColumnDirection getBubbleColumnDirection(BlockState state) {
        if (state.is(Blocks.SOUL_SAND)) {
            return BubbleColumnDirection.UPWARD;
        } else if (state.is(Blocks.MAGMA_BLOCK)) {
            return BubbleColumnDirection.DOWNWARD;
        } else {
            return BubbleColumnDirection.NONE;
        }
    }

    /**
     * Determines if a fluid adjacent to the block on the given side should not be rendered.
     *
     * @param state         the block state of the block
     * @param selfFace      the face of this block that the fluid is adjacent to
     * @param adjacentFluid the fluid that is touching that face
     * @return true if this block should cause the fluid's face to not render
     */
    default boolean shouldHideAdjacentFluidFace(BlockState state, Direction selfFace, FluidState adjacentFluid) {
        return state.getFluidState().getType().isSame(adjacentFluid.getType());
    }
}
