package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class EndCityPieces {
    private static final int MAX_GEN_DEPTH = 8;
    static final EndCityPieces.SectionGenerator HOUSE_TOWER_GENERATOR = new EndCityPieces.SectionGenerator() {
        @Override
        public void init() {
        }

        @Override
        public boolean generate(
            StructureTemplateManager p_227456_,
            int p_227457_,
            EndCityPieces.EndCityPiece p_227458_,
            BlockPos p_227459_,
            List<StructurePiece> p_227460_,
            RandomSource p_227461_
        ) {
            if (p_227457_ > 8) {
                return false;
            } else {
                Rotation rotation = p_227458_.placeSettings().getRotation();
                EndCityPieces.EndCityPiece endcitypieces$endcitypiece = EndCityPieces.addHelper(
                    p_227460_, EndCityPieces.addPiece(p_227456_, p_227458_, p_227459_, "base_floor", rotation, true)
                );
                int i = p_227461_.nextInt(3);
                if (i == 0) {
                    endcitypieces$endcitypiece = EndCityPieces.addHelper(
                        p_227460_, EndCityPieces.addPiece(p_227456_, endcitypieces$endcitypiece, new BlockPos(-1, 4, -1), "base_roof", rotation, true)
                    );
                } else if (i == 1) {
                    endcitypieces$endcitypiece = EndCityPieces.addHelper(
                        p_227460_, EndCityPieces.addPiece(p_227456_, endcitypieces$endcitypiece, new BlockPos(-1, 0, -1), "second_floor_2", rotation, false)
                    );
                    endcitypieces$endcitypiece = EndCityPieces.addHelper(
                        p_227460_, EndCityPieces.addPiece(p_227456_, endcitypieces$endcitypiece, new BlockPos(-1, 8, -1), "second_roof", rotation, false)
                    );
                    EndCityPieces.recursiveChildren(
                        p_227456_, EndCityPieces.TOWER_GENERATOR, p_227457_ + 1, endcitypieces$endcitypiece, null, p_227460_, p_227461_
                    );
                } else if (i == 2) {
                    endcitypieces$endcitypiece = EndCityPieces.addHelper(
                        p_227460_, EndCityPieces.addPiece(p_227456_, endcitypieces$endcitypiece, new BlockPos(-1, 0, -1), "second_floor_2", rotation, false)
                    );
                    endcitypieces$endcitypiece = EndCityPieces.addHelper(
                        p_227460_, EndCityPieces.addPiece(p_227456_, endcitypieces$endcitypiece, new BlockPos(-1, 4, -1), "third_floor_2", rotation, false)
                    );
                    endcitypieces$endcitypiece = EndCityPieces.addHelper(
                        p_227460_, EndCityPieces.addPiece(p_227456_, endcitypieces$endcitypiece, new BlockPos(-1, 8, -1), "third_roof", rotation, true)
                    );
                    EndCityPieces.recursiveChildren(
                        p_227456_, EndCityPieces.TOWER_GENERATOR, p_227457_ + 1, endcitypieces$endcitypiece, null, p_227460_, p_227461_
                    );
                }

                return true;
            }
        }
    };
    static final List<Tuple<Rotation, BlockPos>> TOWER_BRIDGES = Lists.newArrayList(
        new Tuple<>(Rotation.NONE, new BlockPos(1, -1, 0)),
        new Tuple<>(Rotation.CLOCKWISE_90, new BlockPos(6, -1, 1)),
        new Tuple<>(Rotation.COUNTERCLOCKWISE_90, new BlockPos(0, -1, 5)),
        new Tuple<>(Rotation.CLOCKWISE_180, new BlockPos(5, -1, 6))
    );
    static final EndCityPieces.SectionGenerator TOWER_GENERATOR = new EndCityPieces.SectionGenerator() {
        @Override
        public void init() {
        }

        @Override
        public boolean generate(
            StructureTemplateManager p_227465_,
            int p_227466_,
            EndCityPieces.EndCityPiece p_227467_,
            BlockPos p_227468_,
            List<StructurePiece> p_227469_,
            RandomSource p_227470_
        ) {
            Rotation rotation = p_227467_.placeSettings().getRotation();
            EndCityPieces.EndCityPiece $$7 = EndCityPieces.addHelper(
                p_227469_,
                EndCityPieces.addPiece(p_227465_, p_227467_, new BlockPos(3 + p_227470_.nextInt(2), -3, 3 + p_227470_.nextInt(2)), "tower_base", rotation, true)
            );
            $$7 = EndCityPieces.addHelper(p_227469_, EndCityPieces.addPiece(p_227465_, $$7, new BlockPos(0, 7, 0), "tower_piece", rotation, true));
            EndCityPieces.EndCityPiece endcitypieces$endcitypiece1 = p_227470_.nextInt(3) == 0 ? $$7 : null;
            int i = 1 + p_227470_.nextInt(3);

            for (int j = 0; j < i; j++) {
                $$7 = EndCityPieces.addHelper(p_227469_, EndCityPieces.addPiece(p_227465_, $$7, new BlockPos(0, 4, 0), "tower_piece", rotation, true));
                if (j < i - 1 && p_227470_.nextBoolean()) {
                    endcitypieces$endcitypiece1 = $$7;
                }
            }

            if (endcitypieces$endcitypiece1 != null) {
                for (Tuple<Rotation, BlockPos> tuple : EndCityPieces.TOWER_BRIDGES) {
                    if (p_227470_.nextBoolean()) {
                        EndCityPieces.EndCityPiece endcitypieces$endcitypiece2 = EndCityPieces.addHelper(
                            p_227469_,
                            EndCityPieces.addPiece(p_227465_, endcitypieces$endcitypiece1, tuple.getB(), "bridge_end", rotation.getRotated(tuple.getA()), true)
                        );
                        EndCityPieces.recursiveChildren(
                            p_227465_, EndCityPieces.TOWER_BRIDGE_GENERATOR, p_227466_ + 1, endcitypieces$endcitypiece2, null, p_227469_, p_227470_
                        );
                    }
                }

                $$7 = EndCityPieces.addHelper(p_227469_, EndCityPieces.addPiece(p_227465_, $$7, new BlockPos(-1, 4, -1), "tower_top", rotation, true));
            } else {
                if (p_227466_ != 7) {
                    return EndCityPieces.recursiveChildren(p_227465_, EndCityPieces.FAT_TOWER_GENERATOR, p_227466_ + 1, $$7, null, p_227469_, p_227470_);
                }

                $$7 = EndCityPieces.addHelper(p_227469_, EndCityPieces.addPiece(p_227465_, $$7, new BlockPos(-1, 4, -1), "tower_top", rotation, true));
            }

            return true;
        }
    };
    static final EndCityPieces.SectionGenerator TOWER_BRIDGE_GENERATOR = new EndCityPieces.SectionGenerator() {
        public boolean shipCreated;

        @Override
        public void init() {
            this.shipCreated = false;
        }

        @Override
        public boolean generate(
            StructureTemplateManager p_227475_,
            int p_227476_,
            EndCityPieces.EndCityPiece p_227477_,
            BlockPos p_227478_,
            List<StructurePiece> p_227479_,
            RandomSource p_227480_
        ) {
            Rotation rotation = p_227477_.placeSettings().getRotation();
            int i = p_227480_.nextInt(4) + 1;
            EndCityPieces.EndCityPiece endcitypieces$endcitypiece = EndCityPieces.addHelper(
                p_227479_, EndCityPieces.addPiece(p_227475_, p_227477_, new BlockPos(0, 0, -4), "bridge_piece", rotation, true)
            );
            endcitypieces$endcitypiece.setGenDepth(-1);
            int j = 0;

            for (int k = 0; k < i; k++) {
                if (p_227480_.nextBoolean()) {
                    endcitypieces$endcitypiece = EndCityPieces.addHelper(
                        p_227479_, EndCityPieces.addPiece(p_227475_, endcitypieces$endcitypiece, new BlockPos(0, j, -4), "bridge_piece", rotation, true)
                    );
                    j = 0;
                } else {
                    if (p_227480_.nextBoolean()) {
                        endcitypieces$endcitypiece = EndCityPieces.addHelper(
                            p_227479_,
                            EndCityPieces.addPiece(p_227475_, endcitypieces$endcitypiece, new BlockPos(0, j, -4), "bridge_steep_stairs", rotation, true)
                        );
                    } else {
                        endcitypieces$endcitypiece = EndCityPieces.addHelper(
                            p_227479_,
                            EndCityPieces.addPiece(p_227475_, endcitypieces$endcitypiece, new BlockPos(0, j, -8), "bridge_gentle_stairs", rotation, true)
                        );
                    }

                    j = 4;
                }
            }

            if (!this.shipCreated && p_227480_.nextInt(10 - p_227476_) == 0) {
                EndCityPieces.addHelper(
                    p_227479_,
                    EndCityPieces.addPiece(
                        p_227475_, endcitypieces$endcitypiece, new BlockPos(-8 + p_227480_.nextInt(8), j, -70 + p_227480_.nextInt(10)), "ship", rotation, true
                    )
                );
                this.shipCreated = true;
            } else if (!EndCityPieces.recursiveChildren(
                p_227475_, EndCityPieces.HOUSE_TOWER_GENERATOR, p_227476_ + 1, endcitypieces$endcitypiece, new BlockPos(-3, j + 1, -11), p_227479_, p_227480_
            )) {
                return false;
            }

            endcitypieces$endcitypiece = EndCityPieces.addHelper(
                p_227479_,
                EndCityPieces.addPiece(
                    p_227475_, endcitypieces$endcitypiece, new BlockPos(4, j, 0), "bridge_end", rotation.getRotated(Rotation.CLOCKWISE_180), true
                )
            );
            endcitypieces$endcitypiece.setGenDepth(-1);
            return true;
        }
    };
    static final List<Tuple<Rotation, BlockPos>> FAT_TOWER_BRIDGES = Lists.newArrayList(
        new Tuple<>(Rotation.NONE, new BlockPos(4, -1, 0)),
        new Tuple<>(Rotation.CLOCKWISE_90, new BlockPos(12, -1, 4)),
        new Tuple<>(Rotation.COUNTERCLOCKWISE_90, new BlockPos(0, -1, 8)),
        new Tuple<>(Rotation.CLOCKWISE_180, new BlockPos(8, -1, 12))
    );
    static final EndCityPieces.SectionGenerator FAT_TOWER_GENERATOR = new EndCityPieces.SectionGenerator() {
        @Override
        public void init() {
        }

        @Override
        public boolean generate(
            StructureTemplateManager p_227484_,
            int p_227485_,
            EndCityPieces.EndCityPiece p_227486_,
            BlockPos p_227487_,
            List<StructurePiece> p_227488_,
            RandomSource p_227489_
        ) {
            Rotation rotation = p_227486_.placeSettings().getRotation();
            EndCityPieces.EndCityPiece endcitypieces$endcitypiece = EndCityPieces.addHelper(
                p_227488_, EndCityPieces.addPiece(p_227484_, p_227486_, new BlockPos(-3, 4, -3), "fat_tower_base", rotation, true)
            );
            endcitypieces$endcitypiece = EndCityPieces.addHelper(
                p_227488_, EndCityPieces.addPiece(p_227484_, endcitypieces$endcitypiece, new BlockPos(0, 4, 0), "fat_tower_middle", rotation, true)
            );

            for (int i = 0; i < 2 && p_227489_.nextInt(3) != 0; i++) {
                endcitypieces$endcitypiece = EndCityPieces.addHelper(
                    p_227488_, EndCityPieces.addPiece(p_227484_, endcitypieces$endcitypiece, new BlockPos(0, 8, 0), "fat_tower_middle", rotation, true)
                );

                for (Tuple<Rotation, BlockPos> tuple : EndCityPieces.FAT_TOWER_BRIDGES) {
                    if (p_227489_.nextBoolean()) {
                        EndCityPieces.EndCityPiece endcitypieces$endcitypiece1 = EndCityPieces.addHelper(
                            p_227488_,
                            EndCityPieces.addPiece(p_227484_, endcitypieces$endcitypiece, tuple.getB(), "bridge_end", rotation.getRotated(tuple.getA()), true)
                        );
                        EndCityPieces.recursiveChildren(
                            p_227484_, EndCityPieces.TOWER_BRIDGE_GENERATOR, p_227485_ + 1, endcitypieces$endcitypiece1, null, p_227488_, p_227489_
                        );
                    }
                }
            }

            endcitypieces$endcitypiece = EndCityPieces.addHelper(
                p_227488_, EndCityPieces.addPiece(p_227484_, endcitypieces$endcitypiece, new BlockPos(-2, 8, -2), "fat_tower_top", rotation, true)
            );
            return true;
        }
    };

    static EndCityPieces.EndCityPiece addPiece(
        StructureTemplateManager structureTemplateManager, EndCityPieces.EndCityPiece piece, BlockPos startPos, String name, Rotation rotation, boolean overwrite
    ) {
        EndCityPieces.EndCityPiece endcitypieces$endcitypiece = new EndCityPieces.EndCityPiece(
            structureTemplateManager, name, piece.templatePosition(), rotation, overwrite
        );
        BlockPos blockpos = piece.template()
            .calculateConnectedPosition(piece.placeSettings(), startPos, endcitypieces$endcitypiece.placeSettings(), BlockPos.ZERO);
        endcitypieces$endcitypiece.move(blockpos.getX(), blockpos.getY(), blockpos.getZ());
        return endcitypieces$endcitypiece;
    }

    public static void startHouseTower(
        StructureTemplateManager structureTemplateManager, BlockPos startPos, Rotation rotation, List<StructurePiece> pieces, RandomSource random
    ) {
        FAT_TOWER_GENERATOR.init();
        HOUSE_TOWER_GENERATOR.init();
        TOWER_BRIDGE_GENERATOR.init();
        TOWER_GENERATOR.init();
        EndCityPieces.EndCityPiece endcitypieces$endcitypiece = addHelper(
            pieces, new EndCityPieces.EndCityPiece(structureTemplateManager, "base_floor", startPos, rotation, true)
        );
        endcitypieces$endcitypiece = addHelper(
            pieces, addPiece(structureTemplateManager, endcitypieces$endcitypiece, new BlockPos(-1, 0, -1), "second_floor_1", rotation, false)
        );
        endcitypieces$endcitypiece = addHelper(
            pieces, addPiece(structureTemplateManager, endcitypieces$endcitypiece, new BlockPos(-1, 4, -1), "third_floor_1", rotation, false)
        );
        endcitypieces$endcitypiece = addHelper(
            pieces, addPiece(structureTemplateManager, endcitypieces$endcitypiece, new BlockPos(-1, 8, -1), "third_roof", rotation, true)
        );
        recursiveChildren(structureTemplateManager, TOWER_GENERATOR, 1, endcitypieces$endcitypiece, null, pieces, random);
    }

    static EndCityPieces.EndCityPiece addHelper(List<StructurePiece> pieces, EndCityPieces.EndCityPiece piece) {
        pieces.add(piece);
        return piece;
    }

    static boolean recursiveChildren(
        StructureTemplateManager structureTemplateManager,
        EndCityPieces.SectionGenerator sectionGenerator,
        int counter,
        EndCityPieces.EndCityPiece piece,
        BlockPos startPos,
        List<StructurePiece> pieces,
        RandomSource random
    ) {
        if (counter > 8) {
            return false;
        } else {
            List<StructurePiece> list = Lists.newArrayList();
            if (sectionGenerator.generate(structureTemplateManager, counter, piece, startPos, list, random)) {
                boolean flag = false;
                int i = random.nextInt();

                for (StructurePiece structurepiece : list) {
                    structurepiece.setGenDepth(i);
                    StructurePiece structurepiece1 = StructurePiece.findCollisionPiece(pieces, structurepiece.getBoundingBox());
                    if (structurepiece1 != null && structurepiece1.getGenDepth() != piece.getGenDepth()) {
                        flag = true;
                        break;
                    }
                }

                if (!flag) {
                    pieces.addAll(list);
                    return true;
                }
            }

            return false;
        }
    }

    public static class EndCityPiece extends TemplateStructurePiece {
        public EndCityPiece(StructureTemplateManager structureTemplateManager, String name, BlockPos startPos, Rotation rotation, boolean overwrite) {
            super(StructurePieceType.END_CITY_PIECE, 0, structureTemplateManager, makeResourceLocation(name), name, makeSettings(overwrite, rotation), startPos);
        }

        public EndCityPiece(StructureTemplateManager structureTemplateManager, CompoundTag tag) {
            super(
                StructurePieceType.END_CITY_PIECE,
                tag,
                structureTemplateManager,
                p_227512_ -> makeSettings(tag.getBoolean("OW"), Rotation.valueOf(tag.getString("Rot")))
            );
        }

        private static StructurePlaceSettings makeSettings(boolean overwrite, Rotation rotation) {
            BlockIgnoreProcessor blockignoreprocessor = overwrite ? BlockIgnoreProcessor.STRUCTURE_BLOCK : BlockIgnoreProcessor.STRUCTURE_AND_AIR;
            return new StructurePlaceSettings().setIgnoreEntities(true).addProcessor(blockignoreprocessor).setRotation(rotation);
        }

        @Override
        protected ResourceLocation makeTemplateLocation() {
            return makeResourceLocation(this.templateName);
        }

        private static ResourceLocation makeResourceLocation(String name) {
            return ResourceLocation.withDefaultNamespace("end_city/" + name);
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
            super.addAdditionalSaveData(context, tag);
            tag.putString("Rot", this.placeSettings.getRotation().name());
            tag.putBoolean("OW", this.placeSettings.getProcessors().get(0) == BlockIgnoreProcessor.STRUCTURE_BLOCK);
        }

        @Override
        protected void handleDataMarker(String name, BlockPos pos, ServerLevelAccessor level, RandomSource random, BoundingBox box) {
            if (name.startsWith("Chest")) {
                BlockPos blockpos = pos.below();
                if (box.isInside(blockpos)) {
                    RandomizableContainer.setBlockEntityLootTable(level, random, blockpos, BuiltInLootTables.END_CITY_TREASURE);
                }
            } else if (box.isInside(pos) && Level.isInSpawnableBounds(pos)) {
                if (name.startsWith("Sentry")) {
                    Shulker shulker = EntityType.SHULKER.create(level.getLevel());
                    if (shulker != null) {
                        shulker.setPos((double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5);
                        level.addFreshEntity(shulker);
                    }
                } else if (name.startsWith("Elytra")) {
                    ItemFrame itemframe = new ItemFrame(level.getLevel(), pos, this.placeSettings.getRotation().rotate(Direction.SOUTH));
                    itemframe.setItem(new ItemStack(Items.ELYTRA), false);
                    level.addFreshEntity(itemframe);
                }
            }
        }
    }

    interface SectionGenerator {
        void init();

        boolean generate(
            StructureTemplateManager structureTemplateManager,
            int counter,
            EndCityPieces.EndCityPiece piece,
            BlockPos startPos,
            List<StructurePiece> pieces,
            RandomSource random
        );
    }
}
