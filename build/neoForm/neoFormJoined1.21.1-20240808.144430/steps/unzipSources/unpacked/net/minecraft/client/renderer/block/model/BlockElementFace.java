package net.minecraft.client.renderer.block.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record BlockElementFace(@Nullable Direction cullForDirection, int tintIndex, String texture, BlockFaceUV uv, @Nullable net.neoforged.neoforge.client.model.ExtraFaceData faceData, org.apache.commons.lang3.mutable.MutableObject<BlockElement> parent) {
    public static final int NO_TINT = -1;

    public BlockElementFace(@Nullable Direction p_111359_, int p_111360_, String p_111361_, BlockFaceUV p_111362_) {
        this(p_111359_, p_111360_, p_111361_, p_111362_, null, new org.apache.commons.lang3.mutable.MutableObject<>());
    }

    @Override
    public net.neoforged.neoforge.client.model.ExtraFaceData faceData() {
        if(this.faceData != null) {
            return this.faceData;
        } else if(this.parent.getValue() != null) {
            return this.parent.getValue().getFaceData();
        }
        return net.neoforged.neoforge.client.model.ExtraFaceData.DEFAULT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Deserializer implements JsonDeserializer<BlockElementFace> {
        private static final int DEFAULT_TINT_INDEX = -1;

        public BlockElementFace deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonobject = json.getAsJsonObject();
            Direction direction = this.getCullFacing(jsonobject);
            int i = this.getTintIndex(jsonobject);
            String s = this.getTexture(jsonobject);
            BlockFaceUV blockfaceuv = context.deserialize(jsonobject, BlockFaceUV.class);
            if (jsonobject.has("forge_data")) throw new JsonParseException("forge_data should be replaced by neoforge_data"); // TODO 1.22: Remove
            var faceData = net.neoforged.neoforge.client.model.ExtraFaceData.read(jsonobject.get("neoforge_data"), null);
            return new BlockElementFace(direction, i, s, blockfaceuv, faceData, new org.apache.commons.lang3.mutable.MutableObject<>());
        }

        protected int getTintIndex(JsonObject json) {
            return GsonHelper.getAsInt(json, "tintindex", -1);
        }

        private String getTexture(JsonObject json) {
            return GsonHelper.getAsString(json, "texture");
        }

        @Nullable
        private Direction getCullFacing(JsonObject json) {
            String s = GsonHelper.getAsString(json, "cullface", "");
            return Direction.byName(s);
        }
    }
}
