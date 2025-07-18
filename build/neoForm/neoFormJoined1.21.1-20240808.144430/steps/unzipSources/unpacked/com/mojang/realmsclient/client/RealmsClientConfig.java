package com.mojang.realmsclient.client;

import java.net.Proxy;
import javax.annotation.Nullable;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsClientConfig {
    @Nullable
    private static Proxy proxy;

    @Nullable
    public static Proxy getProxy() {
        return proxy;
    }

    public static void setProxy(Proxy p_proxy) {
        if (proxy == null) {
            proxy = p_proxy;
        }
    }
}
