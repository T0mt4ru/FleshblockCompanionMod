/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.geometry;

import com.mojang.math.Transformation;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.Util;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BuiltInModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.model.ElementsModel;
import net.neoforged.neoforge.client.model.ExtraFaceData;
import net.neoforged.neoforge.client.model.IModelBuilder;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import net.neoforged.neoforge.client.model.QuadTransformers;
import net.neoforged.neoforge.client.model.SimpleModelState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Helper for dealing with unbaked models and geometries.
 */
public class UnbakedGeometryHelper {
    private static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();
    private static final FaceBakery FACE_BAKERY = new FaceBakery();

    /**
     * Explanation:
     * This takes anything that looks like a valid resourcepack texture location, and tries to extract a resourcelocation out of it.
     * 1. it will ignore anything up to and including an /assets/ folder,
     * 2. it will take the next path component as a namespace,
     * 3. it will match but skip the /textures/ part of the path,
     * 4. it will take the rest of the path up to but excluding the .png extension as the resource path
     * It's a best-effort situation, to allow model files exported by modelling software to be used without post-processing.
     * Example:
     * C:\Something\Or Other\src\main\resources\assets\mymodid\textures\item\my_thing.png
     * ........................................--------_______----------_____________----
     * <namespace> <path>
     * Result after replacing '\' to '/': mymodid:item/my_thing
     */
    private static final Pattern FILESYSTEM_PATH_TO_RESLOC = Pattern.compile("(?:.*[\\\\/]assets[\\\\/](?<namespace>[a-z_-]+)[\\\\/]textures[\\\\/])?(?<path>[a-z_\\\\/-]+)\\.png");

