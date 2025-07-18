/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.VersionChecker;
import net.neoforged.fml.i18n.FMLTranslations;
import net.neoforged.fml.i18n.MavenVersionTranslator;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.StringUtils;
import net.neoforged.neoforge.client.gui.widget.ModListWidget;
import net.neoforged.neoforge.client.gui.widget.ScrollPanel;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.util.Size2i;
import net.neoforged.neoforge.resource.ResourcePackLoader;
import net.neoforged.neoforgespi.language.IModInfo;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.artifact.versioning.ComparableVersion;

public class ModListScreen extends Screen {
    private static String stripControlCodes(String value) {
        return net.minecraft.util.StringUtil.stripColor(value);
    }

    private static final Logger LOGGER = LogManager.getLogger();

    private enum SortType implements Comparator<ModContainer> {
        NORMAL,
        A_TO_Z {
            @Override
            protected int compare(String name1, String name2) {
                return name1.compareTo(name2);
            }
        },
        Z_TO_A {
            @Override
            protected int compare(String name1, String name2) {
                return name2.compareTo(name1);
            }
        };

        Button button;

        protected int compare(String name1, String name2) {
            return 0;
        }

        @Override
        public int compare(ModContainer o1, ModContainer o2) {
            String name1 = StringUtils.toLowerCase(stripControlCodes(o1.getModInfo().getDisplayName()));
            String name2 = StringUtils.toLowerCase(stripControlCodes(o2.getModInfo().getDisplayName()));
            return compare(name1, name2);
        }

        Component getButtonText() {
            return Component.translatable("fml.menu.mods." + StringUtils.toLowerCase(name()));
        }
    }

    private static final int PADDING = 6;

    private Screen parentScreen;

    private ModListWidget modList;
    private InfoPanel modInfo;
    private ModListWidget.ModEntry selected = null;
    private int listWidth;
    private List<ModContainer> mods;
    private final List<ModContainer> unsortedMods;
    private Button configButton, openModsFolderButton, doneButton;

    private int buttonMargin = 1;
    private int numButtons = SortType.values().length;
    private String lastFilterText = "";

    private EditBox search;

    private boolean sorted = false;
    private SortType sortType = SortType.NORMAL;

    public ModListScreen(Screen parentScreen) {
        super(Component.translatable("fml.menu.mods.title"));
        this.parentScreen = parentScreen;
        this.mods = Collections.unmodifiableList(ModList.get().getSortedMods());
        this.unsortedMods = Collections.unmodifiableList(this.mods);
    }

    class InfoPanel extends ScrollPanel {
        private ResourceLocation logoPath;
        private Size2i logoDims = new Size2i(0, 0);
        private List<FormattedCharSequence> lines = Collections.emptyList();

        InfoPanel(Minecraft mcIn, int widthIn, int heightIn, int topIn) {
            super(mcIn, widthIn, heightIn, topIn, modList.getRight() + PADDING);
        }

        void setInfo(List<String> lines, ResourceLocation logoPath, Size2i logoDims) {
            this.logoPath = logoPath;
            this.logoDims = logoDims;
            this.lines = resizeContent(lines);
        }

        void clearInfo() {
            this.logoPath = null;
            this.logoDims = new Size2i(0, 0);
            this.lines = Collections.emptyList();
        }

        private List<FormattedCharSequence> resizeContent(List<String> lines) {
            List<FormattedCharSequence> ret = new ArrayList<>();
            for (String line : lines) {
                if (line == null) {
                    ret.add(null);
                    continue;
                }

                Component chat = CommonHooks.newChatWithLinks(line, false);
                int maxTextLength = this.width - 12;
                if (maxTextLength >= 0) {
                    ret.addAll(Language.getInstance().getVisualOrder(font.getSplitter().splitLines(chat, maxTextLength, Style.EMPTY)));
                }
            }
            return ret;
        }

        @Override
        public int getContentHeight() {
            int height = 50;
            height += (lines.size() * font.lineHeight);
            if (height < this.bottom - this.top - 8)
                height = this.bottom - this.top - 8;
            return height;
        }

        @Override
        protected int getScrollAmount() {
            return font.lineHeight * 3;
        }

