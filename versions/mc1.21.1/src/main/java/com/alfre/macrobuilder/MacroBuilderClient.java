package com.alfre.macrobuilder;

import com.mojang.blaze3d.platform.InputConstants.Type;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents.Chat;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents.Game;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MacroBuilderClient implements ClientModInitializer {
   public static final String MOD_ID = "macro_builder";
   static final Logger LOGGER = LoggerFactory.getLogger("macro_builder");
   static final MacroRunner RUNNER = new MacroRunner();
   private static final String KEY_CATEGORY = "key.categories.macro_builder";
   private static KeyMapping openKey;
   private static boolean openKeyWasDown;

   public void onInitializeClient() {
      RUNNER.loadOrCreate();
      registerKeybind();
      registerScreenEvents();
      registerCommands();
      registerMessageEvents();
      registerConnectionEvents();
      registerHudOverlay();
      ClientTickEvents.END_CLIENT_TICK.register(RUNNER::tick);
   }

   private static void registerHudOverlay() {
      HudRenderCallback.EVENT.register((context, tickCounter) -> renderMacroOverlay(context, Minecraft.getInstance()));
   }

   private static void renderMacroOverlay(GuiGraphics context, Minecraft client) {
      if (client == null || client.player == null || client.level == null || !RUNNER.running()) {
         return;
      }

      if (client.screen instanceof MacroListScreen || client.screen instanceof MacroBuilderScreen || client.screen instanceof MacroMarketplaceScreen) {
         return;
      }

      List<MacroRunner.OverlaySnapshot> snapshots = RUNNER.overlaySnapshots();
      if (snapshots.isEmpty()) {
         return;
      }

      int visibleRows = Math.min(4, snapshots.size());
      int panelWidth = 164;
      int panelHeight = 20 + visibleRows * 27 + (snapshots.size() > visibleRows ? 10 : 0);
      int x = Math.max(6, client.getWindow().getGuiScaledWidth() - panelWidth - 8);
      int y = 8;
      context.fill(x, y, x + panelWidth, y + panelHeight, 0xDD101820);
      context.fill(x, y, x + panelWidth, y + 1, 0xFF4A6A82);
      context.fill(x, y + panelHeight - 1, x + panelWidth, y + panelHeight, 0xFF0A0F14);
      context.fill(x, y, x + 1, y + panelHeight, 0xFF4A6A82);
      context.fill(x + panelWidth - 1, y, x + panelWidth, y + panelHeight, 0xFF0A0F14);
      context.drawString(client.font, "Litemacro Running", x + 8, y + 6, 0xFFEFF6FF);

      int rowY = y + 20;
      for (int index = 0; index < visibleRows; index++) {
         MacroRunner.OverlaySnapshot snapshot = snapshots.get(index);
         int accent = index % 2 == 0 ? 0xFF22D3EE : 0xFF35E887;
         context.fill(x + 7, rowY + 2, x + 9, rowY + 23, accent);
         context.drawString(client.font, fitOverlay(client, "#" + snapshot.macroNumber() + " " + snapshot.macroName(), panelWidth - 24), x + 14, rowY + 2, 0xFFEFF6FF);
         String detail = snapshot.componentName() + "  " + Math.max(0, snapshot.ticks() / 20) + "s";
         context.drawString(client.font, fitOverlay(client, detail, panelWidth - 24), x + 14, rowY + 14, 0xFF9BAFC2);
         rowY += 27;
      }

      if (snapshots.size() > visibleRows) {
         context.drawString(client.font, "+" + (snapshots.size() - visibleRows) + " more", x + 14, y + panelHeight - 10, 0xFF6F8192);
      }
   }

   private static String fitOverlay(Minecraft client, String text, int maxWidth) {
      if (text == null || maxWidth <= 0 || client.font.width(text) <= maxWidth) {
         return text == null ? "" : text;
      }

      String suffix = "...";
      String value = text;
      while (!value.isEmpty() && client.font.width(value) + client.font.width(suffix) > maxWidth) {
         value = value.substring(0, value.length() - 1);
      }

      return value.isEmpty() ? "" : value + suffix;
   }

   private static void registerKeybind() {
      openKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.macro_builder.open", Type.KEYSYM, 66, KEY_CATEGORY));
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> {
         boolean opened = false;
         while (openKey.consumeClick()) {
            openMacroList(client);
            opened = true;
         }

         boolean down = openKey.isDown();
         if (!opened && down && !openKeyWasDown) {
            openMacroList(client);
         }

         openKeyWasDown = down;
      });
   }

   private static void registerScreenEvents() {
      ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
         if (!isNonLitemacroMenu(client, screen)) {
            return;
         }

         ScreenKeyboardEvents.afterKeyPress(screen).register((currentScreen, key, scanCode, modifiers) -> {
            handleMenuKeyPress(client, key, scanCode);
         });

         if (screen instanceof TitleScreen) {
            Screens.getButtons(screen)
               .add(Button.builder(Component.literal("Litemacro"), button -> openMacroList(client, true)).bounds(scaledWidth - 104, 8, 96, 20).build());
         }
      });
   }

   private static void handleMenuKeyPress(Minecraft client, int key, int scanCode) {
      if (openKey != null && openKey.matches(key, scanCode)) {
         openMacroList(client, true);
      } else {
         RUNNER.handleMacroHotkeyPress(client, key);
      }
   }

   private static boolean canOpenMacroList(Minecraft client) {
      if (client == null || client.screen instanceof MacroListScreen || client.screen instanceof MacroBuilderScreen) {
         return false;
      }

      return client.screen == null || client.player == null || client.level == null;
   }

   private static boolean isNonLitemacroMenu(Minecraft client, Screen screen) {
      if (client == null || screen == null || screen instanceof MacroListScreen || screen instanceof MacroBuilderScreen) {
         return false;
      }

      return client.player == null || client.level == null || screen instanceof TitleScreen;
   }

   private static void openMacroList(Minecraft client) {
      openMacroList(client, false);
   }

   private static void openMacroList(Minecraft client, boolean force) {
      if ((force && client != null && !(client.screen instanceof MacroListScreen) && !(client.screen instanceof MacroBuilderScreen)) || canOpenMacroList(client)) {
         client.setScreen(new MacroListScreen(RUNNER));
      }
   }

   private static void registerCommands() {
      ClientCommandRegistrationCallback.EVENT.register((ClientCommandRegistrationCallback)(dispatcher, registryAccess) -> {
         dispatcher.register(commandRoot("litemacro"));
         dispatcher.register(commandRoot("lm"));
         dispatcher.register(commandRoot("macrobuilder"));
         dispatcher.register(commandRoot("macro_builder"));
      });
   }

   private static void registerConnectionEvents() {
      ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
         String reason = "Disconnected";
         if (handler != null && handler.getConnection() != null) {
            DisconnectionDetails details = handler.getConnection().getDisconnectionDetails();
            if (details != null && details.reason() != null) {
               String text = details.reason().getString();
               if (!text.isBlank()) {
                  reason = text;
               }
            }
         }

         RUNNER.recordDisconnect(reason);
      });
   }

   private static LiteralArgumentBuilder<FabricClientCommandSource> commandRoot(String commandName) {
      return (LiteralArgumentBuilder<FabricClientCommandSource>)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)ClientCommandManager.literal(
                                                                  commandName
                                                               )
                                                               .executes(context -> openMacroScreen()))
                                                            .then(ClientCommandManager.literal("help").executes(context -> showCommandHelp())))
                                                         .then(ClientCommandManager.literal("list").executes(context -> showCommandHelp())))
                                                      .then(ClientCommandManager.literal("open").executes(context -> openMacroScreen())))
                                                   .then(ClientCommandManager.literal("macro1").executes(context -> selectMacro(1))))
                                                .then(ClientCommandManager.literal("macro2").executes(context -> selectMacro(2))))
                                             .then(
                                                ClientCommandManager.literal("macro")
                                                   .then(
                                                      ClientCommandManager.argument("number", IntegerArgumentType.integer(1, 100))
                                                         .executes(context -> selectMacro(IntegerArgumentType.getInteger(context, "number")))
                                                   )
                                             ))
                                          .then(
                                             ClientCommandManager.literal("select")
                                                .then(
                                                   ClientCommandManager.argument("number", IntegerArgumentType.integer(1, 100))
                                                      .executes(context -> selectMacro(IntegerArgumentType.getInteger(context, "number")))
                                                )
                                          ))
                                       .then(
                                          ClientCommandManager.literal("run")
                                             .then(
                                                ClientCommandManager.argument("number", IntegerArgumentType.integer(1, 100))
                                                   .executes(context -> toggleMacro(IntegerArgumentType.getInteger(context, "number")))
                                             )
                                       ))
                                    .then(
                                       ClientCommandManager.literal("start")
                                          .then(
                                             ClientCommandManager.argument("number", IntegerArgumentType.integer(1, 100))
                                                .executes(context -> startMacro(IntegerArgumentType.getInteger(context, "number")))
                                          )
                                    ))
                                 .then(ClientCommandManager.literal("resume").executes(context -> resumeMacro())))
                              .then(ClientCommandManager.literal("stop").executes(context -> stopMacro())))
                           .then(ClientCommandManager.literal("reload").executes(context -> reloadMacro())))
                        .then(
                           ClientCommandManager.literal("copy")
                              .then(
                                 ClientCommandManager.argument("number", IntegerArgumentType.integer(1, 100))
                                    .executes(context -> copyMacro(IntegerArgumentType.getInteger(context, "number")))
                              )
                        ))
                     .then(
                        ClientCommandManager.literal("paste")
                           .then(
                              ClientCommandManager.argument("number", IntegerArgumentType.integer(1, 100))
                                 .executes(context -> pasteMacro(IntegerArgumentType.getInteger(context, "number")))
                           )
                     ))
                  .then(
                     ClientCommandManager.literal("reset")
                        .then(
                           ClientCommandManager.argument("number", IntegerArgumentType.integer(1, 100))
                              .executes(context -> resetMacro(IntegerArgumentType.getInteger(context, "number")))
                        )
                  ))
               .then(
                  ClientCommandManager.literal("export")
                     .then(
                        ClientCommandManager.argument("number", IntegerArgumentType.integer(1, 100))
                           .executes(context -> exportMacro(IntegerArgumentType.getInteger(context, "number")))
                     )
               ))
            .then(
               ClientCommandManager.literal("import")
                  .then(
                     ClientCommandManager.argument("number", IntegerArgumentType.integer(1, 100))
                        .executes(context -> importMacro(IntegerArgumentType.getInteger(context, "number")))
                  )
            ))
         .then(
            ((LiteralArgumentBuilder)((LiteralArgumentBuilder)ClientCommandManager.literal("chat")
                     .then(ClientCommandManager.literal("on").executes(context -> setChatNotifications(true))))
                  .then(ClientCommandManager.literal("off").executes(context -> setChatNotifications(false))))
               .then(ClientCommandManager.literal("toggle").executes(context -> toggleChatNotifications()))
         );
   }

   private static int openMacroScreen() {
      Minecraft client = Minecraft.getInstance();
      client.execute(() -> openMacroList(client, true));
      return 1;
   }

   private static int selectMacro(int number) {
      RUNNER.selectMacro(Minecraft.getInstance(), number);
      return 1;
   }

   private static int toggleMacro(int number) {
      RUNNER.toggleMacro(Minecraft.getInstance(), number);
      return 1;
   }

   private static int startMacro(int number) {
      Minecraft client = Minecraft.getInstance();
      RUNNER.selectMacro(client, number);
      RUNNER.resume(client, RUNNER.model());
      return 1;
   }

   private static int resumeMacro() {
      RUNNER.resume(Minecraft.getInstance(), RUNNER.model());
      return 1;
   }

   private static int stopMacro() {
      RUNNER.stop(Minecraft.getInstance(), "Stopped.");
      return 1;
   }

   private static int reloadMacro() {
      RUNNER.reload(Minecraft.getInstance());
      return 1;
   }

   private static int copyMacro(int number) {
      RUNNER.copyMacro(Minecraft.getInstance(), number);
      return 1;
   }

   private static int pasteMacro(int number) {
      RUNNER.pasteCopiedMacro(Minecraft.getInstance(), number);
      return 1;
   }

   private static int resetMacro(int number) {
      RUNNER.resetMacro(Minecraft.getInstance(), number);
      return 1;
   }

   private static int exportMacro(int number) {
      RUNNER.exportMacroToDownloads(Minecraft.getInstance(), number);
      return 1;
   }

   private static int importMacro(int number) {
      RUNNER.importNewestMacroFromDownloads(Minecraft.getInstance(), number);
      return 1;
   }

   private static int setChatNotifications(boolean enabled) {
      RUNNER.setChatNotifications(enabled);
      sendClientMessage(enabled ? "Chat notifications on." : "Chat notifications off.");
      return 1;
   }

   private static int toggleChatNotifications() {
      RUNNER.toggleChatNotifications();
      sendClientMessage("Chat notifications toggled.");
      return 1;
   }

   private static int showCommandHelp() {
      sendClientMessage("Commands: /litemacro open, help, macro <1-100>, run <1-100>, start <1-100>, stop, reload.");
      sendClientMessage("More: /litemacro copy <n>, paste <n>, reset <n>, export <n>, import <n>, chat on/off/toggle.");
      sendClientMessage("Aliases: /lm, /litemacro, /macrobuilder, /macro_builder.");
      return 1;
   }

   private static void sendClientMessage(String message) {
      Minecraft client = Minecraft.getInstance();
      if (client.player != null) {
         client.player.displayClientMessage(Component.literal("[Litemacro] " + message), false);
      }
   }

   private static void registerMessageEvents() {
      ClientReceiveMessageEvents.CHAT
         .register((Chat)(message, signedMessage, sender, params, receptionTimestamp) -> RUNNER.recordChatMessage(message.getString()));
      ClientReceiveMessageEvents.GAME.register((Game)(message, overlay) -> RUNNER.recordChatMessage(message.getString()));
   }
}
