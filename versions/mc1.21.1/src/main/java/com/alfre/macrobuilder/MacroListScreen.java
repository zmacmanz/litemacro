package com.alfre.macrobuilder;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

final class MacroListScreen extends Screen {
   private static final int PAGE_SIZE = 10;
   private static final int MIN_LAYOUT_WIDTH = 960;
   private static final int MIN_LAYOUT_HEIGHT = 540;
   private final MacroRunner runner;
   private final int page;
   private int bindingMacroNumber;

   MacroListScreen(MacroRunner runner) {
      this(runner, (runner.currentMacroNumber() - 1) / 10);
   }

   MacroListScreen(MacroRunner runner, int page) {
      super(Component.literal("Litemacro Macros"));
      this.runner = runner;
      int maxPage = Math.max(0, (int)Math.ceil(runner.macroCount() / 10.0) - 1);
      this.page = Math.max(0, Math.min(maxPage, page));
   }

   protected void init() {
      int actualWidth = this.width;
      int actualHeight = this.height;
      this.useLayoutSize(actualWidth, actualHeight);

      try {
         int centerX = this.width / 2;
         int listWidth = Math.min(620, this.width - 32);
         int left = centerX - listWidth / 2;
         int y = 78;
         int startMacro = this.page * 10 + 1;
         int endMacro = Math.min(this.runner.macroCount(), startMacro + 10 - 1);

         for (int macroNumber = startMacro; macroNumber <= endMacro; macroNumber++) {
            int rowY = y + (macroNumber - startMacro) * 24;
            String prefix = macroNumber == this.runner.currentMacroNumber() ? "> " : "";
            String label = prefix + macroNumber + ". " + this.runner.macroName(macroNumber);
            int selectedMacro = macroNumber;
            this.addRenderableWidget(Button.builder(Component.literal(this.fit(label, listWidth - 266)), button -> {
               this.runner.selectMacro(this.minecraft, selectedMacro);
               this.minecraft.setScreen(new MacroListScreen(this.runner, this.page));
            }).bounds(left, rowY, listWidth - 256, 20).build());
            this.addRenderableWidget(Button.builder(Component.literal("Edit"), button -> {
               this.runner.selectMacro(this.minecraft, selectedMacro);
               this.minecraft.setScreen(new MacroBuilderScreen(this.runner, this.runner.model()));
            }).bounds(left + listWidth - 248, rowY, 44, 20).build());
            this.addRenderableWidget(Button.builder(Component.literal("Run"), button -> {
               this.runner.toggleMacro(this.minecraft, selectedMacro);
               this.minecraft.setScreen(new MacroListScreen(this.runner, this.page));
            }).bounds(left + listWidth - 198, rowY, 44, 20).build());
            this.addRenderableWidget(
               Button.builder(Component.literal("Key: " + this.runner.macroKeyName(selectedMacro)), button -> this.bindingMacroNumber = selectedMacro)
                  .bounds(left + listWidth - 148, rowY, 100, 20)
                  .build()
            );
            this.addRenderableWidget(Button.builder(Component.literal("Clear"), button -> {
               this.runner.clearMacroKey(selectedMacro);
               this.minecraft.setScreen(new MacroListScreen(this.runner, this.page));
            }).bounds(left + listWidth - 42, rowY, 42, 20).build());
         }

         int footerY = Math.min(this.height - 112, y + 240 + 12);
         ((Button)this.addRenderableWidget(
               Button.builder(Component.literal("Prev"), button -> this.minecraft.setScreen(new MacroListScreen(this.runner, this.page - 1)))
                  .bounds(left, footerY, 58, 20)
                  .build()
            ))
            .active = this.page > 0;
         ((Button)this.addRenderableWidget(
               Button.builder(Component.literal("Next"), button -> this.minecraft.setScreen(new MacroListScreen(this.runner, this.page + 1)))
                  .bounds(left + 66, footerY, 58, 20)
                  .build()
            ))
            .active = endMacro < this.runner.macroCount();
         this.addRenderableWidget(
            Button.builder(Component.literal("Edit Selected"), button -> this.minecraft.setScreen(new MacroBuilderScreen(this.runner, this.runner.model())))
               .bounds(centerX - 74, footerY, 148, 20)
               .build()
         );
         this.addRenderableWidget(
            Button.builder(Component.literal("Stop"), button -> this.runner.stop(this.minecraft, "Stopped."))
               .bounds(left + listWidth - 128, footerY, 58, 20)
               .build()
         );
         this.addRenderableWidget(Button.builder(Component.literal(this.runner.chatNotifications() ? "Chat: On" : "Chat: Off"), button -> {
            this.runner.toggleChatNotifications();
            this.minecraft.setScreen(new MacroListScreen(this.runner, this.page));
         }).bounds(left + listWidth - 234, footerY, 98, 20).build());
         this.addRenderableWidget(Button.builder(Component.literal("Close"), button -> this.onClose()).bounds(left + listWidth - 64, footerY, 64, 20).build());
         int toolY = footerY + 24;
         int toolLeft = centerX - 226;
         this.addRenderableWidget(Button.builder(Component.literal("Copy"), button -> {
            this.runner.copyMacro(this.minecraft, this.runner.currentMacroNumber());
            this.minecraft.setScreen(new MacroListScreen(this.runner, this.page));
         }).bounds(toolLeft, toolY, 58, 20).build());
         this.addRenderableWidget(Button.builder(Component.literal("Paste"), button -> {
            this.runner.pasteCopiedMacro(this.minecraft, this.runner.currentMacroNumber());
            this.minecraft.setScreen(new MacroListScreen(this.runner, this.page));
         }).bounds(toolLeft + 66, toolY, 58, 20).build());
         this.addRenderableWidget(Button.builder(Component.literal("Download"), button -> {
            this.runner.exportMacroToDownloads(this.minecraft, this.runner.currentMacroNumber());
            this.minecraft.setScreen(new MacroListScreen(this.runner, this.page));
         }).bounds(toolLeft + 132, toolY, 82, 20).build());
         this.addRenderableWidget(
            Button.builder(
                  Component.literal("Import"), button -> this.minecraft.setScreen(new MacroImportScreen(this.runner, this.runner.currentMacroNumber(), this.page))
               )
               .bounds(toolLeft + 222, toolY, 58, 20)
               .build()
         );
         this.addRenderableWidget(
            Button.builder(
                  Component.literal("Reset"),
                  button -> this.minecraft.setScreen(new MacroResetConfirmScreen(this.runner, this.runner.currentMacroNumber(), this.page))
               )
               .bounds(toolLeft + 288, toolY, 58, 20)
               .build()
         );
         this.addRenderableWidget(
            Button.builder(Component.literal("Marketplace"), button -> this.minecraft.setScreen(new MacroMarketplaceScreen(this.runner, this.page)))
               .bounds(toolLeft + 354, toolY, 98, 20)
               .build()
         );
      } finally {
         this.restoreActualSize(actualWidth, actualHeight);
      }
   }

