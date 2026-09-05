package com.alfre.macrobuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

final class MacroBuilderScreen extends Screen {
   private static final int TOP_BAR_HEIGHT = 36;
   private static final int TOP_BUTTON_Y = 7;
   private static final int TOP_BUTTON_HEIGHT = 18;
   private static final int TOP_MORE_X = 198;
   private static final int TOP_MORE_WIDTH = 48;
   private static final int TOP_ACTION_MENU_WIDTH = 86;
   private static final int TOP_ACTION_ROW_HEIGHT = 18;
   private static final int PALETTE_WIDTH = 168;
   private static final int PROPERTIES_WIDTH = 238;
   private static final int NODE_WIDTH = 158;
   private static final int NODE_HEIGHT = 74;
   private static final int NOTE_WIDTH = 196;
   private static final int NOTE_HEIGHT = 112;
   private static final int NOTE_MIN_WIDTH = 120;
   private static final int NOTE_MIN_HEIGHT = 84;
   private static final int NOTE_HEADER_HEIGHT = 18;
   private static final int NOTE_TEXT_MARGIN = 10;
   private static final int NOTE_HANDLE_SIZE = 8;
   private static final int NOTE_MAX_CHARS = 4096;
   private static final long NOTE_CARET_BLINK_MS = 500L;
   private static final int NOTE_BODY_COLOR = 0xFF20262E;
   private static final int NOTE_BORDER_COLOR = 0xFF323A44;
   private static final int NOTE_SELECTED_BORDER_COLOR = 0xFFFFFFFF;
   private static final int NOTE_TEXT_COLOR = 0xFFE8EEF7;
   private static final int PALETTE_GROUP_HEIGHT = 22;
   private static final int PALETTE_ITEM_HEIGHT = 34;
   private static final int ITEM_PICKER_CELL = 26;
   private static final int RIGHT_STATUS_Y = 48;
   private static final int RIGHT_COMPONENT_Y = 110;
   private static final int RIGHT_NODE_DELAY_LABEL_Y = 164;
   private static final int RIGHT_NODE_DELAY_FIELD_Y = 178;
   private static final int RIGHT_PRIMARY_LABEL_Y = 224;
   private static final int RIGHT_PRIMARY_FIELD_Y = 238;
   private static final int RIGHT_PRIMARY_ITEM_Y = 264;
   private static final int RIGHT_SECONDARY_LABEL_Y = 290;
   private static final int RIGHT_SECONDARY_FIELD_Y = 304;
   private static final int RIGHT_SECONDARY_ITEM_Y = 330;
   private static final int RIGHT_MODE_Y = 354;
   private static final int RIGHT_EXTRA_LABEL_Y = 400;
   private static final int RIGHT_EXTRA_FIELD_Y = 414;
   private static final int RIGHT_EXTRA_HINT_Y = 438;
   private static final int RIGHT_CONNECTION_LABEL_Y = 454;
   private static final int RIGHT_CONNECTION_BUTTON_Y = 472;
   private static final int RIGHT_ACTION_BUTTON_Y = 500;
   private static final int RIGHT_CONNECTION_LIST_Y = 526;
   private static final int MIN_LAYOUT_WIDTH = 960;
   private static final int MIN_LAYOUT_HEIGHT = 540;
   private static final int CONTEXT_MENU_WIDTH = 136;
   private static final int CONTEXT_MENU_ROW_HEIGHT = 18;
   private static final int CONTEXT_CLICK_DRAG_THRESHOLD = 5;
   private static final long AUTO_SAVE_INTERVAL_MS = 2500L;
   private final MacroRunner runner;
   private final MacroModel model;
   private final Map<String, Boolean> expandedGroups = new LinkedHashMap<>();
   private final List<MacroBuilderScreen.ItemEntry> itemEntries = new ArrayList<>();
   private EditBox componentSearchField;
   private EditBox nameField;
   private EditBox delayField;
   private EditBox primaryField;
   private EditBox secondaryField;
   private EditBox excludeSlotsField;
   private EditBox nodeDelayField;
   private Button setNextButton;
   private Button setTrueButton;
   private Button setFalseButton;
   private Button setToolLowButton;
   private Button setFailedButton;
   private Button setAllDelayButton;
   private Button randomDelayButton;
   private Button autoSettingsButton;
   private Button clearConnectionsButton;
   private Button deleteButton;
   private Button modeButton;
   private Button shiftClickButton;
   private Button fastDepositButton;
   private Button clickButtonButton;
   private Button enabledButton;
   private Button repeatIndefinitelyButton;
   private Button primaryItemButton;
   private Button secondaryItemButton;
   private Button copyComponentButton;
   private Button pasteComponentButton;
   private Button moreActionsButton;
   private MacroModel.Node selectedNode;
   private final List<MacroModel.Node> selectedNodes = new ArrayList<>();
   private MacroModel.Node draggedNode;
   private MacroModel.Node pendingConnectionSource;
   private String pendingConnectionOutput;
   private String paletteDragType;
   private double dragOffsetX;
   private double dragOffsetY;
   private int dragMouseX;
   private int dragMouseY;
   private boolean panning;
   private double panX;
   private double panY;
   private double panStartX;
   private double panStartY;
   private double panStartMouseX;
   private double panStartMouseY;
   private double zoom = 1.0;
   private int paletteScroll;
   private boolean itemPickerOpen;
   private boolean itemPickerSecondary;
   private boolean itemSearchFocused;
   private int itemPickerScroll;
   private String itemSearchText = "";
   private String status = "";
   private int statusColor = -4200769;
   private String savedSnapshot;
   private MacroBuilderScreen.CopiedComponent copiedComponent;
   private List<MacroBuilderScreen.CopiedComponent> copiedComponents = List.of();
   private boolean contextMenuOpen;
   private boolean topActionsMenuOpen;
   private int contextMenuX;
   private int contextMenuY;
   private double contextMenuWorldX;
   private double contextMenuWorldY;
   private MacroModel.Node contextMenuNode;
   private MacroBuilderScreen.ConnectionHit contextMenuConnection;
   private MacroBuilderScreen.ConnectionHit highlightedConnection;
   private MacroBuilderScreen.ConnectionHit draggedConnection;
   private double connectionDragOffsetX;
   private boolean rightClickGesture;
   private boolean rightClickDragged;
   private double rightClickStartMouseX;
   private double rightClickStartMouseY;
   private boolean selectionBoxActive;
   private int selectionBoxStartX;
   private int selectionBoxStartY;
   private int selectionBoxEndX;
   private int selectionBoxEndY;
   private MacroModel.Node editingNote;
   private String editingNoteText = "";
   private int editingNoteCaret;
   private boolean noteCaretVisible = true;
   private long noteCaretLastToggleMs;
   private MacroModel.Node resizingNote;
   private MacroBuilderScreen.NoteResizeCorner resizingNoteCorner;
   private int resizeStartX;
   private int resizeStartY;
   private int resizeStartWidth;
   private int resizeStartHeight;
   private long lastAutoSaveTimeMs;

   MacroBuilderScreen(MacroRunner runner, MacroModel model) {
      super(Component.literal("Macro Builder"));
      this.runner = runner;
      this.model = model;
      this.selectedNode = model.startNode();
      if (this.selectedNode != null) {
         this.selectedNodes.add(this.selectedNode);
      }

      this.savedSnapshot = model.toJson();
      this.lastAutoSaveTimeMs = System.currentTimeMillis();

      for (MacroModel.Descriptor descriptor : MacroModel.paletteDescriptors()) {
         this.expandedGroups.putIfAbsent(descriptor.group(), false);
      }

      BuiltInRegistries.ITEM
         .stream()
         .map(item -> new MacroBuilderScreen.ItemEntry(BuiltInRegistries.ITEM.getKey(item), item))
         .forEach(this.itemEntries::add);
   }

   protected void init() {
      int actualWidth = this.width;
      int actualHeight = this.height;
      this.useLayoutSize(actualWidth, actualHeight);

      try {
         int rightX = this.propertyLeft();
         int rightWidth = this.propertyInnerWidth();
          this.addRenderableWidget(Button.builder(Component.literal("Back"), button -> this.leaveToMacroList()).bounds(8, TOP_BUTTON_Y, 42, TOP_BUTTON_HEIGHT).build());
       this.addRenderableWidget(Button.builder(Component.literal("Save"), button -> this.save()).bounds(54, TOP_BUTTON_Y, 42, TOP_BUTTON_HEIGHT).build());
       this.addRenderableWidget(Button.builder(Component.literal("Run"), button -> {
          this.syncSelectedFromFields();
          this.runner.resume(this.minecraft, this.model);
          this.status = "Running";
          this.statusColor = -4200769;
       }).bounds(100, TOP_BUTTON_Y, 38, TOP_BUTTON_HEIGHT).build());
      this.addRenderableWidget(Button.builder(Component.literal("Stop"), button -> {
          this.runner.stop(this.minecraft, "Stopped.");
          this.status = "Stopped";
          this.statusColor = -11382;
       }).bounds(142, TOP_BUTTON_Y, 42, TOP_BUTTON_HEIGHT).build());
       this.moreActionsButton = (Button)this.addRenderableWidget(
          Button.builder(Component.literal("Market"), button -> {
             this.topActionsMenuOpen = false;
             this.openMarketplace();
          }).bounds(TOP_MORE_X, TOP_BUTTON_Y, TOP_MORE_WIDTH, TOP_BUTTON_HEIGHT).build()
       );
      this.componentSearchField = new EditBox(this.font, 14, 64, Math.max(92, this.paletteWidth() - 28), 18, Component.empty());
      this.componentSearchField.setMaxLength(60);
      this.componentSearchField.setSuggestion("Search components...");
      this.addRenderableWidget(this.componentSearchField);
      this.nameField = new EditBox(this.font, rightX + 56, TOP_BUTTON_Y, Math.max(82, this.propertiesWidth() - 70), TOP_BUTTON_HEIGHT, Component.empty());
      this.nameField.setMaxLength(60);
      this.nameField.setValue(this.model.name());
      this.addRenderableWidget(this.nameField);
      this.delayField = new EditBox(this.font, this.globalDelayFieldX(), TOP_BUTTON_Y, 40, TOP_BUTTON_HEIGHT, Component.empty());
      this.delayField.setMaxLength(8);
      this.delayField.setValue(Integer.toString(this.model.stepDelayMs()));
      this.addRenderableWidget(this.delayField);
      this.setAllDelayButton = (Button)this.addRenderableWidget(
         Button.builder(Component.literal("All"), button -> this.setAllComponentDelays()).bounds(this.globalDelayButtonX(), TOP_BUTTON_Y, 36, TOP_BUTTON_HEIGHT).build()
      );
      this.randomDelayButton = (Button)this.addRenderableWidget(
         Button.builder(Component.literal("Rand"), button -> this.openRandomDelaySettings()).bounds(this.globalDelayButtonX() + 40, TOP_BUTTON_Y, 42, TOP_BUTTON_HEIGHT).build()
      );
      this.autoSettingsButton = (Button)this.addRenderableWidget(
          Button.builder(Component.literal("Auto: Off"), button -> this.openAutoSettings()).bounds(rightX + 14, 82, rightWidth, 20).build()
      );
      this.enabledButton = (Button)this.addRenderableWidget(
         Button.builder(Component.literal("Enabled: On"), button -> this.toggleSelectedEnabled()).bounds(rightX + 14, 202, rightWidth, 20).build()
      );
      this.primaryField = new EditBox(this.font, rightX + 14, 238, rightWidth, 20, Component.empty());
      this.primaryField.setMaxLength(2048);
      this.addRenderableWidget(this.primaryField);
      this.secondaryField = new EditBox(this.font, rightX + 14, 304, rightWidth, 20, Component.empty());
      this.secondaryField.setMaxLength(2048);
      this.addRenderableWidget(this.secondaryField);
      this.excludeSlotsField = new EditBox(this.font, rightX + 14, 414, rightWidth, 20, Component.empty());
      this.excludeSlotsField.setMaxLength(128);
      this.addRenderableWidget(this.excludeSlotsField);
      this.nodeDelayField = new EditBox(this.font, rightX + 14, 178, rightWidth, 20, Component.empty());
      this.nodeDelayField.setMaxLength(8);
      this.addRenderableWidget(this.nodeDelayField);
      this.primaryItemButton = (Button)this.addRenderableWidget(
         Button.builder(Component.literal("Pick Item"), button -> this.openItemPicker(false)).bounds(rightX + 14, 264, rightWidth, 20).build()
      );
      this.secondaryItemButton = (Button)this.addRenderableWidget(
         Button.builder(Component.literal("Pick Item"), button -> this.openItemPicker(true)).bounds(rightX + 14, 330, rightWidth, 20).build()
      );
      this.modeButton = (Button)this.addRenderableWidget(
         Button.builder(Component.literal("Toggle Mode"), button -> this.toggleDepositMode()).bounds(rightX + 14, 354, Math.max(78, (rightWidth - 8) / 2), 20).build()
      );
      this.shiftClickButton = (Button)this.addRenderableWidget(
         Button.builder(Component.literal("Shift: On"), button -> this.toggleDepositWithdrawShiftClick())
            .bounds(rightX + 22 + Math.max(78, (rightWidth - 8) / 2), 354, Math.max(78, rightWidth - Math.max(78, (rightWidth - 8) / 2) - 8), 20)
            .build()
      );
      this.fastDepositButton = (Button)this.addRenderableWidget(
         Button.builder(Component.literal("Fast: Off"), button -> this.toggleFastDeposit()).bounds(rightX + 14, 376, rightWidth, 20).build()
      );
      this.clickButtonButton = (Button)this.addRenderableWidget(
         Button.builder(Component.literal("Button: Left"), button -> this.toggleClickGuiButton()).bounds(rightX + 14, 376, rightWidth, 20).build()
      );
      this.repeatIndefinitelyButton = (Button)this.addRenderableWidget(
         Button.builder(Component.literal("Repeat: Count"), button -> this.toggleRepeatIndefinitely()).bounds(rightX + 14, 376, rightWidth, 20).build()
      );
      this.setNextButton = (Button)this.addRenderableWidget(
         Button.builder(Component.literal("Done"), button -> this.startConnection(this.defaultOutputKey())).bounds(rightX + 14, 472, 58, 20).build()
      );
      this.setTrueButton = (Button)this.addRenderableWidget(
         Button.builder(Component.literal("True"), button -> this.startConnection(this.firstBranchOutput())).bounds(rightX + 14, 472, 58, 20).build()
      );
      this.setFalseButton = (Button)this.addRenderableWidget(
         Button.builder(Component.literal("False"), button -> this.startConnection(this.secondBranchOutput())).bounds(rightX + 78, 472, 58, 20).build()
      );
      this.setToolLowButton = (Button)this.addRenderableWidget(
         Button.builder(Component.literal("Low"), button -> this.startConnection(this.specialOutputKey())).bounds(rightX + 82, 472, 58, 20).build()
      );
      this.setFailedButton = (Button)this.addRenderableWidget(
         Button.builder(Component.literal("Fail"), button -> this.startConnection(this.thirdBranchOutput())).bounds(rightX + 146, 472, 58, 20).build()
      );
      this.clearConnectionsButton = (Button)this.addRenderableWidget(
         Button.builder(Component.literal("Clear"), button -> this.clearConnections()).bounds(rightX + 14, 500, 82, 20).build()
      );
         this.deleteButton = (Button)this.addRenderableWidget(
             Button.builder(Component.literal("Delete"), button -> this.deleteSelected()).bounds(rightX + 104, 500, 82, 20).build()
         );
         this.refreshProperties();
      } finally {
         this.restoreActualSize(actualWidth, actualHeight);
      }
   }

   public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      int actualWidth = this.width;
      int actualHeight = this.height;
      double scale = this.layoutScale(actualWidth, actualHeight);
      mouseX = this.toLayoutMouse(mouseX, scale);
      mouseY = this.toLayoutMouse(mouseY, scale);
      context.pose().pushMatrix();
      context.pose().scale((float)scale, (float)scale);
      this.useLayoutSize(actualWidth, actualHeight);

