package com.mojang.realmsclient.util.task;

import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerAddress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsConnect;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ConnectTask extends LongRunningTask {
    private static final Component TITLE = Component.translatable("mco.connect.connecting");
    private final RealmsConnect realmsConnect;
    private final RealmsServer server;
    private final RealmsServerAddress address;

    public ConnectTask(Screen onlineScreen, RealmsServer server, RealmsServerAddress address) {
        this.server = server;
        this.address = address;
        this.realmsConnect = new RealmsConnect(onlineScreen);
    }

    @Override
    public void run() {
        this.realmsConnect.connect(this.server, ServerAddress.parseString(this.address.address));
    }

    @Override
    public void abortTask() {
        super.abortTask();
        this.realmsConnect.abort();
        Minecraft.getInstance().getDownloadedPackSource().cleanupAfterDisconnect();
    }

    @Override
    public void tick() {
        this.realmsConnect.tick();
    }

    @Override
    public Component getTitle() {
        return TITLE;
    }
}
