package net.minecraft.server.players;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class SleepStatus {
    private int activePlayers;
    private int sleepingPlayers;

    public boolean areEnoughSleeping(int requiredSleepPercentage) {
        return this.sleepingPlayers >= this.sleepersNeeded(requiredSleepPercentage);
    }

    public boolean areEnoughDeepSleeping(int requiredSleepPercentage, List<ServerPlayer> sleepingPlayers) {
        int i = (int)sleepingPlayers.stream().filter(Player::isSleepingLongEnough).count();
        return i >= this.sleepersNeeded(requiredSleepPercentage);
    }

    public int sleepersNeeded(int requiredSleepPercentage) {
        return Math.max(1, Mth.ceil((float)(this.activePlayers * requiredSleepPercentage) / 100.0F));
    }

    public void removeAllSleepers() {
        this.sleepingPlayers = 0;
    }

    public int amountSleeping() {
        return this.sleepingPlayers;
    }

    public boolean update(List<ServerPlayer> players) {
        int i = this.activePlayers;
        int j = this.sleepingPlayers;
        this.activePlayers = 0;
        this.sleepingPlayers = 0;

        for (ServerPlayer serverplayer : players) {
            if (!serverplayer.isSpectator()) {
                this.activePlayers++;
                if (serverplayer.isSleeping()) {
                    this.sleepingPlayers++;
                }
            }
        }

        return (j > 0 || this.sleepingPlayers > 0) && (i != this.activePlayers || j != this.sleepingPlayers);
    }
}
