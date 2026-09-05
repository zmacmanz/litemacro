package com.alfre.macrobuilder;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletionException;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

final class MacroMarketplaceScreen extends Screen {
   private static final int BG = 0xFF18212B;
   private static final int GRID = 0x332B4153;
   private static final int PANEL = 0xEE101820;
   private static final int PANEL_2 = 0xF01D2935;
   private static final int PANEL_3 = 0xFF223140;
   private static final int BORDER = 0xFF385064;
   private static final int BORDER_HI = 0xFF5B7C96;
   private static final int TEXT = 0xFFEFF6FF;
   private static final int MUTED = 0xFF9BAFC2;
   private static final int DIM = 0xFF6F8192;
   private static final int ACCENT = 0xFF22D3EE;
   private static final int GREEN = 0xFF35E887;
   private static final int ORANGE = 0xFFF59E42;
   private static final int RED = 0xFFFF7088;
   private static final int CARD_WIDTH = 156;
   private static final int CARD_HEIGHT = 128;
   private static final int CARD_GAP = 10;
   private static final int PAGE_SIZE = 12;
   private static final int TOP_BAR_HEIGHT = 62;
   private static final int TOP_BAR_CONTROL_Y = 34;
   private static final int MIN_LAYOUT_WIDTH = 960;
   private static final int MIN_LAYOUT_HEIGHT = 540;
   private final MacroRunner runner;
   private final int returnPage;
   private final List<LitemacroMarketplacePreset> allPresets = new ArrayList<>();
   private List<LitemacroMarketplacePreset> filteredPresets = List.of();
   private LitemacroMarketplacePreset selectedPreset;
   private LitemacroMarketplaceService.ListingMode sortMode = LitemacroMarketplaceService.ListingMode.TRENDING;
   private EditBox searchField;
   private EditBox publishNameField;
   private EditBox publishAuthorField;
   private EditBox publishDescriptionField;
   private EditBox publishTagsField;
   private String searchText = "";
   private String status = "Loading Litemacro marketplace...";
   private int page;
   private boolean loading;
   private boolean refreshQueued;
   private boolean busy;
   private boolean publishPopupOpen;
   private boolean settingsPopupOpen;

   MacroMarketplaceScreen(MacroRunner runner, int returnPage) {
      super(Component.literal("Litemacro Marketplace"));
      this.runner = runner;
      this.returnPage = returnPage;
   }

   protected void init() {
      this.searchField = new EditBox(this.font, 118, 16, 172, 18, Component.literal("Search macros"));
      this.searchField.setMaxLength(64);
      this.searchField.setValue(this.searchText);
      this.searchField.setResponder(value -> {
         this.searchText = value == null ? "" : value;
         this.applyFilter();
      });
      this.addRenderableWidget(this.searchField);

      this.publishNameField = this.popupField("Macro name");
      this.publishAuthorField = this.popupField("Author");
      this.publishDescriptionField = this.popupField("Description");
      this.publishTagsField = this.popupField("Tags");
      this.addRenderableWidget(this.publishNameField);
      this.addRenderableWidget(this.publishAuthorField);
      this.addRenderableWidget(this.publishDescriptionField);
      this.addRenderableWidget(this.publishTagsField);
      this.updatePopupFieldVisibility();
      if (this.allPresets.isEmpty() && !this.loading) {
         this.refreshMarketplace();
      }
   }

   private EditBox popupField(String label) {
      EditBox field = new EditBox(this.font, 0, 0, 120, 18, Component.literal(label));
      field.setMaxLength(256);
      field.visible = false;
      field.active = false;
      return field;
   }

   public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
      int actualWidth = this.width;
      int actualHeight = this.height;
      double scale = this.layoutScale(actualWidth, actualHeight);
      mouseX = this.toLayoutMouse(mouseX, scale);
      mouseY = this.toLayoutMouse(mouseY, scale);
      context.pose().pushMatrix();
      context.pose().scale((float)scale, (float)scale);
      this.useLayoutSize(actualWidth, actualHeight);

