package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsNews extends ValueObject {
    private static final Logger LOGGER = LogUtils.getLogger();
    public String newsLink;

    public static RealmsNews parse(String json) {
        RealmsNews realmsnews = new RealmsNews();

        try {
            JsonParser jsonparser = new JsonParser();
            JsonObject jsonobject = jsonparser.parse(json).getAsJsonObject();
            realmsnews.newsLink = JsonUtils.getStringOr("newsLink", jsonobject, null);
        } catch (Exception exception) {
            LOGGER.error("Could not parse RealmsNews: {}", exception.getMessage());
        }

        return realmsnews;
    }
}
