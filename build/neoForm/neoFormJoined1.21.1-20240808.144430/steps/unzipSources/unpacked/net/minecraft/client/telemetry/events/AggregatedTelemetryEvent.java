package net.minecraft.client.telemetry.events;

import java.time.Duration;
import java.time.Instant;
import javax.annotation.Nullable;
import net.minecraft.client.telemetry.TelemetryEventSender;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AggregatedTelemetryEvent {
    private static final int SAMPLE_INTERVAL_MS = 60000;
    private static final int SAMPLES_PER_EVENT = 10;
    private int sampleCount;
    private boolean ticking = false;
    @Nullable
    private Instant lastSampleTime;

    public void start() {
        this.ticking = true;
        this.lastSampleTime = Instant.now();
        this.sampleCount = 0;
    }

    public void tick(TelemetryEventSender sender) {
        if (this.shouldTakeSample()) {
            this.takeSample();
            this.sampleCount++;
            this.lastSampleTime = Instant.now();
        }

        if (this.shouldSentEvent()) {
            this.sendEvent(sender);
            this.sampleCount = 0;
        }
    }

    public boolean shouldTakeSample() {
        return this.ticking && this.lastSampleTime != null && Duration.between(this.lastSampleTime, Instant.now()).toMillis() > 60000L;
    }

    public boolean shouldSentEvent() {
        return this.sampleCount >= 10;
    }

    public void stop() {
        this.ticking = false;
    }

    protected int getSampleCount() {
        return this.sampleCount;
    }

    public abstract void takeSample();

    public abstract void sendEvent(TelemetryEventSender sender);
}
