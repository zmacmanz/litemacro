package com.alfre.macrobuilder;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

final class MacroAutoSettingsScreen extends Screen {
   private final MacroBuilderScreen parent;
   private final MacroModel model;
   private boolean alwaysOn;
   private String autoStartMode;
   private EditBox filterField;
   private Button alwaysButton;
   private Button modeButton;

   MacroAutoSettingsScreen(MacroBuilderScreen parent, MacroModel model) {
      super(Component.literal("Auto Start"));
      this.parent = parent;
      this.model = model;
      this.alwaysOn = model.alwaysOn();
      this.autoStartMode = model.autoStartMode();
   }

   protected void init() {
      int panelWidth = 336;
      int panelHeight = 206;
      int left = this.width / 2 - panelWidth / 2;
      int top = this.height / 2 - panelHeight / 2;
      this.alwaysButton = (Button)this.addRenderableWidget(
         Button.builder(Component.literal(this.alwaysText()), button -> {
            this.alwaysOn = !this.alwaysOn;
            this.alwaysButton.setMessage(Component.literal(this.alwaysText()));
         }).bounds(left + 16, top + 44, 304, 20).build()
      );
      this.modeButton = (Button)this.addRenderableWidget(
         Button.builder(Component.literal(this.modeText()), button -> {
            this.autoStartMode = this.nextMode(this.autoStartMode);
            this.modeButton.setMessage(Component.literal(this.modeText()));
         }).bounds(left + 16, top + 76, 304, 20).build()
      );
      this.filterField = new EditBox(this.font, left + 16, top + 124, 304, 20, Component.empty());
      this.filterField.setMaxLength(256);
      this.filterField.setValue(this.model.autoStartFilter());
      this.filterField.setSuggestion("optional chat text, player, or kick reason");
      this.addRenderableWidget(this.filterField);
      this.addRenderableWidget(Button.builder(Component.literal("Apply"), button -> this.applyAndClose()).bounds(left + 128, top + 170, 92, 20).build());
      this.addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> this.onClose()).bounds(left + 228, top + 170, 92, 20).build());
   }

   public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      this.extractBackground(context, mouseX, mouseY, delta);
      int panelWidth = 336;
      int panelHeight = 206;
      int left = this.width / 2 - panelWidth / 2;
      int top = this.height / 2 - panelHeight / 2;
      context.fill(left, top, left + panelWidth, top + panelHeight, -15724528);
      context.fill(left, top, left + panelWidth, top + 1, -9205921);
      context.fill(left, top + panelHeight - 1, left + panelWidth, top + panelHeight, -9205921);
      context.fill(left, top, left + 1, top + panelHeight, -9205921);
      context.fill(left + panelWidth - 1, top, left + panelWidth, top + panelHeight, -9205921);
      context.centeredText(this.font, this.title, this.width / 2, top + 14, -1);
      context.text(this.font, "Filter", left + 16, top + 112, -2565928);
      context.text(this.font, "Chat/player/kicked can use this filter.", left + 16, top + 150, -7366491);
      super.extractRenderState(context, mouseX, mouseY, delta);
   }

   public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      context.fill(0, 0, this.width, this.height, -804253680);
   }

   public boolean isPauseScreen() {
      return false;
   }

   public void onClose() {
      this.minecraft.setScreenAndShow(this.parent);
   }

   private void applyAndClose() {
      this.model.setAlwaysOn(this.alwaysOn);
      this.model.setAutoStartMode(this.autoStartMode);
      this.model.setAutoStartFilter(this.filterField.getValue());
      this.parent.refreshAfterAutoSettings();
      this.minecraft.setScreenAndShow(this.parent);
   }

   private String alwaysText() {
      return this.alwaysOn ? "Always On: On" : "Always On: Off";
   }

   private String modeText() {
      return "Enable When: " + this.modeLabel(this.autoStartMode);
   }

   private String nextMode(String mode) {
      return switch (mode == null ? "" : mode) {
         case "off", "" -> "chat";
         case "chat" -> "player";
         case "player" -> "inventory_full";
         case "inventory_full" -> "kicked";
         case "kicked" -> "off";
         default -> "off";
      };
   }

   private String modeLabel(String mode) {
      return switch (mode == null ? "" : mode) {
         case "chat" -> "Chat Message";
         case "player" -> "Player Nearby";
         case "inventory_full" -> "Inventory Full";
         case "kicked" -> "Kicked/Disconnected";
         default -> "Off";
      };
   }
}