        @Override
        protected void drawPanel(GuiGraphics guiGraphics, int entryRight, int relativeY, Tesselator tess, int mouseX, int mouseY) {
            if (logoPath != null) {
                RenderSystem.enableBlend();
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                // Draw the logo image inscribed in a rectangle with width entryWidth (minus some padding) and height 50
                int headerHeight = 50;
                guiGraphics.blitInscribed(logoPath, left + PADDING, relativeY, width - (PADDING * 2), headerHeight, logoDims.width, logoDims.height, false, true);
                relativeY += headerHeight + PADDING;
            }

            for (FormattedCharSequence line : lines) {
                if (line != null) {
                    RenderSystem.enableBlend();
                    guiGraphics.drawString(ModListScreen.this.font, line, left + PADDING, relativeY, 0xFFFFFF);
                    RenderSystem.disableBlend();
                }
                relativeY += font.lineHeight;
            }

            final Style component = findTextLine(mouseX, mouseY);
            if (component != null) {
                guiGraphics.renderComponentHoverEffect(ModListScreen.this.font, component, mouseX, mouseY);
            }
        }

        private Style findTextLine(final int mouseX, final int mouseY) {
            if (!isMouseOver(mouseX, mouseY))
                return null;

            double offset = (mouseY - top - PADDING - border) + scrollDistance;
            if (logoPath != null) {
                offset -= 50;
            }
            if (offset <= 0)
                return null;

            int lineIdx = (int) (offset / font.lineHeight);
            if (lineIdx >= lines.size() || lineIdx < 0)
                return null;

            FormattedCharSequence line = lines.get(lineIdx);
            if (line != null) {
                return font.getSplitter().componentStyleAtWidth(line, mouseX - left - border - 1);
            }
            return null;
        }

        @Override
        public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
            final Style component = findTextLine((int) mouseX, (int) mouseY);
            if (component != null) {
                ModListScreen.this.handleComponentClicked(component);
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public NarrationPriority narrationPriority() {
            return NarrationPriority.NONE;
        }

        @Override
        public void updateNarration(NarrationElementOutput p_169152_) {}
    }

    @Override
    public void init() {
        for (var mod : mods) {
            listWidth = Math.max(listWidth, getFontRenderer().width(mod.getModInfo().getDisplayName()) + 10);
            listWidth = Math.max(listWidth, getFontRenderer().width(MavenVersionTranslator.artifactVersionToString(mod.getModInfo().getVersion())) + 5);
        }
        listWidth = Math.max(Math.min(listWidth, width / 3), 100);
        listWidth += listWidth % numButtons != 0 ? (numButtons - listWidth % numButtons) : 0;

        int modInfoWidth = this.width - this.listWidth - (PADDING * 3);
        int doneButtonWidth = Math.min(modInfoWidth, 200);
        int y = this.height - 20 - PADDING;
        int fullButtonHeight = PADDING + 20 + PADDING;

        doneButton = Button.builder(Component.translatable("gui.done"), b -> ModListScreen.this.onClose()).bounds(((listWidth + PADDING + this.width - doneButtonWidth) / 2), y, doneButtonWidth, 20).build();
        openModsFolderButton = Button.builder(Component.translatable("fml.menu.mods.openmodsfolder"), b -> Util.getPlatform().openFile(FMLPaths.MODSDIR.get().toFile())).bounds(6, y, this.listWidth, 20).build();
        y -= 20 + PADDING;
        configButton = Button.builder(Component.translatable("fml.menu.mods.config"), b -> ModListScreen.this.displayModConfig()).bounds(6, y, this.listWidth, 20).build();
        y -= 14 + PADDING;
        search = new EditBox(getFontRenderer(), PADDING, y, listWidth, 14, Component.translatable("fml.menu.mods.search"));

        this.modList = new ModListWidget(this, listWidth, fullButtonHeight, search.getY() - getFontRenderer().lineHeight - PADDING);
        this.modList.setX(6);
        this.modInfo = new InfoPanel(this.minecraft, modInfoWidth, this.height - PADDING - fullButtonHeight, PADDING);

        this.addRenderableWidget(modList);
        this.addRenderableWidget(modInfo);
        this.addRenderableWidget(search);
        this.addRenderableWidget(doneButton);
        this.addRenderableWidget(configButton);
        this.addRenderableWidget(openModsFolderButton);

        search.setFocused(false);
        search.setCanLoseFocus(true);
        configButton.active = false;

        final int width = listWidth / numButtons;
        int x = PADDING;
        addRenderableWidget(SortType.NORMAL.button = Button.builder(SortType.NORMAL.getButtonText(), b -> resortMods(SortType.NORMAL)).bounds(x, PADDING, width - buttonMargin, 20).build());
        x += width + buttonMargin;
        addRenderableWidget(SortType.A_TO_Z.button = Button.builder(SortType.A_TO_Z.getButtonText(), b -> resortMods(SortType.A_TO_Z)).bounds(x, PADDING, width - buttonMargin, 20).build());
        x += width + buttonMargin;
        addRenderableWidget(SortType.Z_TO_A.button = Button.builder(SortType.Z_TO_A.getButtonText(), b -> resortMods(SortType.Z_TO_A)).bounds(x, PADDING, width - buttonMargin, 20).build());
        resortMods(SortType.NORMAL);
        updateCache();
    }

