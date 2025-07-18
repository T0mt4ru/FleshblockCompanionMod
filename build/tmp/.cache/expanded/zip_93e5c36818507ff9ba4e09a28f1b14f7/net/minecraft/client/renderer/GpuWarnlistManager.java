package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.platform.GlUtil;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class GpuWarnlistManager extends SimplePreparableReloadListener<GpuWarnlistManager.Preparations> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation GPU_WARNLIST_LOCATION = ResourceLocation.withDefaultNamespace("gpu_warnlist.json");
    private ImmutableMap<String, String> warnings = ImmutableMap.of();
    private boolean showWarning;
    private boolean warningDismissed;
    private boolean skipFabulous;

    public boolean hasWarnings() {
        return !this.warnings.isEmpty();
    }

    public boolean willShowWarning() {
        return this.hasWarnings() && !this.warningDismissed;
    }

    public void showWarning() {
        this.showWarning = true;
    }

    public void dismissWarning() {
        this.warningDismissed = true;
    }

    public void dismissWarningAndSkipFabulous() {
        this.warningDismissed = true;
        this.skipFabulous = true;
    }

    public boolean isShowingWarning() {
        return this.showWarning && !this.warningDismissed;
    }

    public boolean isSkippingFabulous() {
        return this.skipFabulous;
    }

    public void resetWarnings() {
        this.showWarning = false;
        this.warningDismissed = false;
        this.skipFabulous = false;
    }

    @Nullable
    public String getRendererWarnings() {
        return this.warnings.get("renderer");
    }

    @Nullable
    public String getVersionWarnings() {
        return this.warnings.get("version");
    }

    @Nullable
    public String getVendorWarnings() {
        return this.warnings.get("vendor");
    }

    @Nullable
    public String getAllWarnings() {
        StringBuilder stringbuilder = new StringBuilder();
        this.warnings.forEach((p_109235_, p_109236_) -> stringbuilder.append(p_109235_).append(": ").append(p_109236_));
        return stringbuilder.length() == 0 ? null : stringbuilder.toString();
    }

    /**
     * Performs any reloading that can be done off-thread, such as file IO
     */
    protected GpuWarnlistManager.Preparations prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        List<Pattern> list = Lists.newArrayList();
        List<Pattern> list1 = Lists.newArrayList();
        List<Pattern> list2 = Lists.newArrayList();
        profiler.startTick();
        JsonObject jsonobject = parseJson(resourceManager, profiler);
        if (jsonobject != null) {
            profiler.push("compile_regex");
            compilePatterns(jsonobject.getAsJsonArray("renderer"), list);
            compilePatterns(jsonobject.getAsJsonArray("version"), list1);
            compilePatterns(jsonobject.getAsJsonArray("vendor"), list2);
            profiler.pop();
        }

        profiler.endTick();
        return new GpuWarnlistManager.Preparations(list, list1, list2);
    }

    protected void apply(GpuWarnlistManager.Preparations object, ResourceManager resourceManager, ProfilerFiller profiler) {
        this.warnings = object.apply();
    }

    private static void compilePatterns(JsonArray jsonArray, List<Pattern> patterns) {
        jsonArray.forEach(p_109239_ -> patterns.add(Pattern.compile(p_109239_.getAsString(), 2)));
    }

    @Nullable
    private static JsonObject parseJson(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        profilerFiller.push("parse_json");
        JsonObject jsonobject = null;

        try (Reader reader = resourceManager.openAsReader(GPU_WARNLIST_LOCATION)) {
            jsonobject = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (JsonSyntaxException | IOException ioexception) {
            LOGGER.warn("Failed to load GPU warnlist");
        }

        profilerFiller.pop();
        return jsonobject;
    }

    @OnlyIn(Dist.CLIENT)
    protected static final class Preparations {
        private final List<Pattern> rendererPatterns;
        private final List<Pattern> versionPatterns;
        private final List<Pattern> vendorPatterns;

        Preparations(List<Pattern> rendererPatterns, List<Pattern> versionPatterns, List<Pattern> vendorPatterns) {
            this.rendererPatterns = rendererPatterns;
            this.versionPatterns = versionPatterns;
            this.vendorPatterns = vendorPatterns;
        }

        private static String matchAny(List<Pattern> patterns, String string) {
            List<String> list = Lists.newArrayList();

            for (Pattern pattern : patterns) {
                Matcher matcher = pattern.matcher(string);

                while (matcher.find()) {
                    list.add(matcher.group());
                }
            }

            return String.join(", ", list);
        }

        ImmutableMap<String, String> apply() {
            Builder<String, String> builder = new Builder<>();
            String s = matchAny(this.rendererPatterns, GlUtil.getRenderer());
            if (!s.isEmpty()) {
                builder.put("renderer", s);
            }

            String s1 = matchAny(this.versionPatterns, GlUtil.getOpenGLVersion());
            if (!s1.isEmpty()) {
                builder.put("version", s1);
            }

            String s2 = matchAny(this.vendorPatterns, GlUtil.getVendor());
            if (!s2.isEmpty()) {
                builder.put("vendor", s2);
            }

            return builder.build();
        }
    }
}
