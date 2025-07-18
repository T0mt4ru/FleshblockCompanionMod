package net.minecraft.server.players;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;

public abstract class StoredUserEntry<T> {
    @Nullable
    private final T user;

    public StoredUserEntry(@Nullable T user) {
        this.user = user;
    }

    @Nullable
    T getUser() {
        return this.user;
    }

    boolean hasExpired() {
        return false;
    }

    protected abstract void serialize(JsonObject data);
}
