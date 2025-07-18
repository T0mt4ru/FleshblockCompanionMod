package net.minecraft.client.renderer.block.model.multipart;

import com.google.common.collect.Streams;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OrCondition implements Condition {
    public static final String TOKEN = "OR";
    private final Iterable<? extends Condition> conditions;

    public OrCondition(Iterable<? extends Condition> conditions) {
        this.conditions = conditions;
    }

    @Override
    public Predicate<BlockState> getPredicate(StateDefinition<Block, BlockState> definition) {
        List<Predicate<BlockState>> list = Streams.stream(this.conditions).map(p_112009_ -> p_112009_.getPredicate(definition)).collect(Collectors.toList());
        return p_112012_ -> list.stream().anyMatch(p_173513_ -> p_173513_.test(p_112012_));
    }
}