    private void displayModConfig() {
        if (selected == null) return;
        try {
            IConfigScreenFactory.getForMod(selected.getInfo()).map(f -> f.createScreen(selected.getContainer(), this)).ifPresent(newScreen -> this.minecraft.setScreen(newScreen));
        } catch (final Exception e) {
            LOGGER.error("There was a critical issue trying to build the config GUI for {}", selected.getInfo().getModId(), e);
        }
    }

    @Override
    public void tick() {
        modList.setSelected(selected);

        if (!search.getValue().equals(lastFilterText)) {
            reloadMods();
            sorted = false;
        }

        if (!sorted) {
            reloadMods();
            mods.sort(sortType);
            modList.refreshList();
            if (selected != null) {
                selected = modList.children().stream().filter(e -> e.getInfo() == selected.getInfo()).findFirst().orElse(null);
                updateCache();
            }
            sorted = true;
        }
    }

    public <T extends ObjectSelectionList.Entry<T>> void buildModList(Consumer<T> modListViewConsumer, Function<ModContainer, T> newEntry) {
        mods.forEach(mod -> modListViewConsumer.accept(newEntry.apply(mod)));
    }

    private void reloadMods() {
        this.mods = this.unsortedMods.stream().filter(mi -> StringUtils.toLowerCase(stripControlCodes(mi.getModInfo().getDisplayName())).contains(StringUtils.toLowerCase(search.getValue()))).collect(Collectors.toList());
        lastFilterText = search.getValue();
    }

    private void resortMods(SortType newSort) {
        this.sortType = newSort;

        for (SortType sort : SortType.values()) {
            if (sort.button != null)
                sort.button.active = sortType != sort;
        }
        sorted = false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        Component text = Component.translatable("fml.menu.mods.search");
        int x = modList.getX() + ((modList.getRight() - modList.getX()) / 2) - (getFontRenderer().width(text) / 2);
        guiGraphics.drawString(getFontRenderer(), text.getVisualOrderText(), x, search.getY() - getFontRenderer().lineHeight, 0xFFFFFF, false);
    }

    public Minecraft getMinecraftInstance() {
        return minecraft;
    }

    public Font getFontRenderer() {
        return font;
    }

    public void setSelected(ModListWidget.ModEntry entry) {
        this.selected = entry;
        updateCache();
    }

