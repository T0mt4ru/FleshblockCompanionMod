package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public class UsedEnderEyeTrigger extends SimpleCriterionTrigger<UsedEnderEyeTrigger.TriggerInstance> {
    @Override
    public Codec<UsedEnderEyeTrigger.TriggerInstance> codec() {
        return UsedEnderEyeTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, BlockPos pos) {
        double d0 = player.getX() - (double)pos.getX();
        double d1 = player.getZ() - (double)pos.getZ();
        double d2 = d0 * d0 + d1 * d1;
        this.trigger(player, p_73934_ -> p_73934_.matches(d2));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, MinMaxBounds.Doubles distance)
        implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<UsedEnderEyeTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            p_337402_ -> p_337402_.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(UsedEnderEyeTrigger.TriggerInstance::player),
                        MinMaxBounds.Doubles.CODEC
                            .optionalFieldOf("distance", MinMaxBounds.Doubles.ANY)
                            .forGetter(UsedEnderEyeTrigger.TriggerInstance::distance)
                    )
                    .apply(p_337402_, UsedEnderEyeTrigger.TriggerInstance::new)
        );

        public boolean matches(double distanceSq) {
            return this.distance.matchesSqr(distanceSq);
        }
    }
}
