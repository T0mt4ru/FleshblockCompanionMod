package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceOrIdArgument;
import net.minecraft.commands.arguments.SlotArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class ItemCommands {
    static final Dynamic3CommandExceptionType ERROR_TARGET_NOT_A_CONTAINER = new Dynamic3CommandExceptionType(
        (p_304251_, p_304252_, p_304253_) -> Component.translatableEscape("commands.item.target.not_a_container", p_304251_, p_304252_, p_304253_)
    );
    static final Dynamic3CommandExceptionType ERROR_SOURCE_NOT_A_CONTAINER = new Dynamic3CommandExceptionType(
        (p_304247_, p_304248_, p_304249_) -> Component.translatableEscape("commands.item.source.not_a_container", p_304247_, p_304248_, p_304249_)
    );
    static final DynamicCommandExceptionType ERROR_TARGET_INAPPLICABLE_SLOT = new DynamicCommandExceptionType(
        p_304250_ -> Component.translatableEscape("commands.item.target.no_such_slot", p_304250_)
    );
    private static final DynamicCommandExceptionType ERROR_SOURCE_INAPPLICABLE_SLOT = new DynamicCommandExceptionType(
        p_304246_ -> Component.translatableEscape("commands.item.source.no_such_slot", p_304246_)
    );
    private static final DynamicCommandExceptionType ERROR_TARGET_NO_CHANGES = new DynamicCommandExceptionType(
        p_304245_ -> Component.translatableEscape("commands.item.target.no_changes", p_304245_)
    );
    private static final Dynamic2CommandExceptionType ERROR_TARGET_NO_CHANGES_KNOWN_ITEM = new Dynamic2CommandExceptionType(
        (p_304254_, p_304255_) -> Component.translatableEscape("commands.item.target.no_changed.known_item", p_304254_, p_304255_)
    );
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_MODIFIER = (p_335220_, p_335221_) -> {
        ReloadableServerRegistries.Holder reloadableserverregistries$holder = p_335220_.getSource().getServer().reloadableRegistries();
        return SharedSuggestionProvider.suggestResource(reloadableserverregistries$holder.getKeys(Registries.ITEM_MODIFIER), p_335221_);
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register(
            Commands.literal("item")
                .requires(p_180256_ -> p_180256_.hasPermission(2))
                .then(
                    Commands.literal("replace")
                        .then(
                            Commands.literal("block")
                                .then(
                                    Commands.argument("pos", BlockPosArgument.blockPos())
                                        .then(
                                            Commands.argument("slot", SlotArgument.slot())
                                                .then(
                                                    Commands.literal("with")
                                                        .then(
                                                            Commands.argument("item", ItemArgument.item(context))
                                                                .executes(
                                                                    p_180383_ -> setBlockItem(
                                                                            p_180383_.getSource(),
                                                                            BlockPosArgument.getLoadedBlockPos(p_180383_, "pos"),
                                                                            SlotArgument.getSlot(p_180383_, "slot"),
                                                                            ItemArgument.getItem(p_180383_, "item").createItemStack(1, false)
                                                                        )
                                                                )
                                                                .then(
                                                                    Commands.argument("count", IntegerArgumentType.integer(1, 99))
                                                                        .executes(
                                                                            p_180381_ -> setBlockItem(
                                                                                    p_180381_.getSource(),
                                                                                    BlockPosArgument.getLoadedBlockPos(p_180381_, "pos"),
                                                                                    SlotArgument.getSlot(p_180381_, "slot"),
                                                                                    ItemArgument.getItem(p_180381_, "item")
                                                                                        .createItemStack(
                                                                                            IntegerArgumentType.getInteger(p_180381_, "count"), true
                                                                                        )
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                                .then(
                                                    Commands.literal("from")
                                                        .then(
                                                            Commands.literal("block")
                                                                .then(
                                                                    Commands.argument("source", BlockPosArgument.blockPos())
                                                                        .then(
                                                                            Commands.argument("sourceSlot", SlotArgument.slot())
                                                                                .executes(
                                                                                    p_180379_ -> blockToBlock(
                                                                                            p_180379_.getSource(),
                                                                                            BlockPosArgument.getLoadedBlockPos(p_180379_, "source"),
                                                                                            SlotArgument.getSlot(p_180379_, "sourceSlot"),
                                                                                            BlockPosArgument.getLoadedBlockPos(p_180379_, "pos"),
                                                                                            SlotArgument.getSlot(p_180379_, "slot")
                                                                                        )
                                                                                )
                                                                                .then(
                                                                                    Commands.argument("modifier", ResourceOrIdArgument.lootModifier(context))
                                                                                        .suggests(SUGGEST_MODIFIER)
                                                                                        .executes(
                                                                                            p_335226_ -> blockToBlock(
                                                                                                    (CommandSourceStack)p_335226_.getSource(),
                                                                                                    BlockPosArgument.getLoadedBlockPos(p_335226_, "source"),
                                                                                                    SlotArgument.getSlot(p_335226_, "sourceSlot"),
                                                                                                    BlockPosArgument.getLoadedBlockPos(p_335226_, "pos"),
                                                                                                    SlotArgument.getSlot(p_335226_, "slot"),
                                                                                                    ResourceOrIdArgument.getLootModifier(p_335226_, "modifier")
                                                                                                )
                                                                                        )
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                        .then(
                                                            Commands.literal("entity")
                                                                .then(
                                                                    Commands.argument("source", EntityArgument.entity())
                                                                        .then(
                                                                            Commands.argument("sourceSlot", SlotArgument.slot())
                                                                                .executes(
                                                                                    p_180375_ -> entityToBlock(
                                                                                            p_180375_.getSource(),
                                                                                            EntityArgument.getEntity(p_180375_, "source"),
                                                                                            SlotArgument.getSlot(p_180375_, "sourceSlot"),
                                                                                            BlockPosArgument.getLoadedBlockPos(p_180375_, "pos"),
                                                                                            SlotArgument.getSlot(p_180375_, "slot")
                                                                                        )
                                                                                )
                                                                                .then(
                                                                                    Commands.argument("modifier", ResourceOrIdArgument.lootModifier(context))
                                                                                        .suggests(SUGGEST_MODIFIER)
                                                                                        .executes(
                                                                                            p_335223_ -> entityToBlock(
                                                                                                    (CommandSourceStack)p_335223_.getSource(),
                                                                                                    EntityArgument.getEntity(p_335223_, "source"),
                                                                                                    SlotArgument.getSlot(p_335223_, "sourceSlot"),
                                                                                                    BlockPosArgument.getLoadedBlockPos(p_335223_, "pos"),
                                                                                                    SlotArgument.getSlot(p_335223_, "slot"),
                                                                                                    ResourceOrIdArgument.getLootModifier(p_335223_, "modifier")
                                                                                                )
                                                                                        )
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(
                            Commands.literal("entity")
                                .then(
                                    Commands.argument("targets", EntityArgument.entities())
                                        .then(
                                            Commands.argument("slot", SlotArgument.slot())
                                                .then(
                                                    Commands.literal("with")
                                                        .then(
                                                            Commands.argument("item", ItemArgument.item(context))
                                                                .executes(
                                                                    p_180371_ -> setEntityItem(
                                                                            p_180371_.getSource(),
                                                                            EntityArgument.getEntities(p_180371_, "targets"),
                                                                            SlotArgument.getSlot(p_180371_, "slot"),
                                                                            ItemArgument.getItem(p_180371_, "item").createItemStack(1, false)
                                                                        )
                                                                )
                                                                .then(
                                                                    Commands.argument("count", IntegerArgumentType.integer(1, 99))
                                                                        .executes(
                                                                            p_180369_ -> setEntityItem(
                                                                                    p_180369_.getSource(),
                                                                                    EntityArgument.getEntities(p_180369_, "targets"),
                                                                                    SlotArgument.getSlot(p_180369_, "slot"),
                                                                                    ItemArgument.getItem(p_180369_, "item")
                                                                                        .createItemStack(
                                                                                            IntegerArgumentType.getInteger(p_180369_, "count"), true
                                                                                        )
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                                .then(
                                                    Commands.literal("from")
                                                        .then(
                                                            Commands.literal("block")
                                                                .then(
                                                                    Commands.argument("source", BlockPosArgument.blockPos())
                                                                        .then(
                                                                            Commands.argument("sourceSlot", SlotArgument.slot())
                                                                                .executes(
                                                                                    p_180367_ -> blockToEntities(
                                                                                            p_180367_.getSource(),
                                                                                            BlockPosArgument.getLoadedBlockPos(p_180367_, "source"),
                                                                                            SlotArgument.getSlot(p_180367_, "sourceSlot"),
                                                                                            EntityArgument.getEntities(p_180367_, "targets"),
                                                                                            SlotArgument.getSlot(p_180367_, "slot")
                                                                                        )
                                                                                )
                                                                                .then(
                                                                                    Commands.argument("modifier", ResourceOrIdArgument.lootModifier(context))
                                                                                        .suggests(SUGGEST_MODIFIER)
                                                                                        .executes(
                                                                                            p_335224_ -> blockToEntities(
                                                                                                    (CommandSourceStack)p_335224_.getSource(),
                                                                                                    BlockPosArgument.getLoadedBlockPos(p_335224_, "source"),
                                                                                                    SlotArgument.getSlot(p_335224_, "sourceSlot"),
                                                                                                    EntityArgument.getEntities(p_335224_, "targets"),
                                                                                                    SlotArgument.getSlot(p_335224_, "slot"),
                                                                                                    ResourceOrIdArgument.getLootModifier(p_335224_, "modifier")
                                                                                                )
                                                                                        )
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                        .then(
                                                            Commands.literal("entity")
                                                                .then(
                                                                    Commands.argument("source", EntityArgument.entity())
                                                                        .then(
                                                                            Commands.argument("sourceSlot", SlotArgument.slot())
                                                                                .executes(
                                                                                    p_180363_ -> entityToEntities(
                                                                                            p_180363_.getSource(),
                                                                                            EntityArgument.getEntity(p_180363_, "source"),
                                                                                            SlotArgument.getSlot(p_180363_, "sourceSlot"),
                                                                                            EntityArgument.getEntities(p_180363_, "targets"),
                                                                                            SlotArgument.getSlot(p_180363_, "slot")
                                                                                        )
                                                                                )
                                                                                .then(
                                                                                    Commands.argument("modifier", ResourceOrIdArgument.lootModifier(context))
                                                                                        .suggests(SUGGEST_MODIFIER)
                                                                                        .executes(
                                                                                            p_335225_ -> entityToEntities(
                                                                                                    (CommandSourceStack)p_335225_.getSource(),
                                                                                                    EntityArgument.getEntity(p_335225_, "source"),
                                                                                                    SlotArgument.getSlot(p_335225_, "sourceSlot"),
                                                                                                    EntityArgument.getEntities(p_335225_, "targets"),
                                                                                                    SlotArgument.getSlot(p_335225_, "slot"),
                                                                                                    ResourceOrIdArgument.getLootModifier(p_335225_, "modifier")
                                                                                                )
                                                                                        )
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .then(
                    Commands.literal("modify")
                        .then(
                            Commands.literal("block")
                                .then(
                                    Commands.argument("pos", BlockPosArgument.blockPos())
                                        .then(
                                            Commands.argument("slot", SlotArgument.slot())
                                                .then(
                                                    Commands.argument("modifier", ResourceOrIdArgument.lootModifier(context))
                                                        .suggests(SUGGEST_MODIFIER)
                                                        .executes(
                                                            p_335222_ -> modifyBlockItem(
                                                                    (CommandSourceStack)p_335222_.getSource(),
                                                                    BlockPosArgument.getLoadedBlockPos(p_335222_, "pos"),
                                                                    SlotArgument.getSlot(p_335222_, "slot"),
                                                                    ResourceOrIdArgument.getLootModifier(p_335222_, "modifier")
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(
                            Commands.literal("entity")
                                .then(
                                    Commands.argument("targets", EntityArgument.entities())
                                        .then(
                                            Commands.argument("slot", SlotArgument.slot())
                                                .then(
                                                    Commands.argument("modifier", ResourceOrIdArgument.lootModifier(context))
                                                        .suggests(SUGGEST_MODIFIER)
                                                        .executes(
                                                            p_335227_ -> modifyEntityItem(
                                                                    (CommandSourceStack)p_335227_.getSource(),
                                                                    EntityArgument.getEntities(p_335227_, "targets"),
                                                                    SlotArgument.getSlot(p_335227_, "slot"),
                                                                    ResourceOrIdArgument.getLootModifier(p_335227_, "modifier")
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int modifyBlockItem(CommandSourceStack source, BlockPos pos, int slot, Holder<LootItemFunction> modifier) throws CommandSyntaxException {
        Container container = getContainer(source, pos, ERROR_TARGET_NOT_A_CONTAINER);
        if (slot >= 0 && slot < container.getContainerSize()) {
            ItemStack itemstack = applyModifier(source, modifier, container.getItem(slot));
            container.setItem(slot, itemstack);
            source.sendSuccess(
                () -> Component.translatable(
                        "commands.item.block.set.success", pos.getX(), pos.getY(), pos.getZ(), itemstack.getDisplayName()
                    ),
                true
            );
            return 1;
        } else {
            throw ERROR_TARGET_INAPPLICABLE_SLOT.create(slot);
        }
    }

    private static int modifyEntityItem(CommandSourceStack source, Collection<? extends Entity> targets, int sourceSlot, Holder<LootItemFunction> modifer) throws CommandSyntaxException {
        Map<Entity, ItemStack> map = Maps.newHashMapWithExpectedSize(targets.size());

        for (Entity entity : targets) {
            SlotAccess slotaccess = entity.getSlot(sourceSlot);
            if (slotaccess != SlotAccess.NULL) {
                ItemStack itemstack = applyModifier(source, modifer, slotaccess.get().copy());
                if (slotaccess.set(itemstack)) {
                    map.put(entity, itemstack);
                    if (entity instanceof ServerPlayer) {
                        ((ServerPlayer)entity).containerMenu.broadcastChanges();
                    }
                }
            }
        }

        if (map.isEmpty()) {
            throw ERROR_TARGET_NO_CHANGES.create(sourceSlot);
        } else {
            if (map.size() == 1) {
                Entry<Entity, ItemStack> entry = map.entrySet().iterator().next();
                source.sendSuccess(
                    () -> Component.translatable("commands.item.entity.set.success.single", entry.getKey().getDisplayName(), entry.getValue().getDisplayName()),
                    true
                );
            } else {
                source.sendSuccess(() -> Component.translatable("commands.item.entity.set.success.multiple", map.size()), true);
            }

            return map.size();
        }
    }

    private static int setBlockItem(CommandSourceStack source, BlockPos pos, int slot, ItemStack item) throws CommandSyntaxException {
        Container container = getContainer(source, pos, ERROR_TARGET_NOT_A_CONTAINER);
        if (slot >= 0 && slot < container.getContainerSize()) {
            container.setItem(slot, item);
            source.sendSuccess(
                () -> Component.translatable(
                        "commands.item.block.set.success", pos.getX(), pos.getY(), pos.getZ(), item.getDisplayName()
                    ),
                true
            );
            return 1;
        } else {
            throw ERROR_TARGET_INAPPLICABLE_SLOT.create(slot);
        }
    }

    static Container getContainer(CommandSourceStack source, BlockPos pos, Dynamic3CommandExceptionType exception) throws CommandSyntaxException {
        BlockEntity blockentity = source.getLevel().getBlockEntity(pos);
        if (!(blockentity instanceof Container)) {
            throw exception.create(pos.getX(), pos.getY(), pos.getZ());
        } else {
            return (Container)blockentity;
        }
    }

    private static int setEntityItem(CommandSourceStack source, Collection<? extends Entity> targets, int slot, ItemStack item) throws CommandSyntaxException {
        List<Entity> list = Lists.newArrayListWithCapacity(targets.size());

        for (Entity entity : targets) {
            SlotAccess slotaccess = entity.getSlot(slot);
            if (slotaccess != SlotAccess.NULL && slotaccess.set(item.copy())) {
                list.add(entity);
                if (entity instanceof ServerPlayer) {
                    ((ServerPlayer)entity).containerMenu.broadcastChanges();
                }
            }
        }

        if (list.isEmpty()) {
            throw ERROR_TARGET_NO_CHANGES_KNOWN_ITEM.create(item.getDisplayName(), slot);
        } else {
            if (list.size() == 1) {
                source.sendSuccess(
                    () -> Component.translatable("commands.item.entity.set.success.single", list.iterator().next().getDisplayName(), item.getDisplayName()),
                    true
                );
            } else {
                source.sendSuccess(() -> Component.translatable("commands.item.entity.set.success.multiple", list.size(), item.getDisplayName()), true);
            }

            return list.size();
        }
    }

    private static int blockToEntities(CommandSourceStack source, BlockPos pos, int sourceSlot, Collection<? extends Entity> targets, int slot) throws CommandSyntaxException {
        return setEntityItem(source, targets, slot, getBlockItem(source, pos, sourceSlot));
    }

    private static int blockToEntities(
        CommandSourceStack source,
        BlockPos pos,
        int sourceSlot,
        Collection<? extends Entity> targets,
        int slot,
        Holder<LootItemFunction> modifier
    ) throws CommandSyntaxException {
        return setEntityItem(source, targets, slot, applyModifier(source, modifier, getBlockItem(source, pos, sourceSlot)));
    }

    private static int blockToBlock(CommandSourceStack source, BlockPos sourcePos, int sourceSlot, BlockPos pos, int slot) throws CommandSyntaxException {
        return setBlockItem(source, pos, slot, getBlockItem(source, sourcePos, sourceSlot));
    }

    private static int blockToBlock(
        CommandSourceStack source, BlockPos sourcePos, int sourceSlot, BlockPos pos, int slot, Holder<LootItemFunction> modifier
    ) throws CommandSyntaxException {
        return setBlockItem(source, pos, slot, applyModifier(source, modifier, getBlockItem(source, sourcePos, sourceSlot)));
    }

    private static int entityToBlock(CommandSourceStack source, Entity sourceEntity, int sourceSlot, BlockPos pos, int slot) throws CommandSyntaxException {
        return setBlockItem(source, pos, slot, getEntityItem(sourceEntity, sourceSlot));
    }

    private static int entityToBlock(
        CommandSourceStack source, Entity sourceEntity, int sourceSlot, BlockPos pos, int slot, Holder<LootItemFunction> modifier
    ) throws CommandSyntaxException {
        return setBlockItem(source, pos, slot, applyModifier(source, modifier, getEntityItem(sourceEntity, sourceSlot)));
    }

    private static int entityToEntities(CommandSourceStack source, Entity sourceEntity, int sourceSlot, Collection<? extends Entity> targets, int slot) throws CommandSyntaxException {
        return setEntityItem(source, targets, slot, getEntityItem(sourceEntity, sourceSlot));
    }

    private static int entityToEntities(
        CommandSourceStack source,
        Entity sourceEntity,
        int sourceSlot,
        Collection<? extends Entity> targets,
        int slot,
        Holder<LootItemFunction> modifier
    ) throws CommandSyntaxException {
        return setEntityItem(source, targets, slot, applyModifier(source, modifier, getEntityItem(sourceEntity, sourceSlot)));
    }

    private static ItemStack applyModifier(CommandSourceStack source, Holder<LootItemFunction> modifier, ItemStack stack) {
        ServerLevel serverlevel = source.getLevel();
        LootParams lootparams = new LootParams.Builder(serverlevel)
            .withParameter(LootContextParams.ORIGIN, source.getPosition())
            .withOptionalParameter(LootContextParams.THIS_ENTITY, source.getEntity())
            .create(LootContextParamSets.COMMAND);
        LootContext lootcontext = new LootContext.Builder(lootparams).create(Optional.empty());
        lootcontext.pushVisitedElement(LootContext.createVisitedEntry(modifier.value()));
        ItemStack itemstack = modifier.value().apply(stack, lootcontext);
        itemstack.limitSize(itemstack.getMaxStackSize());
        return itemstack;
    }

    private static ItemStack getEntityItem(Entity entity, int slot) throws CommandSyntaxException {
        SlotAccess slotaccess = entity.getSlot(slot);
        if (slotaccess == SlotAccess.NULL) {
            throw ERROR_SOURCE_INAPPLICABLE_SLOT.create(slot);
        } else {
            return slotaccess.get().copy();
        }
    }

    private static ItemStack getBlockItem(CommandSourceStack source, BlockPos pos, int slot) throws CommandSyntaxException {
        Container container = getContainer(source, pos, ERROR_SOURCE_NOT_A_CONTAINER);
        if (slot >= 0 && slot < container.getContainerSize()) {
            return container.getItem(slot).copy();
        } else {
            throw ERROR_SOURCE_INAPPLICABLE_SLOT.create(slot);
        }
    }
}
