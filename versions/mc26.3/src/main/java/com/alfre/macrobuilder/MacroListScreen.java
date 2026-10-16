package com.alfre.macrobuilder;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

final class MacroListScreen extends Screen {
   private static final int PAGE_SIZE = 10;
   private static final int MIN_LAYOUT_WIDTH = 960;
   private static final int MIN_LAYOUT_HEIGHT = 540;
   private static final int BG = 0xFF0B1117;
   private static final int GRID = 0x332B4153;
   private static final int PANEL = 0xF0101820;
   private static final int PANEL_2 = 0xFF18232E;
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
   private final MacroRunner runner;
   private final int page;
   private int bindingMacroNumber;

   MacroListScreen(MacroRunner runner) {
      this(runner, (runner.currentMacroNumber() - 1) / PAGE_SIZE);
   }

   MacroListScreen(MacroRunner runner, int page) {
      super(Component.literal("Litemacro Macros"));
      this.runner = runner;
      int maxPage = Math.max(0, (int)Math.ceil(runner.macroCount() / (double)PAGE_SIZE) - 1);
      this.page = Math.max(0, Math.min(maxPage, page));
   }

   protected void init() {
   }

   public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      int actualWidth = this.width;
      int actualHeight = this.height;
      double scale = this.layoutScale(actualWidth, actualHeight);
      int layoutMouseX = this.toLayoutMouse(mouseX, scale);
      int layoutMouseY = this.toLayoutMouse(mouseY, scale);
      context.pose().pushMatrix();
      context.pose().scale((float)scale, (float)scale);
      this.useLayoutSize(actualWidth, actualHeight);

