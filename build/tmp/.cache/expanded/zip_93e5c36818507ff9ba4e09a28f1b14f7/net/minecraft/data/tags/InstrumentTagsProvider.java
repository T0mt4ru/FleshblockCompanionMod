package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.InstrumentTags;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.Instruments;

public class InstrumentTagsProvider extends TagsProvider<Instrument> {
    /**
 * @deprecated Forge: Use the {@linkplain #InstrumentTagsProvider(PackOutput,
 *             CompletableFuture, String,
 *             net.neoforged.neoforge.common.data.ExistingFileHelper) mod id
 *             variant}
 */
    @Deprecated
    public InstrumentTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, Registries.INSTRUMENT, provider);
    }
    public InstrumentTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, String modId, @org.jetbrains.annotations.Nullable net.neoforged.neoforge.common.data.ExistingFileHelper existingFileHelper) {
        super(output, Registries.INSTRUMENT, provider, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(InstrumentTags.REGULAR_GOAT_HORNS)
            .add(Instruments.PONDER_GOAT_HORN)
            .add(Instruments.SING_GOAT_HORN)
            .add(Instruments.SEEK_GOAT_HORN)
            .add(Instruments.FEEL_GOAT_HORN);
        this.tag(InstrumentTags.SCREAMING_GOAT_HORNS)
            .add(Instruments.ADMIRE_GOAT_HORN)
            .add(Instruments.CALL_GOAT_HORN)
            .add(Instruments.YEARN_GOAT_HORN)
            .add(Instruments.DREAM_GOAT_HORN);
        this.tag(InstrumentTags.GOAT_HORNS).addTag(InstrumentTags.REGULAR_GOAT_HORNS).addTag(InstrumentTags.SCREAMING_GOAT_HORNS);
    }
}
