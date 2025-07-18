package net.minecraft.client.multiplayer;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ClientConfigurationPacketListener;
import net.minecraft.network.protocol.configuration.ClientboundFinishConfigurationPacket;
import net.minecraft.network.protocol.configuration.ClientboundRegistryDataPacket;
import net.minecraft.network.protocol.configuration.ClientboundResetChatPacket;
import net.minecraft.network.protocol.configuration.ClientboundSelectKnownPacks;
import net.minecraft.network.protocol.configuration.ClientboundUpdateEnabledFeaturesPacket;
import net.minecraft.network.protocol.configuration.ServerboundFinishConfigurationPacket;
import net.minecraft.network.protocol.configuration.ServerboundSelectKnownPacks;
import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ClientConfigurationPacketListenerImpl extends ClientCommonPacketListenerImpl implements ClientConfigurationPacketListener, TickablePacketListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final GameProfile localGameProfile;
    private FeatureFlagSet enabledFeatures;
    private final RegistryAccess.Frozen receivedRegistries;
    private final RegistryDataCollector registryDataCollector = new RegistryDataCollector();
    @Nullable
    private KnownPacksManager knownPacks;
    @Nullable
    protected ChatComponent.State chatState;
    private net.neoforged.neoforge.network.connection.ConnectionType connectionType = net.neoforged.neoforge.network.connection.ConnectionType.OTHER;
    private boolean initializedConnection = false;
    private java.util.Map<net.minecraft.resources.ResourceLocation, net.minecraft.network.chat.Component> failureReasons = new java.util.HashMap<>();

    public ClientConfigurationPacketListenerImpl(Minecraft minecraft, Connection connection, CommonListenerCookie commonListenerCookie) {
        super(minecraft, connection, commonListenerCookie);
        this.localGameProfile = commonListenerCookie.localGameProfile();
        this.receivedRegistries = commonListenerCookie.receivedRegistries();
        this.enabledFeatures = commonListenerCookie.enabledFeatures();
        this.chatState = commonListenerCookie.chatState();
    }

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected();
    }

    @Override
    protected void handleCustomPayload(CustomPacketPayload payload) {
        this.handleUnknownCustomPayload(payload);
    }

    private void handleUnknownCustomPayload(CustomPacketPayload payload) {
        LOGGER.warn("Unknown custom packet payload: {}", payload.type().id());
    }

    @Override
    public void handleRegistryData(ClientboundRegistryDataPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        this.registryDataCollector.appendContents(packet.registry(), packet.entries());
    }

    @Override
    public void handleUpdateTags(ClientboundUpdateTagsPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        this.registryDataCollector.appendTags(packet.getTags());
    }

    @Override
    public void handleEnabledFeatures(ClientboundUpdateEnabledFeaturesPacket packet) {
        this.enabledFeatures = FeatureFlags.REGISTRY.fromNames(packet.features());
        // Neo: Fallback detection layer for vanilla servers
        if (this.connectionType.isOther()) {
            this.initializedConnection = true;
            net.neoforged.neoforge.network.registration.NetworkRegistry.initializeOtherConnection(this);
        }
    }

    @Override
    public void handleSelectKnownPacks(ClientboundSelectKnownPacks packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        if (this.knownPacks == null) {
            this.knownPacks = new KnownPacksManager();
        }

        List<KnownPack> list = this.knownPacks.trySelectingPacks(packet.knownPacks());
        this.send(new ServerboundSelectKnownPacks(list));
    }

    @Override
    public void handleResetChat(ClientboundResetChatPacket packet) {
        this.chatState = null;
    }

    private <T> T runWithResources(Function<ResourceProvider, T> resources) {
        if (this.knownPacks == null) {
            return resources.apply(ResourceProvider.EMPTY);
        } else {
            Object object;
            try (CloseableResourceManager closeableresourcemanager = this.knownPacks.createResourceManager()) {
                object = resources.apply(closeableresourcemanager);
            }

            return (T)object;
        }
    }

    @Override
    public void handleConfigurationFinished(ClientboundFinishConfigurationPacket packet) {
        PacketUtils.ensureRunningOnSameThread(packet, this, this.minecraft);
        RegistryAccess.Frozen registryaccess$frozen = this.runWithResources(
            p_326197_ -> this.registryDataCollector.collectGameRegistries(p_326197_, this.receivedRegistries, this.connection.isMemoryConnection())
        );
        this.connection
            .setupInboundProtocol(
                GameProtocols.CLIENTBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(registryaccess$frozen, this.connectionType)),
                new ClientPacketListener(
                    this.minecraft,
                    this.connection,
                    new CommonListenerCookie(
                        this.localGameProfile,
                        this.telemetryManager,
                        registryaccess$frozen,
                        this.enabledFeatures,
                        this.serverBrand,
                        this.serverData,
                        this.postDisconnectScreen,
                        this.serverCookies,
                        this.chatState,
                        this.strictErrorHandling,
                        this.customReportDetails,
                        this.serverLinks,
                        this.connectionType
                    )
                )
            );
        // Packets can only be sent after the outbound protocol is set up again
        if (!this.initializedConnection && this.connectionType.isOther()) {
            // Neo: Fallback detection for servers with a delayed brand payload (BungeeCord)
            net.neoforged.neoforge.network.registration.NetworkRegistry.initializeOtherConnection(this);
        }
        net.neoforged.neoforge.network.registration.NetworkRegistry.onConfigurationFinished(this);
        this.connection.send(ServerboundFinishConfigurationPacket.INSTANCE);
        this.connection.setupOutboundProtocol(GameProtocols.SERVERBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(registryaccess$frozen, this.connectionType)));
    }

    @Override
    public void tick() {
        this.sendDeferredPackets();
    }

    @Override
    public void onDisconnect(DisconnectionDetails details) {
        super.onDisconnect(details);
        this.minecraft.clearDownloadedResourcePacks();
    }

    @Override
    public void handleCustomPayload(net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket packet) {
        // Handle the query payload by responding with the client's network channels. Update the connection type accordingly.
        if (packet.payload() instanceof net.neoforged.neoforge.network.payload.ModdedNetworkQueryPayload) {
            this.connectionType = net.neoforged.neoforge.network.connection.ConnectionType.NEOFORGE;
            net.neoforged.neoforge.network.registration.NetworkRegistry.onNetworkQuery(this);
            return;
        }

        // Receiving a modded network payload implies a successful negotiation by the server.
        if (packet.payload() instanceof net.neoforged.neoforge.network.payload.ModdedNetworkPayload moddedNetworkPayload) {
            this.initializedConnection = true;
            net.neoforged.neoforge.network.registration.NetworkRegistry.initializeNeoForgeConnection(this, moddedNetworkPayload.setup());
            return;
        }

        // Receiving a setup failed payload will be followed by a disconnect from the server, so we don't need to disconnect manually here.
        if (packet.payload() instanceof net.neoforged.neoforge.network.payload.ModdedNetworkSetupFailedPayload setupFailedPayload) {
            failureReasons = setupFailedPayload.failureReasons();
            return;
        }

        // Receiving a brand payload without having transitioned to a Neo connection implies a non-modded connection has begun.
        if (this.connectionType.isOther() && packet.payload() instanceof net.minecraft.network.protocol.common.custom.BrandPayload) {
            this.initializedConnection = true;
            net.neoforged.neoforge.network.registration.NetworkRegistry.initializeOtherConnection(this);
            // Continue processing the brand payload
        }

        // Fallback to super for un/register, modded, and vanilla payloads.
        super.handleCustomPayload(packet);
    }

    @Override
    protected net.minecraft.client.gui.screens.Screen createDisconnectScreen(DisconnectionDetails p_350769_) {
        final net.minecraft.client.gui.screens.Screen superScreen = super.createDisconnectScreen(p_350769_);
        if (failureReasons.isEmpty())
            return superScreen;

        return new net.neoforged.neoforge.client.gui.ModMismatchDisconnectedScreen(superScreen, net.minecraft.network.chat.Component.translatable("disconnect.lost"), failureReasons);
    }

    public net.neoforged.neoforge.network.connection.ConnectionType getConnectionType() {
        return connectionType;
    }
}
