package net.minecraft.world.item;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

@net.neoforged.fml.common.asm.enumextension.IndexedEnum
@net.neoforged.fml.common.asm.enumextension.NamedEnum(1)
@net.neoforged.fml.common.asm.enumextension.NetworkedEnum(net.neoforged.fml.common.asm.enumextension.NetworkedEnum.NetworkCheck.BIDIRECTIONAL)
public enum Rarity implements StringRepresentable, net.neoforged.fml.common.asm.enumextension.IExtensibleEnum {
    COMMON(0, "common", ChatFormatting.WHITE),
    UNCOMMON(1, "uncommon", ChatFormatting.YELLOW),
    RARE(2, "rare", ChatFormatting.AQUA),
    EPIC(3, "epic", ChatFormatting.LIGHT_PURPLE);

    public static final Codec<Rarity> CODEC = StringRepresentable.fromValues(Rarity::values);
    public static final IntFunction<Rarity> BY_ID = ByIdMap.continuous(p_335877_ -> p_335877_.id, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, Rarity> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, p_335484_ -> p_335484_.id);
    private final int id;
    private final String name;
    private final ChatFormatting color;
    private final java.util.function.UnaryOperator<net.minecraft.network.chat.Style> styleModifier;

    private Rarity(int id, String name, ChatFormatting color) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.styleModifier = style -> style.withColor(color);
    }

    Rarity(int id, String name, java.util.function.UnaryOperator<net.minecraft.network.chat.Style> styleModifier) {
        this.id = id;
        this.name = name;
        this.color = ChatFormatting.BLACK;
        this.styleModifier = styleModifier;
    }

    /** @deprecated NeoForge: Use {@link #getStyleModifier()} */
    @Deprecated
    public ChatFormatting color() {
        return this.color;
    }

    public java.util.function.UnaryOperator<net.minecraft.network.chat.Style> getStyleModifier() {
        return this.styleModifier;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public static net.neoforged.fml.common.asm.enumextension.ExtensionInfo getExtensionInfo() {
        return net.neoforged.fml.common.asm.enumextension.ExtensionInfo.nonExtended(Rarity.class);
    }
}
