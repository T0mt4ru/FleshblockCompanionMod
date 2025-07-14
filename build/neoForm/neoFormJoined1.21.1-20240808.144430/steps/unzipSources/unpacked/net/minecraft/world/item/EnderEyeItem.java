package net.minecraft.world.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.StructureTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class EnderEyeItem extends Item {
    public EnderEyeItem(Item.Properties properties) {
        super(properties);
    }

    /**
     * Called when this item is used when targeting a Block
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        BlockState blockstate = level.getBlockState(blockpos);
        if (!blockstate.is(Blocks.END_PORTAL_FRAME) || blockstate.getValue(EndPortalFrameBlock.HAS_EYE)) {
            return InteractionResult.PASS;
        } else if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            BlockState blockstate1 = blockstate.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(true));
            Block.pushEntitiesUp(blockstate, blockstate1, level, blockpos);
            level.setBlock(blockpos, blockstate1, 2);
            level.updateNeighbourForOutputSignal(blockpos, Blocks.END_PORTAL_FRAME);
            context.getItemInHand().shrink(1);
            level.levelEvent(1503, blockpos, 0);
            BlockPattern.BlockPatternMatch blockpattern$blockpatternmatch = EndPortalFrameBlock.getOrCreatePortalShape().find(level, blockpos);
            if (blockpattern$blockpatternmatch != null) {
                BlockPos blockpos1 = blockpattern$blockpatternmatch.getFrontTopLeft().offset(-3, 0, -3);

                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        level.setBlock(blockpos1.offset(i, 0, j), Blocks.END_PORTAL.defaultBlockState(), 2);
                    }
                }

                level.globalLevelEvent(1038, blockpos1.offset(1, 0, 1), 0);
            }

            return InteractionResult.CONSUME;
        }
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 0;
    }

    /**
     * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see {@link #onItemUse}.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        BlockHitResult blockhitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if (blockhitresult.getType() == HitResult.Type.BLOCK && level.getBlockState(blockhitresult.getBlockPos()).is(Blocks.END_PORTAL_FRAME)) {
            return InteractionResultHolder.pass(itemstack);
        } else {
            player.startUsingItem(hand);
            if (level instanceof ServerLevel serverlevel) {
                BlockPos blockpos = serverlevel.findNearestMapStructure(StructureTags.EYE_OF_ENDER_LOCATED, player.blockPosition(), 100, false);
                if (blockpos != null) {
                    EyeOfEnder eyeofender = new EyeOfEnder(level, player.getX(), player.getY(0.5), player.getZ());
                    eyeofender.setItem(itemstack);
                    eyeofender.signalTo(blockpos);
                    level.gameEvent(GameEvent.PROJECTILE_SHOOT, eyeofender.position(), GameEvent.Context.of(player));
                    level.addFreshEntity(eyeofender);
                    if (player instanceof ServerPlayer serverplayer) {
                        CriteriaTriggers.USED_ENDER_EYE.trigger(serverplayer, blockpos);
                    }

                    float f = Mth.lerp(level.random.nextFloat(), 0.33F, 0.5F);
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_EYE_LAUNCH, SoundSource.NEUTRAL, 1.0F, f);
                    itemstack.consume(1, player);
                    player.awardStat(Stats.ITEM_USED.get(this));
                    player.swing(hand, true);
                    return InteractionResultHolder.success(itemstack);
                }
            }

            return InteractionResultHolder.consume(itemstack);
        }
    }
}
