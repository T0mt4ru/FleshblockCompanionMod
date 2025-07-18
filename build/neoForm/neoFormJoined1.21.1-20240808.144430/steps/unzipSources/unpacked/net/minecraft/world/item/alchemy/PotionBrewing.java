package net.minecraft.world.item.alchemy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.crafting.Ingredient;

public class PotionBrewing {
    public static final int BREWING_TIME_SECONDS = 20;
    public static final PotionBrewing EMPTY = new PotionBrewing(List.of(), List.of(), List.of());
    private final List<Ingredient> containers;
    private final List<PotionBrewing.Mix<Potion>> potionMixes;
    private final List<PotionBrewing.Mix<Item>> containerMixes;
    private final net.neoforged.neoforge.common.brewing.BrewingRecipeRegistry registry;

    PotionBrewing(List<Ingredient> containers, List<PotionBrewing.Mix<Potion>> potionMixes, List<PotionBrewing.Mix<Item>> containerMixes) {
        this(containers, potionMixes, containerMixes, List.of());
    }

    PotionBrewing(List<Ingredient> containers, List<PotionBrewing.Mix<Potion>> potionMixes, List<PotionBrewing.Mix<Item>> containerMixes, List<net.neoforged.neoforge.common.brewing.IBrewingRecipe> recipes) {
        this.containers = containers;
        this.potionMixes = potionMixes;
        this.containerMixes = containerMixes;
        this.registry = new net.neoforged.neoforge.common.brewing.BrewingRecipeRegistry(recipes);
    }

    public boolean isIngredient(ItemStack stack) {
        return this.registry.isValidIngredient(stack) || this.isContainerIngredient(stack) || this.isPotionIngredient(stack);
    }

    /**
     * Checks if an item stack is a valid input for brewing,
     * for use in the lower 3 slots where water bottles would normally go.
     */
    public boolean isInput(ItemStack stack) {
        return this.registry.isValidInput(stack) || isContainer(stack);
    }

    /**
     * Retrieves recipes that use the more general interface.
     * This does NOT include the container and potion mixes.
     */
    public List<net.neoforged.neoforge.common.brewing.IBrewingRecipe> getRecipes() {
        return registry.recipes();
    }

    private boolean isContainer(ItemStack stack) {
        for (Ingredient ingredient : this.containers) {
            if (ingredient.test(stack)) {
                return true;
            }
        }

        return false;
    }

    public boolean isContainerIngredient(ItemStack stack) {
        for (PotionBrewing.Mix<Item> mix : this.containerMixes) {
            if (mix.ingredient.test(stack)) {
                return true;
            }
        }

        return false;
    }

    public boolean isPotionIngredient(ItemStack stack) {
        for (PotionBrewing.Mix<Potion> mix : this.potionMixes) {
            if (mix.ingredient.test(stack)) {
                return true;
            }
        }

        return false;
    }

