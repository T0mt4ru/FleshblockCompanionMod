package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.shaders.Program;
import com.mojang.blaze3d.shaders.ProgramManager;
import com.mojang.blaze3d.shaders.Shader;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ChainedJsonException;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.GsonHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.commons.io.IOUtils;
import org.joml.Matrix4f;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ShaderInstance implements Shader, AutoCloseable {
    public static final String SHADER_PATH = "shaders";
    private static final String SHADER_CORE_PATH = "shaders/core/";
    private static final String SHADER_INCLUDE_PATH = "shaders/include/";
    static final Logger LOGGER = LogUtils.getLogger();
    private static final AbstractUniform DUMMY_UNIFORM = new AbstractUniform();
    private static final boolean ALWAYS_REAPPLY = true;
    private static ShaderInstance lastAppliedShader;
    private static int lastProgramId = -1;
    private final Map<String, Object> samplerMap = Maps.newHashMap();
    private final List<String> samplerNames = Lists.newArrayList();
    private final List<Integer> samplerLocations = Lists.newArrayList();
    private final List<Uniform> uniforms = Lists.newArrayList();
    private final List<Integer> uniformLocations = Lists.newArrayList();
    private final Map<String, Uniform> uniformMap = Maps.newHashMap();
    private final int programId;
    private final String name;
    private boolean dirty;
    private final Program vertexProgram;
    private final Program fragmentProgram;
    private final VertexFormat vertexFormat;
    @Nullable
    public final Uniform MODEL_VIEW_MATRIX;
    @Nullable
    public final Uniform PROJECTION_MATRIX;
    @Nullable
    public final Uniform TEXTURE_MATRIX;
    @Nullable
    public final Uniform SCREEN_SIZE;
    @Nullable
    public final Uniform COLOR_MODULATOR;
    @Nullable
    public final Uniform LIGHT0_DIRECTION;
    @Nullable
    public final Uniform LIGHT1_DIRECTION;
    @Nullable
    public final Uniform GLINT_ALPHA;
    @Nullable
    public final Uniform FOG_START;
    @Nullable
    public final Uniform FOG_END;
    @Nullable
    public final Uniform FOG_COLOR;
    @Nullable
    public final Uniform FOG_SHAPE;
    @Nullable
    public final Uniform LINE_WIDTH;
    @Nullable
    public final Uniform GAME_TIME;
    @Nullable
    public final Uniform CHUNK_OFFSET;

    @Deprecated // FORGE: Use the ResourceLocation variant below
    public ShaderInstance(ResourceProvider resourceProvider, String name, VertexFormat vertexFormat) throws IOException {
        this(resourceProvider, ResourceLocation.parse(name), vertexFormat);
    }
    public ShaderInstance(ResourceProvider p_173336_, ResourceLocation shaderLocation, VertexFormat p_173338_) throws IOException {
        this.name = shaderLocation.getNamespace().equals("minecraft") ? shaderLocation.getPath() : shaderLocation.toString();
        this.vertexFormat = p_173338_;
        ResourceLocation resourcelocation = ResourceLocation.fromNamespaceAndPath(shaderLocation.getNamespace(), "shaders/core/" + shaderLocation.getPath() + ".json");

        try (Reader reader = p_173336_.openAsReader(resourcelocation)) {
            JsonObject jsonobject = GsonHelper.parse(reader);
            String s1 = GsonHelper.getAsString(jsonobject, "vertex");
            String s = GsonHelper.getAsString(jsonobject, "fragment");
            JsonArray jsonarray = GsonHelper.getAsJsonArray(jsonobject, "samplers", null);
            if (jsonarray != null) {
                int i = 0;

                for (JsonElement jsonelement : jsonarray) {
                    try {
                        this.parseSamplerNode(jsonelement);
                    } catch (Exception exception1) {
                        ChainedJsonException chainedjsonexception1 = ChainedJsonException.forException(exception1);
                        chainedjsonexception1.prependJsonKey("samplers[" + i + "]");
                        throw chainedjsonexception1;
                    }

                    i++;
                }
            }

            JsonArray jsonarray1 = GsonHelper.getAsJsonArray(jsonobject, "uniforms", null);
            if (jsonarray1 != null) {
                int j = 0;

                for (JsonElement jsonelement1 : jsonarray1) {
                    try {
                        this.parseUniformNode(jsonelement1);
                    } catch (Exception exception) {
                        ChainedJsonException chainedjsonexception2 = ChainedJsonException.forException(exception);
                        chainedjsonexception2.prependJsonKey("uniforms[" + j + "]");
                        throw chainedjsonexception2;
                    }

                    j++;
                }
            }

            this.vertexProgram = getOrCreate(p_173336_, Program.Type.VERTEX, s1);
            this.fragmentProgram = getOrCreate(p_173336_, Program.Type.FRAGMENT, s);
            this.programId = ProgramManager.createProgram();
            int k = 0;

            for (String s2 : p_173338_.getElementAttributeNames()) {
                Uniform.glBindAttribLocation(this.programId, k, s2);
                k++;
            }

            ProgramManager.linkShader(this);
            this.updateLocations();
        } catch (Exception exception2) {
            ChainedJsonException chainedjsonexception = ChainedJsonException.forException(exception2);
            chainedjsonexception.setFilenameAndFlush(resourcelocation.getPath());
            throw chainedjsonexception;
        }

        this.markDirty();
        this.MODEL_VIEW_MATRIX = this.getUniform("ModelViewMat");
        this.PROJECTION_MATRIX = this.getUniform("ProjMat");
        this.TEXTURE_MATRIX = this.getUniform("TextureMat");
        this.SCREEN_SIZE = this.getUniform("ScreenSize");
        this.COLOR_MODULATOR = this.getUniform("ColorModulator");
        this.LIGHT0_DIRECTION = this.getUniform("Light0_Direction");
        this.LIGHT1_DIRECTION = this.getUniform("Light1_Direction");
        this.GLINT_ALPHA = this.getUniform("GlintAlpha");
        this.FOG_START = this.getUniform("FogStart");
        this.FOG_END = this.getUniform("FogEnd");
        this.FOG_COLOR = this.getUniform("FogColor");
        this.FOG_SHAPE = this.getUniform("FogShape");
        this.LINE_WIDTH = this.getUniform("LineWidth");
        this.GAME_TIME = this.getUniform("GameTime");
        this.CHUNK_OFFSET = this.getUniform("ChunkOffset");
    }

    private static Program getOrCreate(final ResourceProvider resourceProvider, Program.Type programType, String name) throws IOException {
        Program program1 = programType.getPrograms().get(name);
        Program program;
        if (program1 == null) {
            ResourceLocation loc = ResourceLocation.parse(name);
            String s = "shaders/core/" + loc.getPath() + programType.getExtension();
            ResourceLocation resourcelocation = ResourceLocation.fromNamespaceAndPath(loc.getNamespace(), s);
            Resource resource = resourceProvider.getResourceOrThrow(resourcelocation);

            try (InputStream inputstream = resource.open()) {
                final String s1 = FileUtil.getFullResourcePath(s);
                program = Program.compileShader(programType, name, inputstream, resource.sourcePackId(), new GlslPreprocessor() {
                    private final Set<String> importedPaths = Sets.newHashSet();

                    @Override
                    public String applyImport(boolean p_173374_, String p_173375_) {
                        // FORGE: use the mod's namespace to look up resources if specified
                        ResourceLocation resourcelocation = net.neoforged.neoforge.client.ClientHooks.getShaderImportLocation(s1, p_173374_, p_173375_);
                        if (!this.importedPaths.add(resourcelocation.toString())) {
                            return null;
                        } else {

                            try {
                                String s2;
                                try (Reader reader = resourceProvider.openAsReader(resourcelocation)) {
                                    s2 = IOUtils.toString(reader);
                                }

                                return s2;
                            } catch (IOException ioexception) {
                                // FORGE: specify the namespace of the failed import in case of duplicates from multiple mods
                                ShaderInstance.LOGGER.error("Could not open GLSL import {}: {}", resourcelocation, ioexception.getMessage());
                                return "#error " + ioexception.getMessage();
                            }
                        }
                    }
                });
            }
        } else {
            program = program1;
        }

        return program;
    }

    @Override
    public void close() {
        for (Uniform uniform : this.uniforms) {
            uniform.close();
        }

        ProgramManager.releaseProgram(this);
    }

    public void clear() {
        RenderSystem.assertOnRenderThread();
        ProgramManager.glUseProgram(0);
        lastProgramId = -1;
        lastAppliedShader = null;
        int i = GlStateManager._getActiveTexture();

        for (int j = 0; j < this.samplerLocations.size(); j++) {
            if (this.samplerMap.get(this.samplerNames.get(j)) != null) {
                GlStateManager._activeTexture(33984 + j);
                GlStateManager._bindTexture(0);
            }
        }

        GlStateManager._activeTexture(i);
    }

    public void apply() {
        RenderSystem.assertOnRenderThread();
        this.dirty = false;
        lastAppliedShader = this;
        if (this.programId != lastProgramId) {
            ProgramManager.glUseProgram(this.programId);
            lastProgramId = this.programId;
        }

        int i = GlStateManager._getActiveTexture();

        for (int j = 0; j < this.samplerLocations.size(); j++) {
            String s = this.samplerNames.get(j);
            if (this.samplerMap.get(s) != null) {
                int k = Uniform.glGetUniformLocation(this.programId, s);
                Uniform.uploadInteger(k, j);
                RenderSystem.activeTexture(33984 + j);
                Object object = this.samplerMap.get(s);
                int l = -1;
                if (object instanceof RenderTarget) {
                    l = ((RenderTarget)object).getColorTextureId();
                } else if (object instanceof AbstractTexture) {
                    l = ((AbstractTexture)object).getId();
                } else if (object instanceof Integer) {
                    l = (Integer)object;
                }

                if (l != -1) {
                    RenderSystem.bindTexture(l);
                }
            }
        }

        GlStateManager._activeTexture(i);

        for (Uniform uniform : this.uniforms) {
            uniform.upload();
        }
    }

    @Override
    public void markDirty() {
        this.dirty = true;
    }

    @Nullable
    public Uniform getUniform(String name) {
        RenderSystem.assertOnRenderThread();
        return this.uniformMap.get(name);
    }

    public AbstractUniform safeGetUniform(String name) {
        Uniform uniform = this.getUniform(name);
        return (AbstractUniform)(uniform == null ? DUMMY_UNIFORM : uniform);
    }

    private void updateLocations() {
        RenderSystem.assertOnRenderThread();
        IntList intlist = new IntArrayList();

        for (int i = 0; i < this.samplerNames.size(); i++) {
            String s = this.samplerNames.get(i);
            int j = Uniform.glGetUniformLocation(this.programId, s);
            if (j == -1) {
                LOGGER.warn("Shader {} could not find sampler named {} in the specified shader program.", this.name, s);
                this.samplerMap.remove(s);
                intlist.add(i);
            } else {
                this.samplerLocations.add(j);
            }
        }

        for (int l = intlist.size() - 1; l >= 0; l--) {
            int i1 = intlist.getInt(l);
            this.samplerNames.remove(i1);
        }

        for (Uniform uniform : this.uniforms) {
            String s1 = uniform.getName();
            int k = Uniform.glGetUniformLocation(this.programId, s1);
            if (k == -1) {
                LOGGER.warn("Shader {} could not find uniform named {} in the specified shader program.", this.name, s1);
            } else {
                this.uniformLocations.add(k);
                uniform.setLocation(k);
                this.uniformMap.put(s1, uniform);
            }
        }
    }

    private void parseSamplerNode(JsonElement json) {
        JsonObject jsonobject = GsonHelper.convertToJsonObject(json, "sampler");
        String s = GsonHelper.getAsString(jsonobject, "name");
        if (!GsonHelper.isStringValue(jsonobject, "file")) {
            this.samplerMap.put(s, null);
            this.samplerNames.add(s);
        } else {
            this.samplerNames.add(s);
        }
    }

    public void setSampler(String name, Object textureId) {
        this.samplerMap.put(name, textureId);
        this.markDirty();
    }

    private void parseUniformNode(JsonElement json) throws ChainedJsonException {
        JsonObject jsonobject = GsonHelper.convertToJsonObject(json, "uniform");
        String s = GsonHelper.getAsString(jsonobject, "name");
        int i = Uniform.getTypeFromString(GsonHelper.getAsString(jsonobject, "type"));
        int j = GsonHelper.getAsInt(jsonobject, "count");
        float[] afloat = new float[Math.max(j, 16)];
        JsonArray jsonarray = GsonHelper.getAsJsonArray(jsonobject, "values");
        if (jsonarray.size() != j && jsonarray.size() > 1) {
            throw new ChainedJsonException("Invalid amount of values specified (expected " + j + ", found " + jsonarray.size() + ")");
        } else {
            int k = 0;

            for (JsonElement jsonelement : jsonarray) {
                try {
                    afloat[k] = GsonHelper.convertToFloat(jsonelement, "value");
                } catch (Exception exception) {
                    ChainedJsonException chainedjsonexception = ChainedJsonException.forException(exception);
                    chainedjsonexception.prependJsonKey("values[" + k + "]");
                    throw chainedjsonexception;
                }

                k++;
            }

            if (j > 1 && jsonarray.size() == 1) {
                while (k < j) {
                    afloat[k] = afloat[0];
                    k++;
                }
            }

            int l = j > 1 && j <= 4 && i < 8 ? j - 1 : 0;
            Uniform uniform = new Uniform(s, i + l, j, this);
            if (i <= 3) {
                uniform.setSafe((int)afloat[0], (int)afloat[1], (int)afloat[2], (int)afloat[3]);
            } else if (i <= 7) {
                uniform.setSafe(afloat[0], afloat[1], afloat[2], afloat[3]);
            } else {
                uniform.set(Arrays.copyOfRange(afloat, 0, j));
            }

            this.uniforms.add(uniform);
        }
    }

    @Override
    public Program getVertexProgram() {
        return this.vertexProgram;
    }

    @Override
    public Program getFragmentProgram() {
        return this.fragmentProgram;
    }

    @Override
    public void attachToProgram() {
        this.fragmentProgram.attachToShader(this);
        this.vertexProgram.attachToShader(this);
    }

    public VertexFormat getVertexFormat() {
        return this.vertexFormat;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public int getId() {
        return this.programId;
    }

    public void setDefaultUniforms(VertexFormat.Mode mode, Matrix4f frustumMatrix, Matrix4f projectionMatrix, Window window) {
        for (int i = 0; i < 12; i++) {
            int j = RenderSystem.getShaderTexture(i);
            this.setSampler("Sampler" + i, j);
        }

        if (this.MODEL_VIEW_MATRIX != null) {
            this.MODEL_VIEW_MATRIX.set(frustumMatrix);
        }

        if (this.PROJECTION_MATRIX != null) {
            this.PROJECTION_MATRIX.set(projectionMatrix);
        }

        if (this.COLOR_MODULATOR != null) {
            this.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
        }

        if (this.GLINT_ALPHA != null) {
            this.GLINT_ALPHA.set(RenderSystem.getShaderGlintAlpha());
        }

        if (this.FOG_START != null) {
            this.FOG_START.set(RenderSystem.getShaderFogStart());
        }

        if (this.FOG_END != null) {
            this.FOG_END.set(RenderSystem.getShaderFogEnd());
        }

        if (this.FOG_COLOR != null) {
            this.FOG_COLOR.set(RenderSystem.getShaderFogColor());
        }

        if (this.FOG_SHAPE != null) {
            this.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
        }

        if (this.TEXTURE_MATRIX != null) {
            this.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
        }

        if (this.GAME_TIME != null) {
            this.GAME_TIME.set(RenderSystem.getShaderGameTime());
        }

        if (this.SCREEN_SIZE != null) {
            this.SCREEN_SIZE.set((float)window.getWidth(), (float)window.getHeight());
        }

        if (this.LINE_WIDTH != null && (mode == VertexFormat.Mode.LINES || mode == VertexFormat.Mode.LINE_STRIP)) {
            this.LINE_WIDTH.set(RenderSystem.getShaderLineWidth());
        }

        RenderSystem.setupShaderLights(this);
    }
}