    /**
     * Resolves a material that may have been defined with a filesystem path instead of a proper {@link ResourceLocation}.
     * <p>
     * The target atlas will always be {@link TextureAtlas#LOCATION_BLOCKS}.
     */
    public static Material resolveDirtyMaterial(@Nullable String tex, IGeometryBakingContext owner) {
        if (tex == null)
            return new Material(TextureAtlas.LOCATION_BLOCKS, MissingTextureAtlasSprite.getLocation());
        if (tex.startsWith("#"))
            return owner.getMaterial(tex);

        // Attempt to convert a common (windows/linux/mac) filesystem path to a ResourceLocation.
        // This makes no promises, if it doesn't work, too bad, fix your mtl file.
        Matcher match = FILESYSTEM_PATH_TO_RESLOC.matcher(tex);
        if (match.matches()) {
            String namespace = match.group("namespace");
            String path = match.group("path").replace("\\", "/");
            tex = namespace != null ? namespace + ":" + path : path;
        }

        return new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.parse(tex));
    }

    /**
     * Helper for baking {@link BlockModel} instances. Handles baking custom geometries and deferring item model baking.
     */
    @ApiStatus.Internal
    public static BakedModel bake(BlockModel blockModel, ModelBaker modelBaker, BlockModel owner, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, boolean guiLight3d) {
        IUnbakedGeometry<?> customModel = blockModel.customData.getCustomGeometry();
        if (customModel != null)
            return customModel.bake(blockModel.customData, modelBaker, spriteGetter, modelState, blockModel.getOverrides(modelBaker, owner, spriteGetter));

        // Handle vanilla item models here, since vanilla has a shortcut for them
        if (blockModel.getRootModel() == ModelBakery.GENERATION_MARKER)
            return ITEM_MODEL_GENERATOR.generateBlockModel(spriteGetter, blockModel).bakeVanilla(modelBaker, blockModel, spriteGetter, modelState, guiLight3d);

        if (blockModel.getRootModel() == ModelBakery.BLOCK_ENTITY_MARKER) {
            var particleSprite = spriteGetter.apply(blockModel.getMaterial("particle"));
            return new BuiltInModel(blockModel.getTransforms(), blockModel.getOverrides(modelBaker, owner, spriteGetter), particleSprite, blockModel.getGuiLight().lightLikeBlock());
        }

        var elementsModel = new ElementsModel(blockModel.getElements());
        return elementsModel.bake(blockModel.customData, modelBaker, spriteGetter, modelState, blockModel.getOverrides(modelBaker, owner, spriteGetter));
    }

    /**
     * @see #createUnbakedItemElements(int, TextureAtlasSprite, ExtraFaceData)
     */
    public static List<BlockElement> createUnbakedItemElements(int layerIndex, TextureAtlasSprite sprite) {
        return createUnbakedItemElements(layerIndex, sprite, null);
    }

    /**
     * Creates a list of {@linkplain BlockElement block elements} in the shape of the specified sprite contents.
     * These can later be baked using the same, or another texture.
     * <p>
     * The {@link Direction#NORTH} and {@link Direction#SOUTH} faces take up the whole surface.
     */
    public static List<BlockElement> createUnbakedItemElements(int layerIndex, TextureAtlasSprite sprite, @Nullable ExtraFaceData faceData) {
        var elements = ITEM_MODEL_GENERATOR.processFrames(layerIndex, "layer" + layerIndex, sprite.contents());
        ClientHooks.fixItemModelSeams(elements, sprite);
        if (faceData != null) {
            elements.forEach(element -> element.setFaceData(faceData));
        }
        return elements;
    }

    /**
     * @see #createUnbakedItemMaskElements(int, TextureAtlasSprite, ExtraFaceData)
     */
    public static List<BlockElement> createUnbakedItemMaskElements(int layerIndex, TextureAtlasSprite sprite) {
        return createUnbakedItemMaskElements(layerIndex, sprite, null);
    }

    /**
     * Creates a list of {@linkplain BlockElement block elements} in the shape of the specified sprite contents.
     * These can later be baked using the same, or another texture.
     * <p>
     * The {@link Direction#NORTH} and {@link Direction#SOUTH} faces take up only the pixels the texture uses.
     */
    public static List<BlockElement> createUnbakedItemMaskElements(int layerIndex, TextureAtlasSprite sprite, @Nullable ExtraFaceData faceData) {
        var elements = createUnbakedItemElements(layerIndex, sprite, faceData);
        elements.remove(0); // Remove north and south faces

        SpriteContents spriteContents = sprite.contents();
        int width = spriteContents.width(), height = spriteContents.height();
        var bits = new BitSet(width * height);

        // For every frame in the texture, mark all the opaque pixels (this is what vanilla does too)
        spriteContents.getUniqueFrames().forEach(frame -> {
            for (int x = 0; x < width; x++)
                for (int y = 0; y < height; y++)
                    if (!spriteContents.isTransparent(frame, x, y))
                        bits.set(x + y * width);
        });

        // Scan in search of opaque pixels
        for (int y = 0; y < height; y++) {
            int xStart = -1;
            for (int x = 0; x < width; x++) {
                var opaque = bits.get(x + y * width);
                if (opaque == (xStart == -1)) // (opaque && -1) || (!opaque && !-1)
                {
                    if (xStart == -1) {
                        // We have found the start of a new segment, continue
                        xStart = x;
                        continue;
                    }

                    // The segment is over, expand down as far as possible
                    int yEnd = y + 1;
                    expand:
                    for (; yEnd < height; yEnd++)
                        for (int x2 = xStart; x2 <= x; x2++)
                            if (!bits.get(x2 + yEnd * width))
                                break expand;

                    // Mark all pixels in the area as visited
                    for (int i = xStart; i < x; i++)
                        for (int j = y; j < yEnd; j++)
                            bits.clear(i + j * width);

                    // Create element
                    elements.add(new BlockElement(
                            new Vector3f(16 * xStart / (float) width, 16 - 16 * yEnd / (float) height, 7.5F),
                            new Vector3f(16 * x / (float) width, 16 - 16 * y / (float) height, 8.5F),
                            Util.make(new HashMap<>(), map -> {
                                for (Direction direction : Direction.values())
                                    map.put(direction, new BlockElementFace(null, layerIndex, "layer" + layerIndex, new BlockFaceUV(null, 0)));
                            }),
                            null,
                            true));

                    // Reset xStart
                    xStart = -1;
                }
            }
        }
        return elements;
    }

    /**
     * Bakes a list of {@linkplain BlockElement block elements} and feeds the baked quads to a {@linkplain IModelBuilder model builder}.
     */
    public static void bakeElements(IModelBuilder<?> builder, List<BlockElement> elements, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState) {
        for (BlockElement element : elements) {
            element.faces.forEach((side, face) -> {
                var sprite = spriteGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.parse(face.texture())));
                var quad = bakeElementFace(element, face, sprite, side, modelState);
                if (face.cullForDirection() == null)
                    builder.addUnculledFace(quad);
                else
                    builder.addCulledFace(Direction.rotate(modelState.getRotation().getMatrix(), face.cullForDirection()), quad);
            });
        }
    }

    /**
     * Bakes a list of {@linkplain BlockElement block elements} and returns the list of baked quads.
     */
    public static List<BakedQuad> bakeElements(List<BlockElement> elements, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState) {
        if (elements.isEmpty())
            return List.of();
        var list = new ArrayList<BakedQuad>();
        bakeElements(IModelBuilder.collecting(list), elements, spriteGetter, modelState);
        return list;
    }

    /**
     * Turns a single {@link BlockElementFace} into a {@link BakedQuad}.
     */
    public static BakedQuad bakeElementFace(BlockElement element, BlockElementFace face, TextureAtlasSprite sprite, Direction direction, ModelState state) {
        return FACE_BAKERY.bakeQuad(element.from, element.to, face, sprite, direction, state, element.rotation, element.shade);
    }

    /**
     * Create an {@link IQuadTransformer} to apply a {@link Transformation} that undoes the {@link ModelState}
     * transform (blockstate transform), applies the given root transform and then re-applies the
     * blockstate transform.
     *
     * @return an {@code IQuadTransformer} that applies the root transform to a baked quad that already has the
     *         transformation of the given {@code ModelState} applied to it
     */
    public static IQuadTransformer applyRootTransform(ModelState modelState, Transformation rootTransform) {
        // Move the origin of the ModelState transform and its inverse from the negative corner to the block center
        // to replicate the way the ModelState transform is applied in the FaceBakery by moving the vertices such that
        // the negative corner acts as the block center
        Transformation transform = modelState.getRotation().applyOrigin(new Vector3f(.5F, .5F, .5F));
        return QuadTransformers.applying(transform.compose(rootTransform).compose(transform.inverse()));
    }

    /**
     * {@return a {@link ModelState} that combines the existing model state and the {@linkplain Transformation root transform}}
     */
    public static ModelState composeRootTransformIntoModelState(ModelState modelState, Transformation rootTransform) {
        if (rootTransform.isIdentity()) {
            return modelState;
        }

        // Move the origin of the root transform as if the negative corner were the block center to match the way the
        // ModelState transform is applied in the FaceBakery by moving the vertices to be centered on that corner
        rootTransform = rootTransform.applyOrigin(new Vector3f(-.5F, -.5F, -.5F));
        return new SimpleModelState(modelState.getRotation().compose(rootTransform), modelState.isUvLocked());
    }
}
