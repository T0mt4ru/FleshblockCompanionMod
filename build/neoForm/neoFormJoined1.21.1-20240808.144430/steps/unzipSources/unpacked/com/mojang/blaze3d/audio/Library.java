package com.mojang.blaze3d.audio;

import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.OptionalLong;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALC11;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.openal.ALUtil;
import org.lwjgl.openal.SOFTHRTF;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class Library {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int NO_DEVICE = 0;
    private static final int DEFAULT_CHANNEL_COUNT = 30;
    private long currentDevice;
    private long context;
    private boolean supportsDisconnections;
    @Nullable
    private String defaultDeviceName;
    private static final Library.ChannelPool EMPTY = new Library.ChannelPool() {
        @Nullable
        @Override
        public Channel acquire() {
            return null;
        }

        @Override
        public boolean release(Channel p_83708_) {
            return false;
        }

        @Override
        public void cleanup() {
        }

        @Override
        public int getMaxCount() {
            return 0;
        }

        @Override
        public int getUsedCount() {
            return 0;
        }
    };
    private Library.ChannelPool staticChannels = EMPTY;
    private Library.ChannelPool streamingChannels = EMPTY;
    private final Listener listener = new Listener();

    public Library() {
        this.defaultDeviceName = getDefaultDeviceName();
    }

    /**
     * Initializes the OpenAL device and context.
     * @throws IllegalStateException if an error occurs during initialization.
     *
     * @param deviceSpecifier A string specifying the name of the audio device to use,
     *                        or null to use the default device.
     * @param enableHrtf      Whether to enable HRTF (head-related transfer function)
     *                        for spatial audio.
     */
    public void init(@Nullable String deviceSpecifier, boolean enableHrtf) {
        this.currentDevice = openDeviceOrFallback(deviceSpecifier);
        this.supportsDisconnections = false;
        ALCCapabilities alccapabilities = ALC.createCapabilities(this.currentDevice);
        if (OpenAlUtil.checkALCError(this.currentDevice, "Get capabilities")) {
            throw new IllegalStateException("Failed to get OpenAL capabilities");
        } else if (!alccapabilities.OpenALC11) {
            throw new IllegalStateException("OpenAL 1.1 not supported");
        } else {
            this.setHrtf(alccapabilities.ALC_SOFT_HRTF && enableHrtf);

            try (MemoryStack memorystack = MemoryStack.stackPush()) {
                IntBuffer intbuffer = memorystack.callocInt(3).put(6554).put(1).put(0).flip();
                this.context = ALC10.alcCreateContext(this.currentDevice, intbuffer);
            }

            if (OpenAlUtil.checkALCError(this.currentDevice, "Create context")) {
                throw new IllegalStateException("Unable to create OpenAL context");
            } else {
                ALC10.alcMakeContextCurrent(this.context);
                int j = this.getChannelCount();
                int k = Mth.clamp((int)Mth.sqrt((float)j), 2, 8);
                int i = Mth.clamp(j - k, 8, 255);
                this.staticChannels = new Library.CountingChannelPool(i);
                this.streamingChannels = new Library.CountingChannelPool(k);
                ALCapabilities alcapabilities = AL.createCapabilities(alccapabilities);
                OpenAlUtil.checkALError("Initialization");
                if (!alcapabilities.AL_EXT_source_distance_model) {
                    throw new IllegalStateException("AL_EXT_source_distance_model is not supported");
                } else {
                    AL10.alEnable(512);
                    if (!alcapabilities.AL_EXT_LINEAR_DISTANCE) {
                        throw new IllegalStateException("AL_EXT_LINEAR_DISTANCE is not supported");
                    } else {
                        OpenAlUtil.checkALError("Enable per-source distance models");
                        LOGGER.info("OpenAL initialized on device {}", this.getCurrentDeviceName());
                        this.supportsDisconnections = ALC10.alcIsExtensionPresent(this.currentDevice, "ALC_EXT_disconnect");
                    }
                }
            }
        }
    }

    /**
     * Sets the HRTF (head-related transfer function) for spatial audio, if it is supported by the current device.
     *
     * @param enableHrtf Whether to enable HRTF.
     */
    private void setHrtf(boolean enableHrtf) {
        int i = ALC10.alcGetInteger(this.currentDevice, 6548);
        if (i > 0) {
            try (MemoryStack memorystack = MemoryStack.stackPush()) {
                IntBuffer intbuffer = memorystack.callocInt(10).put(6546).put(enableHrtf ? 1 : 0).put(6550).put(0).put(0).flip();
                if (!SOFTHRTF.alcResetDeviceSOFT(this.currentDevice, intbuffer)) {
                    LOGGER.warn("Failed to reset device: {}", ALC10.alcGetString(this.currentDevice, ALC10.alcGetError(this.currentDevice)));
                }
            }
        }
    }

    private int getChannelCount() {
        try (MemoryStack memorystack = MemoryStack.stackPush()) {
            int i = ALC10.alcGetInteger(this.currentDevice, 4098);
            if (OpenAlUtil.checkALCError(this.currentDevice, "Get attributes size")) {
                throw new IllegalStateException("Failed to get OpenAL attributes");
            }

            IntBuffer intbuffer = memorystack.mallocInt(i);
            ALC10.alcGetIntegerv(this.currentDevice, 4099, intbuffer);
            if (OpenAlUtil.checkALCError(this.currentDevice, "Get attributes")) {
                throw new IllegalStateException("Failed to get OpenAL attributes");
            }

            int j = 0;

            while (j < i) {
                int k = intbuffer.get(j++);
                if (k == 0) {
                    break;
                }

                int l = intbuffer.get(j++);
                if (k == 4112) {
                    return l;
                }
            }
        }

        return 30;
    }

    @Nullable
    public static String getDefaultDeviceName() {
        if (!ALC10.alcIsExtensionPresent(0L, "ALC_ENUMERATE_ALL_EXT")) {
            return null;
        } else {
            ALUtil.getStringList(0L, 4115);
            return ALC10.alcGetString(0L, 4114);
        }
    }

    public String getCurrentDeviceName() {
        String s = ALC10.alcGetString(this.currentDevice, 4115);
        if (s == null) {
            s = ALC10.alcGetString(this.currentDevice, 4101);
        }

        if (s == null) {
            s = "Unknown";
        }

        return s;
    }

    public synchronized boolean hasDefaultDeviceChanged() {
        String s = getDefaultDeviceName();
        if (Objects.equals(this.defaultDeviceName, s)) {
            return false;
        } else {
            this.defaultDeviceName = s;
            return true;
        }
    }

    /**
     * Opens the specified audio device, or the default device if the specifier is null.
     * @return The handle of the opened device.
     * @throws IllegalStateException if the device cannot be opened.
     *
     * @param deviceSpecifier The name of the audio device to open, or null to open
     *                        the default device.
     */
    private static long openDeviceOrFallback(@Nullable String deviceSpecifier) {
        OptionalLong optionallong = OptionalLong.empty();
        if (deviceSpecifier != null) {
            optionallong = tryOpenDevice(deviceSpecifier);
        }

        if (optionallong.isEmpty()) {
            optionallong = tryOpenDevice(getDefaultDeviceName());
        }

        if (optionallong.isEmpty()) {
            optionallong = tryOpenDevice(null);
        }

        if (optionallong.isEmpty()) {
            throw new IllegalStateException("Failed to open OpenAL device");
        } else {
            return optionallong.getAsLong();
        }
    }

    /**
     * Attempts to open the specified audio device.
     * @return an {@linkplain OptionalLong} containing the handle of the opened device if successful, or empty if the device could not be opened
     *
     * @param deviceSpecifier A string specifying the name of the audio device to open
     *                        , or null to use the default device.
     */
    private static OptionalLong tryOpenDevice(@Nullable String deviceSpecifier) {
        long i = ALC10.alcOpenDevice(deviceSpecifier);
        return i != 0L && !OpenAlUtil.checkALCError(i, "Open device") ? OptionalLong.of(i) : OptionalLong.empty();
    }

    public void cleanup() {
        this.staticChannels.cleanup();
        this.streamingChannels.cleanup();
        ALC10.alcDestroyContext(this.context);
        if (this.currentDevice != 0L) {
            ALC10.alcCloseDevice(this.currentDevice);
        }
    }

    public Listener getListener() {
        return this.listener;
    }

    /**
     * Acquires a sound channel based on the given mode.
     */
    @Nullable
    public Channel acquireChannel(Library.Pool pool) {
        return (pool == Library.Pool.STREAMING ? this.streamingChannels : this.staticChannels).acquire();
    }

    /**
     * Releases a channel.
     * @return whether the channel was successfully released
     *
     * @param channel The channel to release.
     */
    public void releaseChannel(Channel channel) {
        if (!this.staticChannels.release(channel) && !this.streamingChannels.release(channel)) {
            throw new IllegalStateException("Tried to release unknown channel");
        }
    }

    public String getDebugString() {
        return String.format(
            Locale.ROOT,
            "Sounds: %d/%d + %d/%d",
            this.staticChannels.getUsedCount(),
            this.staticChannels.getMaxCount(),
            this.streamingChannels.getUsedCount(),
            this.streamingChannels.getMaxCount()
        );
    }

    public List<String> getAvailableSoundDevices() {
        List<String> list = ALUtil.getStringList(0L, 4115);
        return list == null ? Collections.emptyList() : list;
    }

    public boolean isCurrentDeviceDisconnected() {
        return this.supportsDisconnections && ALC11.alcGetInteger(this.currentDevice, 787) == 0;
    }

    @OnlyIn(Dist.CLIENT)
    interface ChannelPool {
        @Nullable
        Channel acquire();

        boolean release(Channel channel);

        void cleanup();

        int getMaxCount();

        int getUsedCount();
    }

    @OnlyIn(Dist.CLIENT)
    static class CountingChannelPool implements Library.ChannelPool {
        private final int limit;
        private final Set<Channel> activeChannels = Sets.newIdentityHashSet();

        public CountingChannelPool(int limit) {
            this.limit = limit;
        }

        @Nullable
        @Override
        public Channel acquire() {
            if (this.activeChannels.size() >= this.limit) {
                if (SharedConstants.IS_RUNNING_IN_IDE) {
                    Library.LOGGER.warn("Maximum sound pool size {} reached", this.limit);
                }

                return null;
            } else {
                Channel channel = Channel.create();
                if (channel != null) {
                    this.activeChannels.add(channel);
                }

                return channel;
            }
        }

        @Override
        public boolean release(Channel channel) {
            if (!this.activeChannels.remove(channel)) {
                return false;
            } else {
                channel.destroy();
                return true;
            }
        }

        @Override
        public void cleanup() {
            this.activeChannels.forEach(Channel::destroy);
            this.activeChannels.clear();
        }

        @Override
        public int getMaxCount() {
            return this.limit;
        }

        @Override
        public int getUsedCount() {
            return this.activeChannels.size();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Pool {
        STATIC,
        STREAMING;
    }
}