    public boolean isBrewablePotion(Holder<Potion> potion) {
        for (PotionBrewing.Mix<Potion> mix : this.potionMixes) {
            if (mix.to.is(potion)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasMix(ItemStack reagent, ItemStack potionItem) {
        if (registry.hasOutput(reagent, potionItem)) return true;
        return !this.isContainer(reagent) ? false : this.hasContainerMix(reagent, potionItem) || this.hasPotionMix(reagent, potionItem);
    }

    public boolean hasContainerMix(ItemStack reagent, ItemStack potionItem) {
        for (PotionBrewing.Mix<Item> mix : this.containerMixes) {
            if (reagent.is(mix.from) && mix.ingredient.test(potionItem)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasPotionMix(ItemStack reagent, ItemStack potionItem) {
        Optional<Holder<Potion>> optional = reagent.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion();
        if (optional.isEmpty()) {
            return false;
        } else {
            for (PotionBrewing.Mix<Potion> mix : this.potionMixes) {
                if (mix.from.is(optional.get()) && mix.ingredient.test(potionItem)) {
                    return true;
                }
            }

            return false;
        }
    }

    public ItemStack mix(ItemStack potion, ItemStack potionItem) {
        if (potionItem.isEmpty()) {
            return potionItem;
        } else {
            var customMix = registry.getOutput(potionItem, potion); // Parameters are swapped compared to what vanilla passes!
            if (!customMix.isEmpty()) return customMix;
            Optional<Holder<Potion>> optional = potionItem.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion();
            if (optional.isEmpty()) {
                return potionItem;
            } else {
                for (PotionBrewing.Mix<Item> mix : this.containerMixes) {
                    if (potionItem.is(mix.from) && mix.ingredient.test(potion)) {
                        return PotionContents.createItemStack(mix.to.value(), optional.get());
                    }
                }

                for (PotionBrewing.Mix<Potion> mix1 : this.potionMixes) {
                    if (mix1.from.is(optional.get()) && mix1.ingredient.test(potion)) {
                        return PotionContents.createItemStack(potionItem.getItem(), mix1.to);
                    }
                }

                return potionItem;
            }
        }
    }

    /**
     * @deprecated Use {@link #bootstrap(FeatureFlagSet,
     *             net.minecraft.core.RegistryAccess)} instead
     */
    @Deprecated
    public static PotionBrewing bootstrap(FeatureFlagSet enabledFeatures) {
        return bootstrap(enabledFeatures, net.minecraft.core.RegistryAccess.EMPTY);
    }

    public static PotionBrewing bootstrap(FeatureFlagSet enabledFeatures, net.minecraft.core.RegistryAccess registryAccess) {
        PotionBrewing.Builder potionbrewing$builder = new PotionBrewing.Builder(enabledFeatures);
        addVanillaMixes(potionbrewing$builder);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent(potionbrewing$builder, registryAccess));
        return potionbrewing$builder.build();
    }

    public static void addVanillaMixes(PotionBrewing.Builder builder) {
        builder.addContainer(Items.POTION);
        builder.addContainer(Items.SPLASH_POTION);
        builder.addContainer(Items.LINGERING_POTION);
        builder.addContainerRecipe(Items.POTION, Items.GUNPOWDER, Items.SPLASH_POTION);
        builder.addContainerRecipe(Items.SPLASH_POTION, Items.DRAGON_BREATH, Items.LINGERING_POTION);
        builder.addMix(Potions.WATER, Items.GLOWSTONE_DUST, Potions.THICK);
        builder.addMix(Potions.WATER, Items.REDSTONE, Potions.MUNDANE);
        builder.addMix(Potions.WATER, Items.NETHER_WART, Potions.AWKWARD);
        builder.addStartMix(Items.BREEZE_ROD, Potions.WIND_CHARGED);
        builder.addStartMix(Items.SLIME_BLOCK, Potions.OOZING);
        builder.addStartMix(Items.STONE, Potions.INFESTED);
        builder.addStartMix(Items.COBWEB, Potions.WEAVING);
        builder.addMix(Potions.AWKWARD, Items.GOLDEN_CARROT, Potions.NIGHT_VISION);
        builder.addMix(Potions.NIGHT_VISION, Items.REDSTONE, Potions.LONG_NIGHT_VISION);
        builder.addMix(Potions.NIGHT_VISION, Items.FERMENTED_SPIDER_EYE, Potions.INVISIBILITY);
        builder.addMix(Potions.LONG_NIGHT_VISION, Items.FERMENTED_SPIDER_EYE, Potions.LONG_INVISIBILITY);
        builder.addMix(Potions.INVISIBILITY, Items.REDSTONE, Potions.LONG_INVISIBILITY);
        builder.addStartMix(Items.MAGMA_CREAM, Potions.FIRE_RESISTANCE);
        builder.addMix(Potions.FIRE_RESISTANCE, Items.REDSTONE, Potions.LONG_FIRE_RESISTANCE);
        builder.addStartMix(Items.RABBIT_FOOT, Potions.LEAPING);
        builder.addMix(Potions.LEAPING, Items.REDSTONE, Potions.LONG_LEAPING);
        builder.addMix(Potions.LEAPING, Items.GLOWSTONE_DUST, Potions.STRONG_LEAPING);
        builder.addMix(Potions.LEAPING, Items.FERMENTED_SPIDER_EYE, Potions.SLOWNESS);
        builder.addMix(Potions.LONG_LEAPING, Items.FERMENTED_SPIDER_EYE, Potions.LONG_SLOWNESS);
        builder.addMix(Potions.SLOWNESS, Items.REDSTONE, Potions.LONG_SLOWNESS);
        builder.addMix(Potions.SLOWNESS, Items.GLOWSTONE_DUST, Potions.STRONG_SLOWNESS);
        builder.addMix(Potions.AWKWARD, Items.TURTLE_HELMET, Potions.TURTLE_MASTER);
        builder.addMix(Potions.TURTLE_MASTER, Items.REDSTONE, Potions.LONG_TURTLE_MASTER);
        builder.addMix(Potions.TURTLE_MASTER, Items.GLOWSTONE_DUST, Potions.STRONG_TURTLE_MASTER);
        builder.addMix(Potions.SWIFTNESS, Items.FERMENTED_SPIDER_EYE, Potions.SLOWNESS);
        builder.addMix(Potions.LONG_SWIFTNESS, Items.FERMENTED_SPIDER_EYE, Potions.LONG_SLOWNESS);
        builder.addStartMix(Items.SUGAR, Potions.SWIFTNESS);
        builder.addMix(Potions.SWIFTNESS, Items.REDSTONE, Potions.LONG_SWIFTNESS);
        builder.addMix(Potions.SWIFTNESS, Items.GLOWSTONE_DUST, Potions.STRONG_SWIFTNESS);
        builder.addMix(Potions.AWKWARD, Items.PUFFERFISH, Potions.WATER_BREATHING);
        builder.addMix(Potions.WATER_BREATHING, Items.REDSTONE, Potions.LONG_WATER_BREATHING);
        builder.addStartMix(Items.GLISTERING_MELON_SLICE, Potions.HEALING);
        builder.addMix(Potions.HEALING, Items.GLOWSTONE_DUST, Potions.STRONG_HEALING);
        builder.addMix(Potions.HEALING, Items.FERMENTED_SPIDER_EYE, Potions.HARMING);
        builder.addMix(Potions.STRONG_HEALING, Items.FERMENTED_SPIDER_EYE, Potions.STRONG_HARMING);
        builder.addMix(Potions.HARMING, Items.GLOWSTONE_DUST, Potions.STRONG_HARMING);
        builder.addMix(Potions.POISON, Items.FERMENTED_SPIDER_EYE, Potions.HARMING);
        builder.addMix(Potions.LONG_POISON, Items.FERMENTED_SPIDER_EYE, Potions.HARMING);
        builder.addMix(Potions.STRONG_POISON, Items.FERMENTED_SPIDER_EYE, Potions.STRONG_HARMING);
        builder.addStartMix(Items.SPIDER_EYE, Potions.POISON);
        builder.addMix(Potions.POISON, Items.REDSTONE, Potions.LONG_POISON);
        builder.addMix(Potions.POISON, Items.GLOWSTONE_DUST, Potions.STRONG_POISON);
        builder.addStartMix(Items.GHAST_TEAR, Potions.REGENERATION);
        builder.addMix(Potions.REGENERATION, Items.REDSTONE, Potions.LONG_REGENERATION);
        builder.addMix(Potions.REGENERATION, Items.GLOWSTONE_DUST, Potions.STRONG_REGENERATION);
        builder.addStartMix(Items.BLAZE_POWDER, Potions.STRENGTH);
        builder.addMix(Potions.STRENGTH, Items.REDSTONE, Potions.LONG_STRENGTH);
        builder.addMix(Potions.STRENGTH, Items.GLOWSTONE_DUST, Potions.STRONG_STRENGTH);
        builder.addMix(Potions.WATER, Items.FERMENTED_SPIDER_EYE, Potions.WEAKNESS);
        builder.addMix(Potions.WEAKNESS, Items.REDSTONE, Potions.LONG_WEAKNESS);
        builder.addMix(Potions.AWKWARD, Items.PHANTOM_MEMBRANE, Potions.SLOW_FALLING);
        builder.addMix(Potions.SLOW_FALLING, Items.REDSTONE, Potions.LONG_SLOW_FALLING);
    }

    public static class Builder {
        private final List<Ingredient> containers = new ArrayList<>();
        private final List<PotionBrewing.Mix<Potion>> potionMixes = new ArrayList<>();
        private final List<PotionBrewing.Mix<Item>> containerMixes = new ArrayList<>();
        private final List<net.neoforged.neoforge.common.brewing.IBrewingRecipe> recipes = new ArrayList<>();
        private final FeatureFlagSet enabledFeatures;

        public Builder(FeatureFlagSet enabledFeatures) {
            this.enabledFeatures = enabledFeatures;
        }

        private static void expectPotion(Item item) {
            if (!(item instanceof PotionItem)) {
                throw new IllegalArgumentException("Expected a potion, got: " + BuiltInRegistries.ITEM.getKey(item));
            }
        }

        public void addContainerRecipe(Item input, Item reagent, Item result) {
            if (input.isEnabled(this.enabledFeatures) && reagent.isEnabled(this.enabledFeatures) && result.isEnabled(this.enabledFeatures)) {
                expectPotion(input);
                expectPotion(result);
                this.containerMixes
                    .add(new PotionBrewing.Mix<>(input.builtInRegistryHolder(), Ingredient.of(reagent), result.builtInRegistryHolder()));
            }
        }

        public void addContainer(Item container) {
            if (container.isEnabled(this.enabledFeatures)) {
                expectPotion(container);
                this.containers.add(Ingredient.of(container));
            }
        }

        public void addMix(Holder<Potion> input, Item reagent, Holder<Potion> result) {
            if (input.value().isEnabled(this.enabledFeatures)
                && reagent.isEnabled(this.enabledFeatures)
                && result.value().isEnabled(this.enabledFeatures)) {
                this.potionMixes.add(new PotionBrewing.Mix<>(input, Ingredient.of(reagent), result));
            }
        }

        public void addStartMix(Item reagent, Holder<Potion> result) {
            if (result.value().isEnabled(this.enabledFeatures)) {
                this.addMix(Potions.WATER, reagent, Potions.MUNDANE);
                this.addMix(Potions.AWKWARD, reagent, result);
            }
        }

        /**
         * Adds a new simple brewing recipe.
         *
         * @param input      the ingredient that goes in the same slot as water bottles would
         * @param ingredient the ingredient that goes in the same slot as nether wart would
         * @param output     the item stack that will replace the input once brewing is done
         */
        public void addRecipe(Ingredient input, Ingredient ingredient, ItemStack output) {
            addRecipe(new net.neoforged.neoforge.common.brewing.BrewingRecipe(input, ingredient, output));
        }

        /**
         * Adds a new brewing recipe with custom logic.
         */
        public void addRecipe(net.neoforged.neoforge.common.brewing.IBrewingRecipe recipe) {
            this.recipes.add(recipe);
        }

        public PotionBrewing build() {
            return new PotionBrewing(List.copyOf(this.containers), List.copyOf(this.potionMixes), List.copyOf(this.containerMixes), List.copyOf(this.recipes));
        }
    }

    static record Mix<T>(Holder<T> from, Ingredient ingredient, Holder<T> to) {
    }
}