      try {
         super.extractRenderState(context, layoutMouseX, layoutMouseY, delta);
         this.drawBackgroundGrid(context);
         this.drawScreen(context, layoutMouseX, layoutMouseY);
      } finally {
         this.restoreActualSize(actualWidth, actualHeight);
         context.pose().popMatrix();
      }
   }

   public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      context.fill(0, 0, this.width, this.height, BG);
   }

   private void drawScreen(GuiGraphicsExtractor context, int mouseX, int mouseY) {
      this.drawTopBar(context, mouseX, mouseY);
      Rect list = this.listPanel();
      this.drawPanel(context, list);
      context.text(this.font, "SAVED MACROS", list.x + 14, list.y + 11, ACCENT);
      context.text(this.font, "Select a macro or use the quick actions", list.x + list.w - 212, list.y + 11, MUTED);

      int startMacro = this.page * PAGE_SIZE + 1;
      int endMacro = Math.min(this.runner.macroCount(), startMacro + PAGE_SIZE - 1);
      for (int macroNumber = startMacro; macroNumber <= endMacro; macroNumber++) {
         this.drawMacroRow(context, mouseX, mouseY, macroNumber, macroNumber - startMacro);
      }
      if (endMacro < startMacro) {
         context.centeredText(this.font, "No macros on this page", list.x + list.w / 2, list.y + 150, MUTED);
      }

      this.drawActionPanel(context, mouseX, mouseY, endMacro);
      this.drawStatusPanel(context);
   }

   private void drawTopBar(GuiGraphicsExtractor context, int mouseX, int mouseY) {
      Rect bar = new Rect(0, 0, this.width, 56);
      context.fill(bar.x, bar.y, bar.right(), bar.bottom(), 0xFF121B24);
      context.fill(0, 55, this.width, 56, BORDER);
      int left = this.contentLeft();
      int contentWidth = this.contentWidth();
      context.text(this.font, "Litemacro", left, 12, TEXT);
      context.text(this.font, "MACRO LIBRARY", left, 29, ACCENT);
      String summary = this.bindingMacroNumber > 0
         ? "Press a key for Macro " + this.bindingMacroNumber + "  |  Esc clears"
         : "Page " + (this.page + 1) + " / " + this.totalPages() + "  |  Selected: " + this.runner.macroName(this.runner.currentMacroNumber());
      context.centeredText(this.font, this.fit(summary, 400), left + 420, 22, this.bindingMacroNumber > 0 ? ORANGE : MUTED);
      this.drawButton(context, this.overlayRect(), this.runner.runningOverlay() ? "Overlay On" : "Overlay Off", mouseX, mouseY, this.runner.runningOverlay() ? GREEN : MUTED, true);
      this.drawButton(context, this.chatRect(), this.runner.chatNotifications() ? "Chat On" : "Chat Off", mouseX, mouseY, this.runner.chatNotifications() ? GREEN : MUTED, true);
      this.drawButton(context, this.closeRect(), "Close", mouseX, mouseY, TEXT, true);
   }

   private void drawMacroRow(GuiGraphicsExtractor context, int mouseX, int mouseY, int macroNumber, int row) {
      Rect whole = this.rowRect(row);
      boolean selected = macroNumber == this.runner.currentMacroNumber();
      context.fill(whole.x, whole.y, whole.right(), whole.bottom(), selected ? PANEL_3 : PANEL_2);
      context.fill(whole.x, whole.y, whole.x + (selected ? 4 : 1), whole.bottom(), selected ? ACCENT : BORDER);
      context.fill(whole.x, whole.bottom() - 1, whole.right(), whole.bottom(), BORDER);
      Rect select = this.rowSelectRect(row);
      String number = String.format("%02d", macroNumber);
      context.text(this.font, number, select.x + 12, select.y + 8, selected ? ACCENT : DIM);
      context.text(this.font, this.fit(this.runner.macroName(macroNumber), select.w - 52), select.x + 42, select.y + 8, TEXT);
      this.drawButton(context, this.rowEditRect(row), "Edit", mouseX, mouseY, TEXT, true);
      this.drawButton(context, this.rowRunRect(row), "Run", mouseX, mouseY, GREEN, true);
      this.drawButton(context, this.rowKeyRect(row), "Key: " + this.runner.macroKeyName(macroNumber), mouseX, mouseY, ORANGE, true);
      this.drawButton(context, this.rowClearRect(row), "Clear", mouseX, mouseY, RED, true);
   }

   private void drawActionPanel(GuiGraphicsExtractor context, int mouseX, int mouseY, int endMacro) {
      Rect panel = this.actionPanel();
      this.drawPanel(context, panel);
      int x = panel.x;
      context.text(this.font, "CURRENT MACRO", x + 14, panel.y + 10, ACCENT);
      context.text(this.font, this.fit(this.runner.currentMacroNumber() + ". " + this.runner.macroName(this.runner.currentMacroNumber()), 196), x + 14, panel.y + 26, TEXT);
      this.drawButton(context, this.prevRect(), "< Prev", mouseX, mouseY, TEXT, this.page > 0);
      this.drawButton(context, this.nextRect(), "Next >", mouseX, mouseY, TEXT, endMacro < this.runner.macroCount());
      this.drawButton(context, this.editSelectedRect(), "Edit Selected", mouseX, mouseY, TEXT, true);
      this.drawButton(context, this.runSelectedRect(), "Run Selected", mouseX, mouseY, GREEN, true);
      this.drawButton(context, this.stopRect(), "Stop", mouseX, mouseY, RED, true);
      this.drawButton(context, this.copyRect(), "Copy", mouseX, mouseY, TEXT, true);
      this.drawButton(context, this.pasteRect(), "Paste", mouseX, mouseY, TEXT, true);
      this.drawButton(context, this.exportRect(), "Export", mouseX, mouseY, TEXT, true);
      this.drawButton(context, this.importRect(), "Import", mouseX, mouseY, TEXT, true);
      this.drawButton(context, this.resetRect(), "Reset", mouseX, mouseY, RED, true);
      this.drawButton(context, this.marketRect(), "Marketplace", mouseX, mouseY, ACCENT, true);
   }

   private void drawStatusPanel(GuiGraphicsExtractor context) {
      Rect panel = this.statusPanel();
      this.drawPanel(context, panel);
      context.text(this.font, "STATUS", panel.x + 12, panel.y + 11, ACCENT);
      context.text(this.font, this.fit(this.runner.status(), panel.w - 78), panel.x + 64, panel.y + 11, TEXT);
   }

   private void drawBackgroundGrid(GuiGraphicsExtractor context) {
      context.fill(0, 0, this.width, this.height, BG);
      for (int x = 0; x < this.width; x += 32) context.fill(x, 56, x + 1, this.height, GRID);
      for (int y = 56; y < this.height; y += 32) context.fill(0, y, this.width, y + 1, GRID);
   }

   private void drawPanel(GuiGraphicsExtractor context, Rect rect) {
      context.fill(rect.x, rect.y, rect.right(), rect.bottom(), PANEL);
      context.fill(rect.x, rect.y, rect.right(), rect.y + 1, BORDER);
      context.fill(rect.x, rect.bottom() - 1, rect.right(), rect.bottom(), BORDER);
      context.fill(rect.x, rect.y, rect.x + 1, rect.bottom(), BORDER);
      context.fill(rect.right() - 1, rect.y, rect.right(), rect.bottom(), BORDER);
   }

   private void drawButton(GuiGraphicsExtractor context, Rect rect, String label, int mouseX, int mouseY, int color, boolean active) {
      boolean hovered = active && rect.contains(mouseX, mouseY);
      int fill = active ? (hovered ? PANEL_3 : PANEL_2) : 0xFF121920;
      int edge = active ? (hovered ? color : BORDER_HI) : 0xFF283642;
      context.fill(rect.x, rect.y, rect.right(), rect.bottom(), fill);
      context.fill(rect.x, rect.y, rect.right(), rect.y + 1, edge);
      context.fill(rect.x, rect.bottom() - 1, rect.right(), rect.bottom(), edge);
      context.fill(rect.x, rect.y, rect.x + 1, rect.bottom(), edge);
      context.fill(rect.right() - 1, rect.y, rect.right(), rect.bottom(), edge);
      context.centeredText(this.font, this.fit(label, rect.w - 8), rect.x + rect.w / 2, rect.y + (rect.h - 8) / 2, active ? color : DIM);
   }

   public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
      int actualWidth = this.width;
      int actualHeight = this.height;
      double scale = this.layoutScale(actualWidth, actualHeight);
      click = this.layoutClick(click, scale);
      this.useLayoutSize(actualWidth, actualHeight);
      try {
         if (this.handleClick(click.x(), click.y())) return true;
         return super.mouseClicked(click, doubled);
      } finally {
         this.restoreActualSize(actualWidth, actualHeight);
      }
   }

   private boolean handleClick(double x, double y) {
      if (this.overlayRect().contains(x, y)) { this.runner.toggleRunningOverlay(); return true; }
      if (this.chatRect().contains(x, y)) { this.runner.toggleChatNotifications(); return true; }
      if (this.closeRect().contains(x, y)) { this.onClose(); return true; }
      int startMacro = this.page * PAGE_SIZE + 1;
      int endMacro = Math.min(this.runner.macroCount(), startMacro + PAGE_SIZE - 1);
      for (int macroNumber = startMacro; macroNumber <= endMacro; macroNumber++) {
         int row = macroNumber - startMacro;
         if (this.rowEditRect(row).contains(x, y)) { this.runner.selectMacro(this.minecraft, macroNumber); this.minecraft.setScreenAndShow(new MacroBuilderScreen(this.runner, this.runner.model())); return true; }
         if (this.rowRunRect(row).contains(x, y)) { this.runner.toggleMacro(this.minecraft, macroNumber); return true; }
         if (this.rowKeyRect(row).contains(x, y)) { this.bindingMacroNumber = macroNumber; return true; }
         if (this.rowClearRect(row).contains(x, y)) { this.runner.clearMacroKey(macroNumber); return true; }
         if (this.rowSelectRect(row).contains(x, y)) { this.runner.selectMacro(this.minecraft, macroNumber); return true; }
      }
      if (this.page > 0 && this.prevRect().contains(x, y)) { this.minecraft.setScreenAndShow(new MacroListScreen(this.runner, this.page - 1)); return true; }
      if (endMacro < this.runner.macroCount() && this.nextRect().contains(x, y)) { this.minecraft.setScreenAndShow(new MacroListScreen(this.runner, this.page + 1)); return true; }
      if (this.editSelectedRect().contains(x, y)) { this.minecraft.setScreenAndShow(new MacroBuilderScreen(this.runner, this.runner.model())); return true; }
      if (this.runSelectedRect().contains(x, y)) { this.runner.toggleMacro(this.minecraft, this.runner.currentMacroNumber()); return true; }
      if (this.stopRect().contains(x, y)) { this.runner.stop(this.minecraft, "Stopped."); return true; }
      if (this.copyRect().contains(x, y)) { this.runner.copyMacro(this.minecraft, this.runner.currentMacroNumber()); return true; }
      if (this.pasteRect().contains(x, y)) { this.runner.pasteCopiedMacro(this.minecraft, this.runner.currentMacroNumber()); return true; }
      if (this.exportRect().contains(x, y)) { this.runner.exportMacroToDownloads(this.minecraft, this.runner.currentMacroNumber()); return true; }
      if (this.importRect().contains(x, y)) { this.minecraft.setScreenAndShow(new MacroImportScreen(this.runner, this.runner.currentMacroNumber(), this.page)); return true; }
      if (this.resetRect().contains(x, y)) { this.minecraft.setScreenAndShow(new MacroResetConfirmScreen(this.runner, this.runner.currentMacroNumber(), this.page)); return true; }
      if (this.marketRect().contains(x, y)) { this.minecraft.setScreenAndShow(new MacroMarketplaceScreen(this.runner, this.page)); return true; }
      return false;
   }

   public boolean keyPressed(KeyEvent input) {
      if (this.bindingMacroNumber <= 0) return super.keyPressed(input);
      int keyCode = input.key();
      if (keyCode == 256 || keyCode == 259 || keyCode == 261) this.runner.clearMacroKey(this.bindingMacroNumber);
      else this.runner.setMacroKey(this.bindingMacroNumber, keyCode);
      this.bindingMacroNumber = 0;
      return true;
   }

   public boolean isPauseScreen() { return false; }
   private int contentWidth() { return Math.min(900, this.width - 32); }
   private int contentLeft() { return (this.width - this.contentWidth()) / 2; }
   private Rect listPanel() { return new Rect(this.contentLeft(), 66, this.contentWidth(), 312); }
   private Rect actionPanel() { return new Rect(this.contentLeft(), 388, this.contentWidth(), 80); }
   private Rect statusPanel() { return new Rect(this.contentLeft(), this.height - 42, this.contentWidth(), 30); }
   private Rect overlayRect() { return new Rect(this.contentLeft() + this.contentWidth() - 250, 14, 82, 28); }
   private Rect chatRect() { return new Rect(this.contentLeft() + this.contentWidth() - 162, 14, 74, 28); }
   private Rect closeRect() { return new Rect(this.contentLeft() + this.contentWidth() - 82, 14, 82, 28); }
   private Rect rowRect(int row) { return new Rect(this.contentLeft() + 10, 92 + row * 28, this.contentWidth() - 20, 24); }
   private Rect rowSelectRect(int row) { Rect r = this.rowRect(row); return new Rect(r.x, r.y, r.w - 304, r.h); }
   private Rect rowEditRect(int row) { Rect r = this.rowRect(row); return new Rect(r.right() - 296, r.y + 2, 52, 20); }
   private Rect rowRunRect(int row) { Rect r = this.rowRect(row); return new Rect(r.right() - 238, r.y + 2, 52, 20); }
   private Rect rowKeyRect(int row) { Rect r = this.rowRect(row); return new Rect(r.right() - 180, r.y + 2, 116, 20); }
   private Rect rowClearRect(int row) { Rect r = this.rowRect(row); return new Rect(r.right() - 58, r.y + 2, 54, 20); }
   private Rect prevRect() { return new Rect(this.contentLeft() + 14, 438, 58, 22); }
   private Rect nextRect() { return new Rect(this.contentLeft() + 78, 438, 58, 22); }
   private Rect editSelectedRect() { return new Rect(this.contentLeft() + 242, 398, 132, 26); }
   private Rect runSelectedRect() { return new Rect(this.contentLeft() + 382, 398, 132, 26); }
   private Rect stopRect() { return new Rect(this.contentLeft() + 522, 398, 74, 26); }
   private Rect copyRect() { return new Rect(this.contentLeft() + 242, 432, 64, 24); }
   private Rect pasteRect() { return new Rect(this.contentLeft() + 312, 432, 64, 24); }
   private Rect exportRect() { return new Rect(this.contentLeft() + 382, 432, 72, 24); }
   private Rect importRect() { return new Rect(this.contentLeft() + 460, 432, 72, 24); }
   private Rect resetRect() { return new Rect(this.contentLeft() + 538, 432, 72, 24); }
   private Rect marketRect() { return new Rect(this.contentLeft() + 616, 432, 112, 24); }
   private int totalPages() { return Math.max(1, (int)Math.ceil(this.runner.macroCount() / (double)PAGE_SIZE)); }
   private MouseButtonEvent layoutClick(MouseButtonEvent click, double scale) {
      return scale >= 1.0 ? click : new MouseButtonEvent(click.x() / scale, click.y() / scale, click.buttonInfo());
   }

   private int toLayoutMouse(int coordinate, double scale) { return scale >= 1.0 ? coordinate : (int)Math.round(coordinate / scale); }
   private void useLayoutSize(int actualWidth, int actualHeight) { double scale = this.layoutScale(actualWidth, actualHeight); this.width = Math.max(MIN_LAYOUT_WIDTH, (int)Math.ceil(actualWidth / scale)); this.height = Math.max(MIN_LAYOUT_HEIGHT, (int)Math.ceil(actualHeight / scale)); }
   private void restoreActualSize(int actualWidth, int actualHeight) { this.width = actualWidth; this.height = actualHeight; }
   private double layoutScale(int actualWidth, int actualHeight) { return actualWidth <= 0 || actualHeight <= 0 ? 1.0 : Math.min(1.0, Math.min(actualWidth / (double)MIN_LAYOUT_WIDTH, actualHeight / (double)MIN_LAYOUT_HEIGHT)); }

   private String fit(String text, int maxWidth) {
      if (text == null) return "";
      if (this.font == null || this.font.width(text) <= maxWidth) return text;
      String suffix = "...";
      String trimmed = text;
      while (!trimmed.isEmpty() && this.font.width(trimmed) + this.font.width(suffix) > maxWidth) trimmed = trimmed.substring(0, trimmed.length() - 1);
      return trimmed + suffix;
   }

   private record Rect(int x, int y, int w, int h) {
      int right() { return this.x + this.w; }
      int bottom() { return this.y + this.h; }
      boolean contains(double px, double py) { return px >= this.x && px < this.right() && py >= this.y && py < this.bottom(); }
   }
}