      try {
         this.drawBackgroundGrid(context);
         this.layoutFields();
         this.updatePopupFieldVisibility();
         this.drawTopBar(context, mouseX, mouseY);
         this.drawGallery(context, mouseX, mouseY);
         this.drawFooter(context);
         if (this.selectedPreset != null) {
            this.drawPresetPopup(context, mouseX, mouseY);
         }
         if (this.settingsPopupOpen) {
            this.drawSettingsPopup(context, mouseX, mouseY);
         }
         if (this.publishPopupOpen) {
            this.drawPublishPopup(context, mouseX, mouseY);
         }
         super.render(context, mouseX, mouseY, delta);
      } finally {
         this.restoreActualSize(actualWidth, actualHeight);
         context.pose().popMatrix();
      }
   }

   public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
      this.drawBackgroundGrid(context);
   }

   public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
      int actualWidth = this.width;
      int actualHeight = this.height;
      double scale = this.layoutScale(actualWidth, actualHeight);
      click = this.layoutClick(click, scale);
      double mouseX = click.x();
      double mouseY = click.y();
      this.useLayoutSize(actualWidth, actualHeight);

      try {
         if (this.publishPopupOpen) {
            if (this.hit(this.publishUploadRect(), mouseX, mouseY)) {
               this.uploadCurrentMacro();
               return true;
            }
            if (this.hit(this.publishCancelRect(), mouseX, mouseY)) {
               this.publishPopupOpen = false;
               this.updatePopupFieldVisibility();
               return true;
            }
            return super.mouseClicked(click, doubled);
         }

         if (this.settingsPopupOpen) {
            if (this.hit(this.settingsCloseRect(), mouseX, mouseY)) {
               this.settingsPopupOpen = false;
               return true;
            }
            if (this.hit(this.settingsRefreshRect(), mouseX, mouseY)) {
               this.refreshMarketplace();
               return true;
            }
            this.settingsPopupOpen = false;
            return true;
         }

         if (this.selectedPreset != null) {
            if (this.hit(this.popupLikeRect(), mouseX, mouseY)) {
               this.likeSelectedPreset();
               return true;
            }
            if (this.hit(this.popupDownloadRect(), mouseX, mouseY)) {
               this.downloadSelectedPreset();
               return true;
            }
            if (this.hit(this.popupCloseRect(), mouseX, mouseY)) {
               this.selectedPreset = null;
               return true;
            }
            this.selectedPreset = null;
            return true;
         }

         if (this.hit(this.backRect(), mouseX, mouseY)) {
            this.minecraft.setScreen(new MacroListScreen(this.runner, this.returnPage));
            return true;
         }
         if (this.hit(this.refreshRect(), mouseX, mouseY)) {
            this.refreshMarketplace();
            return true;
         }
         if (this.hit(this.publishRect(), mouseX, mouseY)) {
            this.openPublishFlow();
            return true;
         }
         if (this.hit(this.sortRect(), mouseX, mouseY)) {
            this.cycleSortMode();
            return true;
         }
         if (this.hit(this.prevRect(), mouseX, mouseY)) {
            this.page = Math.max(0, this.page - 1);
            return true;
         }
         if (this.hit(this.nextRect(), mouseX, mouseY)) {
            this.page = Math.min(this.totalPages() - 1, this.page + 1);
            return true;
         }

         LitemacroMarketplacePreset card = this.cardAt(mouseX, mouseY);
         if (card != null) {
            this.selectedPreset = card;
            return true;
         }

         return super.mouseClicked(click, doubled);
      } finally {
         this.restoreActualSize(actualWidth, actualHeight);
      }
   }

   public boolean keyPressed(KeyEvent input) {
      if (input.key() == 256) {
         if (this.publishPopupOpen) {
            this.publishPopupOpen = false;
            this.updatePopupFieldVisibility();
            return true;
         }
         if (this.settingsPopupOpen) {
            this.settingsPopupOpen = false;
            return true;
         }
         if (this.selectedPreset != null) {
            this.selectedPreset = null;
            return true;
         }
      }

      return super.keyPressed(input);
   }

   public boolean isPauseScreen() {
      return false;
   }

   private void refreshMarketplace() {
      if (this.loading) {
         this.refreshQueued = true;
         return;
      }

      if (!LitemacroMarketplaceService.isConfigured()) {
         this.allPresets.clear();
         this.filteredPresets = List.of();
         this.page = 0;
         this.refreshQueued = false;
         this.status = LitemacroMarketplaceService.configurationHelp();
         return;
      }

      this.loading = true;
      this.refreshQueued = false;
      this.status = "Loading Litemacro marketplace...";
      LitemacroMarketplaceService.fetchPublishedPresets(this.sortMode).whenComplete((presets, error) -> this.finishMarketplaceRefresh(presets, error));
   }

   private void finishMarketplaceRefresh(List<LitemacroMarketplacePreset> presets, Throwable error) {
      this.runOnClient(() -> {
         this.loading = false;
         if (error != null) {
            this.allPresets.clear();
            this.filteredPresets = List.of();
            this.selectedPreset = null;
            this.page = 0;
            this.status = this.shortError(error);
         } else {
            this.allPresets.clear();
            if (presets != null) {
               this.allPresets.addAll(presets);
            }

            this.page = 0;
            this.applyFilter();
            this.status = this.allPresets.isEmpty() ? "No Litemacro uploads found yet." : "Loaded " + this.allPresets.size() + " Litemacro marketplace macros.";
         }

         if (this.refreshQueued) {
            this.refreshQueued = false;
            this.refreshMarketplace();
         }
      });
   }

   private void applyFilter() {
      String query = this.searchText == null ? "" : this.searchText.trim().toLowerCase(Locale.ROOT);
      List<LitemacroMarketplacePreset> values = new ArrayList<>();
      for (LitemacroMarketplacePreset preset : this.allPresets) {
         if (query.isBlank() || this.presetContains(preset, query)) {
            values.add(preset);
         }
      }

      values.sort(this.comparatorForSort());
      this.filteredPresets = values;
      if (this.page >= this.totalPages()) {
         this.page = Math.max(0, this.totalPages() - 1);
      }
   }

   private boolean presetContains(LitemacroMarketplacePreset preset, String query) {
      if (preset == null) {
         return false;
      }

      if (contains(preset.name(), query) || contains(preset.authorName(), query) || contains(preset.description(), query) || contains(preset.gameVersion(), query)) {
         return true;
      }

      for (String tag : preset.tags()) {
         if (contains(tag, query)) {
            return true;
         }
      }

      return false;
   }

   private Comparator<LitemacroMarketplacePreset> comparatorForSort() {
      return switch (this.sortMode) {
         case DOWNLOADS -> Comparator.comparingInt(LitemacroMarketplacePreset::downloadsCount).reversed();
         case LIKES -> Comparator.comparingInt(LitemacroMarketplacePreset::likesCount).reversed();
         case NEWEST -> Comparator.comparing(LitemacroMarketplacePreset::createdAt, Comparator.nullsLast(String::compareTo)).reversed();
         case UPDATED -> Comparator.comparing(LitemacroMarketplacePreset::updatedAt, Comparator.nullsLast(String::compareTo)).reversed();
         case TRENDING -> Comparator.comparingInt((LitemacroMarketplacePreset preset) -> preset.downloadsCount() + preset.likesCount() * 3).reversed();
      };
   }

   private void cycleSortMode() {
      this.sortMode = switch (this.sortMode) {
         case TRENDING -> LitemacroMarketplaceService.ListingMode.NEWEST;
         case NEWEST -> LitemacroMarketplaceService.ListingMode.DOWNLOADS;
         case DOWNLOADS -> LitemacroMarketplaceService.ListingMode.LIKES;
         case LIKES -> LitemacroMarketplaceService.ListingMode.UPDATED;
         case UPDATED -> LitemacroMarketplaceService.ListingMode.TRENDING;
      };
      this.refreshMarketplace();
   }

   private void openPublishFlow() {
      String macroName = this.runner.macroName(this.runner.currentMacroNumber());
      this.publishNameField.setValue(macroName);
      this.publishAuthorField.setValue("Litemacro user");
      this.publishDescriptionField.setValue("Litemacro macro for " + LitemacroMarketplaceService.minecraftVersionLabel());
      this.publishTagsField.setValue("litemacro, macro");
      this.publishPopupOpen = true;
      this.updatePopupFieldVisibility();
   }

   private void uploadCurrentMacro() {
      if (this.busy) {
         return;
      }

      String name = firstNonBlank(this.publishNameField.getValue(), this.runner.macroName(this.runner.currentMacroNumber()));
      String author = firstNonBlank(this.publishAuthorField.getValue(), "Litemacro user");
      String description = firstNonBlank(this.publishDescriptionField.getValue(), "Litemacro macro");
      try {
         this.runner.save(this.runner.model());
      } catch (IOException var8) {
         this.status = "Save before upload failed: " + var8.getMessage();
         return;
      }

      Path path = this.runner.macroFilePath(this.runner.currentMacroNumber());
      this.busy = true;
      this.status = "Submitting " + name + "...";
      LitemacroMarketplaceService.PublishRequest request = new LitemacroMarketplaceService.PublishRequest(
         path,
         null,
         null,
         name,
         name,
         author,
         description,
         this.parseTags(this.publishTagsField.getValue()),
         LitemacroMarketplaceService.minecraftVersionLabel(),
         LitemacroMarketplaceService.litemacroVersionLabel(),
         true
      );
      LitemacroMarketplaceService.submitPreset(request)
         .whenComplete((result, error) -> this.runOnClient(() -> {
            this.busy = false;
            if (error != null) {
               this.status = this.shortError(error);
               return;
            }

            this.publishPopupOpen = false;
            this.updatePopupFieldVisibility();
            this.status = result.message() + " " + result.remaining() + " upload(s) left.";
            this.refreshMarketplace();
         }));
   }

   private void downloadSelectedPreset() {
      if (this.busy || this.selectedPreset == null) {
         return;
      }

      LitemacroMarketplacePreset preset = this.selectedPreset;
      this.busy = true;
      this.status = "Downloading " + preset.name() + "...";
      LitemacroMarketplaceService.downloadPresetToTempFile(preset).whenComplete((path, error) -> this.runOnClient(() -> {
         this.busy = false;
         if (error != null) {
            this.status = this.shortError(error);
            return;
         }

         try {
            this.runner.importMacroFromDownloads(this.minecraft, this.runner.currentMacroNumber(), path);
            Files.deleteIfExists(path);
            this.status = "Imported " + preset.name() + " into Macro " + this.runner.currentMacroNumber() + ".";
            this.selectedPreset = null;
            LitemacroMarketplaceService.incrementDownload(preset.id());
         } catch (Exception var5) {
            this.status = "Import failed: " + var5.getMessage();
         }
      }));
   }

   private void likeSelectedPreset() {
      if (this.busy || this.selectedPreset == null) {
         return;
      }

      LitemacroMarketplacePreset preset = this.selectedPreset;
      this.busy = true;
      this.status = "Liking " + preset.name() + "...";
      LitemacroMarketplaceService.likePreset(preset.id()).whenComplete((updated, error) -> this.runOnClient(() -> {
         this.busy = false;
         if (error != null) {
            this.status = this.shortError(error);
            return;
         }

         if (updated != null) {
            this.replacePreset(updated);
            this.selectedPreset = updated;
            this.applyFilter();
            this.status = "Liked " + updated.name() + ".";
         }
      }));
   }

   private void replacePreset(LitemacroMarketplacePreset updated) {
      for (int index = 0; index < this.allPresets.size(); index++) {
         if (this.allPresets.get(index).id().equals(updated.id())) {
            this.allPresets.set(index, updated);
            return;
         }
      }

      this.allPresets.add(updated);
   }

   private void drawTopBar(GuiGraphics context, int mouseX, int mouseY) {
      context.fill(0, 0, this.width, TOP_BAR_HEIGHT, 0xF3121A23);
      context.fill(0, TOP_BAR_HEIGHT - 1, this.width, TOP_BAR_HEIGHT, BORDER);
      this.drawButton(context, this.backRect(), "Back", mouseX, mouseY, true);
      this.drawButton(context, this.publishRect(), "Submit", mouseX, mouseY, !this.busy);
      this.drawButton(context, this.sortRect(), this.sortShortLabel(), mouseX, mouseY, true);
      this.drawButton(context, this.refreshRect(), this.loading ? "..." : "\u21bb", mouseX, mouseY, !this.loading);
      context.drawCenteredString(this.font, "Litemacro Marketplace", this.width / 2, 8, TEXT);
      if (this.width >= 760) {
         context.drawCenteredString(this.font, "Litemacro online macro gallery", this.width / 2, 22, MUTED);
      }
   }

   private void drawGallery(GuiGraphics context, int mouseX, int mouseY) {
      int left = 18;
      int top = TOP_BAR_HEIGHT + 14;
      int width = this.width - 36;
      int bottom = this.height - 34;
      this.drawPanel(context, left, top, left + width, bottom, PANEL);
      context.drawString(this.font, "Community macros", left + 14, top + 12, TEXT);
      context.drawString(this.font, this.filteredPresets.size() + " shown / " + this.allPresets.size() + " loaded", left + 14, top + 26, MUTED);
      int galleryTop = top + 48;
      int columns = Math.max(1, Math.min(6, (width - 28 + CARD_GAP) / (CARD_WIDTH + CARD_GAP)));
      int start = this.page * PAGE_SIZE;
      int end = Math.min(this.filteredPresets.size(), start + PAGE_SIZE);
      if (this.loading) {
         context.drawCenteredString(this.font, "Loading marketplace...", this.width / 2, galleryTop + 48, ACCENT);
         return;
      }
      if (this.filteredPresets.isEmpty()) {
         context.drawCenteredString(this.font, "No Litemacro macros match this view.", this.width / 2, galleryTop + 48, MUTED);
         context.drawCenteredString(this.font, "Submit one from the button above.", this.width / 2, galleryTop + 64, DIM);
         return;
      }

      for (int index = start; index < end; index++) {
         int local = index - start;
         int column = local % columns;
         int row = local / columns;
         int x = left + 14 + column * (CARD_WIDTH + CARD_GAP);
         int y = galleryTop + row * (CARD_HEIGHT + CARD_GAP);
         this.drawPresetCard(context, this.filteredPresets.get(index), x, y, CARD_WIDTH, CARD_HEIGHT, mouseX, mouseY);
      }
   }

   private void drawPresetCard(GuiGraphics context, LitemacroMarketplacePreset preset, int x, int y, int width, int height, int mouseX, int mouseY) {
      boolean hover = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
      this.drawPanel(context, x, y, x + width, y + height, hover ? 0xF3253545 : PANEL_2);
      int accent = preset.published() ? ACCENT : ORANGE;
      context.fill(x + 1, y + 1, x + width - 1, y + 4, accent);
      this.drawMiniGraph(context, x + 10, y + 14, width - 20, 38, accent);
      context.drawString(this.font, this.fit(preset.name(), width - 18), x + 9, y + 60, TEXT);
      context.drawString(this.font, this.fit(firstNonBlank(preset.authorName(), "Unknown author"), width - 18), x + 9, y + 74, MUTED);
      context.drawString(this.font, "DL " + preset.downloadsCount() + "   Like " + preset.likesCount(), x + 9, y + 90, DIM);
      context.drawString(this.font, this.fit(tagLine(preset.tags()), width - 18), x + 9, y + 106, accent);
   }

   private void drawMiniGraph(GuiGraphics context, int x, int y, int width, int height, int accent) {
      int nodeW = Math.max(28, width / 4);
      context.fill(x, y + 6, x + nodeW, y + 20, PANEL_3);
      context.fill(x + width / 2 - nodeW / 2, y + height - 20, x + width / 2 + nodeW / 2, y + height - 6, PANEL_3);
      context.fill(x + width - nodeW, y + 5, x + width, y + 19, PANEL_3);
      context.fill(x + nodeW, y + 13, x + width / 2 - nodeW / 2, y + 15, accent);
      context.fill(x + width / 2 + nodeW / 2, y + height - 14, x + width - nodeW, y + 13, accent);
      context.fill(x, y + 6, x + 2, y + 20, accent);
      context.fill(x + width / 2 - nodeW / 2, y + height - 20, x + width / 2 - nodeW / 2 + 2, y + height - 6, GREEN);
      context.fill(x + width - nodeW, y + 5, x + width - nodeW + 2, y + 19, ORANGE);
   }

   private void drawPresetPopup(GuiGraphics context, int mouseX, int mouseY) {
      int w = this.presetPopupWidth();
      int h = this.presetPopupHeight();
      int x = (this.width - w) / 2;
      int y = (this.height - h) / 2;
      this.drawOverlay(context);
      this.drawPanel(context, x, y, x + w, y + h, 0xFA0D141C);
      context.fill(x + 1, y + 1, x + w - 1, y + 4, ACCENT);
      context.drawString(this.font, this.fit(this.selectedPreset.name(), w - 54), x + 18, y + 20, TEXT);
      context.drawString(this.font, "x", x + w - 24, y + 20, TEXT);
      context.drawString(this.font, "By " + firstNonBlank(this.selectedPreset.authorName(), "Unknown author"), x + 18, y + 38, MUTED);
      context.drawString(this.font, "Downloads " + this.selectedPreset.downloadsCount() + "   Likes " + this.selectedPreset.likesCount(), x + 18, y + 54, DIM);
      this.drawWrapped(context, firstNonBlank(this.selectedPreset.description(), "No description."), x + 18, y + 78, w - 36, MUTED, 6);
      this.drawWrapped(context, "Tags: " + tagLine(this.selectedPreset.tags()), x + 18, y + 160, w - 36, ACCENT, 2);
      this.drawWrapped(
         context,
         firstNonBlank(this.selectedPreset.gameVersion(), "Minecraft") + "  |  " + firstNonBlank(this.selectedPreset.litemacroVersion(), "Litemacro"),
         x + 18,
         y + 188,
         w - 36,
         DIM,
         1
      );
      this.drawButton(context, this.popupDownloadRect(), this.busy ? "Working..." : "Download to Current Macro", mouseX, mouseY, !this.busy);
      this.drawButton(context, this.popupLikeRect(), this.busy ? "..." : "Like +1", mouseX, mouseY, !this.busy);
      this.drawButton(context, this.popupCloseRect(), "Close", mouseX, mouseY, true);
   }

   private void drawPublishPopup(GuiGraphics context, int mouseX, int mouseY) {
      int w = Math.min(520, this.width - 60);
      int h = 274;
      int x = (this.width - w) / 2;
      int y = (this.height - h) / 2;
      this.drawOverlay(context);
      this.drawPanel(context, x, y, x + w, y + h, 0xFA0D141C);
      context.fill(x + 1, y + 1, x + w - 1, y + 4, GREEN);
      context.drawString(this.font, "Submit Litemacro Macro", x + 18, y + 18, TEXT);
      context.drawString(this.font, "Uploads the selected macro. Limit 5 per user.", x + 18, y + 34, MUTED);
      this.drawFieldLabel(context, "Name", this.publishNameField);
      this.drawFieldLabel(context, "Author", this.publishAuthorField);
      this.drawFieldLabel(context, "Description", this.publishDescriptionField);
      this.drawFieldLabel(context, "Tags", this.publishTagsField);
      this.drawButton(context, this.publishUploadRect(), this.busy ? "Submitting..." : "Submit", mouseX, mouseY, !this.busy);
      this.drawButton(context, this.publishCancelRect(), "Cancel", mouseX, mouseY, true);
   }

   private void drawSettingsPopup(GuiGraphics context, int mouseX, int mouseY) {
      int w = this.settingsPopupWidth();
      int h = this.settingsPopupHeight();
      int x = (this.width - w) / 2;
      int y = (this.height - h) / 2;
      this.drawOverlay(context);
      this.drawPanel(context, x, y, x + w, y + h, 0xFA0D141C);
      context.fill(x + 1, y + 1, x + w - 1, y + 4, ORANGE);
      context.drawString(this.font, "Marketplace Settings", x + 18, y + 18, TEXT);
      context.drawString(this.font, "Uploads", x + 18, y + 42, MUTED);
      this.drawWrapped(context, "No sign-in. Uploads use a local uploader ID and are limited by the marketplace backend.", x + 18, y + 56, w - 36, TEXT, 3);
      context.drawString(this.font, this.fit("Uploader: " + LitemacroMarketplaceService.uploaderInstallId(), w - 36), x + 18, y + 98, MUTED);
      context.drawString(this.font, this.fit("Limit: " + LitemacroMarketplaceService.ANONYMOUS_UPLOAD_LIMIT + " submissions", w - 36), x + 18, y + 114, MUTED);
      context.drawString(this.font, this.fit(LitemacroMarketplaceService.litemacroVersionLabel() + " / " + LitemacroMarketplaceService.minecraftVersionLabel(), w - 36), x + 18, y + 132, DIM);
      this.drawButton(context, this.settingsRefreshRect(), "Refresh", mouseX, mouseY, true);
      this.drawButton(context, this.settingsCloseRect(), "Close", mouseX, mouseY, true);
   }

   private void drawFooter(GuiGraphics context) {
      int y = this.height - 24;
      context.fill(0, y - 5, this.width, this.height, 0xC6101820);
      int statusWidth = Math.max(80, Math.min(this.width / 2 - 44, this.width - 230));
      context.drawString(this.font, this.fit(this.cleanStatusMessage(this.status), statusWidth), 18, y, this.status.toLowerCase(Locale.ROOT).contains("failed") ? RED : GREEN);
      context.drawCenteredString(this.font, "Page " + (this.page + 1) + " / " + this.totalPages(), this.width / 2, y, MUTED);
      this.drawButton(context, this.prevRect(), "Prev", -1, -1, this.page > 0);
      this.drawButton(context, this.nextRect(), "Next", -1, -1, this.page + 1 < this.totalPages());
   }

   private void layoutFields() {
      this.searchField.setX(84);
      this.searchField.setY(TOP_BAR_CONTROL_Y + 1);
      this.searchField.setWidth(Math.min(190, Math.max(90, this.width / 6)));
      int w = Math.min(520, this.width - 60);
      int x = (this.width - w) / 2;
      int y = (this.height - 274) / 2;
      int fieldLeft = x + 134;
      int fieldWidth = w - 154;
      this.publishNameField.setX(fieldLeft);
      this.publishNameField.setY(y + 64);
      this.publishNameField.setWidth(fieldWidth);
      this.publishAuthorField.setX(fieldLeft);
      this.publishAuthorField.setY(y + 94);
      this.publishAuthorField.setWidth(fieldWidth);
      this.publishDescriptionField.setX(fieldLeft);
      this.publishDescriptionField.setY(y + 124);
      this.publishDescriptionField.setWidth(fieldWidth);
      this.publishTagsField.setX(fieldLeft);
      this.publishTagsField.setY(y + 154);
      this.publishTagsField.setWidth(fieldWidth);
   }

   private void updatePopupFieldVisibility() {
      if (this.publishNameField == null) {
         return;
      }

      boolean visible = this.publishPopupOpen;
      this.publishNameField.visible = visible;
      this.publishAuthorField.visible = visible;
      this.publishDescriptionField.visible = visible;
      this.publishTagsField.visible = visible;
      this.publishNameField.active = visible;
      this.publishAuthorField.active = visible;
      this.publishDescriptionField.active = visible;
      this.publishTagsField.active = visible;
   }

   private void drawFieldLabel(GuiGraphics context, String label, EditBox field) {
      context.drawString(this.font, label, field.getX() - 112, field.getY() + 5, MUTED);
   }

   private void drawBackgroundGrid(GuiGraphics context) {
      context.fill(0, 0, this.width, this.height, BG);
      for (int x = 0; x < this.width; x += 28) {
         context.fill(x, 0, x + 1, this.height, GRID);
      }
      for (int y = 0; y < this.height; y += 28) {
         context.fill(0, y, this.width, y + 1, GRID);
      }
      for (int x = 0; x < this.width; x += 112) {
         context.fill(x, 0, x + 1, this.height, 0x55384B5F);
      }
      for (int y = 0; y < this.height; y += 112) {
         context.fill(0, y, this.width, y + 1, 0x55384B5F);
      }
   }

   private void drawOverlay(GuiGraphics context) {
      context.fill(0, 0, this.width, this.height, 0xAA000000);
   }

   private void drawPanel(GuiGraphics context, int x1, int y1, int x2, int y2, int color) {
      context.fill(x1, y1, x2, y2, color);
      context.fill(x1, y1, x2, y1 + 1, BORDER);
      context.fill(x1, y2 - 1, x2, y2, 0xFF1B2A37);
      context.fill(x1, y1, x1 + 1, y2, BORDER);
      context.fill(x2 - 1, y1, x2, y2, 0xFF1B2A37);
   }

   private void drawButton(GuiGraphics context, Rect rect, String label, int mouseX, int mouseY, boolean active) {
      boolean hover = active && mouseX >= rect.x && mouseX <= rect.x + rect.w && mouseY >= rect.y && mouseY <= rect.y + rect.h;
      int bg = !active ? 0xFF27313D : hover ? 0xFF2E4154 : 0xFF1D2A36;
      context.fill(rect.x, rect.y, rect.x + rect.w, rect.y + rect.h, bg);
      context.fill(rect.x, rect.y, rect.x + rect.w, rect.y + 1, active ? BORDER_HI : 0xFF303A44);
      context.fill(rect.x, rect.y + rect.h - 1, rect.x + rect.w, rect.y + rect.h, 0xFF111820);
      context.fill(rect.x, rect.y, rect.x + 1, rect.y + rect.h, active ? BORDER_HI : 0xFF303A44);
      context.fill(rect.x + rect.w - 1, rect.y, rect.x + rect.w, rect.y + rect.h, 0xFF111820);
      int color = active ? TEXT : DIM;
      context.drawCenteredString(this.font, this.fit(label, rect.w - 8), rect.x + rect.w / 2, rect.y + (rect.h - 8) / 2, color);
   }

   private void drawWrapped(GuiGraphics context, String text, int x, int y, int maxWidth, int color, int maxLines) {
      String[] words = text.split("\\s+");
      String line = "";
      int drawn = 0;
      for (String word : words) {
         String next = line.isBlank() ? word : line + " " + word;
         if (this.font.width(next) > maxWidth && !line.isBlank()) {
            context.drawString(this.font, this.fit(line, maxWidth), x, y + drawn * 12, color);
            drawn++;
            line = word;
            if (drawn >= maxLines) {
               return;
            }
            if (this.font.width(line) > maxWidth) {
               context.drawString(this.font, this.fit(line, maxWidth), x, y + drawn * 12, color);
               drawn++;
               line = "";
               if (drawn >= maxLines) {
                  return;
               }
            }
         } else {
            line = next;
         }
      }

      if (!line.isBlank() && drawn < maxLines) {
         context.drawString(this.font, this.fit(line, maxWidth), x, y + drawn * 12, color);
      }
   }

   private LitemacroMarketplacePreset cardAt(double mouseX, double mouseY) {
      int left = 18;
      int top = 112;
      int width = this.width - 36;
      int columns = Math.max(1, Math.min(6, (width - 28 + CARD_GAP) / (CARD_WIDTH + CARD_GAP)));
      int start = this.page * PAGE_SIZE;
      int end = Math.min(this.filteredPresets.size(), start + PAGE_SIZE);
      for (int index = start; index < end; index++) {
         int local = index - start;
         int x = left + 14 + local % columns * (CARD_WIDTH + CARD_GAP);
         int y = top + local / columns * (CARD_HEIGHT + CARD_GAP);
         if (mouseX >= x && mouseX <= x + CARD_WIDTH && mouseY >= y && mouseY <= y + CARD_HEIGHT) {
            return this.filteredPresets.get(index);
         }
      }

      return null;
   }

   private int totalPages() {
      return Math.max(1, (int)Math.ceil(this.filteredPresets.size() / (double)PAGE_SIZE));
   }

   private List<String> parseTags(String value) {
      List<String> tags = new ArrayList<>();
      tags.add("litemacro");
      if (value != null) {
         for (String tag : value.split(",")) {
            String trimmed = tag.trim();
            if (!trimmed.isBlank() && tags.stream().noneMatch(existing -> existing.equalsIgnoreCase(trimmed))) {
               tags.add(trimmed);
            }
         }
      }

      return tags;
   }

   private String sortLabel() {
      return switch (this.sortMode) {
         case TRENDING -> "Sort: Trending";
         case NEWEST -> "Sort: New";
         case UPDATED -> "Sort: Updated";
         case DOWNLOADS -> "Sort: Downloads";
         case LIKES -> "Sort: Likes";
      };
   }

   private String sortShortLabel() {
      return switch (this.sortMode) {
         case TRENDING -> "Trend";
         case NEWEST -> "New";
         case UPDATED -> "Update";
         case DOWNLOADS -> "DL";
         case LIKES -> "Likes";
      };
   }

   private static String tagLine(List<String> tags) {
      if (tags == null || tags.isEmpty()) {
         return "litemacro";
      }

      StringBuilder builder = new StringBuilder();
      for (String tag : tags) {
         if (tag == null || tag.isBlank()) {
            continue;
         }
         if (!builder.isEmpty()) {
            builder.append(", ");
         }
         builder.append(tag.trim());
         if (builder.length() > 36) {
            break;
         }
      }

      return builder.isEmpty() ? "litemacro" : builder.toString();
   }

   private static boolean contains(String value, String query) {
      return value != null && value.toLowerCase(Locale.ROOT).contains(query);
   }

   private String fit(String text, int maxWidth) {
      if (text == null) {
         return "";
      }
      if (maxWidth <= 0) {
         return "";
      }
      if (this.font == null || this.font.width(text) <= maxWidth) {
         return text;
      }

      String suffix = "...";
      if (this.font.width(suffix) > maxWidth) {
         return "";
      }
      String trimmed = text;
      while (!trimmed.isEmpty() && this.font.width(trimmed) + this.font.width(suffix) > maxWidth) {
         trimmed = trimmed.substring(0, trimmed.length() - 1);
      }
      return trimmed + suffix;
   }

   private String shortError(Throwable error) {
      Throwable cause = error;
      while (cause instanceof CompletionException && cause.getCause() != null) {
         cause = cause.getCause();
      }
      while (cause.getCause() != null && cause.getMessage() != null && cause.getMessage().contains(cause.getCause().getClass().getSimpleName())) {
         cause = cause.getCause();
      }

      return this.cleanStatusMessage(cause.getMessage() == null ? cause.toString() : cause.getMessage());
   }

   private String cleanStatusMessage(String message) {
      if (message == null) {
         return "";
      }
      String cleaned = message.trim();
      for (int pass = 0; pass < 2 && cleaned.contains("%"); pass++) {
         try {
            String decoded = URLDecoder.decode(cleaned, StandardCharsets.UTF_8);
            if (decoded.equals(cleaned)) {
               break;
            }
            cleaned = decoded;
         } catch (IllegalArgumentException ignored) {
            break;
         }
      }
      return cleaned;
   }

   private void runOnClient(Runnable runnable) {
      if (this.minecraft == null) {
         runnable.run();
      } else {
         this.minecraft.execute(runnable);
      }
   }

   private boolean hit(Rect rect, double x, double y) {
      return rect != null && x >= rect.x && x <= rect.x + rect.w && y >= rect.y && y <= rect.y + rect.h;
   }

   private Rect backRect() {
      return new Rect(12, TOP_BAR_CONTROL_Y, 46, 18);
   }

   private Rect publishRect() {
      return new Rect(286, TOP_BAR_CONTROL_Y, 68, 18);
   }

   private Rect myUploadsRect() {
      return new Rect(330, TOP_BAR_CONTROL_Y, 62, 18);
   }

   private Rect sortRect() {
      return new Rect(360, TOP_BAR_CONTROL_Y, 54, 18);
   }

   private Rect refreshRect() {
      return new Rect(420, TOP_BAR_CONTROL_Y, 50, 18);
   }

   private Rect settingsRect() {
      return new Rect(this.width - 150, TOP_BAR_CONTROL_Y, 48, 18);
   }

   private Rect accountRect() {
      return new Rect(this.width - 96, TOP_BAR_CONTROL_Y, 84, 18);
   }

   private Rect prevRect() {
      return new Rect(this.width - 138, this.height - 29, 58, 18);
   }

   private Rect nextRect() {
      return new Rect(this.width - 74, this.height - 29, 58, 18);
   }

   private Rect popupDownloadRect() {
      int w = this.presetPopupWidth();
      int h = this.presetPopupHeight();
      int x = (this.width - w) / 2;
      int y = (this.height - h) / 2;
      return new Rect(x + 18, y + h - 40, 188, 22);
   }

   private Rect popupLikeRect() {
      int w = this.presetPopupWidth();
      int h = this.presetPopupHeight();
      int x = (this.width - w) / 2;
      int y = (this.height - h) / 2;
      return new Rect(x + 214, y + h - 40, 76, 22);
   }

   private Rect popupCloseRect() {
      int w = this.presetPopupWidth();
      int h = this.presetPopupHeight();
      int x = (this.width - w) / 2;
      int y = (this.height - h) / 2;
      return new Rect(x + w - 96, y + h - 40, 78, 22);
   }

   private int presetPopupWidth() {
      return Math.min(520, Math.max(360, this.width - 60));
   }

   private int presetPopupHeight() {
      return Math.min(270, Math.max(244, this.height - 80));
   }

   private Rect publishVisibilityRect() {
      int w = Math.min(520, this.width - 60);
      int x = (this.width - w) / 2;
      int y = (this.height - 274) / 2;
      return new Rect(x + 18, y + 190, 150, 22);
   }

   private Rect publishUploadRect() {
      int w = Math.min(520, this.width - 60);
      int x = (this.width - w) / 2;
      int y = (this.height - 274) / 2;
      return new Rect(x + w - 188, y + 234, 82, 22);
   }

   private Rect publishCancelRect() {
      int w = Math.min(520, this.width - 60);
      int x = (this.width - w) / 2;
      int y = (this.height - 274) / 2;
      return new Rect(x + w - 96, y + 234, 78, 22);
   }

   private Rect settingsRefreshRect() {
      int x = (this.width - this.settingsPopupWidth()) / 2;
      int y = (this.height - this.settingsPopupHeight()) / 2;
      return new Rect(x + 18, y + this.settingsPopupHeight() - 48, 86, 22);
   }

   private Rect settingsSignOutRect() {
      int x = (this.width - this.settingsPopupWidth()) / 2;
      int y = (this.height - this.settingsPopupHeight()) / 2;
      return new Rect(x + 114, y + this.settingsPopupHeight() - 48, 94, 22);
   }

   private Rect settingsCloseRect() {
      int w = this.settingsPopupWidth();
      int x = (this.width - w) / 2;
      int y = (this.height - this.settingsPopupHeight()) / 2;
      return new Rect(x + w - 104, y + this.settingsPopupHeight() - 48, 86, 22);
   }

   private int settingsPopupWidth() {
      return Math.min(392, Math.max(280, this.width - 48));
   }

   private int settingsPopupHeight() {
      return 198;
   }

   private MouseButtonEvent layoutClick(MouseButtonEvent click, double scale) {
      return scale >= 1.0 ? click : new MouseButtonEvent(click.x() / scale, click.y() / scale, click.buttonInfo());
   }

   private int toLayoutMouse(int coordinate, double scale) {
      return scale >= 1.0 ? coordinate : (int)Math.round(coordinate / scale);
   }

   private void useLayoutSize(int actualWidth, int actualHeight) {
      double scale = this.layoutScale(actualWidth, actualHeight);
      this.width = Math.max(MIN_LAYOUT_WIDTH, (int)Math.ceil(actualWidth / scale));
      this.height = Math.max(MIN_LAYOUT_HEIGHT, (int)Math.ceil(actualHeight / scale));
   }

   private void restoreActualSize(int actualWidth, int actualHeight) {
      this.width = actualWidth;
      this.height = actualHeight;
   }

   private double layoutScale(int actualWidth, int actualHeight) {
      if (actualWidth <= 0 || actualHeight <= 0) {
         return 1.0;
      }

      return Math.min(1.0, Math.min(actualWidth / (double)MIN_LAYOUT_WIDTH, actualHeight / (double)MIN_LAYOUT_HEIGHT));
   }

   private static String firstNonBlank(String... values) {
      if (values != null) {
         for (String value : values) {
            if (value != null && !value.isBlank()) {
               return value;
            }
         }
      }
      return "";
   }

   private record Rect(int x, int y, int w, int h) {
   }
}
