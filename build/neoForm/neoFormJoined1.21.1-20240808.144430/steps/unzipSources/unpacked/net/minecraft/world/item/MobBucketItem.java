package net.minecraft.world.item;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;

public class MobBucketItem extends BucketItem {
    private static final MapCodec<TropicalFish.Variant> VARIANT_FIELD_CODEC = TropicalFish.Variant.CODEC.fieldOf("BucketVariantTag");
    private final EntityType<?> type;
    private final SoundEvent emptySound;

    public MobBucketItem(EntityType<?> type, Fluid content, SoundEvent emptySound, Item.Properties properties) {
        super(content, properties);
        this.type = type;
        this.emptySound = emptySound;
    }

    @Override
    public void checkExtraContent(@Nullable Player player, Level level, ItemStack containerStack, BlockPos pos) {
        if (level instanceof ServerLevel) {
            this.spawn((ServerLevel)level, containerStack, pos);
            level.gameEvent(player, GameEvent.ENTITY_PLACE, pos);
        }
    }

    @Override
    protected void playEmptySound(@Nullable Player player, LevelAccessor level, BlockPos pos) {
        level.playSound(player, pos, this.emptySound, SoundSource.NEUTRAL, 1.0F, 1.0F);
    }

    private void spawn(ServerLevel serverLevel, ItemStack bucketedMobStack, BlockPos pos) {
        if (this.type.spawn(serverLevel, bucketedMobStack, null, pos, MobSpawnType.BUCKET, true, false) instanceof Bucketable bucketable) {
            CustomData customdata = bucketedMobStack.getOrDefault(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY);
            bucketable.loadFromBucketTag(customdata.copyTag());
            bucketable.setFromBucket(true);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (this.type == EntityType.TROPICAL_FISH) {
            CustomData customdata = stack.getOrDefault(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY);
            if (customdata.isEmpty()) {
                return;
            }

            Optional<TropicalFish.Variant> optional = customdata.read(VARIANT_FIELD_CODEC).result();
            if (optional.isPresent()) {
                TropicalFish.Variant tropicalfish$variant = optional.get();
                ChatFormatting[] achatformatting = new ChatFormatting[]{ChatFormatting.ITALIC, ChatFormatting.GRAY};
                String s = "color.minecraft." + tropicalfish$variant.baseColor();
                String s1 = "color.minecraft." + tropicalfish$variant.patternColor();
                int i = TropicalFish.COMMON_VARIANTS.indexOf(tropicalfish$variant);
                if (i != -1) {
                    tooltipComponents.add(Component.translatable(TropicalFish.getPredefinedName(i)).withStyle(achatformatting));
                    return;
                }

                tooltipComponents.add(tropicalfish$variant.pattern().displayName().plainCopy().withStyle(achatformatting));
                MutableComponent mutablecomponent = Component.translatable(s);
                if (!s.equals(s1)) {
                    mutablecomponent.append(", ").append(Component.translatable(s1));
                }

                mutablecomponent.withStyle(achatformatting);
                tooltipComponents.add(mutablecomponent);
            }
        }
    }
}
