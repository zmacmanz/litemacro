package com.alfre.macrobuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

final class MacroImportScreen extends Screen {
   private static final int PAGE_SIZE = 8;
   private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("M/d HH:mm");
   private final MacroRunner runner;
   private final int macroNumber;
   private final int returnPage;
   private final int page;
   private List<Path> files = List.of();
   private String status = "";

   MacroImportScreen(MacroRunner runner, int macroNumber, int returnPage) {
      this(runner, macroNumber, returnPage, 0);
   }

   private MacroImportScreen(MacroRunner runner, int macroNumber, int returnPage, int page) {
      super(Component.literal("Import Macro"));
      this.runner = runner;
      this.macroNumber = macroNumber;
      this.returnPage = returnPage;
      this.page = Math.max(0, page);
   }

   protected void init() {
      this.loadFiles();
      int centerX = this.width / 2;
      int listWidth = Math.min(660, this.width - 32);
      int left = centerX - listWidth / 2;
      int y = 86;
      int currentPage = this.currentPage();
      int start = currentPage * 8;
      int end = Math.min(this.files.size(), start + 8);

      for (int index = start; index < end; index++) {
         Path file = this.files.get(index);
         int rowY = y + (index - start) * 28;
         String label = this.fit(file.getFileName().toString(), listWidth - 236);
         this.addRenderableWidget(Button.builder(Component.literal(label), button -> {
            this.runner.importMacroFromDownloads(this.minecraft, this.macroNumber, file);
            this.minecraft.setScreen(new MacroListScreen(this.runner, this.returnPage));
         }).bounds(left, rowY, listWidth - 220, 22).build());
         this.addRenderableWidget(Button.builder(Component.literal("Import"), button -> {
            this.runner.importMacroFromDownloads(this.minecraft, this.macroNumber, file);
            this.minecraft.setScreen(new MacroListScreen(this.runner, this.returnPage));
         }).bounds(left + listWidth - 104, rowY, 104, 22).build());
      }

      int footerY = Math.min(this.height - 62, y + 224 + 12);
      this.addRenderableWidget(
         Button.builder(Component.literal("Back"), button -> this.minecraft.setScreen(new MacroListScreen(this.runner, this.returnPage)))
            .bounds(left, footerY, 74, 20)
            .build()
      );
      ((Button)this.addRenderableWidget(
            Button.builder(
                  Component.literal("Prev"),
                  button -> this.minecraft.setScreen(new MacroImportScreen(this.runner, this.macroNumber, this.returnPage, currentPage - 1))
               )
               .bounds(left + 82, footerY, 58, 20)
               .build()
         ))
         .active = currentPage > 0;
      ((Button)this.addRenderableWidget(
            Button.builder(
                  Component.literal("Next"),
                  button -> this.minecraft.setScreen(new MacroImportScreen(this.runner, this.macroNumber, this.returnPage, currentPage + 1))
               )
               .bounds(left + 148, footerY, 58, 20)
               .build()
         ))
         .active = end < this.files.size();
      this.addRenderableWidget(
         Button.builder(
               Component.literal("Refresh"),
               button -> this.minecraft.setScreen(new MacroImportScreen(this.runner, this.macroNumber, this.returnPage, currentPage))
            )
            .bounds(left + listWidth - 82, footerY, 82, 20)
            .build()
      );
   }

   public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
      super.render(context, mouseX, mouseY, delta);
      int centerX = this.width / 2;
      int listWidth = Math.min(660, this.width - 32);
      int left = centerX - listWidth / 2;
      int currentPage = this.currentPage();
      int start = currentPage * 8;
      int end = Math.min(this.files.size(), start + 8);
      context.drawCenteredString(this.font, this.title, centerX, 24, -1);
      context.drawCenteredString(this.font, Component.literal("Choose a .json file from Downloads for Macro " + this.macroNumber), centerX, 42, -2561793);
      context.drawCenteredString(this.font, Component.literal(this.fit(this.runner.downloadsDirectory().toString(), this.width - 32)), centerX, 58, -6645094);
      context.drawCenteredString(this.font, Component.literal(this.status), centerX, 72, -6488);

      for (int index = start; index < end; index++) {
         Path file = this.files.get(index);
         int rowY = 92 + (index - start) * 28;
         context.drawString(this.font, this.modifiedLabel(file), left + listWidth - 208, rowY + 7, -4671304);
      }
   }

   public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
      context.fill(0, 0, this.width, this.height, -804253680);
   }

   public boolean isPauseScreen() {
      return false;
   }

   private void loadFiles() {
      try {
         this.files = this.runner.downloadsJsonFiles();
         this.status = this.files.isEmpty() ? "No .json files found in Downloads." : "Page " + (this.currentPage() + 1) + " / " + this.totalPages();
      } catch (IOException var2) {
         this.files = List.of();
         this.status = var2.getMessage();
      }
   }

   private int currentPage() {
      return Math.max(0, Math.min(this.page, this.totalPages() - 1));
   }

   private int totalPages() {
      return Math.max(1, (int)Math.ceil(this.files.size() / 8.0));
   }

   private String modifiedLabel(Path path) {
      try {
         FileTime modified = Files.getLastModifiedTime(path);
         return DATE_FORMAT.format(modified.toInstant().atZone(ZoneId.systemDefault()));
      } catch (IOException var3) {
         return "";
      }
   }

   private String fit(String text, int maxWidth) {
      if (this.font != null && text != null && this.font.width(text) > maxWidth) {
         String suffix = "...";
         int suffixWidth = this.font.width(suffix);
         String trimmed = text;

         while (!trimmed.isEmpty() && this.font.width(trimmed) + suffixWidth > maxWidth) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
         }

         return trimmed + suffix;
      } else {
         return text == null ? "" : text;
      }
   }
}
