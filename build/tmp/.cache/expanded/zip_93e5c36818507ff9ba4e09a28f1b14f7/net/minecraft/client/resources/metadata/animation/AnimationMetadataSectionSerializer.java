package net.minecraft.client.resources.metadata.animation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import javax.annotation.Nullable;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.util.GsonHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.commons.lang3.Validate;

@OnlyIn(Dist.CLIENT)
public class AnimationMetadataSectionSerializer implements MetadataSectionSerializer<AnimationMetadataSection> {
    public AnimationMetadataSection fromJson(JsonObject json) {
        Builder<AnimationFrame> builder = ImmutableList.builder();
        int i = GsonHelper.getAsInt(json, "frametime", 1);
        if (i != 1) {
            Validate.inclusiveBetween(1L, 2147483647L, (long)i, "Invalid default frame time");
        }

        if (json.has("frames")) {
            try {
                JsonArray jsonarray = GsonHelper.getAsJsonArray(json, "frames");

                for (int j = 0; j < jsonarray.size(); j++) {
                    JsonElement jsonelement = jsonarray.get(j);
                    AnimationFrame animationframe = this.getFrame(j, jsonelement);
                    if (animationframe != null) {
                        builder.add(animationframe);
                    }
                }
            } catch (ClassCastException classcastexception) {
                throw new JsonParseException("Invalid animation->frames: expected array, was " + json.get("frames"), classcastexception);
            }
        }

        int k = GsonHelper.getAsInt(json, "width", -1);
        int l = GsonHelper.getAsInt(json, "height", -1);
        if (k != -1) {
            Validate.inclusiveBetween(1L, 2147483647L, (long)k, "Invalid width");
        }

        if (l != -1) {
            Validate.inclusiveBetween(1L, 2147483647L, (long)l, "Invalid height");
        }

        boolean flag = GsonHelper.getAsBoolean(json, "interpolate", false);
        return new AnimationMetadataSection(builder.build(), k, l, i, flag);
    }

    @Nullable
    private AnimationFrame getFrame(int frame, JsonElement element) {
        if (element.isJsonPrimitive()) {
            return new AnimationFrame(GsonHelper.convertToInt(element, "frames[" + frame + "]"));
        } else if (element.isJsonObject()) {
            JsonObject jsonobject = GsonHelper.convertToJsonObject(element, "frames[" + frame + "]");
            int i = GsonHelper.getAsInt(jsonobject, "time", -1);
            if (jsonobject.has("time")) {
                Validate.inclusiveBetween(1L, 2147483647L, (long)i, "Invalid frame time");
            }

            int j = GsonHelper.getAsInt(jsonobject, "index");
            Validate.inclusiveBetween(0L, 2147483647L, (long)j, "Invalid frame index");
            return new AnimationFrame(j, i);
        } else {
            return null;
        }
    }

    @Override
    public String getMetadataSectionName() {
        return "animation";
    }
}
