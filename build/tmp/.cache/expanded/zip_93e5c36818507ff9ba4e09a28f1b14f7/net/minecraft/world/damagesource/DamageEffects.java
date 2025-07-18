package net.minecraft.world.damagesource;

import com.mojang.serialization.Codec;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.StringRepresentable;

@net.neoforged.fml.common.asm.enumextension.NamedEnum
@net.neoforged.fml.common.asm.enumextension.NetworkedEnum(net.neoforged.fml.common.asm.enumextension.NetworkedEnum.NetworkCheck.CLIENTBOUND)
public enum DamageEffects implements StringRepresentable, net.neoforged.fml.common.asm.enumextension.IExtensibleEnum {
    HURT("hurt", SoundEvents.PLAYER_HURT),
    THORNS("thorns", SoundEvents.THORNS_HIT),
    DROWNING("drowning", SoundEvents.PLAYER_HURT_DROWN),
    BURNING("burning", SoundEvents.PLAYER_HURT_ON_FIRE),
    POKING("poking", SoundEvents.PLAYER_HURT_SWEET_BERRY_BUSH),
    FREEZING("freezing", SoundEvents.PLAYER_HURT_FREEZE);

    public static final Codec<DamageEffects> CODEC = StringRepresentable.fromEnum(DamageEffects::values);
    private final String id;
    @Deprecated // Neo: Always set to null. Use the getter.
    private final SoundEvent sound;
    private final java.util.function.Supplier<SoundEvent> soundSupplier;

    @net.neoforged.fml.common.asm.enumextension.ReservedConstructor
    private DamageEffects(String id, SoundEvent sound) {
        this(id, () -> sound);
    }

    /**
     * Creates a new DamageEffects with the specified ID and sound. Store the created enum in a static final field.<p>
     * Use an enumextender.json to link to your {@link net.neoforged.fml.common.asm.enumextension.EnumProxy} in order to create new entries for this enum.<p>
     * Example usage:
     * {@snippet :
     * public static final EnumProxy<DamageEffects> ELECTRIFYING = new EnumProxy<>(
     *             DamageEffects.class,
     *             "mymod:electrifying",
     *             MySounds.ELECTRIFYING::value);
     * }
     * @param id The {@linkplain StringRepresentable#getSerializedName() serialized name}. Prefix this with your modid and `:`
     * @param sound The sound event that will play when a damage type with this effect deals damage to a player.
     * @apiNote This method must be called as early as possible, as if {@link #CODEC} is resolved before this is called, it will be unusable.
     */
    private DamageEffects(String id, java.util.function.Supplier<SoundEvent> sound) {
        this.id = id;
        this.soundSupplier = sound;
        this.sound = null;
    }

    @Override
    public String getSerializedName() {
        return this.id;
    }

    public SoundEvent sound() {
        return this.soundSupplier.get();
    }

    public static net.neoforged.fml.common.asm.enumextension.ExtensionInfo getExtensionInfo() {
        return net.neoforged.fml.common.asm.enumextension.ExtensionInfo.nonExtended(DamageEffects.class);
    }
}
