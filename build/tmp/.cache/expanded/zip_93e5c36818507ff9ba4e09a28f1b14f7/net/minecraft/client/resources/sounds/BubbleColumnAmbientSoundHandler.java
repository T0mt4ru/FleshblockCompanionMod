package net.minecraft.client.resources.sounds;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BubbleColumnAmbientSoundHandler implements AmbientSoundHandler {
    private final LocalPlayer player;
    private boolean wasInBubbleColumn;
    private boolean firstTick = true;

    public BubbleColumnAmbientSoundHandler(LocalPlayer player) {
        this.player = player;
    }

    @Override
    public void tick() {
        Level level = this.player.level();
        BlockState blockstate = level.getBlockStatesIfLoaded(this.player.getBoundingBox().inflate(0.0, -0.4F, 0.0).deflate(1.0E-6))
            .filter(p_119669_ -> p_119669_.is(Blocks.BUBBLE_COLUMN))
            .findFirst()
            .orElse(null);
        if (blockstate != null) {
            if (!this.wasInBubbleColumn && !this.firstTick && blockstate.is(Blocks.BUBBLE_COLUMN) && !this.player.isSpectator()) {
                boolean flag = blockstate.getValue(BubbleColumnBlock.DRAG_DOWN);
                if (flag) {
                    this.player.playSound(SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_INSIDE, 1.0F, 1.0F);
                } else {
                    this.player.playSound(SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE, 1.0F, 1.0F);
                }
            }

            this.wasInBubbleColumn = true;
        } else {
            this.wasInBubbleColumn = false;
        }

        this.firstTick = false;
    }
}
