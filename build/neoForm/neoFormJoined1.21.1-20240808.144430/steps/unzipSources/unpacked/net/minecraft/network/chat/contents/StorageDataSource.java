package net.minecraft.network.chat.contents;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public record StorageDataSource(ResourceLocation id) implements DataSource {
    public static final MapCodec<StorageDataSource> SUB_CODEC = RecordCodecBuilder.mapCodec(
        p_304609_ -> p_304609_.group(ResourceLocation.CODEC.fieldOf("storage").forGetter(StorageDataSource::id)).apply(p_304609_, StorageDataSource::new)
    );
    public static final DataSource.Type<StorageDataSource> TYPE = new DataSource.Type<>(SUB_CODEC, "storage");

    @Override
    public Stream<CompoundTag> getData(CommandSourceStack source) {
        CompoundTag compoundtag = source.getServer().getCommandStorage().get(this.id);
        return Stream.of(compoundtag);
    }

    @Override
    public DataSource.Type<?> type() {
        return TYPE;
    }

    @Override
    public String toString() {
        return "storage=" + this.id;
    }
}
