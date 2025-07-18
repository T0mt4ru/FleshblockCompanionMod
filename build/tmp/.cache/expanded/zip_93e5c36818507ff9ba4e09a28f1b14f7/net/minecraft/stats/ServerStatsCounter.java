package net.minecraft.stats;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundAwardStatsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

/**
 * Server-side implementation of {@link net.minecraft.stats.StatsCounter}; handles counting, serialising, and de-serialising statistics, as well as sending them to connected clients via the {@linkplain net.minecraft.network.protocol.game.ClientboundAwardStatsPacket award stats packet}.
 */
public class ServerStatsCounter extends StatsCounter {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final MinecraftServer server;
    private final File file;
    private final Set<Stat<?>> dirty = Sets.newHashSet();

    public ServerStatsCounter(MinecraftServer server, File file) {
        this.server = server;
        this.file = file;
        if (file.isFile()) {
            try {
                this.parseLocal(server.getFixerUpper(), FileUtils.readFileToString(file));
            } catch (IOException ioexception) {
                LOGGER.error("Couldn't read statistics file {}", file, ioexception);
            } catch (JsonParseException jsonparseexception) {
                LOGGER.error("Couldn't parse statistics file {}", file, jsonparseexception);
            }
        }
    }

    public void save() {
        try {
            FileUtils.writeStringToFile(this.file, this.toJson());
        } catch (IOException ioexception) {
            LOGGER.error("Couldn't save stats", (Throwable)ioexception);
        }
    }

    @Override
    public void setValue(Player player, Stat<?> stat, int value) {
        super.setValue(player, stat, value);
        this.dirty.add(stat);
    }

    private Set<Stat<?>> getDirty() {
        Set<Stat<?>> set = Sets.newHashSet(this.dirty);
        this.dirty.clear();
        return set;
    }

    public void parseLocal(DataFixer fixerUpper, String json) {
        try {
            try (JsonReader jsonreader = new JsonReader(new StringReader(json))) {
                jsonreader.setLenient(false);
                JsonElement jsonelement = Streams.parse(jsonreader);
                if (!jsonelement.isJsonNull()) {
                    CompoundTag compoundtag = fromJson(jsonelement.getAsJsonObject());
                    compoundtag = DataFixTypes.STATS.updateToCurrentVersion(fixerUpper, compoundtag, NbtUtils.getDataVersion(compoundtag, 1343));
                    if (!compoundtag.contains("stats", 10)) {
                        return;
                    }

                    CompoundTag compoundtag1 = compoundtag.getCompound("stats");

                    for (String s : compoundtag1.getAllKeys()) {
                        if (compoundtag1.contains(s, 10)) {
                            Util.ifElse(
                                BuiltInRegistries.STAT_TYPE.getOptional(ResourceLocation.parse(s)),
                                p_12844_ -> {
                                    CompoundTag compoundtag2 = compoundtag1.getCompound(s);

                                    for (String s1 : compoundtag2.getAllKeys()) {
                                        if (compoundtag2.contains(s1, 99)) {
                                            Util.ifElse(
                                                this.getStat(p_12844_, s1),
                                                p_144252_ -> this.stats.put(p_144252_, compoundtag2.getInt(s1)),
                                                () -> LOGGER.warn("Invalid statistic in {}: Don't know what {} is", this.file, s1)
                                            );
                                        } else {
                                            LOGGER.warn("Invalid statistic value in {}: Don't know what {} is for key {}", this.file, compoundtag2.get(s1), s1);
                                        }
                                    }
                                },
                                () -> LOGGER.warn("Invalid statistic type in {}: Don't know what {} is", this.file, s)
                            );
                        }
                    }

                    return;
                }

                LOGGER.error("Unable to parse Stat data from {}", this.file);
            }
        } catch (IOException | JsonParseException jsonparseexception) {
            LOGGER.error("Unable to parse Stat data from {}", this.file, jsonparseexception);
        }
    }

    private <T> Optional<Stat<T>> getStat(StatType<T> type, String location) {
        return Optional.ofNullable(ResourceLocation.tryParse(location)).flatMap(type.getRegistry()::getOptional).map(type::get);
    }

    private static CompoundTag fromJson(JsonObject json) {
        CompoundTag compoundtag = new CompoundTag();

        for (Entry<String, JsonElement> entry : json.entrySet()) {
            JsonElement jsonelement = entry.getValue();
            if (jsonelement.isJsonObject()) {
                compoundtag.put(entry.getKey(), fromJson(jsonelement.getAsJsonObject()));
            } else if (jsonelement.isJsonPrimitive()) {
                JsonPrimitive jsonprimitive = jsonelement.getAsJsonPrimitive();
                if (jsonprimitive.isNumber()) {
                    compoundtag.putInt(entry.getKey(), jsonprimitive.getAsInt());
                }
            }
        }

        return compoundtag;
    }

    protected String toJson() {
        Map<StatType<?>, JsonObject> map = Maps.newHashMap();

        for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Stat<?>> entry : this.stats.object2IntEntrySet()) {
            Stat<?> stat = entry.getKey();
            map.computeIfAbsent(stat.getType(), p_12822_ -> new JsonObject()).addProperty(getKey(stat).toString(), entry.getIntValue());
        }

        JsonObject jsonobject = new JsonObject();

        for (Entry<StatType<?>, JsonObject> entry1 : map.entrySet()) {
            jsonobject.add(BuiltInRegistries.STAT_TYPE.getKey(entry1.getKey()).toString(), entry1.getValue());
        }

        JsonObject jsonobject1 = new JsonObject();
        jsonobject1.add("stats", jsonobject);
        jsonobject1.addProperty("DataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());
        return jsonobject1.toString();
    }

    private static <T> ResourceLocation getKey(Stat<T> stat) {
        return stat.getType().getRegistry().getKey(stat.getValue());
    }

    public void markAllDirty() {
        this.dirty.addAll(this.stats.keySet());
    }

    public void sendStats(ServerPlayer player) {
        Object2IntMap<Stat<?>> object2intmap = new Object2IntOpenHashMap<>();

        for (Stat<?> stat : this.getDirty()) {
            object2intmap.put(stat, this.getValue(stat));
        }

        player.connection.send(new ClientboundAwardStatsPacket(object2intmap));
    }
}
