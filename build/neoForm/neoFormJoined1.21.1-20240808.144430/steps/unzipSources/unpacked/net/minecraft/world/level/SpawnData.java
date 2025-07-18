package net.minecraft.world.level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.InclusiveRange;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.entity.EquipmentTable;

public record SpawnData(CompoundTag entityToSpawn, Optional<SpawnData.CustomSpawnRules> customSpawnRules, Optional<EquipmentTable> equipment) {
    public static final String ENTITY_TAG = "entity";
    public static final Codec<SpawnData> CODEC = RecordCodecBuilder.create(
        p_340790_ -> p_340790_.group(
                    CompoundTag.CODEC.fieldOf("entity").forGetter(p_186576_ -> p_186576_.entityToSpawn),
                    SpawnData.CustomSpawnRules.CODEC.optionalFieldOf("custom_spawn_rules").forGetter(p_186569_ -> p_186569_.customSpawnRules),
                    EquipmentTable.CODEC.optionalFieldOf("equipment").forGetter(p_340789_ -> p_340789_.equipment)
                )
                .apply(p_340790_, SpawnData::new)
    );
    public static final Codec<SimpleWeightedRandomList<SpawnData>> LIST_CODEC = SimpleWeightedRandomList.wrappedCodecAllowingEmpty(CODEC);

    public SpawnData() {
        this(new CompoundTag(), Optional.empty(), Optional.empty());
    }

    public SpawnData(CompoundTag entityToSpawn, Optional<SpawnData.CustomSpawnRules> customSpawnRules, Optional<EquipmentTable> equipment) {
        if (entityToSpawn.contains("id")) {
            ResourceLocation resourcelocation = ResourceLocation.tryParse(entityToSpawn.getString("id"));
            if (resourcelocation != null) {
                entityToSpawn.putString("id", resourcelocation.toString());
            } else {
                entityToSpawn.remove("id");
            }
        }

        this.entityToSpawn = entityToSpawn;
        this.customSpawnRules = customSpawnRules;
        this.equipment = equipment;
    }

    public CompoundTag getEntityToSpawn() {
        return this.entityToSpawn;
    }

    public Optional<SpawnData.CustomSpawnRules> getCustomSpawnRules() {
        return this.customSpawnRules;
    }

    public Optional<EquipmentTable> getEquipment() {
        return this.equipment;
    }

    public static record CustomSpawnRules(InclusiveRange<Integer> blockLightLimit, InclusiveRange<Integer> skyLightLimit) {
        private static final InclusiveRange<Integer> LIGHT_RANGE = new InclusiveRange<>(0, 15);
        public static final Codec<SpawnData.CustomSpawnRules> CODEC = RecordCodecBuilder.create(
            p_286217_ -> p_286217_.group(
                        lightLimit("block_light_limit").forGetter(p_186600_ -> p_186600_.blockLightLimit),
                        lightLimit("sky_light_limit").forGetter(p_186595_ -> p_186595_.skyLightLimit)
                    )
                    .apply(p_286217_, SpawnData.CustomSpawnRules::new)
        );

        private static DataResult<InclusiveRange<Integer>> checkLightBoundaries(InclusiveRange<Integer> lightValues) {
            return !LIGHT_RANGE.contains(lightValues)
                ? DataResult.error(() -> "Light values must be withing range " + LIGHT_RANGE)
                : DataResult.success(lightValues);
        }

        private static MapCodec<InclusiveRange<Integer>> lightLimit(String fieldName) {
            return InclusiveRange.INT.lenientOptionalFieldOf(fieldName, LIGHT_RANGE).validate(SpawnData.CustomSpawnRules::checkLightBoundaries);
        }

        public boolean isValidPosition(BlockPos pos, ServerLevel level) {
            return this.blockLightLimit.isValueInRange(level.getBrightness(LightLayer.BLOCK, pos))
                && this.skyLightLimit.isValueInRange(level.getBrightness(LightLayer.SKY, pos));
        }
    }
}
