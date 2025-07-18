package net.minecraft.world.damagesource;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

@net.neoforged.fml.common.asm.enumextension.NamedEnum
@net.neoforged.fml.common.asm.enumextension.NetworkedEnum(net.neoforged.fml.common.asm.enumextension.NetworkedEnum.NetworkCheck.CLIENTBOUND)
public enum DamageScaling implements StringRepresentable, net.neoforged.fml.common.asm.enumextension.IExtensibleEnum {
    NEVER("never"),
    WHEN_CAUSED_BY_LIVING_NON_PLAYER("when_caused_by_living_non_player"),
    ALWAYS("always");

    public static final Codec<DamageScaling> CODEC = StringRepresentable.fromEnum(DamageScaling::values);
    private final String id;
    private final net.neoforged.neoforge.common.damagesource.IScalingFunction scaling;

    @net.neoforged.fml.common.asm.enumextension.ReservedConstructor
    private DamageScaling(String id) {
        this(id, net.neoforged.neoforge.common.damagesource.IScalingFunction.DEFAULT);
    }

    /**
     * Creates a new DamageScaling with the specified ID and scaling function. Store the created enum in a static final field.<p>
     * 

     * Use an enumextender.json to link to your
     * {@link net.neoforged.fml.common.asm.enumextension.EnumProxy}
     * in order to create new entries for this enum.<p>
     * 

     * Example usage:
     * 

     *
     * {@snippet :
     * public static final EnumProxy<DamageScaling> CUSTOM_FUNCTION = new EnumProxy<>(
     *             DamageScaling.class,
     *             "mymod:custom",
     *             MyDamageScalings.CUSTOM_MESSAGE_PROVIDER::value);
     * }
     *
     * @apiNote This method must be called as early as possible, as if {@link #CODEC}
     *          is resolved before this is called, it will be unusable.
     * @param p_id    The {@linkplain StringRepresentable#getSerializedName()
     *                serialized name}. Prefix this with your modid and `:`
     * @param scaling The scaling function that will be used when a player is hurt by
     *                a damage type with this type of scaling.
     */
    private DamageScaling(String p_id, net.neoforged.neoforge.common.damagesource.IScalingFunction scaling) {
        this.id = p_id;
        this.scaling = scaling;
    }

    @Override
    public String getSerializedName() {
        return this.id;
    }

    /**
     * The scaling function is used when a player is hurt by a damage type with this type of scaling.
     * @return The {@link net.neoforged.neoforge.common.damagesource.IScalingFunction} associated with this damage scaling.
     */
    public net.neoforged.neoforge.common.damagesource.IScalingFunction getScalingFunction() {
        return this.scaling;
    }

    public static net.neoforged.fml.common.asm.enumextension.ExtensionInfo getExtensionInfo() {
        return net.neoforged.fml.common.asm.enumextension.ExtensionInfo.nonExtended(DamageScaling.class);
    }
}
