package net.minecraft.world.damagesource;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class CombatTracker {
    public static final int RESET_DAMAGE_STATUS_TIME = 100;
    public static final int RESET_COMBAT_STATUS_TIME = 300;
    public static final Style INTENTIONAL_GAME_DESIGN_STYLE = Style.EMPTY
        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://bugs.mojang.com/browse/MCPE-28723"))
        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("MCPE-28723")));
    private final List<CombatEntry> entries = Lists.newArrayList();
    private final LivingEntity mob;
    private int lastDamageTime;
    private int combatStartTime;
    private int combatEndTime;
    private boolean inCombat;
    private boolean takingDamage;

    public CombatTracker(LivingEntity mob) {
        this.mob = mob;
    }

    public void recordDamage(DamageSource source, float damage) {
        this.recheckStatus();
        FallLocation falllocation = FallLocation.getCurrentFallLocation(this.mob);
        CombatEntry combatentry = new CombatEntry(source, damage, falllocation, this.mob.fallDistance);
        this.entries.add(combatentry);
        this.lastDamageTime = this.mob.tickCount;
        this.takingDamage = true;
        if (!this.inCombat && this.mob.isAlive() && shouldEnterCombat(source)) {
            this.inCombat = true;
            this.combatStartTime = this.mob.tickCount;
            this.combatEndTime = this.combatStartTime;
            this.mob.onEnterCombat();
        }
    }

    private static boolean shouldEnterCombat(DamageSource source) {
        return source.getEntity() instanceof LivingEntity;
    }

    private Component getMessageForAssistedFall(Entity entity, Component entityDisplayName, String hasWeaponTranslationKey, String noWeaponTranslationKey) {
        ItemStack itemstack = entity instanceof LivingEntity livingentity ? livingentity.getMainHandItem() : ItemStack.EMPTY;
        return !itemstack.isEmpty() && itemstack.has(DataComponents.CUSTOM_NAME)
            ? Component.translatable(hasWeaponTranslationKey, this.mob.getDisplayName(), entityDisplayName, itemstack.getDisplayName())
            : Component.translatable(noWeaponTranslationKey, this.mob.getDisplayName(), entityDisplayName);
    }

    public Component getFallMessage(CombatEntry combatEntry, @Nullable Entity p_entity) {
        DamageSource damagesource = combatEntry.source();
        if (!damagesource.is(DamageTypeTags.IS_FALL) && !damagesource.is(DamageTypeTags.ALWAYS_MOST_SIGNIFICANT_FALL)) {
            Component component1 = getDisplayName(p_entity);
            Entity entity = damagesource.getEntity();
            Component component = getDisplayName(entity);
            if (component != null && !component.equals(component1)) {
                return this.getMessageForAssistedFall(entity, component, "death.fell.assist.item", "death.fell.assist");
            } else {
                return (Component)(component1 != null
                    ? this.getMessageForAssistedFall(p_entity, component1, "death.fell.finish.item", "death.fell.finish")
                    : Component.translatable("death.fell.killer", this.mob.getDisplayName()));
            }
        } else {
            FallLocation falllocation = Objects.requireNonNullElse(combatEntry.fallLocation(), FallLocation.GENERIC);
            return Component.translatable(falllocation.languageKey(), this.mob.getDisplayName());
        }
    }

    @Nullable
    private static Component getDisplayName(@Nullable Entity entity) {
        return entity == null ? null : entity.getDisplayName();
    }

    public Component getDeathMessage() {
        if (this.entries.isEmpty()) {
            return Component.translatable("death.attack.generic", this.mob.getDisplayName());
        } else {
            CombatEntry combatentry = this.entries.get(this.entries.size() - 1);
            DamageSource damagesource = combatentry.source();
            CombatEntry combatentry1 = this.getMostSignificantFall();
            DeathMessageType deathmessagetype = damagesource.type().deathMessageType();
            // Neo: Implement IDeathMessageProvider#getDeathMessage
            // Vanilla logic is replicated in IDeathMessageProvider.DEFAULT
            if (true) {
                return deathmessagetype.getMessageFunction().getDeathMessage(this.mob, combatentry, combatentry1);
            }
            if (deathmessagetype == DeathMessageType.FALL_VARIANTS && combatentry1 != null) {
                return this.getFallMessage(combatentry1, damagesource.getEntity());
            } else if (deathmessagetype == DeathMessageType.INTENTIONAL_GAME_DESIGN) {
                String s = "death.attack." + damagesource.getMsgId();
                Component component = ComponentUtils.wrapInSquareBrackets(Component.translatable(s + ".link")).withStyle(INTENTIONAL_GAME_DESIGN_STYLE);
                return Component.translatable(s + ".message", this.mob.getDisplayName(), component);
            } else {
                return damagesource.getLocalizedDeathMessage(this.mob);
            }
        }
    }

    @Nullable
    public CombatEntry getMostSignificantFall() {
        CombatEntry combatentry = null;
        CombatEntry combatentry1 = null;
        float f = 0.0F;
        float f1 = 0.0F;

        for (int i = 0; i < this.entries.size(); i++) {
            CombatEntry combatentry2 = this.entries.get(i);
            CombatEntry combatentry3 = i > 0 ? this.entries.get(i - 1) : null;
            DamageSource damagesource = combatentry2.source();
            boolean flag = damagesource.is(DamageTypeTags.ALWAYS_MOST_SIGNIFICANT_FALL);
            float f2 = flag ? Float.MAX_VALUE : combatentry2.fallDistance();
            if ((damagesource.is(DamageTypeTags.IS_FALL) || flag) && f2 > 0.0F && (combatentry == null || f2 > f1)) {
                if (i > 0) {
                    combatentry = combatentry3;
                } else {
                    combatentry = combatentry2;
                }

                f1 = f2;
            }

            if (combatentry2.fallLocation() != null && (combatentry1 == null || combatentry2.damage() > f)) {
                combatentry1 = combatentry2;
                f = combatentry2.damage();
            }
        }

        if (f1 > 5.0F && combatentry != null) {
            return combatentry;
        } else {
            return f > 5.0F && combatentry1 != null ? combatentry1 : null;
        }
    }

    public int getCombatDuration() {
        return this.inCombat ? this.mob.tickCount - this.combatStartTime : this.combatEndTime - this.combatStartTime;
    }

    public void recheckStatus() {
        int i = this.inCombat ? 300 : 100;
        if (this.takingDamage && (!this.mob.isAlive() || this.mob.tickCount - this.lastDamageTime > i)) {
            boolean flag = this.inCombat;
            this.takingDamage = false;
            this.inCombat = false;
            this.combatEndTime = this.mob.tickCount;
            if (flag) {
                this.mob.onLeaveCombat();
            }

            this.entries.clear();
        }
    }
}
