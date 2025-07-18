package net.minecraft.server.network;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ClientInformation;

public record CommonListenerCookie(GameProfile gameProfile, int latency, ClientInformation clientInformation, boolean transferred, net.neoforged.neoforge.network.connection.ConnectionType connectionType) {
    /**
     * @deprecated Use {@link #CommonListenerCookie(GameProfile, int, ClientInformation, boolean, net.neoforged.neoforge.network.connection.ConnectionType)} instead, to indicate whether the connection is modded.
     */
    @Deprecated
    public CommonListenerCookie(GameProfile gameProfile, int latency, ClientInformation clientInformation, boolean transferred) {
        this(gameProfile, latency, clientInformation, transferred, net.neoforged.neoforge.network.connection.ConnectionType.OTHER);
    }

    public static CommonListenerCookie createInitial(GameProfile gameProfile, boolean transferred) {
        return new CommonListenerCookie(gameProfile, 0, ClientInformation.createDefault(), transferred);
    }
}
