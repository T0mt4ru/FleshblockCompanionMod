package net.minecraft.world.entity.ai.attributes;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

/**
 * Contains all entity attributes defined and registered by the vanilla game.
 */
public class Attributes {
    /**
     * Handles the armor points for an entity. Each point represents half a chestplate of armor on the armor bar.
     */
    public static final Holder<Attribute> ARMOR = register(
        "generic.armor", new RangedAttribute("attribute.name.generic.armor", 0.0, 0.0, 30.0).setSyncable(true)
    );
    /**
     * Handles the amount of damage mitigated by wearing armor.
     */
    public static final Holder<Attribute> ARMOR_TOUGHNESS = register(
        "generic.armor_toughness", new RangedAttribute("attribute.name.generic.armor_toughness", 0.0, 0.0, 20.0).setSyncable(true)
    );
    /**
     * Handles the attack damage inflicted by entities. The value of this attribute represents half hearts.
     */
    public static final Holder<Attribute> ATTACK_DAMAGE = register(
        "generic.attack_damage", new RangedAttribute("attribute.name.generic.attack_damage", 2.0, 0.0, 2048.0)
    );
    /**
     * Handles additional horizontal knockback when damaging another entity.
     */
    public static final Holder<Attribute> ATTACK_KNOCKBACK = register(
        "generic.attack_knockback", new RangedAttribute("attribute.name.generic.attack_knockback", 0.0, 0.0, 5.0)
    );
    /**
     * Handles the cooldown rate when attacking with an item. The value represents the number of full strength attacks that can be performed per second.
     */
    public static final Holder<Attribute> ATTACK_SPEED = register(
        "generic.attack_speed", new RangedAttribute("attribute.name.generic.attack_speed", 4.0, 0.0, 1024.0).setSyncable(true)
    );
    public static final Holder<Attribute> BLOCK_BREAK_SPEED = register(
        "player.block_break_speed", new RangedAttribute("attribute.name.player.block_break_speed", 1.0, 0.0, 1024.0).setSyncable(true)
    );
    public static final Holder<Attribute> BLOCK_INTERACTION_RANGE = register(
        "player.block_interaction_range", new RangedAttribute("attribute.name.player.block_interaction_range", 4.5, 0.0, 64.0).setSyncable(true)
    );
    public static final Holder<Attribute> BURNING_TIME = register(
        "generic.burning_time",
        new RangedAttribute("attribute.name.generic.burning_time", 1.0, 0.0, 1024.0).setSyncable(true).setSentiment(Attribute.Sentiment.NEGATIVE)
    );
    public static final Holder<Attribute> EXPLOSION_KNOCKBACK_RESISTANCE = register(
        "generic.explosion_knockback_resistance", new RangedAttribute("attribute.name.generic.explosion_knockback_resistance", 0.0, 0.0, 1.0).setSyncable(true)
    );
    public static final Holder<Attribute> ENTITY_INTERACTION_RANGE = register(
        "player.entity_interaction_range", new RangedAttribute("attribute.name.player.entity_interaction_range", 3.0, 0.0, 64.0).setSyncable(true)
    );
    public static final Holder<Attribute> FALL_DAMAGE_MULTIPLIER = register(
        "generic.fall_damage_multiplier",
        new RangedAttribute("attribute.name.generic.fall_damage_multiplier", 1.0, 0.0, 100.0).setSyncable(true).setSentiment(Attribute.Sentiment.NEGATIVE)
    );
    /**
     * Handles the movement speed of flying entities such as parrots and bees.
     */
    public static final Holder<Attribute> FLYING_SPEED = register(
        "generic.flying_speed", new RangedAttribute("attribute.name.generic.flying_speed", 0.4, 0.0, 1024.0).setSyncable(true)
    );
    /**
     * Handles the range in blocks that a mob will notice and track players and other potential targets.
     */
    public static final Holder<Attribute> FOLLOW_RANGE = register(
        "generic.follow_range", new RangedAttribute("attribute.name.generic.follow_range", 32.0, 0.0, 2048.0)
    );
    public static final Holder<Attribute> GRAVITY = register(
        "generic.gravity", new RangedAttribute("attribute.name.generic.gravity", 0.08, -1.0, 1.0).setSyncable(true).setSentiment(Attribute.Sentiment.NEUTRAL)
    );
    /**
     * Handles the jump strength for horses.
     */
    public static final Holder<Attribute> JUMP_STRENGTH = register(
        "generic.jump_strength", new RangedAttribute("attribute.name.generic.jump_strength", 0.42F, 0.0, 32.0).setSyncable(true)
    );
    /**
     * Handles the reduction of horizontal knockback when damaged by attacks or projectiles.
     */
    public static final Holder<Attribute> KNOCKBACK_RESISTANCE = register(
        // Neo: Convert Knockback Resistance to percent-based for more appropriate display using IAttributeExtension.
        "generic.knockback_resistance", new net.neoforged.neoforge.common.PercentageAttribute("attribute.name.generic.knockback_resistance", 0.0, 0.0, 1.0)
    );
    /**
     * Handles luck when a player generates loot from a loot table. This can impact the quality of loot and influence bonus rolls.
     */
    public static final Holder<Attribute> LUCK = register(
        "generic.luck", new RangedAttribute("attribute.name.generic.luck", 0.0, -1024.0, 1024.0).setSyncable(true)
    );
    public static final Holder<Attribute> MAX_ABSORPTION = register(
        "generic.max_absorption", new RangedAttribute("attribute.name.generic.max_absorption", 0.0, 0.0, 2048.0).setSyncable(true)
    );
    /**
     * Handles the maximum health of an entity.
     */
    public static final Holder<Attribute> MAX_HEALTH = register(
        "generic.max_health", new RangedAttribute("attribute.name.generic.max_health", 20.0, 1.0, 1024.0).setSyncable(true)
    );
    public static final Holder<Attribute> MINING_EFFICIENCY = register(
        "player.mining_efficiency", new RangedAttribute("attribute.name.player.mining_efficiency", 0.0, 0.0, 1024.0).setSyncable(true)
    );
    public static final Holder<Attribute> MOVEMENT_EFFICIENCY = register(
        "generic.movement_efficiency", new RangedAttribute("attribute.name.generic.movement_efficiency", 0.0, 0.0, 1.0).setSyncable(true)
    );
    /**
     * Handles the movement speed of entities.
     */
    public static final Holder<Attribute> MOVEMENT_SPEED = register(
        // Neo: Convert Movement Speed to percent-based for more appropriate display using IAttributeExtension. Use a scale factor of 1000 since movement speed has 0.001 units.
        "generic.movement_speed", new net.neoforged.neoforge.common.PercentageAttribute("attribute.name.generic.movement_speed", 0.7, 0.0, 1024.0, 1000).setSyncable(true)
    );
    public static final Holder<Attribute> OXYGEN_BONUS = register(
        "generic.oxygen_bonus", new RangedAttribute("attribute.name.generic.oxygen_bonus", 0.0, 0.0, 1024.0).setSyncable(true)
    );
    public static final Holder<Attribute> SAFE_FALL_DISTANCE = register(
        "generic.safe_fall_distance", new RangedAttribute("attribute.name.generic.safe_fall_distance", 3.0, -1024.0, 1024.0).setSyncable(true)
    );
    public static final Holder<Attribute> SCALE = register(
        "generic.scale", new RangedAttribute("attribute.name.generic.scale", 1.0, 0.0625, 16.0).setSyncable(true).setSentiment(Attribute.Sentiment.NEUTRAL)
    );
    public static final Holder<Attribute> SNEAKING_SPEED = register(
        "player.sneaking_speed", new RangedAttribute("attribute.name.player.sneaking_speed", 0.3, 0.0, 1.0).setSyncable(true)
    );
    /**
     * Handles the chance for a zombie to summon reinforcements when attacked.
     */
    public static final Holder<Attribute> SPAWN_REINFORCEMENTS_CHANCE = register(
        "zombie.spawn_reinforcements", new RangedAttribute("attribute.name.zombie.spawn_reinforcements", 0.0, 0.0, 1.0)
    );
    public static final Holder<Attribute> STEP_HEIGHT = register(
        "generic.step_height", new RangedAttribute("attribute.name.generic.step_height", 0.6, 0.0, 10.0).setSyncable(true)
    );
    public static final Holder<Attribute> SUBMERGED_MINING_SPEED = register(
        "player.submerged_mining_speed", new RangedAttribute("attribute.name.player.submerged_mining_speed", 0.2, 0.0, 20.0).setSyncable(true)
    );
    public static final Holder<Attribute> SWEEPING_DAMAGE_RATIO = register(
        "player.sweeping_damage_ratio", new RangedAttribute("attribute.name.player.sweeping_damage_ratio", 0.0, 0.0, 1.0).setSyncable(true)
    );
    public static final Holder<Attribute> WATER_MOVEMENT_EFFICIENCY = register(
        "generic.water_movement_efficiency", new RangedAttribute("attribute.name.generic.water_movement_efficiency", 0.0, 0.0, 1.0).setSyncable(true)
    );

    private static Holder<Attribute> register(String name, Attribute attribute) {
        return Registry.registerForHolder(BuiltInRegistries.ATTRIBUTE, ResourceLocation.withDefaultNamespace(name), attribute);
    }

    public static Holder<Attribute> bootstrap(Registry<Attribute> registry) {
        return MAX_HEALTH;
    }
}
