package net.minecraft.data.info;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

public class RegistryDumpReport implements DataProvider {
    private final PackOutput output;

    public RegistryDumpReport(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        JsonObject jsonobject = new JsonObject();
        BuiltInRegistries.REGISTRY
            .holders()
            .forEach(p_211088_ -> jsonobject.add(p_211088_.key().location().toString(), dumpRegistry((Registry<?>)p_211088_.value())));
        Path path = this.output.getOutputFolder(PackOutput.Target.REPORTS).resolve("registries.json");
        return DataProvider.saveStable(output, jsonobject, path);
    }

    private static <T> JsonElement dumpRegistry(Registry<T> registry) {
        JsonObject jsonobject = new JsonObject();
        if (registry instanceof DefaultedRegistry) {
            ResourceLocation resourcelocation = ((DefaultedRegistry)registry).getDefaultKey();
            jsonobject.addProperty("default", resourcelocation.toString());
        }

        int i = ((Registry<Registry<T>>)BuiltInRegistries.REGISTRY).getId(registry);
        jsonobject.addProperty("protocol_id", i);
        JsonObject jsonobject1 = new JsonObject();
        registry.holders().forEach(p_211092_ -> {
            T t = p_211092_.value();
            int j = registry.getId(t);
            JsonObject jsonobject2 = new JsonObject();
            jsonobject2.addProperty("protocol_id", j);
            jsonobject1.add(p_211092_.key().location().toString(), jsonobject2);
        });
        jsonobject.add("entries", jsonobject1);
        return jsonobject;
    }

    @Override
    public final String getName() {
        return "Registry Dump";
    }
}