      try {
         this.extractBackground(context, mouseX, mouseY, delta);
         this.drawLayout(context);
         this.highlightedConnection = this.draggedConnection == null && this.pendingConnectionSource == null && this.insideCanvas(mouseX, mouseY)
            ? this.connectionAt(mouseX, mouseY)
            : this.draggedConnection;
         context.enableScissor(this.canvasLeft(), this.canvasTop(), Math.max(this.canvasLeft() + 1, this.canvasRight()), this.height);
         this.drawConnections(context);
         this.drawNodes(context);
         this.drawSelectionBox(context);
         context.disableScissor();
         this.drawPaletteDragPreview(context);
         this.drawPropertyLabels(context);
         super.extractRenderState(context, mouseX, mouseY, delta);
         this.drawSelectedItemPreview(context);
         this.drawItemPicker(context, mouseX, mouseY);
          if (this.canDrawTopTitle()) {
             context.centeredText(this.font, this.title, this.width / 2, 14, -1);
          }
          if (!this.status.isEmpty()) {
             int statusWidth = Math.max(120, this.canvasRight() - this.canvasLeft() - 160);
             context.centeredText(this.font, Component.literal(this.fit(this.status, statusWidth)), this.width / 2, this.height - 18, this.statusColor);
          }

          if (this.pendingConnectionSource != null) {
             context.centeredText(this.font, Component.literal("Select a target component"), this.width / 2, 44, -3672);
          }

          this.drawContextMenu(context, mouseX, mouseY);
          this.drawTopActionsMenu(context, mouseX, mouseY);
          this.maybeAutoSave();
      } finally {
         this.restoreActualSize(actualWidth, actualHeight);
         context.pose().popMatrix();
      }
   }

   public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      context.fill(0, 0, this.width, this.height, -535818224);
   }

   public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
      int actualWidth = this.width;
      int actualHeight = this.height;
      double scale = this.layoutScale(actualWidth, actualHeight);
      click = this.layoutClick(click, scale);
      this.useLayoutSize(actualWidth, actualHeight);

      try {
         if (this.itemPickerOpen) {
            return this.handleItemPickerClick(click);
         } else {
            double mouseX = click.x();
            double mouseY = click.y();
         if (this.contextMenuOpen) {
            if (click.button() == 0) {
               return this.handleContextMenuClick((int)mouseX, (int)mouseY);
            }

            this.closeContextMenu();
         }

         if (this.topActionsMenuOpen) {
            if (click.button() == 0) {
               return this.handleTopActionsMenuClick((int)mouseX, (int)mouseY);
            }

            this.topActionsMenuOpen = false;
         }

           if (click.button() == 1) {
             if (this.insideCanvas((int)mouseX, (int)mouseY)) {
                this.rightClickGesture = true;
                this.rightClickDragged = false;
                this.rightClickStartMouseX = mouseX;
                this.rightClickStartMouseY = mouseY;
                return true;
             }

             return false;
          } else if (click.button() == 2) {
             if (this.insideCanvas((int)mouseX, (int)mouseY)) {
                this.beginPanning(mouseX, mouseY);
                return true;
             }

             return false;
          } else if (super.mouseClicked(click, doubled)) {
             return true;
          }

         if (click.button() != 0) {
            return false;
         } else {
            this.syncSelectedFromFields();
            String outputToClear = this.connectionClearOutputAt((int)mouseX, (int)mouseY);
            if (outputToClear != null) {
               for (String targetId : this.selectedNode.nextTargets(outputToClear)) {
                  this.selectedNode.removeConnectionRoute(outputToClear, targetId);
                  this.selectedNode.removeConnectionColor(outputToClear, targetId);
               }

               this.selectedNode.outputs.remove(outputToClear);
               this.status = "Cleared " + outputToClear + " link";
               this.statusColor = -11382;
               this.refreshProperties();
               return true;
            } else {
               String outputToConnect = this.connectionOutputAt((int)mouseX, (int)mouseY);
               if (outputToConnect != null) {
                  this.startConnection(outputToConnect);
                  return true;
               } else {
                  MacroBuilderScreen.PaletteHit paletteHit = this.paletteHitAt((int)mouseX, (int)mouseY);
                  if (paletteHit != null && paletteHit.group() != null) {
                     this.expandedGroups.put(paletteHit.group(), !this.expandedGroups.getOrDefault(paletteHit.group(), false));
                     this.paletteScroll = clamp(this.paletteScroll, 0, this.maxPaletteScroll());
                     return true;
                  } else {
                     String paletteType = paletteHit == null ? null : paletteHit.type();
                     if (paletteType != null) {
                        this.paletteDragType = paletteType;
                        this.dragMouseX = (int)mouseX;
                        this.dragMouseY = (int)mouseY;
                        return true;
                     } else {
                         MacroBuilderScreen.ConnectionHit connectionHit = this.pendingConnectionSource == null
                            ? this.connectionGripAt((int)mouseX, (int)mouseY, true)
                            : null;
                         MacroModel.Node clickedNode = this.nodeAt((int)mouseX, (int)mouseY);
                         if (this.pendingConnectionSource == null) {
                            if (connectionHit != null && clickedNode == null) {
                               this.highlightedConnection = connectionHit;
                               this.draggedConnection = connectionHit;
                               this.selectNode(connectionHit.source(), false);
                               this.connectionDragOffsetX = this.worldX(mouseX)
                                  - this.connectionRouteWorldX(connectionHit.source(), connectionHit.output(), connectionHit.target());
                               this.refreshProperties();
                               return true;
                           } else if (clickedNode != null) {
                               boolean additiveSelection = this.controlDown();
                               if (!additiveSelection && this.isNoteNode(clickedNode)) {
                                  this.selectNode(clickedNode, false);
                                  if (this.startNoteResize(clickedNode, (int)mouseX, (int)mouseY)) {
                                     this.highlightedConnection = null;
                                     this.refreshProperties();
                                     return true;
                                  }

                                  if (this.noteBodyAt(clickedNode, (int)mouseX, (int)mouseY)) {
                                     this.highlightedConnection = null;
                                     this.startNoteEditing(clickedNode);
                                     this.refreshProperties();
                                     return true;
                                  }
                               }

                               if (!this.isNoteNode(clickedNode)) {
                                  this.stopNoteEditing(true);
                               }

                               this.selectNode(clickedNode, additiveSelection);
                               this.highlightedConnection = null;
                               if (!additiveSelection || this.nodeSelected(clickedNode)) {
                                  this.draggedNode = clickedNode;
                                  this.dragOffsetX = this.worldX(mouseX) - clickedNode.x;
                                  this.dragOffsetY = this.worldY(mouseY) - clickedNode.y;
                               }
                               this.refreshProperties();
                               return true;
                            } else if (this.insideCanvas((int)mouseX, (int)mouseY)) {
                               this.stopNoteEditing(true);
                               this.selectNode(null, false);
                               this.highlightedConnection = null;
                               this.beginSelectionBox((int)mouseX, (int)mouseY);
                               this.refreshProperties();
                               return true;
                            } else {
                              return false;
                           }
                        } else {
                           if (clickedNode != null && clickedNode != this.pendingConnectionSource && !this.isNoteNode(clickedNode)) {
                              this.pendingConnectionSource.addTarget(this.pendingConnectionOutput, clickedNode.id);
                              this.selectNode(this.pendingConnectionSource, false);
                              this.status = "Connected " + this.pendingConnectionOutput + " to " + clickedNode.descriptor().label();
                              this.statusColor = -4200769;
                           } else if (this.isNoteNode(clickedNode)) {
                              this.status = "Notes do not run as components";
                              this.statusColor = -11382;
                           } else {
                              this.status = "Connection canceled";
                              this.statusColor = -11382;
                           }

                           this.pendingConnectionSource = null;
                           this.pendingConnectionOutput = null;
                           this.refreshProperties();
                           return true;
                        }
                     }
                  }
               }
            }
         }
         }
      } finally {
         this.restoreActualSize(actualWidth, actualHeight);
      }
   }

   public boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
      int actualWidth = this.width;
      int actualHeight = this.height;
      double scale = this.layoutScale(actualWidth, actualHeight);
      click = this.layoutClick(click, scale);
      offsetX /= scale;
      offsetY /= scale;
      this.useLayoutSize(actualWidth, actualHeight);

      try {
         double mouseX = click.x();
         double mouseY = click.y();
         if (this.resizingNote != null && click.button() == 0) {
            this.updateNoteResize((int)mouseX, (int)mouseY);
            return true;
         } else if (this.draggedConnection != null && click.button() == 0) {
            MacroBuilderScreen.ConnectionHit hit = this.draggedConnection;
            double bendWorldX = this.worldX(mouseX) - this.connectionDragOffsetX;
            hit.source().setConnectionRoute(hit.output(), hit.target().id, bendWorldX);
            this.highlightedConnection = hit;
            return true;
         } else if (this.draggedNode != null && click.button() == 0) {
            int newX = (int)Math.round(this.worldX(mouseX) - this.dragOffsetX);
            int newY = (int)Math.round(this.worldY(mouseY) - this.dragOffsetY);
            int deltaX = newX - this.draggedNode.x;
            int deltaY = newY - this.draggedNode.y;
            if ((deltaX != 0 || deltaY != 0) && this.nodeSelected(this.draggedNode)) {
               for (MacroModel.Node node : this.movableSelection()) {
                  if (node != this.draggedNode) {
                     node.x += deltaX;
                     node.y += deltaY;
                  }
               }
            }

            this.draggedNode.x = newX;
            this.draggedNode.y = newY;
            return true;
          } else if (this.selectionBoxActive && click.button() == 0) {
            this.updateSelectionBox((int)mouseX, (int)mouseY);
            return true;
          } else if (this.rightClickGesture && click.button() == 1) {
            double deltaX = mouseX - this.rightClickStartMouseX;
            double deltaY = mouseY - this.rightClickStartMouseY;
            if (!this.panning && deltaX * deltaX + deltaY * deltaY > CONTEXT_CLICK_DRAG_THRESHOLD * CONTEXT_CLICK_DRAG_THRESHOLD) {
               this.rightClickDragged = true;
               this.beginPanning(this.rightClickStartMouseX, this.rightClickStartMouseY);
            }

            if (this.panning) {
               this.updatePanning(mouseX, mouseY);
            }

            return true;
          } else if (this.panning && (click.button() == 1 || click.button() == 2)) {
            this.updatePanning(mouseX, mouseY);
            return true;
          } else if (this.paletteDragType != null) {
            this.dragMouseX = (int)mouseX;
            this.dragMouseY = (int)mouseY;
            return true;
         } else {
            return super.mouseDragged(click, offsetX, offsetY);
         }
      } finally {
         this.restoreActualSize(actualWidth, actualHeight);
      }
   }

   public boolean mouseReleased(MouseButtonEvent click) {
      int actualWidth = this.width;
      int actualHeight = this.height;
      double scale = this.layoutScale(actualWidth, actualHeight);
      click = this.layoutClick(click, scale);
      this.useLayoutSize(actualWidth, actualHeight);

      try {
         boolean handled = super.mouseReleased(click);
         double mouseX = click.x();
         double mouseY = click.y();
         int button = click.button();
         if (button == 0 && this.resizingNote != null) {
            this.finishNoteResize();
            return true;
         }

         if (this.paletteDragType != null && button == 0) {
            if (this.insideCanvas((int)mouseX, (int)mouseY)) {
                MacroModel.Node node = this.model
                   .addNode(
                      this.paletteDragType,
                      (int)Math.round(this.worldX(mouseX) - this.nodeWorldWidth(this.paletteDragType) / 2.0),
                      (int)Math.round(this.worldY(mouseY) - this.nodeWorldHeight(this.paletteDragType) / 2.0)
                   );
               this.selectNode(node, false);
               this.status = "Added " + node.descriptor().label();
               this.statusColor = -4200769;
               if (this.isNoteNode(node)) {
                  node.noteWidth = NOTE_WIDTH;
                  node.noteHeight = NOTE_HEIGHT;
                  this.startNoteEditing(node);
               }

               this.refreshProperties();
            }

            this.paletteDragType = null;
            return true;
          } else {
             if (button == 0 && this.selectionBoxActive) {
                this.completeSelectionBox();
                return true;
             }

             if (button == 1 && this.rightClickGesture) {
                boolean openMenu = !this.rightClickDragged && !this.panning && this.insideCanvas((int)mouseX, (int)mouseY);
                this.rightClickGesture = false;
                this.rightClickDragged = false;
                this.panning = false;
                if (openMenu) {
                   return this.openContextMenu((int)mouseX, (int)mouseY);
                }

                return true;
             }

             this.draggedNode = null;
             this.draggedConnection = null;
             if (button == 2) {
                this.panning = false;
             }
             return handled;
          }
      } finally {
         this.restoreActualSize(actualWidth, actualHeight);
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      int actualWidth = this.width;
      int actualHeight = this.height;
      double scale = this.layoutScale(actualWidth, actualHeight);
      mouseX /= scale;
      mouseY /= scale;
      this.useLayoutSize(actualWidth, actualHeight);

      try {
         if (this.itemPickerOpen) {
            int maxScroll = this.maxItemPickerScroll();
            this.itemPickerScroll = clamp(this.itemPickerScroll - (int)Math.round(verticalAmount), 0, maxScroll);
            return true;
         } else if (mouseX > 0.0 && mouseX < this.paletteWidth() && mouseY > this.paletteListTop()) {
            this.paletteScroll = clamp(this.paletteScroll - (int)Math.round(verticalAmount * 34.0), 0, this.maxPaletteScroll());
            return true;
         } else if (!this.insideCanvas((int)mouseX, (int)mouseY)) {
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
         } else {
            double beforeX = this.worldX(mouseX);
            double beforeY = this.worldY(mouseY);
            this.zoom = clampDouble(this.zoom + verticalAmount * 0.1, 0.2, 2.0);
            this.panX = (mouseX - this.canvasLeft()) / this.zoom - beforeX;
            this.panY = (mouseY - this.canvasTop()) / this.zoom - beforeY;
            return true;
         }
      } finally {
         this.restoreActualSize(actualWidth, actualHeight);
      }
   }

   public boolean keyPressed(KeyEvent input) {
      if (this.itemPickerOpen) {
         if (input.key() == 256) {
            this.itemPickerOpen = false;
            return true;
         } else if (input.key() == 259 && this.itemSearchFocused && !this.itemSearchText.isEmpty()) {
            this.itemSearchText = this.itemSearchText.substring(0, this.itemSearchText.length() - 1);
            this.itemPickerScroll = 0;
            return true;
         } else if (input.key() == 261 && this.itemSearchFocused && !this.itemSearchText.isEmpty()) {
            this.itemSearchText = "";
            this.itemPickerScroll = 0;
            return true;
         } else {
            return true;
         }
      } else if (this.topActionsMenuOpen && input.key() == 256) {
         this.topActionsMenuOpen = false;
         return true;
      } else if (this.contextMenuOpen && input.key() == 256) {
         this.closeContextMenu();
         return true;
      } else if (this.editingNote != null && this.handleNoteKey(input.key(), input.hasControlDownWithQuirk())) {
         return true;
      } else if (input.key() == 83 && input.hasControlDownWithQuirk()) {
         this.save();
         return true;
      } else if (input.key() == 261 && !this.fieldFocused()) {
         if (this.highlightedConnection != null) {
            this.deleteConnection(this.highlightedConnection);
         } else {
            this.deleteSelected();
         }

         return true;
      } else if (input.key() == 67 && input.hasControlDownWithQuirk() && !this.fieldFocused()) {
         this.copySelectedComponent();
         return true;
      } else if (input.key() == 86 && input.hasControlDownWithQuirk() && !this.fieldFocused()) {
         this.pasteCopiedComponent();
         return true;
      } else {
         return super.keyPressed(input);
      }
   }

   public boolean charTyped(CharacterEvent input) {
      if (this.itemPickerOpen && this.itemSearchFocused && input.isAllowedChatCharacter()) {
         this.itemSearchText = this.itemSearchText + input.codepointAsString();
         this.itemPickerScroll = 0;
         return true;
      } else if (this.editingNote != null && input.isAllowedChatCharacter()) {
         return this.insertNoteText(input.codepointAsString());
      } else {
         return super.charTyped(input);
      }
   }

   public void onClose() {
      this.syncSelectedFromFields();
      if (this.hasUnsavedChanges()) {
         this.minecraft.setScreenAndShow(new SavePromptScreen(this));
      } else {
         super.onClose();
      }
   }

   public boolean isPauseScreen() {
      return false;
   }

   boolean save() {
      this.syncSelectedFromFields();

      try {
         this.runner.save(this.model);
         this.savedSnapshot = this.model.toJson();
         this.status = "Saved";
         this.statusColor = -4200769;
         return true;
      } catch (RuntimeException | IOException var2) {
         MacroBuilderClient.LOGGER.error("Failed to save macro", var2);
         this.status = "Save failed: " + var2.getMessage();
         this.statusColor = -30070;
         return false;
      }
   }

   private void maybeAutoSave() {
      long now = System.currentTimeMillis();
      if (now - this.lastAutoSaveTimeMs < AUTO_SAVE_INTERVAL_MS) {
         return;
      }

      this.lastAutoSaveTimeMs = now;
      this.syncSelectedFromFields();
      String currentSnapshot = this.model.toJson();
      if (currentSnapshot.equals(this.savedSnapshot)) {
         return;
      }

      try {
         this.runner.save(this.model);
         this.savedSnapshot = currentSnapshot;
         this.status = "Auto-saved";
         this.statusColor = -4200769;
      } catch (RuntimeException | IOException error) {
         MacroBuilderClient.LOGGER.error("Failed to auto-save macro", error);
         this.status = "Auto-save failed";
         this.statusColor = -30070;
      }
   }

   void leaveToMacroList() {
      this.syncSelectedFromFields();
      if (this.hasUnsavedChanges()) {
         this.minecraft.setScreenAndShow(new SavePromptScreen(this));
      } else {
         this.minecraft.setScreenAndShow(new MacroListScreen(this.runner));
      }
   }

   void saveAndLeaveToMacroList() {
      if (this.save()) {
         this.minecraft.setScreenAndShow(new MacroListScreen(this.runner));
      } else {
         this.minecraft.setScreenAndShow(this);
      }
   }

   void discardAndLeaveToMacroList() {
      this.runner.reload(this.minecraft);
      this.minecraft.setScreenAndShow(new MacroListScreen(this.runner));
   }

   void cancelSavePrompt() {
      this.minecraft.setScreenAndShow(this);
   }

   private boolean hasUnsavedChanges() {
      return !this.model.toJson().equals(this.savedSnapshot);
   }

   private void syncSelectedFromFields() {
      this.commitEditingNote();
      this.model.setName(this.nameField == null ? this.model.name() : this.nameField.getValue());
      if (this.delayField != null) {
         this.model.setStepDelayMs(this.parseDelayMs(this.delayField.getValue()));
      }

      if (this.selectedNode != null && !this.isNoteNode(this.selectedNode) && this.primaryField != null && this.secondaryField != null && this.excludeSlotsField != null && this.nodeDelayField != null) {
         this.selectedNode.delayMs = this.parseNodeDelayMs(this.nodeDelayField.getValue());
         MacroModel.Descriptor descriptor = this.selectedNode.descriptor();
         if (!descriptor.primaryLabel().isBlank()) {
            this.selectedNode.value = this.primaryField.getValue();
         }

         if (!descriptor.secondaryLabel().isBlank() && !this.isClickGuiItemNode(this.selectedNode)) {
            this.selectedNode.value2 = this.secondaryField.getValue();
         }

         if (this.hasExcludeSlotField(this.selectedNode)) {
            if (this.isDropItemsNode(this.selectedNode)) {
               this.selectedNode.value3 = this.excludeSlotsField.getValue();
            } else {
               this.selectedNode.value5 = this.excludeSlotsField.getValue();
            }
         }
      }
   }

   private void refreshProperties() {
      if (this.primaryField != null && this.secondaryField != null && this.excludeSlotsField != null && this.nodeDelayField != null) {
         boolean hasSelection = this.selectedNode != null;
         MacroModel.Descriptor descriptor = hasSelection ? this.selectedNode.descriptor() : null;
         boolean noteNode = hasSelection && this.isNoteNode(this.selectedNode);
         boolean hasPrimary = hasSelection && !noteNode && !descriptor.primaryLabel().isBlank();
         boolean hasSecondary = hasSelection && !noteNode && !descriptor.secondaryLabel().isBlank();
         boolean clickGuiItem = hasSelection && this.isClickGuiItemNode(this.selectedNode);
         hasSecondary = hasSecondary && !clickGuiItem;
         boolean branch = hasSelection && descriptor.outputs().contains("true") && descriptor.outputs().contains("false");
         boolean randomOutput3 = hasSelection
            && descriptor.outputs().contains("one")
            && descriptor.outputs().contains("two")
            && descriptor.outputs().contains("three");
         boolean connectable = hasSelection && !descriptor.outputs().isEmpty();
         boolean hasFailed = hasSelection && descriptor.outputs().contains("failed");
         boolean hasToolLow = hasSelection && descriptor.outputs().contains("tool_low");
         boolean hasRepeat = hasSelection && descriptor.outputs().contains("repeat");
         boolean depositWithdraw = hasSelection && this.isDepositWithdrawNode(this.selectedNode);
         boolean depositNode = hasSelection && this.isDepositNode(this.selectedNode);
         boolean dropItemsNode = hasSelection && this.isDropItemsNode(this.selectedNode);
         boolean hasExcludeSlots = hasSelection && this.hasExcludeSlotField(this.selectedNode);
         boolean itemTagClick = hasSelection && this.isItemOrSlotHasTagNode(this.selectedNode);
         boolean shiftClickNode = depositWithdraw || clickGuiItem;
         boolean repeatNode = hasSelection && this.isRepeatNode(this.selectedNode);
         String normalOutput = hasSelection ? this.primaryOutputKey(descriptor) : "completed";
         String primaryText = hasPrimary && this.selectedNode.value != null ? this.selectedNode.value : "";
         String secondaryText = hasSecondary && this.selectedNode.value2 != null ? this.selectedNode.value2 : "";
         String excludeSlotsText = hasExcludeSlots ? this.excludeSlotText(this.selectedNode) : "";
         this.primaryField.visible = hasPrimary;
         this.primaryField.active = hasPrimary;
         this.primaryField.setValue(primaryText);
         this.primaryField.setSuggestion(hasPrimary && primaryText.isBlank() ? this.fieldSuggestion(descriptor, false) : "");
         this.secondaryField.visible = hasSecondary;
         this.secondaryField.active = hasSecondary;
         this.secondaryField.setValue(secondaryText);
         this.secondaryField.setSuggestion(hasSecondary && secondaryText.isBlank() ? this.fieldSuggestion(descriptor, true) : "");
         this.excludeSlotsField.visible = hasExcludeSlots;
         this.excludeSlotsField.active = hasExcludeSlots;
         this.excludeSlotsField.setValue(excludeSlotsText);
         this.excludeSlotsField.setSuggestion(hasExcludeSlots && excludeSlotsText.isBlank() ? "0-8, 35" : "");
         this.nodeDelayField.visible = hasSelection && !noteNode;
         this.nodeDelayField.active = this.nodeDelayField.visible;
         this.nodeDelayField.setValue(hasSelection && this.selectedNode.delayMs >= 0 ? Integer.toString(this.selectedNode.delayMs) : "");
         this.nodeDelayField.setSuggestion("");
         boolean primaryItem = hasPrimary && this.isItemField(descriptor.primaryLabel());
         boolean secondaryItem = hasSecondary && this.isItemField(descriptor.secondaryLabel());
         this.primaryItemButton.visible = primaryItem;
         this.primaryItemButton.active = primaryItem;
         this.secondaryItemButton.visible = secondaryItem;
         this.secondaryItemButton.active = secondaryItem;
         this.modeButton.visible = depositWithdraw;
         this.modeButton.active = this.modeButton.visible;
         this.modeButton.setMessage(Component.literal(this.modeButton.visible ? "Mode: " + this.selectedNode.value : "Toggle Mode"));
         this.shiftClickButton.visible = shiftClickNode || itemTagClick;
         this.shiftClickButton.active = this.shiftClickButton.visible;
         this.shiftClickButton.setMessage(
            Component.literal(itemTagClick ? (this.clickMatchedTagEnabled(this.selectedNode) ? "Click: On" : "Click: Off") : (this.shiftClickEnabled(this.selectedNode) ? "Shift: On" : "Shift: Off"))
         );
         this.fastDepositButton.visible = depositNode || dropItemsNode;
         this.fastDepositButton.active = this.fastDepositButton.visible;
         this.fastDepositButton.setMessage(
            Component.literal(dropItemsNode ? (this.dropAllEnabled(this.selectedNode) ? "All: On" : "All: Off") : (this.fastDepositEnabled(this.selectedNode) ? "Fast: On" : "Fast: Off"))
         );
         this.clickButtonButton.visible = clickGuiItem;
         this.clickButtonButton.active = clickGuiItem;
         this.clickButtonButton.setMessage(Component.literal("Button: " + this.clickGuiButtonText(this.selectedNode)));
         this.enabledButton.visible = hasSelection && !noteNode;
         this.enabledButton.active = this.enabledButton.visible && !"official:start".equals(this.selectedNode.type);
         this.enabledButton.setMessage(Component.literal(hasSelection && this.selectedNode.enabled ? "Enabled: On" : "Enabled: Off"));
         this.repeatIndefinitelyButton.visible = repeatNode;
         this.repeatIndefinitelyButton.active = repeatNode;
         this.repeatIndefinitelyButton.setMessage(Component.literal(repeatNode && this.repeatIndefinitelyEnabled(this.selectedNode) ? "Repeat: Forever" : "Repeat: Count"));
         this.setNextButton.visible = connectable && !branch && !randomOutput3;
         this.setNextButton.active = this.setNextButton.visible;
         this.setNextButton.setMessage(Component.literal(this.connectionButtonLabel(normalOutput)));
         this.setTrueButton.visible = branch || randomOutput3;
         this.setTrueButton.active = branch;
         this.setTrueButton.setMessage(Component.literal(randomOutput3 ? "Set One" : "Set True"));
         this.setTrueButton.active = this.setTrueButton.visible;
         this.setFalseButton.visible = branch || randomOutput3;
         this.setFalseButton.active = branch;
         this.setFalseButton.setMessage(Component.literal(randomOutput3 ? "Set Two" : "Set False"));
         this.setFalseButton.active = this.setFalseButton.visible;
         this.setToolLowButton.visible = connectable && (hasToolLow || hasRepeat);
         this.setToolLowButton.active = this.setToolLowButton.visible;
          this.setToolLowButton.setMessage(Component.literal(hasRepeat ? "Repeat" : "Low"));
         this.setFailedButton.visible = randomOutput3 || connectable && hasFailed;
         this.setFailedButton.active = this.setFailedButton.visible;
          this.setFailedButton.setMessage(Component.literal(randomOutput3 ? "Three" : "Fail"));
          this.clearConnectionsButton.visible = hasSelection && this.model.hasAnyConnection(this.selectedNode);
         this.clearConnectionsButton.active = this.clearConnectionsButton.visible;
          this.clearConnectionsButton.setMessage(Component.literal("Clear"));
         this.deleteButton.visible = hasSelection && !"official:start".equals(this.selectedNode.type);
         this.deleteButton.active = this.deleteButton.visible;
         if (this.copyComponentButton != null) {
            this.copyComponentButton.active = hasSelection && !"official:start".equals(this.selectedNode.type);
         }

         if (this.pasteComponentButton != null) {
            this.pasteComponentButton.active = !this.copiedComponents.isEmpty();
         }

         if (this.setAllDelayButton != null) {
            this.setAllDelayButton.active = true;
         }

         if (this.randomDelayButton != null) {
            this.randomDelayButton.active = true;
            this.randomDelayButton.setMessage(Component.literal(this.model.randomDelayEnabled() ? "Rand On" : "Rand"));
         }

         if (this.autoSettingsButton != null) {
            this.autoSettingsButton.active = true;
            this.autoSettingsButton.setMessage(Component.literal(this.autoSettingsButtonText()));
         }
      }
   }

   private void selectNode(MacroModel.Node node, boolean additive) {
      if (node == null) {
         this.selectedNode = null;
         this.selectedNodes.clear();
         return;
      }

      if (additive) {
         if (this.selectedNodes.contains(node)) {
            this.selectedNodes.remove(node);
            if (this.selectedNode == node) {
               this.selectedNode = this.selectedNodes.isEmpty() ? null : this.selectedNodes.get(this.selectedNodes.size() - 1);
            }
         } else {
            this.selectedNodes.add(node);
            this.selectedNode = node;
         }
      } else {
         this.selectedNodes.clear();
         this.selectedNodes.add(node);
         this.selectedNode = node;
      }
   }

   private boolean nodeSelected(MacroModel.Node node) {
      return node != null && this.selectedNodes.contains(node);
   }

   private List<MacroModel.Node> movableSelection() {
      this.selectedNodes.removeIf(node -> !this.model.nodes().contains(node));
      return this.selectedNodes.isEmpty() ? (this.selectedNode == null ? List.of() : List.of(this.selectedNode)) : List.copyOf(this.selectedNodes);
   }

   private void beginPanning(double mouseX, double mouseY) {
      this.panning = true;
      this.panStartX = this.panX;
      this.panStartY = this.panY;
      this.panStartMouseX = mouseX;
      this.panStartMouseY = mouseY;
   }

   private void updatePanning(double mouseX, double mouseY) {
      this.panX = this.panStartX + (mouseX - this.panStartMouseX) / this.zoom;
      this.panY = this.panStartY + (mouseY - this.panStartMouseY) / this.zoom;
   }

   private void beginSelectionBox(int mouseX, int mouseY) {
      this.selectionBoxActive = true;
      this.selectionBoxStartX = mouseX;
      this.selectionBoxStartY = mouseY;
      this.selectionBoxEndX = mouseX;
      this.selectionBoxEndY = mouseY;
   }

   private void updateSelectionBox(int mouseX, int mouseY) {
      this.selectionBoxEndX = mouseX;
      this.selectionBoxEndY = mouseY;
      this.selectedNodes.clear();

      for (MacroModel.Node node : this.model.nodes()) {
         if (this.selectionBoxIntersects(node)) {
            this.selectedNodes.add(node);
         }
      }

      this.selectedNode = this.selectedNodes.isEmpty() ? null : this.selectedNodes.get(this.selectedNodes.size() - 1);
      this.highlightedConnection = null;
      this.refreshProperties();
   }

   private void completeSelectionBox() {
      this.selectionBoxActive = false;
      this.refreshProperties();
   }

   private boolean selectionBoxIntersects(MacroModel.Node node) {
      int left = Math.min(this.selectionBoxStartX, this.selectionBoxEndX);
      int right = Math.max(this.selectionBoxStartX, this.selectionBoxEndX);
      int top = Math.min(this.selectionBoxStartY, this.selectionBoxEndY);
      int bottom = Math.max(this.selectionBoxStartY, this.selectionBoxEndY);
      int nodeLeft = this.screenX(node.x);
      int nodeTop = this.screenY(node.y);
      int nodeRight = nodeLeft + this.nodeScreenWidth(node);
      int nodeBottom = nodeTop + this.nodeScreenHeight(node);
      return nodeRight >= left && nodeLeft <= right && nodeBottom >= top && nodeTop <= bottom;
   }

   private void toggleDepositMode() {
      if (this.isDepositWithdrawNode(this.selectedNode)) {
         this.selectedNode.value = "specific".equalsIgnoreCase(this.selectedNode.value) ? "all" : "specific";
         this.refreshProperties();
      }
   }

   private void toggleDepositWithdrawShiftClick() {
      if (this.isItemOrSlotHasTagNode(this.selectedNode)) {
         boolean next = !this.clickMatchedTagEnabled(this.selectedNode);
         this.selectedNode.value3 = Boolean.toString(next);
         this.refreshProperties();
      } else if (this.isShiftClickNode(this.selectedNode)) {
         boolean next = !this.shiftClickEnabled(this.selectedNode);
         if (this.isClickGuiItemNode(this.selectedNode)) {
            this.selectedNode.value2 = Boolean.toString(next);
         } else {
            this.selectedNode.value3 = Boolean.toString(next);
         }

         this.refreshProperties();
      }
   }

   private void toggleFastDeposit() {
      if (this.isDepositNode(this.selectedNode) || this.isDropItemsNode(this.selectedNode)) {
         boolean next = !this.fastDepositEnabled(this.selectedNode);
         this.selectedNode.value4 = Boolean.toString(next);
         this.refreshProperties();
      }
   }

   private void toggleClickGuiButton() {
      if (this.isClickGuiItemNode(this.selectedNode)) {
         this.selectedNode.value3 = "right".equalsIgnoreCase(this.selectedNode.value3 == null ? "" : this.selectedNode.value3.trim()) ? "left" : "right";
         this.refreshProperties();
      }
   }

   private void toggleSelectedEnabled() {
      List<MacroModel.Node> nodes = this.movableSelection();
      if (nodes.isEmpty()) {
         return;
      }

      boolean enable = nodes.stream().anyMatch(node -> !node.enabled);
      for (MacroModel.Node node : nodes) {
         if (!"official:start".equals(node.type) && !this.isNoteNode(node)) {
            node.enabled = enable;
         }
      }

      this.status = enable ? "Enabled selected components" : "Disabled selected components";
      this.statusColor = -4200769;
      this.refreshProperties();
   }

   private void toggleRepeatIndefinitely() {
      if (!this.isRepeatNode(this.selectedNode)) {
         return;
      }

      boolean enableForever = !this.repeatIndefinitelyEnabled(this.selectedNode);
      this.selectedNode.value = enableForever ? "forever" : "3";
      this.status = enableForever ? "Repeat forever" : "Repeat count";
      this.statusColor = -4200769;
      this.refreshProperties();
   }

   private void openRandomDelaySettings() {
      this.syncSelectedFromFields();
      this.minecraft.setScreenAndShow(new RandomDelaySettingsScreen(this, this.model));
   }

   private void openAutoSettings() {
      this.syncSelectedFromFields();
      this.minecraft.setScreenAndShow(new MacroAutoSettingsScreen(this, this.model));
   }

   void refreshAfterRandomDelaySettings() {
      this.status = this.model.randomDelayEnabled()
         ? "Random delay " + this.model.randomDelayMinMs() + "-" + this.model.randomDelayMaxMs() + " ms"
         : "Random delay off";
      this.statusColor = -4200769;
      this.refreshProperties();
   }

   void refreshAfterAutoSettings() {
      this.status = this.autoSettingsButtonText();
      this.statusColor = -4200769;
      this.refreshProperties();
   }

   private boolean isDepositWithdrawNode(MacroModel.Node node) {
      return node != null && ("official:inventory.chestDepositItems".equals(node.type) || "official:inventory.chestWithdrawItems".equals(node.type));
   }

   private boolean isDepositNode(MacroModel.Node node) {
      return node != null && "official:inventory.chestDepositItems".equals(node.type);
   }

   private boolean isDropItemsNode(MacroModel.Node node) {
      return node != null && "official:inventory.dropItems".equals(node.type);
   }

   private boolean hasExcludeSlotField(MacroModel.Node node) {
      return this.isDropItemsNode(node) || this.isDepositNode(node);
   }

   private String excludeSlotText(MacroModel.Node node) {
      if (node == null) {
         return "";
      } else {
         return this.isDropItemsNode(node) ? (node.value3 == null ? "" : node.value3) : (node.value5 == null ? "" : node.value5);
      }
   }

   private boolean isClickGuiItemNode(MacroModel.Node node) {
      return node != null && "official:inventory.clickOpenContainerSlot".equals(node.type);
   }

   private boolean isItemOrSlotHasTagNode(MacroModel.Node node) {
      return node != null && "builder:inventory.itemOrSlotHasTag".equals(node.type);
   }

   private boolean isRepeatNode(MacroModel.Node node) {
      return node != null && ("builder:misc.repeatMacro".equals(node.type) || "builder:misc.repeatSection".equals(node.type));
   }

   private boolean repeatIndefinitelyEnabled(MacroModel.Node node) {
      String value = node == null || node.value == null ? "" : node.value.trim().toLowerCase();
      return value.equals("forever") || value.equals("infinite") || value.equals("indefinite") || value.equals("always") || value.equals("-1");
   }

   private boolean isShiftClickNode(MacroModel.Node node) {
      return this.isClickGuiItemNode(node) || this.isDepositWithdrawNode(node);
   }

   private boolean clickMatchedTagEnabled(MacroModel.Node node) {
      return Boolean.parseBoolean(node == null || node.value3 == null ? "" : node.value3.trim());
   }

   private boolean fastDepositEnabled(MacroModel.Node node) {
      return Boolean.parseBoolean(node == null || node.value4 == null ? "" : node.value4.trim());
   }

   private boolean dropAllEnabled(MacroModel.Node node) {
      return Boolean.parseBoolean(node == null || node.value4 == null ? "" : node.value4.trim());
   }

   private boolean shiftClickEnabled(MacroModel.Node node) {
      if (this.isClickGuiItemNode(node)) {
         return Boolean.parseBoolean(node.value2 == null ? "" : node.value2.trim());
      } else {
         return node == null || !"false".equalsIgnoreCase(node.value3 == null ? "" : node.value3.trim());
      }
   }

   private String clickGuiButtonText(MacroModel.Node node) {
      String value = node == null || node.value3 == null ? "" : node.value3.trim();
      return "right".equalsIgnoreCase(value) || "1".equals(value) || "use".equalsIgnoreCase(value) ? "Right" : "Left";
   }

   private String autoSettingsButtonText() {
      if (this.model.alwaysOn()) {
         return "Auto: Always";
      } else {
         return "Auto: " + this.autoModeLabel(this.model.autoStartMode());
      }
   }

   private String autoModeLabel(String mode) {
      return switch (mode == null ? "" : mode) {
         case "chat" -> "Chat";
         case "player" -> "Player";
         case "inventory_full" -> "Full Inv";
         case "kicked" -> "Kicked";
         case "always" -> "Always";
         default -> "Off";
      };
   }

   private void startConnection(String outputKey) {
      this.syncSelectedFromFields();
      if (this.selectedNode != null) {
         this.pendingConnectionSource = this.selectedNode;
         this.pendingConnectionOutput = outputKey;
         this.status = "Choose target for " + outputKey;
         this.statusColor = -3672;
      }
   }

   private String defaultOutputKey() {
      return this.selectedNode == null ? "completed" : this.primaryOutputKey(this.selectedNode.descriptor());
   }

   private String firstBranchOutput() {
      return this.selectedNode != null && this.selectedNode.descriptor().outputs().contains("one") ? "one" : "true";
   }

   private String secondBranchOutput() {
      return this.selectedNode != null && this.selectedNode.descriptor().outputs().contains("two") ? "two" : "false";
   }

   private String thirdBranchOutput() {
      return this.selectedNode != null && this.selectedNode.descriptor().outputs().contains("three") ? "three" : "failed";
   }

   private String specialOutputKey() {
      return this.selectedNode != null && this.selectedNode.descriptor().outputs().contains("repeat") ? "repeat" : "tool_low";
   }

   private String primaryOutputKey(MacroModel.Descriptor descriptor) {
      if (descriptor.outputs().isEmpty()) {
         return "completed";
      } else if (descriptor.outputs().contains("started")) {
         return "started";
      } else {
         return descriptor.outputs().contains("completed") ? "completed" : descriptor.outputs().get(0);
      }
   }

   private String connectionButtonLabel(String output) {
      return switch (output) {
         case "started" -> "Start";
         case "triggered" -> "Trig";
         case "completed" -> "Done";
         default -> this.fit(output, 42);
      };
   }

   private void clearConnections() {
      if (this.selectedNode != null) {
         if (this.model.clearConnections(this.selectedNode)) {
            this.highlightedConnection = null;
            this.draggedConnection = null;
            this.status = "Cleared links";
            this.statusColor = -11382;
            this.refreshProperties();
         }
      }
   }

   private boolean openContextMenu(int mouseX, int mouseY) {
      if (!this.insideCanvas(mouseX, mouseY)) {
         return false;
      } else {
         this.syncSelectedFromFields();
         MacroModel.Node node = this.nodeAt(mouseX, mouseY);
         MacroBuilderScreen.ConnectionHit connection = node == null ? this.connectionAt(mouseX, mouseY) : null;
         if (node != null) {
            if (this.nodeSelected(node)) {
               this.selectedNode = node;
            } else {
               this.selectNode(node, false);
            }
         } else if (connection != null) {
            this.highlightedConnection = connection;
            this.selectNode(connection.source(), false);
         }

         this.contextMenuNode = node;
         this.contextMenuConnection = connection;
         this.contextMenuWorldX = this.worldX(mouseX);
         this.contextMenuWorldY = this.worldY(mouseY);
         this.contextMenuX = clamp(mouseX, 2, Math.max(2, this.width - CONTEXT_MENU_WIDTH - 2));
         this.contextMenuY = clamp(mouseY, 38, Math.max(38, this.height - this.contextMenuHeight() - 2));
         this.contextMenuOpen = true;
         this.refreshProperties();
         return true;
      }
   }

   private void closeContextMenu() {
      this.contextMenuOpen = false;
      this.contextMenuNode = null;
      this.contextMenuConnection = null;
   }

   private int contextMenuHeight() {
      return CONTEXT_MENU_ROW_HEIGHT * this.contextMenuLabels().length + 4;
   }

   private boolean handleContextMenuClick(int mouseX, int mouseY) {
      int left = this.contextMenuX;
      int top = this.contextMenuY;
      int right = left + CONTEXT_MENU_WIDTH;
      int bottom = top + this.contextMenuHeight();
      if (mouseX < left || mouseX > right || mouseY < top || mouseY > bottom) {
         this.closeContextMenu();
         return true;
      }

      String[] labels = this.contextMenuLabels();
      int row = (mouseY - top - 2) / CONTEXT_MENU_ROW_HEIGHT;
      if (row < 0 || row >= labels.length || !this.contextMenuActionEnabled(row)) {
         return true;
      }

      MacroModel.Node node = this.contextMenuNode;
      MacroBuilderScreen.ConnectionHit connection = this.contextMenuConnection;
      int pasteX = (int)Math.round(this.contextMenuWorldX - NODE_WIDTH / 2.0);
      int pasteY = (int)Math.round(this.contextMenuWorldY - NODE_HEIGHT / 2.0);
      this.closeContextMenu();
      if (connection != null) {
         this.handleLinkContextAction(connection, row);
         return true;
      }

      switch (row) {
         case 0:
            this.addNoteAt((int)Math.round(this.contextMenuWorldX - NOTE_WIDTH / 2.0), (int)Math.round(this.contextMenuWorldY - NOTE_HEIGHT / 2.0));
            return true;
         case 1:
            if (node != null && !this.nodeSelected(node)) {
               this.selectNode(node, false);
            }

             this.copySelectedComponent();
             return true;
          case 2:
             if (node != null) {
                this.duplicateSelectedComponent(node);
             }

             return true;
          case 3:
             this.pasteCopiedComponentAt(pasteX, pasteY);
             return true;
          case 4:
             if (node != null) {
                this.selectNode(node, false);
             }

             this.deleteSelected();
             return true;
          case 5:
             this.selectNode(node, false);
             this.clearConnections();
             return true;
          case 6:
             if (node != null && !this.nodeSelected(node)) {
                this.selectNode(node, false);
             }

             this.toggleSelectedEnabled();
             return true;
          default:
             return true;
       }
   }

   private boolean contextMenuActionEnabled(int row) {
      if (this.contextMenuConnection != null) {
         return row >= 0 && row < this.contextMenuLabels().length;
      }

      return switch (row) {
         case 0 -> true;
         case 1 -> this.contextMenuNode != null && !"official:start".equals(this.contextMenuNode.type);
         case 2 -> this.contextMenuNode != null && !"official:start".equals(this.contextMenuNode.type);
         case 3 -> !this.copiedComponents.isEmpty();
         case 4 -> this.contextMenuNode != null && !"official:start".equals(this.contextMenuNode.type);
         case 5 -> this.contextMenuNode != null && this.model.hasAnyConnection(this.contextMenuNode);
         case 6 -> this.contextMenuNode != null && !"official:start".equals(this.contextMenuNode.type) && !this.isNoteNode(this.contextMenuNode);
         default -> false;
      };
   }

   private String[] contextMenuLabels() {
      return this.contextMenuConnection == null
         ? new String[]{"New Note", "Copy", "Duplicate", "Paste Here", "Delete", "Clear Links", "Toggle Enabled"}
         : new String[]{"Delete Link", "Color Blue", "Color Green", "Color Yellow", "Color Pink", "Default Color"};
   }

   private void handleLinkContextAction(MacroBuilderScreen.ConnectionHit connection, int row) {
      switch (row) {
         case 0 -> this.deleteConnection(connection);
         case 1 -> this.setConnectionColor(connection, "#4aa3ff", "Link color blue");
         case 2 -> this.setConnectionColor(connection, "#46d66f", "Link color green");
         case 3 -> this.setConnectionColor(connection, "#f4c94d", "Link color yellow");
         case 4 -> this.setConnectionColor(connection, "#ff5aa5", "Link color pink");
         case 5 -> this.setConnectionColor(connection, "", "Link color default");
         default -> {
         }
      }
   }

   private void deleteConnection(MacroBuilderScreen.ConnectionHit connection) {
      if (connection != null && this.model.removeConnection(connection.source(), connection.output(), connection.target().id)) {
         this.highlightedConnection = null;
         this.draggedConnection = null;
         this.status = "Deleted link";
         this.statusColor = -11382;
         this.refreshProperties();
      }
   }

   private void setConnectionColor(MacroBuilderScreen.ConnectionHit connection, String color, String message) {
      if (connection != null) {
         connection.source().setConnectionColor(connection.output(), connection.target().id, color);
         this.highlightedConnection = connection;
         this.status = message;
         this.statusColor = -4200769;
         this.refreshProperties();
      }
   }

   private void setAllComponentDelays() {
      this.syncSelectedFromFields();
      int delay = this.model.stepDelayMs();

      for (MacroModel.Node node : this.model.nodes()) {
         if (!"official:start".equals(node.type)) {
            node.delayMs = delay;
         }
      }

      this.status = "Set all component delays to " + delay + " ms";
      this.statusColor = -4200769;
      this.refreshProperties();
   }

   private void deleteSelected() {
      List<MacroModel.Node> nodes = new ArrayList<>(this.movableSelection());
      if (!nodes.isEmpty()) {
         if (this.editingNote != null && nodes.contains(this.editingNote)) {
            this.stopNoteEditing(true);
         }

         int deleted = 0;
         for (MacroModel.Node node : nodes) {
            if (this.model.removeNode(node)) {
               deleted++;
            }
         }

         if (deleted > 0) {
            this.status = deleted == 1 ? "Deleted" : "Deleted " + deleted + " components";
            this.statusColor = -11382;
            this.selectNode(null, false);
            this.highlightedConnection = null;
            this.draggedConnection = null;
         }

         this.refreshProperties();
      }
   }

   private void drawLayout(GuiGraphicsExtractor context) {
      int paletteWidth = this.paletteWidth();
      int rightX = this.propertyLeft();
      int rightWidth = this.propertiesWidth();
      int propertyInnerWidth = this.propertyInnerWidth();
      context.fill(0, 0, this.width, TOP_BAR_HEIGHT, -15328737);
      context.fill(0, TOP_BAR_HEIGHT, paletteWidth, this.height, -14867926);
      context.fill(rightX, TOP_BAR_HEIGHT, this.width, this.height, -14867926);
      this.drawCanvasGrid(context);
      context.text(this.font, "Components", 14, 48, -1);
      context.text(this.font, "Delay", this.globalDelayLabelX(), 14, -2565928);
      context.text(this.font, "Name", rightX + 14, 14, -2565928);
      context.text(this.font, "Run On: Local Player", rightX + 14, 48, -4204545);
      context.text(this.font, this.fit(this.runner.status(), propertyInnerWidth), rightX + 14, 64, -3092272);
      context.text(this.font, "Zoom " + (int)Math.round(this.zoom * 100.0) + "%", this.canvasLeft() + 10, this.height - 18, -5327166);
      this.paletteScroll = clamp(this.paletteScroll, 0, this.maxPaletteScroll());
      int y = this.paletteListTop() - this.paletteScroll;
      context.enableScissor(0, this.paletteListTop() - 4, paletteWidth, this.height);

      for (MacroBuilderScreen.PaletteRow row : this.paletteRows()) {
         int rowHeight = this.paletteRowHeight(row);
         if (row.group() != null) {
            if (y + 22 >= this.paletteListTop() - 4 && y <= this.height) {
               boolean expanded = this.expandedGroups.getOrDefault(row.group(), false);
               int sectionColor = this.sectionColor(row.group());
               context.fill(8, y, paletteWidth - 8, y + 22 - 2, this.sectionHeaderColor(row.group()));
               context.fill(8, y, 14, y + 22 - 2, sectionColor);
               context.text(this.font, this.fit((expanded ? "v " : "> ") + row.group(), paletteWidth - 28), 20, y + 7, -1);
            }

            y += rowHeight;
         } else {
            MacroModel.Descriptor descriptor = row.descriptor();
            if (descriptor != null) {
               int itemColor = this.sectionColor(descriptor.group());
               if (y + rowHeight >= this.paletteListTop() - 4 && y <= this.height) {
                  context.fill(12, y, paletteWidth - 12, y + rowHeight - 4, -14012359);
                  context.fill(12, y, 18, y + rowHeight - 4, itemColor);
                  context.text(this.font, this.fit(descriptor.label(), this.paletteTextWidth()), 24, y + 6, -1);
                  this.drawWrappedText(context, descriptor.description(), 24, y + 18, this.paletteTextWidth(), -5327166);
               }

               y += rowHeight;
            }
         }
      }

      context.disableScissor();
   }

   private int globalDelayButtonX() {
      return this.globalDelayFieldX() + 44;
   }

   private int paletteWidth() {
      return Math.min(PALETTE_WIDTH, Math.max(132, this.width / 6));
   }

   private int propertiesWidth() {
      return Math.min(PROPERTIES_WIDTH, Math.max(190, this.width / 4));
   }

   private int propertyLeft() {
      return this.width - this.propertiesWidth();
   }

   private int propertyInnerWidth() {
      return Math.max(132, this.propertiesWidth() - 28);
   }

   private int canvasLeft() {
      return this.paletteWidth();
   }

   private int canvasTop() {
      return TOP_BAR_HEIGHT;
   }

   private int canvasRight() {
      return Math.max(this.canvasLeft() + 1, this.propertyLeft());
   }

   private int paletteTextWidth() {
      return Math.max(84, this.paletteWidth() - 42);
   }

   private int globalDelayFieldX() {
      return this.globalDelayLabelX() + 34;
   }

   private int globalDelayLabelX() {
      return Math.max(430, this.propertyLeft() - 176);
   }

   private boolean canDrawTopTitle() {
      int titleWidth = this.font.width(this.title.getString());
      int titleLeft = this.width / 2 - titleWidth / 2;
      int titleRight = this.width / 2 + titleWidth / 2;
      return titleLeft >= 260 && titleRight <= this.globalDelayLabelX() - 12;
   }

   private void drawPropertyLabels(GuiGraphicsExtractor context) {
      int rightX = this.propertyLeft();
      int rightWidth = this.propertyInnerWidth();
      if (this.selectedNode == null) {
         context.text(this.font, "No component selected", rightX + 14, 110, -5327166);
      } else {
         MacroModel.Descriptor descriptor = this.selectedNode.descriptor();
         String title = this.selectedNodes.size() > 1 ? this.selectedNodes.size() + " components selected" : descriptor.label();
         context.text(this.font, this.fit(title, rightWidth), rightX + 14, 110, -1);
         this.drawWrappedText(context, descriptor.description(), rightX + 14, 122, rightWidth, -7366491, 3);
         if (this.isNoteNode(this.selectedNode)) {
            return;
         }

         if ("official:start".equals(this.selectedNode.type)) {
            context.text(this.font, "Start Settings", rightX + 14, 140, -2565928);
            context.text(this.font, this.fit(this.autoSettingsButtonText(), rightWidth), rightX + 14, 152, -4204545);
            context.text(this.font, "Edit with the Auto button above.", rightX + 14, 188, -7366491);
         }

         if (!descriptor.primaryLabel().isBlank()) {
            context.text(this.font, descriptor.primaryLabel(), rightX + 14, 224, -2565928);
         }

         if (!descriptor.secondaryLabel().isBlank() && !this.isClickGuiItemNode(this.selectedNode)) {
            context.text(this.font, descriptor.secondaryLabel(), rightX + 14, 290, -2565928);
         }

         context.text(this.font, "Component delay (blank = global)", rightX + 14, 164, -2565928);
         if (this.hasExcludeSlotField(this.selectedNode)) {
            context.text(this.font, "Exclude slots", rightX + 14, 400, -2565928);
            context.text(this.font, "0-8 hotbar, 9-35 inventory", rightX + 14, 438, -7366491);
         }

         context.text(this.font, "Connections", rightX + 14, 454, -2565928);
         int y = 526;

         for (String output : descriptor.outputs()) {
            if (y > this.height - 18) {
               context.text(this.font, "...", rightX + 14, y, -5327166);
               break;
            }

            List<String> targets = this.selectedNode.nextTargets(output);
            String targetLabel = targets.isEmpty() ? "none" : (targets.size() == 1 ? this.labelForTarget(targets.get(0)) : targets.size() + " links");
             int rowWidth = targets.isEmpty() ? rightWidth : Math.max(80, rightWidth - 50);
             context.text(this.font, this.fit(output + " -> " + targetLabel, rowWidth), rightX + 14, y, -5327166);
            if (!targets.isEmpty()) {
               String clearText = "Clear";
               int clearX = this.width - 14 - this.font.width(clearText);
               context.text(this.font, clearText, clearX, y, -20312);
            }

            y += 12;
         }
      }
   }

   private void drawContextMenu(GuiGraphicsExtractor context, int mouseX, int mouseY) {
      if (this.contextMenuOpen) {
         int left = this.contextMenuX;
         int top = this.contextMenuY;
         int width = CONTEXT_MENU_WIDTH;
         int height = this.contextMenuHeight();
         context.fill(left - 1, top - 1, left + width + 1, top + height + 1, -12615681);
         context.fill(left, top, left + width, top + height, -15592942);
         String[] labels = this.contextMenuLabels();

         for (int row = 0; row < labels.length; row++) {
            int rowTop = top + 2 + row * CONTEXT_MENU_ROW_HEIGHT;
            boolean enabled = this.contextMenuActionEnabled(row);
            boolean hovered = enabled && mouseX >= left && mouseX <= left + width && mouseY >= rowTop && mouseY < rowTop + CONTEXT_MENU_ROW_HEIGHT;
            if (hovered) {
               context.fill(left + 2, rowTop, left + width - 2, rowTop + CONTEXT_MENU_ROW_HEIGHT, -11569250);
            }

            context.text(this.font, labels[row], left + 8, rowTop + 5, enabled ? -1 : -7366491);
         }
      }
   }

   private void drawTopActionsMenu(GuiGraphicsExtractor context, int mouseX, int mouseY) {
      if (this.topActionsMenuOpen) {
         int left = this.topActionsMenuX();
         int top = this.topActionsMenuY();
         int width = TOP_ACTION_MENU_WIDTH;
         int height = this.topActionLabels().length * TOP_ACTION_ROW_HEIGHT + 4;
         context.fill(left - 1, top - 1, left + width + 1, top + height + 1, -12615681);
         context.fill(left, top, left + width, top + height, -15592942);
         String[] labels = this.topActionLabels();

         for (int row = 0; row < labels.length; row++) {
            int rowTop = top + 2 + row * TOP_ACTION_ROW_HEIGHT;
            boolean enabled = this.topActionEnabled(row);
            boolean hovered = enabled && mouseX >= left && mouseX <= left + width && mouseY >= rowTop && mouseY < rowTop + TOP_ACTION_ROW_HEIGHT;
            if (hovered) {
               context.fill(left + 2, rowTop, left + width - 2, rowTop + TOP_ACTION_ROW_HEIGHT, -11569250);
            }

            context.text(this.font, labels[row], left + 8, rowTop + 5, enabled ? -1 : -7366491);
         }
      }
   }

   private boolean handleTopActionsMenuClick(int mouseX, int mouseY) {
      if (this.topActionsButtonAt(mouseX, mouseY)) {
         this.topActionsMenuOpen = false;
         return true;
      }

      int left = this.topActionsMenuX();
      int top = this.topActionsMenuY();
      int row = (mouseY - top - 2) / TOP_ACTION_ROW_HEIGHT;
      if (mouseX < left || mouseX > left + TOP_ACTION_MENU_WIDTH || row < 0 || row >= this.topActionLabels().length || !this.topActionEnabled(row)) {
         this.topActionsMenuOpen = false;
         return true;
      }

      this.topActionsMenuOpen = false;
      switch (row) {
         case 0 -> this.resetViewport();
         case 1 -> this.copySelectedComponent();
         case 2 -> this.pasteCopiedComponent();
         case 3 -> this.openMarketplace();
      }

      return true;
   }

   private String[] topActionLabels() {
      return new String[]{"Reset View", "Copy", "Paste", "Market"};
   }

   private boolean topActionEnabled(int row) {
      return switch (row) {
         case 1 -> this.selectedNode != null && !"official:start".equals(this.selectedNode.type);
         case 2 -> !this.copiedComponents.isEmpty();
         default -> true;
      };
   }

   private boolean topActionsButtonAt(int mouseX, int mouseY) {
      return mouseX >= TOP_MORE_X && mouseX <= TOP_MORE_X + TOP_MORE_WIDTH && mouseY >= TOP_BUTTON_Y && mouseY <= TOP_BUTTON_Y + TOP_BUTTON_HEIGHT;
   }

   private int topActionsMenuX() {
      return TOP_MORE_X;
   }

   private int topActionsMenuY() {
      return TOP_BUTTON_Y + TOP_BUTTON_HEIGHT + 4;
   }

   private void resetViewport() {
      this.panX = 0.0;
      this.panY = 0.0;
      this.zoom = 1.0;
      this.status = "View reset";
      this.statusColor = -4200769;
   }

   private void openMarketplace() {
      this.syncSelectedFromFields();
      if (this.save()) {
         this.minecraft.setScreenAndShow(new MacroMarketplaceScreen(this.runner, (this.runner.currentMacroNumber() - 1) / 10));
      }
   }

   private void drawSelectionBox(GuiGraphicsExtractor context) {
      if (this.selectionBoxActive) {
         int left = Math.min(this.selectionBoxStartX, this.selectionBoxEndX);
         int top = Math.min(this.selectionBoxStartY, this.selectionBoxEndY);
         int right = Math.max(this.selectionBoxStartX, this.selectionBoxEndX);
         int bottom = Math.max(this.selectionBoxStartY, this.selectionBoxEndY);
         context.fill(left, top, right, bottom, 0x224AA3FF);
         context.fill(left, top, right, top + 1, 0xFF4AA3FF);
         context.fill(left, bottom - 1, right, bottom, 0xFF4AA3FF);
         context.fill(left, top, left + 1, bottom, 0xFF4AA3FF);
         context.fill(right - 1, top, right, bottom, 0xFF4AA3FF);
      }
   }

   private void copySelectedComponent() {
      this.syncSelectedFromFields();
      List<MacroModel.Node> nodes = this.movableSelection().stream().filter(node -> !"official:start".equals(node.type)).toList();
      if (!nodes.isEmpty()) {
         List<String> copiedIds = nodes.stream().map(node -> node.id).toList();
         List<MacroBuilderScreen.CopiedComponent> copies = new ArrayList<>();

         for (MacroModel.Node node : nodes) {
            Map<String, String> outputs = new LinkedHashMap<>();
            for (Entry<String, String> output : node.outputs.entrySet()) {
               String targets = "";
               for (String targetId : MacroModel.outputTargets(output.getValue())) {
                  if (copiedIds.contains(targetId)) {
                     targets = MacroModel.addOutputTarget(targets, targetId);
                  }
               }

               if (!targets.isBlank()) {
                  outputs.put(output.getKey(), targets);
               }
            }

            Map<String, String> routes = new LinkedHashMap<>();
            for (Entry<String, String> route : node.connectionRoutes.entrySet()) {
               String targetId = connectionTargetId(route.getKey());
               if (copiedIds.contains(targetId)) {
                  routes.put(route.getKey(), route.getValue());
               }
            }

            Map<String, String> colors = new LinkedHashMap<>();
            for (Entry<String, String> color : node.connectionColors.entrySet()) {
               String targetId = connectionTargetId(color.getKey());
               if (copiedIds.contains(targetId)) {
                  colors.put(color.getKey(), color.getValue());
               }
            }

            copies.add(
               new MacroBuilderScreen.CopiedComponent(
                  node.id,
                  node.type,
                  node.value,
                  node.value2,
                  node.value3,
                  node.value4,
                  node.value5,
                  node.delayMs,
                  node.enabled,
                  node.x,
                  node.y,
                  node.noteWidth,
                  node.noteHeight,
                  outputs,
                  routes,
                  colors
               )
            );
         }

         this.copiedComponents = List.copyOf(copies);
         this.copiedComponent = this.copiedComponents.get(0);
         this.status = this.copiedComponents.size() == 1
            ? "Copied " + MacroModel.descriptor(this.copiedComponent.type()).label()
            : "Copied " + this.copiedComponents.size() + " components";
         this.statusColor = -4200769;
         this.refreshProperties();
      } else {
         this.status = "Select a component to copy";
         this.statusColor = -11382;
      }
   }

   private void pasteCopiedComponent() {
      this.syncSelectedFromFields();
      if (this.copiedComponents.isEmpty()) {
         this.status = "No copied component";
         this.statusColor = -11382;
      } else {
         MacroBuilderScreen.CopiedComponent first = this.copiedComponents.get(0);
         int x = this.selectedNode == null ? first.x() + 32 : this.selectedNode.x + 32;
         int y = this.selectedNode == null ? first.y() + 32 : this.selectedNode.y + 32;
         this.pasteCopiedComponentAt(x, y);
      }
   }

   private void pasteCopiedComponentAt(int x, int y) {
      if (this.copiedComponents.isEmpty()) {
         this.status = "No copied component";
         this.statusColor = -11382;
      } else {
         int minX = this.copiedComponents.stream().mapToInt(MacroBuilderScreen.CopiedComponent::x).min().orElse(x);
         int minY = this.copiedComponents.stream().mapToInt(MacroBuilderScreen.CopiedComponent::y).min().orElse(y);
         Map<String, MacroModel.Node> pastedByOriginalId = new LinkedHashMap<>();
         List<MacroModel.Node> pastedNodes = new ArrayList<>();

         for (MacroBuilderScreen.CopiedComponent copy : this.copiedComponents) {
            MacroModel.Node pasted = this.model.addNode(copy.type(), x + copy.x() - minX, y + copy.y() - minY);
            pasted.value = copy.value();
            pasted.value2 = copy.value2();
            pasted.value3 = copy.value3();
            pasted.value4 = copy.value4();
            pasted.value5 = copy.value5();
            pasted.delayMs = copy.delayMs();
            pasted.enabled = copy.enabled();
            pasted.noteWidth = copy.noteWidth();
            pasted.noteHeight = copy.noteHeight();
            pastedByOriginalId.put(copy.originalId(), pasted);
            pastedNodes.add(pasted);
         }

         for (MacroBuilderScreen.CopiedComponent copy : this.copiedComponents) {
            MacroModel.Node pasted = pastedByOriginalId.get(copy.originalId());
            if (pasted == null) {
               continue;
            }

            for (Entry<String, String> output : copy.outputs().entrySet()) {
               for (String targetId : MacroModel.outputTargets(output.getValue())) {
                  MacroModel.Node target = pastedByOriginalId.get(targetId);
                  if (target != null) {
                     pasted.addTarget(output.getKey(), target.id);
                  }
               }
            }

            for (Entry<String, String> route : copy.routes().entrySet()) {
               String output = connectionOutputKey(route.getKey());
               MacroModel.Node target = pastedByOriginalId.get(connectionTargetId(route.getKey()));
               if (target != null && output != null) {
                  pasted.connectionRoutes.put(output + "->" + target.id, route.getValue());
               }
            }

            for (Entry<String, String> color : copy.colors().entrySet()) {
               String output = connectionOutputKey(color.getKey());
               MacroModel.Node target = pastedByOriginalId.get(connectionTargetId(color.getKey()));
               if (target != null && output != null) {
                  pasted.connectionColors.put(output + "->" + target.id, color.getValue());
               }
            }
         }

         this.selectedNodes.clear();
         this.selectedNodes.addAll(pastedNodes);
         this.selectedNode = pastedNodes.isEmpty() ? null : pastedNodes.get(pastedNodes.size() - 1);
         this.status = pastedNodes.size() == 1 ? "Pasted " + this.selectedNode.descriptor().label() : "Pasted " + pastedNodes.size() + " components";
         this.statusColor = -4200769;
         this.refreshProperties();
      }
   }

   private void duplicateSelectedComponent(MacroModel.Node node) {
      if (node == null || "official:start".equals(node.type)) {
         return;
      }

      if (!this.nodeSelected(node)) {
         this.selectNode(node, false);
      }

      this.copySelectedComponent();
      this.pasteCopiedComponent();
   }

   private static String connectionOutputKey(String key) {
      int arrow = key == null ? -1 : key.indexOf("->");
      return arrow < 0 ? null : key.substring(0, arrow);
   }

   private static String connectionTargetId(String key) {
      int arrow = key == null ? -1 : key.indexOf("->");
      return arrow < 0 ? "" : key.substring(arrow + 2);
   }

   private MacroModel.Node addNoteAt(int x, int y) {
      MacroModel.Node note = this.model.addNode(MacroModel.BUILDER_NOTE, x, y);
      note.noteWidth = NOTE_WIDTH;
      note.noteHeight = NOTE_HEIGHT;
      this.selectNode(note, false);
      this.startNoteEditing(note);
      this.status = "Added note";
      this.statusColor = -4200769;
      this.refreshProperties();
      return note;
   }

   private void startNoteEditing(MacroModel.Node node) {
      if (!this.isNoteNode(node)) {
         this.stopNoteEditing(true);
         return;
      }

      if (this.editingNote == node) {
         return;
      }

      this.stopNoteEditing(true);
      this.editingNote = node;
      this.editingNoteText = this.noteText(node);
      this.editingNoteCaret = this.editingNoteText.length();
      this.resetNoteCaretBlink();
   }

   private void stopNoteEditing(boolean commit) {
      if (this.editingNote == null) {
         return;
      }

      if (commit) {
         this.editingNote.value = this.editingNoteText;
      }

      this.editingNote = null;
      this.editingNoteText = "";
      this.editingNoteCaret = 0;
      this.noteCaretVisible = true;
   }

   private void commitEditingNote() {
      if (this.editingNote != null) {
         this.editingNote.value = this.editingNoteText;
      }
   }

   private boolean handleNoteKey(int keyCode, boolean controlHeld) {
      if (this.editingNote == null) {
         return false;
      }

      switch (keyCode) {
         case GLFW.GLFW_KEY_BACKSPACE:
            if (this.editingNoteCaret > 0 && !this.editingNoteText.isEmpty()) {
               this.editingNoteText = this.editingNoteText.substring(0, this.editingNoteCaret - 1)
                  + this.editingNoteText.substring(this.editingNoteCaret);
               this.editingNoteCaret--;
               this.noteTextChanged();
            }

            return true;
         case GLFW.GLFW_KEY_DELETE:
            if (this.editingNoteCaret < this.editingNoteText.length()) {
               this.editingNoteText = this.editingNoteText.substring(0, this.editingNoteCaret)
                  + this.editingNoteText.substring(this.editingNoteCaret + 1);
               this.noteTextChanged();
            }

            return true;
         case GLFW.GLFW_KEY_LEFT:
            this.editingNoteCaret = Math.max(0, this.editingNoteCaret - 1);
            this.resetNoteCaretBlink();
            return true;
         case GLFW.GLFW_KEY_RIGHT:
            this.editingNoteCaret = Math.min(this.editingNoteText.length(), this.editingNoteCaret + 1);
            this.resetNoteCaretBlink();
            return true;
         case GLFW.GLFW_KEY_HOME:
            this.editingNoteCaret = 0;
            this.resetNoteCaretBlink();
            return true;
         case GLFW.GLFW_KEY_END:
            this.editingNoteCaret = this.editingNoteText.length();
            this.resetNoteCaretBlink();
            return true;
         case GLFW.GLFW_KEY_ENTER:
         case GLFW.GLFW_KEY_KP_ENTER:
            return this.insertNoteText("\n");
         case GLFW.GLFW_KEY_ESCAPE:
            this.stopNoteEditing(true);
            return true;
         case GLFW.GLFW_KEY_A:
            if (controlHeld) {
               this.editingNoteCaret = this.editingNoteText.length();
               this.resetNoteCaretBlink();
               return true;
            }

            return false;
         case GLFW.GLFW_KEY_C:
            if (controlHeld) {
               this.setClipboardText(this.editingNoteText);
               return true;
            }

            return false;
         case GLFW.GLFW_KEY_X:
            if (controlHeld) {
               this.setClipboardText(this.editingNoteText);
               this.editingNoteText = "";
               this.editingNoteCaret = 0;
               this.noteTextChanged();
               return true;
            }

            return false;
         case GLFW.GLFW_KEY_V:
            return controlHeld && this.insertNoteText(this.clipboardText());
         default:
            return false;
      }
   }

   private boolean insertNoteText(String text) {
      if (this.editingNote == null || text == null || text.isEmpty()) {
         return false;
      }

      StringBuilder safe = new StringBuilder();
      for (int index = 0; index < text.length(); index++) {
         char chr = text.charAt(index);
         if (chr == '\r') {
            safe.append('\n');
         } else if (chr == '\n' || chr == '\t' || chr >= 32) {
            safe.append(chr);
         }
      }

      if (safe.isEmpty()) {
         return true;
      }

      int allowed = NOTE_MAX_CHARS - this.editingNoteText.length();
      if (allowed <= 0) {
         return true;
      }

      String insert = safe.length() > allowed ? safe.substring(0, allowed) : safe.toString();
      int caret = clamp(this.editingNoteCaret, 0, this.editingNoteText.length());
      this.editingNoteText = this.editingNoteText.substring(0, caret) + insert + this.editingNoteText.substring(caret);
      this.editingNoteCaret = caret + insert.length();
      this.noteTextChanged();
      return true;
   }

   private void noteTextChanged() {
      this.resetNoteCaretBlink();
      this.commitEditingNote();
   }

   private void resetNoteCaretBlink() {
      this.noteCaretVisible = true;
      this.noteCaretLastToggleMs = System.currentTimeMillis();
   }

   private String noteText(MacroModel.Node node) {
      return node == null || node.value == null ? "" : node.value;
   }

   private boolean noteBodyAt(MacroModel.Node node, int mouseX, int mouseY) {
      if (!this.isNoteNode(node)) {
         return false;
      }

      double worldX = this.worldX(mouseX);
      double worldY = this.worldY(mouseY);
      return worldX >= node.x + NOTE_TEXT_MARGIN
         && worldX <= node.x + this.nodeWorldWidth(node) - NOTE_TEXT_MARGIN
         && worldY >= node.y + NOTE_HEADER_HEIGHT
         && worldY <= node.y + this.nodeWorldHeight(node) - NOTE_TEXT_MARGIN;
   }

   private boolean startNoteResize(MacroModel.Node node, int mouseX, int mouseY) {
      MacroBuilderScreen.NoteResizeCorner corner = this.noteResizeCornerAt(node, mouseX, mouseY);
      if (corner == null) {
         return false;
      }

      this.stopNoteEditing(true);
      this.resizingNote = node;
      this.resizingNoteCorner = corner;
      this.resizeStartX = node.x;
      this.resizeStartY = node.y;
      this.resizeStartWidth = this.nodeWorldWidth(node);
      this.resizeStartHeight = this.nodeWorldHeight(node);
      return true;
   }

   private MacroBuilderScreen.NoteResizeCorner noteResizeCornerAt(MacroModel.Node node, int mouseX, int mouseY) {
      if (!this.isNoteNode(node)) {
         return null;
      }

      int worldX = (int)Math.round(this.worldX(mouseX));
      int worldY = (int)Math.round(this.worldY(mouseY));
      int size = NOTE_HANDLE_SIZE;
      int half = size / 2;
      if (pointInRect(worldX, worldY, node.x - half, node.y - half, size, size)) {
         return MacroBuilderScreen.NoteResizeCorner.TOP_LEFT;
      } else if (pointInRect(worldX, worldY, node.x + this.nodeWorldWidth(node) - half, node.y - half, size, size)) {
         return MacroBuilderScreen.NoteResizeCorner.TOP_RIGHT;
      } else if (pointInRect(worldX, worldY, node.x - half, node.y + this.nodeWorldHeight(node) - half, size, size)) {
         return MacroBuilderScreen.NoteResizeCorner.BOTTOM_LEFT;
      } else {
         return pointInRect(worldX, worldY, node.x + this.nodeWorldWidth(node) - half, node.y + this.nodeWorldHeight(node) - half, size, size)
            ? MacroBuilderScreen.NoteResizeCorner.BOTTOM_RIGHT
            : null;
      }
   }

   private void updateNoteResize(int mouseX, int mouseY) {
      if (this.resizingNote == null || this.resizingNoteCorner == null) {
         return;
      }

      int worldX = (int)Math.round(this.worldX(mouseX));
      int worldY = (int)Math.round(this.worldY(mouseY));
      int left = this.resizeStartX;
      int top = this.resizeStartY;
      int right = this.resizeStartX + this.resizeStartWidth;
      int bottom = this.resizeStartY + this.resizeStartHeight;
      switch (this.resizingNoteCorner) {
         case TOP_LEFT:
            left = Math.min(worldX, right - NOTE_MIN_WIDTH);
            top = Math.min(worldY, bottom - NOTE_MIN_HEIGHT);
            break;
         case TOP_RIGHT:
            right = Math.max(worldX, left + NOTE_MIN_WIDTH);
            top = Math.min(worldY, bottom - NOTE_MIN_HEIGHT);
            break;
         case BOTTOM_LEFT:
            left = Math.min(worldX, right - NOTE_MIN_WIDTH);
            bottom = Math.max(worldY, top + NOTE_MIN_HEIGHT);
            break;
         case BOTTOM_RIGHT:
            right = Math.max(worldX, left + NOTE_MIN_WIDTH);
            bottom = Math.max(worldY, top + NOTE_MIN_HEIGHT);
            break;
      }

      this.resizingNote.x = left;
      this.resizingNote.y = top;
      this.resizingNote.noteWidth = Math.max(NOTE_MIN_WIDTH, right - left);
      this.resizingNote.noteHeight = Math.max(NOTE_MIN_HEIGHT, bottom - top);
   }

   private void finishNoteResize() {
      this.resizingNote = null;
      this.resizingNoteCorner = null;
      this.refreshProperties();
   }

   private String clipboardText() {
      try {
         return this.minecraft == null ? "" : this.minecraft.keyboardHandler.getClipboard();
      } catch (RuntimeException exception) {
         return "";
      }
   }

   private void setClipboardText(String text) {
      try {
         if (this.minecraft != null) {
            this.minecraft.keyboardHandler.setClipboard(text == null ? "" : text);
         }
      } catch (RuntimeException exception) {
      }
   }

   private static boolean pointInRect(int x, int y, int left, int top, int width, int height) {
      return x >= left && x <= left + width && y >= top && y <= top + height;
   }

   private boolean fieldFocused() {
      return this.componentSearchField != null && this.componentSearchField.isFocused()
         || this.nameField != null && this.nameField.isFocused()
         || this.delayField != null && this.delayField.isFocused()
         || this.primaryField != null && this.primaryField.isFocused()
         || this.secondaryField != null && this.secondaryField.isFocused()
         || this.excludeSlotsField != null && this.excludeSlotsField.isFocused()
         || this.nodeDelayField != null && this.nodeDelayField.isFocused();
   }

   private boolean controlDown() {
      return this.minecraft != null
         && (InputConstants.isKeyDown(this.minecraft.getWindow(), InputConstants.KEY_LCONTROL)
            || InputConstants.isKeyDown(this.minecraft.getWindow(), InputConstants.KEY_RCONTROL));
   }

   private void drawSelectedItemPreview(GuiGraphicsExtractor context) {
      if (this.selectedNode != null) {
         MacroModel.Descriptor descriptor = this.selectedNode.descriptor();
         int rightX = this.propertyLeft();
         if (!descriptor.primaryLabel().isBlank() && this.isItemField(descriptor.primaryLabel())) {
            this.drawItemPreview(context, this.primaryField.getValue(), rightX + 14, 264);
         }

         if (!descriptor.secondaryLabel().isBlank() && this.isItemField(descriptor.secondaryLabel())) {
            this.drawItemPreview(context, this.secondaryField.getValue(), rightX + 14, 330);
         }
      }
   }

   private void drawItemPreview(GuiGraphicsExtractor context, String itemId, int x, int y) {
      MacroBuilderScreen.ItemEntry entry = this.itemEntry(itemId);
      context.fill(x, y, x + 22, y + 20, -13617084);
      if (entry != null) {
         ItemStack stack = entry.stack();
         if (!stack.isEmpty()) {
            context.item(stack, x + 3, y + 2);
         }
      }
   }

   private void drawItemPicker(GuiGraphicsExtractor context, int mouseX, int mouseY) {
      if (this.itemPickerOpen) {
         List<MacroBuilderScreen.ItemEntry> items = this.filteredItems();
         MacroBuilderScreen.ItemPickerLayout layout = this.itemPickerLayout();
         context.fill(layout.left() - 2, layout.top() - 2, layout.left() + layout.panelWidth() + 2, layout.top() + layout.panelHeight() + 2, -1511947);
         context.fill(layout.left(), layout.top(), layout.left() + layout.panelWidth(), layout.top() + layout.panelHeight(), -14867926);
         context.text(this.font, "Pick Item", layout.left() + 8, layout.top() + 8, -1);
         context.text(this.font, "Esc closes", layout.left() + layout.panelWidth() - 64, layout.top() + 8, -5327166);
         context.text(this.font, "Search", layout.searchX(), layout.searchY() - 11, -5327166);
         context.fill(
            layout.searchX() - 1,
            layout.searchY() - 1,
            layout.searchX() + layout.searchWidth() + 1,
            layout.searchY() + 17,
            this.itemSearchFocused ? -1511947 : -7366491
         );
         context.fill(layout.searchX(), layout.searchY(), layout.searchX() + layout.searchWidth(), layout.searchY() + 16, -16250355);
         String searchDisplay = this.itemSearchText.isBlank() ? "type item name..." : this.itemSearchText;
         int searchColor = this.itemSearchText.isBlank() ? -9405818 : -1;
         context.text(this.font, this.fit(searchDisplay, layout.searchWidth() - 8), layout.searchX() + 4, layout.searchY() + 4, searchColor);
         if (this.itemSearchFocused && System.currentTimeMillis() / 450L % 2L == 0L) {
            int cursorX = layout.searchX() + 4 + this.font.width(this.fit(this.itemSearchText, layout.searchWidth() - 8));
            context.fill(cursorX, layout.searchY() + 3, cursorX + 1, layout.searchY() + 14, -1);
         }

         context.text(this.font, items.size() + " items", layout.left() + 8, layout.gridTop() - 12, -5327166);
         context.enableScissor(layout.left() + 6, layout.gridTop(), layout.left() + layout.panelWidth() - 6, layout.top() + layout.panelHeight() - 8);
         int startIndex = this.itemPickerScroll * layout.columns();
         int endIndex = Math.min(items.size(), startIndex + layout.rowsVisible() * layout.columns());

         for (int index = startIndex; index < endIndex; index++) {
            int local = index - startIndex;
            int col = local % layout.columns();
            int row = local / layout.columns();
            int x = layout.left() + 8 + col * 26;
            int y = layout.gridTop() + row * 26;
            MacroBuilderScreen.ItemEntry entry = items.get(index);
            boolean hovered = mouseX >= x && mouseX <= x + 22 && mouseY >= y && mouseY <= y + 22;
            context.fill(x, y, x + 22, y + 22, hovered ? -11838867 : -13617084);
            ItemStack stack = entry.stack();
            if (!stack.isEmpty()) {
               context.item(stack, x + 3, y + 3);
            } else {
               context.text(this.font, "?", x + 8, y + 7, -5327166);
            }
         }

         context.disableScissor();
         MacroBuilderScreen.ItemEntry hovered = this.itemPickerEntryAt(mouseX, mouseY);
         if (hovered != null) {
            ItemStack stack = hovered.stack();
            if (!stack.isEmpty()) {
               context.setTooltipForNextFrame(this.font, stack, mouseX, mouseY);
            }
         }
      }
   }

   private void drawNodes(GuiGraphicsExtractor context) {
      for (MacroModel.Node node : this.model.nodes()) {
         this.drawNode(context, node, this.nodeSelected(node));
      }
   }

   private void drawNode(GuiGraphicsExtractor context, MacroModel.Node node, boolean selected) {
      if (this.isNoteNode(node)) {
         this.drawNoteNode(context, node, selected);
         return;
      }

      MacroModel.Descriptor descriptor = node.descriptor();
      int sx = this.screenX(node.x);
      int sy = this.screenY(node.y);
      int nodeWidth = this.nodeScreenWidth(node);
      int nodeHeight = this.nodeScreenHeight(node);
      int headerHeight = Math.max(2, this.scaled(18));
      int border = selected ? -1 : (this.connectionTouchesNode(this.highlightedConnection, node) ? -4270801 : -13485756);
      context.fill(sx - 1, sy - 1, sx + nodeWidth + 1, sy + nodeHeight + 1, border);
      context.fill(sx, sy, sx + nodeWidth, sy + nodeHeight, node.enabled ? -14670290 : -15198184);
      context.fill(sx, sy, sx + nodeWidth, sy + headerHeight, node.enabled ? this.sectionColor(descriptor.group()) : -12434878);
      boolean showText = this.zoom >= 0.58 && nodeWidth >= 86 && nodeHeight >= 42;
      if (showText) {
         int delayBadgeWidth = node.delayMs >= 0 ? Math.max(34, this.font.width(node.delayMs + "ms") + 8) : 0;
         int indicatorWidth = 14;
         int titleWidth = nodeWidth - 12 - indicatorWidth - (delayBadgeWidth > 0 ? delayBadgeWidth + 4 : 0);
         this.drawNodeText(context, descriptor.label(), sx + Math.max(3, this.scaled(6)), sy + Math.max(2, this.scaled(5)), titleWidth, -1);
         this.drawMainMenuIndicator(context, node, sx + nodeWidth - indicatorWidth - Math.max(3, this.scaled(4)), sy + Math.max(2, this.scaled(3)));
         if (delayBadgeWidth > 0 && delayBadgeWidth < nodeWidth - 12) {
            int badgeRight = sx + nodeWidth - indicatorWidth - Math.max(5, this.scaled(7));
            int badgeLeft = badgeRight - delayBadgeWidth;
            int badgeTop = sy + Math.max(2, this.scaled(3));
            int badgeBottom = badgeTop + 12;
            context.fill(badgeLeft, badgeTop, badgeRight, badgeBottom, -15591392);
            this.drawNodeText(context, node.delayMs + "ms", badgeLeft + 3, badgeTop + 2, delayBadgeWidth - 6, -11930);
         }

         String value = this.nodeValueText(node);
         if (!value.isBlank()) {
            this.drawNodeText(context, value, sx + Math.max(3, this.scaled(6)), sy + this.scaled(27), nodeWidth - 12, -2565928);
         }

         if (!node.enabled) {
            this.drawNodeTextRight(context, "OFF", sx + nodeWidth - 6, sy + nodeHeight - 12, nodeWidth - 12, -30070);
         }
      }

      int dotSize = Math.max(2, this.scaled(4));

      for (String output : descriptor.outputs()) {
         int dotColor = node.outputs.containsKey(output) ? this.connectionColor(output) : -9274489;
         int y = this.outputY(node, output);
         int dotX = sx + nodeWidth - Math.max(5, this.scaled(9));
         int dotY = y + Math.max(1, this.scaled(2));
         context.fill(dotX, dotY, dotX + dotSize, dotY + dotSize, dotColor);
         if (showText) {
            this.drawNodeTextRight(context, output, dotX - 5, y, nodeWidth - 18, -5327166);
         }
      }
   }

   private void drawNoteNode(GuiGraphicsExtractor context, MacroModel.Node node, boolean selected) {
      int sx = this.screenX(node.x);
      int sy = this.screenY(node.y);
      int nodeWidth = this.nodeScreenWidth(node);
      int nodeHeight = this.nodeScreenHeight(node);
      int headerHeight = Math.max(2, this.scaled(NOTE_HEADER_HEIGHT));
      int border = selected ? NOTE_SELECTED_BORDER_COLOR : NOTE_BORDER_COLOR;
      context.fill(sx - 1, sy - 1, sx + nodeWidth + 1, sy + nodeHeight + 1, border);
      context.fill(sx, sy, sx + nodeWidth, sy + nodeHeight, NOTE_BODY_COLOR);
      context.fill(sx, sy, sx + nodeWidth, sy + headerHeight, node.enabled ? this.sectionColor(node.descriptor().group()) : -12434878);
      boolean showText = this.zoom >= 0.45 && nodeWidth >= 86 && nodeHeight >= 46;
      if (showText) {
         this.drawReadableNoteText(context, this.fit("Note", nodeWidth - 14), sx + Math.max(4, this.scaled(7)), sy + Math.max(2, this.scaled(5)), NOTE_TEXT_COLOR);
         String value = this.editingNote == node ? this.editingNoteText : this.noteText(node);
         int bodyMargin = Math.max(6, this.scaled(NOTE_TEXT_MARGIN));
         int bodyLeft = sx + bodyMargin;
         int bodyTop = sy + headerHeight + Math.max(7, this.scaled(8));
         int bodyBottom = sy + nodeHeight - Math.max(6, this.scaled(8));
         int bodyWidth = Math.max(1, nodeWidth - bodyMargin * 2);
         int lineStep = this.noteLineStep();
         int maxLines = Math.max(1, (bodyBottom - bodyTop + 1) / Math.max(1, lineStep));
         List<String> lines = StickyNoteTextLayout.wrapLines(value, this.font, bodyWidth, maxLines);
         for (int index = 0; index < lines.size(); index++) {
            int lineY = bodyTop + index * lineStep;
            if (lineY + this.font.lineHeight > bodyBottom) {
               break;
            }

            this.drawReadableNoteText(context, lines.get(index), bodyLeft, lineY, NOTE_TEXT_COLOR);
         }

         this.drawNoteCaret(context, node, bodyLeft, bodyTop, bodyWidth, maxLines, lineStep, bodyBottom);
      }

      if (selected) {
         this.drawNoteResizeHandle(context, node, MacroBuilderScreen.NoteResizeCorner.TOP_LEFT, border);
         this.drawNoteResizeHandle(context, node, MacroBuilderScreen.NoteResizeCorner.TOP_RIGHT, border);
         this.drawNoteResizeHandle(context, node, MacroBuilderScreen.NoteResizeCorner.BOTTOM_LEFT, border);
         this.drawNoteResizeHandle(context, node, MacroBuilderScreen.NoteResizeCorner.BOTTOM_RIGHT, border);
      }
   }

   private void drawReadableNoteText(GuiGraphicsExtractor context, String text, int x, int y, int color) {
      if (text == null || text.isEmpty()) {
         return;
      }

      context.text(this.font, text, x, y, color);
   }

   private void drawNoteCaret(
      GuiGraphicsExtractor context, MacroModel.Node node, int bodyLeft, int bodyTop, int bodyWidth, int maxLines, int lineStep, int bottom
   ) {
      if (this.editingNote != node) {
         return;
      }

      long now = System.currentTimeMillis();
      if (now - this.noteCaretLastToggleMs >= NOTE_CARET_BLINK_MS) {
         this.noteCaretVisible = !this.noteCaretVisible;
         this.noteCaretLastToggleMs = now;
      }

      if (!this.noteCaretVisible) {
         return;
      }

      int caret = clamp(this.editingNoteCaret, 0, this.editingNoteText.length());
      String beforeCaret = this.editingNoteText.substring(0, caret);
      List<String> wrappedBeforeCaret = StickyNoteTextLayout.wrapLines(beforeCaret, this.font, bodyWidth, maxLines + 1);
      if (wrappedBeforeCaret.isEmpty()) {
         wrappedBeforeCaret = List.of("");
      }

      int lineIndex = wrappedBeforeCaret.size() - 1;
      if (lineIndex >= maxLines) {
         return;
      }

      String lineText = wrappedBeforeCaret.get(lineIndex);
      int caretX = Math.min(bodyLeft + this.font.width(lineText), bodyLeft + bodyWidth - 1);
      int caretY = bodyTop + lineIndex * lineStep;
      context.fill(caretX, caretY, caretX + Math.max(1, this.scaled(2)), Math.min(bottom, caretY + this.font.lineHeight), NOTE_TEXT_COLOR);
   }

   private int noteLineStep() {
      return this.font.lineHeight + Math.max(3, this.scaled(3));
   }

   private void drawNoteResizeHandle(GuiGraphicsExtractor context, MacroModel.Node node, MacroBuilderScreen.NoteResizeCorner corner, int color) {
      int size = Math.max(4, this.scaled(NOTE_HANDLE_SIZE));
      int half = size / 2;
      int centerX = switch (corner) {
         case TOP_LEFT, BOTTOM_LEFT -> this.screenX(node.x);
         case TOP_RIGHT, BOTTOM_RIGHT -> this.screenX(node.x + this.nodeWorldWidth(node));
      };
      int centerY = switch (corner) {
         case TOP_LEFT, TOP_RIGHT -> this.screenY(node.y);
         case BOTTOM_LEFT, BOTTOM_RIGHT -> this.screenY(node.y + this.nodeWorldHeight(node));
      };
      int left = centerX - half;
      int top = centerY - half;
      context.fill(left, top, left + size, top + size, 0xFFFFFFFF);
      context.fill(left + 1, top + 1, left + size - 1, top + size - 1, color);
   }

   private void drawMainMenuIndicator(GuiGraphicsExtractor context, MacroModel.Node node, int x, int y) {
      boolean mainMenu = MacroModel.runsInMainMenu(node.type);
      int color = mainMenu ? -11939459 : -9274489;
      context.fill(x, y, x + 10, y + 10, -15591392);
      context.fill(x + 1, y + 1, x + 9, y + 9, color);
      context.text(this.font, "M", x + 2, y + 1, -1);
   }

   private void drawConnections(GuiGraphicsExtractor context) {
      for (MacroModel.Node source : this.model.nodes()) {
         for (String output : source.descriptor().outputs()) {
            for (String targetId : source.nextTargets(output)) {
               MacroModel.Node target = this.model.node(targetId);
               if (target != null) {
                  int startX = this.screenX(source.x + this.nodeWorldWidth(source));
                  int startY = this.outputY(source, output);
                  int endX = this.screenX(target.x);
                  int endY = this.screenY(target.y + 14);
                  int midX = this.connectionBendScreenX(source, output, target);
                  int color = this.connectionColor(source, output, target);
                  boolean highlighted = this.connectionIsHighlighted(source, output, target);
                  if (highlighted) {
                     int highlightSize = Math.max(3, this.scaled(5));
                     this.drawSegmentWide(context, startX, startY, midX, startY, highlightSize, -12549889);
                     this.drawSegmentWide(context, midX, startY, midX, endY, highlightSize, -12549889);
                     this.drawSegmentWide(context, midX, endY, endX, endY, highlightSize, -12549889);
                  }

                  this.drawSegment(context, startX, startY, midX, startY, color);
                  this.drawSegment(context, midX, startY, midX, endY, color);
                  this.drawSegment(context, midX, endY, endX, endY, color);
                  if (highlighted) {
                     this.drawConnectionGrip(context, midX, startY, color);
                     this.drawConnectionGrip(context, midX, (startY + endY) / 2, color);
                     this.drawConnectionGrip(context, midX, endY, color);
                  }
               }
            }
         }
      }
   }

   private void drawConnectionGrip(GuiGraphicsExtractor context, int x, int y, int color) {
      int size = Math.max(3, this.scaled(4));
      context.fill(x - size, y - size, x + size + 1, y + size + 1, -1);
      context.fill(x - size + 1, y - size + 1, x + size, y + size, color);
   }

   private MacroBuilderScreen.ConnectionHit connectionAt(int mouseX, int mouseY) {
      List<MacroModel.Node> nodes = this.model.nodes();

      for (int sourceIndex = nodes.size() - 1; sourceIndex >= 0; sourceIndex--) {
         MacroModel.Node source = nodes.get(sourceIndex);

         for (String output : source.descriptor().outputs()) {
            List<String> targets = source.nextTargets(output);

            for (int targetIndex = targets.size() - 1; targetIndex >= 0; targetIndex--) {
               MacroModel.Node target = this.model.node(targets.get(targetIndex));
               if (target != null) {
                  int startX = this.screenX(source.x + this.nodeWorldWidth(source));
                  int startY = this.outputY(source, output);
                  int endX = this.screenX(target.x);
                  int endY = this.screenY(target.y + 14);
                  int midX = this.connectionBendScreenX(source, output, target);
                  if (this.nearSegment(mouseX, mouseY, startX, startY, midX, startY)
                     || this.nearSegment(mouseX, mouseY, midX, startY, midX, endY)
                     || this.nearSegment(mouseX, mouseY, midX, endY, endX, endY)) {
                     return new MacroBuilderScreen.ConnectionHit(source, output, target);
                  }
               }
            }
         }
      }

      return null;
   }

   private MacroBuilderScreen.ConnectionHit connectionGripAt(int mouseX, int mouseY, boolean fallbackToSegment) {
      List<MacroModel.Node> nodes = this.model.nodes();

      for (int sourceIndex = nodes.size() - 1; sourceIndex >= 0; sourceIndex--) {
         MacroModel.Node source = nodes.get(sourceIndex);

         for (String output : source.descriptor().outputs()) {
            List<String> targets = source.nextTargets(output);

            for (int targetIndex = targets.size() - 1; targetIndex >= 0; targetIndex--) {
               MacroModel.Node target = this.model.node(targets.get(targetIndex));
               if (target != null) {
                  int startY = this.outputY(source, output);
                  int endY = this.screenY(target.y + 14);
                  int midX = this.connectionBendScreenX(source, output, target);
                  if (this.nearPoint(mouseX, mouseY, midX, startY)
                     || this.nearPoint(mouseX, mouseY, midX, (startY + endY) / 2)
                     || this.nearPoint(mouseX, mouseY, midX, endY)) {
                     return new MacroBuilderScreen.ConnectionHit(source, output, target);
                  }
               }
            }
         }
      }

      return fallbackToSegment ? this.connectionAt(mouseX, mouseY) : null;
   }

   private boolean nearPoint(int mouseX, int mouseY, int x, int y) {
      int tolerance = Math.max(6, this.scaled(6));
      return Math.abs(mouseX - x) <= tolerance && Math.abs(mouseY - y) <= tolerance;
   }

   private boolean nearSegment(int mouseX, int mouseY, int x1, int y1, int x2, int y2) {
      int tolerance = 5;
      if (x1 == x2) {
         return Math.abs(mouseX - x1) <= tolerance && mouseY >= Math.min(y1, y2) - tolerance && mouseY <= Math.max(y1, y2) + tolerance;
      } else if (y1 == y2) {
         return Math.abs(mouseY - y1) <= tolerance && mouseX >= Math.min(x1, x2) - tolerance && mouseX <= Math.max(x1, x2) + tolerance;
      } else {
         return false;
      }
   }

   private boolean connectionIsHighlighted(MacroModel.Node source, String output, MacroModel.Node target) {
      return this.connectionMatches(this.highlightedConnection, source, output, target)
         || this.connectionMatches(this.draggedConnection, source, output, target)
         || this.selectedNode == source
         || this.selectedNode == target;
   }

   private boolean connectionTouchesNode(MacroBuilderScreen.ConnectionHit hit, MacroModel.Node node) {
      return hit != null && (hit.source() == node || hit.target() == node);
   }

   private boolean connectionMatches(MacroBuilderScreen.ConnectionHit hit, MacroModel.Node source, String output, MacroModel.Node target) {
      return hit != null && hit.source() == source && hit.target() == target && hit.output().equals(output);
   }

   private int connectionBendScreenX(MacroModel.Node source, String output, MacroModel.Node target) {
      return this.screenX(this.connectionRouteWorldX(source, output, target));
   }

   private double connectionRouteWorldX(MacroModel.Node source, String output, MacroModel.Node target) {
      String route = source.connectionRoute(output, target.id);
      if (route != null && !route.isBlank()) {
         try {
            return Double.parseDouble(route);
         } catch (NumberFormatException ignored) {
         }
      }

      double startX = source.x + this.nodeWorldWidth(source);
      double endX = target.x;
      return startX + Math.max(18.0, (endX - startX) / 2.0);
   }

   private void drawPaletteDragPreview(GuiGraphicsExtractor context) {
      if (this.paletteDragType != null) {
         MacroModel.Descriptor descriptor = MacroModel.descriptor(this.paletteDragType);
         int nodeWidth = this.nodeScreenWidth(this.paletteDragType);
         int nodeHeight = this.nodeScreenHeight(this.paletteDragType);
         int x = this.dragMouseX - nodeWidth / 2;
         int y = this.dragMouseY - nodeHeight / 2;
         context.fill(x, y, x + nodeWidth, y + nodeHeight, -870308306);
         context.fill(x, y, x + nodeWidth, y + Math.max(2, this.scaled(18)), this.sectionColor(descriptor.group()));
         if (this.zoom >= 0.58 && nodeWidth >= 86 && nodeHeight >= 42) {
            context.text(this.font, this.fit(descriptor.label(), nodeWidth - 12), x + Math.max(3, this.scaled(6)), y + Math.max(2, this.scaled(5)), -1);
         }
      }
   }

   private MacroBuilderScreen.PaletteHit paletteHitAt(int mouseX, int mouseY) {
      if (mouseX >= 12 && mouseX <= this.paletteWidth() - 12 && mouseY >= this.paletteListTop()) {
         int y = this.paletteListTop() - this.paletteScroll;

         for (MacroBuilderScreen.PaletteRow row : this.paletteRows()) {
            int height = this.paletteRowHeight(row);
            if (mouseY >= y && mouseY <= y + height - 4) {
               if (row.group() != null) {
                  return new MacroBuilderScreen.PaletteHit(null, row.group());
               }

               MacroModel.Descriptor descriptor = row.descriptor();
               return descriptor == null ? null : new MacroBuilderScreen.PaletteHit(descriptor.type(), null);
            }

            y += height;
         }

         return null;
      } else {
         return null;
      }
   }

   private MacroModel.Node nodeAt(int mouseX, int mouseY) {
      List<MacroModel.Node> nodes = this.model.nodes();

      for (int index = nodes.size() - 1; index >= 0; index--) {
         MacroModel.Node node = nodes.get(index);
         double worldX = this.worldX(mouseX);
         double worldY = this.worldY(mouseY);
         if (worldX >= node.x && worldX <= node.x + this.nodeWorldWidth(node) && worldY >= node.y && worldY <= node.y + this.nodeWorldHeight(node)) {
            return node;
         }
      }

      return null;
   }

   private boolean insideCanvas(int mouseX, int mouseY) {
      return mouseX > this.canvasLeft() && mouseX < this.canvasRight() && mouseY > this.canvasTop() && mouseY < this.height;
   }

   private int outputY(MacroModel.Node node, String output) {
      int outputIndex = 0;

      for (String key : node.descriptor().outputs()) {
         if (key.equals(output)) {
            return this.screenY(node.y + 47 + outputIndex * 10);
         }

         outputIndex++;
      }

      return this.screenY(node.y + this.nodeWorldHeight(node) / 2);
   }

   private String labelForTarget(String targetId) {
      MacroModel.Node target = this.model.node(targetId);
      return target == null ? targetId : target.descriptor().label();
   }

   private String connectionClearOutputAt(int mouseX, int mouseY) {
      if (this.selectedNode != null && !this.selectedNode.outputs.isEmpty()) {
         int rightX = this.propertyLeft();
         int clearRight = this.width - 14;
         int clearLeft = clearRight - this.font.width("Clear") - 4;
         if (mouseX >= clearLeft && mouseX <= clearRight && mouseX >= rightX) {
            int y = RIGHT_CONNECTION_LIST_Y;

            for (String output : this.selectedNode.descriptor().outputs()) {
               if (y > this.height - 18) {
                  break;
               }

               if (this.selectedNode.outputs.containsKey(output) && mouseY >= y - 2 && mouseY <= y + 10) {
                  return output;
               }

               y += 12;
            }

            return null;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private String connectionOutputAt(int mouseX, int mouseY) {
      if (this.selectedNode == null) {
         return null;
      } else {
         int rightX = this.propertyLeft();
         if (mouseX >= rightX + 14 && mouseX <= this.width - 14) {
            int y = RIGHT_CONNECTION_LIST_Y;

            for (String output : this.selectedNode.descriptor().outputs()) {
               if (y > this.height - 18) {
                  break;
               }

               if (mouseY >= y - 2 && mouseY <= y + 10) {
                  return output;
               }

               y += 12;
            }

            return null;
         } else {
            return null;
         }
      }
   }

   private String nodeValueText(MacroModel.Node node) {
      if ("official:misc.wait".equals(node.type)) {
         return node.value + " ms";
      } else if ("official:misc.chat".equals(node.type) || "builder:localMessage".equals(node.type)) {
         return node.value;
      } else if ("builder:misc.repeatMacro".equals(node.type)) {
         return this.repeatIndefinitelyEnabled(node) ? "forever" : node.value + " runs";
      } else if ("builder:misc.repeatSection".equals(node.type)) {
         return this.repeatIndefinitelyEnabled(node) ? "forever" : node.value + " loops";
      } else if ("builder:flow.endConnection".equals(node.type)) {
         return "end branch";
      } else if ("builder:flow.note".equals(node.type)) {
         return node.value == null ? "" : node.value;
      } else if ("builder:misc.idleUntil".equals(node.type)) {
         return node.value + (node.value2 == null || node.value2.isBlank() ? "" : " / " + node.value2);
      } else if ("builder:logic.scoreboardContains".equals(node.type)) {
         return node.value + " / " + node.value2;
      } else if ("builder:event.kicked".equals(node.type)) {
         return node.value + (node.value2 == null || node.value2.isBlank() ? "" : " / " + node.value2);
      } else if ("builder:player.autoClick".equals(node.type)) {
         return node.value + " / " + node.value2;
      } else if ("builder:player.pressKey".equals(node.type)) {
         return node.value + " / " + node.value2;
      } else if ("official:inventory.hasItem".equals(node.type) || "builder:inventory.hasItem".equals(node.type)) {
         return node.value + " x" + node.value2;
      } else if ("builder:player.healthBelow".equals(node.type)) {
         return "<= " + node.value;
      } else if ("builder:player.foodBelow".equals(node.type)) {
         return "food <= " + node.value;
      } else if ("builder:player.xpLevelAtLeast".equals(node.type)) {
         return "level >= " + node.value;
      } else if ("official:player.isAtLocation".equals(node.type)) {
         return node.value + " / " + node.value2;
      } else if ("official:entity.player_nearby".equals(node.type)) {
         return node.value2;
      } else if ("official:inventory.clickOpenContainerSlot".equals(node.type)) {
         return node.value + " / " + this.clickGuiButtonText(node).toLowerCase() + (this.shiftClickEnabled(node) ? " / shift" : "");
      } else if ("official:inventory.hotbarSelect".equals(node.type) || "official:inventory.hotbarUse".equals(node.type)) {
         return node.value != null && !node.value.isBlank() ? node.value : "slot " + node.value2;
      } else if ("official:inventory.dropItems".equals(node.type)) {
         String text = Boolean.parseBoolean(node.value2) ? "stack" : "single";
         if (node.value != null && !node.value.isBlank()) {
            text = node.value + " / " + text;
         }

         if (this.dropAllEnabled(node)) {
            text += " / all";
         }

         return this.withSkipText(text, node.value3);
      } else if ("official:inventory.chestWithdrawItems".equals(node.type)) {
         String text = "specific".equalsIgnoreCase(node.value) ? "specific: " + node.value2 : "all items";
         return text + (this.shiftClickEnabled(node) ? " / shift" : " / click");
      } else if ("official:inventory.chestDepositItems".equals(node.type)) {
         String text = "specific".equalsIgnoreCase(node.value) ? "specific: " + node.value2 : "all items";
         return this.withSkipText(text + (this.shiftClickEnabled(node) ? " / shift" : " / click") + (this.fastDepositEnabled(node) ? " / fast" : ""), node.value5);
      } else if ("builder:inventory.heldItemIs".equals(node.type)) {
         return node.value;
      } else if ("builder:inventory.slotHasItem".equals(node.type)) {
         return "slot " + node.value + " = " + node.value2;
      } else if ("builder:inventory.itemOrSlotHasTag".equals(node.type)) {
         return node.value + " # " + node.value2 + (this.clickMatchedTagEnabled(node) ? " / click" : "");
      } else if ("builder:inventory.itemDurability".equals(node.type)) {
         return node.value + " / " + node.value2;
      } else if ("builder:inventory.emptySlotsAtLeast".equals(node.type)) {
         return "empty >= " + node.value;
      } else if ("builder:inventory.openContainerHasItem".equals(node.type)) {
         return node.value + " x" + node.value2;
      } else if ("builder:inventory.selectHotbarSlot".equals(node.type)) {
         return "slot " + node.value;
      } else if ("builder:inventory.dropSelectedItem".equals(node.type)) {
         return Boolean.parseBoolean(node.value) ? "stack" : "single";
      } else if ("official:player.setMouseButton".equals(node.type)) {
         return this.mouseButtonStateText(node.value) + " " + this.mouseButtonButtonText(node.value2);
      } else if ("builder:player.setSprint".equals(node.type)) {
         return node.value;
      } else if ("builder:world.blockIs".equals(node.type)) {
         return node.value + " = " + node.value2;
      } else if ("builder:world.lookingAtBlock".equals(node.type)) {
         return node.value != null && !node.value.isBlank() ? node.value : "any block";
      } else if ("builder:entity.nearby".equals(node.type)) {
         return node.value + " / " + node.value2;
      } else if ("official:misc.random".equals(node.type)) {
         return node.value + "% true";
      } else if ("builder:misc.randomOutput3".equals(node.type)) {
         return "one / two / three";
      } else if ("official:misc.rejoinServer".equals(node.type)) {
         return node.value + " ms" + (node.value2 == null || node.value2.isBlank() ? "" : " -> " + node.value2);
      } else if ("official:misc.joinServer".equals(node.type)) {
         String delay = node.value2 == null || node.value2.isBlank() ? "1000" : node.value2;
         return node.value + " / " + delay + " ms";
      } else if ("official:misc.macroStart".equals(node.type) || "official:misc.macroStop".equals(node.type)) {
         return "macro " + node.value;
      } else if ("official:notification.discord".equals(node.type)) {
         return node.value2;
      } else if ("builder:event.ifChatSame".equals(node.type)) {
         return node.value + " / " + node.value2;
      } else {
         return !"official:event.chat".equals(node.type)
               && !"official:event.player_spawned".equals(node.type)
               && !"official:event.player_despawned".equals(node.type)
            ? ""
            : node.value2;
      }
   }

   private String withSkipText(String text, String slots) {
      return slots != null && !slots.isBlank() ? text + " / skip " + slots : text;
   }

   private String mouseButtonStateText(String value) {
      String text = value == null ? "" : value.trim();
      if (text.isBlank()) {
         return "release";
      } else if (this.isTruthyText(text)) {
         return "press";
      } else {
         return this.isFalseyText(text) ? "release" : text;
      }
   }

   private String mouseButtonButtonText(String value) {
      String text = value == null ? "" : value.trim();
      if (text.isBlank() || "0".equals(text) || "left".equalsIgnoreCase(text) || "attack".equalsIgnoreCase(text)) {
         return "left";
      } else {
         return "1".equals(text) || "right".equalsIgnoreCase(text) || "use".equalsIgnoreCase(text) ? "right" : text;
      }
   }

   private boolean isTruthyText(String value) {
      return "1".equals(value)
         || "true".equalsIgnoreCase(value)
         || "on".equalsIgnoreCase(value)
         || "yes".equalsIgnoreCase(value)
         || "press".equalsIgnoreCase(value)
         || "pressed".equalsIgnoreCase(value);
   }

   private boolean isFalseyText(String value) {
      return "0".equals(value)
         || "false".equalsIgnoreCase(value)
         || "off".equalsIgnoreCase(value)
         || "no".equalsIgnoreCase(value)
         || "release".equalsIgnoreCase(value)
         || "released".equalsIgnoreCase(value);
   }

   private String fieldSuggestion(MacroModel.Descriptor descriptor, boolean secondary) {
      if ("builder:world.mineArea".equals(descriptor.type())) {
         return secondary ? "0 64 0 tool=10 move=true" : "0 64 0";
      } else if ("builder:world.farmArea".equals(descriptor.type())) {
         return secondary ? "0 64 0 replant deposit move=true" : "0 64 0";
      } else if ("official:inventory.clickOpenContainerSlot".equals(descriptor.type())) {
         return secondary ? "false" : "minecraft:water_bucket $2k-$5k or slot 1";
      } else if ("builder:inventory.itemOrSlotHasTag".equals(descriptor.type())) {
         return secondary ? "Delivered or $1-10T" : "held, slot 1, or gui slots 0-53";
      } else if ("builder:inventory.itemDurability".equals(descriptor.type())) {
         return secondary ? "lower 20 or higher 80" : "held, slot 1, or gui slots 0-53";
      } else if ("builder:misc.repeatMacro".equals(descriptor.type()) || "builder:misc.repeatSection".equals(descriptor.type())) {
         return secondary ? "" : "3 or forever";
      } else if ("builder:misc.idleUntil".equals(descriptor.type())) {
         return secondary ? "optional chat text, player, or kick reason" : "chat, player, full, or kicked";
      } else if ("builder:logic.scoreboardContains".equals(descriptor.type())) {
         return secondary ? "contains, exact, or regex" : "scoreboard text";
      } else if ("builder:event.kicked".equals(descriptor.type())) {
         return secondary ? "any, allow, block, or regex" : "optional kick reason";
      } else if ("builder:flow.note".equals(descriptor.type())) {
         return secondary ? "" : "Write notes here";
      } else if ("official:misc.rejoinServer".equals(descriptor.type())) {
         return secondary ? "blank = last server, or play.example.net" : "3000";
      } else if ("official:misc.joinServer".equals(descriptor.type())) {
         return secondary ? "1000" : "play.example.net";
      } else if ("official:player.setMouseButton".equals(descriptor.type())) {
         return secondary ? "left" : "press";
      } else if ("builder:player.autoClick".equals(descriptor.type())) {
         return secondary ? "count=5 cps=8 or hold 500" : "left or right";
      } else if ("builder:player.pressKey".equals(descriptor.type())) {
         return secondary ? "tap 100 or hold 1000" : "space, shift, ctrl, alt, E";
      } else if (secondary && "builder:world.openNearestContainer".equals(descriptor.type())) {
         return "move=true type=chest";
      } else {
         return secondary || !"official:entity.attack".equals(descriptor.type()) && !"official:entity.interact".equals(descriptor.type())
            ? ""
            : "Example: minecraft:zombie";
      }
   }

   private void openItemPicker(boolean secondary) {
      this.itemPickerOpen = true;
      this.itemPickerSecondary = secondary;
      this.itemSearchFocused = true;
      this.itemPickerScroll = 0;
      this.itemSearchText = "";
      if (secondary) {
         this.secondaryField.setFocused(false);
      } else {
         this.primaryField.setFocused(false);
      }
   }

   private boolean handleItemPickerClick(MouseButtonEvent click) {
      if (click.button() != 0) {
         return true;
      } else {
         MacroBuilderScreen.ItemPickerLayout layout = this.itemPickerLayout();
         if (click.x() >= layout.searchX()
            && click.x() <= layout.searchX() + layout.searchWidth()
            && click.y() >= layout.searchY()
            && click.y() <= layout.searchY() + 16) {
            this.itemSearchFocused = true;
            return true;
         } else {
            MacroBuilderScreen.ItemEntry entry = this.itemPickerEntryAt((int)click.x(), (int)click.y());
            if (entry != null) {
               String id = entry.id().toString();
               if (this.itemPickerSecondary) {
                  this.secondaryField.setValue(id);
                  if (this.selectedNode != null) {
                     this.selectedNode.value2 = id;
                  }
               } else {
                  this.primaryField.setValue(id);
                  if (this.selectedNode != null) {
                     this.selectedNode.value = id;
                  }
               }

               this.itemPickerOpen = false;
               this.status = "Selected " + entry.displayName();
               this.statusColor = -4200769;
               return true;
            } else {
               this.itemSearchFocused = false;
               if (click.x() < layout.left()
                  || click.x() > layout.left() + layout.panelWidth()
                  || click.y() < layout.top()
                  || click.y() > layout.top() + layout.panelHeight()) {
                  this.itemPickerOpen = false;
               }

               return true;
            }
         }
      }
   }

   private MacroBuilderScreen.ItemEntry itemPickerEntryAt(int mouseX, int mouseY) {
      List<MacroBuilderScreen.ItemEntry> items = this.filteredItems();
      MacroBuilderScreen.ItemPickerLayout layout = this.itemPickerLayout();
      if (mouseX >= layout.left() + 8
         && mouseX <= layout.left() + 8 + layout.columns() * 26
         && mouseY >= layout.gridTop()
         && mouseY <= layout.gridTop() + layout.rowsVisible() * 26) {
         int col = (mouseX - (layout.left() + 8)) / 26;
         int row = (mouseY - layout.gridTop()) / 26;
         if (col >= 0 && col < layout.columns() && row >= 0 && row < layout.rowsVisible()) {
            int index = (this.itemPickerScroll + row) * layout.columns() + col;
            return index >= 0 && index < items.size() ? items.get(index) : null;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private List<MacroBuilderScreen.ItemEntry> filteredItems() {
      String filter = this.itemFilterText().toLowerCase();
      if (!filter.isBlank() && !filter.equals("minecraft:air")) {
         List<MacroBuilderScreen.ItemEntry> filtered = new ArrayList<>();

         for (MacroBuilderScreen.ItemEntry entry : this.itemEntries) {
            String id = entry.id().toString();
            if (id.contains(filter) || entry.searchText().contains(filter)) {
               filtered.add(entry);
            }
         }

         return filtered.isEmpty() ? this.itemEntries : filtered;
      } else {
         return this.itemEntries;
      }
   }

   private String itemFilterText() {
      return this.itemSearchText == null ? "" : this.itemSearchText.trim();
   }

   private int maxItemPickerScroll() {
      List<MacroBuilderScreen.ItemEntry> items = this.filteredItems();
      MacroBuilderScreen.ItemPickerLayout layout = this.itemPickerLayout();
      int totalRows = (int)Math.ceil((double)items.size() / layout.columns());
      return Math.max(0, totalRows - layout.rowsVisible());
   }

   private MacroBuilderScreen.ItemPickerLayout itemPickerLayout() {
      int availableWidth = Math.max(260, this.canvasRight() - this.canvasLeft() - 40);
      int panelWidth = Math.min(460, Math.max(330, availableWidth));
      int panelHeight = Math.min(this.height - 70, 320);
      int left = Math.max(this.canvasLeft() + 10, (this.width - panelWidth) / 2);
      int top = Math.max(48, (this.height - panelHeight) / 2);
      int searchX = left + 8;
      int searchY = top + 34;
      int searchWidth = panelWidth - 16;
      int gridTop = top + 68;
      int columns = Math.max(1, (panelWidth - 16) / 26);
      int rowsVisible = Math.max(1, (panelHeight - 78) / 26);
      return new MacroBuilderScreen.ItemPickerLayout(panelWidth, panelHeight, left, top, searchX, searchY, searchWidth, gridTop, columns, rowsVisible);
   }

   private MacroBuilderScreen.ItemEntry itemEntry(String itemId) {
      if (itemId != null && !itemId.isBlank()) {
         String normalized = MacroModel.normalizeItemId(itemId);

         for (MacroBuilderScreen.ItemEntry entry : this.itemEntries) {
            if (entry.id().toString().equals(normalized)) {
               return entry;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private boolean isItemField(String label) {
      String normalized = label == null ? "" : label.toLowerCase();
      return normalized.contains("item");
   }

   private void drawCanvasGrid(GuiGraphicsExtractor context) {
      int left = this.canvasLeft();
      int top = this.canvasTop();
      int right = this.canvasRight();
      int bottom = this.height;
      context.fill(left, top, right, bottom, -16184046);
      int minor = Math.max(8, this.scaled(24));
      int majorEvery = 4;
      int originX = this.screenX(0);
      int originY = this.screenY(0);
      int firstX = originX - (int)Math.ceil((double)(originX - left) / minor) * minor;
      int indexX = (int)Math.floor((double)(firstX - originX) / minor);

      for (int x = firstX; x < right; indexX++) {
         int color = Math.floorMod(indexX, majorEvery) == 0 ? -15063760 : -15656670;
         context.fill(x, top, x + 1, bottom, color);
         x += minor;
      }

      int firstY = originY - (int)Math.ceil((double)(originY - top) / minor) * minor;
      int indexY = (int)Math.floor((double)(firstY - originY) / minor);

      for (int y = firstY; y < bottom; indexY++) {
         int color = Math.floorMod(indexY, majorEvery) == 0 ? -15063760 : -15656670;
         context.fill(left, y, right, y + 1, color);
         y += minor;
      }
   }

   private void fillRound(GuiGraphicsExtractor context, int x1, int y1, int x2, int y2, int radius, int color) {
      if (x2 > x1 && y2 > y1) {
         int r = Math.max(0, Math.min(radius, Math.min((x2 - x1) / 2, (y2 - y1) / 2)));
         if (r <= 1) {
            context.fill(x1, y1, x2, y2, color);
         } else {
            context.fill(x1 + r, y1, x2 - r, y2, color);
            context.fill(x1, y1 + r, x2, y2 - r, color);
            context.fill(x1 + 1, y1 + 1, x1 + r, y1 + r, color);
            context.fill(x2 - r, y1 + 1, x2 - 1, y1 + r, color);
            context.fill(x1 + 1, y2 - r, x1 + r, y2 - 1, color);
            context.fill(x2 - r, y2 - r, x2 - 1, y2 - 1, color);
         }
      }
   }

   private void strokeRound(GuiGraphicsExtractor context, int x1, int y1, int x2, int y2, int radius, int color) {
      context.fill(x1 + radius, y1, x2 - radius, y1 + 1, color);
      context.fill(x1 + radius, y2 - 1, x2 - radius, y2, color);
      context.fill(x1, y1 + radius, x1 + 1, y2 - radius, color);
      context.fill(x2 - 1, y1 + radius, x2, y2 - radius, color);
      context.fill(x1 + 1, y1 + 1, x1 + radius, y1 + 2, color);
      context.fill(x2 - radius, y1 + 1, x2 - 1, y1 + 2, color);
      context.fill(x1 + 1, y2 - 2, x1 + radius, y2 - 1, color);
      context.fill(x2 - radius, y2 - 2, x2 - 1, y2 - 1, color);
   }

   private void drawSegment(GuiGraphicsExtractor context, int x1, int y1, int x2, int y2, int color) {
      this.drawSegmentWide(context, x1, y1, x2, y2, Math.max(1, this.scaled(2)), color);
   }

   private void drawSegmentWide(GuiGraphicsExtractor context, int x1, int y1, int x2, int y2, int size, int color) {
      int half = Math.max(0, size / 2);
      if (x1 == x2) {
         context.fill(x1 - half, Math.min(y1, y2), x1 + half + 1, Math.max(y1, y2) + 1, color);
      } else {
         if (y1 == y2) {
            context.fill(Math.min(x1, x2), y1 - half, Math.max(x1, x2) + 1, y1 + half + 1, color);
         }
      }
   }

   private int connectionColor(MacroModel.Node source, String output, MacroModel.Node target) {
      String color = source.connectionColor(output, target.id);
      if (color != null && !color.isBlank()) {
         try {
            return 0xFF000000 | Integer.parseInt(color.replace("#", ""), 16);
         } catch (NumberFormatException ignored) {
         }
      }

      return this.connectionColor(output);
   }

   private int connectionColor(String output) {
      if ("true".equals(output)) {
         return -11939459;
      } else if ("false".equals(output)) {
         return -24771;
      } else if ("failed".equals(output)) {
         return -2069387;
      } else if ("one".equals(output)) {
         return -11163649;
      } else if ("two".equals(output)) {
         return -11939459;
      } else if ("three".equals(output)) {
         return -24771;
      } else if ("repeat".equals(output)) {
         return -11163649;
      } else {
         return "tool_low".equals(output) ? -11930 : -11163649;
      }
   }

   private String fit(String text, int maxWidth) {
      if (text == null) {
         return "";
      } else if (this.font.width(text) <= maxWidth) {
         return text;
      } else {
         String suffix = "...";
         int suffixWidth = this.font.width(suffix);
         String trimmed = text;

         while (!trimmed.isEmpty() && this.font.width(trimmed) + suffixWidth > maxWidth) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
         }

         return trimmed + suffix;
      }
   }

   private int parseDelayMs(String text) {
      try {
         return Math.max(0, Integer.parseInt(text == null ? "" : text.trim()));
      } catch (NumberFormatException var3) {
         return 0;
      }
   }

   private int parseNodeDelayMs(String text) {
      return text != null && !text.isBlank() ? this.parseDelayMs(text) : -1;
   }

   private void drawWrappedText(GuiGraphicsExtractor context, String text, int x, int y, int maxWidth, int color) {
      this.drawWrappedText(context, text, x, y, maxWidth, color, Integer.MAX_VALUE);
   }

   private void drawWrappedText(GuiGraphicsExtractor context, String text, int x, int y, int maxWidth, int color, int maxLines) {
      int lineY = y;
      int linesDrawn = 0;

      for (String line : this.wrapText(text, maxWidth)) {
         if (linesDrawn >= maxLines) {
            break;
         }

         context.text(this.font, line, x, lineY, color);
         lineY += 10;
         linesDrawn++;
      }
   }

   private List<String> wrapText(String text, int maxWidth) {
      List<String> lines = new ArrayList<>();
      if (text != null && !text.isBlank()) {
         StringBuilder current = new StringBuilder();

         for (String word : text.trim().split("\\s+")) {
            String candidate = current.isEmpty() ? word : current + " " + word;
            if (this.font.width(candidate) <= maxWidth) {
               current.setLength(0);
               current.append(candidate);
            } else {
               if (!current.isEmpty()) {
                  lines.add(current.toString());
                  current.setLength(0);
               }

               current.append(word);
            }
         }

         if (!current.isEmpty()) {
            lines.add(current.toString());
         }

         return lines;
      } else {
         return lines;
      }
   }

   private void drawNodeText(GuiGraphicsExtractor context, String text, int x, int y, int maxWidth, int color) {
      if (text != null && !text.isBlank() && maxWidth > 0) {
         context.text(this.font, this.fit(text, maxWidth), x, y, color);
      }
   }

   private void drawNodeTextRight(GuiGraphicsExtractor context, String text, int rightX, int y, int maxWidth, int color) {
      if (text != null && !text.isBlank() && maxWidth > 0) {
         String fitted = this.fit(text, maxWidth);
         context.text(this.font, fitted, rightX - this.font.width(fitted), y, color);
      }
   }

   private static int clamp(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
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

   private int maxPaletteScroll() {
      int contentHeight = 0;

      for (MacroBuilderScreen.PaletteRow row : this.paletteRows()) {
         contentHeight += this.paletteRowHeight(row);
      }

      int visibleHeight = Math.max(1, this.height - this.paletteListTop());
      return Math.max(0, contentHeight - visibleHeight);
   }

   private List<MacroBuilderScreen.PaletteRow> paletteRows() {
      List<MacroBuilderScreen.PaletteRow> rows = new ArrayList<>();
      Map<String, List<MacroModel.Descriptor>> grouped = new LinkedHashMap<>();
      String filter = this.componentSearchText();

      for (MacroModel.Descriptor descriptor : MacroModel.paletteDescriptors()) {
         if (this.componentMatches(descriptor, filter)) {
            grouped.computeIfAbsent(descriptor.group(), groupx -> new ArrayList<>()).add(descriptor);
         }
      }

      for (Entry<String, List<MacroModel.Descriptor>> entry : grouped.entrySet()) {
         String group = entry.getKey();
         this.expandedGroups.putIfAbsent(group, false);
         rows.add(new MacroBuilderScreen.PaletteRow(group, null));
         if (!filter.isBlank() || this.expandedGroups.getOrDefault(group, false)) {
            for (MacroModel.Descriptor descriptorx : entry.getValue()) {
               rows.add(new MacroBuilderScreen.PaletteRow(null, descriptorx));
            }
         }
      }

      return rows;
   }

   private String componentSearchText() {
      return this.componentSearchField != null && this.componentSearchField.getValue() != null ? this.componentSearchField.getValue().trim().toLowerCase() : "";
   }

   private boolean componentMatches(MacroModel.Descriptor descriptor, String filter) {
      return filter != null && !filter.isBlank()
         ? descriptor.label().toLowerCase().contains(filter)
            || descriptor.group().toLowerCase().contains(filter)
            || descriptor.description().toLowerCase().contains(filter)
            || descriptor.type().toLowerCase().contains(filter)
         : true;
   }

   private int paletteListTop() {
      return 90;
   }

   private int paletteRowHeight(MacroBuilderScreen.PaletteRow row) {
      if (row.group() != null) {
         return 22;
      } else {
         MacroModel.Descriptor descriptor = row.descriptor();
         if (descriptor == null) {
            return 34;
         } else {
            int descriptionLines = Math.max(1, this.wrapText(descriptor.description(), this.paletteTextWidth()).size());
            return Math.max(34, 24 + descriptionLines * 10);
         }
      }
   }

   private int screenX(int worldX) {
      return this.canvasLeft() + (int)Math.round((worldX + this.panX) * this.zoom);
   }

   private int screenX(double worldX) {
      return this.canvasLeft() + (int)Math.round((worldX + this.panX) * this.zoom);
   }

   private int screenY(int worldY) {
      return this.canvasTop() + (int)Math.round((worldY + this.panY) * this.zoom);
   }

   private double worldX(double screenX) {
      return (screenX - this.canvasLeft()) / this.zoom - this.panX;
   }

   private double worldY(double screenY) {
      return (screenY - this.canvasTop()) / this.zoom - this.panY;
   }

   private int scaled(int value) {
      return Math.max(1, (int)Math.round(value * this.zoom));
   }

   private int nodeScreenWidth() {
      return Math.max(8, this.scaled(NODE_WIDTH));
   }

   private int nodeScreenHeight() {
      return Math.max(8, this.scaled(NODE_HEIGHT));
   }

   private int nodeScreenWidth(MacroModel.Node node) {
      return Math.max(8, this.scaled(this.nodeWorldWidth(node)));
   }

   private int nodeScreenHeight(MacroModel.Node node) {
      return Math.max(8, this.scaled(this.nodeWorldHeight(node)));
   }

   private int nodeScreenWidth(String type) {
      return Math.max(8, this.scaled(this.nodeWorldWidth(type)));
   }

   private int nodeScreenHeight(String type) {
      return Math.max(8, this.scaled(this.nodeWorldHeight(type)));
   }

   private int nodeWorldWidth(MacroModel.Node node) {
      return node == null ? NODE_WIDTH : (this.isNoteNode(node) ? Math.max(NOTE_MIN_WIDTH, node.noteWidth > 0 ? node.noteWidth : NOTE_WIDTH) : this.nodeWorldWidth(node.type));
   }

   private int nodeWorldHeight(MacroModel.Node node) {
      return node == null ? NODE_HEIGHT : (this.isNoteNode(node) ? Math.max(NOTE_MIN_HEIGHT, node.noteHeight > 0 ? node.noteHeight : NOTE_HEIGHT) : this.nodeWorldHeight(node.type));
   }

   private int nodeWorldWidth(String type) {
      return MacroModel.BUILDER_NOTE.equals(type) ? NOTE_WIDTH : NODE_WIDTH;
   }

   private int nodeWorldHeight(String type) {
      return MacroModel.BUILDER_NOTE.equals(type) ? NOTE_HEIGHT : NODE_HEIGHT;
   }

   private boolean isNoteNode(MacroModel.Node node) {
      return node != null && MacroModel.BUILDER_NOTE.equals(node.type);
   }

   private String groupIcon(String group) {
      return switch (group) {
         case "Player" -> "P";
         case "Logic" -> "L";
         case "Flow" -> "F";
         case "Action" -> "A";
         case "Inventory" -> "I";
         case "World" -> "W";
         case "Entity" -> "E";
         case "Misc" -> "M";
         case "Notification" -> "N";
         case "Login" -> "K";
         case "Event" -> "!";
         default -> "*";
      };
   }

   private int sectionColor(String group) {
      return switch (group) {
         case "Entry" -> -11961857;
         case "Player" -> -6765703;
         case "Logic" -> -1726398;
         case "Flow" -> -11094334;
         case "Action" -> -10375185;
         case "Inventory" -> -3040666;
         case "World" -> -11684180;
         case "Entity" -> -3770147;
         case "Misc" -> -7366491;
         case "Notification" -> -10983950;
         case "Login" -> -4794025;
         case "Event" -> -9848833;
         default -> -7366491;
      };
   }

   private int sectionHeaderColor(String group) {
      int color = this.sectionColor(group);
      int red = color >> 16 & 0xFF;
      int green = color >> 8 & 0xFF;
      int blue = color & 0xFF;
      red = (red + 48) / 3;
      green = (green + 48) / 3;
      blue = (blue + 48) / 3;
      return 0xFF000000 | red << 16 | green << 8 | blue;
   }

   private static double clampDouble(double value, double min, double max) {
      return Math.max(min, Math.min(max, value));
   }

   private enum NoteResizeCorner {
      TOP_LEFT,
      TOP_RIGHT,
      BOTTOM_LEFT,
      BOTTOM_RIGHT
   }

   private record CopiedComponent(
      String originalId,
      String type,
      String value,
      String value2,
      String value3,
      String value4,
      String value5,
      int delayMs,
      boolean enabled,
      int x,
      int y,
      int noteWidth,
      int noteHeight,
      Map<String, String> outputs,
      Map<String, String> routes,
      Map<String, String> colors
   ) {
   }

   private record ItemEntry(Identifier id, Item item) {
      ItemStack stack() {
         try {
            return this.item.getDefaultInstance();
         } catch (RuntimeException exception) {
            return ItemStack.EMPTY;
         }
      }

      String displayName() {
         ItemStack stack = this.stack();
         if (!stack.isEmpty()) {
            try {
               return stack.getHoverName().getString();
            } catch (RuntimeException exception) {
            }
         }

         return this.id.toString();
      }

      String searchText() {
         return (this.id.toString() + " " + this.item.getDescriptionId()).toLowerCase();
      }
   }

   private record ItemPickerLayout(
      int panelWidth, int panelHeight, int left, int top, int searchX, int searchY, int searchWidth, int gridTop, int columns, int rowsVisible
   ) {
   }

   private record PaletteHit(String type, String group) {
   }

   private record PaletteRow(String group, MacroModel.Descriptor descriptor) {
   }

   private record ConnectionHit(MacroModel.Node source, String output, MacroModel.Node target) {
   }
}
