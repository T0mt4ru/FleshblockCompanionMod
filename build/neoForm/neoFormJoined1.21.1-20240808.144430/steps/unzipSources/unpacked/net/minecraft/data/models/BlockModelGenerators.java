package net.minecraft.data.models;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.data.BlockFamilies;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.models.blockstates.BlockStateGenerator;
import net.minecraft.data.models.blockstates.Condition;
import net.minecraft.data.models.blockstates.MultiPartGenerator;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.data.models.model.DelegatedModel;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.data.models.model.TexturedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.MangrovePropaguleBlock;
import net.minecraft.world.level.block.PitcherCropBlock;
import net.minecraft.world.level.block.SnifferEggBlock;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.block.entity.vault.VaultState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.level.block.state.properties.SculkSensorPhase;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.block.state.properties.Tilt;
import net.minecraft.world.level.block.state.properties.WallSide;

public class BlockModelGenerators {
    final Consumer<BlockStateGenerator> blockStateOutput;
    final BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput;
    private final Consumer<Item> skippedAutoModelsOutput;
    final List<Block> nonOrientableTrapdoor = ImmutableList.of(Blocks.OAK_TRAPDOOR, Blocks.DARK_OAK_TRAPDOOR, Blocks.IRON_TRAPDOOR);
    final Map<Block, BlockModelGenerators.BlockStateGeneratorSupplier> fullBlockModelCustomGenerators = ImmutableMap.<Block, BlockModelGenerators.BlockStateGeneratorSupplier>builder()
        .put(Blocks.STONE, BlockModelGenerators::createMirroredCubeGenerator)
        .put(Blocks.DEEPSLATE, BlockModelGenerators::createMirroredColumnGenerator)
        .put(Blocks.MUD_BRICKS, BlockModelGenerators::createNorthWestMirroredCubeGenerator)
        .build();
    final Map<Block, TexturedModel> texturedModels = ImmutableMap.<Block, TexturedModel>builder()
        .put(Blocks.SANDSTONE, TexturedModel.TOP_BOTTOM_WITH_WALL.get(Blocks.SANDSTONE))
        .put(Blocks.RED_SANDSTONE, TexturedModel.TOP_BOTTOM_WITH_WALL.get(Blocks.RED_SANDSTONE))
        .put(Blocks.SMOOTH_SANDSTONE, TexturedModel.createAllSame(TextureMapping.getBlockTexture(Blocks.SANDSTONE, "_top")))
        .put(Blocks.SMOOTH_RED_SANDSTONE, TexturedModel.createAllSame(TextureMapping.getBlockTexture(Blocks.RED_SANDSTONE, "_top")))
        .put(
            Blocks.CUT_SANDSTONE,
            TexturedModel.COLUMN
                .get(Blocks.SANDSTONE)
                .updateTextures(p_176223_ -> p_176223_.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.CUT_SANDSTONE)))
        )
        .put(
            Blocks.CUT_RED_SANDSTONE,
            TexturedModel.COLUMN
                .get(Blocks.RED_SANDSTONE)
                .updateTextures(p_176211_ -> p_176211_.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.CUT_RED_SANDSTONE)))
        )
        .put(Blocks.QUARTZ_BLOCK, TexturedModel.COLUMN.get(Blocks.QUARTZ_BLOCK))
        .put(Blocks.SMOOTH_QUARTZ, TexturedModel.createAllSame(TextureMapping.getBlockTexture(Blocks.QUARTZ_BLOCK, "_bottom")))
        .put(Blocks.BLACKSTONE, TexturedModel.COLUMN_WITH_WALL.get(Blocks.BLACKSTONE))
        .put(Blocks.DEEPSLATE, TexturedModel.COLUMN_WITH_WALL.get(Blocks.DEEPSLATE))
        .put(
            Blocks.CHISELED_QUARTZ_BLOCK,
            TexturedModel.COLUMN
                .get(Blocks.CHISELED_QUARTZ_BLOCK)
                .updateTextures(p_176202_ -> p_176202_.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.CHISELED_QUARTZ_BLOCK)))
        )
        .put(Blocks.CHISELED_SANDSTONE, TexturedModel.COLUMN.get(Blocks.CHISELED_SANDSTONE).updateTextures(p_176190_ -> {
            p_176190_.put(TextureSlot.END, TextureMapping.getBlockTexture(Blocks.SANDSTONE, "_top"));
            p_176190_.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.CHISELED_SANDSTONE));
        }))
        .put(Blocks.CHISELED_RED_SANDSTONE, TexturedModel.COLUMN.get(Blocks.CHISELED_RED_SANDSTONE).updateTextures(p_176145_ -> {
            p_176145_.put(TextureSlot.END, TextureMapping.getBlockTexture(Blocks.RED_SANDSTONE, "_top"));
            p_176145_.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.CHISELED_RED_SANDSTONE));
        }))
        .put(Blocks.CHISELED_TUFF_BRICKS, TexturedModel.COLUMN_WITH_WALL.get(Blocks.CHISELED_TUFF_BRICKS))
        .put(Blocks.CHISELED_TUFF, TexturedModel.COLUMN_WITH_WALL.get(Blocks.CHISELED_TUFF))
        .build();
    static final Map<BlockFamily.Variant, BiConsumer<BlockModelGenerators.BlockFamilyProvider, Block>> SHAPE_CONSUMERS = ImmutableMap.<BlockFamily.Variant, BiConsumer<BlockModelGenerators.BlockFamilyProvider, Block>>builder()
        .put(BlockFamily.Variant.BUTTON, BlockModelGenerators.BlockFamilyProvider::button)
        .put(BlockFamily.Variant.DOOR, BlockModelGenerators.BlockFamilyProvider::door)
        .put(BlockFamily.Variant.CHISELED, BlockModelGenerators.BlockFamilyProvider::fullBlockVariant)
        .put(BlockFamily.Variant.CRACKED, BlockModelGenerators.BlockFamilyProvider::fullBlockVariant)
        .put(BlockFamily.Variant.CUSTOM_FENCE, BlockModelGenerators.BlockFamilyProvider::customFence)
        .put(BlockFamily.Variant.FENCE, BlockModelGenerators.BlockFamilyProvider::fence)
        .put(BlockFamily.Variant.CUSTOM_FENCE_GATE, BlockModelGenerators.BlockFamilyProvider::customFenceGate)
        .put(BlockFamily.Variant.FENCE_GATE, BlockModelGenerators.BlockFamilyProvider::fenceGate)
        .put(BlockFamily.Variant.SIGN, BlockModelGenerators.BlockFamilyProvider::sign)
        .put(BlockFamily.Variant.SLAB, BlockModelGenerators.BlockFamilyProvider::slab)
        .put(BlockFamily.Variant.STAIRS, BlockModelGenerators.BlockFamilyProvider::stairs)
        .put(BlockFamily.Variant.PRESSURE_PLATE, BlockModelGenerators.BlockFamilyProvider::pressurePlate)
        .put(BlockFamily.Variant.TRAPDOOR, BlockModelGenerators.BlockFamilyProvider::trapdoor)
        .put(BlockFamily.Variant.WALL, BlockModelGenerators.BlockFamilyProvider::wall)
        .build();
    public static final List<Pair<BooleanProperty, Function<ResourceLocation, Variant>>> MULTIFACE_GENERATOR = List.of(
        Pair.of(BlockStateProperties.NORTH, p_176234_ -> Variant.variant().with(VariantProperties.MODEL, p_176234_)),
        Pair.of(
            BlockStateProperties.EAST,
            p_176229_ -> Variant.variant()
                    .with(VariantProperties.MODEL, p_176229_)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    .with(VariantProperties.UV_LOCK, true)
        ),
        Pair.of(
            BlockStateProperties.SOUTH,
            p_176225_ -> Variant.variant()
                    .with(VariantProperties.MODEL, p_176225_)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    .with(VariantProperties.UV_LOCK, true)
        ),
        Pair.of(
            BlockStateProperties.WEST,
            p_176213_ -> Variant.variant()
                    .with(VariantProperties.MODEL, p_176213_)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    .with(VariantProperties.UV_LOCK, true)
        ),
        Pair.of(
            BlockStateProperties.UP,
            p_176204_ -> Variant.variant()
                    .with(VariantProperties.MODEL, p_176204_)
                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                    .with(VariantProperties.UV_LOCK, true)
        ),
        Pair.of(
            BlockStateProperties.DOWN,
            p_176195_ -> Variant.variant()
                    .with(VariantProperties.MODEL, p_176195_)
                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                    .with(VariantProperties.UV_LOCK, true)
        )
    );
    private static final Map<BlockModelGenerators.BookSlotModelCacheKey, ResourceLocation> CHISELED_BOOKSHELF_SLOT_MODEL_CACHE = new HashMap<>();

    private static BlockStateGenerator createMirroredCubeGenerator(
        Block cubeBlock, ResourceLocation location, TextureMapping textureMapping, BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput
    ) {
        ResourceLocation resourcelocation = ModelTemplates.CUBE_MIRRORED_ALL.create(cubeBlock, textureMapping, modelOutput);
        return createRotatedVariant(cubeBlock, location, resourcelocation);
    }

    private static BlockStateGenerator createNorthWestMirroredCubeGenerator(
        Block cubeBlock, ResourceLocation location, TextureMapping textureMapping, BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput
    ) {
        ResourceLocation resourcelocation = ModelTemplates.CUBE_NORTH_WEST_MIRRORED_ALL.create(cubeBlock, textureMapping, modelOutput);
        return createSimpleBlock(cubeBlock, resourcelocation);
    }

    private static BlockStateGenerator createMirroredColumnGenerator(
        Block columnBlock, ResourceLocation location, TextureMapping textureMapping, BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput
    ) {
        ResourceLocation resourcelocation = ModelTemplates.CUBE_COLUMN_MIRRORED.create(columnBlock, textureMapping, modelOutput);
        return createRotatedVariant(columnBlock, location, resourcelocation).with(createRotatedPillar());
    }

    public BlockModelGenerators(
        Consumer<BlockStateGenerator> blockStateOutput, BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput, Consumer<Item> skippedAutoModelsOutput
    ) {
        this.blockStateOutput = blockStateOutput;
        this.modelOutput = modelOutput;
        this.skippedAutoModelsOutput = skippedAutoModelsOutput;
    }

    void skipAutoItemBlock(Block block) {
        this.skippedAutoModelsOutput.accept(block.asItem());
    }

    void delegateItemModel(Block block, ResourceLocation delegateModelLocation) {
        this.modelOutput.accept(ModelLocationUtils.getModelLocation(block.asItem()), new DelegatedModel(delegateModelLocation));
    }

    private void delegateItemModel(Item item, ResourceLocation delegateModelLocation) {
        this.modelOutput.accept(ModelLocationUtils.getModelLocation(item), new DelegatedModel(delegateModelLocation));
    }

    void createSimpleFlatItemModel(Item flatItem) {
        ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(flatItem), TextureMapping.layer0(flatItem), this.modelOutput);
    }

    private void createSimpleFlatItemModel(Block flatBlock) {
        Item item = flatBlock.asItem();
        if (item != Items.AIR) {
            ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(item), TextureMapping.layer0(flatBlock), this.modelOutput);
        }
    }

    private void createSimpleFlatItemModel(Block flatBlock, String layerZeroTextureSuffix) {
        Item item = flatBlock.asItem();
        ModelTemplates.FLAT_ITEM
            .create(ModelLocationUtils.getModelLocation(item), TextureMapping.layer0(TextureMapping.getBlockTexture(flatBlock, layerZeroTextureSuffix)), this.modelOutput);
    }

    private static PropertyDispatch createHorizontalFacingDispatch() {
        return PropertyDispatch.property(BlockStateProperties.HORIZONTAL_FACING)
            .select(Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
            .select(Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
            .select(Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
            .select(Direction.NORTH, Variant.variant());
    }

    private static PropertyDispatch createHorizontalFacingDispatchAlt() {
        return PropertyDispatch.property(BlockStateProperties.HORIZONTAL_FACING)
            .select(Direction.SOUTH, Variant.variant())
            .select(Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
            .select(Direction.NORTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
            .select(Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270));
    }

    private static PropertyDispatch createTorchHorizontalDispatch() {
        return PropertyDispatch.property(BlockStateProperties.HORIZONTAL_FACING)
            .select(Direction.EAST, Variant.variant())
            .select(Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
            .select(Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
            .select(Direction.NORTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270));
    }

    private static PropertyDispatch createFacingDispatch() {
        return PropertyDispatch.property(BlockStateProperties.FACING)
            .select(Direction.DOWN, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
            .select(Direction.UP, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R270))
            .select(Direction.NORTH, Variant.variant())
            .select(Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
            .select(Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
            .select(Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90));
    }

    private static MultiVariantGenerator createRotatedVariant(Block block, ResourceLocation modelLocation) {
        return MultiVariantGenerator.multiVariant(block, createRotatedVariants(modelLocation));
    }

    private static Variant[] createRotatedVariants(ResourceLocation modelLocation) {
        return new Variant[]{
            Variant.variant().with(VariantProperties.MODEL, modelLocation),
            Variant.variant().with(VariantProperties.MODEL, modelLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90),
            Variant.variant().with(VariantProperties.MODEL, modelLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180),
            Variant.variant().with(VariantProperties.MODEL, modelLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
        };
    }

    private static MultiVariantGenerator createRotatedVariant(Block block, ResourceLocation normalModelLocation, ResourceLocation mirroredModelLocation) {
        return MultiVariantGenerator.multiVariant(
            block,
            Variant.variant().with(VariantProperties.MODEL, normalModelLocation),
            Variant.variant().with(VariantProperties.MODEL, mirroredModelLocation),
            Variant.variant().with(VariantProperties.MODEL, normalModelLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180),
            Variant.variant().with(VariantProperties.MODEL, mirroredModelLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
        );
    }

    private static PropertyDispatch createBooleanModelDispatch(BooleanProperty property, ResourceLocation trueModelLocation, ResourceLocation falseModelLocation) {
        return PropertyDispatch.property(property)
            .select(true, Variant.variant().with(VariantProperties.MODEL, trueModelLocation))
            .select(false, Variant.variant().with(VariantProperties.MODEL, falseModelLocation));
    }

    private void createRotatedMirroredVariantBlock(Block block) {
        ResourceLocation resourcelocation = TexturedModel.CUBE.create(block, this.modelOutput);
        ResourceLocation resourcelocation1 = TexturedModel.CUBE_MIRRORED.create(block, this.modelOutput);
        this.blockStateOutput.accept(createRotatedVariant(block, resourcelocation, resourcelocation1));
    }

    private void createRotatedVariantBlock(Block block) {
        ResourceLocation resourcelocation = TexturedModel.CUBE.create(block, this.modelOutput);
        this.blockStateOutput.accept(createRotatedVariant(block, resourcelocation));
    }

    private void createBrushableBlock(Block block) {
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(block)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.DUSTED)
                            .generate(
                                p_277253_ -> {
                                    String s = "_" + p_277253_;
                                    ResourceLocation resourcelocation = TextureMapping.getBlockTexture(block, s);
                                    return Variant.variant()
                                        .with(
                                            VariantProperties.MODEL,
                                            ModelTemplates.CUBE_ALL
                                                .createWithSuffix(block, s, new TextureMapping().put(TextureSlot.ALL, resourcelocation), this.modelOutput)
                                        );
                                }
                            )
                    )
            );
        this.delegateItemModel(block, TextureMapping.getBlockTexture(block, "_0"));
    }

    static BlockStateGenerator createButton(Block buttonBlock, ResourceLocation unpoweredModelLocation, ResourceLocation poweredModelLocation) {
        return MultiVariantGenerator.multiVariant(buttonBlock)
            .with(
                PropertyDispatch.property(BlockStateProperties.POWERED)
                    .select(false, Variant.variant().with(VariantProperties.MODEL, unpoweredModelLocation))
                    .select(true, Variant.variant().with(VariantProperties.MODEL, poweredModelLocation))
            )
            .with(
                PropertyDispatch.properties(BlockStateProperties.ATTACH_FACE, BlockStateProperties.HORIZONTAL_FACING)
                    .select(AttachFace.FLOOR, Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                    .select(AttachFace.FLOOR, Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                    .select(AttachFace.FLOOR, Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                    .select(AttachFace.FLOOR, Direction.NORTH, Variant.variant())
                    .select(
                        AttachFace.WALL,
                        Direction.EAST,
                        Variant.variant()
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        AttachFace.WALL,
                        Direction.WEST,
                        Variant.variant()
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        AttachFace.WALL,
                        Direction.SOUTH,
                        Variant.variant()
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        AttachFace.WALL,
                        Direction.NORTH,
                        Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        AttachFace.CEILING,
                        Direction.EAST,
                        Variant.variant()
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                    )
                    .select(
                        AttachFace.CEILING,
                        Direction.WEST,
                        Variant.variant()
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                    )
                    .select(AttachFace.CEILING, Direction.SOUTH, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R180))
                    .select(
                        AttachFace.CEILING,
                        Direction.NORTH,
                        Variant.variant()
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                    )
            );
    }

    private static PropertyDispatch.C4<Direction, DoubleBlockHalf, DoorHingeSide, Boolean> configureDoorHalf(
        PropertyDispatch.C4<Direction, DoubleBlockHalf, DoorHingeSide, Boolean> properties,
        DoubleBlockHalf half,
        ResourceLocation leftModelLocation,
        ResourceLocation leftOpenModelLocation,
        ResourceLocation rightModelLocation,
        ResourceLocation rightOpenModelLocation
    ) {
        return properties.select(Direction.EAST, half, DoorHingeSide.LEFT, false, Variant.variant().with(VariantProperties.MODEL, leftModelLocation))
            .select(
                Direction.SOUTH,
                half,
                DoorHingeSide.LEFT,
                false,
                Variant.variant().with(VariantProperties.MODEL, leftModelLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
            )
            .select(
                Direction.WEST,
                half,
                DoorHingeSide.LEFT,
                false,
                Variant.variant().with(VariantProperties.MODEL, leftModelLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
            )
            .select(
                Direction.NORTH,
                half,
                DoorHingeSide.LEFT,
                false,
                Variant.variant().with(VariantProperties.MODEL, leftModelLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
            )
            .select(Direction.EAST, half, DoorHingeSide.RIGHT, false, Variant.variant().with(VariantProperties.MODEL, rightModelLocation))
            .select(
                Direction.SOUTH,
                half,
                DoorHingeSide.RIGHT,
                false,
                Variant.variant().with(VariantProperties.MODEL, rightModelLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
            )
            .select(
                Direction.WEST,
                half,
                DoorHingeSide.RIGHT,
                false,
                Variant.variant().with(VariantProperties.MODEL, rightModelLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
            )
            .select(
                Direction.NORTH,
                half,
                DoorHingeSide.RIGHT,
                false,
                Variant.variant().with(VariantProperties.MODEL, rightModelLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
            )
            .select(
                Direction.EAST,
                half,
                DoorHingeSide.LEFT,
                true,
                Variant.variant().with(VariantProperties.MODEL, leftOpenModelLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
            )
            .select(
                Direction.SOUTH,
                half,
                DoorHingeSide.LEFT,
                true,
                Variant.variant().with(VariantProperties.MODEL, leftOpenModelLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
            )
            .select(
                Direction.WEST,
                half,
                DoorHingeSide.LEFT,
                true,
                Variant.variant().with(VariantProperties.MODEL, leftOpenModelLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
            )
            .select(Direction.NORTH, half, DoorHingeSide.LEFT, true, Variant.variant().with(VariantProperties.MODEL, leftOpenModelLocation))
            .select(
                Direction.EAST,
                half,
                DoorHingeSide.RIGHT,
                true,
                Variant.variant().with(VariantProperties.MODEL, rightOpenModelLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
            )
            .select(Direction.SOUTH, half, DoorHingeSide.RIGHT, true, Variant.variant().with(VariantProperties.MODEL, rightOpenModelLocation))
            .select(
                Direction.WEST,
                half,
                DoorHingeSide.RIGHT,
                true,
                Variant.variant().with(VariantProperties.MODEL, rightOpenModelLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
            )
            .select(
                Direction.NORTH,
                half,
                DoorHingeSide.RIGHT,
                true,
                Variant.variant().with(VariantProperties.MODEL, rightOpenModelLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
            );
    }

    private static BlockStateGenerator createDoor(
        Block doorBlock,
        ResourceLocation topLeftModelLocation,
        ResourceLocation topLeftOpenModelLocation,
        ResourceLocation topRightModelLocation,
        ResourceLocation topRightOpenModelLocation,
        ResourceLocation bottomLeftModelLocation,
        ResourceLocation bottomLeftOpenModelLocation,
        ResourceLocation bottomRightModelLocation,
        ResourceLocation bottomRightOpenModelLocation
    ) {
        return MultiVariantGenerator.multiVariant(doorBlock)
            .with(
                configureDoorHalf(
                    configureDoorHalf(
                        PropertyDispatch.properties(
                            BlockStateProperties.HORIZONTAL_FACING,
                            BlockStateProperties.DOUBLE_BLOCK_HALF,
                            BlockStateProperties.DOOR_HINGE,
                            BlockStateProperties.OPEN
                        ),
                        DoubleBlockHalf.LOWER,
                        topLeftModelLocation,
                        topLeftOpenModelLocation,
                        topRightModelLocation,
                        topRightOpenModelLocation
                    ),
                    DoubleBlockHalf.UPPER,
                    bottomLeftModelLocation,
                    bottomLeftOpenModelLocation,
                    bottomRightModelLocation,
                    bottomRightOpenModelLocation
                )
            );
    }

    static BlockStateGenerator createCustomFence(
        Block customFenceBlock,
        ResourceLocation postModelId,
        ResourceLocation northModelId,
        ResourceLocation eastModelId,
        ResourceLocation southModelId,
        ResourceLocation westModelId
    ) {
        return MultiPartGenerator.multiPart(customFenceBlock)
            .with(Variant.variant().with(VariantProperties.MODEL, postModelId))
            .with(
                Condition.condition().term(BlockStateProperties.NORTH, true),
                Variant.variant().with(VariantProperties.MODEL, northModelId).with(VariantProperties.UV_LOCK, false)
            )
            .with(
                Condition.condition().term(BlockStateProperties.EAST, true),
                Variant.variant().with(VariantProperties.MODEL, eastModelId).with(VariantProperties.UV_LOCK, false)
            )
            .with(
                Condition.condition().term(BlockStateProperties.SOUTH, true),
                Variant.variant().with(VariantProperties.MODEL, southModelId).with(VariantProperties.UV_LOCK, false)
            )
            .with(
                Condition.condition().term(BlockStateProperties.WEST, true),
                Variant.variant().with(VariantProperties.MODEL, westModelId).with(VariantProperties.UV_LOCK, false)
            );
    }

    static BlockStateGenerator createFence(Block fenceBlock, ResourceLocation fencePostModelLocation, ResourceLocation fenceSideModelLocation) {
        return MultiPartGenerator.multiPart(fenceBlock)
            .with(Variant.variant().with(VariantProperties.MODEL, fencePostModelLocation))
            .with(
                Condition.condition().term(BlockStateProperties.NORTH, true),
                Variant.variant().with(VariantProperties.MODEL, fenceSideModelLocation).with(VariantProperties.UV_LOCK, true)
            )
            .with(
                Condition.condition().term(BlockStateProperties.EAST, true),
                Variant.variant()
                    .with(VariantProperties.MODEL, fenceSideModelLocation)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    .with(VariantProperties.UV_LOCK, true)
            )
            .with(
                Condition.condition().term(BlockStateProperties.SOUTH, true),
                Variant.variant()
                    .with(VariantProperties.MODEL, fenceSideModelLocation)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    .with(VariantProperties.UV_LOCK, true)
            )
            .with(
                Condition.condition().term(BlockStateProperties.WEST, true),
                Variant.variant()
                    .with(VariantProperties.MODEL, fenceSideModelLocation)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    .with(VariantProperties.UV_LOCK, true)
            );
    }

    static BlockStateGenerator createWall(Block wallBlock, ResourceLocation postModelLocation, ResourceLocation lowSideModelLocation, ResourceLocation tallSideModelLocation) {
        return MultiPartGenerator.multiPart(wallBlock)
            .with(Condition.condition().term(BlockStateProperties.UP, true), Variant.variant().with(VariantProperties.MODEL, postModelLocation))
            .with(
                Condition.condition().term(BlockStateProperties.NORTH_WALL, WallSide.LOW),
                Variant.variant().with(VariantProperties.MODEL, lowSideModelLocation).with(VariantProperties.UV_LOCK, true)
            )
            .with(
                Condition.condition().term(BlockStateProperties.EAST_WALL, WallSide.LOW),
                Variant.variant()
                    .with(VariantProperties.MODEL, lowSideModelLocation)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    .with(VariantProperties.UV_LOCK, true)
            )
            .with(
                Condition.condition().term(BlockStateProperties.SOUTH_WALL, WallSide.LOW),
                Variant.variant()
                    .with(VariantProperties.MODEL, lowSideModelLocation)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    .with(VariantProperties.UV_LOCK, true)
            )
            .with(
                Condition.condition().term(BlockStateProperties.WEST_WALL, WallSide.LOW),
                Variant.variant()
                    .with(VariantProperties.MODEL, lowSideModelLocation)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    .with(VariantProperties.UV_LOCK, true)
            )
            .with(
                Condition.condition().term(BlockStateProperties.NORTH_WALL, WallSide.TALL),
                Variant.variant().with(VariantProperties.MODEL, tallSideModelLocation).with(VariantProperties.UV_LOCK, true)
            )
            .with(
                Condition.condition().term(BlockStateProperties.EAST_WALL, WallSide.TALL),
                Variant.variant()
                    .with(VariantProperties.MODEL, tallSideModelLocation)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    .with(VariantProperties.UV_LOCK, true)
            )
            .with(
                Condition.condition().term(BlockStateProperties.SOUTH_WALL, WallSide.TALL),
                Variant.variant()
                    .with(VariantProperties.MODEL, tallSideModelLocation)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    .with(VariantProperties.UV_LOCK, true)
            )
            .with(
                Condition.condition().term(BlockStateProperties.WEST_WALL, WallSide.TALL),
                Variant.variant()
                    .with(VariantProperties.MODEL, tallSideModelLocation)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    .with(VariantProperties.UV_LOCK, true)
            );
    }

    static BlockStateGenerator createFenceGate(
        Block fenceGateBlock, ResourceLocation openModelLocation, ResourceLocation closedModelLocation, ResourceLocation wallOpenModelLocation, ResourceLocation wallClosedModelLocation, boolean uvLock
    ) {
        return MultiVariantGenerator.multiVariant(fenceGateBlock, Variant.variant().with(VariantProperties.UV_LOCK, uvLock))
            .with(createHorizontalFacingDispatchAlt())
            .with(
                PropertyDispatch.properties(BlockStateProperties.IN_WALL, BlockStateProperties.OPEN)
                    .select(false, false, Variant.variant().with(VariantProperties.MODEL, closedModelLocation))
                    .select(true, false, Variant.variant().with(VariantProperties.MODEL, wallClosedModelLocation))
                    .select(false, true, Variant.variant().with(VariantProperties.MODEL, openModelLocation))
                    .select(true, true, Variant.variant().with(VariantProperties.MODEL, wallOpenModelLocation))
            );
    }

    static BlockStateGenerator createStairs(Block stairsBlock, ResourceLocation innerModelLocation, ResourceLocation straightModelLocation, ResourceLocation outerModelLocation) {
        return MultiVariantGenerator.multiVariant(stairsBlock)
            .with(
                PropertyDispatch.properties(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.HALF, BlockStateProperties.STAIRS_SHAPE)
                    .select(Direction.EAST, Half.BOTTOM, StairsShape.STRAIGHT, Variant.variant().with(VariantProperties.MODEL, straightModelLocation))
                    .select(
                        Direction.WEST,
                        Half.BOTTOM,
                        StairsShape.STRAIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, straightModelLocation)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.SOUTH,
                        Half.BOTTOM,
                        StairsShape.STRAIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, straightModelLocation)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.NORTH,
                        Half.BOTTOM,
                        StairsShape.STRAIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, straightModelLocation)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(Direction.EAST, Half.BOTTOM, StairsShape.OUTER_RIGHT, Variant.variant().with(VariantProperties.MODEL, outerModelLocation))
                    .select(
                        Direction.WEST,
                        Half.BOTTOM,
                        StairsShape.OUTER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, outerModelLocation)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.SOUTH,
                        Half.BOTTOM,
                        StairsShape.OUTER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, outerModelLocation)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.NORTH,
                        Half.BOTTOM,
                        StairsShape.OUTER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, outerModelLocation)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.EAST,
                        Half.BOTTOM,
                        StairsShape.OUTER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, outerModelLocation)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.WEST,
                        Half.BOTTOM,
                        StairsShape.OUTER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, outerModelLocation)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(Direction.SOUTH, Half.BOTTOM, StairsShape.OUTER_LEFT, Variant.variant().with(VariantProperties.MODEL, outerModelLocation))
                    .select(
                        Direction.NORTH,
                        Half.BOTTOM,
                        StairsShape.OUTER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, outerModelLocation)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(Direction.EAST, Half.BOTTOM, StairsShape.INNER_RIGHT, Variant.variant().with(VariantProperties.MODEL, innerModelLocation))
                    .select(
                        Direction.WEST,
                        Half.BOTTOM,
                        StairsShape.INNER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, innerModelLocation)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.SOUTH,
                        Half.BOTTOM,
                        StairsShape.INNER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, innerModelLocation)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.NORTH,
                        Half.BOTTOM,
                        StairsShape.INNER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, innerModelLocation)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.EAST,
                        Half.BOTTOM,
                        StairsShape.INNER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, innerModelLocation)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.WEST,
                        Half.BOTTOM,
                        StairsShape.INNER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, innerModelLocation)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(Direction.SOUTH, Half.BOTTOM, StairsShape.INNER_LEFT, Variant.variant().with(VariantProperties.MODEL, innerModelLocation))
                    .select(
                        Direction.NORTH,
                        Half.BOTTOM,
                        StairsShape.INNER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, innerModelLocation)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.EAST,
                        Half.TOP,
                        StairsShape.STRAIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, straightModelLocation)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.WEST,
                        Half.TOP,
                        StairsShape.STRAIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, straightModelLocation)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.SOUTH,
                        Half.TOP,
                        StairsShape.STRAIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, straightModelLocation)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.NORTH,
                        Half.TOP,
                        StairsShape.STRAIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, straightModelLocation)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.EAST,
                        Half.TOP,
                        StairsShape.OUTER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, outerModelLocation)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.WEST,
                        Half.TOP,
                        StairsShape.OUTER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, outerModelLocation)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.SOUTH,
                        Half.TOP,
                        StairsShape.OUTER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, outerModelLocation)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.NORTH,
                        Half.TOP,
                        StairsShape.OUTER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, outerModelLocation)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.EAST,
                        Half.TOP,
                        StairsShape.OUTER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, outerModelLocation)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.WEST,
                        Half.TOP,
                        StairsShape.OUTER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, outerModelLocation)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.SOUTH,
                        Half.TOP,
                        StairsShape.OUTER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, outerModelLocation)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.NORTH,
                        Half.TOP,
                        StairsShape.OUTER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, outerModelLocation)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.EAST,
                        Half.TOP,
                        StairsShape.INNER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, innerModelLocation)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.WEST,
                        Half.TOP,
                        StairsShape.INNER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, innerModelLocation)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.SOUTH,
                        Half.TOP,
                        StairsShape.INNER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, innerModelLocation)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.NORTH,
                        Half.TOP,
                        StairsShape.INNER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, innerModelLocation)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.EAST,
                        Half.TOP,
                        StairsShape.INNER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, innerModelLocation)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.WEST,
                        Half.TOP,
                        StairsShape.INNER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, innerModelLocation)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.SOUTH,
                        Half.TOP,
                        StairsShape.INNER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, innerModelLocation)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.NORTH,
                        Half.TOP,
                        StairsShape.INNER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, innerModelLocation)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
            );
    }

    private static BlockStateGenerator createOrientableTrapdoor(
        Block orientableTrapdoorBlock, ResourceLocation topModelLocation, ResourceLocation bottomModelLocation, ResourceLocation openModelLocation
    ) {
        return MultiVariantGenerator.multiVariant(orientableTrapdoorBlock)
            .with(
                PropertyDispatch.properties(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.HALF, BlockStateProperties.OPEN)
                    .select(Direction.NORTH, Half.BOTTOM, false, Variant.variant().with(VariantProperties.MODEL, bottomModelLocation))
                    .select(
                        Direction.SOUTH,
                        Half.BOTTOM,
                        false,
                        Variant.variant().with(VariantProperties.MODEL, bottomModelLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    )
                    .select(
                        Direction.EAST,
                        Half.BOTTOM,
                        false,
                        Variant.variant().with(VariantProperties.MODEL, bottomModelLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .select(
                        Direction.WEST,
                        Half.BOTTOM,
                        false,
                        Variant.variant().with(VariantProperties.MODEL, bottomModelLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
                    .select(Direction.NORTH, Half.TOP, false, Variant.variant().with(VariantProperties.MODEL, topModelLocation))
                    .select(
                        Direction.SOUTH,
                        Half.TOP,
                        false,
                        Variant.variant().with(VariantProperties.MODEL, topModelLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    )
                    .select(
                        Direction.EAST,
                        Half.TOP,
                        false,
                        Variant.variant().with(VariantProperties.MODEL, topModelLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .select(
                        Direction.WEST,
                        Half.TOP,
                        false,
                        Variant.variant().with(VariantProperties.MODEL, topModelLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
                    .select(Direction.NORTH, Half.BOTTOM, true, Variant.variant().with(VariantProperties.MODEL, openModelLocation))
                    .select(
                        Direction.SOUTH,
                        Half.BOTTOM,
                        true,
                        Variant.variant().with(VariantProperties.MODEL, openModelLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    )
                    .select(
                        Direction.EAST,
                        Half.BOTTOM,
                        true,
                        Variant.variant().with(VariantProperties.MODEL, openModelLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .select(
                        Direction.WEST,
                        Half.BOTTOM,
                        true,
                        Variant.variant().with(VariantProperties.MODEL, openModelLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
                    .select(
                        Direction.NORTH,
                        Half.TOP,
                        true,
                        Variant.variant()
                            .with(VariantProperties.MODEL, openModelLocation)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    )
                    .select(
                        Direction.SOUTH,
                        Half.TOP,
                        true,
                        Variant.variant()
                            .with(VariantProperties.MODEL, openModelLocation)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R0)
                    )
                    .select(
                        Direction.EAST,
                        Half.TOP,
                        true,
                        Variant.variant()
                            .with(VariantProperties.MODEL, openModelLocation)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
                    .select(
                        Direction.WEST,
                        Half.TOP,
                        true,
                        Variant.variant()
                            .with(VariantProperties.MODEL, openModelLocation)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
            );
    }

    private static BlockStateGenerator createTrapdoor(Block trapdoorBlock, ResourceLocation topModelLocation, ResourceLocation bottomModelLocation, ResourceLocation openModelLocation) {
        return MultiVariantGenerator.multiVariant(trapdoorBlock)
            .with(
                PropertyDispatch.properties(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.HALF, BlockStateProperties.OPEN)
                    .select(Direction.NORTH, Half.BOTTOM, false, Variant.variant().with(VariantProperties.MODEL, bottomModelLocation))
                    .select(Direction.SOUTH, Half.BOTTOM, false, Variant.variant().with(VariantProperties.MODEL, bottomModelLocation))
                    .select(Direction.EAST, Half.BOTTOM, false, Variant.variant().with(VariantProperties.MODEL, bottomModelLocation))
                    .select(Direction.WEST, Half.BOTTOM, false, Variant.variant().with(VariantProperties.MODEL, bottomModelLocation))
                    .select(Direction.NORTH, Half.TOP, false, Variant.variant().with(VariantProperties.MODEL, topModelLocation))
                    .select(Direction.SOUTH, Half.TOP, false, Variant.variant().with(VariantProperties.MODEL, topModelLocation))
                    .select(Direction.EAST, Half.TOP, false, Variant.variant().with(VariantProperties.MODEL, topModelLocation))
                    .select(Direction.WEST, Half.TOP, false, Variant.variant().with(VariantProperties.MODEL, topModelLocation))
                    .select(Direction.NORTH, Half.BOTTOM, true, Variant.variant().with(VariantProperties.MODEL, openModelLocation))
                    .select(
                        Direction.SOUTH,
                        Half.BOTTOM,
                        true,
                        Variant.variant().with(VariantProperties.MODEL, openModelLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    )
                    .select(
                        Direction.EAST,
                        Half.BOTTOM,
                        true,
                        Variant.variant().with(VariantProperties.MODEL, openModelLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .select(
                        Direction.WEST,
                        Half.BOTTOM,
                        true,
                        Variant.variant().with(VariantProperties.MODEL, openModelLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
                    .select(Direction.NORTH, Half.TOP, true, Variant.variant().with(VariantProperties.MODEL, openModelLocation))
                    .select(
                        Direction.SOUTH,
                        Half.TOP,
                        true,
                        Variant.variant().with(VariantProperties.MODEL, openModelLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    )
                    .select(
                        Direction.EAST,
                        Half.TOP,
                        true,
                        Variant.variant().with(VariantProperties.MODEL, openModelLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .select(
                        Direction.WEST,
                        Half.TOP,
                        true,
                        Variant.variant().with(VariantProperties.MODEL, openModelLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
            );
    }

    static MultiVariantGenerator createSimpleBlock(Block block, ResourceLocation modelLocation) {
        return MultiVariantGenerator.multiVariant(block, Variant.variant().with(VariantProperties.MODEL, modelLocation));
    }

    private static PropertyDispatch createRotatedPillar() {
        return PropertyDispatch.property(BlockStateProperties.AXIS)
            .select(Direction.Axis.Y, Variant.variant())
            .select(Direction.Axis.Z, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
            .select(
                Direction.Axis.X,
                Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
            );
    }

    static BlockStateGenerator createPillarBlockUVLocked(
        Block block, TextureMapping textureMapping, BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput
    ) {
        ResourceLocation resourcelocation = ModelTemplates.CUBE_COLUMN_UV_LOCKED_X.create(block, textureMapping, modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.CUBE_COLUMN_UV_LOCKED_Y.create(block, textureMapping, modelOutput);
        ResourceLocation resourcelocation2 = ModelTemplates.CUBE_COLUMN_UV_LOCKED_Z.create(block, textureMapping, modelOutput);
        ResourceLocation resourcelocation3 = ModelTemplates.CUBE_COLUMN.create(block, textureMapping, modelOutput);
        return MultiVariantGenerator.multiVariant(block, Variant.variant().with(VariantProperties.MODEL, resourcelocation3))
            .with(
                PropertyDispatch.property(BlockStateProperties.AXIS)
                    .select(Direction.Axis.X, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                    .select(Direction.Axis.Y, Variant.variant().with(VariantProperties.MODEL, resourcelocation1))
                    .select(Direction.Axis.Z, Variant.variant().with(VariantProperties.MODEL, resourcelocation2))
            );
    }

    static BlockStateGenerator createAxisAlignedPillarBlock(Block axisAlignedPillarBlock, ResourceLocation modelLocation) {
        return MultiVariantGenerator.multiVariant(axisAlignedPillarBlock, Variant.variant().with(VariantProperties.MODEL, modelLocation)).with(createRotatedPillar());
    }

    private void createAxisAlignedPillarBlockCustomModel(Block axisAlignedPillarBlock, ResourceLocation modelLocation) {
        this.blockStateOutput.accept(createAxisAlignedPillarBlock(axisAlignedPillarBlock, modelLocation));
    }

    public void createAxisAlignedPillarBlock(Block axisAlignedPillarBlock, TexturedModel.Provider provider) {
        ResourceLocation resourcelocation = provider.create(axisAlignedPillarBlock, this.modelOutput);
        this.blockStateOutput.accept(createAxisAlignedPillarBlock(axisAlignedPillarBlock, resourcelocation));
    }

    private void createHorizontallyRotatedBlock(Block horizontallyRotatedBlock, TexturedModel.Provider provider) {
        ResourceLocation resourcelocation = provider.create(horizontallyRotatedBlock, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(horizontallyRotatedBlock, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                    .with(createHorizontalFacingDispatch())
            );
    }

    static BlockStateGenerator createRotatedPillarWithHorizontalVariant(Block rotatedPillarBlock, ResourceLocation modelLocation, ResourceLocation horizontalModelLocation) {
        return MultiVariantGenerator.multiVariant(rotatedPillarBlock)
            .with(
                PropertyDispatch.property(BlockStateProperties.AXIS)
                    .select(Direction.Axis.Y, Variant.variant().with(VariantProperties.MODEL, modelLocation))
                    .select(
                        Direction.Axis.Z,
                        Variant.variant().with(VariantProperties.MODEL, horizontalModelLocation).with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                    )
                    .select(
                        Direction.Axis.X,
                        Variant.variant()
                            .with(VariantProperties.MODEL, horizontalModelLocation)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
            );
    }

    private void createRotatedPillarWithHorizontalVariant(Block rotatedPillarBlock, TexturedModel.Provider modelProvider, TexturedModel.Provider horizontalModelProvider) {
        ResourceLocation resourcelocation = modelProvider.create(rotatedPillarBlock, this.modelOutput);
        ResourceLocation resourcelocation1 = horizontalModelProvider.create(rotatedPillarBlock, this.modelOutput);
        this.blockStateOutput.accept(createRotatedPillarWithHorizontalVariant(rotatedPillarBlock, resourcelocation, resourcelocation1));
    }

    private ResourceLocation createSuffixedVariant(
        Block block, String suffix, ModelTemplate modelTemplate, Function<ResourceLocation, TextureMapping> textureMappingGetter
    ) {
        return modelTemplate.createWithSuffix(block, suffix, textureMappingGetter.apply(TextureMapping.getBlockTexture(block, suffix)), this.modelOutput);
    }

    static BlockStateGenerator createPressurePlate(Block pressurePlateBlock, ResourceLocation unpoweredModelLocation, ResourceLocation poweredModelLocation) {
        return MultiVariantGenerator.multiVariant(pressurePlateBlock).with(createBooleanModelDispatch(BlockStateProperties.POWERED, poweredModelLocation, unpoweredModelLocation));
    }

    static BlockStateGenerator createSlab(Block slabBlock, ResourceLocation bottomHalfModelLocation, ResourceLocation topHalfModelLocation, ResourceLocation doubleModelLocation) {
        return MultiVariantGenerator.multiVariant(slabBlock)
            .with(
                PropertyDispatch.property(BlockStateProperties.SLAB_TYPE)
                    .select(SlabType.BOTTOM, Variant.variant().with(VariantProperties.MODEL, bottomHalfModelLocation))
                    .select(SlabType.TOP, Variant.variant().with(VariantProperties.MODEL, topHalfModelLocation))
                    .select(SlabType.DOUBLE, Variant.variant().with(VariantProperties.MODEL, doubleModelLocation))
            );
    }

    public void createTrivialCube(Block block) {
        this.createTrivialBlock(block, TexturedModel.CUBE);
    }

    public void createTrivialBlock(Block block, TexturedModel.Provider provider) {
        this.blockStateOutput.accept(createSimpleBlock(block, provider.create(block, this.modelOutput)));
    }

    private void createTrivialBlock(Block block, TextureMapping textureMapping, ModelTemplate modelTemplate) {
        ResourceLocation resourcelocation = modelTemplate.create(block, textureMapping, this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(block, resourcelocation));
    }

    private BlockModelGenerators.BlockFamilyProvider family(Block block) {
        TexturedModel texturedmodel = this.texturedModels.getOrDefault(block, TexturedModel.CUBE.get(block));
        return new BlockModelGenerators.BlockFamilyProvider(texturedmodel.getMapping()).fullBlock(block, texturedmodel.getTemplate());
    }

    public void createHangingSign(Block particleBlock, Block hangingSignBlock, Block wallHangingSignBlock) {
        TextureMapping texturemapping = TextureMapping.particle(particleBlock);
        ResourceLocation resourcelocation = ModelTemplates.PARTICLE_ONLY.create(hangingSignBlock, texturemapping, this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(hangingSignBlock, resourcelocation));
        this.blockStateOutput.accept(createSimpleBlock(wallHangingSignBlock, resourcelocation));
        this.createSimpleFlatItemModel(hangingSignBlock.asItem());
        this.skipAutoItemBlock(wallHangingSignBlock);
    }

    void createDoor(Block doorBlock) {
        TextureMapping texturemapping = TextureMapping.door(doorBlock);
        ResourceLocation resourcelocation = ModelTemplates.DOOR_BOTTOM_LEFT.create(doorBlock, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.DOOR_BOTTOM_LEFT_OPEN.create(doorBlock, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation2 = ModelTemplates.DOOR_BOTTOM_RIGHT.create(doorBlock, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation3 = ModelTemplates.DOOR_BOTTOM_RIGHT_OPEN.create(doorBlock, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation4 = ModelTemplates.DOOR_TOP_LEFT.create(doorBlock, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation5 = ModelTemplates.DOOR_TOP_LEFT_OPEN.create(doorBlock, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation6 = ModelTemplates.DOOR_TOP_RIGHT.create(doorBlock, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation7 = ModelTemplates.DOOR_TOP_RIGHT_OPEN.create(doorBlock, texturemapping, this.modelOutput);
        this.createSimpleFlatItemModel(doorBlock.asItem());
        this.blockStateOutput
            .accept(
                createDoor(
                    doorBlock,
                    resourcelocation,
                    resourcelocation1,
                    resourcelocation2,
                    resourcelocation3,
                    resourcelocation4,
                    resourcelocation5,
                    resourcelocation6,
                    resourcelocation7
                )
            );
    }

    private void copyDoorModel(Block doorBlock, Block sourceBlock) {
        ResourceLocation resourcelocation = ModelTemplates.DOOR_BOTTOM_LEFT.getDefaultModelLocation(doorBlock);
        ResourceLocation resourcelocation1 = ModelTemplates.DOOR_BOTTOM_LEFT_OPEN.getDefaultModelLocation(doorBlock);
        ResourceLocation resourcelocation2 = ModelTemplates.DOOR_BOTTOM_RIGHT.getDefaultModelLocation(doorBlock);
        ResourceLocation resourcelocation3 = ModelTemplates.DOOR_BOTTOM_RIGHT_OPEN.getDefaultModelLocation(doorBlock);
        ResourceLocation resourcelocation4 = ModelTemplates.DOOR_TOP_LEFT.getDefaultModelLocation(doorBlock);
        ResourceLocation resourcelocation5 = ModelTemplates.DOOR_TOP_LEFT_OPEN.getDefaultModelLocation(doorBlock);
        ResourceLocation resourcelocation6 = ModelTemplates.DOOR_TOP_RIGHT.getDefaultModelLocation(doorBlock);
        ResourceLocation resourcelocation7 = ModelTemplates.DOOR_TOP_RIGHT_OPEN.getDefaultModelLocation(doorBlock);
        this.delegateItemModel(sourceBlock, ModelLocationUtils.getModelLocation(doorBlock.asItem()));
        this.blockStateOutput
            .accept(
                createDoor(
                    sourceBlock,
                    resourcelocation,
                    resourcelocation1,
                    resourcelocation2,
                    resourcelocation3,
                    resourcelocation4,
                    resourcelocation5,
                    resourcelocation6,
                    resourcelocation7
                )
            );
    }

    void createOrientableTrapdoor(Block orientableTrapdoorBlock) {
        TextureMapping texturemapping = TextureMapping.defaultTexture(orientableTrapdoorBlock);
        ResourceLocation resourcelocation = ModelTemplates.ORIENTABLE_TRAPDOOR_TOP.create(orientableTrapdoorBlock, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.ORIENTABLE_TRAPDOOR_BOTTOM.create(orientableTrapdoorBlock, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation2 = ModelTemplates.ORIENTABLE_TRAPDOOR_OPEN.create(orientableTrapdoorBlock, texturemapping, this.modelOutput);
        this.blockStateOutput.accept(createOrientableTrapdoor(orientableTrapdoorBlock, resourcelocation, resourcelocation1, resourcelocation2));
        this.delegateItemModel(orientableTrapdoorBlock, resourcelocation1);
    }

    void createTrapdoor(Block trapdoorBlock) {
        TextureMapping texturemapping = TextureMapping.defaultTexture(trapdoorBlock);
        ResourceLocation resourcelocation = ModelTemplates.TRAPDOOR_TOP.create(trapdoorBlock, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.TRAPDOOR_BOTTOM.create(trapdoorBlock, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation2 = ModelTemplates.TRAPDOOR_OPEN.create(trapdoorBlock, texturemapping, this.modelOutput);
        this.blockStateOutput.accept(createTrapdoor(trapdoorBlock, resourcelocation, resourcelocation1, resourcelocation2));
        this.delegateItemModel(trapdoorBlock, resourcelocation1);
    }

    private void copyTrapdoorModel(Block trapdoorBlock, Block sourceBlock) {
        ResourceLocation resourcelocation = ModelTemplates.TRAPDOOR_TOP.getDefaultModelLocation(trapdoorBlock);
        ResourceLocation resourcelocation1 = ModelTemplates.TRAPDOOR_BOTTOM.getDefaultModelLocation(trapdoorBlock);
        ResourceLocation resourcelocation2 = ModelTemplates.TRAPDOOR_OPEN.getDefaultModelLocation(trapdoorBlock);
        this.delegateItemModel(sourceBlock, ModelLocationUtils.getModelLocation(trapdoorBlock.asItem()));
        this.blockStateOutput.accept(createTrapdoor(sourceBlock, resourcelocation, resourcelocation1, resourcelocation2));
    }

    private void createBigDripLeafBlock() {
        this.skipAutoItemBlock(Blocks.BIG_DRIPLEAF);
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.BIG_DRIPLEAF);
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.BIG_DRIPLEAF, "_partial_tilt");
        ResourceLocation resourcelocation2 = ModelLocationUtils.getModelLocation(Blocks.BIG_DRIPLEAF, "_full_tilt");
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.BIG_DRIPLEAF)
                    .with(createHorizontalFacingDispatch())
                    .with(
                        PropertyDispatch.property(BlockStateProperties.TILT)
                            .select(Tilt.NONE, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                            .select(Tilt.UNSTABLE, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                            .select(Tilt.PARTIAL, Variant.variant().with(VariantProperties.MODEL, resourcelocation1))
                            .select(Tilt.FULL, Variant.variant().with(VariantProperties.MODEL, resourcelocation2))
                    )
            );
    }

    private BlockModelGenerators.WoodProvider woodProvider(Block logBlock) {
        return new BlockModelGenerators.WoodProvider(TextureMapping.logColumn(logBlock));
    }

    private void createNonTemplateModelBlock(Block block) {
        this.createNonTemplateModelBlock(block, block);
    }

    private void createNonTemplateModelBlock(Block block, Block modelBlock) {
        this.blockStateOutput.accept(createSimpleBlock(block, ModelLocationUtils.getModelLocation(modelBlock)));
    }

    private void createCrossBlockWithDefaultItem(Block crossBlock, BlockModelGenerators.TintState tintState) {
        this.createSimpleFlatItemModel(crossBlock);
        this.createCrossBlock(crossBlock, tintState);
    }

    private void createCrossBlockWithDefaultItem(Block crossBlock, BlockModelGenerators.TintState tintState, TextureMapping textureMapping) {
        this.createSimpleFlatItemModel(crossBlock);
        this.createCrossBlock(crossBlock, tintState, textureMapping);
    }

    private void createCrossBlock(Block crossBlock, BlockModelGenerators.TintState tintState) {
        TextureMapping texturemapping = TextureMapping.cross(crossBlock);
        this.createCrossBlock(crossBlock, tintState, texturemapping);
    }

    private void createCrossBlock(Block crossBlock, BlockModelGenerators.TintState tintState, TextureMapping textureMapping) {
        ResourceLocation resourcelocation = tintState.getCross().create(crossBlock, textureMapping, this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(crossBlock, resourcelocation));
    }

    private void createCrossBlock(Block crossBlock, BlockModelGenerators.TintState tintState, Property<Integer> property, int... propertyValues) {
        if (property.getPossibleValues().size() != propertyValues.length) {
            throw new IllegalArgumentException("missing values for property: " + property);
        } else {
            PropertyDispatch propertydispatch = PropertyDispatch.property(property).generate(p_272381_ -> {
                String s = "_stage" + propertyValues[p_272381_];
                TextureMapping texturemapping = TextureMapping.cross(TextureMapping.getBlockTexture(crossBlock, s));
                ResourceLocation resourcelocation = tintState.getCross().createWithSuffix(crossBlock, s, texturemapping, this.modelOutput);
                return Variant.variant().with(VariantProperties.MODEL, resourcelocation);
            });
            this.createSimpleFlatItemModel(crossBlock.asItem());
            this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(crossBlock).with(propertydispatch));
        }
    }

    private void createPlant(Block plantBlock, Block pottedPlantBlock, BlockModelGenerators.TintState tintState) {
        this.createCrossBlockWithDefaultItem(plantBlock, tintState);
        TextureMapping texturemapping = TextureMapping.plant(plantBlock);
        ResourceLocation resourcelocation = tintState.getCrossPot().create(pottedPlantBlock, texturemapping, this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(pottedPlantBlock, resourcelocation));
    }

    private void createCoralFans(Block coralFanBlock, Block coralWallFanBlock) {
        TexturedModel texturedmodel = TexturedModel.CORAL_FAN.get(coralFanBlock);
        ResourceLocation resourcelocation = texturedmodel.create(coralFanBlock, this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(coralFanBlock, resourcelocation));
        ResourceLocation resourcelocation1 = ModelTemplates.CORAL_WALL_FAN.create(coralWallFanBlock, texturedmodel.getMapping(), this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(coralWallFanBlock, Variant.variant().with(VariantProperties.MODEL, resourcelocation1))
                    .with(createHorizontalFacingDispatch())
            );
        this.createSimpleFlatItemModel(coralFanBlock);
    }

    private void createStems(Block unattachedStemBlock, Block attachedStemBlock) {
        this.createSimpleFlatItemModel(unattachedStemBlock.asItem());
        TextureMapping texturemapping = TextureMapping.stem(unattachedStemBlock);
        TextureMapping texturemapping1 = TextureMapping.attachedStem(unattachedStemBlock, attachedStemBlock);
        ResourceLocation resourcelocation = ModelTemplates.ATTACHED_STEM.create(attachedStemBlock, texturemapping1, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(attachedStemBlock, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                    .with(
                        PropertyDispatch.property(BlockStateProperties.HORIZONTAL_FACING)
                            .select(Direction.WEST, Variant.variant())
                            .select(Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                            .select(Direction.NORTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                            .select(Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                    )
            );
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(unattachedStemBlock)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.AGE_7)
                            .generate(
                                p_176108_ -> Variant.variant()
                                        .with(VariantProperties.MODEL, ModelTemplates.STEMS[p_176108_].create(unattachedStemBlock, texturemapping, this.modelOutput))
                            )
                    )
            );
    }

    private void createPitcherPlant() {
        Block block = Blocks.PITCHER_PLANT;
        this.createSimpleFlatItemModel(block.asItem());
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(block, "_top");
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(block, "_bottom");
        this.createDoubleBlock(block, resourcelocation, resourcelocation1);
    }

    private void createPitcherCrop() {
        Block block = Blocks.PITCHER_CROP;
        this.createSimpleFlatItemModel(block.asItem());
        PropertyDispatch propertydispatch = PropertyDispatch.properties(PitcherCropBlock.AGE, BlockStateProperties.DOUBLE_BLOCK_HALF)
            .generate((p_339371_, p_339372_) -> {
                return switch (p_339372_) {
                    case UPPER -> Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(block, "_top_stage_" + p_339371_));
                    case LOWER -> Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(block, "_bottom_stage_" + p_339371_));
                };
            });
        this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(block).with(propertydispatch));
    }

    private void createCoral(
        Block coralBlock, Block deadCoralBlock, Block coralFullBlock, Block deadCoralFullBlock, Block coralFanBlock, Block deadCoralFanBlock, Block coralWallFanBlock, Block deadCoralWallFanBlock
    ) {
        this.createCrossBlockWithDefaultItem(coralBlock, BlockModelGenerators.TintState.NOT_TINTED);
        this.createCrossBlockWithDefaultItem(deadCoralBlock, BlockModelGenerators.TintState.NOT_TINTED);
        this.createTrivialCube(coralFullBlock);
        this.createTrivialCube(deadCoralFullBlock);
        this.createCoralFans(coralFanBlock, coralWallFanBlock);
        this.createCoralFans(deadCoralFanBlock, deadCoralWallFanBlock);
    }

    private void createDoublePlant(Block doublePlantBlock, BlockModelGenerators.TintState tintState) {
        this.createSimpleFlatItemModel(doublePlantBlock, "_top");
        ResourceLocation resourcelocation = this.createSuffixedVariant(doublePlantBlock, "_top", tintState.getCross(), TextureMapping::cross);
        ResourceLocation resourcelocation1 = this.createSuffixedVariant(doublePlantBlock, "_bottom", tintState.getCross(), TextureMapping::cross);
        this.createDoubleBlock(doublePlantBlock, resourcelocation, resourcelocation1);
    }

    private void createSunflower() {
        this.createSimpleFlatItemModel(Blocks.SUNFLOWER, "_front");
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.SUNFLOWER, "_top");
        ResourceLocation resourcelocation1 = this.createSuffixedVariant(
            Blocks.SUNFLOWER, "_bottom", BlockModelGenerators.TintState.NOT_TINTED.getCross(), TextureMapping::cross
        );
        this.createDoubleBlock(Blocks.SUNFLOWER, resourcelocation, resourcelocation1);
    }

    private void createTallSeagrass() {
        ResourceLocation resourcelocation = this.createSuffixedVariant(Blocks.TALL_SEAGRASS, "_top", ModelTemplates.SEAGRASS, TextureMapping::defaultTexture);
        ResourceLocation resourcelocation1 = this.createSuffixedVariant(
            Blocks.TALL_SEAGRASS, "_bottom", ModelTemplates.SEAGRASS, TextureMapping::defaultTexture
        );
        this.createDoubleBlock(Blocks.TALL_SEAGRASS, resourcelocation, resourcelocation1);
    }

    private void createSmallDripleaf() {
        this.skipAutoItemBlock(Blocks.SMALL_DRIPLEAF);
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.SMALL_DRIPLEAF, "_top");
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.SMALL_DRIPLEAF, "_bottom");
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.SMALL_DRIPLEAF)
                    .with(createHorizontalFacingDispatch())
                    .with(
                        PropertyDispatch.property(BlockStateProperties.DOUBLE_BLOCK_HALF)
                            .select(DoubleBlockHalf.LOWER, Variant.variant().with(VariantProperties.MODEL, resourcelocation1))
                            .select(DoubleBlockHalf.UPPER, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                    )
            );
    }

    private void createDoubleBlock(Block doubleBlock, ResourceLocation topHalfModelLocation, ResourceLocation bottomHalfModelLocation) {
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(doubleBlock)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.DOUBLE_BLOCK_HALF)
                            .select(DoubleBlockHalf.LOWER, Variant.variant().with(VariantProperties.MODEL, bottomHalfModelLocation))
                            .select(DoubleBlockHalf.UPPER, Variant.variant().with(VariantProperties.MODEL, topHalfModelLocation))
                    )
            );
    }

    private void createPassiveRail(Block railBlock) {
        TextureMapping texturemapping = TextureMapping.rail(railBlock);
        TextureMapping texturemapping1 = TextureMapping.rail(TextureMapping.getBlockTexture(railBlock, "_corner"));
        ResourceLocation resourcelocation = ModelTemplates.RAIL_FLAT.create(railBlock, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.RAIL_CURVED.create(railBlock, texturemapping1, this.modelOutput);
        ResourceLocation resourcelocation2 = ModelTemplates.RAIL_RAISED_NE.create(railBlock, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation3 = ModelTemplates.RAIL_RAISED_SW.create(railBlock, texturemapping, this.modelOutput);
        this.createSimpleFlatItemModel(railBlock);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(railBlock)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.RAIL_SHAPE)
                            .select(RailShape.NORTH_SOUTH, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                            .select(
                                RailShape.EAST_WEST,
                                Variant.variant().with(VariantProperties.MODEL, resourcelocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                RailShape.ASCENDING_EAST,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation2)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                RailShape.ASCENDING_WEST,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation3)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(RailShape.ASCENDING_NORTH, Variant.variant().with(VariantProperties.MODEL, resourcelocation2))
                            .select(RailShape.ASCENDING_SOUTH, Variant.variant().with(VariantProperties.MODEL, resourcelocation3))
                            .select(RailShape.SOUTH_EAST, Variant.variant().with(VariantProperties.MODEL, resourcelocation1))
                            .select(
                                RailShape.SOUTH_WEST,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation1)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                RailShape.NORTH_WEST,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation1)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                RailShape.NORTH_EAST,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation1)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                    )
            );
    }

    private void createActiveRail(Block railBlock) {
        ResourceLocation resourcelocation = this.createSuffixedVariant(railBlock, "", ModelTemplates.RAIL_FLAT, TextureMapping::rail);
        ResourceLocation resourcelocation1 = this.createSuffixedVariant(railBlock, "", ModelTemplates.RAIL_RAISED_NE, TextureMapping::rail);
        ResourceLocation resourcelocation2 = this.createSuffixedVariant(railBlock, "", ModelTemplates.RAIL_RAISED_SW, TextureMapping::rail);
        ResourceLocation resourcelocation3 = this.createSuffixedVariant(railBlock, "_on", ModelTemplates.RAIL_FLAT, TextureMapping::rail);
        ResourceLocation resourcelocation4 = this.createSuffixedVariant(railBlock, "_on", ModelTemplates.RAIL_RAISED_NE, TextureMapping::rail);
        ResourceLocation resourcelocation5 = this.createSuffixedVariant(railBlock, "_on", ModelTemplates.RAIL_RAISED_SW, TextureMapping::rail);
        PropertyDispatch propertydispatch = PropertyDispatch.properties(BlockStateProperties.POWERED, BlockStateProperties.RAIL_SHAPE_STRAIGHT)
            .generate(
                (p_176166_, p_176167_) -> {
                    switch (p_176167_) {
                        case NORTH_SOUTH:
                            return Variant.variant().with(VariantProperties.MODEL, p_176166_ ? resourcelocation3 : resourcelocation);
                        case EAST_WEST:
                            return Variant.variant()
                                .with(VariantProperties.MODEL, p_176166_ ? resourcelocation3 : resourcelocation)
                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
                        case ASCENDING_EAST:
                            return Variant.variant()
                                .with(VariantProperties.MODEL, p_176166_ ? resourcelocation4 : resourcelocation1)
                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
                        case ASCENDING_WEST:
                            return Variant.variant()
                                .with(VariantProperties.MODEL, p_176166_ ? resourcelocation5 : resourcelocation2)
                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
                        case ASCENDING_NORTH:
                            return Variant.variant().with(VariantProperties.MODEL, p_176166_ ? resourcelocation4 : resourcelocation1);
                        case ASCENDING_SOUTH:
                            return Variant.variant().with(VariantProperties.MODEL, p_176166_ ? resourcelocation5 : resourcelocation2);
                        default:
                            throw new UnsupportedOperationException("Fix you generator!");
                    }
                }
            );
        this.createSimpleFlatItemModel(railBlock);
        this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(railBlock).with(propertydispatch));
    }

    private BlockModelGenerators.BlockEntityModelGenerator blockEntityModels(ResourceLocation entityBlockModelLocation, Block particleBlock) {
        return new BlockModelGenerators.BlockEntityModelGenerator(entityBlockModelLocation, particleBlock);
    }

    private BlockModelGenerators.BlockEntityModelGenerator blockEntityModels(Block entityBlockBaseModel, Block particleBlock) {
        return new BlockModelGenerators.BlockEntityModelGenerator(ModelLocationUtils.getModelLocation(entityBlockBaseModel), particleBlock);
    }

    private void createAirLikeBlock(Block airLikeBlock, Item particleItem) {
        ResourceLocation resourcelocation = ModelTemplates.PARTICLE_ONLY.create(airLikeBlock, TextureMapping.particleFromItem(particleItem), this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(airLikeBlock, resourcelocation));
    }

    private void createAirLikeBlock(Block airLikeBlock, ResourceLocation particleTexture) {
        ResourceLocation resourcelocation = ModelTemplates.PARTICLE_ONLY.create(airLikeBlock, TextureMapping.particle(particleTexture), this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(airLikeBlock, resourcelocation));
    }

    private void createFullAndCarpetBlocks(Block fullBlock, Block carpetBlock) {
        this.createTrivialCube(fullBlock);
        ResourceLocation resourcelocation = TexturedModel.CARPET.get(fullBlock).create(carpetBlock, this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(carpetBlock, resourcelocation));
    }

    private void createFlowerBed(Block flowerBedBlock) {
        this.createSimpleFlatItemModel(flowerBedBlock.asItem());
        ResourceLocation resourcelocation = TexturedModel.FLOWERBED_1.create(flowerBedBlock, this.modelOutput);
        ResourceLocation resourcelocation1 = TexturedModel.FLOWERBED_2.create(flowerBedBlock, this.modelOutput);
        ResourceLocation resourcelocation2 = TexturedModel.FLOWERBED_3.create(flowerBedBlock, this.modelOutput);
        ResourceLocation resourcelocation3 = TexturedModel.FLOWERBED_4.create(flowerBedBlock, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(flowerBedBlock)
                    .with(
                        Condition.condition()
                            .term(BlockStateProperties.FLOWER_AMOUNT, 1, 2, 3, 4)
                            .term(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 1, 2, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .with(
                        Condition.condition()
                            .term(BlockStateProperties.FLOWER_AMOUNT, 1, 2, 3, 4)
                            .term(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 1, 2, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 2, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation1)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 2, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation1).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 2, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation1).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 2, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation1).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation2)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation3)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
            );
    }

    private void createColoredBlockWithRandomRotations(TexturedModel.Provider modelProvider, Block... coloredBlocks) {
        for (Block block : coloredBlocks) {
            ResourceLocation resourcelocation = modelProvider.create(block, this.modelOutput);
            this.blockStateOutput.accept(createRotatedVariant(block, resourcelocation));
        }
    }

    private void createColoredBlockWithStateRotations(TexturedModel.Provider modelProvider, Block... coloredBlocks) {
        for (Block block : coloredBlocks) {
            ResourceLocation resourcelocation = modelProvider.create(block, this.modelOutput);
            this.blockStateOutput
                .accept(
                    MultiVariantGenerator.multiVariant(block, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                        .with(createHorizontalFacingDispatchAlt())
                );
        }
    }

    private void createGlassBlocks(Block glassBlock, Block paneBlock) {
        this.createTrivialCube(glassBlock);
        TextureMapping texturemapping = TextureMapping.pane(glassBlock, paneBlock);
        ResourceLocation resourcelocation = ModelTemplates.STAINED_GLASS_PANE_POST.create(paneBlock, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.STAINED_GLASS_PANE_SIDE.create(paneBlock, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation2 = ModelTemplates.STAINED_GLASS_PANE_SIDE_ALT.create(paneBlock, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation3 = ModelTemplates.STAINED_GLASS_PANE_NOSIDE.create(paneBlock, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation4 = ModelTemplates.STAINED_GLASS_PANE_NOSIDE_ALT.create(paneBlock, texturemapping, this.modelOutput);
        Item item = paneBlock.asItem();
        ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(item), TextureMapping.layer0(glassBlock), this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(paneBlock)
                    .with(Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                    .with(Condition.condition().term(BlockStateProperties.NORTH, true), Variant.variant().with(VariantProperties.MODEL, resourcelocation1))
                    .with(
                        Condition.condition().term(BlockStateProperties.EAST, true),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation1).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .with(Condition.condition().term(BlockStateProperties.SOUTH, true), Variant.variant().with(VariantProperties.MODEL, resourcelocation2))
                    .with(
                        Condition.condition().term(BlockStateProperties.WEST, true),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .with(Condition.condition().term(BlockStateProperties.NORTH, false), Variant.variant().with(VariantProperties.MODEL, resourcelocation3))
                    .with(Condition.condition().term(BlockStateProperties.EAST, false), Variant.variant().with(VariantProperties.MODEL, resourcelocation4))
                    .with(
                        Condition.condition().term(BlockStateProperties.SOUTH, false),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation4).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.WEST, false),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
            );
    }

    private void createCommandBlock(Block commandBlock) {
        TextureMapping texturemapping = TextureMapping.commandBlock(commandBlock);
        ResourceLocation resourcelocation = ModelTemplates.COMMAND_BLOCK.create(commandBlock, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = this.createSuffixedVariant(
            commandBlock, "_conditional", ModelTemplates.COMMAND_BLOCK, p_176193_ -> texturemapping.copyAndUpdate(TextureSlot.SIDE, p_176193_)
        );
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(commandBlock)
                    .with(createBooleanModelDispatch(BlockStateProperties.CONDITIONAL, resourcelocation1, resourcelocation))
                    .with(createFacingDispatch())
            );
    }

    private void createAnvil(Block anvilBlock) {
        ResourceLocation resourcelocation = TexturedModel.ANVIL.create(anvilBlock, this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(anvilBlock, resourcelocation).with(createHorizontalFacingDispatchAlt()));
    }

    private List<Variant> createBambooModels(int age) {
        String s = "_age" + age;
        return IntStream.range(1, 5)
            .mapToObj(p_176139_ -> Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.BAMBOO, p_176139_ + s)))
            .collect(Collectors.toList());
    }

    private void createBamboo() {
        this.skipAutoItemBlock(Blocks.BAMBOO);
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(Blocks.BAMBOO)
                    .with(Condition.condition().term(BlockStateProperties.AGE_1, 0), this.createBambooModels(0))
                    .with(Condition.condition().term(BlockStateProperties.AGE_1, 1), this.createBambooModels(1))
                    .with(
                        Condition.condition().term(BlockStateProperties.BAMBOO_LEAVES, BambooLeaves.SMALL),
                        Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.BAMBOO, "_small_leaves"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.BAMBOO_LEAVES, BambooLeaves.LARGE),
                        Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.BAMBOO, "_large_leaves"))
                    )
            );
    }

    private PropertyDispatch createColumnWithFacing() {
        return PropertyDispatch.property(BlockStateProperties.FACING)
            .select(Direction.DOWN, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R180))
            .select(Direction.UP, Variant.variant())
            .select(Direction.NORTH, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
            .select(
                Direction.SOUTH,
                Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
            )
            .select(
                Direction.WEST,
                Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
            )
            .select(
                Direction.EAST,
                Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
            );
    }

    private void createBarrel() {
        ResourceLocation resourcelocation = TextureMapping.getBlockTexture(Blocks.BARREL, "_top_open");
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.BARREL)
                    .with(this.createColumnWithFacing())
                    .with(
                        PropertyDispatch.property(BlockStateProperties.OPEN)
                            .select(
                                false, Variant.variant().with(VariantProperties.MODEL, TexturedModel.CUBE_TOP_BOTTOM.create(Blocks.BARREL, this.modelOutput))
                            )
                            .select(
                                true,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        TexturedModel.CUBE_TOP_BOTTOM
                                            .get(Blocks.BARREL)
                                            .updateTextures(p_176216_ -> p_176216_.put(TextureSlot.TOP, resourcelocation))
                                            .createWithSuffix(Blocks.BARREL, "_open", this.modelOutput)
                                    )
                            )
                    )
            );
    }

    private static <T extends Comparable<T>> PropertyDispatch createEmptyOrFullDispatch(
        Property<T> property, T minimumValueForFullVariant, ResourceLocation fullVariantModelLocation, ResourceLocation emptyVariantModelLocation
    ) {
        Variant variant = Variant.variant().with(VariantProperties.MODEL, fullVariantModelLocation);
        Variant variant1 = Variant.variant().with(VariantProperties.MODEL, emptyVariantModelLocation);
        return PropertyDispatch.property(property).generate(p_176130_ -> {
            boolean flag = p_176130_.compareTo(minimumValueForFullVariant) >= 0;
            return flag ? variant : variant1;
        });
    }

    private void createBeeNest(Block beeNestBlock, Function<Block, TextureMapping> textureMappingGetter) {
        TextureMapping texturemapping = textureMappingGetter.apply(beeNestBlock).copyForced(TextureSlot.SIDE, TextureSlot.PARTICLE);
        TextureMapping texturemapping1 = texturemapping.copyAndUpdate(TextureSlot.FRONT, TextureMapping.getBlockTexture(beeNestBlock, "_front_honey"));
        ResourceLocation resourcelocation = ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM.create(beeNestBlock, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM.createWithSuffix(beeNestBlock, "_honey", texturemapping1, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(beeNestBlock)
                    .with(createHorizontalFacingDispatch())
                    .with(createEmptyOrFullDispatch(BlockStateProperties.LEVEL_HONEY, 5, resourcelocation1, resourcelocation))
            );
    }

    private void createCropBlock(Block cropBlock, Property<Integer> ageProperty, int... ageToVisualStageMapping) {
        if (ageProperty.getPossibleValues().size() != ageToVisualStageMapping.length) {
            throw new IllegalArgumentException();
        } else {
            Int2ObjectMap<ResourceLocation> int2objectmap = new Int2ObjectOpenHashMap<>();
            PropertyDispatch propertydispatch = PropertyDispatch.property(ageProperty)
                .generate(
                    p_176172_ -> {
                        int i = ageToVisualStageMapping[p_176172_];
                        ResourceLocation resourcelocation = int2objectmap.computeIfAbsent(
                            i, p_176098_ -> this.createSuffixedVariant(cropBlock, "_stage" + i, ModelTemplates.CROP, TextureMapping::crop)
                        );
                        return Variant.variant().with(VariantProperties.MODEL, resourcelocation);
                    }
                );
            this.createSimpleFlatItemModel(cropBlock.asItem());
            this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(cropBlock).with(propertydispatch));
        }
    }

    private void createBell() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.BELL, "_floor");
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.BELL, "_ceiling");
        ResourceLocation resourcelocation2 = ModelLocationUtils.getModelLocation(Blocks.BELL, "_wall");
        ResourceLocation resourcelocation3 = ModelLocationUtils.getModelLocation(Blocks.BELL, "_between_walls");
        this.createSimpleFlatItemModel(Items.BELL);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.BELL)
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.BELL_ATTACHMENT)
                            .select(Direction.NORTH, BellAttachType.FLOOR, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                            .select(
                                Direction.SOUTH,
                                BellAttachType.FLOOR,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                Direction.EAST,
                                BellAttachType.FLOOR,
                                Variant.variant().with(VariantProperties.MODEL, resourcelocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                Direction.WEST,
                                BellAttachType.FLOOR,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(Direction.NORTH, BellAttachType.CEILING, Variant.variant().with(VariantProperties.MODEL, resourcelocation1))
                            .select(
                                Direction.SOUTH,
                                BellAttachType.CEILING,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation1)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                Direction.EAST,
                                BellAttachType.CEILING,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation1)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                Direction.WEST,
                                BellAttachType.CEILING,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation1)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                Direction.NORTH,
                                BellAttachType.SINGLE_WALL,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation2)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                Direction.SOUTH,
                                BellAttachType.SINGLE_WALL,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation2)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(Direction.EAST, BellAttachType.SINGLE_WALL, Variant.variant().with(VariantProperties.MODEL, resourcelocation2))
                            .select(
                                Direction.WEST,
                                BellAttachType.SINGLE_WALL,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation2)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                Direction.SOUTH,
                                BellAttachType.DOUBLE_WALL,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation3)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                Direction.NORTH,
                                BellAttachType.DOUBLE_WALL,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation3)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(Direction.EAST, BellAttachType.DOUBLE_WALL, Variant.variant().with(VariantProperties.MODEL, resourcelocation3))
                            .select(
                                Direction.WEST,
                                BellAttachType.DOUBLE_WALL,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation3)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                    )
            );
    }

    private void createGrindstone() {
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(
                        Blocks.GRINDSTONE, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.GRINDSTONE))
                    )
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.ATTACH_FACE, BlockStateProperties.HORIZONTAL_FACING)
                            .select(AttachFace.FLOOR, Direction.NORTH, Variant.variant())
                            .select(AttachFace.FLOOR, Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                            .select(AttachFace.FLOOR, Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                            .select(AttachFace.FLOOR, Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                            .select(AttachFace.WALL, Direction.NORTH, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
                            .select(
                                AttachFace.WALL,
                                Direction.EAST,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                AttachFace.WALL,
                                Direction.SOUTH,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                AttachFace.WALL,
                                Direction.WEST,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(AttachFace.CEILING, Direction.SOUTH, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R180))
                            .select(
                                AttachFace.CEILING,
                                Direction.WEST,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                AttachFace.CEILING,
                                Direction.NORTH,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                AttachFace.CEILING,
                                Direction.EAST,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                    )
            );
    }

    private void createFurnace(Block furnaceBlock, TexturedModel.Provider modelProvider) {
        ResourceLocation resourcelocation = modelProvider.create(furnaceBlock, this.modelOutput);
        ResourceLocation resourcelocation1 = TextureMapping.getBlockTexture(furnaceBlock, "_front_on");
        ResourceLocation resourcelocation2 = modelProvider.get(furnaceBlock)
            .updateTextures(p_176207_ -> p_176207_.put(TextureSlot.FRONT, resourcelocation1))
            .createWithSuffix(furnaceBlock, "_on", this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(furnaceBlock)
                    .with(createBooleanModelDispatch(BlockStateProperties.LIT, resourcelocation2, resourcelocation))
                    .with(createHorizontalFacingDispatch())
            );
    }

    private void createCampfires(Block... campfireBlocks) {
        ResourceLocation resourcelocation = ModelLocationUtils.decorateBlockModelLocation("campfire_off");

        for (Block block : campfireBlocks) {
            ResourceLocation resourcelocation1 = ModelTemplates.CAMPFIRE.create(block, TextureMapping.campfire(block), this.modelOutput);
            this.createSimpleFlatItemModel(block.asItem());
            this.blockStateOutput
                .accept(
                    MultiVariantGenerator.multiVariant(block)
                        .with(createBooleanModelDispatch(BlockStateProperties.LIT, resourcelocation1, resourcelocation))
                        .with(createHorizontalFacingDispatchAlt())
                );
        }
    }

    private void createAzalea(Block azaleaBlock) {
        ResourceLocation resourcelocation = ModelTemplates.AZALEA.create(azaleaBlock, TextureMapping.cubeTop(azaleaBlock), this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(azaleaBlock, resourcelocation));
    }

    private void createPottedAzalea(Block pottedAzaleaBlock) {
        ResourceLocation resourcelocation;
        if (pottedAzaleaBlock == Blocks.POTTED_FLOWERING_AZALEA) {
            resourcelocation = ModelTemplates.POTTED_FLOWERING_AZALEA.create(pottedAzaleaBlock, TextureMapping.pottedAzalea(pottedAzaleaBlock), this.modelOutput);
        } else {
            resourcelocation = ModelTemplates.POTTED_AZALEA.create(pottedAzaleaBlock, TextureMapping.pottedAzalea(pottedAzaleaBlock), this.modelOutput);
        }

        this.blockStateOutput.accept(createSimpleBlock(pottedAzaleaBlock, resourcelocation));
    }

    private void createBookshelf() {
        TextureMapping texturemapping = TextureMapping.column(
            TextureMapping.getBlockTexture(Blocks.BOOKSHELF), TextureMapping.getBlockTexture(Blocks.OAK_PLANKS)
        );
        ResourceLocation resourcelocation = ModelTemplates.CUBE_COLUMN.create(Blocks.BOOKSHELF, texturemapping, this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(Blocks.BOOKSHELF, resourcelocation));
    }

    private void createRedstoneWire() {
        this.createSimpleFlatItemModel(Items.REDSTONE);
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(Blocks.REDSTONE_WIRE)
                    .with(
                        Condition.or(
                            Condition.condition()
                                .term(BlockStateProperties.NORTH_REDSTONE, RedstoneSide.NONE)
                                .term(BlockStateProperties.EAST_REDSTONE, RedstoneSide.NONE)
                                .term(BlockStateProperties.SOUTH_REDSTONE, RedstoneSide.NONE)
                                .term(BlockStateProperties.WEST_REDSTONE, RedstoneSide.NONE),
                            Condition.condition()
                                .term(BlockStateProperties.NORTH_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP)
                                .term(BlockStateProperties.EAST_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
                            Condition.condition()
                                .term(BlockStateProperties.EAST_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP)
                                .term(BlockStateProperties.SOUTH_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
                            Condition.condition()
                                .term(BlockStateProperties.SOUTH_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP)
                                .term(BlockStateProperties.WEST_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
                            Condition.condition()
                                .term(BlockStateProperties.WEST_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP)
                                .term(BlockStateProperties.NORTH_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP)
                        ),
                        Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_dot"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.NORTH_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
                        Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_side0"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.SOUTH_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
                        Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_side_alt0"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.EAST_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
                        Variant.variant()
                            .with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_side_alt1"))
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.WEST_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
                        Variant.variant()
                            .with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_side1"))
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.NORTH_REDSTONE, RedstoneSide.UP),
                        Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_up"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.EAST_REDSTONE, RedstoneSide.UP),
                        Variant.variant()
                            .with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_up"))
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.SOUTH_REDSTONE, RedstoneSide.UP),
                        Variant.variant()
                            .with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_up"))
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.WEST_REDSTONE, RedstoneSide.UP),
                        Variant.variant()
                            .with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_up"))
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
            );
    }

    private void createComparator() {
        this.createSimpleFlatItemModel(Items.COMPARATOR);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.COMPARATOR)
                    .with(createHorizontalFacingDispatchAlt())
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.MODE_COMPARATOR, BlockStateProperties.POWERED)
                            .select(
                                ComparatorMode.COMPARE,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.COMPARATOR))
                            )
                            .select(
                                ComparatorMode.COMPARE,
                                true,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.COMPARATOR, "_on"))
                            )
                            .select(
                                ComparatorMode.SUBTRACT,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.COMPARATOR, "_subtract"))
                            )
                            .select(
                                ComparatorMode.SUBTRACT,
                                true,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.COMPARATOR, "_on_subtract"))
                            )
                    )
            );
    }

    private void createSmoothStoneSlab() {
        TextureMapping texturemapping = TextureMapping.cube(Blocks.SMOOTH_STONE);
        TextureMapping texturemapping1 = TextureMapping.column(
            TextureMapping.getBlockTexture(Blocks.SMOOTH_STONE_SLAB, "_side"), texturemapping.get(TextureSlot.TOP)
        );
        ResourceLocation resourcelocation = ModelTemplates.SLAB_BOTTOM.create(Blocks.SMOOTH_STONE_SLAB, texturemapping1, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.SLAB_TOP.create(Blocks.SMOOTH_STONE_SLAB, texturemapping1, this.modelOutput);
        ResourceLocation resourcelocation2 = ModelTemplates.CUBE_COLUMN
            .createWithOverride(Blocks.SMOOTH_STONE_SLAB, "_double", texturemapping1, this.modelOutput);
        this.blockStateOutput.accept(createSlab(Blocks.SMOOTH_STONE_SLAB, resourcelocation, resourcelocation1, resourcelocation2));
        this.blockStateOutput
            .accept(createSimpleBlock(Blocks.SMOOTH_STONE, ModelTemplates.CUBE_ALL.create(Blocks.SMOOTH_STONE, texturemapping, this.modelOutput)));
    }

    private void createBrewingStand() {
        this.createSimpleFlatItemModel(Items.BREWING_STAND);
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(Blocks.BREWING_STAND)
                    .with(Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.BREWING_STAND)))
                    .with(
                        Condition.condition().term(BlockStateProperties.HAS_BOTTLE_0, true),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.BREWING_STAND, "_bottle0"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.HAS_BOTTLE_1, true),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.BREWING_STAND, "_bottle1"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.HAS_BOTTLE_2, true),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.BREWING_STAND, "_bottle2"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.HAS_BOTTLE_0, false),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.BREWING_STAND, "_empty0"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.HAS_BOTTLE_1, false),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.BREWING_STAND, "_empty1"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.HAS_BOTTLE_2, false),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.BREWING_STAND, "_empty2"))
                    )
            );
    }

    private void createMushroomBlock(Block mushroomBlock) {
        ResourceLocation resourcelocation = ModelTemplates.SINGLE_FACE.create(mushroomBlock, TextureMapping.defaultTexture(mushroomBlock), this.modelOutput);
        ResourceLocation resourcelocation1 = ModelLocationUtils.decorateBlockModelLocation("mushroom_block_inside");
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(mushroomBlock)
                    .with(Condition.condition().term(BlockStateProperties.NORTH, true), Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                    .with(
                        Condition.condition().term(BlockStateProperties.EAST, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.SOUTH, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.WEST, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.UP, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.DOWN, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(Condition.condition().term(BlockStateProperties.NORTH, false), Variant.variant().with(VariantProperties.MODEL, resourcelocation1))
                    .with(
                        Condition.condition().term(BlockStateProperties.EAST, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation1)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, false)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.SOUTH, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation1)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, false)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.WEST, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation1)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, false)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.UP, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation1)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, false)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.DOWN, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation1)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, false)
                    )
            );
        this.delegateItemModel(mushroomBlock, TexturedModel.CUBE.createWithSuffix(mushroomBlock, "_inventory", this.modelOutput));
    }

    private void createCakeBlock() {
        this.createSimpleFlatItemModel(Items.CAKE);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.CAKE)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.BITES)
                            .select(0, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAKE)))
                            .select(1, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAKE, "_slice1")))
                            .select(2, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAKE, "_slice2")))
                            .select(3, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAKE, "_slice3")))
                            .select(4, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAKE, "_slice4")))
                            .select(5, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAKE, "_slice5")))
                            .select(6, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAKE, "_slice6")))
                    )
            );
    }

    private void createCartographyTable() {
        TextureMapping texturemapping = new TextureMapping()
            .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(Blocks.CARTOGRAPHY_TABLE, "_side3"))
            .put(TextureSlot.DOWN, TextureMapping.getBlockTexture(Blocks.DARK_OAK_PLANKS))
            .put(TextureSlot.UP, TextureMapping.getBlockTexture(Blocks.CARTOGRAPHY_TABLE, "_top"))
            .put(TextureSlot.NORTH, TextureMapping.getBlockTexture(Blocks.CARTOGRAPHY_TABLE, "_side3"))
            .put(TextureSlot.EAST, TextureMapping.getBlockTexture(Blocks.CARTOGRAPHY_TABLE, "_side3"))
            .put(TextureSlot.SOUTH, TextureMapping.getBlockTexture(Blocks.CARTOGRAPHY_TABLE, "_side1"))
            .put(TextureSlot.WEST, TextureMapping.getBlockTexture(Blocks.CARTOGRAPHY_TABLE, "_side2"));
        this.blockStateOutput
            .accept(createSimpleBlock(Blocks.CARTOGRAPHY_TABLE, ModelTemplates.CUBE.create(Blocks.CARTOGRAPHY_TABLE, texturemapping, this.modelOutput)));
    }

    private void createSmithingTable() {
        TextureMapping texturemapping = new TextureMapping()
            .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_front"))
            .put(TextureSlot.DOWN, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_bottom"))
            .put(TextureSlot.UP, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_top"))
            .put(TextureSlot.NORTH, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_front"))
            .put(TextureSlot.SOUTH, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_front"))
            .put(TextureSlot.EAST, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_side"))
            .put(TextureSlot.WEST, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_side"));
        this.blockStateOutput
            .accept(createSimpleBlock(Blocks.SMITHING_TABLE, ModelTemplates.CUBE.create(Blocks.SMITHING_TABLE, texturemapping, this.modelOutput)));
    }

    private void createCraftingTableLike(Block craftingTableBlock, Block craftingTableMaterialBlock, BiFunction<Block, Block, TextureMapping> textureMappingGetter) {
        TextureMapping texturemapping = textureMappingGetter.apply(craftingTableBlock, craftingTableMaterialBlock);
        this.blockStateOutput.accept(createSimpleBlock(craftingTableBlock, ModelTemplates.CUBE.create(craftingTableBlock, texturemapping, this.modelOutput)));
    }

    public void createGenericCube(Block block) {
        TextureMapping texturemapping = new TextureMapping()
            .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(block, "_particle"))
            .put(TextureSlot.DOWN, TextureMapping.getBlockTexture(block, "_down"))
            .put(TextureSlot.UP, TextureMapping.getBlockTexture(block, "_up"))
            .put(TextureSlot.NORTH, TextureMapping.getBlockTexture(block, "_north"))
            .put(TextureSlot.SOUTH, TextureMapping.getBlockTexture(block, "_south"))
            .put(TextureSlot.EAST, TextureMapping.getBlockTexture(block, "_east"))
            .put(TextureSlot.WEST, TextureMapping.getBlockTexture(block, "_west"));
        this.blockStateOutput.accept(createSimpleBlock(block, ModelTemplates.CUBE.create(block, texturemapping, this.modelOutput)));
    }

    private void createPumpkins() {
        TextureMapping texturemapping = TextureMapping.column(Blocks.PUMPKIN);
        this.blockStateOutput.accept(createSimpleBlock(Blocks.PUMPKIN, ModelLocationUtils.getModelLocation(Blocks.PUMPKIN)));
        this.createPumpkinVariant(Blocks.CARVED_PUMPKIN, texturemapping);
        this.createPumpkinVariant(Blocks.JACK_O_LANTERN, texturemapping);
    }

    private void createPumpkinVariant(Block pumpkinBlock, TextureMapping columnTextureMapping) {
        ResourceLocation resourcelocation = ModelTemplates.CUBE_ORIENTABLE
            .create(pumpkinBlock, columnTextureMapping.copyAndUpdate(TextureSlot.FRONT, TextureMapping.getBlockTexture(pumpkinBlock)), this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(pumpkinBlock, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                    .with(createHorizontalFacingDispatch())
            );
    }

    private void createCauldrons() {
        this.createSimpleFlatItemModel(Items.CAULDRON);
        this.createNonTemplateModelBlock(Blocks.CAULDRON);
        this.blockStateOutput
            .accept(
                createSimpleBlock(
                    Blocks.LAVA_CAULDRON,
                    ModelTemplates.CAULDRON_FULL
                        .create(Blocks.LAVA_CAULDRON, TextureMapping.cauldron(TextureMapping.getBlockTexture(Blocks.LAVA, "_still")), this.modelOutput)
                )
            );
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.WATER_CAULDRON)
                    .with(
                        PropertyDispatch.property(LayeredCauldronBlock.LEVEL)
                            .select(
                                1,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        ModelTemplates.CAULDRON_LEVEL1
                                            .createWithSuffix(
                                                Blocks.WATER_CAULDRON,
                                                "_level1",
                                                TextureMapping.cauldron(TextureMapping.getBlockTexture(Blocks.WATER, "_still")),
                                                this.modelOutput
                                            )
                                    )
                            )
                            .select(
                                2,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        ModelTemplates.CAULDRON_LEVEL2
                                            .createWithSuffix(
                                                Blocks.WATER_CAULDRON,
                                                "_level2",
                                                TextureMapping.cauldron(TextureMapping.getBlockTexture(Blocks.WATER, "_still")),
                                                this.modelOutput
                                            )
                                    )
                            )
                            .select(
                                3,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        ModelTemplates.CAULDRON_FULL
                                            .createWithSuffix(
                                                Blocks.WATER_CAULDRON,
                                                "_full",
                                                TextureMapping.cauldron(TextureMapping.getBlockTexture(Blocks.WATER, "_still")),
                                                this.modelOutput
                                            )
                                    )
                            )
                    )
            );
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.POWDER_SNOW_CAULDRON)
                    .with(
                        PropertyDispatch.property(LayeredCauldronBlock.LEVEL)
                            .select(
                                1,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        ModelTemplates.CAULDRON_LEVEL1
                                            .createWithSuffix(
                                                Blocks.POWDER_SNOW_CAULDRON,
                                                "_level1",
                                                TextureMapping.cauldron(TextureMapping.getBlockTexture(Blocks.POWDER_SNOW)),
                                                this.modelOutput
                                            )
                                    )
                            )
                            .select(
                                2,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        ModelTemplates.CAULDRON_LEVEL2
                                            .createWithSuffix(
                                                Blocks.POWDER_SNOW_CAULDRON,
                                                "_level2",
                                                TextureMapping.cauldron(TextureMapping.getBlockTexture(Blocks.POWDER_SNOW)),
                                                this.modelOutput
                                            )
                                    )
                            )
                            .select(
                                3,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        ModelTemplates.CAULDRON_FULL
                                            .createWithSuffix(
                                                Blocks.POWDER_SNOW_CAULDRON,
                                                "_full",
                                                TextureMapping.cauldron(TextureMapping.getBlockTexture(Blocks.POWDER_SNOW)),
                                                this.modelOutput
                                            )
                                    )
                            )
                    )
            );
    }

    private void createChorusFlower() {
        TextureMapping texturemapping = TextureMapping.defaultTexture(Blocks.CHORUS_FLOWER);
        ResourceLocation resourcelocation = ModelTemplates.CHORUS_FLOWER.create(Blocks.CHORUS_FLOWER, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = this.createSuffixedVariant(
            Blocks.CHORUS_FLOWER, "_dead", ModelTemplates.CHORUS_FLOWER, p_176148_ -> texturemapping.copyAndUpdate(TextureSlot.TEXTURE, p_176148_)
        );
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.CHORUS_FLOWER)
                    .with(createEmptyOrFullDispatch(BlockStateProperties.AGE_5, 5, resourcelocation1, resourcelocation))
            );
    }

    private void createCrafterBlock() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.CRAFTER);
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.CRAFTER, "_triggered");
        ResourceLocation resourcelocation2 = ModelLocationUtils.getModelLocation(Blocks.CRAFTER, "_crafting");
        ResourceLocation resourcelocation3 = ModelLocationUtils.getModelLocation(Blocks.CRAFTER, "_crafting_triggered");
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.CRAFTER)
                    .with(PropertyDispatch.property(BlockStateProperties.ORIENTATION).generate(p_236301_ -> this.applyRotation(p_236301_, Variant.variant())))
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.TRIGGERED, CrafterBlock.CRAFTING)
                            .select(false, false, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                            .select(true, true, Variant.variant().with(VariantProperties.MODEL, resourcelocation3))
                            .select(true, false, Variant.variant().with(VariantProperties.MODEL, resourcelocation1))
                            .select(false, true, Variant.variant().with(VariantProperties.MODEL, resourcelocation2))
                    )
            );
    }

    private void createDispenserBlock(Block dispenserBlock) {
        TextureMapping texturemapping = new TextureMapping()
            .put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.FURNACE, "_top"))
            .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.FURNACE, "_side"))
            .put(TextureSlot.FRONT, TextureMapping.getBlockTexture(dispenserBlock, "_front"));
        TextureMapping texturemapping1 = new TextureMapping()
            .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.FURNACE, "_top"))
            .put(TextureSlot.FRONT, TextureMapping.getBlockTexture(dispenserBlock, "_front_vertical"));
        ResourceLocation resourcelocation = ModelTemplates.CUBE_ORIENTABLE.create(dispenserBlock, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.CUBE_ORIENTABLE_VERTICAL.create(dispenserBlock, texturemapping1, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(dispenserBlock)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.FACING)
                            .select(
                                Direction.DOWN,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation1)
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(Direction.UP, Variant.variant().with(VariantProperties.MODEL, resourcelocation1))
                            .select(Direction.NORTH, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                            .select(
                                Direction.EAST,
                                Variant.variant().with(VariantProperties.MODEL, resourcelocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                Direction.SOUTH,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                Direction.WEST,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                    )
            );
    }

    private void createEndPortalFrame() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.END_PORTAL_FRAME);
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.END_PORTAL_FRAME, "_filled");
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.END_PORTAL_FRAME)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.EYE)
                            .select(false, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                            .select(true, Variant.variant().with(VariantProperties.MODEL, resourcelocation1))
                    )
                    .with(createHorizontalFacingDispatchAlt())
            );
    }

    private void createChorusPlant() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.CHORUS_PLANT, "_side");
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.CHORUS_PLANT, "_noside");
        ResourceLocation resourcelocation2 = ModelLocationUtils.getModelLocation(Blocks.CHORUS_PLANT, "_noside1");
        ResourceLocation resourcelocation3 = ModelLocationUtils.getModelLocation(Blocks.CHORUS_PLANT, "_noside2");
        ResourceLocation resourcelocation4 = ModelLocationUtils.getModelLocation(Blocks.CHORUS_PLANT, "_noside3");
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(Blocks.CHORUS_PLANT)
                    .with(Condition.condition().term(BlockStateProperties.NORTH, true), Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                    .with(
                        Condition.condition().term(BlockStateProperties.EAST, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.SOUTH, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.WEST, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.UP, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.DOWN, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.NORTH, false),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation1).with(VariantProperties.WEIGHT, 2),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation2),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation3),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation4)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.EAST, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation2)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation3)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation4)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation1)
                            .with(VariantProperties.WEIGHT, 2)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.SOUTH, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation3)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation4)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation1)
                            .with(VariantProperties.WEIGHT, 2)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation2)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.WEST, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation4)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation1)
                            .with(VariantProperties.WEIGHT, 2)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation2)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation3)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.UP, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation1)
                            .with(VariantProperties.WEIGHT, 2)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation4)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation2)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation3)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.DOWN, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation4)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation3)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation2)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation1)
                            .with(VariantProperties.WEIGHT, 2)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
            );
    }

    private void createComposter() {
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(Blocks.COMPOSTER)
                    .with(Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER)))
                    .with(
                        Condition.condition().term(BlockStateProperties.LEVEL_COMPOSTER, 1),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents1"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.LEVEL_COMPOSTER, 2),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents2"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.LEVEL_COMPOSTER, 3),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents3"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.LEVEL_COMPOSTER, 4),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents4"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.LEVEL_COMPOSTER, 5),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents5"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.LEVEL_COMPOSTER, 6),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents6"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.LEVEL_COMPOSTER, 7),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents7"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.LEVEL_COMPOSTER, 8),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents_ready"))
                    )
            );
    }

    private void createCopperBulb(Block bulbBlock) {
        ResourceLocation resourcelocation = ModelTemplates.CUBE_ALL.create(bulbBlock, TextureMapping.cube(bulbBlock), this.modelOutput);
        ResourceLocation resourcelocation1 = this.createSuffixedVariant(bulbBlock, "_powered", ModelTemplates.CUBE_ALL, TextureMapping::cube);
        ResourceLocation resourcelocation2 = this.createSuffixedVariant(bulbBlock, "_lit", ModelTemplates.CUBE_ALL, TextureMapping::cube);
        ResourceLocation resourcelocation3 = this.createSuffixedVariant(bulbBlock, "_lit_powered", ModelTemplates.CUBE_ALL, TextureMapping::cube);
        this.blockStateOutput.accept(this.createCopperBulb(bulbBlock, resourcelocation, resourcelocation2, resourcelocation1, resourcelocation3));
    }

    private BlockStateGenerator createCopperBulb(
        Block bulbBlock, ResourceLocation unlit, ResourceLocation unlitPowered, ResourceLocation lit, ResourceLocation litPowered
    ) {
        return MultiVariantGenerator.multiVariant(bulbBlock)
            .with(
                PropertyDispatch.properties(BlockStateProperties.LIT, BlockStateProperties.POWERED)
                    .generate(
                        (p_308471_, p_308472_) -> p_308471_
                                ? Variant.variant().with(VariantProperties.MODEL, p_308472_ ? litPowered : unlitPowered)
                                : Variant.variant().with(VariantProperties.MODEL, p_308472_ ? lit : unlit)
                    )
            );
    }

    private void copyCopperBulbModel(Block bulbBlock, Block sourceBlock) {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(bulbBlock);
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(bulbBlock, "_powered");
        ResourceLocation resourcelocation2 = ModelLocationUtils.getModelLocation(bulbBlock, "_lit");
        ResourceLocation resourcelocation3 = ModelLocationUtils.getModelLocation(bulbBlock, "_lit_powered");
        this.delegateItemModel(sourceBlock, ModelLocationUtils.getModelLocation(bulbBlock.asItem()));
        this.blockStateOutput.accept(this.createCopperBulb(sourceBlock, resourcelocation, resourcelocation2, resourcelocation1, resourcelocation3));
    }

    private void createAmethystCluster(Block amethystBlock) {
        this.skipAutoItemBlock(amethystBlock);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(
                        amethystBlock,
                        Variant.variant()
                            .with(VariantProperties.MODEL, ModelTemplates.CROSS.create(amethystBlock, TextureMapping.cross(amethystBlock), this.modelOutput))
                    )
                    .with(this.createColumnWithFacing())
            );
    }

    private void createAmethystClusters() {
        this.createAmethystCluster(Blocks.SMALL_AMETHYST_BUD);
        this.createAmethystCluster(Blocks.MEDIUM_AMETHYST_BUD);
        this.createAmethystCluster(Blocks.LARGE_AMETHYST_BUD);
        this.createAmethystCluster(Blocks.AMETHYST_CLUSTER);
    }

    private void createPointedDripstone() {
        this.skipAutoItemBlock(Blocks.POINTED_DRIPSTONE);
        PropertyDispatch.C2<Direction, DripstoneThickness> c2 = PropertyDispatch.properties(
            BlockStateProperties.VERTICAL_DIRECTION, BlockStateProperties.DRIPSTONE_THICKNESS
        );

        for (DripstoneThickness dripstonethickness : DripstoneThickness.values()) {
            c2.select(Direction.UP, dripstonethickness, this.createPointedDripstoneVariant(Direction.UP, dripstonethickness));
        }

        for (DripstoneThickness dripstonethickness1 : DripstoneThickness.values()) {
            c2.select(Direction.DOWN, dripstonethickness1, this.createPointedDripstoneVariant(Direction.DOWN, dripstonethickness1));
        }

        this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(Blocks.POINTED_DRIPSTONE).with(c2));
    }

    private Variant createPointedDripstoneVariant(Direction direction, DripstoneThickness dripstoneThickness) {
        String s = "_" + direction.getSerializedName() + "_" + dripstoneThickness.getSerializedName();
        TextureMapping texturemapping = TextureMapping.cross(TextureMapping.getBlockTexture(Blocks.POINTED_DRIPSTONE, s));
        return Variant.variant()
            .with(VariantProperties.MODEL, ModelTemplates.POINTED_DRIPSTONE.createWithSuffix(Blocks.POINTED_DRIPSTONE, s, texturemapping, this.modelOutput));
    }

    private void createNyliumBlock(Block nyliumBlock) {
        TextureMapping texturemapping = new TextureMapping()
            .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(Blocks.NETHERRACK))
            .put(TextureSlot.TOP, TextureMapping.getBlockTexture(nyliumBlock))
            .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(nyliumBlock, "_side"));
        this.blockStateOutput.accept(createSimpleBlock(nyliumBlock, ModelTemplates.CUBE_BOTTOM_TOP.create(nyliumBlock, texturemapping, this.modelOutput)));
    }

    private void createDaylightDetector() {
        ResourceLocation resourcelocation = TextureMapping.getBlockTexture(Blocks.DAYLIGHT_DETECTOR, "_side");
        TextureMapping texturemapping = new TextureMapping()
            .put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.DAYLIGHT_DETECTOR, "_top"))
            .put(TextureSlot.SIDE, resourcelocation);
        TextureMapping texturemapping1 = new TextureMapping()
            .put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.DAYLIGHT_DETECTOR, "_inverted_top"))
            .put(TextureSlot.SIDE, resourcelocation);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.DAYLIGHT_DETECTOR)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.INVERTED)
                            .select(
                                false,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        ModelTemplates.DAYLIGHT_DETECTOR.create(Blocks.DAYLIGHT_DETECTOR, texturemapping, this.modelOutput)
                                    )
                            )
                            .select(
                                true,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        ModelTemplates.DAYLIGHT_DETECTOR
                                            .create(
                                                ModelLocationUtils.getModelLocation(Blocks.DAYLIGHT_DETECTOR, "_inverted"), texturemapping1, this.modelOutput
                                            )
                                    )
                            )
                    )
            );
    }

    private void createRotatableColumn(Block rotatableColumnBlock) {
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(rotatableColumnBlock, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(rotatableColumnBlock)))
                    .with(this.createColumnWithFacing())
            );
    }

    private void createLightningRod() {
        Block block = Blocks.LIGHTNING_ROD;
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(block, "_on");
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(block);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(block, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(block)))
                    .with(this.createColumnWithFacing())
                    .with(createBooleanModelDispatch(BlockStateProperties.POWERED, resourcelocation, resourcelocation1))
            );
    }

    private void createFarmland() {
        TextureMapping texturemapping = new TextureMapping()
            .put(TextureSlot.DIRT, TextureMapping.getBlockTexture(Blocks.DIRT))
            .put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.FARMLAND));
        TextureMapping texturemapping1 = new TextureMapping()
            .put(TextureSlot.DIRT, TextureMapping.getBlockTexture(Blocks.DIRT))
            .put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.FARMLAND, "_moist"));
        ResourceLocation resourcelocation = ModelTemplates.FARMLAND.create(Blocks.FARMLAND, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.FARMLAND
            .create(TextureMapping.getBlockTexture(Blocks.FARMLAND, "_moist"), texturemapping1, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.FARMLAND)
                    .with(createEmptyOrFullDispatch(BlockStateProperties.MOISTURE, 7, resourcelocation1, resourcelocation))
            );
    }

    private List<ResourceLocation> createFloorFireModels(Block fireBlock) {
        ResourceLocation resourcelocation = ModelTemplates.FIRE_FLOOR
            .create(ModelLocationUtils.getModelLocation(fireBlock, "_floor0"), TextureMapping.fire0(fireBlock), this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.FIRE_FLOOR
            .create(ModelLocationUtils.getModelLocation(fireBlock, "_floor1"), TextureMapping.fire1(fireBlock), this.modelOutput);
        return ImmutableList.of(resourcelocation, resourcelocation1);
    }

    private List<ResourceLocation> createSideFireModels(Block fireBlock) {
        ResourceLocation resourcelocation = ModelTemplates.FIRE_SIDE
            .create(ModelLocationUtils.getModelLocation(fireBlock, "_side0"), TextureMapping.fire0(fireBlock), this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.FIRE_SIDE
            .create(ModelLocationUtils.getModelLocation(fireBlock, "_side1"), TextureMapping.fire1(fireBlock), this.modelOutput);
        ResourceLocation resourcelocation2 = ModelTemplates.FIRE_SIDE_ALT
            .create(ModelLocationUtils.getModelLocation(fireBlock, "_side_alt0"), TextureMapping.fire0(fireBlock), this.modelOutput);
        ResourceLocation resourcelocation3 = ModelTemplates.FIRE_SIDE_ALT
            .create(ModelLocationUtils.getModelLocation(fireBlock, "_side_alt1"), TextureMapping.fire1(fireBlock), this.modelOutput);
        return ImmutableList.of(resourcelocation, resourcelocation1, resourcelocation2, resourcelocation3);
    }

    private List<ResourceLocation> createTopFireModels(Block fireBlock) {
        ResourceLocation resourcelocation = ModelTemplates.FIRE_UP
            .create(ModelLocationUtils.getModelLocation(fireBlock, "_up0"), TextureMapping.fire0(fireBlock), this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.FIRE_UP
            .create(ModelLocationUtils.getModelLocation(fireBlock, "_up1"), TextureMapping.fire1(fireBlock), this.modelOutput);
        ResourceLocation resourcelocation2 = ModelTemplates.FIRE_UP_ALT
            .create(ModelLocationUtils.getModelLocation(fireBlock, "_up_alt0"), TextureMapping.fire0(fireBlock), this.modelOutput);
        ResourceLocation resourcelocation3 = ModelTemplates.FIRE_UP_ALT
            .create(ModelLocationUtils.getModelLocation(fireBlock, "_up_alt1"), TextureMapping.fire1(fireBlock), this.modelOutput);
        return ImmutableList.of(resourcelocation, resourcelocation1, resourcelocation2, resourcelocation3);
    }

    private static List<Variant> wrapModels(List<ResourceLocation> modelLocations, UnaryOperator<Variant> variantMapper) {
        return modelLocations.stream().map(p_176238_ -> Variant.variant().with(VariantProperties.MODEL, p_176238_)).map(variantMapper).collect(Collectors.toList());
    }

    private void createFire() {
        Condition condition = Condition.condition()
            .term(BlockStateProperties.NORTH, false)
            .term(BlockStateProperties.EAST, false)
            .term(BlockStateProperties.SOUTH, false)
            .term(BlockStateProperties.WEST, false)
            .term(BlockStateProperties.UP, false);
        List<ResourceLocation> list = this.createFloorFireModels(Blocks.FIRE);
        List<ResourceLocation> list1 = this.createSideFireModels(Blocks.FIRE);
        List<ResourceLocation> list2 = this.createTopFireModels(Blocks.FIRE);
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(Blocks.FIRE)
                    .with(condition, wrapModels(list, p_124894_ -> p_124894_))
                    .with(Condition.or(Condition.condition().term(BlockStateProperties.NORTH, true), condition), wrapModels(list1, p_176243_ -> p_176243_))
                    .with(
                        Condition.or(Condition.condition().term(BlockStateProperties.EAST, true), condition),
                        wrapModels(list1, p_176240_ -> p_176240_.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                    )
                    .with(
                        Condition.or(Condition.condition().term(BlockStateProperties.SOUTH, true), condition),
                        wrapModels(list1, p_176236_ -> p_176236_.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                    )
                    .with(
                        Condition.or(Condition.condition().term(BlockStateProperties.WEST, true), condition),
                        wrapModels(list1, p_176232_ -> p_176232_.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                    )
                    .with(Condition.condition().term(BlockStateProperties.UP, true), wrapModels(list2, p_176227_ -> p_176227_))
            );
    }

    private void createSoulFire() {
        List<ResourceLocation> list = this.createFloorFireModels(Blocks.SOUL_FIRE);
        List<ResourceLocation> list1 = this.createSideFireModels(Blocks.SOUL_FIRE);
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(Blocks.SOUL_FIRE)
                    .with(wrapModels(list, p_176221_ -> p_176221_))
                    .with(wrapModels(list1, p_176209_ -> p_176209_))
                    .with(wrapModels(list1, p_176200_ -> p_176200_.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)))
                    .with(wrapModels(list1, p_176188_ -> p_176188_.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)))
                    .with(wrapModels(list1, p_176143_ -> p_176143_.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)))
            );
    }

    private void createLantern(Block lanternBlock) {
        ResourceLocation resourcelocation = TexturedModel.LANTERN.create(lanternBlock, this.modelOutput);
        ResourceLocation resourcelocation1 = TexturedModel.HANGING_LANTERN.create(lanternBlock, this.modelOutput);
        this.createSimpleFlatItemModel(lanternBlock.asItem());
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(lanternBlock)
                    .with(createBooleanModelDispatch(BlockStateProperties.HANGING, resourcelocation1, resourcelocation))
            );
    }

    private void createMuddyMangroveRoots() {
        TextureMapping texturemapping = TextureMapping.column(
            TextureMapping.getBlockTexture(Blocks.MUDDY_MANGROVE_ROOTS, "_side"), TextureMapping.getBlockTexture(Blocks.MUDDY_MANGROVE_ROOTS, "_top")
        );
        ResourceLocation resourcelocation = ModelTemplates.CUBE_COLUMN.create(Blocks.MUDDY_MANGROVE_ROOTS, texturemapping, this.modelOutput);
        this.blockStateOutput.accept(createAxisAlignedPillarBlock(Blocks.MUDDY_MANGROVE_ROOTS, resourcelocation));
    }

    private void createMangrovePropagule() {
        this.createSimpleFlatItemModel(Items.MANGROVE_PROPAGULE);
        Block block = Blocks.MANGROVE_PROPAGULE;
        PropertyDispatch.C2<Boolean, Integer> c2 = PropertyDispatch.properties(MangrovePropaguleBlock.HANGING, MangrovePropaguleBlock.AGE);
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(block);

        for (int i = 0; i <= 4; i++) {
            ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(block, "_hanging_" + i);
            c2.select(true, i, Variant.variant().with(VariantProperties.MODEL, resourcelocation1));
            c2.select(false, i, Variant.variant().with(VariantProperties.MODEL, resourcelocation));
        }

        this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(Blocks.MANGROVE_PROPAGULE).with(c2));
    }

    private void createFrostedIce() {
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.FROSTED_ICE)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.AGE_3)
                            .select(
                                0,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        this.createSuffixedVariant(Blocks.FROSTED_ICE, "_0", ModelTemplates.CUBE_ALL, TextureMapping::cube)
                                    )
                            )
                            .select(
                                1,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        this.createSuffixedVariant(Blocks.FROSTED_ICE, "_1", ModelTemplates.CUBE_ALL, TextureMapping::cube)
                                    )
                            )
                            .select(
                                2,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        this.createSuffixedVariant(Blocks.FROSTED_ICE, "_2", ModelTemplates.CUBE_ALL, TextureMapping::cube)
                                    )
                            )
                            .select(
                                3,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        this.createSuffixedVariant(Blocks.FROSTED_ICE, "_3", ModelTemplates.CUBE_ALL, TextureMapping::cube)
                                    )
                            )
                    )
            );
    }

    private void createGrassBlocks() {
        ResourceLocation resourcelocation = TextureMapping.getBlockTexture(Blocks.DIRT);
        TextureMapping texturemapping = new TextureMapping()
            .put(TextureSlot.BOTTOM, resourcelocation)
            .copyForced(TextureSlot.BOTTOM, TextureSlot.PARTICLE)
            .put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.GRASS_BLOCK, "_top"))
            .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.GRASS_BLOCK, "_snow"));
        Variant variant = Variant.variant()
            .with(VariantProperties.MODEL, ModelTemplates.CUBE_BOTTOM_TOP.createWithSuffix(Blocks.GRASS_BLOCK, "_snow", texturemapping, this.modelOutput));
        this.createGrassLikeBlock(Blocks.GRASS_BLOCK, ModelLocationUtils.getModelLocation(Blocks.GRASS_BLOCK), variant);
        ResourceLocation resourcelocation1 = TexturedModel.CUBE_TOP_BOTTOM
            .get(Blocks.MYCELIUM)
            .updateTextures(p_176198_ -> p_176198_.put(TextureSlot.BOTTOM, resourcelocation))
            .create(Blocks.MYCELIUM, this.modelOutput);
        this.createGrassLikeBlock(Blocks.MYCELIUM, resourcelocation1, variant);
        ResourceLocation resourcelocation2 = TexturedModel.CUBE_TOP_BOTTOM
            .get(Blocks.PODZOL)
            .updateTextures(p_176154_ -> p_176154_.put(TextureSlot.BOTTOM, resourcelocation))
            .create(Blocks.PODZOL, this.modelOutput);
        this.createGrassLikeBlock(Blocks.PODZOL, resourcelocation2, variant);
    }

    private void createGrassLikeBlock(Block grassLikeBlock, ResourceLocation modelLocation, Variant variant) {
        List<Variant> list = Arrays.asList(createRotatedVariants(modelLocation));
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(grassLikeBlock)
                    .with(PropertyDispatch.property(BlockStateProperties.SNOWY).select(true, variant).select(false, list))
            );
    }

    private void createCocoa() {
        this.createSimpleFlatItemModel(Items.COCOA_BEANS);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.COCOA)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.AGE_2)
                            .select(0, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.COCOA, "_stage0")))
                            .select(1, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.COCOA, "_stage1")))
                            .select(2, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.COCOA, "_stage2")))
                    )
                    .with(createHorizontalFacingDispatchAlt())
            );
    }

    private void createDirtPath() {
        this.blockStateOutput.accept(createRotatedVariant(Blocks.DIRT_PATH, ModelLocationUtils.getModelLocation(Blocks.DIRT_PATH)));
    }

    private void createWeightedPressurePlate(Block pressurePlateBlock, Block plateMaterialBlock) {
        TextureMapping texturemapping = TextureMapping.defaultTexture(plateMaterialBlock);
        ResourceLocation resourcelocation = ModelTemplates.PRESSURE_PLATE_UP.create(pressurePlateBlock, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.PRESSURE_PLATE_DOWN.create(pressurePlateBlock, texturemapping, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(pressurePlateBlock)
                    .with(createEmptyOrFullDispatch(BlockStateProperties.POWER, 1, resourcelocation1, resourcelocation))
            );
    }

    private void createHopper() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.HOPPER);
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.HOPPER, "_side");
        this.createSimpleFlatItemModel(Items.HOPPER);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.HOPPER)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.FACING_HOPPER)
                            .select(Direction.DOWN, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                            .select(Direction.NORTH, Variant.variant().with(VariantProperties.MODEL, resourcelocation1))
                            .select(
                                Direction.EAST,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation1)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                Direction.SOUTH,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation1)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                Direction.WEST,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation1)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                    )
            );
    }

    private void copyModel(Block sourceBlock, Block targetBlock) {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(sourceBlock);
        this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(targetBlock, Variant.variant().with(VariantProperties.MODEL, resourcelocation)));
        this.delegateItemModel(targetBlock, resourcelocation);
    }

    private void createIronBars() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.IRON_BARS, "_post_ends");
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.IRON_BARS, "_post");
        ResourceLocation resourcelocation2 = ModelLocationUtils.getModelLocation(Blocks.IRON_BARS, "_cap");
        ResourceLocation resourcelocation3 = ModelLocationUtils.getModelLocation(Blocks.IRON_BARS, "_cap_alt");
        ResourceLocation resourcelocation4 = ModelLocationUtils.getModelLocation(Blocks.IRON_BARS, "_side");
        ResourceLocation resourcelocation5 = ModelLocationUtils.getModelLocation(Blocks.IRON_BARS, "_side_alt");
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(Blocks.IRON_BARS)
                    .with(Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                    .with(
                        Condition.condition()
                            .term(BlockStateProperties.NORTH, false)
                            .term(BlockStateProperties.EAST, false)
                            .term(BlockStateProperties.SOUTH, false)
                            .term(BlockStateProperties.WEST, false),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation1)
                    )
                    .with(
                        Condition.condition()
                            .term(BlockStateProperties.NORTH, true)
                            .term(BlockStateProperties.EAST, false)
                            .term(BlockStateProperties.SOUTH, false)
                            .term(BlockStateProperties.WEST, false),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation2)
                    )
                    .with(
                        Condition.condition()
                            .term(BlockStateProperties.NORTH, false)
                            .term(BlockStateProperties.EAST, true)
                            .term(BlockStateProperties.SOUTH, false)
                            .term(BlockStateProperties.WEST, false),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .with(
                        Condition.condition()
                            .term(BlockStateProperties.NORTH, false)
                            .term(BlockStateProperties.EAST, false)
                            .term(BlockStateProperties.SOUTH, true)
                            .term(BlockStateProperties.WEST, false),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation3)
                    )
                    .with(
                        Condition.condition()
                            .term(BlockStateProperties.NORTH, false)
                            .term(BlockStateProperties.EAST, false)
                            .term(BlockStateProperties.SOUTH, false)
                            .term(BlockStateProperties.WEST, true),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .with(Condition.condition().term(BlockStateProperties.NORTH, true), Variant.variant().with(VariantProperties.MODEL, resourcelocation4))
                    .with(
                        Condition.condition().term(BlockStateProperties.EAST, true),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation4).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .with(Condition.condition().term(BlockStateProperties.SOUTH, true), Variant.variant().with(VariantProperties.MODEL, resourcelocation5))
                    .with(
                        Condition.condition().term(BlockStateProperties.WEST, true),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation5).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
            );
        this.createSimpleFlatItemModel(Blocks.IRON_BARS);
    }

    private void createNonTemplateHorizontalBlock(Block horizontalBlock) {
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(horizontalBlock, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(horizontalBlock)))
                    .with(createHorizontalFacingDispatch())
            );
    }

    private void createLever() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.LEVER);
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.LEVER, "_on");
        this.createSimpleFlatItemModel(Blocks.LEVER);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.LEVER)
                    .with(createBooleanModelDispatch(BlockStateProperties.POWERED, resourcelocation, resourcelocation1))
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.ATTACH_FACE, BlockStateProperties.HORIZONTAL_FACING)
                            .select(
                                AttachFace.CEILING,
                                Direction.NORTH,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                AttachFace.CEILING,
                                Direction.EAST,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(AttachFace.CEILING, Direction.SOUTH, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R180))
                            .select(
                                AttachFace.CEILING,
                                Direction.WEST,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(AttachFace.FLOOR, Direction.NORTH, Variant.variant())
                            .select(AttachFace.FLOOR, Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                            .select(AttachFace.FLOOR, Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                            .select(AttachFace.FLOOR, Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                            .select(AttachFace.WALL, Direction.NORTH, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
                            .select(
                                AttachFace.WALL,
                                Direction.EAST,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                AttachFace.WALL,
                                Direction.SOUTH,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                AttachFace.WALL,
                                Direction.WEST,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                    )
            );
    }

    private void createLilyPad() {
        this.createSimpleFlatItemModel(Blocks.LILY_PAD);
        this.blockStateOutput.accept(createRotatedVariant(Blocks.LILY_PAD, ModelLocationUtils.getModelLocation(Blocks.LILY_PAD)));
    }

    private void createFrogspawnBlock() {
        this.createSimpleFlatItemModel(Blocks.FROGSPAWN);
        this.blockStateOutput.accept(createSimpleBlock(Blocks.FROGSPAWN, ModelLocationUtils.getModelLocation(Blocks.FROGSPAWN)));
    }

    private void createNetherPortalBlock() {
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.NETHER_PORTAL)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.HORIZONTAL_AXIS)
                            .select(
                                Direction.Axis.X,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.NETHER_PORTAL, "_ns"))
                            )
                            .select(
                                Direction.Axis.Z,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.NETHER_PORTAL, "_ew"))
                            )
                    )
            );
    }

    private void createNetherrack() {
        ResourceLocation resourcelocation = TexturedModel.CUBE.create(Blocks.NETHERRACK, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(
                    Blocks.NETHERRACK,
                    Variant.variant().with(VariantProperties.MODEL, resourcelocation),
                    Variant.variant().with(VariantProperties.MODEL, resourcelocation).with(VariantProperties.X_ROT, VariantProperties.Rotation.R90),
                    Variant.variant().with(VariantProperties.MODEL, resourcelocation).with(VariantProperties.X_ROT, VariantProperties.Rotation.R180),
                    Variant.variant().with(VariantProperties.MODEL, resourcelocation).with(VariantProperties.X_ROT, VariantProperties.Rotation.R270),
                    Variant.variant().with(VariantProperties.MODEL, resourcelocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90),
                    Variant.variant()
                        .with(VariantProperties.MODEL, resourcelocation)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90),
                    Variant.variant()
                        .with(VariantProperties.MODEL, resourcelocation)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180),
                    Variant.variant()
                        .with(VariantProperties.MODEL, resourcelocation)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270),
                    Variant.variant().with(VariantProperties.MODEL, resourcelocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180),
                    Variant.variant()
                        .with(VariantProperties.MODEL, resourcelocation)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90),
                    Variant.variant()
                        .with(VariantProperties.MODEL, resourcelocation)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180),
                    Variant.variant()
                        .with(VariantProperties.MODEL, resourcelocation)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270),
                    Variant.variant().with(VariantProperties.MODEL, resourcelocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270),
                    Variant.variant()
                        .with(VariantProperties.MODEL, resourcelocation)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90),
                    Variant.variant()
                        .with(VariantProperties.MODEL, resourcelocation)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180),
                    Variant.variant()
                        .with(VariantProperties.MODEL, resourcelocation)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                )
            );
    }

    private void createObserver() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.OBSERVER);
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.OBSERVER, "_on");
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.OBSERVER)
                    .with(createBooleanModelDispatch(BlockStateProperties.POWERED, resourcelocation1, resourcelocation))
                    .with(createFacingDispatch())
            );
    }

    private void createPistons() {
        TextureMapping texturemapping = new TextureMapping()
            .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(Blocks.PISTON, "_bottom"))
            .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.PISTON, "_side"));
        ResourceLocation resourcelocation = TextureMapping.getBlockTexture(Blocks.PISTON, "_top_sticky");
        ResourceLocation resourcelocation1 = TextureMapping.getBlockTexture(Blocks.PISTON, "_top");
        TextureMapping texturemapping1 = texturemapping.copyAndUpdate(TextureSlot.PLATFORM, resourcelocation);
        TextureMapping texturemapping2 = texturemapping.copyAndUpdate(TextureSlot.PLATFORM, resourcelocation1);
        ResourceLocation resourcelocation2 = ModelLocationUtils.getModelLocation(Blocks.PISTON, "_base");
        this.createPistonVariant(Blocks.PISTON, resourcelocation2, texturemapping2);
        this.createPistonVariant(Blocks.STICKY_PISTON, resourcelocation2, texturemapping1);
        ResourceLocation resourcelocation3 = ModelTemplates.CUBE_BOTTOM_TOP
            .createWithSuffix(Blocks.PISTON, "_inventory", texturemapping.copyAndUpdate(TextureSlot.TOP, resourcelocation1), this.modelOutput);
        ResourceLocation resourcelocation4 = ModelTemplates.CUBE_BOTTOM_TOP
            .createWithSuffix(Blocks.STICKY_PISTON, "_inventory", texturemapping.copyAndUpdate(TextureSlot.TOP, resourcelocation), this.modelOutput);
        this.delegateItemModel(Blocks.PISTON, resourcelocation3);
        this.delegateItemModel(Blocks.STICKY_PISTON, resourcelocation4);
    }

    private void createPistonVariant(Block pistonBlock, ResourceLocation baseModelLocation, TextureMapping topTextureMapping) {
        ResourceLocation resourcelocation = ModelTemplates.PISTON.create(pistonBlock, topTextureMapping, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(pistonBlock)
                    .with(createBooleanModelDispatch(BlockStateProperties.EXTENDED, baseModelLocation, resourcelocation))
                    .with(createFacingDispatch())
            );
    }

    private void createPistonHeads() {
        TextureMapping texturemapping = new TextureMapping()
            .put(TextureSlot.UNSTICKY, TextureMapping.getBlockTexture(Blocks.PISTON, "_top"))
            .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.PISTON, "_side"));
        TextureMapping texturemapping1 = texturemapping.copyAndUpdate(TextureSlot.PLATFORM, TextureMapping.getBlockTexture(Blocks.PISTON, "_top_sticky"));
        TextureMapping texturemapping2 = texturemapping.copyAndUpdate(TextureSlot.PLATFORM, TextureMapping.getBlockTexture(Blocks.PISTON, "_top"));
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.PISTON_HEAD)
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.SHORT, BlockStateProperties.PISTON_TYPE)
                            .select(
                                false,
                                PistonType.DEFAULT,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        ModelTemplates.PISTON_HEAD.createWithSuffix(Blocks.PISTON, "_head", texturemapping2, this.modelOutput)
                                    )
                            )
                            .select(
                                false,
                                PistonType.STICKY,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        ModelTemplates.PISTON_HEAD.createWithSuffix(Blocks.PISTON, "_head_sticky", texturemapping1, this.modelOutput)
                                    )
                            )
                            .select(
                                true,
                                PistonType.DEFAULT,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        ModelTemplates.PISTON_HEAD_SHORT.createWithSuffix(Blocks.PISTON, "_head_short", texturemapping2, this.modelOutput)
                                    )
                            )
                            .select(
                                true,
                                PistonType.STICKY,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        ModelTemplates.PISTON_HEAD_SHORT
                                            .createWithSuffix(Blocks.PISTON, "_head_short_sticky", texturemapping1, this.modelOutput)
                                    )
                            )
                    )
                    .with(createFacingDispatch())
            );
    }

    private void createTrialSpawner() {
        Block block = Blocks.TRIAL_SPAWNER;
        TextureMapping texturemapping = TextureMapping.trialSpawner(block, "_side_inactive", "_top_inactive");
        TextureMapping texturemapping1 = TextureMapping.trialSpawner(block, "_side_active", "_top_active");
        TextureMapping texturemapping2 = TextureMapping.trialSpawner(block, "_side_active", "_top_ejecting_reward");
        TextureMapping texturemapping3 = TextureMapping.trialSpawner(block, "_side_inactive_ominous", "_top_inactive_ominous");
        TextureMapping texturemapping4 = TextureMapping.trialSpawner(block, "_side_active_ominous", "_top_active_ominous");
        TextureMapping texturemapping5 = TextureMapping.trialSpawner(block, "_side_active_ominous", "_top_ejecting_reward_ominous");
        ResourceLocation resourcelocation = ModelTemplates.CUBE_BOTTOM_TOP_INNER_FACES.create(block, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.CUBE_BOTTOM_TOP_INNER_FACES.createWithSuffix(block, "_active", texturemapping1, this.modelOutput);
        ResourceLocation resourcelocation2 = ModelTemplates.CUBE_BOTTOM_TOP_INNER_FACES
            .createWithSuffix(block, "_ejecting_reward", texturemapping2, this.modelOutput);
        ResourceLocation resourcelocation3 = ModelTemplates.CUBE_BOTTOM_TOP_INNER_FACES
            .createWithSuffix(block, "_inactive_ominous", texturemapping3, this.modelOutput);
        ResourceLocation resourcelocation4 = ModelTemplates.CUBE_BOTTOM_TOP_INNER_FACES
            .createWithSuffix(block, "_active_ominous", texturemapping4, this.modelOutput);
        ResourceLocation resourcelocation5 = ModelTemplates.CUBE_BOTTOM_TOP_INNER_FACES
            .createWithSuffix(block, "_ejecting_reward_ominous", texturemapping5, this.modelOutput);
        this.delegateItemModel(block, resourcelocation);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(block)
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.TRIAL_SPAWNER_STATE, BlockStateProperties.OMINOUS)
                            .generate(
                                (p_337483_, p_337484_) -> {
                                    return switch (p_337483_) {
                                        case INACTIVE, COOLDOWN -> Variant.variant()
                                        .with(VariantProperties.MODEL, p_337484_ ? resourcelocation3 : resourcelocation);
                                        case WAITING_FOR_PLAYERS, ACTIVE, WAITING_FOR_REWARD_EJECTION -> Variant.variant()
                                        .with(VariantProperties.MODEL, p_337484_ ? resourcelocation4 : resourcelocation1);
                                        case EJECTING_REWARD -> Variant.variant()
                                        .with(VariantProperties.MODEL, p_337484_ ? resourcelocation5 : resourcelocation2);
                                    };
                                }
                            )
                    )
            );
    }

    private void createVault() {
        Block block = Blocks.VAULT;
        TextureMapping texturemapping = TextureMapping.vault(block, "_front_off", "_side_off", "_top", "_bottom");
        TextureMapping texturemapping1 = TextureMapping.vault(block, "_front_on", "_side_on", "_top", "_bottom");
        TextureMapping texturemapping2 = TextureMapping.vault(block, "_front_ejecting", "_side_on", "_top", "_bottom");
        TextureMapping texturemapping3 = TextureMapping.vault(block, "_front_ejecting", "_side_on", "_top_ejecting", "_bottom");
        ResourceLocation resourcelocation = ModelTemplates.VAULT.create(block, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.VAULT.createWithSuffix(block, "_active", texturemapping1, this.modelOutput);
        ResourceLocation resourcelocation2 = ModelTemplates.VAULT.createWithSuffix(block, "_unlocking", texturemapping2, this.modelOutput);
        ResourceLocation resourcelocation3 = ModelTemplates.VAULT.createWithSuffix(block, "_ejecting_reward", texturemapping3, this.modelOutput);
        TextureMapping texturemapping4 = TextureMapping.vault(block, "_front_off_ominous", "_side_off_ominous", "_top_ominous", "_bottom_ominous");
        TextureMapping texturemapping5 = TextureMapping.vault(block, "_front_on_ominous", "_side_on_ominous", "_top_ominous", "_bottom_ominous");
        TextureMapping texturemapping6 = TextureMapping.vault(block, "_front_ejecting_ominous", "_side_on_ominous", "_top_ominous", "_bottom_ominous");
        TextureMapping texturemapping7 = TextureMapping.vault(block, "_front_ejecting_ominous", "_side_on_ominous", "_top_ejecting_ominous", "_bottom_ominous");
        ResourceLocation resourcelocation4 = ModelTemplates.VAULT.createWithSuffix(block, "_ominous", texturemapping4, this.modelOutput);
        ResourceLocation resourcelocation5 = ModelTemplates.VAULT.createWithSuffix(block, "_active_ominous", texturemapping5, this.modelOutput);
        ResourceLocation resourcelocation6 = ModelTemplates.VAULT.createWithSuffix(block, "_unlocking_ominous", texturemapping6, this.modelOutput);
        ResourceLocation resourcelocation7 = ModelTemplates.VAULT.createWithSuffix(block, "_ejecting_reward_ominous", texturemapping7, this.modelOutput);
        this.delegateItemModel(block, resourcelocation);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(block)
                    .with(createHorizontalFacingDispatch())
                    .with(PropertyDispatch.properties(VaultBlock.STATE, VaultBlock.OMINOUS).generate((p_337475_, p_337476_) -> {
                        return switch (p_337475_) {
                            case INACTIVE -> Variant.variant().with(VariantProperties.MODEL, p_337476_ ? resourcelocation4 : resourcelocation);
                            case ACTIVE -> Variant.variant().with(VariantProperties.MODEL, p_337476_ ? resourcelocation5 : resourcelocation1);
                            case UNLOCKING -> Variant.variant().with(VariantProperties.MODEL, p_337476_ ? resourcelocation6 : resourcelocation2);
                            case EJECTING -> Variant.variant().with(VariantProperties.MODEL, p_337476_ ? resourcelocation7 : resourcelocation3);
                        };
                    }))
            );
    }

    private void createSculkSensor() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.SCULK_SENSOR, "_inactive");
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.SCULK_SENSOR, "_active");
        this.delegateItemModel(Blocks.SCULK_SENSOR, resourcelocation);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.SCULK_SENSOR)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.SCULK_SENSOR_PHASE)
                            .generate(
                                p_284650_ -> Variant.variant()
                                        .with(
                                            VariantProperties.MODEL,
                                            p_284650_ != SculkSensorPhase.ACTIVE && p_284650_ != SculkSensorPhase.COOLDOWN
                                                ? resourcelocation
                                                : resourcelocation1
                                        )
                            )
                    )
            );
    }

    private void createCalibratedSculkSensor() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.CALIBRATED_SCULK_SENSOR, "_inactive");
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.CALIBRATED_SCULK_SENSOR, "_active");
        this.delegateItemModel(Blocks.CALIBRATED_SCULK_SENSOR, resourcelocation);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.CALIBRATED_SCULK_SENSOR)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.SCULK_SENSOR_PHASE)
                            .generate(
                                p_284647_ -> Variant.variant()
                                        .with(
                                            VariantProperties.MODEL,
                                            p_284647_ != SculkSensorPhase.ACTIVE && p_284647_ != SculkSensorPhase.COOLDOWN
                                                ? resourcelocation
                                                : resourcelocation1
                                        )
                            )
                    )
                    .with(createHorizontalFacingDispatch())
            );
    }

    private void createSculkShrieker() {
        ResourceLocation resourcelocation = ModelTemplates.SCULK_SHRIEKER.create(Blocks.SCULK_SHRIEKER, TextureMapping.sculkShrieker(false), this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.SCULK_SHRIEKER
            .createWithSuffix(Blocks.SCULK_SHRIEKER, "_can_summon", TextureMapping.sculkShrieker(true), this.modelOutput);
        this.delegateItemModel(Blocks.SCULK_SHRIEKER, resourcelocation);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.SCULK_SHRIEKER)
                    .with(createBooleanModelDispatch(BlockStateProperties.CAN_SUMMON, resourcelocation1, resourcelocation))
            );
    }

    private void createScaffolding() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.SCAFFOLDING, "_stable");
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.SCAFFOLDING, "_unstable");
        this.delegateItemModel(Blocks.SCAFFOLDING, resourcelocation);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.SCAFFOLDING)
                    .with(createBooleanModelDispatch(BlockStateProperties.BOTTOM, resourcelocation1, resourcelocation))
            );
    }

    private void createCaveVines() {
        ResourceLocation resourcelocation = this.createSuffixedVariant(Blocks.CAVE_VINES, "", ModelTemplates.CROSS, TextureMapping::cross);
        ResourceLocation resourcelocation1 = this.createSuffixedVariant(Blocks.CAVE_VINES, "_lit", ModelTemplates.CROSS, TextureMapping::cross);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.CAVE_VINES)
                    .with(createBooleanModelDispatch(BlockStateProperties.BERRIES, resourcelocation1, resourcelocation))
            );
        ResourceLocation resourcelocation2 = this.createSuffixedVariant(Blocks.CAVE_VINES_PLANT, "", ModelTemplates.CROSS, TextureMapping::cross);
        ResourceLocation resourcelocation3 = this.createSuffixedVariant(Blocks.CAVE_VINES_PLANT, "_lit", ModelTemplates.CROSS, TextureMapping::cross);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.CAVE_VINES_PLANT)
                    .with(createBooleanModelDispatch(BlockStateProperties.BERRIES, resourcelocation3, resourcelocation2))
            );
    }

    private void createRedstoneLamp() {
        ResourceLocation resourcelocation = TexturedModel.CUBE.create(Blocks.REDSTONE_LAMP, this.modelOutput);
        ResourceLocation resourcelocation1 = this.createSuffixedVariant(Blocks.REDSTONE_LAMP, "_on", ModelTemplates.CUBE_ALL, TextureMapping::cube);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.REDSTONE_LAMP)
                    .with(createBooleanModelDispatch(BlockStateProperties.LIT, resourcelocation1, resourcelocation))
            );
    }

    private void createNormalTorch(Block torchBlock, Block wallTorchBlock) {
        TextureMapping texturemapping = TextureMapping.torch(torchBlock);
        this.blockStateOutput.accept(createSimpleBlock(torchBlock, ModelTemplates.TORCH.create(torchBlock, texturemapping, this.modelOutput)));
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(
                        wallTorchBlock,
                        Variant.variant().with(VariantProperties.MODEL, ModelTemplates.WALL_TORCH.create(wallTorchBlock, texturemapping, this.modelOutput))
                    )
                    .with(createTorchHorizontalDispatch())
            );
        this.createSimpleFlatItemModel(torchBlock);
        this.skipAutoItemBlock(wallTorchBlock);
    }

    private void createRedstoneTorch() {
        TextureMapping texturemapping = TextureMapping.torch(Blocks.REDSTONE_TORCH);
        TextureMapping texturemapping1 = TextureMapping.torch(TextureMapping.getBlockTexture(Blocks.REDSTONE_TORCH, "_off"));
        ResourceLocation resourcelocation = ModelTemplates.TORCH.create(Blocks.REDSTONE_TORCH, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.TORCH.createWithSuffix(Blocks.REDSTONE_TORCH, "_off", texturemapping1, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.REDSTONE_TORCH)
                    .with(createBooleanModelDispatch(BlockStateProperties.LIT, resourcelocation, resourcelocation1))
            );
        ResourceLocation resourcelocation2 = ModelTemplates.WALL_TORCH.create(Blocks.REDSTONE_WALL_TORCH, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation3 = ModelTemplates.WALL_TORCH.createWithSuffix(Blocks.REDSTONE_WALL_TORCH, "_off", texturemapping1, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.REDSTONE_WALL_TORCH)
                    .with(createBooleanModelDispatch(BlockStateProperties.LIT, resourcelocation2, resourcelocation3))
                    .with(createTorchHorizontalDispatch())
            );
        this.createSimpleFlatItemModel(Blocks.REDSTONE_TORCH);
        this.skipAutoItemBlock(Blocks.REDSTONE_WALL_TORCH);
    }

    private void createRepeater() {
        this.createSimpleFlatItemModel(Items.REPEATER);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.REPEATER)
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.DELAY, BlockStateProperties.LOCKED, BlockStateProperties.POWERED)
                            .generate((p_337485_, p_337486_, p_337487_) -> {
                                StringBuilder stringbuilder = new StringBuilder();
                                stringbuilder.append('_').append(p_337485_).append("tick");
                                if (p_337487_) {
                                    stringbuilder.append("_on");
                                }

                                if (p_337486_) {
                                    stringbuilder.append("_locked");
                                }

                                return Variant.variant()
                                    .with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.REPEATER, stringbuilder.toString()));
                            })
                    )
                    .with(createHorizontalFacingDispatchAlt())
            );
    }

    private void createSeaPickle() {
        this.createSimpleFlatItemModel(Items.SEA_PICKLE);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.SEA_PICKLE)
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.PICKLES, BlockStateProperties.WATERLOGGED)
                            .select(1, false, Arrays.asList(createRotatedVariants(ModelLocationUtils.decorateBlockModelLocation("dead_sea_pickle"))))
                            .select(2, false, Arrays.asList(createRotatedVariants(ModelLocationUtils.decorateBlockModelLocation("two_dead_sea_pickles"))))
                            .select(3, false, Arrays.asList(createRotatedVariants(ModelLocationUtils.decorateBlockModelLocation("three_dead_sea_pickles"))))
                            .select(4, false, Arrays.asList(createRotatedVariants(ModelLocationUtils.decorateBlockModelLocation("four_dead_sea_pickles"))))
                            .select(1, true, Arrays.asList(createRotatedVariants(ModelLocationUtils.decorateBlockModelLocation("sea_pickle"))))
                            .select(2, true, Arrays.asList(createRotatedVariants(ModelLocationUtils.decorateBlockModelLocation("two_sea_pickles"))))
                            .select(3, true, Arrays.asList(createRotatedVariants(ModelLocationUtils.decorateBlockModelLocation("three_sea_pickles"))))
                            .select(4, true, Arrays.asList(createRotatedVariants(ModelLocationUtils.decorateBlockModelLocation("four_sea_pickles"))))
                    )
            );
    }

    private void createSnowBlocks() {
        TextureMapping texturemapping = TextureMapping.cube(Blocks.SNOW);
        ResourceLocation resourcelocation = ModelTemplates.CUBE_ALL.create(Blocks.SNOW_BLOCK, texturemapping, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.SNOW)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.LAYERS)
                            .generate(
                                p_176151_ -> Variant.variant()
                                        .with(
                                            VariantProperties.MODEL,
                                            p_176151_ < 8 ? ModelLocationUtils.getModelLocation(Blocks.SNOW, "_height" + p_176151_ * 2) : resourcelocation
                                        )
                            )
                    )
            );
        this.delegateItemModel(Blocks.SNOW, ModelLocationUtils.getModelLocation(Blocks.SNOW, "_height2"));
        this.blockStateOutput.accept(createSimpleBlock(Blocks.SNOW_BLOCK, resourcelocation));
    }

    private void createStonecutter() {
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(
                        Blocks.STONECUTTER, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.STONECUTTER))
                    )
                    .with(createHorizontalFacingDispatch())
            );
    }

    private void createStructureBlock() {
        ResourceLocation resourcelocation = TexturedModel.CUBE.create(Blocks.STRUCTURE_BLOCK, this.modelOutput);
        this.delegateItemModel(Blocks.STRUCTURE_BLOCK, resourcelocation);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.STRUCTURE_BLOCK)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.STRUCTUREBLOCK_MODE)
                            .generate(
                                p_176115_ -> Variant.variant()
                                        .with(
                                            VariantProperties.MODEL,
                                            this.createSuffixedVariant(
                                                Blocks.STRUCTURE_BLOCK, "_" + p_176115_.getSerializedName(), ModelTemplates.CUBE_ALL, TextureMapping::cube
                                            )
                                        )
                            )
                    )
            );
    }

    private void createSweetBerryBush() {
        this.createSimpleFlatItemModel(Items.SWEET_BERRIES);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.SWEET_BERRY_BUSH)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.AGE_3)
                            .generate(
                                p_176132_ -> Variant.variant()
                                        .with(
                                            VariantProperties.MODEL,
                                            this.createSuffixedVariant(
                                                Blocks.SWEET_BERRY_BUSH, "_stage" + p_176132_, ModelTemplates.CROSS, TextureMapping::cross
                                            )
                                        )
                            )
                    )
            );
    }

    private void createTripwire() {
        this.createSimpleFlatItemModel(Items.STRING);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.TRIPWIRE)
                    .with(
                        PropertyDispatch.properties(
                                BlockStateProperties.ATTACHED,
                                BlockStateProperties.EAST,
                                BlockStateProperties.NORTH,
                                BlockStateProperties.SOUTH,
                                BlockStateProperties.WEST
                            )
                            .select(
                                false,
                                false,
                                false,
                                false,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ns"))
                            )
                            .select(
                                false,
                                true,
                                false,
                                false,
                                false,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_n"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                false,
                                false,
                                true,
                                false,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_n"))
                            )
                            .select(
                                false,
                                false,
                                false,
                                true,
                                false,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_n"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                false,
                                false,
                                false,
                                false,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_n"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                false,
                                true,
                                true,
                                false,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ne"))
                            )
                            .select(
                                false,
                                true,
                                false,
                                true,
                                false,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ne"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                false,
                                false,
                                false,
                                true,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ne"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                false,
                                false,
                                true,
                                false,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ne"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                false,
                                false,
                                true,
                                true,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ns"))
                            )
                            .select(
                                false,
                                true,
                                false,
                                false,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ns"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                false,
                                true,
                                true,
                                true,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_nse"))
                            )
                            .select(
                                false,
                                true,
                                false,
                                true,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_nse"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                false,
                                false,
                                true,
                                true,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_nse"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                false,
                                true,
                                true,
                                false,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_nse"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                false,
                                true,
                                true,
                                true,
                                true,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_nsew"))
                            )
                            .select(
                                true,
                                false,
                                false,
                                false,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ns"))
                            )
                            .select(
                                true,
                                false,
                                true,
                                false,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_n"))
                            )
                            .select(
                                true,
                                false,
                                false,
                                true,
                                false,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_n"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                true,
                                true,
                                false,
                                false,
                                false,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_n"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                true,
                                false,
                                false,
                                false,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_n"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                true,
                                true,
                                true,
                                false,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ne"))
                            )
                            .select(
                                true,
                                true,
                                false,
                                true,
                                false,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ne"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                true,
                                false,
                                false,
                                true,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ne"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                true,
                                false,
                                true,
                                false,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ne"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                true,
                                false,
                                true,
                                true,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ns"))
                            )
                            .select(
                                true,
                                true,
                                false,
                                false,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ns"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                true,
                                true,
                                true,
                                true,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_nse"))
                            )
                            .select(
                                true,
                                true,
                                false,
                                true,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_nse"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                true,
                                false,
                                true,
                                true,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_nse"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                true,
                                true,
                                true,
                                false,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_nse"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                true,
                                true,
                                true,
                                true,
                                true,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_nsew"))
                            )
                    )
            );
    }

    private void createTripwireHook() {
        this.createSimpleFlatItemModel(Blocks.TRIPWIRE_HOOK);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.TRIPWIRE_HOOK)
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.ATTACHED, BlockStateProperties.POWERED)
                            .generate(
                                (p_176124_, p_176125_) -> Variant.variant()
                                        .with(
                                            VariantProperties.MODEL,
                                            TextureMapping.getBlockTexture(Blocks.TRIPWIRE_HOOK, (p_176124_ ? "_attached" : "") + (p_176125_ ? "_on" : ""))
                                        )
                            )
                    )
                    .with(createHorizontalFacingDispatch())
            );
    }

    private ResourceLocation createTurtleEggModel(int hatchAmount, String variantName, TextureMapping textureMapping) {
        switch (hatchAmount) {
            case 1:
                return ModelTemplates.TURTLE_EGG.create(ModelLocationUtils.decorateBlockModelLocation(variantName + "turtle_egg"), textureMapping, this.modelOutput);
            case 2:
                return ModelTemplates.TWO_TURTLE_EGGS
                    .create(ModelLocationUtils.decorateBlockModelLocation("two_" + variantName + "turtle_eggs"), textureMapping, this.modelOutput);
            case 3:
                return ModelTemplates.THREE_TURTLE_EGGS
                    .create(ModelLocationUtils.decorateBlockModelLocation("three_" + variantName + "turtle_eggs"), textureMapping, this.modelOutput);
            case 4:
                return ModelTemplates.FOUR_TURTLE_EGGS
                    .create(ModelLocationUtils.decorateBlockModelLocation("four_" + variantName + "turtle_eggs"), textureMapping, this.modelOutput);
            default:
                throw new UnsupportedOperationException();
        }
    }

    private ResourceLocation createTurtleEggModel(Integer eggAmount, Integer variantId) {
        switch (variantId) {
            case 0:
                return this.createTurtleEggModel(eggAmount, "", TextureMapping.cube(TextureMapping.getBlockTexture(Blocks.TURTLE_EGG)));
            case 1:
                return this.createTurtleEggModel(
                    eggAmount, "slightly_cracked_", TextureMapping.cube(TextureMapping.getBlockTexture(Blocks.TURTLE_EGG, "_slightly_cracked"))
                );
            case 2:
                return this.createTurtleEggModel(
                    eggAmount, "very_cracked_", TextureMapping.cube(TextureMapping.getBlockTexture(Blocks.TURTLE_EGG, "_very_cracked"))
                );
            default:
                throw new UnsupportedOperationException();
        }
    }

    private void createTurtleEgg() {
        this.createSimpleFlatItemModel(Items.TURTLE_EGG);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.TURTLE_EGG)
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.EGGS, BlockStateProperties.HATCH)
                            .generateList((p_176185_, p_176186_) -> Arrays.asList(createRotatedVariants(this.createTurtleEggModel(p_176185_, p_176186_))))
                    )
            );
    }

    private void createSnifferEgg() {
        this.createSimpleFlatItemModel(Items.SNIFFER_EGG);
        Function<Integer, ResourceLocation> function = p_278206_ -> {
            String s = switch (p_278206_) {
                case 1 -> "_slightly_cracked";
                case 2 -> "_very_cracked";
                default -> "_not_cracked";
            };
            TextureMapping texturemapping = TextureMapping.snifferEgg(s);
            return ModelTemplates.SNIFFER_EGG.createWithSuffix(Blocks.SNIFFER_EGG, s, texturemapping, this.modelOutput);
        };
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.SNIFFER_EGG)
                    .with(
                        PropertyDispatch.property(SnifferEggBlock.HATCH)
                            .generate(p_277261_ -> Variant.variant().with(VariantProperties.MODEL, function.apply(p_277261_)))
                    )
            );
    }

    private void createMultiface(Block multifaceBlock) {
        this.createSimpleFlatItemModel(multifaceBlock);
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(multifaceBlock);
        MultiPartGenerator multipartgenerator = MultiPartGenerator.multiPart(multifaceBlock);
        Condition.TerminalCondition condition$terminalcondition = Util.make(
            Condition.condition(), p_236295_ -> MULTIFACE_GENERATOR.stream().map(Pair::getFirst).forEach(p_236299_ -> {
                    if (multifaceBlock.defaultBlockState().hasProperty(p_236299_)) {
                        p_236295_.term(p_236299_, false);
                    }
                })
        );

        for (Pair<BooleanProperty, Function<ResourceLocation, Variant>> pair : MULTIFACE_GENERATOR) {
            BooleanProperty booleanproperty = pair.getFirst();
            Function<ResourceLocation, Variant> function = pair.getSecond();
            if (multifaceBlock.defaultBlockState().hasProperty(booleanproperty)) {
                multipartgenerator.with(Condition.condition().term(booleanproperty, true), function.apply(resourcelocation));
                multipartgenerator.with(condition$terminalcondition, function.apply(resourcelocation));
            }
        }

        this.blockStateOutput.accept(multipartgenerator);
    }

    private void createSculkCatalyst() {
        ResourceLocation resourcelocation = TextureMapping.getBlockTexture(Blocks.SCULK_CATALYST, "_bottom");
        TextureMapping texturemapping = new TextureMapping()
            .put(TextureSlot.BOTTOM, resourcelocation)
            .put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.SCULK_CATALYST, "_top"))
            .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.SCULK_CATALYST, "_side"));
        TextureMapping texturemapping1 = new TextureMapping()
            .put(TextureSlot.BOTTOM, resourcelocation)
            .put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.SCULK_CATALYST, "_top_bloom"))
            .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.SCULK_CATALYST, "_side_bloom"));
        ResourceLocation resourcelocation1 = ModelTemplates.CUBE_BOTTOM_TOP.createWithSuffix(Blocks.SCULK_CATALYST, "", texturemapping, this.modelOutput);
        ResourceLocation resourcelocation2 = ModelTemplates.CUBE_BOTTOM_TOP
            .createWithSuffix(Blocks.SCULK_CATALYST, "_bloom", texturemapping1, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.SCULK_CATALYST)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.BLOOM)
                            .generate(p_236280_ -> Variant.variant().with(VariantProperties.MODEL, p_236280_ ? resourcelocation2 : resourcelocation1))
                    )
            );
        this.delegateItemModel(Items.SCULK_CATALYST, resourcelocation1);
    }

    private void createChiseledBookshelf() {
        Block block = Blocks.CHISELED_BOOKSHELF;
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(block);
        MultiPartGenerator multipartgenerator = MultiPartGenerator.multiPart(block);
        List.of(
                Pair.of(Direction.NORTH, VariantProperties.Rotation.R0),
                Pair.of(Direction.EAST, VariantProperties.Rotation.R90),
                Pair.of(Direction.SOUTH, VariantProperties.Rotation.R180),
                Pair.of(Direction.WEST, VariantProperties.Rotation.R270)
            )
            .forEach(
                p_329851_ -> {
                    Direction direction = p_329851_.getFirst();
                    VariantProperties.Rotation variantproperties$rotation = p_329851_.getSecond();
                    Condition.TerminalCondition condition$terminalcondition = Condition.condition().term(BlockStateProperties.HORIZONTAL_FACING, direction);
                    multipartgenerator.with(
                        condition$terminalcondition,
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation)
                            .with(VariantProperties.Y_ROT, variantproperties$rotation)
                            .with(VariantProperties.UV_LOCK, true)
                    );
                    this.addSlotStateAndRotationVariants(multipartgenerator, condition$terminalcondition, variantproperties$rotation);
                }
            );
        this.blockStateOutput.accept(multipartgenerator);
        this.delegateItemModel(block, ModelLocationUtils.getModelLocation(block, "_inventory"));
        CHISELED_BOOKSHELF_SLOT_MODEL_CACHE.clear();
    }

    private void addSlotStateAndRotationVariants(MultiPartGenerator generator, Condition.TerminalCondition condition, VariantProperties.Rotation rotation) {
        List.of(
                Pair.of(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_0_OCCUPIED, ModelTemplates.CHISELED_BOOKSHELF_SLOT_TOP_LEFT),
                Pair.of(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_1_OCCUPIED, ModelTemplates.CHISELED_BOOKSHELF_SLOT_TOP_MID),
                Pair.of(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_2_OCCUPIED, ModelTemplates.CHISELED_BOOKSHELF_SLOT_TOP_RIGHT),
                Pair.of(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_3_OCCUPIED, ModelTemplates.CHISELED_BOOKSHELF_SLOT_BOTTOM_LEFT),
                Pair.of(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_4_OCCUPIED, ModelTemplates.CHISELED_BOOKSHELF_SLOT_BOTTOM_MID),
                Pair.of(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_5_OCCUPIED, ModelTemplates.CHISELED_BOOKSHELF_SLOT_BOTTOM_RIGHT)
            )
            .forEach(p_307151_ -> {
                BooleanProperty booleanproperty = p_307151_.getFirst();
                ModelTemplate modeltemplate = p_307151_.getSecond();
                this.addBookSlotModel(generator, condition, rotation, booleanproperty, modeltemplate, true);
                this.addBookSlotModel(generator, condition, rotation, booleanproperty, modeltemplate, false);
            });
    }

    private void addBookSlotModel(
        MultiPartGenerator generator,
        Condition.TerminalCondition condition,
        VariantProperties.Rotation rotation,
        BooleanProperty hasBookProperty,
        ModelTemplate template,
        boolean hasBook
    ) {
        String s = hasBook ? "_occupied" : "_empty";
        TextureMapping texturemapping = new TextureMapping().put(TextureSlot.TEXTURE, TextureMapping.getBlockTexture(Blocks.CHISELED_BOOKSHELF, s));
        BlockModelGenerators.BookSlotModelCacheKey blockmodelgenerators$bookslotmodelcachekey = new BlockModelGenerators.BookSlotModelCacheKey(template, s);
        ResourceLocation resourcelocation = CHISELED_BOOKSHELF_SLOT_MODEL_CACHE.computeIfAbsent(
            blockmodelgenerators$bookslotmodelcachekey, p_261415_ -> template.createWithSuffix(Blocks.CHISELED_BOOKSHELF, s, texturemapping, this.modelOutput)
        );
        generator.with(
            Condition.and(condition, Condition.condition().term(hasBookProperty, hasBook)),
            Variant.variant().with(VariantProperties.MODEL, resourcelocation).with(VariantProperties.Y_ROT, rotation)
        );
    }

    private void createMagmaBlock() {
        this.blockStateOutput
            .accept(
                createSimpleBlock(
                    Blocks.MAGMA_BLOCK,
                    ModelTemplates.CUBE_ALL
                        .create(Blocks.MAGMA_BLOCK, TextureMapping.cube(ModelLocationUtils.decorateBlockModelLocation("magma")), this.modelOutput)
                )
            );
    }

    private void createShulkerBox(Block shulkerBoxBlock) {
        this.createTrivialBlock(shulkerBoxBlock, TexturedModel.PARTICLE_ONLY);
        ModelTemplates.SHULKER_BOX_INVENTORY
            .create(ModelLocationUtils.getModelLocation(shulkerBoxBlock.asItem()), TextureMapping.particle(shulkerBoxBlock), this.modelOutput);
    }

    private void createGrowingPlant(Block growingPlantBlock, Block plantBlock, BlockModelGenerators.TintState tintState) {
        this.createCrossBlock(growingPlantBlock, tintState);
        this.createCrossBlock(plantBlock, tintState);
    }

    private void createBedItem(Block bedBlock, Block woolBlock) {
        ModelTemplates.BED_INVENTORY.create(ModelLocationUtils.getModelLocation(bedBlock.asItem()), TextureMapping.particle(woolBlock), this.modelOutput);
    }

    private void createInfestedStone() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.STONE);
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.STONE, "_mirrored");
        this.blockStateOutput.accept(createRotatedVariant(Blocks.INFESTED_STONE, resourcelocation, resourcelocation1));
        this.delegateItemModel(Blocks.INFESTED_STONE, resourcelocation);
    }

    private void createInfestedDeepslate() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.DEEPSLATE);
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.DEEPSLATE, "_mirrored");
        this.blockStateOutput.accept(createRotatedVariant(Blocks.INFESTED_DEEPSLATE, resourcelocation, resourcelocation1).with(createRotatedPillar()));
        this.delegateItemModel(Blocks.INFESTED_DEEPSLATE, resourcelocation);
    }

    private void createNetherRoots(Block plantBlock, Block pottedPlantBlock) {
        this.createCrossBlockWithDefaultItem(plantBlock, BlockModelGenerators.TintState.NOT_TINTED);
        TextureMapping texturemapping = TextureMapping.plant(TextureMapping.getBlockTexture(plantBlock, "_pot"));
        ResourceLocation resourcelocation = BlockModelGenerators.TintState.NOT_TINTED.getCrossPot().create(pottedPlantBlock, texturemapping, this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(pottedPlantBlock, resourcelocation));
    }

    private void createRespawnAnchor() {
        ResourceLocation resourcelocation = TextureMapping.getBlockTexture(Blocks.RESPAWN_ANCHOR, "_bottom");
        ResourceLocation resourcelocation1 = TextureMapping.getBlockTexture(Blocks.RESPAWN_ANCHOR, "_top_off");
        ResourceLocation resourcelocation2 = TextureMapping.getBlockTexture(Blocks.RESPAWN_ANCHOR, "_top");
        ResourceLocation[] aresourcelocation = new ResourceLocation[5];

        for (int i = 0; i < 5; i++) {
            TextureMapping texturemapping = new TextureMapping()
                .put(TextureSlot.BOTTOM, resourcelocation)
                .put(TextureSlot.TOP, i == 0 ? resourcelocation1 : resourcelocation2)
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.RESPAWN_ANCHOR, "_side" + i));
            aresourcelocation[i] = ModelTemplates.CUBE_BOTTOM_TOP.createWithSuffix(Blocks.RESPAWN_ANCHOR, "_" + i, texturemapping, this.modelOutput);
        }

        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.RESPAWN_ANCHOR)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.RESPAWN_ANCHOR_CHARGES)
                            .generate(p_236313_ -> Variant.variant().with(VariantProperties.MODEL, aresourcelocation[p_236313_]))
                    )
            );
        this.delegateItemModel(Items.RESPAWN_ANCHOR, aresourcelocation[0]);
    }

    private Variant applyRotation(FrontAndTop frontAndTop, Variant variant) {
        switch (frontAndTop) {
            case DOWN_NORTH:
                return variant.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90);
            case DOWN_SOUTH:
                return variant.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180);
            case DOWN_WEST:
                return variant.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270);
            case DOWN_EAST:
                return variant.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
            case UP_NORTH:
                return variant.with(VariantProperties.X_ROT, VariantProperties.Rotation.R270).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180);
            case UP_SOUTH:
                return variant.with(VariantProperties.X_ROT, VariantProperties.Rotation.R270);
            case UP_WEST:
                return variant.with(VariantProperties.X_ROT, VariantProperties.Rotation.R270).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
            case UP_EAST:
                return variant.with(VariantProperties.X_ROT, VariantProperties.Rotation.R270).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270);
            case NORTH_UP:
                return variant;
            case SOUTH_UP:
                return variant.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180);
            case WEST_UP:
                return variant.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270);
            case EAST_UP:
                return variant.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
            default:
                throw new UnsupportedOperationException("Rotation " + frontAndTop + " can't be expressed with existing x and y values");
        }
    }

    private void createJigsaw() {
        ResourceLocation resourcelocation = TextureMapping.getBlockTexture(Blocks.JIGSAW, "_top");
        ResourceLocation resourcelocation1 = TextureMapping.getBlockTexture(Blocks.JIGSAW, "_bottom");
        ResourceLocation resourcelocation2 = TextureMapping.getBlockTexture(Blocks.JIGSAW, "_side");
        ResourceLocation resourcelocation3 = TextureMapping.getBlockTexture(Blocks.JIGSAW, "_lock");
        TextureMapping texturemapping = new TextureMapping()
            .put(TextureSlot.DOWN, resourcelocation2)
            .put(TextureSlot.WEST, resourcelocation2)
            .put(TextureSlot.EAST, resourcelocation2)
            .put(TextureSlot.PARTICLE, resourcelocation)
            .put(TextureSlot.NORTH, resourcelocation)
            .put(TextureSlot.SOUTH, resourcelocation1)
            .put(TextureSlot.UP, resourcelocation3);
        ResourceLocation resourcelocation4 = ModelTemplates.CUBE_DIRECTIONAL.create(Blocks.JIGSAW, texturemapping, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.JIGSAW, Variant.variant().with(VariantProperties.MODEL, resourcelocation4))
                    .with(PropertyDispatch.property(BlockStateProperties.ORIENTATION).generate(p_307147_ -> this.applyRotation(p_307147_, Variant.variant())))
            );
    }

    private void createPetrifiedOakSlab() {
        Block block = Blocks.OAK_PLANKS;
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(block);
        TexturedModel texturedmodel = TexturedModel.CUBE.get(block);
        Block block1 = Blocks.PETRIFIED_OAK_SLAB;
        ResourceLocation resourcelocation1 = ModelTemplates.SLAB_BOTTOM.create(block1, texturedmodel.getMapping(), this.modelOutput);
        ResourceLocation resourcelocation2 = ModelTemplates.SLAB_TOP.create(block1, texturedmodel.getMapping(), this.modelOutput);
        this.blockStateOutput.accept(createSlab(block1, resourcelocation1, resourcelocation2, resourcelocation));
    }

    public void run() {
        BlockFamilies.getAllFamilies()
            .filter(BlockFamily::shouldGenerateModel)
            .forEach(p_236303_ -> this.family(p_236303_.getBaseBlock()).generateFor(p_236303_));
        this.family(Blocks.CUT_COPPER)
            .generateFor(BlockFamilies.CUT_COPPER)
            .donateModelTo(Blocks.CUT_COPPER, Blocks.WAXED_CUT_COPPER)
            .donateModelTo(Blocks.CHISELED_COPPER, Blocks.WAXED_CHISELED_COPPER)
            .generateFor(BlockFamilies.WAXED_CUT_COPPER);
        this.family(Blocks.EXPOSED_CUT_COPPER)
            .generateFor(BlockFamilies.EXPOSED_CUT_COPPER)
            .donateModelTo(Blocks.EXPOSED_CUT_COPPER, Blocks.WAXED_EXPOSED_CUT_COPPER)
            .donateModelTo(Blocks.EXPOSED_CHISELED_COPPER, Blocks.WAXED_EXPOSED_CHISELED_COPPER)
            .generateFor(BlockFamilies.WAXED_EXPOSED_CUT_COPPER);
        this.family(Blocks.WEATHERED_CUT_COPPER)
            .generateFor(BlockFamilies.WEATHERED_CUT_COPPER)
            .donateModelTo(Blocks.WEATHERED_CUT_COPPER, Blocks.WAXED_WEATHERED_CUT_COPPER)
            .donateModelTo(Blocks.WEATHERED_CHISELED_COPPER, Blocks.WAXED_WEATHERED_CHISELED_COPPER)
            .generateFor(BlockFamilies.WAXED_WEATHERED_CUT_COPPER);
        this.family(Blocks.OXIDIZED_CUT_COPPER)
            .generateFor(BlockFamilies.OXIDIZED_CUT_COPPER)
            .donateModelTo(Blocks.OXIDIZED_CUT_COPPER, Blocks.WAXED_OXIDIZED_CUT_COPPER)
            .donateModelTo(Blocks.OXIDIZED_CHISELED_COPPER, Blocks.WAXED_OXIDIZED_CHISELED_COPPER)
            .generateFor(BlockFamilies.WAXED_OXIDIZED_CUT_COPPER);
        this.createCopperBulb(Blocks.COPPER_BULB);
        this.createCopperBulb(Blocks.EXPOSED_COPPER_BULB);
        this.createCopperBulb(Blocks.WEATHERED_COPPER_BULB);
        this.createCopperBulb(Blocks.OXIDIZED_COPPER_BULB);
        this.copyCopperBulbModel(Blocks.COPPER_BULB, Blocks.WAXED_COPPER_BULB);
        this.copyCopperBulbModel(Blocks.EXPOSED_COPPER_BULB, Blocks.WAXED_EXPOSED_COPPER_BULB);
        this.copyCopperBulbModel(Blocks.WEATHERED_COPPER_BULB, Blocks.WAXED_WEATHERED_COPPER_BULB);
        this.copyCopperBulbModel(Blocks.OXIDIZED_COPPER_BULB, Blocks.WAXED_OXIDIZED_COPPER_BULB);
        this.createNonTemplateModelBlock(Blocks.AIR);
        this.createNonTemplateModelBlock(Blocks.CAVE_AIR, Blocks.AIR);
        this.createNonTemplateModelBlock(Blocks.VOID_AIR, Blocks.AIR);
        this.createNonTemplateModelBlock(Blocks.BEACON);
        this.createNonTemplateModelBlock(Blocks.CACTUS);
        this.createNonTemplateModelBlock(Blocks.BUBBLE_COLUMN, Blocks.WATER);
        this.createNonTemplateModelBlock(Blocks.DRAGON_EGG);
        this.createNonTemplateModelBlock(Blocks.DRIED_KELP_BLOCK);
        this.createNonTemplateModelBlock(Blocks.ENCHANTING_TABLE);
        this.createNonTemplateModelBlock(Blocks.FLOWER_POT);
        this.createSimpleFlatItemModel(Items.FLOWER_POT);
        this.createNonTemplateModelBlock(Blocks.HONEY_BLOCK);
        this.createNonTemplateModelBlock(Blocks.WATER);
        this.createNonTemplateModelBlock(Blocks.LAVA);
        this.createNonTemplateModelBlock(Blocks.SLIME_BLOCK);
        this.createSimpleFlatItemModel(Items.CHAIN);
        this.createCandleAndCandleCake(Blocks.WHITE_CANDLE, Blocks.WHITE_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.ORANGE_CANDLE, Blocks.ORANGE_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.MAGENTA_CANDLE, Blocks.MAGENTA_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.LIGHT_BLUE_CANDLE, Blocks.LIGHT_BLUE_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.YELLOW_CANDLE, Blocks.YELLOW_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.LIME_CANDLE, Blocks.LIME_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.PINK_CANDLE, Blocks.PINK_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.GRAY_CANDLE, Blocks.GRAY_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.LIGHT_GRAY_CANDLE, Blocks.LIGHT_GRAY_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.CYAN_CANDLE, Blocks.CYAN_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.PURPLE_CANDLE, Blocks.PURPLE_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.BLUE_CANDLE, Blocks.BLUE_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.BROWN_CANDLE, Blocks.BROWN_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.GREEN_CANDLE, Blocks.GREEN_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.RED_CANDLE, Blocks.RED_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.BLACK_CANDLE, Blocks.BLACK_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.CANDLE, Blocks.CANDLE_CAKE);
        this.createNonTemplateModelBlock(Blocks.POTTED_BAMBOO);
        this.createNonTemplateModelBlock(Blocks.POTTED_CACTUS);
        this.createNonTemplateModelBlock(Blocks.POWDER_SNOW);
        this.createNonTemplateModelBlock(Blocks.SPORE_BLOSSOM);
        this.createAzalea(Blocks.AZALEA);
        this.createAzalea(Blocks.FLOWERING_AZALEA);
        this.createPottedAzalea(Blocks.POTTED_AZALEA);
        this.createPottedAzalea(Blocks.POTTED_FLOWERING_AZALEA);
        this.createCaveVines();
        this.createFullAndCarpetBlocks(Blocks.MOSS_BLOCK, Blocks.MOSS_CARPET);
        this.createFlowerBed(Blocks.PINK_PETALS);
        this.createAirLikeBlock(Blocks.BARRIER, Items.BARRIER);
        this.createSimpleFlatItemModel(Items.BARRIER);
        this.createLightBlock();
        this.createAirLikeBlock(Blocks.STRUCTURE_VOID, Items.STRUCTURE_VOID);
        this.createSimpleFlatItemModel(Items.STRUCTURE_VOID);
        this.createAirLikeBlock(Blocks.MOVING_PISTON, TextureMapping.getBlockTexture(Blocks.PISTON, "_side"));
        this.createTrivialCube(Blocks.COAL_ORE);
        this.createTrivialCube(Blocks.DEEPSLATE_COAL_ORE);
        this.createTrivialCube(Blocks.COAL_BLOCK);
        this.createTrivialCube(Blocks.DIAMOND_ORE);
        this.createTrivialCube(Blocks.DEEPSLATE_DIAMOND_ORE);
        this.createTrivialCube(Blocks.DIAMOND_BLOCK);
        this.createTrivialCube(Blocks.EMERALD_ORE);
        this.createTrivialCube(Blocks.DEEPSLATE_EMERALD_ORE);
        this.createTrivialCube(Blocks.EMERALD_BLOCK);
        this.createTrivialCube(Blocks.GOLD_ORE);
        this.createTrivialCube(Blocks.NETHER_GOLD_ORE);
        this.createTrivialCube(Blocks.DEEPSLATE_GOLD_ORE);
        this.createTrivialCube(Blocks.GOLD_BLOCK);
        this.createTrivialCube(Blocks.IRON_ORE);
        this.createTrivialCube(Blocks.DEEPSLATE_IRON_ORE);
        this.createTrivialCube(Blocks.IRON_BLOCK);
        this.createTrivialBlock(Blocks.ANCIENT_DEBRIS, TexturedModel.COLUMN);
        this.createTrivialCube(Blocks.NETHERITE_BLOCK);
        this.createTrivialCube(Blocks.LAPIS_ORE);
        this.createTrivialCube(Blocks.DEEPSLATE_LAPIS_ORE);
        this.createTrivialCube(Blocks.LAPIS_BLOCK);
        this.createTrivialCube(Blocks.NETHER_QUARTZ_ORE);
        this.createTrivialCube(Blocks.REDSTONE_ORE);
        this.createTrivialCube(Blocks.DEEPSLATE_REDSTONE_ORE);
        this.createTrivialCube(Blocks.REDSTONE_BLOCK);
        this.createTrivialCube(Blocks.GILDED_BLACKSTONE);
        this.createTrivialCube(Blocks.BLUE_ICE);
        this.createTrivialCube(Blocks.CLAY);
        this.createTrivialCube(Blocks.COARSE_DIRT);
        this.createTrivialCube(Blocks.CRYING_OBSIDIAN);
        this.createTrivialCube(Blocks.END_STONE);
        this.createTrivialCube(Blocks.GLOWSTONE);
        this.createTrivialCube(Blocks.GRAVEL);
        this.createTrivialCube(Blocks.HONEYCOMB_BLOCK);
        this.createTrivialCube(Blocks.ICE);
        this.createTrivialBlock(Blocks.JUKEBOX, TexturedModel.CUBE_TOP);
        this.createTrivialBlock(Blocks.LODESTONE, TexturedModel.COLUMN);
        this.createTrivialBlock(Blocks.MELON, TexturedModel.COLUMN);
        this.createNonTemplateModelBlock(Blocks.MANGROVE_ROOTS);
        this.createNonTemplateModelBlock(Blocks.POTTED_MANGROVE_PROPAGULE);
        this.createTrivialCube(Blocks.NETHER_WART_BLOCK);
        this.createTrivialCube(Blocks.NOTE_BLOCK);
        this.createTrivialCube(Blocks.PACKED_ICE);
        this.createTrivialCube(Blocks.OBSIDIAN);
        this.createTrivialCube(Blocks.QUARTZ_BRICKS);
        this.createTrivialCube(Blocks.SEA_LANTERN);
        this.createTrivialCube(Blocks.SHROOMLIGHT);
        this.createTrivialCube(Blocks.SOUL_SAND);
        this.createTrivialCube(Blocks.SOUL_SOIL);
        this.createTrivialBlock(Blocks.SPAWNER, TexturedModel.CUBE_INNER_FACES);
        this.createTrivialCube(Blocks.SPONGE);
        this.createTrivialBlock(Blocks.SEAGRASS, TexturedModel.SEAGRASS);
        this.createSimpleFlatItemModel(Items.SEAGRASS);
        this.createTrivialBlock(Blocks.TNT, TexturedModel.CUBE_TOP_BOTTOM);
        this.createTrivialBlock(Blocks.TARGET, TexturedModel.COLUMN);
        this.createTrivialCube(Blocks.WARPED_WART_BLOCK);
        this.createTrivialCube(Blocks.WET_SPONGE);
        this.createTrivialCube(Blocks.AMETHYST_BLOCK);
        this.createTrivialCube(Blocks.BUDDING_AMETHYST);
        this.createTrivialCube(Blocks.CALCITE);
        this.createTrivialCube(Blocks.DRIPSTONE_BLOCK);
        this.createTrivialCube(Blocks.RAW_IRON_BLOCK);
        this.createTrivialCube(Blocks.RAW_COPPER_BLOCK);
        this.createTrivialCube(Blocks.RAW_GOLD_BLOCK);
        this.createRotatedMirroredVariantBlock(Blocks.SCULK);
        this.createNonTemplateModelBlock(Blocks.HEAVY_CORE);
        this.createPetrifiedOakSlab();
        this.createTrivialCube(Blocks.COPPER_ORE);
        this.createTrivialCube(Blocks.DEEPSLATE_COPPER_ORE);
        this.createTrivialCube(Blocks.COPPER_BLOCK);
        this.createTrivialCube(Blocks.EXPOSED_COPPER);
        this.createTrivialCube(Blocks.WEATHERED_COPPER);
        this.createTrivialCube(Blocks.OXIDIZED_COPPER);
        this.copyModel(Blocks.COPPER_BLOCK, Blocks.WAXED_COPPER_BLOCK);
        this.copyModel(Blocks.EXPOSED_COPPER, Blocks.WAXED_EXPOSED_COPPER);
        this.copyModel(Blocks.WEATHERED_COPPER, Blocks.WAXED_WEATHERED_COPPER);
        this.copyModel(Blocks.OXIDIZED_COPPER, Blocks.WAXED_OXIDIZED_COPPER);
        this.createDoor(Blocks.COPPER_DOOR);
        this.createDoor(Blocks.EXPOSED_COPPER_DOOR);
        this.createDoor(Blocks.WEATHERED_COPPER_DOOR);
        this.createDoor(Blocks.OXIDIZED_COPPER_DOOR);
        this.copyDoorModel(Blocks.COPPER_DOOR, Blocks.WAXED_COPPER_DOOR);
        this.copyDoorModel(Blocks.EXPOSED_COPPER_DOOR, Blocks.WAXED_EXPOSED_COPPER_DOOR);
        this.copyDoorModel(Blocks.WEATHERED_COPPER_DOOR, Blocks.WAXED_WEATHERED_COPPER_DOOR);
        this.copyDoorModel(Blocks.OXIDIZED_COPPER_DOOR, Blocks.WAXED_OXIDIZED_COPPER_DOOR);
        this.createTrapdoor(Blocks.COPPER_TRAPDOOR);
        this.createTrapdoor(Blocks.EXPOSED_COPPER_TRAPDOOR);
        this.createTrapdoor(Blocks.WEATHERED_COPPER_TRAPDOOR);
        this.createTrapdoor(Blocks.OXIDIZED_COPPER_TRAPDOOR);
        this.copyTrapdoorModel(Blocks.COPPER_TRAPDOOR, Blocks.WAXED_COPPER_TRAPDOOR);
        this.copyTrapdoorModel(Blocks.EXPOSED_COPPER_TRAPDOOR, Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR);
        this.copyTrapdoorModel(Blocks.WEATHERED_COPPER_TRAPDOOR, Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR);
        this.copyTrapdoorModel(Blocks.OXIDIZED_COPPER_TRAPDOOR, Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR);
        this.createTrivialCube(Blocks.COPPER_GRATE);
        this.createTrivialCube(Blocks.EXPOSED_COPPER_GRATE);
        this.createTrivialCube(Blocks.WEATHERED_COPPER_GRATE);
        this.createTrivialCube(Blocks.OXIDIZED_COPPER_GRATE);
        this.copyModel(Blocks.COPPER_GRATE, Blocks.WAXED_COPPER_GRATE);
        this.copyModel(Blocks.EXPOSED_COPPER_GRATE, Blocks.WAXED_EXPOSED_COPPER_GRATE);
        this.copyModel(Blocks.WEATHERED_COPPER_GRATE, Blocks.WAXED_WEATHERED_COPPER_GRATE);
        this.copyModel(Blocks.OXIDIZED_COPPER_GRATE, Blocks.WAXED_OXIDIZED_COPPER_GRATE);
        this.createWeightedPressurePlate(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, Blocks.GOLD_BLOCK);
        this.createWeightedPressurePlate(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, Blocks.IRON_BLOCK);
        this.createAmethystClusters();
        this.createBookshelf();
        this.createChiseledBookshelf();
        this.createBrewingStand();
        this.createCakeBlock();
        this.createCampfires(Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE);
        this.createCartographyTable();
        this.createCauldrons();
        this.createChorusFlower();
        this.createChorusPlant();
        this.createComposter();
        this.createDaylightDetector();
        this.createEndPortalFrame();
        this.createRotatableColumn(Blocks.END_ROD);
        this.createLightningRod();
        this.createFarmland();
        this.createFire();
        this.createSoulFire();
        this.createFrostedIce();
        this.createGrassBlocks();
        this.createCocoa();
        this.createDirtPath();
        this.createGrindstone();
        this.createHopper();
        this.createIronBars();
        this.createLever();
        this.createLilyPad();
        this.createNetherPortalBlock();
        this.createNetherrack();
        this.createObserver();
        this.createPistons();
        this.createPistonHeads();
        this.createScaffolding();
        this.createRedstoneTorch();
        this.createRedstoneLamp();
        this.createRepeater();
        this.createSeaPickle();
        this.createSmithingTable();
        this.createSnowBlocks();
        this.createStonecutter();
        this.createStructureBlock();
        this.createSweetBerryBush();
        this.createTripwire();
        this.createTripwireHook();
        this.createTurtleEgg();
        this.createSnifferEgg();
        this.createMultiface(Blocks.VINE);
        this.createMultiface(Blocks.GLOW_LICHEN);
        this.createMultiface(Blocks.SCULK_VEIN);
        this.createMagmaBlock();
        this.createJigsaw();
        this.createSculkSensor();
        this.createCalibratedSculkSensor();
        this.createSculkShrieker();
        this.createFrogspawnBlock();
        this.createMangrovePropagule();
        this.createMuddyMangroveRoots();
        this.createTrialSpawner();
        this.createVault();
        this.createNonTemplateHorizontalBlock(Blocks.LADDER);
        this.createSimpleFlatItemModel(Blocks.LADDER);
        this.createNonTemplateHorizontalBlock(Blocks.LECTERN);
        this.createBigDripLeafBlock();
        this.createNonTemplateHorizontalBlock(Blocks.BIG_DRIPLEAF_STEM);
        this.createNormalTorch(Blocks.TORCH, Blocks.WALL_TORCH);
        this.createNormalTorch(Blocks.SOUL_TORCH, Blocks.SOUL_WALL_TORCH);
        this.createCraftingTableLike(Blocks.CRAFTING_TABLE, Blocks.OAK_PLANKS, TextureMapping::craftingTable);
        this.createCraftingTableLike(Blocks.FLETCHING_TABLE, Blocks.BIRCH_PLANKS, TextureMapping::fletchingTable);
        this.createNyliumBlock(Blocks.CRIMSON_NYLIUM);
        this.createNyliumBlock(Blocks.WARPED_NYLIUM);
        this.createDispenserBlock(Blocks.DISPENSER);
        this.createDispenserBlock(Blocks.DROPPER);
        this.createCrafterBlock();
        this.createLantern(Blocks.LANTERN);
        this.createLantern(Blocks.SOUL_LANTERN);
        this.createAxisAlignedPillarBlockCustomModel(Blocks.CHAIN, ModelLocationUtils.getModelLocation(Blocks.CHAIN));
        this.createAxisAlignedPillarBlock(Blocks.BASALT, TexturedModel.COLUMN);
        this.createAxisAlignedPillarBlock(Blocks.POLISHED_BASALT, TexturedModel.COLUMN);
        this.createTrivialCube(Blocks.SMOOTH_BASALT);
        this.createAxisAlignedPillarBlock(Blocks.BONE_BLOCK, TexturedModel.COLUMN);
        this.createRotatedVariantBlock(Blocks.DIRT);
        this.createRotatedVariantBlock(Blocks.ROOTED_DIRT);
        this.createRotatedVariantBlock(Blocks.SAND);
        this.createBrushableBlock(Blocks.SUSPICIOUS_SAND);
        this.createBrushableBlock(Blocks.SUSPICIOUS_GRAVEL);
        this.createRotatedVariantBlock(Blocks.RED_SAND);
        this.createRotatedMirroredVariantBlock(Blocks.BEDROCK);
        this.createTrivialBlock(Blocks.REINFORCED_DEEPSLATE, TexturedModel.CUBE_TOP_BOTTOM);
        this.createRotatedPillarWithHorizontalVariant(Blocks.HAY_BLOCK, TexturedModel.COLUMN, TexturedModel.COLUMN_HORIZONTAL);
        this.createRotatedPillarWithHorizontalVariant(Blocks.PURPUR_PILLAR, TexturedModel.COLUMN_ALT, TexturedModel.COLUMN_HORIZONTAL_ALT);
        this.createRotatedPillarWithHorizontalVariant(Blocks.QUARTZ_PILLAR, TexturedModel.COLUMN_ALT, TexturedModel.COLUMN_HORIZONTAL_ALT);
        this.createRotatedPillarWithHorizontalVariant(Blocks.OCHRE_FROGLIGHT, TexturedModel.COLUMN, TexturedModel.COLUMN_HORIZONTAL);
        this.createRotatedPillarWithHorizontalVariant(Blocks.VERDANT_FROGLIGHT, TexturedModel.COLUMN, TexturedModel.COLUMN_HORIZONTAL);
        this.createRotatedPillarWithHorizontalVariant(Blocks.PEARLESCENT_FROGLIGHT, TexturedModel.COLUMN, TexturedModel.COLUMN_HORIZONTAL);
        this.createHorizontallyRotatedBlock(Blocks.LOOM, TexturedModel.ORIENTABLE);
        this.createPumpkins();
        this.createBeeNest(Blocks.BEE_NEST, TextureMapping::orientableCube);
        this.createBeeNest(Blocks.BEEHIVE, TextureMapping::orientableCubeSameEnds);
        this.createCropBlock(Blocks.BEETROOTS, BlockStateProperties.AGE_3, 0, 1, 2, 3);
        this.createCropBlock(Blocks.CARROTS, BlockStateProperties.AGE_7, 0, 0, 1, 1, 2, 2, 2, 3);
        this.createCropBlock(Blocks.NETHER_WART, BlockStateProperties.AGE_3, 0, 1, 1, 2);
        this.createCropBlock(Blocks.POTATOES, BlockStateProperties.AGE_7, 0, 0, 1, 1, 2, 2, 2, 3);
        this.createCropBlock(Blocks.WHEAT, BlockStateProperties.AGE_7, 0, 1, 2, 3, 4, 5, 6, 7);
        this.createCrossBlock(Blocks.TORCHFLOWER_CROP, BlockModelGenerators.TintState.NOT_TINTED, BlockStateProperties.AGE_1, 0, 1);
        this.createPitcherCrop();
        this.createPitcherPlant();
        this.blockEntityModels(ModelLocationUtils.decorateBlockModelLocation("decorated_pot"), Blocks.TERRACOTTA).createWithoutBlockItem(Blocks.DECORATED_POT);
        this.blockEntityModels(ModelLocationUtils.decorateBlockModelLocation("banner"), Blocks.OAK_PLANKS)
            .createWithCustomBlockItemModel(
                ModelTemplates.BANNER_INVENTORY,
                Blocks.WHITE_BANNER,
                Blocks.ORANGE_BANNER,
                Blocks.MAGENTA_BANNER,
                Blocks.LIGHT_BLUE_BANNER,
                Blocks.YELLOW_BANNER,
                Blocks.LIME_BANNER,
                Blocks.PINK_BANNER,
                Blocks.GRAY_BANNER,
                Blocks.LIGHT_GRAY_BANNER,
                Blocks.CYAN_BANNER,
                Blocks.PURPLE_BANNER,
                Blocks.BLUE_BANNER,
                Blocks.BROWN_BANNER,
                Blocks.GREEN_BANNER,
                Blocks.RED_BANNER,
                Blocks.BLACK_BANNER
            )
            .createWithoutBlockItem(
                Blocks.WHITE_WALL_BANNER,
                Blocks.ORANGE_WALL_BANNER,
                Blocks.MAGENTA_WALL_BANNER,
                Blocks.LIGHT_BLUE_WALL_BANNER,
                Blocks.YELLOW_WALL_BANNER,
                Blocks.LIME_WALL_BANNER,
                Blocks.PINK_WALL_BANNER,
                Blocks.GRAY_WALL_BANNER,
                Blocks.LIGHT_GRAY_WALL_BANNER,
                Blocks.CYAN_WALL_BANNER,
                Blocks.PURPLE_WALL_BANNER,
                Blocks.BLUE_WALL_BANNER,
                Blocks.BROWN_WALL_BANNER,
                Blocks.GREEN_WALL_BANNER,
                Blocks.RED_WALL_BANNER,
                Blocks.BLACK_WALL_BANNER
            );
        this.blockEntityModels(ModelLocationUtils.decorateBlockModelLocation("bed"), Blocks.OAK_PLANKS)
            .createWithoutBlockItem(
                Blocks.WHITE_BED,
                Blocks.ORANGE_BED,
                Blocks.MAGENTA_BED,
                Blocks.LIGHT_BLUE_BED,
                Blocks.YELLOW_BED,
                Blocks.LIME_BED,
                Blocks.PINK_BED,
                Blocks.GRAY_BED,
                Blocks.LIGHT_GRAY_BED,
                Blocks.CYAN_BED,
                Blocks.PURPLE_BED,
                Blocks.BLUE_BED,
                Blocks.BROWN_BED,
                Blocks.GREEN_BED,
                Blocks.RED_BED,
                Blocks.BLACK_BED
            );
        this.createBedItem(Blocks.WHITE_BED, Blocks.WHITE_WOOL);
        this.createBedItem(Blocks.ORANGE_BED, Blocks.ORANGE_WOOL);
        this.createBedItem(Blocks.MAGENTA_BED, Blocks.MAGENTA_WOOL);
        this.createBedItem(Blocks.LIGHT_BLUE_BED, Blocks.LIGHT_BLUE_WOOL);
        this.createBedItem(Blocks.YELLOW_BED, Blocks.YELLOW_WOOL);
        this.createBedItem(Blocks.LIME_BED, Blocks.LIME_WOOL);
        this.createBedItem(Blocks.PINK_BED, Blocks.PINK_WOOL);
        this.createBedItem(Blocks.GRAY_BED, Blocks.GRAY_WOOL);
        this.createBedItem(Blocks.LIGHT_GRAY_BED, Blocks.LIGHT_GRAY_WOOL);
        this.createBedItem(Blocks.CYAN_BED, Blocks.CYAN_WOOL);
        this.createBedItem(Blocks.PURPLE_BED, Blocks.PURPLE_WOOL);
        this.createBedItem(Blocks.BLUE_BED, Blocks.BLUE_WOOL);
        this.createBedItem(Blocks.BROWN_BED, Blocks.BROWN_WOOL);
        this.createBedItem(Blocks.GREEN_BED, Blocks.GREEN_WOOL);
        this.createBedItem(Blocks.RED_BED, Blocks.RED_WOOL);
        this.createBedItem(Blocks.BLACK_BED, Blocks.BLACK_WOOL);
        this.blockEntityModels(ModelLocationUtils.decorateBlockModelLocation("skull"), Blocks.SOUL_SAND)
            .createWithCustomBlockItemModel(
                ModelTemplates.SKULL_INVENTORY,
                Blocks.CREEPER_HEAD,
                Blocks.PLAYER_HEAD,
                Blocks.ZOMBIE_HEAD,
                Blocks.SKELETON_SKULL,
                Blocks.WITHER_SKELETON_SKULL,
                Blocks.PIGLIN_HEAD
            )
            .create(Blocks.DRAGON_HEAD)
            .createWithoutBlockItem(
                Blocks.CREEPER_WALL_HEAD,
                Blocks.DRAGON_WALL_HEAD,
                Blocks.PLAYER_WALL_HEAD,
                Blocks.ZOMBIE_WALL_HEAD,
                Blocks.SKELETON_WALL_SKULL,
                Blocks.WITHER_SKELETON_WALL_SKULL,
                Blocks.PIGLIN_WALL_HEAD
            );
        this.createShulkerBox(Blocks.SHULKER_BOX);
        this.createShulkerBox(Blocks.WHITE_SHULKER_BOX);
        this.createShulkerBox(Blocks.ORANGE_SHULKER_BOX);
        this.createShulkerBox(Blocks.MAGENTA_SHULKER_BOX);
        this.createShulkerBox(Blocks.LIGHT_BLUE_SHULKER_BOX);
        this.createShulkerBox(Blocks.YELLOW_SHULKER_BOX);
        this.createShulkerBox(Blocks.LIME_SHULKER_BOX);
        this.createShulkerBox(Blocks.PINK_SHULKER_BOX);
        this.createShulkerBox(Blocks.GRAY_SHULKER_BOX);
        this.createShulkerBox(Blocks.LIGHT_GRAY_SHULKER_BOX);
        this.createShulkerBox(Blocks.CYAN_SHULKER_BOX);
        this.createShulkerBox(Blocks.PURPLE_SHULKER_BOX);
        this.createShulkerBox(Blocks.BLUE_SHULKER_BOX);
        this.createShulkerBox(Blocks.BROWN_SHULKER_BOX);
        this.createShulkerBox(Blocks.GREEN_SHULKER_BOX);
        this.createShulkerBox(Blocks.RED_SHULKER_BOX);
        this.createShulkerBox(Blocks.BLACK_SHULKER_BOX);
        this.createTrivialBlock(Blocks.CONDUIT, TexturedModel.PARTICLE_ONLY);
        this.skipAutoItemBlock(Blocks.CONDUIT);
        this.blockEntityModels(ModelLocationUtils.decorateBlockModelLocation("chest"), Blocks.OAK_PLANKS)
            .createWithoutBlockItem(Blocks.CHEST, Blocks.TRAPPED_CHEST);
        this.blockEntityModels(ModelLocationUtils.decorateBlockModelLocation("ender_chest"), Blocks.OBSIDIAN).createWithoutBlockItem(Blocks.ENDER_CHEST);
        this.blockEntityModels(Blocks.END_PORTAL, Blocks.OBSIDIAN).create(Blocks.END_PORTAL, Blocks.END_GATEWAY);
        this.createTrivialCube(Blocks.AZALEA_LEAVES);
        this.createTrivialCube(Blocks.FLOWERING_AZALEA_LEAVES);
        this.createTrivialCube(Blocks.WHITE_CONCRETE);
        this.createTrivialCube(Blocks.ORANGE_CONCRETE);
        this.createTrivialCube(Blocks.MAGENTA_CONCRETE);
        this.createTrivialCube(Blocks.LIGHT_BLUE_CONCRETE);
        this.createTrivialCube(Blocks.YELLOW_CONCRETE);
        this.createTrivialCube(Blocks.LIME_CONCRETE);
        this.createTrivialCube(Blocks.PINK_CONCRETE);
        this.createTrivialCube(Blocks.GRAY_CONCRETE);
        this.createTrivialCube(Blocks.LIGHT_GRAY_CONCRETE);
        this.createTrivialCube(Blocks.CYAN_CONCRETE);
        this.createTrivialCube(Blocks.PURPLE_CONCRETE);
        this.createTrivialCube(Blocks.BLUE_CONCRETE);
        this.createTrivialCube(Blocks.BROWN_CONCRETE);
        this.createTrivialCube(Blocks.GREEN_CONCRETE);
        this.createTrivialCube(Blocks.RED_CONCRETE);
        this.createTrivialCube(Blocks.BLACK_CONCRETE);
        this.createColoredBlockWithRandomRotations(
            TexturedModel.CUBE,
            Blocks.WHITE_CONCRETE_POWDER,
            Blocks.ORANGE_CONCRETE_POWDER,
            Blocks.MAGENTA_CONCRETE_POWDER,
            Blocks.LIGHT_BLUE_CONCRETE_POWDER,
            Blocks.YELLOW_CONCRETE_POWDER,
            Blocks.LIME_CONCRETE_POWDER,
            Blocks.PINK_CONCRETE_POWDER,
            Blocks.GRAY_CONCRETE_POWDER,
            Blocks.LIGHT_GRAY_CONCRETE_POWDER,
            Blocks.CYAN_CONCRETE_POWDER,
            Blocks.PURPLE_CONCRETE_POWDER,
            Blocks.BLUE_CONCRETE_POWDER,
            Blocks.BROWN_CONCRETE_POWDER,
            Blocks.GREEN_CONCRETE_POWDER,
            Blocks.RED_CONCRETE_POWDER,
            Blocks.BLACK_CONCRETE_POWDER
        );
        this.createTrivialCube(Blocks.TERRACOTTA);
        this.createTrivialCube(Blocks.WHITE_TERRACOTTA);
        this.createTrivialCube(Blocks.ORANGE_TERRACOTTA);
        this.createTrivialCube(Blocks.MAGENTA_TERRACOTTA);
        this.createTrivialCube(Blocks.LIGHT_BLUE_TERRACOTTA);
        this.createTrivialCube(Blocks.YELLOW_TERRACOTTA);
        this.createTrivialCube(Blocks.LIME_TERRACOTTA);
        this.createTrivialCube(Blocks.PINK_TERRACOTTA);
        this.createTrivialCube(Blocks.GRAY_TERRACOTTA);
        this.createTrivialCube(Blocks.LIGHT_GRAY_TERRACOTTA);
        this.createTrivialCube(Blocks.CYAN_TERRACOTTA);
        this.createTrivialCube(Blocks.PURPLE_TERRACOTTA);
        this.createTrivialCube(Blocks.BLUE_TERRACOTTA);
        this.createTrivialCube(Blocks.BROWN_TERRACOTTA);
        this.createTrivialCube(Blocks.GREEN_TERRACOTTA);
        this.createTrivialCube(Blocks.RED_TERRACOTTA);
        this.createTrivialCube(Blocks.BLACK_TERRACOTTA);
        this.createTrivialCube(Blocks.TINTED_GLASS);
        this.createGlassBlocks(Blocks.GLASS, Blocks.GLASS_PANE);
        this.createGlassBlocks(Blocks.WHITE_STAINED_GLASS, Blocks.WHITE_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.ORANGE_STAINED_GLASS, Blocks.ORANGE_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.MAGENTA_STAINED_GLASS, Blocks.MAGENTA_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.LIGHT_BLUE_STAINED_GLASS, Blocks.LIGHT_BLUE_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.YELLOW_STAINED_GLASS, Blocks.YELLOW_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.LIME_STAINED_GLASS, Blocks.LIME_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.PINK_STAINED_GLASS, Blocks.PINK_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.GRAY_STAINED_GLASS, Blocks.GRAY_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.LIGHT_GRAY_STAINED_GLASS, Blocks.LIGHT_GRAY_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.CYAN_STAINED_GLASS, Blocks.CYAN_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.PURPLE_STAINED_GLASS, Blocks.PURPLE_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.BLUE_STAINED_GLASS, Blocks.BLUE_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.BROWN_STAINED_GLASS, Blocks.BROWN_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.GREEN_STAINED_GLASS, Blocks.GREEN_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.RED_STAINED_GLASS, Blocks.RED_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.BLACK_STAINED_GLASS, Blocks.BLACK_STAINED_GLASS_PANE);
        this.createColoredBlockWithStateRotations(
            TexturedModel.GLAZED_TERRACOTTA,
            Blocks.WHITE_GLAZED_TERRACOTTA,
            Blocks.ORANGE_GLAZED_TERRACOTTA,
            Blocks.MAGENTA_GLAZED_TERRACOTTA,
            Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA,
            Blocks.YELLOW_GLAZED_TERRACOTTA,
            Blocks.LIME_GLAZED_TERRACOTTA,
            Blocks.PINK_GLAZED_TERRACOTTA,
            Blocks.GRAY_GLAZED_TERRACOTTA,
            Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA,
            Blocks.CYAN_GLAZED_TERRACOTTA,
            Blocks.PURPLE_GLAZED_TERRACOTTA,
            Blocks.BLUE_GLAZED_TERRACOTTA,
            Blocks.BROWN_GLAZED_TERRACOTTA,
            Blocks.GREEN_GLAZED_TERRACOTTA,
            Blocks.RED_GLAZED_TERRACOTTA,
            Blocks.BLACK_GLAZED_TERRACOTTA
        );
        this.createFullAndCarpetBlocks(Blocks.WHITE_WOOL, Blocks.WHITE_CARPET);
        this.createFullAndCarpetBlocks(Blocks.ORANGE_WOOL, Blocks.ORANGE_CARPET);
        this.createFullAndCarpetBlocks(Blocks.MAGENTA_WOOL, Blocks.MAGENTA_CARPET);
        this.createFullAndCarpetBlocks(Blocks.LIGHT_BLUE_WOOL, Blocks.LIGHT_BLUE_CARPET);
        this.createFullAndCarpetBlocks(Blocks.YELLOW_WOOL, Blocks.YELLOW_CARPET);
        this.createFullAndCarpetBlocks(Blocks.LIME_WOOL, Blocks.LIME_CARPET);
        this.createFullAndCarpetBlocks(Blocks.PINK_WOOL, Blocks.PINK_CARPET);
        this.createFullAndCarpetBlocks(Blocks.GRAY_WOOL, Blocks.GRAY_CARPET);
        this.createFullAndCarpetBlocks(Blocks.LIGHT_GRAY_WOOL, Blocks.LIGHT_GRAY_CARPET);
        this.createFullAndCarpetBlocks(Blocks.CYAN_WOOL, Blocks.CYAN_CARPET);
        this.createFullAndCarpetBlocks(Blocks.PURPLE_WOOL, Blocks.PURPLE_CARPET);
        this.createFullAndCarpetBlocks(Blocks.BLUE_WOOL, Blocks.BLUE_CARPET);
        this.createFullAndCarpetBlocks(Blocks.BROWN_WOOL, Blocks.BROWN_CARPET);
        this.createFullAndCarpetBlocks(Blocks.GREEN_WOOL, Blocks.GREEN_CARPET);
        this.createFullAndCarpetBlocks(Blocks.RED_WOOL, Blocks.RED_CARPET);
        this.createFullAndCarpetBlocks(Blocks.BLACK_WOOL, Blocks.BLACK_CARPET);
        this.createTrivialCube(Blocks.MUD);
        this.createTrivialCube(Blocks.PACKED_MUD);
        this.createPlant(Blocks.FERN, Blocks.POTTED_FERN, BlockModelGenerators.TintState.TINTED);
        this.createPlant(Blocks.DANDELION, Blocks.POTTED_DANDELION, BlockModelGenerators.TintState.NOT_TINTED);
        this.createPlant(Blocks.POPPY, Blocks.POTTED_POPPY, BlockModelGenerators.TintState.NOT_TINTED);
        this.createPlant(Blocks.BLUE_ORCHID, Blocks.POTTED_BLUE_ORCHID, BlockModelGenerators.TintState.NOT_TINTED);
        this.createPlant(Blocks.ALLIUM, Blocks.POTTED_ALLIUM, BlockModelGenerators.TintState.NOT_TINTED);
        this.createPlant(Blocks.AZURE_BLUET, Blocks.POTTED_AZURE_BLUET, BlockModelGenerators.TintState.NOT_TINTED);
        this.createPlant(Blocks.RED_TULIP, Blocks.POTTED_RED_TULIP, BlockModelGenerators.TintState.NOT_TINTED);
        this.createPlant(Blocks.ORANGE_TULIP, Blocks.POTTED_ORANGE_TULIP, BlockModelGenerators.TintState.NOT_TINTED);
        this.createPlant(Blocks.WHITE_TULIP, Blocks.POTTED_WHITE_TULIP, BlockModelGenerators.TintState.NOT_TINTED);
        this.createPlant(Blocks.PINK_TULIP, Blocks.POTTED_PINK_TULIP, BlockModelGenerators.TintState.NOT_TINTED);
        this.createPlant(Blocks.OXEYE_DAISY, Blocks.POTTED_OXEYE_DAISY, BlockModelGenerators.TintState.NOT_TINTED);
        this.createPlant(Blocks.CORNFLOWER, Blocks.POTTED_CORNFLOWER, BlockModelGenerators.TintState.NOT_TINTED);
        this.createPlant(Blocks.LILY_OF_THE_VALLEY, Blocks.POTTED_LILY_OF_THE_VALLEY, BlockModelGenerators.TintState.NOT_TINTED);
        this.createPlant(Blocks.WITHER_ROSE, Blocks.POTTED_WITHER_ROSE, BlockModelGenerators.TintState.NOT_TINTED);
        this.createPlant(Blocks.RED_MUSHROOM, Blocks.POTTED_RED_MUSHROOM, BlockModelGenerators.TintState.NOT_TINTED);
        this.createPlant(Blocks.BROWN_MUSHROOM, Blocks.POTTED_BROWN_MUSHROOM, BlockModelGenerators.TintState.NOT_TINTED);
        this.createPlant(Blocks.DEAD_BUSH, Blocks.POTTED_DEAD_BUSH, BlockModelGenerators.TintState.NOT_TINTED);
        this.createPlant(Blocks.TORCHFLOWER, Blocks.POTTED_TORCHFLOWER, BlockModelGenerators.TintState.NOT_TINTED);
        this.createPointedDripstone();
        this.createMushroomBlock(Blocks.BROWN_MUSHROOM_BLOCK);
        this.createMushroomBlock(Blocks.RED_MUSHROOM_BLOCK);
        this.createMushroomBlock(Blocks.MUSHROOM_STEM);
        this.createCrossBlockWithDefaultItem(Blocks.SHORT_GRASS, BlockModelGenerators.TintState.TINTED);
        this.createCrossBlock(Blocks.SUGAR_CANE, BlockModelGenerators.TintState.TINTED);
        this.createSimpleFlatItemModel(Items.SUGAR_CANE);
        this.createGrowingPlant(Blocks.KELP, Blocks.KELP_PLANT, BlockModelGenerators.TintState.NOT_TINTED);
        this.createSimpleFlatItemModel(Items.KELP);
        this.skipAutoItemBlock(Blocks.KELP_PLANT);
        this.createCrossBlock(Blocks.HANGING_ROOTS, BlockModelGenerators.TintState.NOT_TINTED);
        this.skipAutoItemBlock(Blocks.HANGING_ROOTS);
        this.skipAutoItemBlock(Blocks.CAVE_VINES_PLANT);
        this.createGrowingPlant(Blocks.WEEPING_VINES, Blocks.WEEPING_VINES_PLANT, BlockModelGenerators.TintState.NOT_TINTED);
        this.createGrowingPlant(Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT, BlockModelGenerators.TintState.NOT_TINTED);
        this.createSimpleFlatItemModel(Blocks.WEEPING_VINES, "_plant");
        this.skipAutoItemBlock(Blocks.WEEPING_VINES_PLANT);
        this.createSimpleFlatItemModel(Blocks.TWISTING_VINES, "_plant");
        this.skipAutoItemBlock(Blocks.TWISTING_VINES_PLANT);
        this.createCrossBlockWithDefaultItem(
            Blocks.BAMBOO_SAPLING, BlockModelGenerators.TintState.TINTED, TextureMapping.cross(TextureMapping.getBlockTexture(Blocks.BAMBOO, "_stage0"))
        );
        this.createBamboo();
        this.createCrossBlockWithDefaultItem(Blocks.COBWEB, BlockModelGenerators.TintState.NOT_TINTED);
        this.createDoublePlant(Blocks.LILAC, BlockModelGenerators.TintState.NOT_TINTED);
        this.createDoublePlant(Blocks.ROSE_BUSH, BlockModelGenerators.TintState.NOT_TINTED);
        this.createDoublePlant(Blocks.PEONY, BlockModelGenerators.TintState.NOT_TINTED);
        this.createDoublePlant(Blocks.TALL_GRASS, BlockModelGenerators.TintState.TINTED);
        this.createDoublePlant(Blocks.LARGE_FERN, BlockModelGenerators.TintState.TINTED);
        this.createSunflower();
        this.createTallSeagrass();
        this.createSmallDripleaf();
        this.createCoral(
            Blocks.TUBE_CORAL,
            Blocks.DEAD_TUBE_CORAL,
            Blocks.TUBE_CORAL_BLOCK,
            Blocks.DEAD_TUBE_CORAL_BLOCK,
            Blocks.TUBE_CORAL_FAN,
            Blocks.DEAD_TUBE_CORAL_FAN,
            Blocks.TUBE_CORAL_WALL_FAN,
            Blocks.DEAD_TUBE_CORAL_WALL_FAN
        );
        this.createCoral(
            Blocks.BRAIN_CORAL,
            Blocks.DEAD_BRAIN_CORAL,
            Blocks.BRAIN_CORAL_BLOCK,
            Blocks.DEAD_BRAIN_CORAL_BLOCK,
            Blocks.BRAIN_CORAL_FAN,
            Blocks.DEAD_BRAIN_CORAL_FAN,
            Blocks.BRAIN_CORAL_WALL_FAN,
            Blocks.DEAD_BRAIN_CORAL_WALL_FAN
        );
        this.createCoral(
            Blocks.BUBBLE_CORAL,
            Blocks.DEAD_BUBBLE_CORAL,
            Blocks.BUBBLE_CORAL_BLOCK,
            Blocks.DEAD_BUBBLE_CORAL_BLOCK,
            Blocks.BUBBLE_CORAL_FAN,
            Blocks.DEAD_BUBBLE_CORAL_FAN,
            Blocks.BUBBLE_CORAL_WALL_FAN,
            Blocks.DEAD_BUBBLE_CORAL_WALL_FAN
        );
        this.createCoral(
            Blocks.FIRE_CORAL,
            Blocks.DEAD_FIRE_CORAL,
            Blocks.FIRE_CORAL_BLOCK,
            Blocks.DEAD_FIRE_CORAL_BLOCK,
            Blocks.FIRE_CORAL_FAN,
            Blocks.DEAD_FIRE_CORAL_FAN,
            Blocks.FIRE_CORAL_WALL_FAN,
            Blocks.DEAD_FIRE_CORAL_WALL_FAN
        );
        this.createCoral(
            Blocks.HORN_CORAL,
            Blocks.DEAD_HORN_CORAL,
            Blocks.HORN_CORAL_BLOCK,
            Blocks.DEAD_HORN_CORAL_BLOCK,
            Blocks.HORN_CORAL_FAN,
            Blocks.DEAD_HORN_CORAL_FAN,
            Blocks.HORN_CORAL_WALL_FAN,
            Blocks.DEAD_HORN_CORAL_WALL_FAN
        );
        this.createStems(Blocks.MELON_STEM, Blocks.ATTACHED_MELON_STEM);
        this.createStems(Blocks.PUMPKIN_STEM, Blocks.ATTACHED_PUMPKIN_STEM);
        this.woodProvider(Blocks.MANGROVE_LOG).logWithHorizontal(Blocks.MANGROVE_LOG).wood(Blocks.MANGROVE_WOOD);
        this.woodProvider(Blocks.STRIPPED_MANGROVE_LOG).logWithHorizontal(Blocks.STRIPPED_MANGROVE_LOG).wood(Blocks.STRIPPED_MANGROVE_WOOD);
        this.createHangingSign(Blocks.STRIPPED_MANGROVE_LOG, Blocks.MANGROVE_HANGING_SIGN, Blocks.MANGROVE_WALL_HANGING_SIGN);
        this.createTrivialBlock(Blocks.MANGROVE_LEAVES, TexturedModel.LEAVES);
        this.woodProvider(Blocks.ACACIA_LOG).logWithHorizontal(Blocks.ACACIA_LOG).wood(Blocks.ACACIA_WOOD);
        this.woodProvider(Blocks.STRIPPED_ACACIA_LOG).logWithHorizontal(Blocks.STRIPPED_ACACIA_LOG).wood(Blocks.STRIPPED_ACACIA_WOOD);
        this.createHangingSign(Blocks.STRIPPED_ACACIA_LOG, Blocks.ACACIA_HANGING_SIGN, Blocks.ACACIA_WALL_HANGING_SIGN);
        this.createPlant(Blocks.ACACIA_SAPLING, Blocks.POTTED_ACACIA_SAPLING, BlockModelGenerators.TintState.NOT_TINTED);
        this.createTrivialBlock(Blocks.ACACIA_LEAVES, TexturedModel.LEAVES);
        this.woodProvider(Blocks.CHERRY_LOG).logUVLocked(Blocks.CHERRY_LOG).wood(Blocks.CHERRY_WOOD);
        this.woodProvider(Blocks.STRIPPED_CHERRY_LOG).logUVLocked(Blocks.STRIPPED_CHERRY_LOG).wood(Blocks.STRIPPED_CHERRY_WOOD);
        this.createHangingSign(Blocks.STRIPPED_CHERRY_LOG, Blocks.CHERRY_HANGING_SIGN, Blocks.CHERRY_WALL_HANGING_SIGN);
        this.createPlant(Blocks.CHERRY_SAPLING, Blocks.POTTED_CHERRY_SAPLING, BlockModelGenerators.TintState.NOT_TINTED);
        this.createTrivialBlock(Blocks.CHERRY_LEAVES, TexturedModel.LEAVES);
        this.woodProvider(Blocks.BIRCH_LOG).logWithHorizontal(Blocks.BIRCH_LOG).wood(Blocks.BIRCH_WOOD);
        this.woodProvider(Blocks.STRIPPED_BIRCH_LOG).logWithHorizontal(Blocks.STRIPPED_BIRCH_LOG).wood(Blocks.STRIPPED_BIRCH_WOOD);
        this.createHangingSign(Blocks.STRIPPED_BIRCH_LOG, Blocks.BIRCH_HANGING_SIGN, Blocks.BIRCH_WALL_HANGING_SIGN);
        this.createPlant(Blocks.BIRCH_SAPLING, Blocks.POTTED_BIRCH_SAPLING, BlockModelGenerators.TintState.NOT_TINTED);
        this.createTrivialBlock(Blocks.BIRCH_LEAVES, TexturedModel.LEAVES);
        this.woodProvider(Blocks.OAK_LOG).logWithHorizontal(Blocks.OAK_LOG).wood(Blocks.OAK_WOOD);
        this.woodProvider(Blocks.STRIPPED_OAK_LOG).logWithHorizontal(Blocks.STRIPPED_OAK_LOG).wood(Blocks.STRIPPED_OAK_WOOD);
        this.createHangingSign(Blocks.STRIPPED_OAK_LOG, Blocks.OAK_HANGING_SIGN, Blocks.OAK_WALL_HANGING_SIGN);
        this.createPlant(Blocks.OAK_SAPLING, Blocks.POTTED_OAK_SAPLING, BlockModelGenerators.TintState.NOT_TINTED);
        this.createTrivialBlock(Blocks.OAK_LEAVES, TexturedModel.LEAVES);
        this.woodProvider(Blocks.SPRUCE_LOG).logWithHorizontal(Blocks.SPRUCE_LOG).wood(Blocks.SPRUCE_WOOD);
        this.woodProvider(Blocks.STRIPPED_SPRUCE_LOG).logWithHorizontal(Blocks.STRIPPED_SPRUCE_LOG).wood(Blocks.STRIPPED_SPRUCE_WOOD);
        this.createHangingSign(Blocks.STRIPPED_SPRUCE_LOG, Blocks.SPRUCE_HANGING_SIGN, Blocks.SPRUCE_WALL_HANGING_SIGN);
        this.createPlant(Blocks.SPRUCE_SAPLING, Blocks.POTTED_SPRUCE_SAPLING, BlockModelGenerators.TintState.NOT_TINTED);
        this.createTrivialBlock(Blocks.SPRUCE_LEAVES, TexturedModel.LEAVES);
        this.woodProvider(Blocks.DARK_OAK_LOG).logWithHorizontal(Blocks.DARK_OAK_LOG).wood(Blocks.DARK_OAK_WOOD);
        this.woodProvider(Blocks.STRIPPED_DARK_OAK_LOG).logWithHorizontal(Blocks.STRIPPED_DARK_OAK_LOG).wood(Blocks.STRIPPED_DARK_OAK_WOOD);
        this.createHangingSign(Blocks.STRIPPED_DARK_OAK_LOG, Blocks.DARK_OAK_HANGING_SIGN, Blocks.DARK_OAK_WALL_HANGING_SIGN);
        this.createPlant(Blocks.DARK_OAK_SAPLING, Blocks.POTTED_DARK_OAK_SAPLING, BlockModelGenerators.TintState.NOT_TINTED);
        this.createTrivialBlock(Blocks.DARK_OAK_LEAVES, TexturedModel.LEAVES);
        this.woodProvider(Blocks.JUNGLE_LOG).logWithHorizontal(Blocks.JUNGLE_LOG).wood(Blocks.JUNGLE_WOOD);
        this.woodProvider(Blocks.STRIPPED_JUNGLE_LOG).logWithHorizontal(Blocks.STRIPPED_JUNGLE_LOG).wood(Blocks.STRIPPED_JUNGLE_WOOD);
        this.createHangingSign(Blocks.STRIPPED_JUNGLE_LOG, Blocks.JUNGLE_HANGING_SIGN, Blocks.JUNGLE_WALL_HANGING_SIGN);
        this.createPlant(Blocks.JUNGLE_SAPLING, Blocks.POTTED_JUNGLE_SAPLING, BlockModelGenerators.TintState.NOT_TINTED);
        this.createTrivialBlock(Blocks.JUNGLE_LEAVES, TexturedModel.LEAVES);
        this.woodProvider(Blocks.CRIMSON_STEM).log(Blocks.CRIMSON_STEM).wood(Blocks.CRIMSON_HYPHAE);
        this.woodProvider(Blocks.STRIPPED_CRIMSON_STEM).log(Blocks.STRIPPED_CRIMSON_STEM).wood(Blocks.STRIPPED_CRIMSON_HYPHAE);
        this.createHangingSign(Blocks.STRIPPED_CRIMSON_STEM, Blocks.CRIMSON_HANGING_SIGN, Blocks.CRIMSON_WALL_HANGING_SIGN);
        this.createPlant(Blocks.CRIMSON_FUNGUS, Blocks.POTTED_CRIMSON_FUNGUS, BlockModelGenerators.TintState.NOT_TINTED);
        this.createNetherRoots(Blocks.CRIMSON_ROOTS, Blocks.POTTED_CRIMSON_ROOTS);
        this.woodProvider(Blocks.WARPED_STEM).log(Blocks.WARPED_STEM).wood(Blocks.WARPED_HYPHAE);
        this.woodProvider(Blocks.STRIPPED_WARPED_STEM).log(Blocks.STRIPPED_WARPED_STEM).wood(Blocks.STRIPPED_WARPED_HYPHAE);
        this.createHangingSign(Blocks.STRIPPED_WARPED_STEM, Blocks.WARPED_HANGING_SIGN, Blocks.WARPED_WALL_HANGING_SIGN);
        this.createPlant(Blocks.WARPED_FUNGUS, Blocks.POTTED_WARPED_FUNGUS, BlockModelGenerators.TintState.NOT_TINTED);
        this.createNetherRoots(Blocks.WARPED_ROOTS, Blocks.POTTED_WARPED_ROOTS);
        this.woodProvider(Blocks.BAMBOO_BLOCK).logUVLocked(Blocks.BAMBOO_BLOCK);
        this.woodProvider(Blocks.STRIPPED_BAMBOO_BLOCK).logUVLocked(Blocks.STRIPPED_BAMBOO_BLOCK);
        this.createHangingSign(Blocks.BAMBOO_PLANKS, Blocks.BAMBOO_HANGING_SIGN, Blocks.BAMBOO_WALL_HANGING_SIGN);
        this.createCrossBlock(Blocks.NETHER_SPROUTS, BlockModelGenerators.TintState.NOT_TINTED);
        this.createSimpleFlatItemModel(Items.NETHER_SPROUTS);
        this.createDoor(Blocks.IRON_DOOR);
        this.createTrapdoor(Blocks.IRON_TRAPDOOR);
        this.createSmoothStoneSlab();
        this.createPassiveRail(Blocks.RAIL);
        this.createActiveRail(Blocks.POWERED_RAIL);
        this.createActiveRail(Blocks.DETECTOR_RAIL);
        this.createActiveRail(Blocks.ACTIVATOR_RAIL);
        this.createComparator();
        this.createCommandBlock(Blocks.COMMAND_BLOCK);
        this.createCommandBlock(Blocks.REPEATING_COMMAND_BLOCK);
        this.createCommandBlock(Blocks.CHAIN_COMMAND_BLOCK);
        this.createAnvil(Blocks.ANVIL);
        this.createAnvil(Blocks.CHIPPED_ANVIL);
        this.createAnvil(Blocks.DAMAGED_ANVIL);
        this.createBarrel();
        this.createBell();
        this.createFurnace(Blocks.FURNACE, TexturedModel.ORIENTABLE_ONLY_TOP);
        this.createFurnace(Blocks.BLAST_FURNACE, TexturedModel.ORIENTABLE_ONLY_TOP);
        this.createFurnace(Blocks.SMOKER, TexturedModel.ORIENTABLE);
        this.createRedstoneWire();
        this.createRespawnAnchor();
        this.createSculkCatalyst();
        this.copyModel(Blocks.CHISELED_STONE_BRICKS, Blocks.INFESTED_CHISELED_STONE_BRICKS);
        this.copyModel(Blocks.COBBLESTONE, Blocks.INFESTED_COBBLESTONE);
        this.copyModel(Blocks.CRACKED_STONE_BRICKS, Blocks.INFESTED_CRACKED_STONE_BRICKS);
        this.copyModel(Blocks.MOSSY_STONE_BRICKS, Blocks.INFESTED_MOSSY_STONE_BRICKS);
        this.createInfestedStone();
        this.copyModel(Blocks.STONE_BRICKS, Blocks.INFESTED_STONE_BRICKS);
        this.createInfestedDeepslate();
        SpawnEggItem.eggs().forEach(p_236282_ -> this.delegateItemModel(p_236282_, ModelLocationUtils.decorateItemModelLocation("template_spawn_egg")));
    }

    private void createLightBlock() {
        this.skipAutoItemBlock(Blocks.LIGHT);
        PropertyDispatch.C1<Integer> c1 = PropertyDispatch.property(BlockStateProperties.LEVEL);

        for (int i = 0; i < 16; i++) {
            String s = String.format(Locale.ROOT, "_%02d", i);
            ResourceLocation resourcelocation = TextureMapping.getItemTexture(Items.LIGHT, s);
            c1.select(
                i,
                Variant.variant()
                    .with(
                        VariantProperties.MODEL,
                        ModelTemplates.PARTICLE_ONLY.createWithSuffix(Blocks.LIGHT, s, TextureMapping.particle(resourcelocation), this.modelOutput)
                    )
            );
            ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(Items.LIGHT, s), TextureMapping.layer0(resourcelocation), this.modelOutput);
        }

        this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(Blocks.LIGHT).with(c1));
    }

    private void createCandleAndCandleCake(Block candleBlock, Block candleCakeBlock) {
        this.createSimpleFlatItemModel(candleBlock.asItem());
        TextureMapping texturemapping = TextureMapping.cube(TextureMapping.getBlockTexture(candleBlock));
        TextureMapping texturemapping1 = TextureMapping.cube(TextureMapping.getBlockTexture(candleBlock, "_lit"));
        ResourceLocation resourcelocation = ModelTemplates.CANDLE.createWithSuffix(candleBlock, "_one_candle", texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.TWO_CANDLES.createWithSuffix(candleBlock, "_two_candles", texturemapping, this.modelOutput);
        ResourceLocation resourcelocation2 = ModelTemplates.THREE_CANDLES.createWithSuffix(candleBlock, "_three_candles", texturemapping, this.modelOutput);
        ResourceLocation resourcelocation3 = ModelTemplates.FOUR_CANDLES.createWithSuffix(candleBlock, "_four_candles", texturemapping, this.modelOutput);
        ResourceLocation resourcelocation4 = ModelTemplates.CANDLE.createWithSuffix(candleBlock, "_one_candle_lit", texturemapping1, this.modelOutput);
        ResourceLocation resourcelocation5 = ModelTemplates.TWO_CANDLES.createWithSuffix(candleBlock, "_two_candles_lit", texturemapping1, this.modelOutput);
        ResourceLocation resourcelocation6 = ModelTemplates.THREE_CANDLES.createWithSuffix(candleBlock, "_three_candles_lit", texturemapping1, this.modelOutput);
        ResourceLocation resourcelocation7 = ModelTemplates.FOUR_CANDLES.createWithSuffix(candleBlock, "_four_candles_lit", texturemapping1, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(candleBlock)
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.CANDLES, BlockStateProperties.LIT)
                            .select(1, false, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                            .select(2, false, Variant.variant().with(VariantProperties.MODEL, resourcelocation1))
                            .select(3, false, Variant.variant().with(VariantProperties.MODEL, resourcelocation2))
                            .select(4, false, Variant.variant().with(VariantProperties.MODEL, resourcelocation3))
                            .select(1, true, Variant.variant().with(VariantProperties.MODEL, resourcelocation4))
                            .select(2, true, Variant.variant().with(VariantProperties.MODEL, resourcelocation5))
                            .select(3, true, Variant.variant().with(VariantProperties.MODEL, resourcelocation6))
                            .select(4, true, Variant.variant().with(VariantProperties.MODEL, resourcelocation7))
                    )
            );
        ResourceLocation resourcelocation8 = ModelTemplates.CANDLE_CAKE.create(candleCakeBlock, TextureMapping.candleCake(candleBlock, false), this.modelOutput);
        ResourceLocation resourcelocation9 = ModelTemplates.CANDLE_CAKE
            .createWithSuffix(candleCakeBlock, "_lit", TextureMapping.candleCake(candleBlock, true), this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(candleCakeBlock).with(createBooleanModelDispatch(BlockStateProperties.LIT, resourcelocation9, resourcelocation8))
            );
    }

    class BlockEntityModelGenerator {
        private final ResourceLocation baseModel;

        public BlockEntityModelGenerator(ResourceLocation baseModel, Block particleBlock) {
            this.baseModel = ModelTemplates.PARTICLE_ONLY.create(baseModel, TextureMapping.particle(particleBlock), BlockModelGenerators.this.modelOutput);
        }

        public BlockModelGenerators.BlockEntityModelGenerator create(Block... blocks) {
            for (Block block : blocks) {
                BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, this.baseModel));
            }

            return this;
        }

        public BlockModelGenerators.BlockEntityModelGenerator createWithoutBlockItem(Block... blocks) {
            for (Block block : blocks) {
                BlockModelGenerators.this.skipAutoItemBlock(block);
            }

            return this.create(blocks);
        }

        public BlockModelGenerators.BlockEntityModelGenerator createWithCustomBlockItemModel(ModelTemplate modelTemplate, Block... blocks) {
            for (Block block : blocks) {
                modelTemplate.create(ModelLocationUtils.getModelLocation(block.asItem()), TextureMapping.particle(block), BlockModelGenerators.this.modelOutput);
            }

            return this.create(blocks);
        }
    }

    class BlockFamilyProvider {
        private final TextureMapping mapping;
        private final Map<ModelTemplate, ResourceLocation> models = Maps.newHashMap();
        @Nullable
        private BlockFamily family;
        @Nullable
        private ResourceLocation fullBlock;
        private final Set<Block> skipGeneratingModelsFor = new HashSet<>();

        public BlockFamilyProvider(TextureMapping mapping) {
            this.mapping = mapping;
        }

        public BlockModelGenerators.BlockFamilyProvider fullBlock(Block block, ModelTemplate modelTemplate) {
            this.fullBlock = modelTemplate.create(block, this.mapping, BlockModelGenerators.this.modelOutput);
            if (BlockModelGenerators.this.fullBlockModelCustomGenerators.containsKey(block)) {
                BlockModelGenerators.this.blockStateOutput
                    .accept(
                        BlockModelGenerators.this.fullBlockModelCustomGenerators
                            .get(block)
                            .create(block, this.fullBlock, this.mapping, BlockModelGenerators.this.modelOutput)
                    );
            } else {
                BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, this.fullBlock));
            }

            return this;
        }

        public BlockModelGenerators.BlockFamilyProvider donateModelTo(Block sourceBlock, Block block) {
            ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(sourceBlock);
            BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, resourcelocation));
            BlockModelGenerators.this.delegateItemModel(block, resourcelocation);
            this.skipGeneratingModelsFor.add(block);
            return this;
        }

        public BlockModelGenerators.BlockFamilyProvider button(Block buttonBlock) {
            ResourceLocation resourcelocation = ModelTemplates.BUTTON.create(buttonBlock, this.mapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation1 = ModelTemplates.BUTTON_PRESSED.create(buttonBlock, this.mapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createButton(buttonBlock, resourcelocation, resourcelocation1));
            ResourceLocation resourcelocation2 = ModelTemplates.BUTTON_INVENTORY.create(buttonBlock, this.mapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.delegateItemModel(buttonBlock, resourcelocation2);
            return this;
        }

        public BlockModelGenerators.BlockFamilyProvider wall(Block wallBlock) {
            ResourceLocation resourcelocation = ModelTemplates.WALL_POST.create(wallBlock, this.mapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation1 = ModelTemplates.WALL_LOW_SIDE.create(wallBlock, this.mapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation2 = ModelTemplates.WALL_TALL_SIDE.create(wallBlock, this.mapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput
                .accept(BlockModelGenerators.createWall(wallBlock, resourcelocation, resourcelocation1, resourcelocation2));
            ResourceLocation resourcelocation3 = ModelTemplates.WALL_INVENTORY.create(wallBlock, this.mapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.delegateItemModel(wallBlock, resourcelocation3);
            return this;
        }

        public BlockModelGenerators.BlockFamilyProvider customFence(Block fenceBlock) {
            TextureMapping texturemapping = TextureMapping.customParticle(fenceBlock);
            ResourceLocation resourcelocation = ModelTemplates.CUSTOM_FENCE_POST.create(fenceBlock, texturemapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation1 = ModelTemplates.CUSTOM_FENCE_SIDE_NORTH
                .create(fenceBlock, texturemapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation2 = ModelTemplates.CUSTOM_FENCE_SIDE_EAST.create(fenceBlock, texturemapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation3 = ModelTemplates.CUSTOM_FENCE_SIDE_SOUTH
                .create(fenceBlock, texturemapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation4 = ModelTemplates.CUSTOM_FENCE_SIDE_WEST.create(fenceBlock, texturemapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput
                .accept(
                    BlockModelGenerators.createCustomFence(
                        fenceBlock, resourcelocation, resourcelocation1, resourcelocation2, resourcelocation3, resourcelocation4
                    )
                );
            ResourceLocation resourcelocation5 = ModelTemplates.CUSTOM_FENCE_INVENTORY.create(fenceBlock, texturemapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.delegateItemModel(fenceBlock, resourcelocation5);
            return this;
        }

        public BlockModelGenerators.BlockFamilyProvider fence(Block fenceBlock) {
            ResourceLocation resourcelocation = ModelTemplates.FENCE_POST.create(fenceBlock, this.mapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation1 = ModelTemplates.FENCE_SIDE.create(fenceBlock, this.mapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createFence(fenceBlock, resourcelocation, resourcelocation1));
            ResourceLocation resourcelocation2 = ModelTemplates.FENCE_INVENTORY.create(fenceBlock, this.mapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.delegateItemModel(fenceBlock, resourcelocation2);
            return this;
        }

        public BlockModelGenerators.BlockFamilyProvider customFenceGate(Block customFenceGateBlock) {
            TextureMapping texturemapping = TextureMapping.customParticle(customFenceGateBlock);
            ResourceLocation resourcelocation = ModelTemplates.CUSTOM_FENCE_GATE_OPEN.create(customFenceGateBlock, texturemapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation1 = ModelTemplates.CUSTOM_FENCE_GATE_CLOSED
                .create(customFenceGateBlock, texturemapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation2 = ModelTemplates.CUSTOM_FENCE_GATE_WALL_OPEN
                .create(customFenceGateBlock, texturemapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation3 = ModelTemplates.CUSTOM_FENCE_GATE_WALL_CLOSED
                .create(customFenceGateBlock, texturemapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput
                .accept(BlockModelGenerators.createFenceGate(customFenceGateBlock, resourcelocation, resourcelocation1, resourcelocation2, resourcelocation3, false));
            return this;
        }

        public BlockModelGenerators.BlockFamilyProvider fenceGate(Block fenceGateBlock) {
            ResourceLocation resourcelocation = ModelTemplates.FENCE_GATE_OPEN.create(fenceGateBlock, this.mapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation1 = ModelTemplates.FENCE_GATE_CLOSED.create(fenceGateBlock, this.mapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation2 = ModelTemplates.FENCE_GATE_WALL_OPEN.create(fenceGateBlock, this.mapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation3 = ModelTemplates.FENCE_GATE_WALL_CLOSED.create(fenceGateBlock, this.mapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput
                .accept(BlockModelGenerators.createFenceGate(fenceGateBlock, resourcelocation, resourcelocation1, resourcelocation2, resourcelocation3, true));
            return this;
        }

        public BlockModelGenerators.BlockFamilyProvider pressurePlate(Block pressurePlateBlock) {
            ResourceLocation resourcelocation = ModelTemplates.PRESSURE_PLATE_UP.create(pressurePlateBlock, this.mapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation1 = ModelTemplates.PRESSURE_PLATE_DOWN.create(pressurePlateBlock, this.mapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createPressurePlate(pressurePlateBlock, resourcelocation, resourcelocation1));
            return this;
        }

        public BlockModelGenerators.BlockFamilyProvider sign(Block signBlock) {
            if (this.family == null) {
                throw new IllegalStateException("Family not defined");
            } else {
                Block block = this.family.getVariants().get(BlockFamily.Variant.WALL_SIGN);
                ResourceLocation resourcelocation = ModelTemplates.PARTICLE_ONLY.create(signBlock, this.mapping, BlockModelGenerators.this.modelOutput);
                BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(signBlock, resourcelocation));
                BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, resourcelocation));
                BlockModelGenerators.this.createSimpleFlatItemModel(signBlock.asItem());
                BlockModelGenerators.this.skipAutoItemBlock(block);
                return this;
            }
        }

        public BlockModelGenerators.BlockFamilyProvider slab(Block slabBlock) {
            if (this.fullBlock == null) {
                throw new IllegalStateException("Full block not generated yet");
            } else {
                ResourceLocation resourcelocation = this.getOrCreateModel(ModelTemplates.SLAB_BOTTOM, slabBlock);
                ResourceLocation resourcelocation1 = this.getOrCreateModel(ModelTemplates.SLAB_TOP, slabBlock);
                BlockModelGenerators.this.blockStateOutput
                    .accept(BlockModelGenerators.createSlab(slabBlock, resourcelocation, resourcelocation1, this.fullBlock));
                BlockModelGenerators.this.delegateItemModel(slabBlock, resourcelocation);
                return this;
            }
        }

        public BlockModelGenerators.BlockFamilyProvider stairs(Block stairsBlock) {
            ResourceLocation resourcelocation = this.getOrCreateModel(ModelTemplates.STAIRS_INNER, stairsBlock);
            ResourceLocation resourcelocation1 = this.getOrCreateModel(ModelTemplates.STAIRS_STRAIGHT, stairsBlock);
            ResourceLocation resourcelocation2 = this.getOrCreateModel(ModelTemplates.STAIRS_OUTER, stairsBlock);
            BlockModelGenerators.this.blockStateOutput
                .accept(BlockModelGenerators.createStairs(stairsBlock, resourcelocation, resourcelocation1, resourcelocation2));
            BlockModelGenerators.this.delegateItemModel(stairsBlock, resourcelocation1);
            return this;
        }

        private BlockModelGenerators.BlockFamilyProvider fullBlockVariant(Block block) {
            TexturedModel texturedmodel = BlockModelGenerators.this.texturedModels.getOrDefault(block, TexturedModel.CUBE.get(block));
            ResourceLocation resourcelocation = texturedmodel.create(block, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, resourcelocation));
            return this;
        }

        private BlockModelGenerators.BlockFamilyProvider door(Block doorBlock) {
            BlockModelGenerators.this.createDoor(doorBlock);
            return this;
        }

        private void trapdoor(Block trapdoorBlock) {
            if (BlockModelGenerators.this.nonOrientableTrapdoor.contains(trapdoorBlock)) {
                BlockModelGenerators.this.createTrapdoor(trapdoorBlock);
            } else {
                BlockModelGenerators.this.createOrientableTrapdoor(trapdoorBlock);
            }
        }

        private ResourceLocation getOrCreateModel(ModelTemplate modelTemplate, Block block) {
            return this.models.computeIfAbsent(modelTemplate, p_176268_ -> p_176268_.create(block, this.mapping, BlockModelGenerators.this.modelOutput));
        }

        public BlockModelGenerators.BlockFamilyProvider generateFor(BlockFamily family) {
            this.family = family;
            family.getVariants().forEach((p_308473_, p_308474_) -> {
                if (!this.skipGeneratingModelsFor.contains(p_308474_)) {
                    BiConsumer<BlockModelGenerators.BlockFamilyProvider, Block> biconsumer = BlockModelGenerators.SHAPE_CONSUMERS.get(p_308473_);
                    if (biconsumer != null) {
                        biconsumer.accept(this, p_308474_);
                    }
                }
            });
            return this;
        }
    }

    @FunctionalInterface
    interface BlockStateGeneratorSupplier {
        BlockStateGenerator create(
            Block block, ResourceLocation modelLocation, TextureMapping textureMapping, BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput
        );
    }

    static record BookSlotModelCacheKey(ModelTemplate template, String modelSuffix) {
    }

    static enum TintState {
        TINTED,
        NOT_TINTED;

        public ModelTemplate getCross() {
            return this == TINTED ? ModelTemplates.TINTED_CROSS : ModelTemplates.CROSS;
        }

        public ModelTemplate getCrossPot() {
            return this == TINTED ? ModelTemplates.TINTED_FLOWER_POT_CROSS : ModelTemplates.FLOWER_POT_CROSS;
        }
    }

    class WoodProvider {
        private final TextureMapping logMapping;

        public WoodProvider(TextureMapping logMapping) {
            this.logMapping = logMapping;
        }

        public BlockModelGenerators.WoodProvider wood(Block woodBlock) {
            TextureMapping texturemapping = this.logMapping.copyAndUpdate(TextureSlot.END, this.logMapping.get(TextureSlot.SIDE));
            ResourceLocation resourcelocation = ModelTemplates.CUBE_COLUMN.create(woodBlock, texturemapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createAxisAlignedPillarBlock(woodBlock, resourcelocation));
            return this;
        }

        public BlockModelGenerators.WoodProvider log(Block logBlock) {
            ResourceLocation resourcelocation = ModelTemplates.CUBE_COLUMN.create(logBlock, this.logMapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createAxisAlignedPillarBlock(logBlock, resourcelocation));
            return this;
        }

        public BlockModelGenerators.WoodProvider logWithHorizontal(Block logBlock) {
            ResourceLocation resourcelocation = ModelTemplates.CUBE_COLUMN.create(logBlock, this.logMapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation1 = ModelTemplates.CUBE_COLUMN_HORIZONTAL
                .create(logBlock, this.logMapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput
                .accept(BlockModelGenerators.createRotatedPillarWithHorizontalVariant(logBlock, resourcelocation, resourcelocation1));
            return this;
        }

        public BlockModelGenerators.WoodProvider logUVLocked(Block logBlock) {
            BlockModelGenerators.this.blockStateOutput
                .accept(BlockModelGenerators.createPillarBlockUVLocked(logBlock, this.logMapping, BlockModelGenerators.this.modelOutput));
            return this;
        }
    }
}
