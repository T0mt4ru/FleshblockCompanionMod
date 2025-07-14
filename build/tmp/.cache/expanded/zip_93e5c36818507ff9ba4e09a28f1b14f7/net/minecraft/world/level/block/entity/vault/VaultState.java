package net.minecraft.world.level.block.entity.vault;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public enum VaultState implements StringRepresentable {
    INACTIVE("inactive", VaultState.LightLevel.HALF_LIT) {
        @Override
        protected void onEnter(ServerLevel p_324512_, BlockPos p_324300_, VaultConfig p_323552_, VaultSharedData p_324096_, boolean p_338586_) {
            p_324096_.setDisplayItem(ItemStack.EMPTY);
            p_324512_.levelEvent(3016, p_324300_, p_338586_ ? 1 : 0);
        }
    },
    ACTIVE("active", VaultState.LightLevel.LIT) {
        @Override
        protected void onEnter(ServerLevel p_324513_, BlockPos p_324445_, VaultConfig p_323855_, VaultSharedData p_323750_, boolean p_338489_) {
            if (!p_323750_.hasDisplayItem()) {
                VaultBlockEntity.Server.cycleDisplayItemFromLootTable(p_324513_, this, p_323855_, p_323750_, p_324445_);
            }

            p_324513_.levelEvent(3015, p_324445_, p_338489_ ? 1 : 0);
        }
    },
    UNLOCKING("unlocking", VaultState.LightLevel.LIT) {
        @Override
        protected void onEnter(ServerLevel p_324077_, BlockPos p_323729_, VaultConfig p_323520_, VaultSharedData p_323550_, boolean p_338182_) {
            p_324077_.playSound(null, p_323729_, SoundEvents.VAULT_INSERT_ITEM, SoundSource.BLOCKS);
        }
    },
    EJECTING("ejecting", VaultState.LightLevel.LIT) {
        @Override
        protected void onEnter(ServerLevel p_324167_, BlockPos p_324285_, VaultConfig p_324106_, VaultSharedData p_324596_, boolean p_338590_) {
            p_324167_.playSound(null, p_324285_, SoundEvents.VAULT_OPEN_SHUTTER, SoundSource.BLOCKS);
        }

        @Override
        protected void onExit(ServerLevel p_323987_, BlockPos p_324064_, VaultConfig p_323588_, VaultSharedData p_324224_) {
            p_323987_.playSound(null, p_324064_, SoundEvents.VAULT_CLOSE_SHUTTER, SoundSource.BLOCKS);
        }
    };

    private static final int UPDATE_CONNECTED_PLAYERS_TICK_RATE = 20;
    private static final int DELAY_BETWEEN_EJECTIONS_TICKS = 20;
    private static final int DELAY_AFTER_LAST_EJECTION_TICKS = 20;
    private static final int DELAY_BEFORE_FIRST_EJECTION_TICKS = 20;
    private final String stateName;
    private final VaultState.LightLevel lightLevel;

    VaultState(String stateName, VaultState.LightLevel lightLevel) {
        this.stateName = stateName;
        this.lightLevel = lightLevel;
    }

    @Override
    public String getSerializedName() {
        return this.stateName;
    }

    public int lightLevel() {
        return this.lightLevel.value;
    }

    public VaultState tickAndGetNext(ServerLevel level, BlockPos pos, VaultConfig config, VaultServerData serverData, VaultSharedData sharedData) {
        return switch (this) {
            case INACTIVE -> updateStateForConnectedPlayers(level, pos, config, serverData, sharedData, config.activationRange());
            case ACTIVE -> updateStateForConnectedPlayers(level, pos, config, serverData, sharedData, config.deactivationRange());
            case UNLOCKING -> {
                serverData.pauseStateUpdatingUntil(level.getGameTime() + 20L);
                yield EJECTING;
            }
            case EJECTING -> {
                if (serverData.getItemsToEject().isEmpty()) {
                    serverData.markEjectionFinished();
                    yield updateStateForConnectedPlayers(level, pos, config, serverData, sharedData, config.deactivationRange());
                } else {
                    float f = serverData.ejectionProgress();
                    this.ejectResultItem(level, pos, serverData.popNextItemToEject(), f);
                    sharedData.setDisplayItem(serverData.getNextItemToEject());
                    boolean flag = serverData.getItemsToEject().isEmpty();
                    int i = flag ? 20 : 20;
                    serverData.pauseStateUpdatingUntil(level.getGameTime() + (long)i);
                    yield EJECTING;
                }
            }
        };
    }

    private static VaultState updateStateForConnectedPlayers(
        ServerLevel level, BlockPos pos, VaultConfig config, VaultServerData severData, VaultSharedData sharedData, double deactivationRange
    ) {
        sharedData.updateConnectedPlayersWithinRange(level, pos, severData, config, deactivationRange);
        severData.pauseStateUpdatingUntil(level.getGameTime() + 20L);
        return sharedData.hasConnectedPlayers() ? ACTIVE : INACTIVE;
    }

    public void onTransition(
        ServerLevel level, BlockPos pos, VaultState state, VaultConfig config, VaultSharedData sharedData, boolean isOminous
    ) {
        this.onExit(level, pos, config, sharedData);
        state.onEnter(level, pos, config, sharedData, isOminous);
    }

    protected void onEnter(ServerLevel level, BlockPos pos, VaultConfig config, VaultSharedData sharedData, boolean isOminous) {
    }

    protected void onExit(ServerLevel level, BlockPos pos, VaultConfig config, VaultSharedData sharedData) {
    }

    private void ejectResultItem(ServerLevel level, BlockPos pos, ItemStack stack, float ejectionProgress) {
        DefaultDispenseItemBehavior.spawnItem(level, stack, 2, Direction.UP, Vec3.atBottomCenterOf(pos).relative(Direction.UP, 1.2));
        level.levelEvent(3017, pos, 0);
        level.playSound(null, pos, SoundEvents.VAULT_EJECT_ITEM, SoundSource.BLOCKS, 1.0F, 0.8F + 0.4F * ejectionProgress);
    }

    static enum LightLevel {
        HALF_LIT(6),
        LIT(12);

        final int value;

        private LightLevel(int value) {
            this.value = value;
        }
    }
}
