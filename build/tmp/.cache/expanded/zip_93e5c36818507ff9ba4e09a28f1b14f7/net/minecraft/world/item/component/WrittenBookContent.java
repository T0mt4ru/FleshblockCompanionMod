package net.minecraft.world.item.component;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.network.Filterable;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Player;

public record WrittenBookContent(Filterable<String> title, String author, int generation, List<Filterable<Component>> pages, boolean resolved)
    implements BookContent<Component, WrittenBookContent> {
    public static final WrittenBookContent EMPTY = new WrittenBookContent(Filterable.passThrough(""), "", 0, List.of(), true);
    public static final int PAGE_LENGTH = 32767;
    public static final int TITLE_LENGTH = 16;
    public static final int TITLE_MAX_LENGTH = 32;
    public static final int MAX_GENERATION = 3;
    public static final int MAX_CRAFTABLE_GENERATION = 2;
    public static final Codec<Component> CONTENT_CODEC = ComponentSerialization.flatCodec(32767);
    public static final Codec<List<Filterable<Component>>> PAGES_CODEC = pagesCodec(CONTENT_CODEC);
    public static final Codec<WrittenBookContent> CODEC = RecordCodecBuilder.create(
        p_331461_ -> p_331461_.group(
                    Filterable.codec(Codec.string(0, 32)).fieldOf("title").forGetter(WrittenBookContent::title),
                    Codec.STRING.fieldOf("author").forGetter(WrittenBookContent::author),
                    ExtraCodecs.intRange(0, 3).optionalFieldOf("generation", 0).forGetter(WrittenBookContent::generation),
                    PAGES_CODEC.optionalFieldOf("pages", List.of()).forGetter(WrittenBookContent::pages),
                    Codec.BOOL.optionalFieldOf("resolved", Boolean.valueOf(false)).forGetter(WrittenBookContent::resolved)
                )
                .apply(p_331461_, WrittenBookContent::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, WrittenBookContent> STREAM_CODEC = StreamCodec.composite(
        Filterable.streamCodec(ByteBufCodecs.stringUtf8(32)),
        WrittenBookContent::title,
        ByteBufCodecs.STRING_UTF8,
        WrittenBookContent::author,
        ByteBufCodecs.VAR_INT,
        WrittenBookContent::generation,
        Filterable.streamCodec(ComponentSerialization.STREAM_CODEC).apply(ByteBufCodecs.list()),
        WrittenBookContent::pages,
        ByteBufCodecs.BOOL,
        WrittenBookContent::resolved,
        WrittenBookContent::new
    );

    public WrittenBookContent(Filterable<String> title, String author, int generation, List<Filterable<Component>> pages, boolean resolved) {
        if (generation >= 0 && generation <= 3) {
            this.title = title;
            this.author = author;
            this.generation = generation;
            this.pages = pages;
            this.resolved = resolved;
        } else {
            throw new IllegalArgumentException("Generation was " + generation + ", but must be between 0 and 3");
        }
    }

    private static Codec<Filterable<Component>> pageCodec(Codec<Component> codec) {
        return Filterable.codec(codec);
    }

    public static Codec<List<Filterable<Component>>> pagesCodec(Codec<Component> codec) {
        return pageCodec(codec).listOf();
    }

    @Nullable
    public WrittenBookContent tryCraftCopy() {
        return this.generation >= 2 ? null : new WrittenBookContent(this.title, this.author, this.generation + 1, this.pages, this.resolved);
    }

    @Nullable
    public WrittenBookContent resolve(CommandSourceStack source, @Nullable Player player) {
        if (this.resolved) {
            return null;
        } else {
            Builder<Filterable<Component>> builder = ImmutableList.builderWithExpectedSize(this.pages.size());

            for (Filterable<Component> filterable : this.pages) {
                Optional<Filterable<Component>> optional = resolvePage(source, player, filterable);
                if (optional.isEmpty()) {
                    return null;
                }

                builder.add(optional.get());
            }

            return new WrittenBookContent(this.title, this.author, this.generation, builder.build(), true);
        }
    }

    public WrittenBookContent markResolved() {
        return new WrittenBookContent(this.title, this.author, this.generation, this.pages, true);
    }

    private static Optional<Filterable<Component>> resolvePage(CommandSourceStack source, @Nullable Player player, Filterable<Component> pages) {
        return pages.resolve(p_331526_ -> {
            try {
                Component component = ComponentUtils.updateForEntity(source, p_331526_, player, 0);
                return isPageTooLarge(component, source.registryAccess()) ? Optional.empty() : Optional.of(component);
            } catch (Exception exception) {
                return Optional.of(p_331526_);
            }
        });
    }

    private static boolean isPageTooLarge(Component page, HolderLookup.Provider registryAccess) {
        return Component.Serializer.toJson(page, registryAccess).length() > 32767;
    }

    public List<Component> getPages(boolean filtered) {
        return Lists.transform(this.pages, p_332134_ -> p_332134_.get(filtered));
    }

    public WrittenBookContent withReplacedPages(List<Filterable<Component>> newPages) {
        return new WrittenBookContent(this.title, this.author, this.generation, newPages, false);
    }
}
