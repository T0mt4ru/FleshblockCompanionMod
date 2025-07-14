package net.minecraft.world.entity.animal;

import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;

public class Sheep extends Animal implements Shearable {
    private static final int EAT_ANIMATION_TICKS = 40;
    private static final EntityDataAccessor<Byte> DATA_WOOL_ID = SynchedEntityData.defineId(Sheep.class, EntityDataSerializers.BYTE);
    private static final Map<DyeColor, ItemLike> ITEM_BY_DYE = Util.make(Maps.newEnumMap(DyeColor.class), p_29841_ -> {
        p_29841_.put(DyeColor.WHITE, Blocks.WHITE_WOOL);
        p_29841_.put(DyeColor.ORANGE, Blocks.ORANGE_WOOL);
        p_29841_.put(DyeColor.MAGENTA, Blocks.MAGENTA_WOOL);
        p_29841_.put(DyeColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_WOOL);
        p_29841_.put(DyeColor.YELLOW, Blocks.YELLOW_WOOL);
        p_29841_.put(DyeColor.LIME, Blocks.LIME_WOOL);
        p_29841_.put(DyeColor.PINK, Blocks.PINK_WOOL);
        p_29841_.put(DyeColor.GRAY, Blocks.GRAY_WOOL);
        p_29841_.put(DyeColor.LIGHT_GRAY, Blocks.LIGHT_GRAY_WOOL);
        p_29841_.put(DyeColor.CYAN, Blocks.CYAN_WOOL);
        p_29841_.put(DyeColor.PURPLE, Blocks.PURPLE_WOOL);
        p_29841_.put(DyeColor.BLUE, Blocks.BLUE_WOOL);
        p_29841_.put(DyeColor.BROWN, Blocks.BROWN_WOOL);
        p_29841_.put(DyeColor.GREEN, Blocks.GREEN_WOOL);
        p_29841_.put(DyeColor.RED, Blocks.RED_WOOL);
        p_29841_.put(DyeColor.BLACK, Blocks.BLACK_WOOL);
    });
    private static final Map<DyeColor, Integer> COLOR_BY_DYE = Maps.<DyeColor, Integer>newEnumMap(
        Arrays.stream(DyeColor.values()).collect(Collectors.toMap(p_29868_ -> (DyeColor)p_29868_, Sheep::createSheepColor))
    );
    private int eatAnimationTick;
    private EatBlockGoal eatBlockGoal;

    private static int createSheepColor(DyeColor dyeColor) {
        if (dyeColor == DyeColor.WHITE) {
            return -1644826;
        } else {
            int i = dyeColor.getTextureDiffuseColor();
            float f = 0.75F;
            return FastColor.ARGB32.color(
                255,
                Mth.floor((float)FastColor.ARGB32.red(i) * 0.75F),
                Mth.floor((float)FastColor.ARGB32.green(i) * 0.75F),
                Mth.floor((float)FastColor.ARGB32.blue(i) * 0.75F)
            );
        }
    }

    public static int getColor(DyeColor dyeColor) {
        return COLOR_BY_DYE.get(dyeColor);
    }

