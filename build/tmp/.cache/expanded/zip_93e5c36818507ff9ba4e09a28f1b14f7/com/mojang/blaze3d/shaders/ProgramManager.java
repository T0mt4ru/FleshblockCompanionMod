package com.mojang.blaze3d.shaders;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ProgramManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void glUseProgram(int program) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._glUseProgram(program);
    }

    public static void releaseProgram(Shader shader) {
        RenderSystem.assertOnRenderThread();
        shader.getFragmentProgram().close();
        shader.getVertexProgram().close();
        GlStateManager.glDeleteProgram(shader.getId());
    }

    public static int createProgram() throws IOException {
        RenderSystem.assertOnRenderThread();
        int i = GlStateManager.glCreateProgram();
        if (i <= 0) {
            throw new IOException("Could not create shader program (returned program ID " + i + ")");
        } else {
            return i;
        }
    }

    public static void linkShader(Shader shader) {
        RenderSystem.assertOnRenderThread();
        shader.attachToProgram();
        GlStateManager.glLinkProgram(shader.getId());
        int i = GlStateManager.glGetProgrami(shader.getId(), 35714);
        if (i == 0) {
            LOGGER.warn(
                "Error encountered when linking program containing VS {} and FS {}. Log output:",
                shader.getVertexProgram().getName(),
                shader.getFragmentProgram().getName()
            );
            LOGGER.warn(GlStateManager.glGetProgramInfoLog(shader.getId(), 32768));
        }
    }
}
