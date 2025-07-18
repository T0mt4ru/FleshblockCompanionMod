package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public record NbtPredicate(CompoundTag tag) {
    public static final Codec<NbtPredicate> CODEC = TagParser.LENIENT_CODEC.xmap(NbtPredicate::new, NbtPredicate::tag);
    public static final StreamCodec<ByteBuf, NbtPredicate> STREAM_CODEC = ByteBufCodecs.COMPOUND_TAG.map(NbtPredicate::new, NbtPredicate::tag);

    public boolean matches(ItemStack stack) {
        CustomData customdata = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return customdata.matchedBy(this.tag);
    }

    public boolean matches(Entity entity) {
        return this.matches(getEntityTagToCompare(entity));
    }

    public boolean matches(@Nullable Tag tag) {
        return tag != null && NbtUtils.compareNbt(this.tag, tag, true);
    }

    public static CompoundTag getEntityTagToCompare(Entity entity) {
        CompoundTag compoundtag = entity.saveWithoutId(new CompoundTag());
        if (entity instanceof Player) {
            ItemStack itemstack = ((Player)entity).getInventory().getSelected();
            if (!itemstack.isEmpty()) {
                compoundtag.put("SelectedItem", itemstack.save(entity.registryAccess()));
            }
        }

        return compoundtag;
    }
}
