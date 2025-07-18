package net.minecraft.client;

import java.util.function.IntFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ByIdMap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum NarratorStatus {
    OFF(0, "options.narrator.off"),
    ALL(1, "options.narrator.all"),
    CHAT(2, "options.narrator.chat"),
    SYSTEM(3, "options.narrator.system");

    private static final IntFunction<NarratorStatus> BY_ID = ByIdMap.continuous(NarratorStatus::getId, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    private final int id;
    private final Component name;

    private NarratorStatus(int id, String name) {
        this.id = id;
        this.name = Component.translatable(name);
    }

    public int getId() {
        return this.id;
    }

    public Component getName() {
        return this.name;
    }

    public static NarratorStatus byId(int id) {
        return BY_ID.apply(id);
    }

    public boolean shouldNarrateChat() {
        return this == ALL || this == CHAT;
    }

    public boolean shouldNarrateSystem() {
        return this == ALL || this == SYSTEM;
    }
}
