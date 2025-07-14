package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public class TntBlock extends Block {
    public static final MapCodec<TntBlock> CODEC = simpleCodec(TntBlock::new);
    public static final BooleanProperty UNSTABLE = BlockStateProperties.UNSTABLE;

    @Override
    public MapCodec<TntBlock> codec() {
        return CODEC;
    }

    public TntBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(UNSTABLE, Boolean.valueOf(false)));
    }

    @Override
    public void onCaughtFire(BlockState state, Level world, BlockPos pos, @Nullable net.minecraft.core.Direction face, @Nullable LivingEntity igniter) {
        explode(world, pos, igniter);
    }

    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!oldState.is(state.getBlock())) {
            if (level.hasNeighborSignal(pos)) {
                onCaughtFire(state, level, pos, null, null);
                level.removeBlock(pos, false);
            }
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (level.hasNeighborSignal(pos)) {
            onCaughtFire(state, level, pos, null, null);
            level.removeBlock(pos, false);
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && !player.isCreative() && state.getValue(UNSTABLE)) {
            onCaughtFire(state, level, pos, null, null);
        }

        return super.playerWillDestroy(level, pos, state, player);
    }

    /**
     * Called when this Block is destroyed by an Explosion
     */
    @Override
    public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
        if (!level.isClientSide) {
            PrimedTnt primedtnt = new PrimedTnt(
                level, (double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5, explosion.getIndirectSourceEntity()
            );
            int i = primedtnt.getFuse();
            primedtnt.setFuse((short)(level.random.nextInt(i / 4) + i / 8));
            level.addFreshEntity(primedtnt);
        }
    }

    @Deprecated //Forge: Prefer using IForgeBlock#catchFire
    public static void explode(Level level, BlockPos pos) {
        explode(level, pos, null);
    }

    @Deprecated //Forge: Prefer using IForgeBlock#catchFire
    private static void explode(Level level, BlockPos pos, @Nullable LivingEntity entity) {
        if (!level.isClientSide) {
            PrimedTnt primedtnt = new PrimedTnt(level, (double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5, entity);
            level.addFreshEntity(primedtnt);
            level.playSound(null, primedtnt.getX(), primedtnt.getY(), primedtnt.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(entity, GameEvent.PRIME_FUSE, pos);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(
        ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
    ) {
        if (!stack.is(Items.FLINT_AND_STEEL) && !stack.is(Items.FIRE_CHARGE)) {
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        } else {
            onCaughtFire(state, level, pos, hitResult.getDirection(), player);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
            Item item = stack.getItem();
            if (stack.is(Items.FLINT_AND_STEEL)) {
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
            } else {
                stack.consume(1, player);
            }

            player.awardStat(Stats.ITEM_USED.get(item));
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
    }

    @Override
    protected void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        if (!level.isClientSide) {
            BlockPos blockpos = hit.getBlockPos();
            Entity entity = projectile.getOwner();
            if (projectile.isOnFire() && projectile.mayInteract(level, blockpos)) {
                onCaughtFire(state, level, blockpos, null, entity instanceof LivingEntity ? (LivingEntity)entity : null);
                level.removeBlock(blockpos, false);
            }
        }
    }

    /**
     * @return whether this block should drop its drops when destroyed by the given explosion
     */
    @Override
    public boolean dropFromExplosion(Explosion explosion) {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(UNSTABLE);
    }
}
