package net.minecraft.stats;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ResourceLocationException;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.protocol.game.ClientboundRecipePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.slf4j.Logger;

public class ServerRecipeBook extends RecipeBook {
    public static final String RECIPE_BOOK_TAG = "recipeBook";
    private static final Logger LOGGER = LogUtils.getLogger();

    public int addRecipes(Collection<RecipeHolder<?>> recipes, ServerPlayer player) {
        List<ResourceLocation> list = Lists.newArrayList();
        int i = 0;

        for (RecipeHolder<?> recipeholder : recipes) {
            ResourceLocation resourcelocation = recipeholder.id();
            if (!this.known.contains(resourcelocation) && !recipeholder.value().isSpecial()) {
                this.add(resourcelocation);
                this.addHighlight(resourcelocation);
                list.add(resourcelocation);
                CriteriaTriggers.RECIPE_UNLOCKED.trigger(player, recipeholder);
                i++;
            }
        }

        if (list.size() > 0) {
            this.sendRecipes(ClientboundRecipePacket.State.ADD, player, list);
        }

        return i;
    }

    public int removeRecipes(Collection<RecipeHolder<?>> recipes, ServerPlayer player) {
        List<ResourceLocation> list = Lists.newArrayList();
        int i = 0;

        for (RecipeHolder<?> recipeholder : recipes) {
            ResourceLocation resourcelocation = recipeholder.id();
            if (this.known.contains(resourcelocation)) {
                this.remove(resourcelocation);
                list.add(resourcelocation);
                i++;
            }
        }

        this.sendRecipes(ClientboundRecipePacket.State.REMOVE, player, list);
        return i;
    }

    private void sendRecipes(ClientboundRecipePacket.State state, ServerPlayer player, List<ResourceLocation> recipes) {
        player.connection.send(new ClientboundRecipePacket(state, recipes, Collections.emptyList(), this.getBookSettings()));
    }

    public CompoundTag toNbt() {
        CompoundTag compoundtag = new CompoundTag();
        this.getBookSettings().write(compoundtag);
        ListTag listtag = new ListTag();

        for (ResourceLocation resourcelocation : this.known) {
            listtag.add(StringTag.valueOf(resourcelocation.toString()));
        }

        compoundtag.put("recipes", listtag);
        ListTag listtag1 = new ListTag();

        for (ResourceLocation resourcelocation1 : this.highlight) {
            listtag1.add(StringTag.valueOf(resourcelocation1.toString()));
        }

        compoundtag.put("toBeDisplayed", listtag1);
        return compoundtag;
    }

    public void fromNbt(CompoundTag tag, RecipeManager recipeManager) {
        this.setBookSettings(RecipeBookSettings.read(tag));
        ListTag listtag = tag.getList("recipes", 8);
        this.loadRecipes(listtag, this::add, recipeManager);
        ListTag listtag1 = tag.getList("toBeDisplayed", 8);
        this.loadRecipes(listtag1, this::addHighlight, recipeManager);
    }

    private void loadRecipes(ListTag tags, Consumer<RecipeHolder<?>> recipeConsumer, RecipeManager recipeManager) {
        for (int i = 0; i < tags.size(); i++) {
            String s = tags.getString(i);

            try {
                ResourceLocation resourcelocation = ResourceLocation.parse(s);
                Optional<RecipeHolder<?>> optional = recipeManager.byKey(resourcelocation);
                if (optional.isEmpty()) {
                    LOGGER.error("Tried to load unrecognized recipe: {} removed now.", resourcelocation);
                } else {
                    recipeConsumer.accept(optional.get());
                }
            } catch (ResourceLocationException resourcelocationexception) {
                LOGGER.error("Tried to load improperly formatted recipe: {} removed now.", s);
            }
        }
    }

    public void sendInitialRecipeBook(ServerPlayer player) {
        player.connection.send(new ClientboundRecipePacket(ClientboundRecipePacket.State.INIT, this.known, this.highlight, this.getBookSettings()));
    }
}