    private void updateCache() {
        if (selected == null) {
            this.configButton.active = false;
            this.modInfo.clearInfo();
            return;
        }
        IModInfo selectedMod = selected.getInfo();
        this.configButton.active = IConfigScreenFactory.getForMod(selectedMod).isPresent();
        List<String> lines = new ArrayList<>();
        VersionChecker.CheckResult vercheck = VersionChecker.getResult(selectedMod);

        @SuppressWarnings("resource")
        Pair<ResourceLocation, Size2i> logoData;

        if (selectedMod.getModId().equals(ResourceLocation.DEFAULT_NAMESPACE)) {
            logoData = Pair.of(LogoRenderer.MINECRAFT_LOGO, new Size2i(LogoRenderer.LOGO_TEXTURE_WIDTH, LogoRenderer.LOGO_TEXTURE_HEIGHT));
        } else {
            logoData = selectedMod.getLogoFile().map(logoFile -> {
                TextureManager tm = this.minecraft.getTextureManager();
                final Pack.ResourcesSupplier resourcePack = ResourcePackLoader.getPackFor(selectedMod.getModId())
                        .orElse(ResourcePackLoader.getPackFor("neoforge").orElseThrow(() -> new RuntimeException("Can't find neoforge, WHAT!")));
                try (PackResources packResources = resourcePack.openPrimary(new PackLocationInfo("mod/" + selectedMod.getModId(), Component.empty(), PackSource.BUILT_IN, Optional.empty()))) {
                    NativeImage logo = null;
                    IoSupplier<InputStream> logoResource = packResources.getRootResource(logoFile.split("[/\\\\]"));
                    if (logoResource != null)
                        logo = NativeImage.read(logoResource.get());
                    if (logo != null) {

                        return Pair.of(tm.register("modlogo", new DynamicTexture(logo) {
                            @Override
                            public void upload() {
                                this.bind();
                                NativeImage td = this.getPixels();
                                // Use custom "blur" value which controls texture filtering (nearest-neighbor vs linear)
                                this.getPixels().upload(0, 0, 0, 0, 0, td.getWidth(), td.getHeight(), selectedMod.getLogoBlur(), false, false, false);
                            }
                        }), new Size2i(logo.getWidth(), logo.getHeight()));
                    }
                } catch (IOException | IllegalArgumentException e) {}
                return Pair.<ResourceLocation, Size2i>of(null, new Size2i(0, 0));
            }).orElse(Pair.of(null, new Size2i(0, 0)));
        }

        lines.add(selectedMod.getDisplayName());
        lines.add(FMLTranslations.parseMessage("fml.menu.mods.info.version", MavenVersionTranslator.artifactVersionToString(selectedMod.getVersion())));
        lines.add(FMLTranslations.parseMessage("fml.menu.mods.info.idstate", selectedMod.getModId(), "LOADED")); // TODO: remove mod loading stages from here too

        // Normalizing line endings to LF because it is currently not automatically handled for us. Descriptions are already normalized.
        selectedMod.getConfig().getConfigElement("credits").ifPresent(credits -> lines.add(FMLTranslations.parseMessage("fml.menu.mods.info.credits", credits).replace("\r\n", "\n")));
        selectedMod.getConfig().getConfigElement("authors").ifPresent(authors -> lines.add(FMLTranslations.parseMessage("fml.menu.mods.info.authors", authors).replace("\r\n", "\n")));
        selectedMod.getConfig().getConfigElement("displayURL").ifPresent(displayURL -> lines.add(FMLTranslations.parseMessage("fml.menu.mods.info.displayurl", displayURL).replace("\r\n", "\n")));
        if (selectedMod.getOwningFile() == null || selectedMod.getOwningFile().getMods().size() == 1)
            lines.add(FMLTranslations.parseMessage("fml.menu.mods.info.nochildmods"));
        else
            lines.add(FMLTranslations.parseMessage("fml.menu.mods.info.childmods", selectedMod.getOwningFile().getMods().stream().map(IModInfo::getDisplayName).collect(Collectors.joining(","))));

        if (vercheck.status() == VersionChecker.Status.OUTDATED || vercheck.status() == VersionChecker.Status.BETA_OUTDATED)
            lines.add(FMLTranslations.parseMessage("fml.menu.mods.info.updateavailable", vercheck.url() == null ? "" : vercheck.url()).replace("\r\n", "\n"));
        lines.add(FMLTranslations.parseMessage("fml.menu.mods.info.license", selectedMod.getOwningFile().getLicense()).replace("\r\n", "\n"));
        lines.add(null);
        lines.add(FMLTranslations.getPattern("fml.menu.mods.info.description." + selectedMod.getModId(), selectedMod::getDescription));

        /* Removed because people bitched that this information was misleading.
        lines.add(null);
        if (FMLEnvironment.secureJarsEnabled) {
            lines.add(ForgeI18getOwningFile().getFile().n.parseMessage("fml.menu.mods.info.signature", selectedMod.getOwningFile().getCodeSigningFingerprint().orElse(ForgeI18n.parseMessage("fml.menu.mods.info.signature.unsigned"))));
            lines.add(ForgeI18n.parseMessage("fml.menu.mods.info.trust", selectedMod.getOwningFile().getTrustData().orElse(ForgeI18n.parseMessage("fml.menu.mods.info.trust.noauthority"))));
        } else {
            lines.add(ForgeI18n.parseMessage("fml.menu.mods.info.securejardisabled"));
        }
        */

        if ((vercheck.status() == VersionChecker.Status.OUTDATED || vercheck.status() == VersionChecker.Status.BETA_OUTDATED) && vercheck.changes().size() > 0) {
            lines.add(null);
            lines.add(FMLTranslations.parseMessage("fml.menu.mods.info.changelogheader"));
            for (Entry<ComparableVersion, String> entry : vercheck.changes().entrySet()) {
                lines.add("  " + entry.getKey() + ":");
                lines.add(entry.getValue());
                lines.add(null);
            }
        }

        modInfo.setInfo(lines, logoData.getLeft(), logoData.getRight());
    }

    @Override
    public void resize(Minecraft mc, int width, int height) {
        String s = this.search.getValue();
        SortType sort = this.sortType;
        ModListWidget.ModEntry selected = this.selected;
        this.init(mc, width, height);
        this.search.setValue(s);
        this.selected = selected;
        if (!this.search.getValue().isEmpty())
            reloadMods();
        if (sort != SortType.NORMAL)
            resortMods(sort);
        updateCache();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parentScreen);
    }
}
