package net.minecraft.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public record LockCode(String key) {
    public static final LockCode NO_LOCK = new LockCode("");
    public static final Codec<LockCode> CODEC = Codec.STRING.xmap(LockCode::new, LockCode::key);
    public static final String TAG_LOCK = "Lock";

    public boolean unlocksWith(ItemStack stack) {
        if (this.key.isEmpty()) {
            return true;
        } else {
            Component component = stack.get(DataComponents.CUSTOM_NAME);
            return component != null && this.key.equals(component.getString());
        }
    }

    public void addToTag(CompoundTag nbt) {
        if (!this.key.isEmpty()) {
            nbt.putString("Lock", this.key);
        }
    }

    public static LockCode fromTag(CompoundTag nbt) {
        return nbt.contains("Lock", 8) ? new LockCode(nbt.getString("Lock")) : NO_LOCK;
    }
}
