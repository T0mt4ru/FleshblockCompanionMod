package com.mojang.realmsclient.dto;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.util.JsonUtils;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsText {
    private static final String TRANSLATION_KEY = "translationKey";
    private static final String ARGS = "args";
    private final String translationKey;
    @Nullable
    private final String[] args;

    private RealmsText(String translationKey, @Nullable String[] args) {
        this.translationKey = translationKey;
        this.args = args;
    }

    public Component createComponent(Component defaultValue) {
        return Objects.requireNonNullElse(this.createComponent(), defaultValue);
    }

    @Nullable
    public Component createComponent() {
        if (!I18n.exists(this.translationKey)) {
            return null;
        } else {
            return this.args == null ? Component.translatable(this.translationKey) : Component.translatable(this.translationKey, this.args);
        }
    }

    public static RealmsText parse(JsonObject json) {
        String s = JsonUtils.getRequiredString("translationKey", json);
        JsonElement jsonelement = json.get("args");
        String[] astring;
        if (jsonelement != null && !jsonelement.isJsonNull()) {
            JsonArray jsonarray = jsonelement.getAsJsonArray();
            astring = new String[jsonarray.size()];

            for (int i = 0; i < jsonarray.size(); i++) {
                astring[i] = jsonarray.get(i).getAsString();
            }
        } else {
            astring = null;
        }

        return new RealmsText(s, astring);
    }

    @Override
    public String toString() {
        return this.translationKey;
    }
}
