package net.minecraft.server.commands;

import com.google.common.collect.Sets;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.Set;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.world.entity.Entity;

public class TagCommand {
    private static final SimpleCommandExceptionType ERROR_ADD_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.tag.add.failed"));
    private static final SimpleCommandExceptionType ERROR_REMOVE_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.tag.remove.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("tag")
                .requires(p_138844_ -> p_138844_.hasPermission(2))
                .then(
                    Commands.argument("targets", EntityArgument.entities())
                        .then(
                            Commands.literal("add")
                                .then(
                                    Commands.argument("name", StringArgumentType.word())
                                        .executes(
                                            p_138861_ -> addTag(
                                                    p_138861_.getSource(),
                                                    EntityArgument.getEntities(p_138861_, "targets"),
                                                    StringArgumentType.getString(p_138861_, "name")
                                                )
                                        )
                                )
                        )
                        .then(
                            Commands.literal("remove")
                                .then(
                                    Commands.argument("name", StringArgumentType.word())
                                        .suggests(
                                            (p_138841_, p_138842_) -> SharedSuggestionProvider.suggest(
                                                    getTags(EntityArgument.getEntities(p_138841_, "targets")), p_138842_
                                                )
                                        )
                                        .executes(
                                            p_138855_ -> removeTag(
                                                    p_138855_.getSource(),
                                                    EntityArgument.getEntities(p_138855_, "targets"),
                                                    StringArgumentType.getString(p_138855_, "name")
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("list").executes(p_138839_ -> listTags(p_138839_.getSource(), EntityArgument.getEntities(p_138839_, "targets"))))
                )
        );
    }

    /**
     * Gets all tags that are present on at least one of the given entities.
     */
    private static Collection<String> getTags(Collection<? extends Entity> entities) {
        Set<String> set = Sets.newHashSet();

        for (Entity entity : entities) {
            set.addAll(entity.getTags());
        }

        return set;
    }

    private static int addTag(CommandSourceStack source, Collection<? extends Entity> entities, String tagName) throws CommandSyntaxException {
        int i = 0;

        for (Entity entity : entities) {
            if (entity.addTag(tagName)) {
                i++;
            }
        }

        if (i == 0) {
            throw ERROR_ADD_FAILED.create();
        } else {
            if (entities.size() == 1) {
                source.sendSuccess(
                    () -> Component.translatable("commands.tag.add.success.single", tagName, entities.iterator().next().getDisplayName()), true
                );
            } else {
                source.sendSuccess(() -> Component.translatable("commands.tag.add.success.multiple", tagName, entities.size()), true);
            }

            return i;
        }
    }

    private static int removeTag(CommandSourceStack source, Collection<? extends Entity> entities, String tagName) throws CommandSyntaxException {
        int i = 0;

        for (Entity entity : entities) {
            if (entity.removeTag(tagName)) {
                i++;
            }
        }

        if (i == 0) {
            throw ERROR_REMOVE_FAILED.create();
        } else {
            if (entities.size() == 1) {
                source.sendSuccess(
                    () -> Component.translatable("commands.tag.remove.success.single", tagName, entities.iterator().next().getDisplayName()), true
                );
            } else {
                source.sendSuccess(() -> Component.translatable("commands.tag.remove.success.multiple", tagName, entities.size()), true);
            }

            return i;
        }
    }

    private static int listTags(CommandSourceStack source, Collection<? extends Entity> entities) {
        Set<String> set = Sets.newHashSet();

        for (Entity entity : entities) {
            set.addAll(entity.getTags());
        }

        if (entities.size() == 1) {
            Entity entity1 = entities.iterator().next();
            if (set.isEmpty()) {
                source.sendSuccess(() -> Component.translatable("commands.tag.list.single.empty", entity1.getDisplayName()), false);
            } else {
                source.sendSuccess(
                    () -> Component.translatable("commands.tag.list.single.success", entity1.getDisplayName(), set.size(), ComponentUtils.formatList(set)),
                    false
                );
            }
        } else if (set.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("commands.tag.list.multiple.empty", entities.size()), false);
        } else {
            source.sendSuccess(
                () -> Component.translatable("commands.tag.list.multiple.success", entities.size(), set.size(), ComponentUtils.formatList(set)), false
            );
        }

        return set.size();
    }
}
