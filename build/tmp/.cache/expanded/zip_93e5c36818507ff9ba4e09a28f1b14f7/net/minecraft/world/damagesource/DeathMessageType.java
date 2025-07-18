package net.minecraft.world.damagesource;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

@net.neoforged.fml.common.asm.enumextension.NamedEnum
@net.neoforged.fml.common.asm.enumextension.NetworkedEnum(net.neoforged.fml.common.asm.enumextension.NetworkedEnum.NetworkCheck.CLIENTBOUND)
public enum DeathMessageType implements StringRepresentable, net.neoforged.fml.common.asm.enumextension.IExtensibleEnum {
    DEFAULT("default"),
    FALL_VARIANTS("fall_variants"),
    INTENTIONAL_GAME_DESIGN("intentional_game_design");

    public static final Codec<DeathMessageType> CODEC = StringRepresentable.fromEnum(DeathMessageType::values);
    private final String id;
    private final net.neoforged.neoforge.common.damagesource.IDeathMessageProvider msgFunction;

    @net.neoforged.fml.common.asm.enumextension.ReservedConstructor
    private DeathMessageType(String id) {
        this(id, net.neoforged.neoforge.common.damagesource.IDeathMessageProvider.DEFAULT);
    }

    /**
     * Creates a new DeathMessageType with the specified ID and death message provider. Store the created enum in a static final field.<p>
     * 

     * Use an enumextender.json to link to your
     * {@link net.neoforged.fml.common.asm.enumextension.EnumProxy}
     * in order to create new entries for this enum.<p>
     * 

     * Example usage:
     * 

     *
     * {@snippet :
     * public static final EnumProxy<DeathMessageType> CUSTOM_FUNCTION = new EnumProxy<>(
     *             DeathMessageType.class,
     *             "mymod:custom",
     *             MyDeathMessageTypes.CUSTOM_MESSAGE_PROVIDER::value);
     * }
     *
     * @apiNote This method must be called as early as possible, as if {@link #CODEC}
     *          is resolved before this is called, it will be unusable.
     * @param p_id        The {@linkplain StringRepresentable#getSerializedName()
     *                    serialized name}. Prefix this with your modid and `:`
     * @param msgFunction The provider to use for determine when to display the
     *                    message.
     */
    private DeathMessageType(String p_id, net.neoforged.neoforge.common.damagesource.IDeathMessageProvider msgFunction) {
        this.id = p_id;
        this.msgFunction = msgFunction;
    }

    @Override
    public String getSerializedName() {
        return this.id;
    }

    /**
     * The death message function is used when an entity dies to a damage type with this death message type.
     * @return The {@link net.neoforged.neoforge.common.damagesource.IDeathMessageProvider} associated with this death message type.
     */
    public net.neoforged.neoforge.common.damagesource.IDeathMessageProvider getMessageFunction() {
        return this.msgFunction;
    }

    public static net.neoforged.fml.common.asm.enumextension.ExtensionInfo getExtensionInfo() {
        return net.neoforged.fml.common.asm.enumextension.ExtensionInfo.nonExtended(DeathMessageType.class);
    }
}
