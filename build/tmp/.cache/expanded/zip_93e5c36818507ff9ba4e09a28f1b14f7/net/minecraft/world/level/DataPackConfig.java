package net.minecraft.world.level;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;

public class DataPackConfig {
    /**
     * This is the default "Vanilla and nothing else" codec. Should have a more distinct name compared to field_234881_b_
     */
    public static final DataPackConfig DEFAULT = new DataPackConfig(ImmutableList.of("vanilla"), ImmutableList.of());
    public static final Codec<DataPackConfig> CODEC = RecordCodecBuilder.create(
        p_45854_ -> p_45854_.group(
                    Codec.STRING.listOf().fieldOf("Enabled").forGetter(p_151457_ -> p_151457_.enabled),
                    Codec.STRING.listOf().fieldOf("Disabled").forGetter(p_151455_ -> p_151455_.disabled)
                )
                .apply(p_45854_, DataPackConfig::new)
    );
    private final List<String> enabled;
    private final List<String> disabled;

    public DataPackConfig(List<String> enabled, List<String> disabled) {
        this.enabled = com.google.common.collect.Lists.newArrayList(enabled);
        this.disabled = ImmutableList.copyOf(disabled);
    }

    public List<String> getEnabled() {
        return this.enabled;
    }

    public List<String> getDisabled() {
        return this.disabled;
    }

    public void addModPacks(List<String> modPacks) {
        enabled.addAll(modPacks.stream().filter(p->!enabled.contains(p)).collect(java.util.stream.Collectors.toList()));
    }
}
