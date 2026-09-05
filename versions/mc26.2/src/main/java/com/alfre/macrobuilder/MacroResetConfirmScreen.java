package com.alfre.macrobuilder;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

final class MacroResetConfirmScreen extends Screen {
   private final MacroRunner runner;
   private final int macroNumber;
   private final int returnPage;

   MacroResetConfirmScreen(MacroRunner runner, int macroNumber, int returnPage) {
      super(Component.literal("Reset Macro"));
      this.runner = runner;
      this.macroNumber = macroNumber;
      this.returnPage = returnPage;
   }

   protected void init() {
      int panelWidth = 320;
      int left = (this.width - panelWidth) / 2;
      int top = Math.max(50, (this.height - 130) / 2);
      int buttonY = top + 92;
      this.addRenderableWidget(Button.builder(Component.literal("Reset"), button -> {
         this.runner.resetMacro(this.minecraft, this.macroNumber);
         this.minecraft.setScreenAndShow(new MacroListScreen(this.runner, this.returnPage));
      }).bounds(left + 40, buttonY, 104, 20).build());
      this.addRenderableWidget(
         Button.builder(Component.literal("Cancel"), button -> this.minecraft.setScreenAndShow(new MacroListScreen(this.runner, this.returnPage)))
            .bounds(left + 176, buttonY, 104, 20)
            .build()
      );
   }

   public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      this.extractBackground(context, mouseX, mouseY, delta);
      int panelWidth = 320;
      int left = (this.width - panelWidth) / 2;
      int top = Math.max(50, (this.height - 130) / 2);
      context.fill(left, top, left + panelWidth, top + 122, -14867926);
      context.centeredText(this.font, this.title, this.width / 2, top + 14, -1);
      context.centeredText(this.font, Component.literal("Reset Macro " + this.macroNumber + " back to new?"), this.width / 2, top + 36, -2565928);
      context.centeredText(this.font, Component.literal("This clears components, name, and keybind."), this.width / 2, top + 52, -11823);
      context.centeredText(this.font, Component.literal("Download or copy first if you need a backup."), this.width / 2, top + 68, -5327166);
      super.extractRenderState(context, mouseX, mouseY, delta);
   }

   public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      context.fill(0, 0, this.width, this.height, -804253680);
   }

   public boolean keyPressed(KeyEvent input) {
      if (input.key() == 256) {
         this.minecraft.setScreenAndShow(new MacroListScreen(this.runner, this.returnPage));
         return true;
      } else {
         return super.keyPressed(input);
      }
   }

   public boolean isPauseScreen() {
      return false;
   }
}