    public Sheep(EntityType<? extends Sheep> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        this.eatBlockGoal = new EatBlockGoal(this);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.25));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.1, p_335259_ -> p_335259_.is(ItemTags.SHEEP_FOOD), false));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(5, this.eatBlockGoal);
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    /**
     * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on the animal type)
     */
    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ItemTags.SHEEP_FOOD);
    }

    @Override
    protected void customServerAiStep() {
        this.eatAnimationTick = this.eatBlockGoal.getEatAnimationTick();
        super.customServerAiStep();
    }

    @Override
    public void aiStep() {
        if (this.level().isClientSide) {
            this.eatAnimationTick = Math.max(0, this.eatAnimationTick - 1);
        }

        super.aiStep();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 8.0).add(Attributes.MOVEMENT_SPEED, 0.23F);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_WOOL_ID, (byte)0);
    }

    @Override
    public ResourceKey<LootTable> getDefaultLootTable() {
        if (this.isSheared()) {
            return this.getType().getDefaultLootTable();
        } else {
            return switch (this.getColor()) {
                case WHITE -> BuiltInLootTables.SHEEP_WHITE;
                case ORANGE -> BuiltInLootTables.SHEEP_ORANGE;
                case MAGENTA -> BuiltInLootTables.SHEEP_MAGENTA;
                case LIGHT_BLUE -> BuiltInLootTables.SHEEP_LIGHT_BLUE;
                case YELLOW -> BuiltInLootTables.SHEEP_YELLOW;
                case LIME -> BuiltInLootTables.SHEEP_LIME;
                case PINK -> BuiltInLootTables.SHEEP_PINK;
                case GRAY -> BuiltInLootTables.SHEEP_GRAY;
                case LIGHT_GRAY -> BuiltInLootTables.SHEEP_LIGHT_GRAY;
                case CYAN -> BuiltInLootTables.SHEEP_CYAN;
                case PURPLE -> BuiltInLootTables.SHEEP_PURPLE;
                case BLUE -> BuiltInLootTables.SHEEP_BLUE;
                case BROWN -> BuiltInLootTables.SHEEP_BROWN;
                case GREEN -> BuiltInLootTables.SHEEP_GREEN;
                case RED -> BuiltInLootTables.SHEEP_RED;
                case BLACK -> BuiltInLootTables.SHEEP_BLACK;
            };
        }
    }

    /**
     * Handler for {@link World#setEntityState}
     */
    @Override
    public void handleEntityEvent(byte id) {
        if (id == 10) {
            this.eatAnimationTick = 40;
        } else {
            super.handleEntityEvent(id);
        }
    }

    public float getHeadEatPositionScale(float partialTick) {
        if (this.eatAnimationTick <= 0) {
            return 0.0F;
        } else if (this.eatAnimationTick >= 4 && this.eatAnimationTick <= 36) {
            return 1.0F;
        } else {
            return this.eatAnimationTick < 4 ? ((float)this.eatAnimationTick - partialTick) / 4.0F : -((float)(this.eatAnimationTick - 40) - partialTick) / 4.0F;
        }
    }

    public float getHeadEatAngleScale(float partialTick) {
        if (this.eatAnimationTick > 4 && this.eatAnimationTick <= 36) {
            float f = ((float)(this.eatAnimationTick - 4) - partialTick) / 32.0F;
            return (float) (Math.PI / 5) + 0.21991149F * Mth.sin(f * 28.7F);
        } else {
            return this.eatAnimationTick > 0 ? (float) (Math.PI / 5) : this.getXRot() * (float) (Math.PI / 180.0);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (false && itemstack.is(Items.SHEARS)) { // Neo: Shear logic is handled by IShearable
            if (!this.level().isClientSide && this.readyForShearing()) {
                this.shear(SoundSource.PLAYERS);
                this.gameEvent(GameEvent.SHEAR, player);
                itemstack.hurtAndBreak(1, player, getSlotForHand(hand));
                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.CONSUME;
            }
        } else {
            return super.mobInteract(player, hand);
        }
    }

    @Override
    public void shear(SoundSource category) {
        this.level().playSound(null, this, SoundEvents.SHEEP_SHEAR, category, 1.0F, 1.0F);
        this.setSheared(true);
        int i = 1 + this.random.nextInt(3);

        for (int j = 0; j < i; j++) {
            ItemEntity itementity = this.spawnAtLocation(ITEM_BY_DYE.get(this.getColor()), 1);
            if (itementity != null) {
                itementity.setDeltaMovement(
                    itementity.getDeltaMovement()
                        .add(
                            (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.1F),
                            (double)(this.random.nextFloat() * 0.05F),
                            (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.1F)
                        )
                );
            }
        }
    }

    @Override
    public boolean readyForShearing() {
        return this.isAlive() && !this.isSheared() && !this.isBaby();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Sheared", this.isSheared());
        compound.putByte("Color", (byte)this.getColor().getId());
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setSheared(compound.getBoolean("Sheared"));
        this.setColor(DyeColor.byId(compound.getByte("Color")));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SHEEP_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SHEEP_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SHEEP_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState block) {
        this.playSound(SoundEvents.SHEEP_STEP, 0.15F, 1.0F);
    }

    public DyeColor getColor() {
        return DyeColor.byId(this.entityData.get(DATA_WOOL_ID) & 15);
    }

    /**
     * Sets the wool color of this sheep
     */
    public void setColor(DyeColor dyeColor) {
        byte b0 = this.entityData.get(DATA_WOOL_ID);
        this.entityData.set(DATA_WOOL_ID, (byte)(b0 & 240 | dyeColor.getId() & 15));
    }

    public boolean isSheared() {
        return (this.entityData.get(DATA_WOOL_ID) & 16) != 0;
    }

    /**
     * Makes a sheep sheared if set to {@code true}.
     */
    public void setSheared(boolean sheared) {
        byte b0 = this.entityData.get(DATA_WOOL_ID);
        if (sheared) {
            this.entityData.set(DATA_WOOL_ID, (byte)(b0 | 16));
        } else {
            this.entityData.set(DATA_WOOL_ID, (byte)(b0 & -17));
        }
    }

    public static DyeColor getRandomSheepColor(RandomSource random) {
        int i = random.nextInt(100);
        if (i < 5) {
            return DyeColor.BLACK;
        } else if (i < 10) {
            return DyeColor.GRAY;
        } else if (i < 15) {
            return DyeColor.LIGHT_GRAY;
        } else if (i < 18) {
            return DyeColor.BROWN;
        } else {
            return random.nextInt(500) == 0 ? DyeColor.PINK : DyeColor.WHITE;
        }
    }

    @Nullable
    public Sheep getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        Sheep sheep = EntityType.SHEEP.create(level);
        if (sheep != null) {
            sheep.setColor(this.getOffspringColor(this, (Sheep)otherParent));
        }

        return sheep;
    }

    @Override
    public void ate() {
        super.ate();
        this.setSheared(false);
        if (this.isBaby()) {
            this.ageUp(60);
        }
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        this.setColor(getRandomSheepColor(level.getRandom()));
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    /**
     * Attempts to mix both parent sheep to come up with a mixed dye color.
     */
    private DyeColor getOffspringColor(Animal father, Animal mother) {
        DyeColor dyecolor = ((Sheep)father).getColor();
        DyeColor dyecolor1 = ((Sheep)mother).getColor();
        CraftingInput craftinginput = makeCraftInput(dyecolor, dyecolor1);
        return this.level()
            .getRecipeManager()
            .getRecipeFor(RecipeType.CRAFTING, craftinginput, this.level())
            .map(p_352802_ -> p_352802_.value().assemble(craftinginput, this.level().registryAccess()))
            .map(ItemStack::getItem)
            .filter(DyeItem.class::isInstance)
            .map(DyeItem.class::cast)
            .map(DyeItem::getDyeColor)
            .orElseGet(() -> this.level().random.nextBoolean() ? dyecolor : dyecolor1);
    }

    private static CraftingInput makeCraftInput(DyeColor color1, DyeColor color2) {
        return CraftingInput.of(2, 1, List.of(new ItemStack(DyeItem.byColor(color1)), new ItemStack(DyeItem.byColor(color2))));
    }
}
