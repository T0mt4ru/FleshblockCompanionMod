package net.minecraft.world.entity.npc;

import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

public class ClientSideMerchant implements Merchant {
    private final Player source;
    private MerchantOffers offers = new MerchantOffers();
    private int xp;

    public ClientSideMerchant(Player source) {
        this.source = source;
    }

    @Override
    public Player getTradingPlayer() {
        return this.source;
    }

    @Override
    public void setTradingPlayer(@Nullable Player player) {
    }

    @Override
    public MerchantOffers getOffers() {
        return this.offers;
    }

    @Override
    public void overrideOffers(MerchantOffers offers) {
        this.offers = offers;
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        offer.increaseUses();
    }

    /**
     * Notifies the merchant of a possible merchant recipe being fulfilled or not. Usually, this is just a sound byte being played depending on whether the suggested {@link net.minecraft.world.item.ItemStack} is not empty.
     */
    @Override
    public void notifyTradeUpdated(ItemStack stack) {
    }

    @Override
    public boolean isClientSide() {
        return this.source.level().isClientSide;
    }

    @Override
    public int getVillagerXp() {
        return this.xp;
    }

    @Override
    public void overrideXp(int xp) {
        this.xp = xp;
    }

    @Override
    public boolean showProgressBar() {
        return true;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.VILLAGER_YES;
    }
}
