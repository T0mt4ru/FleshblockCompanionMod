package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import java.util.Set;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/**
 * A LootItemCondition that matches if the last damage to an entity was done by a player.
 *
 * @see LootContextParams#LAST_DAMAGE_PLAYER
 */
public class LootItemKilledByPlayerCondition implements LootItemCondition {
    private static final LootItemKilledByPlayerCondition INSTANCE = new LootItemKilledByPlayerCondition();
    public static final MapCodec<LootItemKilledByPlayerCondition> CODEC = MapCodec.unit(INSTANCE);

    private LootItemKilledByPlayerCondition() {
    }

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.KILLED_BY_PLAYER;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.LAST_DAMAGE_PLAYER);
    }

    public boolean test(LootContext context) {
        return context.hasParam(LootContextParams.LAST_DAMAGE_PLAYER);
    }

    public static LootItemCondition.Builder killedByPlayer() {
        return () -> INSTANCE;
    }
}
