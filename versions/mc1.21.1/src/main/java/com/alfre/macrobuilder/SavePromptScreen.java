package com.alfre.macrobuilder;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

final class SavePromptScreen extends Screen {
   private final MacroBuilderScreen builder;

   SavePromptScreen(MacroBuilderScreen builder) {
      super(Component.literal("Unsaved Changes"));
      this.builder = builder;
   }

   protected void init() {
      int panelWidth = 280;
      int buttonWidth = 82;
      int left = (this.width - panelWidth) / 2;
      int top = Math.max(50, (this.height - 116) / 2);
      int buttonY = top + 76;
      this.addRenderableWidget(
         Button.builder(Component.literal("Save"), button -> this.builder.saveAndLeaveToMacroList()).bounds(left + 12, buttonY, buttonWidth, 20).build()
      );
      this.addRenderableWidget(
         Button.builder(Component.literal("Discard"), button -> this.builder.discardAndLeaveToMacroList()).bounds(left + 99, buttonY, buttonWidth, 20).build()
      );
      this.addRenderableWidget(
         Button.builder(Component.literal("Cancel"), button -> this.builder.cancelSavePrompt()).bounds(left + 186, buttonY, buttonWidth, 20).build()
      );
   }

   public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context, mouseX, mouseY, delta);
      int panelWidth = 280;
      int left = (this.width - panelWidth) / 2;
      int top = Math.max(50, (this.height - 116) / 2);
      context.fill(left, top, left + panelWidth, top + 108, -14867926);
      context.drawCenteredString(this.font, this.title, this.width / 2, top + 14, -1);
      context.drawCenteredString(this.font, Component.literal("Save changes before going back?"), this.width / 2, top + 36, -2565928);
      context.drawCenteredString(this.font, Component.literal("Discard will keep the last saved version."), this.width / 2, top + 50, -5327166);
      super.render(context, mouseX, mouseY, delta);
   }

   public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
      context.fill(0, 0, this.width, this.height, -804253680);
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 256) {
         this.builder.cancelSavePrompt();
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   public boolean isPauseScreen() {
      return false;
   }
}
