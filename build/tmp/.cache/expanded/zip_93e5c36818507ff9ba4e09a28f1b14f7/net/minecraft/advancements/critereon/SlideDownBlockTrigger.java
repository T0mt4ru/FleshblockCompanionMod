package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class SlideDownBlockTrigger extends SimpleCriterionTrigger<SlideDownBlockTrigger.TriggerInstance> {
    @Override
    public Codec<SlideDownBlockTrigger.TriggerInstance> codec() {
        return SlideDownBlockTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, BlockState state) {
        this.trigger(player, p_66986_ -> p_66986_.matches(state));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<Holder<Block>> block, Optional<StatePropertiesPredicate> state)
        implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<SlideDownBlockTrigger.TriggerInstance> CODEC = RecordCodecBuilder.<SlideDownBlockTrigger.TriggerInstance>create(
                p_344150_ -> p_344150_.group(
                            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(SlideDownBlockTrigger.TriggerInstance::player),
                            BuiltInRegistries.BLOCK.holderByNameCodec().optionalFieldOf("block").forGetter(SlideDownBlockTrigger.TriggerInstance::block),
                            StatePropertiesPredicate.CODEC.optionalFieldOf("state").forGetter(SlideDownBlockTrigger.TriggerInstance::state)
                        )
                        .apply(p_344150_, SlideDownBlockTrigger.TriggerInstance::new)
            )
            .validate(SlideDownBlockTrigger.TriggerInstance::validate);

        private static DataResult<SlideDownBlockTrigger.TriggerInstance> validate(SlideDownBlockTrigger.TriggerInstance triggerInstance) {
            return triggerInstance.block
                .<DataResult<SlideDownBlockTrigger.TriggerInstance>>flatMap(
                    p_311441_ -> triggerInstance.state
                            .<String>flatMap(p_311443_ -> p_311443_.checkState(((Block)p_311441_.value()).getStateDefinition()))
                            .map(p_311445_ -> DataResult.error(() -> "Block" + p_311441_ + " has no property " + p_311445_))
                )
                .orElseGet(() -> DataResult.success(triggerInstance));
        }

        public static Criterion<SlideDownBlockTrigger.TriggerInstance> slidesDownBlock(Block block) {
            return CriteriaTriggers.HONEY_BLOCK_SLIDE
                .createCriterion(new SlideDownBlockTrigger.TriggerInstance(Optional.empty(), Optional.of(block.builtInRegistryHolder()), Optional.empty()));
        }

        public boolean matches(BlockState state) {
            return this.block.isPresent() && !state.is(this.block.get()) ? false : !this.state.isPresent() || this.state.get().matches(state);
        }
    }
}