   public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
      int actualWidth = this.width;
      int actualHeight = this.height;
      double scale = this.layoutScale(actualWidth, actualHeight);
      int layoutMouseX = this.toLayoutMouse(mouseX, scale);
      int layoutMouseY = this.toLayoutMouse(mouseY, scale);
      context.pose().pushPose();
      context.pose().scale((float)scale, (float)scale, 1.0F);
      this.useLayoutSize(actualWidth, actualHeight);

      try {
         super.render(context, layoutMouseX, layoutMouseY, delta);
         context.drawCenteredString(this.font, this.title, this.width / 2, 28, -1);
         context.drawCenteredString(this.font, Component.literal("Page " + (this.page + 1) + " / " + this.totalPages()), this.width / 2, 44, -4204545);
         context.drawCenteredString(this.font, Component.literal("Selected: Macro " + this.runner.currentMacroNumber()), this.width / 2, 58, -1);
         if (this.bindingMacroNumber > 0) {
            context.drawCenteredString(
               this.font,
               Component.literal("Press a key for Macro " + this.bindingMacroNumber + ". Esc, Backspace, or Delete clears it."),
               this.width / 2,
               70,
               -3672
            );
         }

         context.drawCenteredString(this.font, Component.literal(this.fit(this.runner.status(), this.width - 32)), this.width / 2, this.height - 52, -2565928);
         context.drawCenteredString(
            this.font, Component.literal(this.fit(this.runner.macroPath().toString(), this.width - 32)), this.width / 2, this.height - 38, -6645094
         );
      } finally {
         this.restoreActualSize(actualWidth, actualHeight);
         context.pose().popPose();
      }
   }

   public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
      context.fill(0, 0, this.width, this.height, -804253680);
   }

   public boolean isPauseScreen() {
      return false;
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      int actualWidth = this.width;
      int actualHeight = this.height;
      double scale = this.layoutScale(actualWidth, actualHeight);
      this.useLayoutSize(actualWidth, actualHeight);

      try {
         return super.mouseClicked(mouseX / scale, mouseY / scale, button);
      } finally {
         this.restoreActualSize(actualWidth, actualHeight);
      }
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      int actualWidth = this.width;
      int actualHeight = this.height;
      double scale = this.layoutScale(actualWidth, actualHeight);
      this.useLayoutSize(actualWidth, actualHeight);

      try {
         return super.mouseReleased(mouseX / scale, mouseY / scale, button);
      } finally {
         this.restoreActualSize(actualWidth, actualHeight);
      }
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
      int actualWidth = this.width;
      int actualHeight = this.height;
      double scale = this.layoutScale(actualWidth, actualHeight);
      this.useLayoutSize(actualWidth, actualHeight);

      try {
         return super.mouseDragged(mouseX / scale, mouseY / scale, button, dragX / scale, dragY / scale);
      } finally {
         this.restoreActualSize(actualWidth, actualHeight);
      }
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.bindingMacroNumber <= 0) {
         return super.keyPressed(keyCode, scanCode, modifiers);
      } else {
         if (keyCode != 256 && keyCode != 259 && keyCode != 261) {
            this.runner.setMacroKey(this.bindingMacroNumber, keyCode);
         } else {
            this.runner.clearMacroKey(this.bindingMacroNumber);
         }

         this.minecraft.setScreen(new MacroListScreen(this.runner, this.page));
         return true;
      }
   }

   private int totalPages() {
      return Math.max(1, (int)Math.ceil(this.runner.macroCount() / 10.0));
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
