package net.minecraft.client.renderer.block.model;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.math.Transformation;
import java.lang.reflect.Type;
import java.util.Objects;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Variant implements ModelState {
    private final ResourceLocation modelLocation;
    private final Transformation rotation;
    private final boolean uvLock;
    private final int weight;

    public Variant(ResourceLocation modelLocation, Transformation rotation, boolean uvLock, int weight) {
        this.modelLocation = modelLocation;
        this.rotation = rotation;
        this.uvLock = uvLock;
        this.weight = weight;
    }

    public ResourceLocation getModelLocation() {
        return this.modelLocation;
    }

    @Override
    public Transformation getRotation() {
        return this.rotation;
    }

    @Override
    public boolean isUvLocked() {
        return this.uvLock;
    }

    public int getWeight() {
        return this.weight;
    }

    @Override
    public String toString() {
        return "Variant{modelLocation=" + this.modelLocation + ", rotation=" + this.rotation + ", uvLock=" + this.uvLock + ", weight=" + this.weight + "}";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else {
            return !(other instanceof Variant variant)
                ? false
                : this.modelLocation.equals(variant.modelLocation)
                    && Objects.equals(this.rotation, variant.rotation)
                    && this.uvLock == variant.uvLock
                    && this.weight == variant.weight;
        }
    }

    @Override
    public int hashCode() {
        int i = this.modelLocation.hashCode();
        i = 31 * i + this.rotation.hashCode();
        i = 31 * i + Boolean.valueOf(this.uvLock).hashCode();
        return 31 * i + this.weight;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Deserializer implements JsonDeserializer<Variant> {
        @VisibleForTesting
        static final boolean DEFAULT_UVLOCK = false;
        @VisibleForTesting
        static final int DEFAULT_WEIGHT = 1;
        @VisibleForTesting
        static final int DEFAULT_X_ROTATION = 0;
        @VisibleForTesting
        static final int DEFAULT_Y_ROTATION = 0;

        public Variant deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonobject = json.getAsJsonObject();
            ResourceLocation resourcelocation = this.getModel(jsonobject);
            BlockModelRotation blockmodelrotation = this.getBlockRotation(jsonobject);
            boolean flag = this.getUvLock(jsonobject);
            int i = this.getWeight(jsonobject);
            return new Variant(resourcelocation, blockmodelrotation.getRotation(), flag, i);
        }

        private boolean getUvLock(JsonObject json) {
            return GsonHelper.getAsBoolean(json, "uvlock", false);
        }

        protected BlockModelRotation getBlockRotation(JsonObject json) {
            int i = GsonHelper.getAsInt(json, "x", 0);
            int j = GsonHelper.getAsInt(json, "y", 0);
            BlockModelRotation blockmodelrotation = BlockModelRotation.by(i, j);
            if (blockmodelrotation == null) {
                throw new JsonParseException("Invalid BlockModelRotation x: " + i + ", y: " + j);
            } else {
                return blockmodelrotation;
            }
        }

        protected ResourceLocation getModel(JsonObject json) {
            return ResourceLocation.parse(GsonHelper.getAsString(json, "model"));
        }

        protected int getWeight(JsonObject json) {
            int i = GsonHelper.getAsInt(json, "weight", 1);
            if (i < 1) {
                throw new JsonParseException("Invalid weight " + i + " found, expected integer >= 1");
            } else {
                return i;
            }
        }
    }
}
