package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.util.JsonUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class Subscription extends ValueObject {
    private static final Logger LOGGER = LogUtils.getLogger();
    public long startDate;
    public int daysLeft;
    public Subscription.SubscriptionType type = Subscription.SubscriptionType.NORMAL;

    public static Subscription parse(String json) {
        Subscription subscription = new Subscription();

        try {
            JsonParser jsonparser = new JsonParser();
            JsonObject jsonobject = jsonparser.parse(json).getAsJsonObject();
            subscription.startDate = JsonUtils.getLongOr("startDate", jsonobject, 0L);
            subscription.daysLeft = JsonUtils.getIntOr("daysLeft", jsonobject, 0);
            subscription.type = typeFrom(JsonUtils.getStringOr("subscriptionType", jsonobject, Subscription.SubscriptionType.NORMAL.name()));
        } catch (Exception exception) {
            LOGGER.error("Could not parse Subscription: {}", exception.getMessage());
        }

        return subscription;
    }

    private static Subscription.SubscriptionType typeFrom(String name) {
        try {
            return Subscription.SubscriptionType.valueOf(name);
        } catch (Exception exception) {
            return Subscription.SubscriptionType.NORMAL;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static enum SubscriptionType {
        NORMAL,
        RECURRING;
    }
}
