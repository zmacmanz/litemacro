package com.alfre.macrobuilder;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

final class RandomDelaySettingsScreen extends Screen {
   private final MacroBuilderScreen parent;
   private final MacroModel model;
   private EditBox minField;
   private EditBox maxField;
   private Button enabledButton;

   RandomDelaySettingsScreen(MacroBuilderScreen parent, MacroModel model) {
      super(Component.literal("Random Delay"));
      this.parent = parent;
      this.model = model;
   }

   protected void init() {
      int panelWidth = 300;
      int panelHeight = 164;
      int left = this.width / 2 - panelWidth / 2;
      int top = this.height / 2 - panelHeight / 2;
      this.enabledButton = (Button)this.addRenderableWidget(
         Button.builder(Component.literal(this.enabledText()), button -> {
            this.model.setRandomDelayEnabled(!this.model.randomDelayEnabled());
            this.enabledButton.setMessage(Component.literal(this.enabledText()));
         }).bounds(left + 16, top + 44, 268, 20).build()
      );
      this.minField = new EditBox(this.font, left + 92, top + 78, 192, 20, Component.empty());
      this.minField.setMaxLength(8);
      this.minField.setValue(Integer.toString(this.model.randomDelayMinMs()));
      this.addRenderableWidget(this.minField);
      this.maxField = new EditBox(this.font, left + 92, top + 106, 192, 20, Component.empty());
      this.maxField.setMaxLength(8);
      this.maxField.setValue(Integer.toString(this.model.randomDelayMaxMs()));
      this.addRenderableWidget(this.maxField);
      this.addRenderableWidget(Button.builder(Component.literal("Apply"), button -> this.applyAndClose()).bounds(left + 92, top + 136, 92, 20).build());
      this.addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> this.onClose()).bounds(left + 192, top + 136, 92, 20).build());
   }

   public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
      this.renderBackground(context, mouseX, mouseY, delta);
      int panelWidth = 300;
      int panelHeight = 164;
      int left = this.width / 2 - panelWidth / 2;
      int top = this.height / 2 - panelHeight / 2;
      context.fill(left, top, left + panelWidth, top + panelHeight, -15724528);
      context.fill(left, top, left + panelWidth, top + 1, -9205921);
      context.fill(left, top + panelHeight - 1, left + panelWidth, top + panelHeight, -9205921);
      context.fill(left, top, left + 1, top + panelHeight, -9205921);
      context.fill(left + panelWidth - 1, top, left + panelWidth, top + panelHeight, -9205921);
      context.drawCenteredString(this.font, this.title, this.width / 2, top + 14, -1);
      context.drawString(this.font, "Min ms", left + 16, top + 84, -2565928);
      context.drawString(this.font, "Max ms", left + 16, top + 112, -2565928);
      super.render(context, mouseX, mouseY, delta);
   }

   public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
      context.fill(0, 0, this.width, this.height, -804253680);
   }

   public boolean isPauseScreen() {
      return false;
   }

   public void onClose() {
      this.minecraft.setScreen(this.parent);
   }

   private void applyAndClose() {
      this.model.setRandomDelayRangeMs(this.parseMs(this.minField.getValue(), this.model.randomDelayMinMs()), this.parseMs(this.maxField.getValue(), this.model.randomDelayMaxMs()));
      this.parent.refreshAfterRandomDelaySettings();
      this.minecraft.setScreen(this.parent);
   }

   private int parseMs(String text, int fallback) {
      try {
         return Math.max(0, Integer.parseInt(text.trim()));
      } catch (RuntimeException var4) {
         return fallback;
      }
   }

   private String enabledText() {
      return this.model.randomDelayEnabled() ? "Random Delay: On" : "Random Delay: Off";
   }
}
