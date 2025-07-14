package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class SmithingTableBlock extends CraftingTableBlock {
    public static final MapCodec<SmithingTableBlock> CODEC = simpleCodec(SmithingTableBlock::new);
    private static final Component CONTAINER_TITLE = Component.translatable("container.upgrade");

    @Override
    public MapCodec<SmithingTableBlock> codec() {
        return CODEC;
    }

    public SmithingTableBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider(
            (p_277304_, p_277305_, p_277306_) -> new SmithingMenu(p_277304_, p_277305_, ContainerLevelAccess.create(level, pos)), CONTAINER_TITLE
        );
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            player.openMenu(state.getMenuProvider(level, pos));
            player.awardStat(Stats.INTERACT_WITH_SMITHING_TABLE);
            return InteractionResult.CONSUME;
        }
    }
}
