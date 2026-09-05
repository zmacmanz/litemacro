package com.alfre.macrobuilder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.InputConstants;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

final class MacroRunner {
   private static final int PLAYER_INVENTORY_SLOT_COUNT = 36;
   private static final int SCREEN_TIMEOUT_TICKS = 100;
   private static final int CLOSE_SETTLE_TICKS = 4;
   private static final int BOW_DRAW_TICKS = 25;
   private static final int ENTITY_REACH_DISTANCE = 3;
   private static final int TELEPORT_DISTANCE_SQUARED = 64;
   private static final int CHAT_MESSAGE_LIMIT = 256;
   private static final Pattern PRICE_RANGE_PATTERN = Pattern.compile(
      "\\$?\\s*([0-9][0-9,]*(?:\\.[0-9]+)?)\\s*([kmbt]?)\\s*(?:-|\\.\\.|to)\\s*\\$?\\s*([0-9][0-9,]*(?:\\.[0-9]+)?)\\s*([kmbt]?)",
      Pattern.CASE_INSENSITIVE
   );
   private static final Pattern PRICE_PATTERN = Pattern.compile("\\$\\s*([0-9][0-9,]*(?:\\.[0-9]+)?)\\s*([kmbt]?)", Pattern.CASE_INSENSITIVE);
   private static final HttpClient DISCORD_HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8L)).build();
   static final int MACRO_COUNT = 100;
   private static final String DEFAULT_EXAMPLE_EXPORT = "/macro_builder/default_macro2_export.json";
   private final Path configDirectory = FabricLoader.getInstance().getConfigDir().resolve("litemacro");
   private final Path previousBrandConfigDirectory = FabricLoader.getInstance().getConfigDir().resolve("litemarco");
   private final Path legacyConfigDirectory = FabricLoader.getInstance().getConfigDir().resolve("minebot-macro-builder");
   private final Path settingsPath = this.configDirectory.resolve("settings.json");
   private final boolean childRunner;
   private final MacroRunner parentRunner;
   private final List<MacroRunner> childRunners = new ArrayList<>();
   private final List<MacroRunner> pendingChildRunners = new ArrayList<>();
   private final boolean[] macroKeyWasDown = new boolean[100];
   private MacroModel model;
   private MacroBuilderSettings settings = new MacroBuilderSettings();
   private String copiedMacroJson;
   private String copiedMacroLabel = "";
   private int copiedMacroNumber;
   private int currentMacroNumber = 1;
   private boolean running;
   private String currentNodeId;
   private String activeNodeId;
   private int activeTicks;
   private int stepDelayTicksRemaining;
   private String lastStatus = "Stopped";
   private float lastHealth = Float.NaN;
   private Vec3 lastPlayerPos;
   private String lastWorldId = "";
   private final Set<UUID> knownPlayerIds = new HashSet<>();
   private final Map<UUID, String> knownPlayerNames = new HashMap<>();
   private long chatSequence;
   private long damageSequence;
   private long teleportSequence;
   private long playerSpawnSequence;
   private long playerDespawnSequence;
   private long kickedSequence;
   private long scheduleMinute = -1L;
   private long nodeChatBaseline;
   private long nodeDamageBaseline;
   private long nodeTeleportBaseline;
   private long nodeSpawnBaseline;
   private long nodeDespawnBaseline;
   private long nodeKickedBaseline;
   private String lastChatMessage = "";
   private String lastPlayerSpawnName = "";
   private String lastPlayerDespawnName = "";
   private String lastKickReason = "";
   private CompletableFuture<MacroRunner.DiscordResult> activeDiscordRequest;
   private String activeDiscordNodeId;
   private BlockPos activeFarmCropPos;
   private String activeFarmSeedItem = "";
   private boolean activeFarmHarvested;
   private BlockPos activeMineAreaPos;
   private int activeMineAreaTicks;
   private BlockPos activeJumpPlaceSupportPos;
   private String lastOpenContainerTagFilter = "";
   private String lastOpenContainerTagNextNodeId = "";
   private final Map<String, Integer> repeatCounters = new HashMap<>();
   private boolean waitingForWorld;
   private long autoStartChatBaseline;
   private long autoStartKickedBaseline;
   private ServerData lastServerData;
   private ServerData pendingRejoinServer;
   private String pendingRejoinNodeId = "";
   private String pendingRejoinLabel = "Rejoin Server";
   private int pendingRejoinDelayTicks;
   private int pendingRejoinConnectTicks;
   private boolean pendingRejoinConnecting;
   private boolean autoReconnectAttempted;
   private boolean wasWorldAvailable;
   private InputConstants.Key activePressedKey;

   MacroRunner() {
      this(false, null);
   }

   private MacroRunner(boolean childRunner, MacroRunner parentRunner) {
      this.childRunner = childRunner;
      this.parentRunner = parentRunner;
   }

   void loadOrCreate() {
      try {
         this.ensureConfigFiles();
         this.settings = MacroBuilderSettings.load(this.settingsPath);
         this.model = MacroModel.load(this.macroPath(this.currentMacroNumber));
      } catch (RuntimeException | IOException var2) {
         MacroBuilderClient.LOGGER.error("Failed to load Litemacro file", var2);
         this.model = MacroModel.createDefault();
         this.lastStatus = "Load failed: " + var2.getMessage();
      }
   }

   MacroModel model() {
      if (this.model == null) {
         this.loadOrCreate();
      }

      return this.model;
   }

   Path macroPath() {
      return this.macroPath(this.currentMacroNumber);
   }

   Path macroFilePath(int macroNumber) {
      return this.macroPath(Math.max(1, Math.min(100, macroNumber)));
   }

   int currentMacroNumber() {
      return this.currentMacroNumber;
   }

   int macroCount() {
      return 100;
   }

   boolean chatNotifications() {
      return this.settings.chatNotifications();
   }

   void setChatNotifications(boolean enabled) {
      this.settings.setChatNotifications(enabled);
      this.saveSettings();
      this.lastStatus = enabled ? "Chat notifications on" : "Chat notifications off";
   }

   void toggleChatNotifications() {
      this.setChatNotifications(!this.settings.chatNotifications());
   }

   int macroKey(int macroNumber) {
      return this.settings.macroKey(macroNumber);
   }

   void setMacroKey(int macroNumber, int keyCode) {
      this.settings.setMacroKey(macroNumber, keyCode);
      this.saveSettings();
      this.lastStatus = "Set Macro " + macroNumber + " key to " + keyName(keyCode);
   }

   void clearMacroKey(int macroNumber) {
      this.settings.setMacroKey(macroNumber, -1);
      this.saveSettings();
      this.lastStatus = "Cleared Macro " + macroNumber + " key";
   }

   String macroKeyName(int macroNumber) {
      return keyName(this.settings.macroKey(macroNumber));
   }

   void selectMacro(Minecraft client, int macroNumber) {
      int clamped = Math.max(1, Math.min(100, macroNumber));
      this.currentMacroNumber = clamped;
      this.repeatCounters.clear();

      try {
         this.ensureConfigFiles();
         this.model = MacroModel.load(this.macroPath(this.currentMacroNumber));
         this.lastStatus = "Selected Macro " + this.currentMacroNumber;
      } catch (RuntimeException | IOException var5) {
         MacroBuilderClient.LOGGER.error("Failed to select macro {}", this.currentMacroNumber, var5);
         this.model = MacroModel.createDefault();
         this.model.setName("Macro " + this.currentMacroNumber);
         this.lastStatus = "Macro " + this.currentMacroNumber + " failed to load: " + var5.getMessage();
         this.send(client, this.lastStatus);
      }
   }

   void toggleMacro(Minecraft client, int macroNumber) {
      int clamped = Math.max(1, Math.min(100, macroNumber));
      if (this.stopMacroRuns(client, clamped, "Stopped Macro " + clamped + ".")) {
         this.lastStatus = "Stopped Macro " + clamped + ".";
      } else {
         this.startMacroSlot(client, clamped);
      }
   }

   String macroName(int macroNumber) {
      int clamped = Math.max(1, Math.min(100, macroNumber));

      try {
         this.ensureConfigFiles();
         Path path = this.macroPath(clamped);
         if (Files.exists(path)) {
            return MacroModel.load(path).name();
         }
      } catch (RuntimeException | IOException var4) {
         MacroBuilderClient.LOGGER.warn("Failed to read macro {} name", clamped, var4);
      }

      return "Macro " + clamped;
   }

   void save(MacroModel model) throws IOException {
      this.model = model;
      this.ensureConfigFiles();
      model.save(this.macroPath(this.currentMacroNumber));
      this.replaceActiveModel(this.currentMacroNumber, model);
      this.lastStatus = "Saved Macro " + this.currentMacroNumber;
   }

   void resetMacro(Minecraft client, int macroNumber) {
      int clamped = Math.max(1, Math.min(100, macroNumber));

      try {
         this.ensureConfigFiles();
         this.stopMacroRuns(client, clamped, "Stopped before resetting macro.");

         MacroModel reset = MacroModel.createDefault();
         reset.setName("Macro " + clamped);
         reset.save(this.macroPath(clamped));
         this.settings.setMacroKey(clamped, -1);
         this.saveSettings();
         if (this.currentMacroNumber == clamped) {
            this.model = reset;
         }

         this.lastStatus = "Reset Macro " + clamped + " to new";
         this.send(client, this.lastStatus);
      } catch (RuntimeException | IOException var5) {
         MacroBuilderClient.LOGGER.error("Failed to reset macro {}", clamped, var5);
         this.lastStatus = "Reset failed: " + var5.getMessage();
         this.send(client, this.lastStatus);
      }
   }

   void copyMacro(Minecraft client, int macroNumber) {
      int clamped = Math.max(1, Math.min(100, macroNumber));

      try {
         this.ensureConfigFiles();
         Path path = this.macroPath(clamped);
         this.copiedMacroJson = Files.readString(path);
         this.copiedMacroLabel = MacroModel.load(path).name();
         this.copiedMacroNumber = clamped;
         this.lastStatus = "Copied Macro " + clamped + ": " + this.copiedMacroLabel;
         this.send(client, this.lastStatus);
      } catch (RuntimeException | IOException var5) {
         MacroBuilderClient.LOGGER.error("Failed to copy macro {}", clamped, var5);
         this.lastStatus = "Copy failed: " + var5.getMessage();
         this.send(client, this.lastStatus);
      }
   }

   void pasteCopiedMacro(Minecraft client, int macroNumber) {
      int clamped = Math.max(1, Math.min(100, macroNumber));
      if (this.copiedMacroJson != null && !this.copiedMacroJson.isBlank()) {
         try {
            this.ensureConfigFiles();
            this.stopMacroRuns(client, clamped, "Stopped before pasting macro.");

            Path target = this.macroPath(clamped);
            Files.writeString(target, this.copiedMacroJson);
            MacroModel pasted = MacroModel.load(target);
            pasted.save(target);
            if (this.currentMacroNumber == clamped) {
               this.model = pasted;
            }

            this.lastStatus = "Pasted Macro " + this.copiedMacroNumber + " into Macro " + clamped;
            this.send(client, this.lastStatus);
         } catch (RuntimeException | IOException var6) {
            MacroBuilderClient.LOGGER.error("Failed to paste macro into {}", clamped, var6);
            this.lastStatus = "Paste failed: " + var6.getMessage();
            this.send(client, this.lastStatus);
         }
      } else {
         this.lastStatus = "No copied macro to paste.";
         this.send(client, this.lastStatus);
      }
   }

   void exportMacroToDownloads(Minecraft client, int macroNumber) {
      int clamped = Math.max(1, Math.min(100, macroNumber));

      try {
         this.ensureConfigFiles();
         Path downloads = this.downloadsDirectory();
         Files.createDirectories(downloads);
         Path target = downloads.resolve("Litemacro-Macro-" + clamped + ".json");
         Files.copy(this.macroPath(clamped), target, StandardCopyOption.REPLACE_EXISTING);
         this.lastStatus = "Downloaded Macro " + clamped + " to " + target;
         this.send(client, "Downloaded Macro " + clamped + " to Downloads.");
      } catch (RuntimeException | IOException var6) {
         MacroBuilderClient.LOGGER.error("Failed to download macro {}", clamped, var6);
         this.lastStatus = "Download failed: " + var6.getMessage();
         this.send(client, this.lastStatus);
      }
   }

   void importNewestMacroFromDownloads(Minecraft client, int macroNumber) {
      try {
         this.importMacroFromDownloads(client, macroNumber, this.newestJsonInDownloads());
      } catch (RuntimeException | IOException var4) {
         MacroBuilderClient.LOGGER.error("Failed to import newest macro into {}", macroNumber, var4);
         this.lastStatus = "Import failed: " + var4.getMessage();
         this.send(client, this.lastStatus);
      }
   }

   void importMacroFromDownloads(Minecraft client, int macroNumber, Path source) {
      int clamped = Math.max(1, Math.min(100, macroNumber));

      try {
         this.ensureConfigFiles();
         MacroModel imported = this.readMacroImport(source, clamped);
         this.stopMacroRuns(client, clamped, "Stopped before importing macro.");

         Path target = this.macroPath(clamped);
         imported.save(target);
         if (this.currentMacroNumber == clamped) {
            this.model = imported;
         }

         this.lastStatus = "Imported " + source.getFileName() + " into Macro " + clamped;
         this.send(client, this.lastStatus);
      } catch (RuntimeException | IOException var7) {
         MacroBuilderClient.LOGGER.error("Failed to import macro into {}", clamped, var7);
         this.lastStatus = "Import failed: " + var7.getMessage();
         this.send(client, this.lastStatus);
      }
   }

   void uploadMacroToMarketplace(Minecraft client, int macroNumber) {
      int clamped = Math.max(1, Math.min(100, macroNumber));

      try {
         this.ensureConfigFiles();
         Path marketplace = this.marketplaceDirectory();
         Files.createDirectories(marketplace);
         MacroModel uploaded = MacroModel.load(this.macroPath(clamped));
         Path target = marketplace.resolve("Litemacro-Macro-" + clamped + "-" + this.safeMarketplaceName(uploaded.name()) + ".json");
         Files.copy(this.macroPath(clamped), target, StandardCopyOption.REPLACE_EXISTING);
         this.lastStatus = "Uploaded Macro " + clamped + " to Marketplace";
         this.send(client, "Uploaded Macro " + clamped + " to Marketplace.");
      } catch (RuntimeException | IOException var7) {
         MacroBuilderClient.LOGGER.error("Failed to upload macro {}", clamped, var7);
         this.lastStatus = "Marketplace upload failed: " + var7.getMessage();
         this.send(client, this.lastStatus);
      }
   }

   void exportMarketplaceMacroToDownloads(Minecraft client, Path source) {
      try {
         Files.createDirectories(this.downloadsDirectory());
         Path target = this.downloadsDirectory().resolve(source.getFileName().toString());
         Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
         this.lastStatus = "Downloaded " + source.getFileName() + " from Marketplace";
         this.send(client, "Downloaded Marketplace macro to Downloads.");
      } catch (RuntimeException | IOException var4) {
         MacroBuilderClient.LOGGER.error("Failed to download marketplace macro", var4);
         this.lastStatus = "Marketplace download failed: " + var4.getMessage();
         this.send(client, this.lastStatus);
      }
   }

   void reload(Minecraft client) {
      this.stopMacroRuns(client, this.currentMacroNumber, "Stopped before reload.");
      this.clearSelfRuntimeState();

      try {
         this.ensureConfigFiles();
         this.model = MacroModel.load(this.macroPath(this.currentMacroNumber));
         this.lastStatus = "Reloaded Macro " + this.currentMacroNumber;
         this.send(client, "Reloaded Macro " + this.currentMacroNumber + ".");
      } catch (RuntimeException | IOException var3) {
         MacroBuilderClient.LOGGER.error("Failed to reload Litemacro file", var3);
         this.lastStatus = "Reload failed: " + var3.getMessage();
         this.send(client, this.lastStatus);
      }
   }

   void resume(Minecraft client, MacroModel model) {
      if (!this.childRunner) {
         this.startModelRuns(client, this.currentMacroNumber, model);
         return;
      }

      this.resumeFromNode(client, model, model.startNode());
   }

   private void resumeFromNode(Minecraft client, MacroModel model, MacroModel.Node start) {
      this.model = model;
      if (start == null) {
         this.lastStatus = "No Macro Entry Point";
         this.send(client, this.lastStatus);
      } else {
         this.currentNodeId = start.id;
         this.activeNodeId = null;
         this.activeTicks = 0;
         this.stepDelayTicksRemaining = 0;
         this.repeatCounters.clear();
         this.waitingForWorld = false;
         this.clearPendingRejoin();
         this.autoReconnectAttempted = false;
         this.captureEventBaselines();
         this.autoStartChatBaseline = this.chatSequence;
         this.autoStartKickedBaseline = this.kickedSequence;
         this.running = true;
         this.lastStatus = "Running Macro " + this.currentMacroNumber + ": " + model.name();
         this.send(client, "Resumed Macro " + this.currentMacroNumber + ": " + model.name() + ".");
      }
   }

   void stop(Minecraft client, String reason) {
      if (!this.childRunner) {
         this.stopSelf(client, reason);
         for (MacroRunner child : new ArrayList<>(this.childRunners)) {
            child.stop(client, reason);
         }

         this.childRunners.clear();
         this.pendingChildRunners.clear();
         return;
      }

      this.stopSelf(client, reason);
   }

   private void stopSelf(Minecraft client, String reason) {
      this.releaseHeldKeys(client);
      this.clearDiscordRequest();
      this.clearFarmState();
      this.clearMineAreaState();
      this.running = false;
      this.waitingForWorld = false;
      this.activeNodeId = null;
      this.activeTicks = 0;
      this.stepDelayTicksRemaining = 0;
      this.repeatCounters.clear();
      this.clearPendingRejoin();
      this.autoStartChatBaseline = this.chatSequence;
      this.autoStartKickedBaseline = this.kickedSequence;
      this.lastStatus = reason;
      this.send(client, reason);
   }

   boolean running() {
      return this.running || this.hasRunningChildren();
   }

   String status() {
      String step = this.currentNodeId == null ? "none" : this.currentNodeId;
      int runningCount = this.runningChildCount();
      return this.lastStatus + " | step: " + step + (runningCount > 0 ? " | active: " + runningCount : "");
   }

   List<MacroRunner.OverlaySnapshot> overlaySnapshots() {
      List<MacroRunner.OverlaySnapshot> snapshots = new ArrayList<>();
      this.collectOverlaySnapshots(snapshots);
      return snapshots;
   }

   private void clearSelfRuntimeState() {
      this.running = false;
      this.activeNodeId = null;
      this.activeTicks = 0;
      this.stepDelayTicksRemaining = 0;
      this.repeatCounters.clear();
      this.waitingForWorld = false;
      this.clearPendingRejoin();
      this.autoReconnectAttempted = false;
      this.activePressedKey = null;
   }

   private MacroRunner rootRunner() {
      MacroRunner runner = this;
      while (runner.parentRunner != null) {
         runner = runner.parentRunner;
      }

      return runner;
   }

   private boolean hasRunningChildren() {
      for (MacroRunner child : this.childRunners) {
         if (child.running()) {
            return true;
         }
      }

      return false;
   }

   private int runningChildCount() {
      int count = this.running ? 1 : 0;

      for (MacroRunner child : this.childRunners) {
         count += child.runningChildCount();
      }

      return count;
   }

   private void collectOverlaySnapshots(List<MacroRunner.OverlaySnapshot> snapshots) {
      if (this.running) {
         snapshots.add(
            new MacroRunner.OverlaySnapshot(
               this.currentMacroNumber,
               this.model == null ? "Macro " + this.currentMacroNumber : this.model.name(),
               this.activeComponentLabel(),
               this.lastStatus,
               this.activeTicks,
               this.runningChildCount()
            )
         );
      }

      for (MacroRunner child : this.childRunners) {
         child.collectOverlaySnapshots(snapshots);
      }
   }

   private String activeComponentLabel() {
      if (this.model == null) {
         return "Waiting";
      }

      MacroModel.Node node = null;
      if (this.activeNodeId != null) {
         node = this.model.node(this.activeNodeId);
      }

      if (node == null && this.currentNodeId != null) {
         node = this.model.node(this.currentNodeId);
      }

      return node == null ? "Waiting" : node.descriptor().label();
   }

   private boolean hasRunningMacro(int macroNumber) {
      if (this.running && this.currentMacroNumber == macroNumber) {
         return true;
      }

      for (MacroRunner child : this.childRunners) {
         if (child.hasRunningMacro(macroNumber)) {
            return true;
         }
      }

      for (MacroRunner child : this.pendingChildRunners) {
         if (child.hasRunningMacro(macroNumber)) {
            return true;
         }
      }

      return false;
   }

   private boolean stopMacroRuns(Minecraft client, int macroNumber, String reason) {
      boolean stopped = false;
      Iterator<MacroRunner> iterator = this.childRunners.iterator();

      while (iterator.hasNext()) {
         MacroRunner child = iterator.next();
         if (child.currentMacroNumber == macroNumber || child.hasRunningMacro(macroNumber)) {
            child.stop(client, reason);
            iterator.remove();
            stopped = true;
         }
      }

      iterator = this.pendingChildRunners.iterator();
      while (iterator.hasNext()) {
         MacroRunner child = iterator.next();
         if (child.currentMacroNumber == macroNumber || child.hasRunningMacro(macroNumber)) {
            child.stop(client, reason);
            iterator.remove();
            stopped = true;
         }
      }

      if (this.childRunner && this.running && this.currentMacroNumber == macroNumber) {
         this.stopSelf(client, reason);
         stopped = true;
      }

      return stopped;
   }

   private void replaceActiveModel(int macroNumber, MacroModel model) {
      if (this.currentMacroNumber == macroNumber) {
         this.model = model;
      }

      for (MacroRunner child : this.childRunners) {
         child.replaceActiveModel(macroNumber, model);
      }
   }

   private void startMacroSlot(Minecraft client, int macroNumber) {
      try {
         this.ensureConfigFiles();
         MacroModel macro = MacroModel.load(this.macroPath(macroNumber));
         this.startModelRuns(client, macroNumber, macro);
      } catch (RuntimeException | IOException error) {
         MacroBuilderClient.LOGGER.error("Failed to start macro {}", macroNumber, error);
         this.lastStatus = "Macro " + macroNumber + " failed to start: " + error.getMessage();
         this.send(client, this.lastStatus);
      }
   }

   private void startModelRuns(Minecraft client, int macroNumber, MacroModel model) {
      this.stopMacroRuns(client, macroNumber, "Restarted Macro " + macroNumber + ".");
      this.model = macroNumber == this.currentMacroNumber ? model : this.model;
      List<MacroModel.Node> starts = model.startNodes();
      if (starts.isEmpty()) {
         this.lastStatus = "No Macro Entry Point";
         this.send(client, this.lastStatus);
         return;
      }

      for (MacroModel.Node start : starts) {
         MacroRunner child = new MacroRunner(true, this.rootRunner());
         child.settings = this.settings;
         child.currentMacroNumber = macroNumber;
         child.resumeFromNode(client, model, start);
         this.rootRunner().queueChildRunner(child);
      }

      this.lastStatus = "Running Macro " + macroNumber + ": " + model.name();
   }

   private void queueChildRunner(MacroRunner child) {
      if (this.childRunner) {
         this.rootRunner().queueChildRunner(child);
      } else {
         this.pendingChildRunners.add(child);
      }
   }

   private MacroRunner spawnBranch(String targetNodeId, MacroModel.Node previousNode) {
      MacroRunner branch = new MacroRunner(true, this.rootRunner());
      branch.settings = this.settings;
      branch.model = this.model;
      branch.currentMacroNumber = this.currentMacroNumber;
      branch.currentNodeId = targetNodeId;
      branch.activeNodeId = null;
      branch.activeTicks = 0;
      branch.stepDelayTicksRemaining = this.stepDelayTicks(previousNode);
      branch.waitingForWorld = false;
      branch.lastServerData = this.lastServerData;
      branch.chatSequence = this.chatSequence;
      branch.damageSequence = this.damageSequence;
      branch.teleportSequence = this.teleportSequence;
      branch.playerSpawnSequence = this.playerSpawnSequence;
      branch.playerDespawnSequence = this.playerDespawnSequence;
      branch.kickedSequence = this.kickedSequence;
      branch.lastChatMessage = this.lastChatMessage;
      branch.lastPlayerSpawnName = this.lastPlayerSpawnName;
      branch.lastPlayerDespawnName = this.lastPlayerDespawnName;
      branch.lastKickReason = this.lastKickReason;
      branch.lastHealth = this.lastHealth;
      branch.lastPlayerPos = this.lastPlayerPos;
      branch.lastWorldId = this.lastWorldId;
      branch.knownPlayerIds.addAll(this.knownPlayerIds);
      branch.knownPlayerNames.putAll(this.knownPlayerNames);
      branch.captureEventBaselines();
      branch.autoStartChatBaseline = this.chatSequence;
      branch.autoStartKickedBaseline = this.kickedSequence;
      branch.running = true;
      branch.lastStatus = "Running Macro " + this.currentMacroNumber + " | branch";
      return branch;
   }

   private void updateLastServerData(Minecraft client) {
      ServerData server = client.getCurrentServer();
      if (server != null && server.ip != null && !server.ip.isBlank()) {
         this.lastServerData = server;
      }
   }

   private void maybeAutoStart(Minecraft client) {
      if (this.model != null) {
         if (this.hasRunningMacro(this.currentMacroNumber)) {
            return;
         }

         String mode = this.model.alwaysOn() ? "always" : this.model.autoStartMode();
         if ("always".equals(mode)) {
            this.resume(client, this.model);
         } else {
            boolean worldAvailable = client != null && client.player != null && client.level != null;
            if (!worldAvailable && !isKickedCondition(mode)) {
               return;
            }

            Boolean triggered = this.idleConditionMatches(client, mode, this.model.autoStartFilter(), this.autoStartChatBaseline, this.autoStartKickedBaseline);
            if (Boolean.TRUE.equals(triggered)) {
               this.resume(client, this.model);
            }
         }
      }
   }

   private Boolean idleConditionMatches(Minecraft client, String conditionText, String filterText, long chatBaseline, long kickedBaseline) {
      String condition = conditionText == null ? "" : conditionText.trim().toLowerCase(Locale.ROOT);
      return switch (condition) {
         case "", "off", "none", "false" -> false;
         case "chat", "message", "text" -> this.chatSequence > chatBaseline && chatFilterMatches(this.lastChatMessage, filterText);
         case "player", "nearby", "player_nearby" -> this.anyNearbyPlayerMatches(client, filterText);
         case "inventory", "inventory_full", "full" -> this.isInventoryFull(client);
         case "kick", "kicked", "disconnect", "disconnected" -> this.kickedSequence > kickedBaseline && eventTextMatches(this.lastKickReason, filterText, "any");
         default -> null;
      };
   }

   private static boolean isKickedCondition(String conditionText) {
      String condition = conditionText == null ? "" : conditionText.trim().toLowerCase(Locale.ROOT);
      return "kick".equals(condition) || "kicked".equals(condition) || "disconnect".equals(condition) || "disconnected".equals(condition);
   }

   private boolean autoReconnectConfigured() {
      return this.model != null && isKickedCondition(this.model.autoStartMode());
   }

   private static boolean chatFilterMatches(String message, String filterText) {
      String filter = filterText == null ? "" : filterText.trim();
      if (filter.isBlank()) {
         return true;
      }

      String text = message == null ? "" : message;
      return text.toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT)) || nameMatchesList(text, filter) || nameMatchesRegex(text, filter);
   }

   private Boolean anyNearbyPlayerMatches(Minecraft client, String filterText) {
      if (client == null || client.player == null || client.level == null) {
         return false;
      }

      String filter = filterText == null ? "" : filterText.trim();

      for (AbstractClientPlayer player : client.level.players()) {
         if (!player.getUUID().equals(client.player.getUUID())) {
            if (filter.isBlank() || eventTextMatches(player.getName().getString(), filter, "any")) {
               return true;
            }
         }
      }

      return false;
   }

   private String idleConditionLabel(String conditionText) {
      String condition = conditionText == null ? "" : conditionText.trim().toLowerCase(Locale.ROOT);
      return switch (condition) {
         case "chat", "message", "text" -> "chat";
         case "player", "nearby", "player_nearby" -> "player nearby";
         case "inventory", "inventory_full", "full" -> "inventory full";
         case "kick", "kicked", "disconnect", "disconnected" -> "kick/disconnect";
         default -> "trigger";
      };
   }

   private void pauseUntilWorldReturns(Minecraft client) {
      this.releaseHeldKeys(client);
      this.clearDiscordRequest();
      this.clearFarmState();
      this.clearMineAreaState();
      this.activeJumpPlaceSupportPos = null;
      this.activeNodeId = null;
      this.activeTicks = 0;
      this.stepDelayTicksRemaining = 0;
      if (!this.waitingForWorld) {
         this.waitingForWorld = true;
         this.lastStatus = "Waiting for server/world to return.";
      }
   }

   private boolean pendingRejoinActive() {
      return this.pendingRejoinNodeId != null && !this.pendingRejoinNodeId.isBlank();
   }

   private void scheduleAutoReconnect(Minecraft client) {
      this.releaseHeldKeys(client);
      this.clearDiscordRequest();
      this.clearFarmState();
      this.clearMineAreaState();
      this.pendingRejoinServer = this.copyServerData(this.lastServerData);
      this.pendingRejoinNodeId = "__auto_reconnect__";
      this.pendingRejoinLabel = "Auto Reconnect";
      this.pendingRejoinDelayTicks = 40;
      this.pendingRejoinConnectTicks = 0;
      this.pendingRejoinConnecting = false;
      this.autoReconnectAttempted = true;
      this.waitingForWorld = false;
      this.lastStatus = "Disconnected; reconnecting once.";
   }

   private void tickPendingRejoin(Minecraft client) {
      String label = this.pendingRejoinLabel == null || this.pendingRejoinLabel.isBlank() ? "Rejoin Server" : this.pendingRejoinLabel;
      if (this.pendingRejoinServer == null || this.pendingRejoinServer.ip == null || this.pendingRejoinServer.ip.isBlank()) {
         this.stop(client, "Stopped: " + label + " lost the saved server.");
      } else if (this.pendingRejoinDelayTicks > 0) {
         this.pendingRejoinDelayTicks--;
         this.lastStatus = label + " in " + (this.pendingRejoinDelayTicks * 50) + " ms.";
      } else if (!this.pendingRejoinConnecting) {
         if (client == null) {
            this.lastStatus = "Waiting for Minecraft client before " + label + ".";
         } else {
            ConnectScreen.startConnecting(
               new TitleScreen(),
               client,
               ServerAddress.parseString(this.pendingRejoinServer.ip),
               this.pendingRejoinServer,
               false,
               null
            );
            this.pendingRejoinConnecting = true;
            this.pendingRejoinConnectTicks = 0;
            this.lastStatus = "Connecting to " + this.pendingRejoinServer.ip + ".";
         }
      } else {
         this.pendingRejoinConnectTicks++;
         if (this.pendingRejoinConnectTicks > 600) {
            this.stop(client, "Stopped: " + label + " timed out.");
         }
      }
   }

   private void finishPendingRejoin(Minecraft client) {
      String nodeId = this.pendingRejoinNodeId;
      String label = this.pendingRejoinLabel == null || this.pendingRejoinLabel.isBlank() ? "Rejoin Server" : this.pendingRejoinLabel;
      this.clearPendingRejoin();
      if ("__auto_reconnect__".equals(nodeId)) {
         this.waitingForWorld = false;
         this.activeNodeId = null;
         this.activeTicks = 0;
         this.stepDelayTicksRemaining = 0;
         this.captureEventBaselines();
         this.lastStatus = "Reconnected Macro " + this.currentMacroNumber + ".";
         return;
      }

      MacroModel.Node node = this.model == null ? null : this.model.node(nodeId);
      if (node == null) {
         this.stop(client, "Stopped: " + label + " component disappeared.");
      } else {
         this.complete(client, node, "completed");
      }
   }

   private void clearPendingRejoin() {
      this.pendingRejoinServer = null;
      this.pendingRejoinNodeId = "";
      this.pendingRejoinDelayTicks = 0;
      this.pendingRejoinConnectTicks = 0;
      this.pendingRejoinConnecting = false;
      this.pendingRejoinLabel = "Rejoin Server";
   }

   private ServerData copyServerData(ServerData original) {
      ServerData copy = new ServerData(original.name, original.ip, original.type());
      copy.copyFrom(original);
      return copy;
   }

   void tick(Minecraft client) {
      if (!this.childRunner) {
         this.pollMacroHotkeys(client);
      }

      boolean worldAvailable = client != null && client.player != null && client.level != null;
      if (!this.childRunner && this.wasWorldAvailable && !worldAvailable) {
         this.recordDisconnect("Disconnected");
      }

      this.wasWorldAvailable = worldAvailable;
      if (worldAvailable) {
         this.updateLastServerData(client);
      }

      if (!this.running) {
         if (!this.childRunner && this.model != null) {
            if (worldAvailable) {
               this.updatePolledEvents(client);
            }

            this.maybeAutoStart(client);
         }

         this.tickChildRunners(client);
         return;
      }

      if (this.running) {
         if (!worldAvailable) {
            if (this.pendingRejoinActive()) {
               this.tickPendingRejoin(client);
            } else if (this.runNoWorldNodeIfPossible(client)) {
               this.tickChildRunners(client);
               return;
            } else if (this.autoReconnectConfigured() && !this.autoReconnectAttempted && this.lastServerData != null) {
               this.scheduleAutoReconnect(client);
            } else if (this.model != null && this.model.alwaysOn()) {
               this.pauseUntilWorldReturns(client);
            } else {
               this.stop(client, "Stopped: player or world was unavailable.");
            }
         } else if (this.model == null) {
            this.stop(client, "Stopped: no macro loaded.");
         } else if (this.pendingRejoinActive()) {
            this.finishPendingRejoin(client);
         } else {
            if (this.waitingForWorld) {
               this.waitingForWorld = false;
               this.activeNodeId = null;
               this.activeTicks = 0;
               this.stepDelayTicksRemaining = 0;
               this.captureEventBaselines();
               this.lastStatus = "Running Macro " + this.currentMacroNumber + " after rejoin.";
            }

            this.updatePolledEvents(client);
            if (this.stepDelayTicksRemaining > 0) {
               this.stepDelayTicksRemaining--;
            } else {
               MacroModel.Node node = this.model.node(this.currentNodeId);
               if (node == null) {
                  this.stop(client, "Stopped: missing component " + this.currentNodeId + ".");
               } else {
                  if (!node.id.equals(this.activeNodeId)) {
                     if (!node.id.equals(this.lastOpenContainerTagNextNodeId)) {
                        this.clearOpenContainerTagMatch();
                     }

                     this.activeNodeId = node.id;
                     this.activeTicks = 0;
                     this.clearFarmState();
                     this.clearMineAreaState();
                     this.activeJumpPlaceSupportPos = null;
                     this.captureEventBaselines();
                  }

                  this.activeTicks++;

                  if (!node.enabled) {
                     this.skipDisabledNode(client, node);
                  } else {
                     try {
                     String exception = node.type;
                     switch (exception) {
                        case "official:start":
                           this.complete(client, node, "started");
                           break;
                        case "official:player.respawn":
                           this.runRespawn(client, node);
                           break;
                        case "official:player.move":
                           this.runPlayerMove(client, node);
                           break;
                        case "official:player.look":
                           this.runPlayerLook(client, node);
                           break;
                        case "official:player.setMouseButton":
                           this.runSetMouseButton(client, node);
                           break;
                        case "official:player.shootBow":
                           this.runShootBow(client, node);
                           break;
                        case "official:player.isAccount":
                           this.completeCondition(client, node, this.isAccount(client, node), "Failed: Is Account needs at least one name.");
                           break;
                        case "official:player.inAction":
                           this.completeCondition(client, node, this.isInAction(client, node), "Failed: Player In Action needs a known state.");
                           break;
                        case "official:player.isAlive":
                           this.completeCondition(client, node, client.player.getHealth() > 0.0F && !client.player.isDeadOrDying(), "");
                           break;
                        case "official:player.setCrouch":
                           this.runSetCrouch(client, node);
                           break;
                        case "builder:player.setSprint":
                           this.runSetSprint(client, node);
                           break;
                        case "official:player.jump":
                           this.runJump(client, node);
                           break;
                        case "builder:player.autoClick":
                           this.runAutoClick(client, node);
                           break;
                        case "builder:player.pressKey":
                           this.runPressKey(client, node);
                           break;
                        case "official:inventory.isFull":
                           this.completeCondition(client, node, this.isInventoryFull(client), "");
                           break;
                        case "official:inventory.hasItem":
                        case "builder:inventory.hasItem":
                           this.completeCondition(client, node, this.inventoryHasItem(client, node), "Failed: Inventory Has Item needs a valid item id.");
                           break;
                        case "builder:player.healthBelow":
                           this.completeCondition(client, node, doubleAtOrBelow(client.player.getHealth(), node.value), "Failed: Health Below needs a number.");
                           break;
                        case "builder:player.foodBelow":
                           this.completeCondition(
                              client, node, intAtOrBelow(client.player.getFoodData().getFoodLevel(), node.value), "Failed: Food Below needs a whole number."
                           );
                           break;
                        case "builder:player.xpLevelAtLeast":
                           this.completeCondition(
                              client, node, intAtLeast(client.player.experienceLevel, node.value), "Failed: XP Level At Least needs a whole number."
                           );
                           break;
                        case "builder:player.onGround":
                           this.completeCondition(client, node, client.player.onGround(), "");
                           break;
                        case "builder:player.inWater":
                           this.completeCondition(client, node, client.player.isInWater(), "");
                           break;
                        case "official:player.isAtLocation":
                           this.completeCondition(client, node, this.isAtLocation(client, node), "Failed: Player At Location needs X Y Z and a max distance.");
                           break;
                        case "official:entity.player_nearby":
                           this.completeCondition(client, node, this.playerNearby(client, node), "Failed: Player Nearby mode must be any, allow, or block.");
                           break;
                        case "builder:logic.scoreboardContains":
                           this.completeCondition(client, node, this.scoreboardContains(client, node), "Failed: Scoreboard Contains mode must be contains, exact, or regex.");
                           break;
                        case "official:misc.wait":
                           this.runWait(client, node);
                           break;
                        case "official:misc.chat":
                           this.runChat(client, node);
                           break;
                        case "official:inventory.clickOpenContainerSlot":
                           this.runClickOpenContainerSlot(client, node);
                           break;
                        case "official:inventory.hotbarSelect":
                           this.runHotbarSelect(client, node);
                           break;
                        case "official:inventory.hotbarUse":
                           this.runHotbarUse(client, node);
                           break;
                        case "official:inventory.dropItems":
                           this.runDropItems(client, node);
                           break;
                        case "official:inventory.chestWithdrawItems":
                           this.runChestWithdrawItems(client, node);
                           break;
                        case "official:inventory.chestDepositItems":
                           this.runChestDepositItems(client, node);
                           break;
                        case "builder:inventory.heldItemIs":
                           this.completeCondition(client, node, this.heldItemIs(client, node), "Failed: Held Item Is needs a valid item id.");
                           break;
                        case "builder:inventory.slotHasItem":
                           this.completeCondition(client, node, this.slotHasItem(client, node), "Failed: Slot Has Item needs slot 1-36 and a valid item id.");
                           break;
                        case "builder:inventory.itemOrSlotHasTag":
                           this.completeCondition(
                              client, node, this.itemOrSlotHasTag(client, node), "Failed: Item/Slot Has Tag needs a valid item/slot and item tag."
                           );
                           break;
                        case "builder:inventory.itemDurability":
                           this.completeCondition(
                              client, node, this.itemDurabilityMatches(client, node), "Failed: Item Durability needs item/slot and lower/higher percent."
                           );
                           break;
                        case "builder:inventory.emptySlotsAtLeast":
                           this.completeCondition(client, node, this.emptySlotsAtLeast(client, node), "Failed: Empty Slots At Least needs a whole number.");
                           break;
                        case "official:inventory.hasOpenContainer":
                           this.completeCondition(client, node, hasOpenContainer(client), "");
                           break;
                        case "official:inventory.isOpenContainerFull":
                           this.completeCondition(client, node, this.isOpenContainerFull(client), "Failed: Open GUI Is Full needs an open GUI.");
                           break;
                        case "builder:inventory.openContainerHasItem":
                           this.completeCondition(
                              client, node, this.openContainerHasItem(client, node), "Failed: Open GUI Has Item needs an open GUI and valid item id."
                           );
                           break;
                        case "official:inventory.openInventory":
                           this.runOpenInventory(client, node);
                           break;
                        case "official:inventory.closeOpenContainer":
                           this.runCloseOpenContainer(client, node);
                           break;
                        case "official:world.interactWithBlock":
                           this.runWorldInteract(client, node);
                           break;
                        case "official:world.mineBlock":
                           this.runWorldMine(client, node);
                           break;
                        case "official:world.placeBlock":
                           this.runWorldPlace(client, node);
                           break;
                        case "official:world.jumpAndPlaceBlock":
                           this.runJumpAndPlaceBlock(client, node);
                           break;
                        case "builder:world.farmArea":
                           this.runFarmArea(client, node);
                           break;
                        case "builder:world.openNearestContainer":
                           this.runOpenNearestContainer(client, node);
                           break;
                        case "builder:world.autoBoneMeal":
                           this.runAutoBoneMeal(client, node);
                           break;
                        case "builder:world.mineArea":
                           this.runMineArea(client, node);
                           break;
                        case "builder:world.blockIs":
                           this.completeCondition(
                              client, node, this.blockAtMatches(client, node), "Failed: Block At Location Is needs loaded X Y Z and valid block id."
                           );
                           break;
                        case "builder:world.lookingAtBlock":
                           this.completeCondition(
                              client,
                              node,
                              this.lookingAtBlockMatches(client, node),
                              "Failed: Looking At Block needs you to look at a block and use a valid block id."
                           );
                           break;
                        case "official:entity.attack":
                           this.runEntityAction(client, node, true);
                           break;
                        case "official:entity.interact":
                           this.runEntityAction(client, node, false);
                           break;
                        case "builder:entity.nearby":
                           this.completeCondition(client, node, this.entityNearby(client, node), "Failed: Entity Nearby needs a positive max distance.");
                           break;
                        case "builder:inventory.selectHotbarSlot":
                           this.runSelectHotbarSlot(client, node);
                           break;
                        case "builder:inventory.dropSelectedItem":
                           this.runDropSelectedItem(client, node);
                           break;
                        case "builder:localMessage":
                           this.runLocalMessage(client, node);
                           break;
                        case "builder:misc.repeatMacro":
                           this.runRepeatMacro(client, node);
                           break;
                        case "builder:misc.repeatSection":
                           this.runRepeatSection(client, node);
                           break;
                        case "builder:misc.idleUntil":
                           this.runIdleUntil(client, node);
                           break;
                        case "builder:flow.endConnection":
                           this.endBranch(client, "Ended connection.");
                           break;
                        case "builder:flow.note":
                           this.endBranch(client, "Ended at note.");
                           break;
                        case "builder:stop":
                           this.stop(client, "Stopped by macro.");
                           break;
                        case "official:misc.disconnect":
                           this.runDisconnect(client, node);
                           break;
                        case "official:misc.rejoinServer":
                           this.runRejoinServer(client, node);
                           break;
                        case "official:misc.joinServer":
                           this.runJoinServer(client, node);
                           break;
                        case "official:misc.macroStop":
                           this.runMacroStop(client, node);
                           break;
                        case "official:misc.macroStart":
                           this.runMacroStart(client, node);
                           break;
                        case "official:misc.random":
                           this.runRandom(client, node);
                           break;
                        case "builder:misc.randomOutput3":
                           this.runRandomOutput3(client, node);
                           break;
                        case "official:misc.updateSign":
                           this.runUpdateSign(client, node);
                           break;
                        case "official:misc.isInLobby":
                           this.completeCondition(client, node, this.isLobbyMatch(client, node), "Failed: Is In Lobby needs a player name.");
                           break;
                        case "official:notification.discord":
                           this.runNotification(client, node);
                           break;
                        case "official:login.repeat":
                           this.complete(client, node, "completed");
                           break;
                        case "builder:event.ifChatSame":
                           this.completeCondition(client, node, this.chatSameMatches(node), "Failed: If Chat Same mode must be same, contains, or regex.");
                           break;
                        case "official:event.chat":
                           this.runEventChat(client, node);
                           break;
                        case "builder:event.kicked":
                           this.runEventKicked(client, node);
                           break;
                        case "official:event.death":
                           this.runEventDeath(client, node);
                           break;
                        case "official:event.damageReceived":
                           this.runEventDamage(client, node);
                           break;
                        case "official:event.teleport":
                           this.runEventTeleport(client, node);
                           break;
                        case "official:event.schedule":
                           this.runEventSchedule(client, node);
                           break;
                        case "official:event.player_spawned":
                           this.runEventPlayerChange(client, node, true);
                           break;
                        case "official:event.player_despawned":
                           this.runEventPlayerChange(client, node, false);
                           break;
                        default:
                           this.stop(client, "Stopped: unsupported component " + node.type + ".");
                     }
                     } catch (RuntimeException var5) {
                        MacroBuilderClient.LOGGER.error("Litemacro component failed: {}", node.type, var5);
                        this.fail(client, node, "Failed: " + node.descriptor().label() + " hit an internal safety check.");
                     }
                  }
               }
            }
         }
      }

      this.tickChildRunners(client);
   }

   private void tickChildRunners(Minecraft client) {
      Iterator<MacroRunner> iterator = this.childRunners.iterator();

      while (iterator.hasNext()) {
         MacroRunner child = iterator.next();
         child.tick(client);
         if (!child.running()) {
            iterator.remove();
         }
      }

      if (!this.pendingChildRunners.isEmpty()) {
         this.childRunners.addAll(this.pendingChildRunners);
         this.pendingChildRunners.clear();
      }
   }

   private boolean runNoWorldNodeIfPossible(Minecraft client) {
      if (this.model == null) {
         return false;
      }

      MacroModel.Node node = this.model.node(this.currentNodeId);
      if (node == null) {
         this.stop(client, "Stopped: missing component " + this.currentNodeId + ".");
         return true;
      }

      if (!this.canRunWithoutWorld(node)) {
         return false;
      }

      if (!node.id.equals(this.activeNodeId)) {
         this.activeNodeId = node.id;
         this.activeTicks = 0;
         this.clearFarmState();
         this.clearMineAreaState();
         this.activeJumpPlaceSupportPos = null;
         this.captureEventBaselines();
      }

      this.activeTicks++;

      try {
         if (!node.enabled) {
            this.skipDisabledNode(client, node);
            return true;
         }

         switch (node.type) {
            case "official:start":
               this.complete(client, node, "started");
               break;
            case "official:misc.wait":
               this.runWait(client, node);
               break;
            case "builder:event.kicked":
               this.runEventKicked(client, node);
               break;
            case "builder:misc.repeatMacro":
               this.runRepeatMacro(client, node);
               break;
            case "builder:misc.repeatSection":
               this.runRepeatSection(client, node);
               break;
            case "builder:misc.idleUntil":
               this.runIdleUntil(client, node);
               break;
            case "builder:flow.endConnection":
               this.endBranch(client, "Ended connection.");
               break;
            case "builder:flow.note":
               this.endBranch(client, "Ended at note.");
               break;
            case "official:misc.rejoinServer":
               this.runRejoinServer(client, node);
               break;
            case "official:misc.joinServer":
               this.runJoinServer(client, node);
               break;
            case "builder:localMessage":
               this.runLocalMessage(client, node);
               break;
            case "official:notification.discord":
               this.runNotification(client, node);
               break;
            case "builder:stop":
               this.stop(client, "Stopped by macro.");
               break;
            case "official:misc.random":
               this.runRandom(client, node);
               break;
            case "builder:misc.randomOutput3":
               this.runRandomOutput3(client, node);
               break;
            default:
               return false;
         }
      } catch (RuntimeException error) {
         MacroBuilderClient.LOGGER.error("Litemacro offline component failed: {}", node.type, error);
         this.fail(client, node, "Failed: " + node.descriptor().label() + " hit an internal safety check.");
      }

      return true;
   }

   private boolean canRunWithoutWorld(MacroModel.Node node) {
      if (node == null) {
         return false;
      }

      return switch (node.type) {
         case "official:start",
            "official:misc.wait",
            "builder:flow.endConnection",
            "builder:flow.note",
            "builder:event.kicked",
            "official:misc.rejoinServer",
            "official:misc.joinServer",
            "builder:localMessage",
            "official:notification.discord",
            "builder:stop",
            "builder:misc.repeatMacro",
            "builder:misc.repeatSection",
            "official:misc.random",
            "builder:misc.randomOutput3" -> true;
         case "builder:misc.idleUntil" -> isKickedCondition(node.value);
         default -> false;
      };
   }

   private void pollMacroHotkeys(Minecraft client) {
      if (macroHotkeysAvailable(client)) {
         for (int index = 0; index < this.macroKeyWasDown.length; index++) {
            int keyCode = this.settings.macroKey(index + 1);
            boolean down = keyCode != -1 && keyCode >= 0 && InputConstants.isKeyDown(client.getWindow().getWindow(), keyCode);
            if (down && !this.macroKeyWasDown[index]) {
               this.toggleMacro(client, index + 1);
            }

            this.macroKeyWasDown[index] = down;
         }
      } else {
         for (int index = 0; index < this.macroKeyWasDown.length; index++) {
            this.macroKeyWasDown[index] = false;
         }
      }
   }

   void handleMacroHotkeyPress(Minecraft client, int keyCode) {
      if (!macroHotkeysAvailable(client) || keyCode < 0) {
         return;
      }

      for (int index = 0; index < this.macroKeyWasDown.length; index++) {
         if (this.settings.macroKey(index + 1) == keyCode) {
            this.toggleMacro(client, index + 1);
            this.macroKeyWasDown[index] = true;
            return;
         }
      }
   }

   private static boolean macroHotkeysAvailable(Minecraft client) {
      if (client == null) {
         return false;
      }

      return client.screen == null || client.screen instanceof TitleScreen || client.player == null || client.level == null;
   }

   void recordChatMessage(String message) {
      if (message != null && !message.isBlank()) {
         this.lastChatMessage = message;
         this.chatSequence++;
         for (MacroRunner child : this.childRunners) {
            child.recordChatMessage(message);
         }

         for (MacroRunner child : this.pendingChildRunners) {
            child.recordChatMessage(message);
         }
      }
   }

   void recordDisconnect(String reason) {
      String text = reason == null || reason.isBlank() ? "Disconnected" : reason.trim();
      this.lastKickReason = text;
      this.kickedSequence++;

      for (MacroRunner child : this.childRunners) {
         child.recordDisconnect(text);
      }

      for (MacroRunner child : this.pendingChildRunners) {
         child.recordDisconnect(text);
      }
   }

   private void runWait(Minecraft client, MacroModel.Node node) {
      Integer durationMs = parseIntOrNull(node.value);
      if (durationMs != null && durationMs >= 0) {
         int durationTicks = Math.max(1, (int)Math.ceil(durationMs.intValue() / 50.0));
         if (this.activeTicks >= durationTicks) {
            this.complete(client, node, "completed");
         }
      } else {
         this.fail(client, node, "Failed: Wait needs a delay of 0 ms or more.");
      }
   }

   private void runRespawn(Minecraft client, MacroModel.Node node) {
      if (this.activeTicks == 1 && (client.player.isDeadOrDying() || client.player.getHealth() <= 0.0F)) {
         client.player.respawn();
      }

      this.complete(client, node, "completed");
   }

   private Boolean isAccount(Minecraft client, MacroModel.Node node) {
      String list = node.value == null ? "" : node.value;
      return list.isBlank() ? null : nameMatchesList(client.player.getName().getString(), list);
   }

   private Boolean isInAction(Minecraft client, MacroModel.Node node) {
      String state = node.value == null ? "" : node.value.trim().toLowerCase();

      return switch (state) {
         case "sneak", "sneaking", "crouch", "crouching" -> client.player.isShiftKeyDown() || client.player.isCrouching();
         case "use", "using", "using_item" -> client.player.isUsingItem();
         case "dead" -> client.player.isDeadOrDying() || client.player.getHealth() <= 0.0F;
         case "alive" -> !client.player.isDeadOrDying() && client.player.getHealth() > 0.0F;
         default -> null;
      };
   }

   private void runSetCrouch(Minecraft client, MacroModel.Node node) {
      if (this.activeTicks == 1) {
         Boolean crouching = parseBooleanOrDefault(node.value, true);
         if (crouching == null) {
            this.fail(client, node, "Failed: Set Crouch needs true or false.");
            return;
         }

         client.options.keyShift.setDown(crouching);
      }

      this.complete(client, node, "completed");
   }

   private void runSetSprint(Minecraft client, MacroModel.Node node) {
      if (this.activeTicks == 1) {
         Boolean sprinting = parseBooleanOrDefault(node.value, true);
         if (sprinting == null) {
            this.fail(client, node, "Failed: Set Sprint needs true or false.");
            return;
         }

         client.options.keySprint.setDown(sprinting);
         client.player.setSprinting(sprinting);
      }

      this.complete(client, node, "completed");
   }

   private void runJump(Minecraft client, MacroModel.Node node) {
      if (this.activeTicks == 1) {
         client.player.jumpFromGround();
      }

      this.complete(client, node, "completed");
   }

   private void runAutoClick(Minecraft client, MacroModel.Node node) {
      Integer button = parseMouseButton(node.value);
      MacroRunner.AutoClickOptions options = autoClickOptions(node.value2);
      if (button == null || options == null) {
         this.fail(client, node, "Failed: Auto Click needs button left/right and options like count=5 cps=8 or hold 500.");
      } else if (options.hold()) {
         if (this.activeTicks == 1) {
            this.setMouseButtonDown(client, button, true);
         }

         if (this.activeTicks >= durationTicks(options.durationMs())) {
            this.setMouseButtonDown(client, button, false);
            this.complete(client, node, "completed");
         }
      } else {
         int intervalTicks = Math.max(1, durationTicks(options.intervalMs()));
         int clickIndex = (this.activeTicks - 1) / intervalTicks;
         if (clickIndex >= options.count()) {
            this.setMouseButtonDown(client, button, false);
            this.complete(client, node, "completed");
         } else if ((this.activeTicks - 1) % intervalTicks == 0) {
            this.clickMouseButton(client, button);
         }
      }
   }

   private void runPressKey(Minecraft client, MacroModel.Node node) {
      InputConstants.Key key = keyFromText(node.value);
      MacroRunner.PressKeyOptions options = pressKeyOptions(node.value2);
      if (key == null || options == null) {
         this.fail(client, node, "Failed: Press Key needs a key like space, shift, ctrl, alt, E, or key code.");
      } else if (options.hold()) {
         if (this.activeTicks == 1) {
            this.activePressedKey = key;
            KeyMapping.set(key, true);
         }

         if (this.activeTicks >= durationTicks(options.durationMs())) {
            KeyMapping.set(key, false);
            this.activePressedKey = null;
            this.complete(client, node, "completed");
         }
      } else {
         if (this.activeTicks == 1) {
            KeyMapping.click(key);
         }

         if (this.activeTicks >= durationTicks(options.durationMs())) {
            KeyMapping.set(key, false);
            this.complete(client, node, "completed");
         }
      }
   }

   private void runPlayerMove(Minecraft client, MacroModel.Node node) {
      Vec3 target = parseVec3(node.value);
      if (target != null) {
         this.lookAt(client, target);
         double distanceSquared = client.player.distanceToSqr(target);
         if (distanceSquared <= 1.0) {
            this.releaseMovementKeys(client);
            this.complete(client, node, "completed");
         } else {
            client.options.keyUp.setDown(true);
            if (this.activeTicks > 300) {
               this.releaseMovementKeys(client);
               this.fail(client, node, "Failed: Move timed out before reaching target.");
            }
         }
      } else {
         Entity targetEntity = this.findTargetEntity(client, node.value);
         if (targetEntity != null) {
            this.lookAt(client, this.entityCenter(targetEntity));
            if (client.player.distanceToSqr(targetEntity) <= 1.25) {
               this.releaseMovementKeys(client);
               this.complete(client, node, "completed");
            } else {
               client.options.keyUp.setDown(true);
               if (this.activeTicks > 300) {
                  this.releaseMovementKeys(client);
                  this.fail(client, node, "Failed: Move timed out before reaching entity.");
               }
            }
         } else {
            if (this.activeTicks == 1) {
               this.releaseMovementKeys(client);
               String direction = node.value != null && !node.value.isBlank() ? node.value.trim().toLowerCase(Locale.ROOT) : "forward";
               switch (direction) {
                  case "back":
                  case "backward":
                  case "backwards":
                     client.options.keyDown.setDown(true);
                     break;
                  case "left":
                     client.options.keyLeft.setDown(true);
                     break;
                  case "right":
                     client.options.keyRight.setDown(true);
                     break;
                  case "jump":
                  case "up":
                     client.options.keyJump.setDown(true);
                     break;
                  default:
                     client.options.keyUp.setDown(true);
               }
            }

            int moveTicks = Math.max(1, (int)Math.ceil(Math.max(0.1, parseDouble(node.value2, 1.0)) * 5.0));
            if (this.activeTicks >= moveTicks) {
               this.releaseMovementKeys(client);
               this.complete(client, node, "completed");
            }
         }
      }
   }

   private void runPlayerLook(Minecraft client, MacroModel.Node node) {
      Vec3 target = parseVec3(node.value);
      if (target != null) {
         this.lookAt(client, target);
         this.complete(client, node, "completed");
      } else {
         Entity targetEntity = this.findTargetEntity(client, node.value);
         if (targetEntity != null) {
            this.lookAt(client, this.entityCenter(targetEntity));
            this.complete(client, node, "completed");
         } else {
            double[] yawPitch = parseTwoDoubles(node.value);
            if (yawPitch != null) {
               client.player.setYRot((float)yawPitch[0]);
               client.player.setXRot((float)Mth.clamp(yawPitch[1], -90.0, 90.0));
               this.complete(client, node, "completed");
            } else {
               this.fail(client, node, "Failed: Look needs yaw/pitch, a target location, or a target entity.");
            }
         }
      }
   }

   private void runSetMouseButton(Minecraft client, MacroModel.Node node) {
      Boolean pressed = parseBooleanOrAction(node.value);
      Integer button = parseMouseButton(node.value2);
      if (pressed != null && button != null && button >= 0 && button <= 1) {
         if (button == 1) {
            client.options.keyUse.setDown(pressed);
         } else {
            client.options.keyAttack.setDown(pressed);
         }

         this.complete(client, node, "completed");
      } else {
         this.fail(client, node, "Failed: Mouse Button needs state press/release and button left/right.");
      }
   }

   private void runShootBow(Minecraft client, MacroModel.Node node) {
      if (this.activeTicks == 1) {
         Boolean equipBow = parseBooleanOrDefault(node.value2, true);
         if (equipBow == null) {
            this.fail(client, node, "Failed: Shoot Bow equip value must be true or false.");
            return;
         }

         if (equipBow && !this.selectHotbarItem(client, "minecraft:bow")) {
            this.fail(client, node, "Failed: Shoot Bow could not find a bow in the hotbar.");
            return;
         }

         Vec3 target = parseVec3(node.value);
         Entity targetEntity = target == null ? this.findTargetEntity(client, node.value) : null;
         if (target != null) {
            this.lookAt(client, target);
         } else if (targetEntity != null) {
            this.lookAt(client, this.entityCenter(targetEntity));
         }

         if (client.gameMode == null) {
            this.fail(client, node, "Failed: Shoot Bow could not use the held item.");
            return;
         }

         client.gameMode.useItem(client.player, InteractionHand.MAIN_HAND);
         client.options.keyUse.setDown(true);
      }

      if (this.activeTicks >= 25) {
         client.options.keyUse.setDown(false);
         if (client.gameMode != null) {
            client.gameMode.releaseUsingItem(client.player);
         }

         this.complete(client, node, "completed");
      }
   }

   private void runChat(Minecraft client, MacroModel.Node node) {
      if (this.activeTicks == 1) {
         String message = node.value == null ? "" : node.value.trim();
         if (message.isBlank()) {
            this.fail(client, node, "Failed: Chat / Command needs text.");
            return;
         }

         if (message.length() > 256) {
            this.fail(client, node, "Failed: Chat / Command is over 256 characters.");
            return;
         }

         if (message.equals("/")) {
            this.fail(client, node, "Failed: Chat / Command needs a command after /.");
            return;
         }

         if (message.startsWith("/")) {
            client.player.connection.sendCommand(message.substring(1));
         } else {
            client.player.connection.sendChat(message);
         }
      }

      this.complete(client, node, "completed");
   }

   private void runLocalMessage(Minecraft client, MacroModel.Node node) {
      if (this.activeTicks == 1 && node.value != null && !node.value.isBlank()) {
         this.send(client, node.value.trim());
      }

      this.complete(client, node, "completed");
   }

   private void runSelectHotbarSlot(Minecraft client, MacroModel.Node node) {
      if (this.activeTicks == 1) {
         int slot = parseInt(node.value, 1);
         if (slot < 1 || slot > 9) {
            this.fail(client, node, "Failed: Select Hotbar Slot needs a slot from 1 to 9.");
            return;
         }

         client.player.getInventory().selected = slot - 1;
      }

      this.complete(client, node, "completed");
   }

   private void runHotbarSelect(Minecraft client, MacroModel.Node node) {
      if (this.activeTicks == 1 && !this.selectHotbarSlot(client, node)) {
         this.fail(client, node, "Failed: Hotbar Select could not find the item.");
      } else {
         this.complete(client, node, "completed");
      }
   }

   private void runHotbarUse(Minecraft client, MacroModel.Node node) {
      if (this.activeTicks == 1) {
         if (!this.selectHotbarSlot(client, node)) {
            this.fail(client, node, "Failed: Hotbar Use could not find the item.");
            return;
         }

         if (client.gameMode == null) {
            this.fail(client, node, "Failed: Hotbar Use could not use the selected item.");
            return;
         }

         client.gameMode.useItem(client.player, InteractionHand.MAIN_HAND);
      }

      this.complete(client, node, "completed");
   }

   private void runDropItems(Minecraft client, MacroModel.Node node) {
      if (this.activeTicks == 1) {
         Boolean dropStack = parseBooleanOrDefault(node.value2, false);
         if (dropStack == null) {
            this.fail(client, node, "Failed: Drop Items stack value must be true or false.");
            return;
         }

         Set<Integer> excludedSlots = parseInventorySlotSet(node.value3);
         if (excludedSlots == null) {
            this.fail(client, node, "Failed: Drop Items exclude slots must be numbers or ranges.");
            return;
         }

         String target = node.value == null ? "" : node.value.trim();
         if (target.isBlank()) {
            client.player.drop(dropStack);
            this.complete(client, node, "completed");
            return;
         }

         Integer targetSlot = inventorySlotNumber(target);
         if (targetSlot != null) {
            if (excludedSlots.contains(targetSlot)) {
               this.fail(client, node, "Failed: Drop Items target slot is excluded.");
               return;
            }

            if (client.player.getInventory().getItem(targetSlot).isEmpty()) {
               this.fail(client, node, "Failed: Drop Items target slot is empty.");
               return;
            }

            if (!this.dropPlayerInventorySlot(client, targetSlot, dropStack)) {
               this.fail(client, node, "Failed: Drop Items could not drop from inventory.");
               return;
            }

            this.complete(client, node, "completed");
            return;
         }

         String itemId = MacroModel.normalizeItemId(target);
         if (!isKnownItemId(itemId)) {
            this.fail(client, node, "Failed: Drop Items needs a valid item id or inventory slot.");
            return;
         }

         if (dropAllMatchingDropItems(node)) {
            int dropped = this.dropMatchingPlayerInventorySlots(client, itemId, dropStack, excludedSlots);
            if (dropped <= 0) {
               this.fail(client, node, "Failed: Drop Items could not find the item.");
               return;
            }

            this.complete(client, node, "completed");
            return;
         }

         Integer inventorySlot = this.firstMatchingPlayerInventorySlot(client, itemId, excludedSlots);
         if (inventorySlot == null) {
            this.fail(client, node, "Failed: Drop Items could not find the item.");
            return;
         }

         if (!this.dropPlayerInventorySlot(client, inventorySlot, dropStack)) {
            this.fail(client, node, "Failed: Drop Items could not drop from inventory.");
            return;
         }

         this.complete(client, node, "completed");
      }
   }

   private void runDropSelectedItem(Minecraft client, MacroModel.Node node) {
      if (this.activeTicks == 1) {
         Boolean dropStack = parseBooleanOrDefault(node.value, false);
         if (dropStack == null) {
            this.fail(client, node, "Failed: Drop Selected Item stack value must be true or false.");
            return;
         }

         client.player.getInventory().removeFromSelected(dropStack);
      }

      this.complete(client, node, "completed");
   }

   private void runClickOpenContainerSlot(Minecraft client, MacroModel.Node node) {
      String target = node.value == null ? "" : node.value.trim();
      String itemId = clickTargetItemId(target);
      Boolean shiftClick = parseBooleanOrDefault(node.value2, false);
      Integer button = parseMouseButton(node.value3);
      if (target.isBlank()) {
         this.fail(client, node, "Failed: Click GUI Item needs an item id or GUI slot.");
      } else if (!looksLikeOpenContainerSlotTarget(target) && !clickTargetHasValidFilter(target, itemId)) {
         this.fail(client, node, "Failed: Click GUI Item needs a valid item id or GUI slot.");
      } else if (shiftClick == null) {
         this.fail(client, node, "Failed: Click GUI Item shift-click value must be true or false.");
      } else if (button == null) {
         this.fail(client, node, "Failed: Click GUI Item button must be left or right.");
      } else if (this.clickOpenContainerTarget(client, target, button, shiftClick)) {
         this.complete(client, node, "completed");
      } else {
         if (this.activeTicks > SCREEN_TIMEOUT_TICKS) {
            this.fail(client, node, "Failed: could not click " + target + " in the open GUI.");
         }
      }
   }

   private void runChestDepositItems(Minecraft client, MacroModel.Node node) {
      MacroRunner.DepositResult result = this.depositNextInventoryStack(client, node);
      if (result == MacroRunner.DepositResult.INVALID_ITEM) {
         this.fail(client, node, "Failed: Deposit Items specific item is not a valid item id.");
      } else if (result == MacroRunner.DepositResult.INVALID_SLOTS) {
         this.fail(client, node, "Failed: Deposit Items exclude slots must be numbers or ranges.");
      } else if (result != MacroRunner.DepositResult.MOVED) {
         if (result == MacroRunner.DepositResult.DONE) {
            this.complete(client, node, "completed");
         } else {
            if (this.activeTicks > SCREEN_TIMEOUT_TICKS) {
               this.fail(client, node, "Failed: no open GUI was available for Deposit Items.");
            }
         }
      }
   }

   private void runChestWithdrawItems(Minecraft client, MacroModel.Node node) {
      MacroRunner.DepositResult result = this.withdrawNextContainerStack(client, node);
      if (result == MacroRunner.DepositResult.INVALID_ITEM) {
         this.fail(client, node, "Failed: Withdraw Items specific item is not a valid item id.");
      } else if (result != MacroRunner.DepositResult.MOVED) {
         if (result == MacroRunner.DepositResult.DONE) {
            this.complete(client, node, "completed");
         } else {
            if (this.activeTicks > SCREEN_TIMEOUT_TICKS) {
               this.fail(client, node, "Failed: no open GUI was available for Withdraw Items.");
            }
         }
      }
   }

   private void runCloseOpenContainer(Minecraft client, MacroModel.Node node) {
      if (this.activeTicks == 1) {
         if (hasOpenContainer(client) || handledScreen(client) != null) {
            client.player.closeContainer();
         } else if (client.screen != null) {
            client.setScreen(null);
         }
         this.complete(client, node, "completed");
      }
   }

   private void runOpenInventory(Minecraft client, MacroModel.Node node) {
      if (client.player == null) {
         this.fail(client, node, "Failed: Open Inventory needs a player.");
      } else if (client.screen instanceof InventoryScreen) {
         this.complete(client, node, "completed");
      } else if (client.screen == null && !hasOpenContainer(client)) {
         client.setScreen(new InventoryScreen(client.player));
         this.complete(client, node, "completed");
      } else {
         if (this.activeTicks == 1) {
            if (hasOpenContainer(client) || handledScreen(client) != null) {
               client.player.closeContainer();
            } else {
               client.setScreen(null);
            }
         }

         if (this.activeTicks > CLOSE_SETTLE_TICKS && client.screen == null && !hasOpenContainer(client)) {
            client.setScreen(new InventoryScreen(client.player));
            this.complete(client, node, "completed");
         } else if (this.activeTicks > SCREEN_TIMEOUT_TICKS) {
            this.fail(client, node, "Failed: Open Inventory could not open the player inventory.");
         }
      }
   }

   private void runWorldInteract(Minecraft client, MacroModel.Node node) {
      BlockHitResult hit = this.targetBlockHit(client, node.value);
      if (hit != null && client.gameMode != null) {
         if (parseInt(node.value2, 1) == 0) {
            client.gameMode.startDestroyBlock(hit.getBlockPos(), hit.getDirection());
         } else {
            client.gameMode.useItemOn(client.player, InteractionHand.MAIN_HAND, hit);
         }

         client.player.swing(InteractionHand.MAIN_HAND);
         this.complete(client, node, "completed");
      } else {
         this.fail(client, node, "Failed: Interact With Block needs a target block.");
      }
   }

   private void runWorldMine(Minecraft client, MacroModel.Node node) {
      BlockHitResult hit = this.targetBlockHit(client, node.value);
      if (hit != null && client.gameMode != null) {
         if (this.activeTicks == 1) {
            client.gameMode.startDestroyBlock(hit.getBlockPos(), hit.getDirection());
         } else {
            client.gameMode.continueDestroyBlock(hit.getBlockPos(), hit.getDirection());
         }

         client.player.swing(InteractionHand.MAIN_HAND);
         if (client.level.getBlockState(hit.getBlockPos()).isAir()) {
            client.options.keyAttack.setDown(false);
            this.complete(client, node, "completed");
         } else {
            if (this.activeTicks > 300) {
               client.options.keyAttack.setDown(false);
               this.fail(client, node, "Failed: Mine Block timed out before the block broke.");
            }
         }
      } else {
         this.fail(client, node, "Failed: Mine Block needs a target block.");
      }
   }

   private void runWorldPlace(Minecraft client, MacroModel.Node node) {
      BlockHitResult hit = this.targetBlockHit(client, node.value);
      if (hit == null || client.gameMode == null) {
         this.fail(client, node, "Failed: Place Block needs a target block.");
      } else if (!this.selectItemIfProvided(client, node.value2)) {
         this.fail(client, node, "Failed: Place Block could not find the selected block.");
      } else {
         InteractionResult result = client.gameMode.useItemOn(client.player, InteractionHand.MAIN_HAND, hit);
         if (result.consumesAction()) {
            client.player.swing(InteractionHand.MAIN_HAND);
            this.complete(client, node, "completed");
         } else {
            this.fail(client, node, "Failed: Place Block was not accepted by the world.");
         }
      }
   }

   private void runJumpAndPlaceBlock(Minecraft client, MacroModel.Node node) {
      if (this.activeTicks == 1) {
         if (!this.selectItemIfProvided(client, node.value)) {
            this.fail(client, node, "Failed: Jump And Place Block could not find the selected block.");
         } else {
            this.activeJumpPlaceSupportPos = this.blockUnderPlayer(client);
            client.options.keyJump.setDown(true);
            client.player.jumpFromGround();
         }
      } else {
         if (this.activeTicks >= 3) {
            client.options.keyJump.setDown(false);
            if (client.gameMode == null) {
               this.fail(client, node, "Failed: Jump And Place Block could not place.");
               return;
            }

            BlockPos pos = this.activeJumpPlaceSupportPos != null ? this.activeJumpPlaceSupportPos : this.blockUnderPlayer(client);
            BlockHitResult hit = this.blockTopHit(pos);
            InteractionResult result = client.gameMode.useItemOn(client.player, InteractionHand.MAIN_HAND, hit);
            if (result.consumesAction()) {
               client.player.swing(InteractionHand.MAIN_HAND);
               this.complete(client, node, "completed");
               return;
            }

            if (this.activeTicks > 18 || this.activeTicks > 6 && client.player.onGround()) {
               this.fail(client, node, "Failed: Jump And Place Block was not accepted by the world.");
            }
         }
      }
   }

   private void runFarmArea(Minecraft client, MacroModel.Node node) {
      MacroRunner.FarmOptions options = this.farmOptions(node);
      if (options == null) {
         this.fail(client, node, "Failed: Farm Area needs radius 1-8 or two XYZ points within 16 blocks per side.");
      } else if (this.activeFarmCropPos == null && options.depositWhenFull() && this.isInventoryFull(client)) {
         this.runFarmDeposit(client, node, options);
      } else {
         if (this.activeFarmCropPos == null) {
            this.activeFarmCropPos = this.nearestMatureCrop(client, options);
            this.activeFarmHarvested = false;
            if (this.activeFarmCropPos == null) {
               if (this.moveToUnloadedArea(client, node, options)) {
                  return;
               }

               this.releaseMovementKeys(client);
               this.complete(client, node, "completed");
               return;
            }

            BlockState cropState = client.level.getBlockState(this.activeFarmCropPos);
            this.activeFarmSeedItem = seedForFarmCrop(cropState);
            if (options.replant() && this.activeFarmSeedItem.isBlank()) {
               this.fail(client, node, "Failed: Farm Area found a crop but no replant item.");
               return;
            }
         }

         BlockState currentState = client.level.getBlockState(this.activeFarmCropPos);
         if (this.canReachFarmTarget(client, node, options, this.activeFarmCropPos)) {
            if (!this.activeFarmHarvested) {
               if (isMatureFarmCrop(currentState)) {
                  if (client.gameMode == null) {
                     this.fail(client, node, "Failed: Farm Area cannot harvest in the current game mode.");
                     return;
                  }

                  this.releaseMovementKeys(client);
                  this.lookAt(client, Vec3.atCenterOf(this.activeFarmCropPos));
                  if (this.activeTicks == 1) {
                     client.gameMode.startDestroyBlock(this.activeFarmCropPos, Direction.UP);
                  } else {
                     client.gameMode.continueDestroyBlock(this.activeFarmCropPos, Direction.UP);
                  }

                  client.player.swing(InteractionHand.MAIN_HAND);
                  if (this.activeTicks > 300) {
                     this.fail(client, node, "Failed: Farm Area timed out while harvesting.");
                  }

                  return;
               }

               this.activeFarmHarvested = true;
            }

            if (!options.replant()) {
               this.releaseMovementKeys(client);
               this.complete(client, node, "completed");
            } else {
               currentState = client.level.getBlockState(this.activeFarmCropPos);
               if (isSupportedFarmCrop(currentState)) {
                  this.releaseMovementKeys(client);
                  this.complete(client, node, "completed");
               } else if (!currentState.isAir()) {
                  this.fail(client, node, "Failed: Farm Area cannot replant because the crop space is blocked.");
               } else if (!this.selectHotbarItem(client, this.activeFarmSeedItem)) {
                  this.fail(client, node, "Failed: Farm Area needs " + this.activeFarmSeedItem + " in the hotbar to replant.");
               } else if (client.gameMode == null) {
                  this.fail(client, node, "Failed: Farm Area cannot replant in the current game mode.");
               } else {
                  this.releaseMovementKeys(client);
                  BlockPos base = this.activeFarmCropPos.below();
                  BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(base), Direction.UP, base, false);
                  InteractionResult result = client.gameMode.useItemOn(client.player, InteractionHand.MAIN_HAND, hit);
                  if (!result.consumesAction() && !isSupportedFarmCrop(client.level.getBlockState(this.activeFarmCropPos))) {
                     this.fail(client, node, "Failed: Farm Area could not replant " + this.activeFarmSeedItem + ".");
                  } else {
                     client.player.swing(InteractionHand.MAIN_HAND);
                     this.releaseMovementKeys(client);
                     this.complete(client, node, "completed");
                  }
               }
            }
         }
      }
   }

   private void runFarmDeposit(Minecraft client, MacroModel.Node node, MacroRunner.FarmOptions options) {
      if (activeContainerHandler(client) != null) {
         MacroRunner.DepositResult result = this.depositAnyInventoryStack(client);
         if (result == MacroRunner.DepositResult.MOVED) {
            return;
         }

         if (result == MacroRunner.DepositResult.NO_SPACE) {
            this.fail(client, node, "Failed: nearest container has no room.");
            return;
         }

         if (result == MacroRunner.DepositResult.DONE) {
            client.player.closeContainer();
            this.releaseMovementKeys(client);
            this.complete(client, node, "completed");
            return;
         }
      }

      int radius = options.radius() > 0 ? options.radius() : 10;
      BlockPos container = this.nearestContainer(client, Math.min(10, radius + 2));
      if (container == null || client.gameMode == null) {
         this.fail(client, node, "Failed: inventory full and no nearby container was found.");
      } else if (canReachBlock(client, container)) {
         this.releaseMovementKeys(client);
         this.lookAt(client, Vec3.atCenterOf(container));
         BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(container), Direction.UP, container, false);
         client.gameMode.useItemOn(client.player, InteractionHand.MAIN_HAND, hit);
         client.player.swing(InteractionHand.MAIN_HAND);
         if (this.activeTicks > 300) {
            this.fail(client, node, "Failed: nearest container did not open.");
         }
      } else if (!options.move()) {
         this.fail(client, node, "Failed: nearest container is out of reach.");
      } else {
         this.moveTowardBlock(client, container);
         if (this.activeTicks > 300) {
            this.releaseMovementKeys(client);
            this.fail(client, node, "Failed: nearest container stayed out of reach.");
         }
      }
   }

   private void runOpenNearestContainer(Minecraft client, MacroModel.Node node) {
      MacroRunner.ContainerSearchOptions options = this.containerSearchOptions(node.value, node.value2);
      if (options == null) {
         this.fail(client, node, "Failed: Open Nearest Container needs radius 1-16 and type any/chest/barrel/ender_chest/shulker.");
      } else if (activeContainerHandler(client) != null) {
         this.releaseMovementKeys(client);
         this.complete(client, node, "completed");
      } else {
         BlockPos container = this.nearestContainer(client, options.radius(), options.type());
         if (container != null && client.gameMode != null) {
            this.lookAt(client, Vec3.atCenterOf(container));
            if (canReachBlock(client, container)) {
               this.releaseMovementKeys(client);
               if (this.activeTicks % 5 == 1) {
                  this.openBlockAt(client, container);
               }

               if (this.activeTicks > 300) {
                  this.fail(client, node, "Failed: nearest container did not open.");
               }
            } else if (!options.move()) {
               this.fail(client, node, "Failed: nearest container is out of reach.");
            } else {
               client.options.keyUp.setDown(true);
               if (this.activeTicks > 300) {
                  this.releaseMovementKeys(client);
                  this.fail(client, node, "Failed: nearest container stayed out of reach.");
               }
            }
         } else {
            this.fail(client, node, "Failed: no nearby " + options.type() + " container was found.");
         }
      }
   }

   private void runAutoBoneMeal(Minecraft client, MacroModel.Node node) {
      String itemId = MacroModel.normalizeItemId(node.value);
      if (!isKnownItemId(itemId)) {
         this.fail(client, node, "Failed: Auto Bone Meal needs a valid item id.");
      } else {
         MacroRunner.BoneMealOptions options = this.boneMealOptions(node.value2);
         if (options == null) {
            this.fail(client, node, "Failed: Auto Bone Meal options need radius 1-16.");
         } else if (activeContainerHandler(client) != null) {
            if (this.selectHotbarItem(client, itemId)) {
               client.player.closeContainer();
            } else {
               MacroRunner.DepositResult result = this.withdrawSpecificFromOpenContainer(client, itemId);
               if (result == MacroRunner.DepositResult.MOVED) {
                  client.player.closeContainer();
               } else if (result == MacroRunner.DepositResult.NO_SPACE) {
                  this.fail(client, node, "Failed: inventory has no room for " + itemId + ".");
               } else {
                  this.fail(client, node, "Failed: nearby container does not have " + itemId + ".");
               }
            }
         } else if (!this.selectHotbarItem(client, itemId)) {
            if (!options.refill()) {
               this.fail(client, node, "Failed: Auto Bone Meal needs " + itemId + " in the hotbar.");
            } else {
               BlockPos container = this.nearestContainer(client, options.radius());
               if (container == null || client.gameMode == null) {
                  this.fail(client, node, "Failed: no nearby refill container was found.");
               } else if (!canReachBlock(client, container)) {
                  this.fail(client, node, "Failed: refill container is out of reach.");
               } else {
                  this.openBlockAt(client, container);
                  if (this.activeTicks > 300) {
                     this.fail(client, node, "Failed: refill container did not open.");
                  }
               }
            }
         } else {
            BlockHitResult hit = this.boneMealTargetHit(client, options);
            if (hit != null && client.gameMode != null) {
               InteractionResult result = client.gameMode.useItemOn(client.player, InteractionHand.MAIN_HAND, hit);
               if (result.consumesAction()) {
                  client.player.swing(InteractionHand.MAIN_HAND);
                  this.complete(client, node, "completed");
               } else {
                  this.fail(client, node, "Failed: Auto Bone Meal was not accepted by the target block.");
               }
            } else {
               this.fail(client, node, "Failed: Auto Bone Meal needs you to look at a block or set target X Y Z.");
            }
         }
      }
   }

   private void runMineArea(Minecraft client, MacroModel.Node node) {
      MacroRunner.MineAreaOptions options = this.mineAreaOptions(client, node);
      if (options == null) {
         this.fail(client, node, "Failed: Mine Area needs From X Y Z and To X Y Z with optional tool=10.");
      } else if (toolIsLow(client, options.toolLowThreshold())) {
         this.releaseHeldKeys(client);
         this.complete(client, node, "tool_low");
      } else if (client.gameMode == null) {
         this.fail(client, node, "Failed: Mine Area cannot mine in the current game mode.");
      } else {
         if (this.activeMineAreaPos == null || client.level.getBlockState(this.activeMineAreaPos).isAir()) {
            this.activeMineAreaPos = this.nextMineAreaBlock(client, options);
            this.activeMineAreaTicks = 0;
            if (this.activeMineAreaPos == null) {
               if (this.moveToUnloadedArea(client, node, options)) {
                  return;
               }

               this.releaseHeldKeys(client);
               this.complete(client, node, "completed");
               return;
            }
         }

         if (!canReachBlock(client, this.activeMineAreaPos)) {
            this.activeMineAreaTicks = 0;
            if (!options.move()) {
               this.releaseHeldKeys(client);
               this.fail(client, node, "Failed: Mine Area target is out of reach.");
            } else {
               this.moveTowardBlock(client, this.activeMineAreaPos);
               if (this.activeTicks > 300) {
                  this.releaseHeldKeys(client);
                  this.fail(client, node, "Failed: Mine Area could not reach the target.");
               }
            }
         } else {
            this.releaseMovementKeys(client);
            this.activeMineAreaTicks++;
            this.lookAt(client, Vec3.atCenterOf(this.activeMineAreaPos));
            if (this.activeMineAreaTicks == 1) {
               client.gameMode.startDestroyBlock(this.activeMineAreaPos, Direction.UP);
            } else {
               client.gameMode.continueDestroyBlock(this.activeMineAreaPos, Direction.UP);
            }

            client.player.swing(InteractionHand.MAIN_HAND);
            if (client.level.getBlockState(this.activeMineAreaPos).isAir()) {
               this.activeMineAreaPos = null;
               this.activeMineAreaTicks = 0;
            } else {
               if (this.activeMineAreaTicks > 300) {
                  this.releaseHeldKeys(client);
                  this.fail(client, node, "Failed: Mine Area timed out before the block broke.");
               }
            }
         }
      }
   }

   private void runEntityAction(Minecraft client, MacroModel.Node node, boolean attack) {
      Entity target = this.findTargetEntity(client, node.value);
      if (target == null) {
         if (this.activeTicks > 300) {
            this.fail(client, node, "Failed: no matching entity found.");
         }
      } else {
         this.lookAt(client, this.entityCenter(target));
         Boolean moveToTarget = parseBooleanOrDefault(node.value2, false);
         if (moveToTarget == null) {
            this.fail(client, node, "Failed: Entity move value must be true or false.");
         } else if (moveToTarget && client.player.distanceToSqr(target) > 9.0) {
            client.options.keyUp.setDown(true);
            if (this.activeTicks > 300) {
               this.releaseMovementKeys(client);
               this.fail(client, node, "Failed: target entity was out of reach.");
            }
         } else {
            this.releaseMovementKeys(client);
            if (client.gameMode == null) {
               this.fail(client, node, "Failed: entity interaction was not available.");
            } else {
               if (attack) {
                  client.gameMode.attack(client.player, target);
               } else {
                  client.gameMode.interact(client.player, target, InteractionHand.MAIN_HAND);
               }

               client.player.swing(InteractionHand.MAIN_HAND);
               this.complete(client, node, "completed");
            }
         }
      }
   }

   private void runDisconnect(Minecraft client, MacroModel.Node node) {
      this.releaseHeldKeys(client);
      this.running = false;
      this.activeNodeId = null;
      this.activeTicks = 0;
      this.lastStatus = "Disconnected by macro.";
      if (client != null) {
         client.disconnect();
      }
   }

   private void runRejoinServer(Minecraft client, MacroModel.Node node) {
      ServerData server = client == null ? null : client.getCurrentServer();
      if (server != null && server.ip != null && !server.ip.isBlank()) {
         this.lastServerData = server;
      }

      ServerData targetServer = this.rejoinTargetServer(node);
      if (targetServer == null || targetServer.ip == null || targetServer.ip.isBlank()) {
         this.fail(client, node, "Failed: Rejoin Server needs a last server or a server IP.");
      } else {
         int delayMs = Math.max(0, parseInt(node.value, 3000));
         this.scheduleServerConnection(client, node, targetServer, delayMs, "Rejoin Server");
      }
   }

   private void runJoinServer(Minecraft client, MacroModel.Node node) {
      String configuredIp = node.value == null ? "" : node.value.trim();
      if (configuredIp.isBlank()) {
         this.fail(client, node, "Failed: Join Server needs a server IP.");
      } else {
         ServerData targetServer = new ServerData(configuredIp, configuredIp, ServerData.Type.OTHER);
         int delayMs = Math.max(0, parseInt(node.value2, 1000));
         this.scheduleServerConnection(client, node, targetServer, delayMs, "Join Server");
      }
   }

   private void scheduleServerConnection(Minecraft client, MacroModel.Node node, ServerData targetServer, int delayMs, String label) {
      if (this.activeTicks == 1) {
         this.pendingRejoinServer = targetServer;
         this.pendingRejoinNodeId = node.id;
         this.pendingRejoinLabel = label;
         this.pendingRejoinDelayTicks = Math.max(1, (int)Math.ceil(delayMs / 50.0));
         this.pendingRejoinConnectTicks = 0;
         this.pendingRejoinConnecting = false;
         this.releaseHeldKeys(client);
         this.clearDiscordRequest();
         this.clearFarmState();
         this.clearMineAreaState();
         this.activeJumpPlaceSupportPos = null;
         this.waitingForWorld = false;
         this.lastStatus = label + " in " + delayMs + " ms.";
         if (client != null && client.player != null && client.level != null) {
            client.disconnect();
         }
      }
   }

   private ServerData rejoinTargetServer(MacroModel.Node node) {
      String configuredIp = node.value2 == null ? "" : node.value2.trim();
      if (!configuredIp.isBlank()) {
         ServerData target = this.lastServerData == null ? new ServerData(configuredIp, configuredIp, ServerData.Type.OTHER) : this.copyServerData(this.lastServerData);
         target.name = configuredIp;
         target.ip = configuredIp;
         return target;
      }

      return this.lastServerData == null ? null : this.copyServerData(this.lastServerData);
   }

   private void runIdleUntil(Minecraft client, MacroModel.Node node) {
      Boolean triggered = this.idleConditionMatches(client, node.value, node.value2, this.nodeChatBaseline, this.nodeKickedBaseline);
      if (triggered == null) {
         this.fail(client, node, "Failed: Idle Until condition must be chat, player, full, or kicked.");
      } else if (triggered) {
         this.complete(client, node, "triggered");
      } else {
         this.lastStatus = "Idle: waiting for " + this.idleConditionLabel(node.value) + ".";
      }
   }

   private void runUpdateSign(Minecraft client, MacroModel.Node node) {
      BlockHitResult hit = this.targetBlockHit(client, "");
      if (hit == null) {
         this.fail(client, node, "Failed: Update Sign needs you to look at a sign.");
      } else {
         String[] lines = signLines(node.value);
         client.player.connection.send(new ServerboundSignUpdatePacket(hit.getBlockPos(), true, lines[0], lines[1], lines[2], lines[3]));
         this.complete(client, node, "completed");
      }
   }

   private void runRandom(Minecraft client, MacroModel.Node node) {
      Double percent = parseDoubleOrNull(node.value);
      if (percent != null && !(percent < 0.0) && !(percent > 100.0)) {
         this.complete(client, node, Math.random() * 100.0 < percent ? "true" : "false");
      } else {
         this.fail(client, node, "Failed: Random True/False needs a number from 0 to 100.");
      }
   }

   private void runRandomOutput3(Minecraft client, MacroModel.Node node) {
      String output = switch ((int)(Math.random() * 3.0)) {
         case 0 -> "one";
         case 1 -> "two";
         default -> "three";
      };
      this.complete(client, node, output);
   }

   private void runRepeatMacro(Minecraft client, MacroModel.Node node) {
      if (isRepeatIndefinitely(node.value)) {
         MacroModel.Node start = this.model == null ? null : this.model.startNode();
         if (start == null) {
            this.stop(client, "Stopped: Repeat Macro could not find the entry point.");
         } else {
            this.releaseHeldKeys(client);
            this.currentNodeId = start.id;
            this.activeNodeId = null;
            this.activeTicks = 0;
            this.stepDelayTicksRemaining = this.stepDelayTicks(node);
            this.lastStatus = "Repeating Macro " + this.currentMacroNumber + " forever.";
         }

         return;
      }

      int targetRuns = parseInt(node.value, 1);
      if (targetRuns < 1 || targetRuns > 100000) {
         this.fail(client, node, "Failed: Repeat Macro needs a repeat count from 1 to 100000 or forever.");
      } else {
         int completedRuns = this.repeatCounters.getOrDefault(node.id, 0) + 1;
         if (completedRuns >= targetRuns) {
            this.repeatCounters.remove(node.id);
            this.stop(client, "Stopped: Repeat Macro finished " + targetRuns + " run" + (targetRuns == 1 ? "" : "s") + ".");
         } else {
            MacroModel.Node start = this.model == null ? null : this.model.startNode();
            if (start == null) {
               this.stop(client, "Stopped: Repeat Macro could not find the entry point.");
            } else {
               this.releaseHeldKeys(client);
               this.repeatCounters.put(node.id, completedRuns);
               this.currentNodeId = start.id;
               this.activeNodeId = null;
               this.activeTicks = 0;
               this.stepDelayTicksRemaining = this.stepDelayTicks(node);
               this.lastStatus = "Repeating Macro " + this.currentMacroNumber + " | run " + (completedRuns + 1) + " of " + targetRuns;
            }
         }
      }
   }

   private void runRepeatSection(Minecraft client, MacroModel.Node node) {
      if (isRepeatIndefinitely(node.value)) {
         this.complete(client, node, "repeat");
         this.lastStatus = "Repeating section forever.";
         return;
      }

      int targetRuns = parseInt(node.value, 1);
      if (targetRuns < 1 || targetRuns > 100000) {
         this.fail(client, node, "Failed: Repeat needs a repeat count from 1 to 100000 or forever.");
      } else {
         int completedRuns = this.repeatCounters.getOrDefault(node.id, 0) + 1;
         if (completedRuns >= targetRuns) {
            this.repeatCounters.remove(node.id);
            this.complete(client, node, "completed");
         } else {
            this.repeatCounters.put(node.id, completedRuns);
            this.complete(client, node, "repeat");
            this.lastStatus = "Repeating section | loop " + (completedRuns + 1) + " of " + targetRuns;
         }
      }
   }

   private static boolean isRepeatIndefinitely(String value) {
      String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
      return normalized.equals("forever")
         || normalized.equals("infinite")
         || normalized.equals("indefinite")
         || normalized.equals("always")
         || normalized.equals("loop")
         || normalized.equals("loops")
         || normalized.equals("-1");
   }

   private void skipDisabledNode(Minecraft client, MacroModel.Node node) {
      List<String> outputs = node.descriptor().outputs();
      if (outputs.isEmpty()) {
         this.endBranch(client, "Skipped disabled " + node.descriptor().label() + ".");
      } else {
         String output = outputs.contains("completed") ? "completed" : outputs.contains("started") ? "started" : outputs.get(0);
         this.lastStatus = "Skipped disabled " + node.descriptor().label() + ".";
         this.complete(client, node, output);
      }
   }

   private void endBranch(Minecraft client, String reason) {
      this.releaseHeldKeys(client);
      this.clearDiscordRequest();
      this.clearFarmState();
      this.clearMineAreaState();
      this.running = false;
      this.waitingForWorld = false;
      this.currentNodeId = null;
      this.activeNodeId = null;
      this.activeTicks = 0;
      this.stepDelayTicksRemaining = 0;
      this.repeatCounters.clear();
      this.clearPendingRejoin();
      this.lastStatus = reason;
   }

   private void completeCondition(Minecraft client, MacroModel.Node node, Boolean result, String failReason) {
      if (result != null) {
         this.complete(client, node, result ? "true" : "false");
      } else {
         this.fail(client, node, failReason != null && !failReason.isBlank() ? failReason : "Failed: " + node.descriptor().label() + " could not run.");
      }
   }

   private void complete(Minecraft client, MacroModel.Node node, String outputKey) {
      List<String> nextTargets = node.nextTargets(outputKey);
      if (nextTargets.isEmpty()) {
         this.stop(client, "Stopped: " + node.descriptor().label() + " has no " + outputKey + " connection.");
      } else if (this.model != null && nextTargets.stream().allMatch(target -> this.model.node(target) != null)) {
         String next = nextTargets.get(0);
         this.currentNodeId = next;
         this.activeNodeId = null;
         this.activeTicks = 0;
         this.stepDelayTicksRemaining = this.stepDelayTicks(node);
         this.lastStatus = "Running Macro " + this.currentMacroNumber + " | " + node.descriptor().label();

         for (int index = 1; index < nextTargets.size(); index++) {
            this.rootRunner().queueChildRunner(this.spawnBranch(nextTargets.get(index), node));
         }
      } else {
         this.stop(client, "Stopped: " + node.descriptor().label() + " has a broken " + outputKey + " link.");
      }
   }

   private void fail(Minecraft client, MacroModel.Node node, String reason) {
      this.releaseHeldKeys(client);
      List<String> nextTargets = node.nextTargets("failed");
      this.lastStatus = reason;
      if (nextTargets.isEmpty()) {
         this.stop(client, reason);
      } else if (this.model != null && nextTargets.stream().allMatch(target -> this.model.node(target) != null)) {
         String next = nextTargets.get(0);
         this.currentNodeId = next;
         this.activeNodeId = null;
         this.activeTicks = 0;
         this.stepDelayTicksRemaining = this.stepDelayTicks(node);

         for (int index = 1; index < nextTargets.size(); index++) {
            this.rootRunner().queueChildRunner(this.spawnBranch(nextTargets.get(index), node));
         }
      } else {
         this.stop(client, reason + " Broken failed link.");
      }
   }

   private int stepDelayTicks(MacroModel.Node node) {
      int delayMs = node != null && node.delayMs >= 0 ? node.delayMs : (this.model == null ? 0 : this.model.stepDelayMs());
      if (this.model != null && this.model.randomDelayEnabled()) {
         delayMs = randomizedDelayMs(this.model.randomDelayMinMs(), this.model.randomDelayMaxMs());
      }

      return Math.max(0, (int)Math.ceil(delayMs / 50.0));
   }

   private static int randomizedDelayMs(int minMs, int maxMs) {
      int min = Math.max(0, Math.min(minMs, maxMs));
      int max = Math.max(min, Math.max(minMs, maxMs));
      return min + (int)Math.floor(Math.random() * (double)(max - min + 1));
   }

   private void runMacroStart(Minecraft client, MacroModel.Node node) {
      int macroNumber = parseInt(node.value, this.currentMacroNumber);
      if (macroNumber >= 1 && macroNumber <= 100) {
         this.rootRunner().startMacroSlot(client, macroNumber);
         this.complete(client, node, "completed");
      } else {
         this.fail(client, node, "Failed: Start Macro Slot needs a macro number from 1 to 100.");
      }
   }

   private void runMacroStop(Minecraft client, MacroModel.Node node) {
      int macroNumber = parseInt(node.value, this.currentMacroNumber);
      if (macroNumber >= 1 && macroNumber <= 100) {
         boolean stopped = this.rootRunner().stopMacroRuns(client, macroNumber, "Stopped Macro " + macroNumber + " by macro component.");
         if (this.childRunner && macroNumber == this.currentMacroNumber) {
            this.stopSelf(client, "Stopped by macro component.");
         } else if (stopped || macroNumber != this.currentMacroNumber) {
            this.complete(client, node, "completed");
         } else {
            this.complete(client, node, "completed");
         }
      } else {
         this.fail(client, node, "Failed: Stop Macro Slot needs a macro number from 1 to 100.");
      }
   }

   private void runNotification(Minecraft client, MacroModel.Node node) {
      if (this.activeTicks != 1 && this.activeDiscordRequest != null && node.id.equals(this.activeDiscordNodeId)) {
         if (this.activeDiscordRequest.isDone()) {
            MacroRunner.DiscordResult result = this.activeDiscordRequest.getNow(new MacroRunner.DiscordResult(false, "Discord failed."));
            this.clearDiscordRequest();
            if (result.success()) {
               this.complete(client, node, "completed");
            } else {
               this.fail(client, node, "Failed: " + result.message());
            }

            this.send(client, result.message());
         } else {
            if (this.activeTicks > 300) {
               this.clearDiscordRequest();
               this.fail(client, node, "Failed: Discord webhook timed out.");
            }
         }
      } else {
         String message = node.value2 != null && !node.value2.isBlank() ? node.value2.trim() : "Notification";
         String webhook = node.value == null ? "" : node.value.trim();
         if (webhook.isBlank()) {
            this.clearDiscordRequest();
            this.fail(client, node, "Failed: Discord Notification needs a webhook URL.");
         } else {
            URI webhookUri = this.parseHttpUri(webhook);
            if (webhookUri == null) {
               this.clearDiscordRequest();
               this.fail(client, node, "Failed: Discord Notification needs a valid webhook URL.");
            } else {
               this.activeDiscordNodeId = node.id;
               this.activeDiscordRequest = this.postDiscordWebhook(webhookUri, message);
               this.send(client, "Discord notification sending.");
            }
         }
      }
   }

   private void runEventChat(Minecraft client, MacroModel.Node node) {
      if (this.chatSequence > this.nodeChatBaseline && eventTextMatches(this.lastChatMessage, node.value, node.value2)) {
         this.complete(client, node, "triggered");
      }
   }

   private void runEventKicked(Minecraft client, MacroModel.Node node) {
      if (this.kickedSequence > this.nodeKickedBaseline && eventTextMatches(this.lastKickReason, node.value, node.value2)) {
         this.complete(client, node, "triggered");
      }
   }

   private Boolean chatSameMatches(MacroModel.Node node) {
      String latest = this.lastChatMessage == null ? "" : this.lastChatMessage;
      if (latest.isBlank()) {
         return false;
      } else {
         String filter = node.value == null ? "" : node.value.trim();
         if (filter.isBlank()) {
            return true;
         } else {
            String mode = node.value2 == null ? "same" : node.value2.trim().toLowerCase(Locale.ROOT);

            return switch (mode) {
               case "", "same", "exact", "equals" -> latest.equalsIgnoreCase(filter);
               case "contains", "any" -> latest.toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT));
               case "regex" -> this.chatRegexMatches(latest, filter);
               default -> null;
            };
         }
      }
   }

   private Boolean chatRegexMatches(String text, String regexText) {
      try {
         return text.matches(regexText);
      } catch (PatternSyntaxException var4) {
         return null;
      }
   }

   private void runEventDeath(Minecraft client, MacroModel.Node node) {
      if (client.player.isDeadOrDying() || client.player.getHealth() <= 0.0F) {
         this.complete(client, node, "triggered");
      }
   }

   private void runEventDamage(Minecraft client, MacroModel.Node node) {
      if (this.damageSequence > this.nodeDamageBaseline) {
         this.complete(client, node, "triggered");
      }
   }

   private void runEventTeleport(Minecraft client, MacroModel.Node node) {
      if (this.teleportSequence > this.nodeTeleportBaseline) {
         this.complete(client, node, "triggered");
      }
   }

   private void runEventSchedule(Minecraft client, MacroModel.Node node) {
      String schedule = node.value == null ? "" : node.value.trim();
      if (!schedule.isBlank() && !schedule.equals("* * * * *")) {
         int delayTicks = parseScheduleDelayTicks(schedule);
         if (delayTicks > 0) {
            if (this.activeTicks >= delayTicks) {
               this.complete(client, node, "triggered");
            }
         } else {
            LocalTime target = parseTimeOfDay(schedule);
            if (target != null) {
               LocalTime now = LocalTime.now();
               if (now.getHour() == target.getHour() && now.getMinute() == target.getMinute()) {
                  this.complete(client, node, "triggered");
               }
            }
         }
      } else {
         long minute = System.currentTimeMillis() / 60000L;
         if (minute != this.scheduleMinute) {
            this.scheduleMinute = minute;
            this.complete(client, node, "triggered");
         }
      }
   }

   private void runEventPlayerChange(Minecraft client, MacroModel.Node node, boolean spawned) {
      long sequence = spawned ? this.playerSpawnSequence : this.playerDespawnSequence;
      long baseline = spawned ? this.nodeSpawnBaseline : this.nodeDespawnBaseline;
      String name = spawned ? this.lastPlayerSpawnName : this.lastPlayerDespawnName;
      if (sequence > baseline && eventTextMatches(name, node.value, node.value2)) {
         this.complete(client, node, "triggered");
      }
   }

   private void captureEventBaselines() {
      this.nodeChatBaseline = this.chatSequence;
      this.nodeDamageBaseline = this.damageSequence;
      this.nodeTeleportBaseline = this.teleportSequence;
      this.nodeSpawnBaseline = this.playerSpawnSequence;
      this.nodeDespawnBaseline = this.playerDespawnSequence;
      this.nodeKickedBaseline = this.kickedSequence;
   }

   private void updatePolledEvents(Minecraft client) {
      float health = client.player.getHealth();
      if (!Float.isNaN(this.lastHealth) && health < this.lastHealth && health > 0.0F) {
         this.damageSequence++;
      }

      this.lastHealth = health;
      Vec3 pos = this.entityCenter(client.player);
      String worldId = client.level.dimension().location().toString();
      if (this.lastPlayerPos != null && (!worldId.equals(this.lastWorldId) || pos.distanceToSqr(this.lastPlayerPos) > 64.0)) {
         this.teleportSequence++;
      }

      this.lastPlayerPos = pos;
      this.lastWorldId = worldId;
      Set<UUID> currentPlayers = new HashSet<>();

      for (AbstractClientPlayer player : client.level.players()) {
         UUID id = player.getUUID();
         String name = player.getName().getString();
         currentPlayers.add(id);
         if (!this.knownPlayerIds.isEmpty() && !this.knownPlayerIds.contains(id)) {
            this.lastPlayerSpawnName = name;
            this.playerSpawnSequence++;
         }

         this.knownPlayerNames.put(id, name);
      }

      if (!this.knownPlayerIds.isEmpty()) {
         for (UUID previous : this.knownPlayerIds) {
            if (!currentPlayers.contains(previous)) {
               this.lastPlayerDespawnName = this.knownPlayerNames.getOrDefault(previous, previous.toString());
               this.playerDespawnSequence++;
            }
         }
      }

      this.knownPlayerIds.clear();
      this.knownPlayerIds.addAll(currentPlayers);
      this.knownPlayerNames.keySet().retainAll(currentPlayers);
   }

   private URI parseHttpUri(String url) {
      try {
         URI uri = URI.create(url);
         String scheme = uri.getScheme();
         String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
         return "https".equalsIgnoreCase(scheme)
               && (host.equals("discord.com") || host.endsWith(".discord.com") || host.equals("discordapp.com") || host.endsWith(".discordapp.com"))
            ? uri
            : null;
      } catch (IllegalArgumentException var5) {
         return null;
      }
   }

   private CompletableFuture<MacroRunner.DiscordResult> postDiscordWebhook(URI webhook, String message) {
      try {
         JsonObject payload = new JsonObject();
         payload.addProperty("content", discordContent(message));
         JsonArray parse = new JsonArray();
         parse.add("users");
         parse.add("roles");
         parse.add("everyone");
         JsonObject allowedMentions = new JsonObject();
         allowedMentions.add("parse", parse);
         payload.add("allowed_mentions", allowedMentions);
         HttpRequest request = HttpRequest.newBuilder(discordWaitUri(webhook))
            .timeout(Duration.ofSeconds(12L))
            .header("Content-Type", "application/json")
            .header("User-Agent", "Litemacro/1.0.0")
            .POST(BodyPublishers.ofString(payload.toString()))
            .build();
         return DISCORD_HTTP.sendAsync(request, BodyHandlers.ofString()).handle((response, throwable) -> {
            if (throwable != null) {
               String errorx = "Discord failed: " + throwable.getMessage();
               this.lastStatus = errorx;
               MacroBuilderClient.LOGGER.warn("Discord webhook failed", throwable);
               return new MacroRunner.DiscordResult(false, errorx);
            } else {
               int statusCode = response.statusCode();
               if (statusCode >= 200 && statusCode < 300) {
                  String success = "Discord notification sent";
                  this.lastStatus = success;
                  return new MacroRunner.DiscordResult(true, success);
               } else {
                  String body = response.body() == null ? "" : response.body().trim();
                  String errorxx = "Discord failed: HTTP " + statusCode + (body.isBlank() ? "" : " " + body);
                  this.lastStatus = errorxx;
                  MacroBuilderClient.LOGGER.warn("Discord webhook returned HTTP {} {}", statusCode, body);
                  return new MacroRunner.DiscordResult(false, errorxx);
               }
            }
         });
      } catch (URISyntaxException | IllegalArgumentException var7) {
         String error = "Discord failed: bad webhook URL";
         this.lastStatus = error;
         MacroBuilderClient.LOGGER.warn("Invalid Discord webhook URL", var7);
         return CompletableFuture.completedFuture(new MacroRunner.DiscordResult(false, error));
      }
   }

   private static URI discordWaitUri(URI webhook) throws URISyntaxException {
      String query = webhook.getRawQuery();
      String waitQuery = query != null && !query.isBlank() ? query + "&wait=true" : "wait=true";
      return new URI(
         webhook.getScheme(), webhook.getRawUserInfo(), webhook.getHost(), webhook.getPort(), webhook.getRawPath(), waitQuery, webhook.getRawFragment()
      );
   }

   private static String discordContent(String message) {
      String content = message != null && !message.isBlank() ? message : "Notification";
      return content.length() <= 2000 ? content : content.substring(0, 1997) + "...";
   }

   private void clearDiscordRequest() {
      this.activeDiscordRequest = null;
      this.activeDiscordNodeId = null;
   }

   private void ensureConfigFiles() throws IOException {
      this.migrateLegacyConfigDirectory();
      Files.createDirectories(this.configDirectory);
      Path legacyPath = this.configDirectory.resolve("macro.json");
      Path macro1Path = this.macroPath(1);
      if (Files.notExists(macro1Path)) {
         if (Files.exists(legacyPath)) {
            Files.copy(legacyPath, macro1Path, StandardCopyOption.REPLACE_EXISTING);
         } else {
            this.saveDefaultExample(macro1Path);
         }
      } else {
         this.migrateOldMacro2ExampleToMacro1(macro1Path);
      }

      this.ensureMacroFile(this.currentMacroNumber);
      if (Files.notExists(this.settingsPath)) {
         this.settings.save(this.settingsPath);
      }
   }

   private void migrateLegacyConfigDirectory() throws IOException {
      if (!Files.exists(this.configDirectory)) {
         if (Files.exists(this.previousBrandConfigDirectory)) {
            this.copyConfigDirectory(this.previousBrandConfigDirectory);
         } else if (!Files.notExists(this.legacyConfigDirectory)) {
            this.copyConfigDirectory(this.legacyConfigDirectory);
         }
      }
   }

   private void copyConfigDirectory(Path sourceDirectory) throws IOException {
      Files.createDirectories(this.configDirectory);

      try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDirectory)) {
         for (Path source : stream) {
            if (Files.isRegularFile(source)) {
               Files.copy(source, this.configDirectory.resolve(source.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            }
         }
      }
   }

   private void ensureMacroFile(int macroNumber) throws IOException {
      Path path = this.macroPath(macroNumber);
      if (!Files.exists(path)) {
         MacroModel macro = MacroModel.createDefault();
         macro.setName("Macro " + macroNumber);
         macro.save(path);
      }
   }

   private void saveDefaultExample(Path path) throws IOException {
      MacroModel macro;
      try (InputStream input = MacroRunner.class.getResourceAsStream("/macro_builder/default_macro2_export.json")) {
         if (input == null) {
            macro = MacroModel.createDefault();
            macro.setName("Example");
         } else {
            macro = MacroModel.fromMineBotExport(input, "Example");
         }
      }

      macro.save(path);
   }

   private void migrateOldMacro2ExampleToMacro1(Path macro1Path) throws IOException {
      Path macro2Path = this.macroPath(2);
      if (!Files.notExists(macro2Path)) {
         MacroModel macro1 = MacroModel.load(macro1Path);
         MacroModel macro2 = MacroModel.load(macro2Path);
         if (this.isBlankMacro(macro1) && this.looksLikeBundledExample(macro2)) {
            macro2.setName("Example");
            macro2.save(macro1Path);
            MacroModel blankMacro2 = MacroModel.createDefault();
            blankMacro2.setName("Macro 2");
            blankMacro2.save(macro2Path);
         }
      }
   }

   private boolean isBlankMacro(MacroModel macro) {
      MacroModel.Node start = macro.startNode();
      return macro.nodes().size() == 1 && start != null && start.outputs.isEmpty();
   }

   private boolean looksLikeBundledExample(MacroModel macro) {
      if ("Macro 2".equals(macro.name()) && macro.nodes().size() >= 4) {
         boolean hasInventoryFull = false;
         boolean hasChat = false;
         boolean hasDeposit = false;

         for (MacroModel.Node node : macro.nodes()) {
            if ("official:inventory.isFull".equals(node.type)) {
               hasInventoryFull = true;
            }

            if ("official:misc.chat".equals(node.type)) {
               hasChat = true;
            }

            if ("official:inventory.chestDepositItems".equals(node.type)) {
               hasDeposit = true;
            }
         }

         return hasInventoryFull && hasChat && hasDeposit;
      } else {
         return false;
      }
   }

   private MacroModel readMacroImport(Path source, int macroNumber) throws IOException {
      JsonObject root;
      try (Reader reader = Files.newBufferedReader(source)) {
         root = JsonParser.parseReader(reader).getAsJsonObject();
      }

      if (root.has("components")) {
         MacroModel var5;
         try (Reader reader = Files.newBufferedReader(source)) {
            var5 = MacroModel.fromMineBotExport(reader, "Imported Macro " + macroNumber);
         }

         return var5;
      } else if (!root.has("nodes") && !root.has("format")) {
         throw new IOException("JSON file is not a Litemacro or MineBot macro.");
      } else {
         return MacroModel.load(source);
      }
   }

   List<Path> downloadsJsonFiles() throws IOException {
      Path downloads = this.downloadsDirectory();
      if (Files.notExists(downloads)) {
         throw new IOException("Downloads folder was not found.");
      } else {
         List<Path> files = new ArrayList<>();

         try (DirectoryStream<Path> stream = Files.newDirectoryStream(downloads, "*.json")) {
            for (Path path : stream) {
               files.add(path);
            }
         }

         files.sort(Comparator.comparing(this::lastModifiedOrOldest).reversed());
         return files;
      }
   }

   List<Path> marketplaceJsonFiles() throws IOException {
      Path marketplace = this.marketplaceDirectory();
      Files.createDirectories(marketplace);
      List<Path> files = new ArrayList<>();

      try (DirectoryStream<Path> stream = Files.newDirectoryStream(marketplace, "*.json")) {
         for (Path path : stream) {
            files.add(path);
         }
      }

      files.sort(Comparator.comparing(this::lastModifiedOrOldest).reversed());
      return files;
   }

   private Path newestJsonInDownloads() throws IOException {
      List<Path> files = this.downloadsJsonFiles();
      if (files.isEmpty()) {
         throw new IOException("No .json macro file found in Downloads.");
      } else {
         return files.get(0);
      }
   }

   Path downloadsDirectory() {
      return Path.of(System.getProperty("user.home"), "Downloads");
   }

   Path marketplaceDirectory() {
      return this.downloadsDirectory().resolve("Litemacro-Marketplace");
   }

   private String safeMarketplaceName(String name) {
      String value = name == null || name.isBlank() ? "Macro" : name.trim();
      StringBuilder builder = new StringBuilder();

      for (int i = 0; i < value.length(); i++) {
         char ch = value.charAt(i);
         if (Character.isLetterOrDigit(ch)) {
            builder.append(ch);
         } else if (ch == '-' || ch == '_') {
            builder.append(ch);
         } else if (ch == ' ' && (builder.length() == 0 || builder.charAt(builder.length() - 1) != '-')) {
            builder.append('-');
         }
      }

      if (builder.isEmpty()) {
         return "Macro";
      } else {
         return builder.substring(0, Math.min(48, builder.length()));
      }
   }

   private FileTime lastModifiedOrOldest(Path path) {
      try {
         return Files.getLastModifiedTime(path);
      } catch (IOException var3) {
         return FileTime.fromMillis(0L);
      }
   }

   private Path macroPath(int macroNumber) {
      return this.configDirectory.resolve("macro" + macroNumber + ".json");
   }

   private boolean isInventoryFull(Minecraft client) {
      for (int index = 0; index < 36 && index < client.player.getInventory().getContainerSize(); index++) {
         if (client.player.getInventory().getItem(index).isEmpty()) {
            return false;
         }
      }

      return true;
   }

   private Boolean isOpenContainerFull(Minecraft client) {
      AbstractContainerMenu handler = activeContainerHandler(client);
      if (handler == null) {
         return null;
      } else {
         int firstPlayerSlot = firstPlayerInventorySlot(handler);

         for (int index = 0; index < firstPlayerSlot; index++) {
            if (((Slot)handler.slots.get(index)).getItem().isEmpty()) {
               return false;
            }
         }

         return firstPlayerSlot > 0;
      }
   }

   private Boolean inventoryHasItem(Minecraft client, MacroModel.Node node) {
      String itemId = MacroModel.normalizeItemId(node.value);
      if (!isKnownItemId(itemId)) {
         return null;
      } else {
         int minCount = Math.max(1, parseInt(node.value2, 1));
         int count = 0;

         for (int index = 0; index < client.player.getInventory().getContainerSize(); index++) {
            ItemStack stack = client.player.getInventory().getItem(index);
            if (stackMatches(stack, itemId)) {
               count += stack.getCount();
               if (count >= minCount) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   private Boolean heldItemIs(Minecraft client, MacroModel.Node node) {
      String itemId = MacroModel.normalizeItemId(node.value);
      return !isKnownItemId(itemId) ? null : stackMatches(client.player.getMainHandItem(), itemId);
   }

   private Boolean slotHasItem(Minecraft client, MacroModel.Node node) {
      ItemStack stack = playerInventoryStack(client, node.value);
      String itemId = MacroModel.normalizeItemId(node.value2);
      return stack != null && isKnownItemId(itemId) ? stackMatches(stack, itemId) : null;
   }

   private Boolean itemOrSlotHasTag(Minecraft client, MacroModel.Node node) {
      String tagText = node.value2 == null ? "" : node.value2.trim();
      boolean openContainerSource = isOpenContainerTagSource(node.value);
      if (tagText.isBlank()) {
         if (openContainerSource) {
            this.clearOpenContainerTagMatch();
         }

         return null;
      } else if (openContainerSource) {
         Boolean result = this.openContainerSourceHasTag(client, node, tagText);
         if (result == null || !result) {
            this.clearOpenContainerTagMatch();
         }

         return result;
      } else {
         List<ItemStack> stacks = stacksFromItemOrSlot(client, node.value);
         if (stacks == null) {
            return null;
         }

         for (ItemStack stack : stacks) {
            if (stackHasConfiguredTag(stack, tagText)) {
               return true;
            }
         }

         return false;
      }
   }

   private Boolean itemDurabilityMatches(Minecraft client, MacroModel.Node node) {
      MacroRunner.DurabilityCheck check = durabilityCheck(node.value2);
      List<ItemStack> stacks = stacksFromItemOrSlot(client, node.value);
      if (check == null || stacks == null) {
         return null;
      }

      boolean sawDamageableItem = false;
      for (ItemStack stack : stacks) {
         if (!stack.isEmpty() && stack.isDamageableItem() && stack.getMaxDamage() > 0) {
            sawDamageableItem = true;
            double remainingPercent = (stack.getMaxDamage() - stack.getDamageValue()) * 100.0 / stack.getMaxDamage();
            if (check.lower() ? remainingPercent <= check.percent() : remainingPercent >= check.percent()) {
               return true;
            }
         }
      }

      return sawDamageableItem ? false : false;
   }

   private Boolean emptySlotsAtLeast(Minecraft client, MacroModel.Node node) {
      Integer minimum = parseIntOrNull(node.value);
      return minimum != null && minimum >= 0 ? this.emptyInventorySlots(client) >= minimum : null;
   }

   private int emptyInventorySlots(Minecraft client) {
      int empty = 0;

      for (int index = 0; index < 36 && index < client.player.getInventory().getContainerSize(); index++) {
         if (client.player.getInventory().getItem(index).isEmpty()) {
            empty++;
         }
      }

      return empty;
   }

   private Boolean openContainerHasItem(Minecraft client, MacroModel.Node node) {
      AbstractContainerMenu handler = activeContainerHandler(client);
      if (handler == null) {
         return null;
      } else {
         String itemId = MacroModel.normalizeItemId(node.value);
         if (!isKnownItemId(itemId)) {
            return null;
         } else {
            int minCount = Math.max(1, parseInt(node.value2, 1));
            int count = 0;
            int firstPlayerSlot = firstPlayerInventorySlot(handler);

            for (int index = 0; index < firstPlayerSlot; index++) {
               ItemStack stack = ((Slot)handler.slots.get(index)).getItem();
               if (stackMatches(stack, itemId)) {
                  count += stack.getCount();
                  if (count >= minCount) {
                     return true;
                  }
               }
            }

            return false;
         }
      }
   }

   private Boolean isLobbyMatch(Minecraft client, MacroModel.Node node) {
      String player = node.value == null ? "" : node.value.trim();
      return player.isBlank() ? null : client.player.getName().getString().equalsIgnoreCase(player);
   }

   private Boolean isAtLocation(Minecraft client, MacroModel.Node node) {
      String[] parts = (node.value == null ? "" : node.value.trim()).split("[,\\s]+");
      if (parts.length < 3) {
         return null;
      } else {
         Double targetX = parseDoubleOrNull(parts[0]);
         Double targetY = parseDoubleOrNull(parts[1]);
         Double targetZ = parseDoubleOrNull(parts[2]);
         Double maxDistance = parseDoubleOrNull(node.value2);
         if (targetX != null && targetY != null && targetZ != null && maxDistance != null && !(maxDistance < 0.0)) {
            double x = client.player.getX() - targetX;
            double y = client.player.getY() - targetY;
            double z = client.player.getZ() - targetZ;
            return x * x + y * y + z * z <= maxDistance * maxDistance;
         } else {
            return null;
         }
      }
   }

   private Boolean playerNearby(Minecraft client, MacroModel.Node node) {
      String filterText = node.value == null ? "" : node.value.trim();
      String mode = node.value2 == null ? "any" : node.value2.trim().toLowerCase();
      if (!mode.equals("any") && !mode.equals("allow") && !mode.equals("block")) {
         return null;
      } else {
         for (AbstractClientPlayer player : client.level.players()) {
            if (!player.getUUID().equals(client.player.getUUID())) {
               String playerName = player.getName().getString();
               boolean listed = filterText.isBlank() || nameMatchesList(playerName, filterText) || nameMatchesRegex(playerName, filterText);
               if ("allow".equals(mode) && listed) {
                  return true;
               }

               if ("block".equals(mode) && !listed) {
                  return true;
               }

               if (!"allow".equals(mode) && !"block".equals(mode)) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   private Boolean scoreboardContains(Minecraft client, MacroModel.Node node) {
      String filter = node.value == null ? "" : node.value.trim();
      if (filter.isBlank()) {
         return true;
      }

      String mode = node.value2 == null || node.value2.isBlank() ? "contains" : node.value2.trim().toLowerCase(Locale.ROOT);
      if (!"contains".equals(mode) && !"any".equals(mode) && !"exact".equals(mode) && !"equals".equals(mode) && !"regex".equals(mode)) {
         return null;
      }

      String text = this.scoreboardText(client);
      return matchesTextMode(text, filter, mode);
   }

   private String scoreboardText(Minecraft client) {
      if (client == null || client.level == null) {
         return "";
      }

      Scoreboard scoreboard = client.level.getScoreboard();
      StringBuilder text = new StringBuilder();
      for (Objective objective : scoreboard.getObjectives()) {
         appendText(text, objective.getName());
         appendText(text, objective.getDisplayName().getString());

         for (PlayerScoreEntry entry : scoreboard.listPlayerScores(objective)) {
            appendText(text, entry.owner());
            appendText(text, entry.ownerName().getString());
            if (entry.display() != null) {
               appendText(text, entry.display().getString());
            }

            appendText(text, Integer.toString(entry.value()));
         }
      }

      for (PlayerTeam team : scoreboard.getPlayerTeams()) {
         appendText(text, team.getName());
         appendText(text, team.getDisplayName().getString());
         appendText(text, team.getPlayerPrefix().getString());
         appendText(text, team.getPlayerSuffix().getString());

         for (String playerName : team.getPlayers()) {
            appendText(text, playerName);
         }
      }

      return text.toString();
   }

   private static void appendText(StringBuilder text, String value) {
      if (value != null && !value.isBlank()) {
         text.append(value).append('\n');
      }
   }

   private static Boolean matchesTextMode(String text, String filter, String mode) {
      String normalizedText = text == null ? "" : text;
      String normalizedFilter = filter == null ? "" : filter.trim();
      if (normalizedFilter.isBlank()) {
         return true;
      }

      return switch (mode) {
         case "contains", "any" -> normalizedText.toLowerCase(Locale.ROOT).contains(normalizedFilter.toLowerCase(Locale.ROOT));
         case "exact", "equals" -> normalizedText.lines().anyMatch(line -> line.trim().equalsIgnoreCase(normalizedFilter));
         case "regex" -> {
            try {
               yield Pattern.compile(normalizedFilter, Pattern.CASE_INSENSITIVE).matcher(normalizedText).find();
            } catch (PatternSyntaxException error) {
               yield null;
            }
         }
         default -> null;
      };
   }

   private Boolean blockAtMatches(Minecraft client, MacroModel.Node node) {
      BlockPos pos = parseBlockPos(node.value);
      if (pos != null && client.level.isLoaded(pos)) {
         String blockId = MacroModel.normalizeItemId(node.value2);
         return !isKnownBlockId(blockId) ? null : BuiltInRegistries.BLOCK.getKey(client.level.getBlockState(pos).getBlock()).toString().equals(blockId);
      } else {
         return null;
      }
   }

   private Boolean lookingAtBlockMatches(Minecraft client, MacroModel.Node node) {
      if (client.hitResult instanceof BlockHitResult hit && hit.getType() == Type.BLOCK) {
         String blockId = node.value == null ? "" : node.value.trim();
         if (blockId.isBlank()) {
            return true;
         } else {
            return !isKnownBlockId(blockId)
               ? null
               : BuiltInRegistries.BLOCK
                  .getKey(client.level.getBlockState(hit.getBlockPos()).getBlock())
                  .toString()
                  .equals(MacroModel.normalizeItemId(blockId));
         }
      } else {
         return null;
      }
   }

   private Boolean entityNearby(Minecraft client, MacroModel.Node node) {
      Double maxDistance = parseDoubleOrNull(node.value2);
      return maxDistance != null && !(maxDistance <= 0.0) ? this.findTargetEntityWithin(client, node.value, maxDistance) != null : null;
   }

   private boolean clickOpenContainerTarget(Minecraft client, String target, int button, boolean shiftClick) {
      AbstractContainerMenu handler = activeContainerHandler(client);
      if (handler != null && client.gameMode != null) {
         ClickType clickType = shiftClick ? ClickType.QUICK_MOVE : ClickType.PICKUP;
         Slot targetSlot = openContainerSlot(handler, target);
         if (targetSlot != null) {
            client.gameMode.handleInventoryMouseClick(handler.containerId, targetSlot.index, button, clickType, client.player);
            return true;
         }

         PriceRange priceRange = priceRange(target);
         String tagText = clickTargetTagText(target);
         String itemId = clickTargetItemId(target);
         if (priceRange == null && tagText.isBlank() && this.activeNodeId != null && this.activeNodeId.equals(this.lastOpenContainerTagNextNodeId)) {
            tagText = this.lastOpenContainerTagFilter;
            priceRange = priceRange(tagText);
         }

         int firstPlayerSlot = firstPlayerInventorySlot(handler);

         for (int index = 0; index < firstPlayerSlot; index++) {
            Slot slot = (Slot)handler.slots.get(index);
            if (stackMatchesClickTarget(slot.getItem(), itemId, tagText, priceRange)) {
               client.gameMode.handleInventoryMouseClick(handler.containerId, slot.index, button, clickType, client.player);
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private boolean selectHotbarSlot(Minecraft client, MacroModel.Node node) {
      String item = node.value == null ? "" : node.value.trim();
      if (item.isBlank()) {
         int slot = parseInt(node.value2, parseInt(node.value, 1));
         if (slot >= 1 && slot <= 9) {
            client.player.getInventory().selected = slot - 1;
            return true;
         } else {
            return false;
         }
      } else {
         String itemId = MacroModel.normalizeItemId(item);

         for (int slot = 0; slot < 9 && slot < client.player.getInventory().getContainerSize(); slot++) {
            if (stackMatches(client.player.getInventory().getItem(slot), itemId)) {
               client.player.getInventory().selected = slot;
               return true;
            }
         }

         return false;
      }
   }

   private boolean selectHotbarItem(Minecraft client, String itemId) {
      String normalized = MacroModel.normalizeItemId(itemId);

      for (int slot = 0; slot < 9 && slot < client.player.getInventory().getContainerSize(); slot++) {
         if (stackMatches(client.player.getInventory().getItem(slot), normalized)) {
            client.player.getInventory().selected = slot;
            return true;
         }
      }

      return false;
   }

   private boolean selectItemIfProvided(Minecraft client, String itemText) {
      return itemText != null && !itemText.isBlank() && !truthy(itemText) && !"false".equalsIgnoreCase(itemText.trim())
         ? this.selectHotbarItem(client, itemText)
         : true;
   }

   private Entity findTargetEntity(Minecraft client, String filter) {
      return this.findTargetEntityWithin(client, filter, Double.POSITIVE_INFINITY);
   }

   private Entity findTargetEntityWithin(Minecraft client, String filter, double maxDistance) {
      String normalized = filter == null ? "" : filter.trim().toLowerCase(Locale.ROOT);
      Entity best = null;
      double bestDistance = maxDistance == Double.POSITIVE_INFINITY ? Double.MAX_VALUE : maxDistance * maxDistance;

      for (Entity entity : client.level.entitiesForRendering()) {
         if (entity != client.player && entity instanceof LivingEntity && entity.isAlive() && (normalized.isBlank() || this.entityMatches(entity, normalized))) {
            double distance = client.player.distanceToSqr(entity);
            if (distance < bestDistance) {
               bestDistance = distance;
               best = entity;
            }
         }
      }

      return best;
   }

   private boolean entityMatches(Entity entity, String filter) {
      String name = entity.getName().getString().toLowerCase(Locale.ROOT);
      String type = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString().toLowerCase(Locale.ROOT);
      return name.contains(filter) || type.contains(filter) || nameMatchesList(name, filter) || nameMatchesRegex(name, filter);
   }

   private BlockHitResult targetBlockHit(Minecraft client, String targetText) {
      Vec3 target = parseVec3(targetText);
      if (target != null) {
         BlockPos pos = BlockPos.containing(target);
         return this.blockFaceHit(client, pos);
      } else {
         return client.hitResult instanceof BlockHitResult hit && hit.getType() == Type.BLOCK ? hit : null;
      }
   }

   private BlockHitResult blockFaceHit(Minecraft client, BlockPos pos) {
      Direction face = Direction.UP;
      if (client != null && client.player != null) {
         Vec3 center = Vec3.atCenterOf(pos);
         face = this.nearestBlockFace(client.player.getX() - center.x, client.player.getEyeY() - center.y, client.player.getZ() - center.z);
      }

      Vec3 hit = Vec3.atCenterOf(pos).add(face.getStepX() * 0.5, face.getStepY() * 0.5, face.getStepZ() * 0.5);
      return new BlockHitResult(hit, face, pos, false);
   }

   private Direction nearestBlockFace(double dx, double dy, double dz) {
      double ax = Math.abs(dx);
      double ay = Math.abs(dy);
      double az = Math.abs(dz);
      if (ay >= ax && ay >= az) {
         return dy >= 0.0 ? Direction.UP : Direction.DOWN;
      } else if (ax >= az) {
         return dx >= 0.0 ? Direction.EAST : Direction.WEST;
      } else {
         return dz >= 0.0 ? Direction.SOUTH : Direction.NORTH;
      }
   }

   private BlockHitResult blockTopHit(BlockPos pos) {
      return new BlockHitResult(new Vec3(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5), Direction.UP, pos, false);
   }

   private BlockPos blockUnderPlayer(Minecraft client) {
      return BlockPos.containing(client.player.getX(), client.player.getY() - 0.05, client.player.getZ());
   }

   private Vec3 entityCenter(Entity entity) {
      return new Vec3(entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ());
   }

   private void lookAt(Minecraft client, Vec3 target) {
      Vec3 eye = client.player.getEyePosition();
      double dx = target.x - eye.x;
      double dy = target.y - eye.y;
      double dz = target.z - eye.z;
      double horizontal = Math.sqrt(dx * dx + dz * dz);
      float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
      float pitch = (float)(-Math.toDegrees(Math.atan2(dy, horizontal)));
      client.player.setYRot(Mth.wrapDegrees(yaw));
      client.player.setXRot((float)Mth.clamp(pitch, -90.0, 90.0));
   }

   private void releaseHeldKeys(Minecraft client) {
      this.releaseMovementKeys(client);
      if (this.activePressedKey != null) {
         KeyMapping.set(this.activePressedKey, false);
         this.activePressedKey = null;
      }

      if (client != null) {
         client.options.keyAttack.setDown(false);
         client.options.keyUse.setDown(false);
         client.options.keySprint.setDown(false);
      }
   }

   private void releaseMovementKeys(Minecraft client) {
      if (client != null) {
         client.options.keyUp.setDown(false);
         client.options.keyDown.setDown(false);
         client.options.keyLeft.setDown(false);
         client.options.keyRight.setDown(false);
         client.options.keyJump.setDown(false);
      }
   }

   private void clearFarmState() {
      this.activeFarmCropPos = null;
      this.activeFarmSeedItem = "";
      this.activeFarmHarvested = false;
   }

   private void clearMineAreaState() {
      this.activeMineAreaPos = null;
      this.activeMineAreaTicks = 0;
   }

   private MacroRunner.FarmOptions farmOptions(MacroModel.Node node) {
      Integer radius = parseIntOrNull(node.value);
      BlockPos min = null;
      BlockPos max = null;
      if (radius == null) {
         BlockPos from = parseBlockPos(node.value);
         BlockPos to = parseFirstBlockPos(node.value2);
         if (from == null || to == null) {
            return null;
         }

         min = minPos(from, to);
         max = maxPos(from, to);
         if (!areaWithinLimit(min, max, 16)) {
            return null;
         }

         radius = -1;
      } else if (radius < 1 || radius > 8) {
         return null;
      }

      String options = node.value2 == null ? "" : node.value2.trim().toLowerCase(Locale.ROOT);
      boolean disabled = options.equals("false") || options.equals("off") || options.equals("harvest only");
      boolean replant = !disabled && !options.contains("no replant") && !options.contains("noreplant");
      boolean deposit = !disabled
         && (options.isBlank() || options.equals("true") || options.contains("deposit") || options.contains("container") || options.contains("drop"))
         && !options.contains("no deposit")
         && !options.contains("nodeposit");
      boolean move = moveOption(options, true);
      return new MacroRunner.FarmOptions(radius, min, max, replant, deposit, move);
   }

   private MacroRunner.ContainerSearchOptions containerSearchOptions(String radiusText, String optionText) {
      Integer radius = parseIntOrNull(radiusText);
      if (radius != null && radius >= 1 && radius <= 16) {
         String options = optionText == null ? "" : optionText.trim().toLowerCase(Locale.ROOT);
         boolean move = true;
         String type = "any";

         for (String token : options.split("[,\\s]+")) {
            String normalized = token.trim();
            if (!normalized.isBlank()) {
               if (normalized.startsWith("move=")) {
                  Boolean parsedMove = parseBooleanOrDefault(normalized.substring("move=".length()), true);
                  if (parsedMove == null) {
                     return null;
                  }

                  move = parsedMove;
               } else {
                  if (normalized.startsWith("type=")) {
                     normalized = normalized.substring("type=".length());
                  }

                  Boolean parsedMove = parseBooleanOrDefault(normalized, true);
                  if (parsedMove == null
                     || !normalized.equals("true")
                        && !normalized.equals("false")
                        && !normalized.equals("on")
                        && !normalized.equals("off")
                        && !normalized.equals("yes")
                        && !normalized.equals("no")) {
                     String parsedType = containerType(normalized);
                     if (parsedType == null) {
                        return null;
                     }

                     type = parsedType;
                  } else {
                     move = parsedMove;
                  }
               }
            }
         }

         return new MacroRunner.ContainerSearchOptions(radius, move, type);
      } else {
         return null;
      }
   }

   private static String containerType(String token) {
      String normalized = token == null ? "" : token.trim().toLowerCase(Locale.ROOT);

      return switch (normalized) {
         case "", "any", "all", "container" -> "any";
         case "chest", "normal_chest", "trapped_chest" -> "chest";
         case "barrel" -> "barrel";
         case "ender", "enderchest", "ender_chest" -> "ender_chest";
         case "shulker", "shulker_box" -> "shulker";
         default -> null;
      };
   }

   private MacroRunner.BoneMealOptions boneMealOptions(String value) {
      String options = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
      boolean refill = options.isBlank() || options.equals("true") || options.contains("refill") || options.contains("container") || options.contains("chest");
      if (options.equals("false") || options.equals("off") || options.contains("no refill") || options.contains("norefill")) {
         refill = false;
      }

      int radius = optionInt(options, "radius=", 8);
      radius = optionInt(options, "r=", radius);
      if (radius >= 1 && radius <= 16) {
         BlockPos target = null;
         int targetIndex = options.indexOf("target=");
         if (targetIndex >= 0) {
            target = parseFirstBlockPos(options.substring(targetIndex + "target=".length()));
         } else {
            target = parseBlockPos(value);
         }

         return new MacroRunner.BoneMealOptions(refill, radius, target);
      } else {
         return null;
      }
   }

   private BlockHitResult boneMealTargetHit(Minecraft client, MacroRunner.BoneMealOptions options) {
      if (options.target() != null) {
         BlockPos pos = options.target();
         return new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
      } else {
         return this.targetBlockHit(client, "");
      }
   }

   private MacroRunner.MineAreaOptions mineAreaOptions(Minecraft client, MacroModel.Node node) {
      BlockPos from = parseBlockPos(node.value);
      BlockPos to = parseFirstBlockPos(node.value2);
      if (from != null && to != null) {
         String options = node.value2 == null ? "" : node.value2.trim().toLowerCase(Locale.ROOT);
         BlockPos min = minPos(from, to);
         BlockPos max = maxPos(from, to);
         if (!areaWithinLimit(min, max, 16)) {
            return null;
         } else {
            int toolLowThreshold = mineToolLowThreshold(options);
            boolean move = moveOption(options, true);
            return new MacroRunner.MineAreaOptions(min, max, toolLowThreshold, move);
         }
      } else {
         return null;
      }
   }

   private BlockPos nextMineAreaBlock(Minecraft client, MacroRunner.MineAreaOptions options) {
      BlockPos nearest = null;
      double nearestDistance = Double.MAX_VALUE;

      for (BlockPos mutable : BlockPos.betweenClosed(options.min(), options.max())) {
         BlockPos pos = mutable.immutable();
         if (client.level.isLoaded(pos) && !client.level.getBlockState(pos).isAir()) {
            double distance = client.player.distanceToSqr(Vec3.atCenterOf(pos));
            if (distance < nearestDistance) {
               nearestDistance = distance;
               nearest = pos;
            }
         }
      }

      return nearest;
   }

   private static int mineToolLowThreshold(String options) {
      int toolLowThreshold = 10;

      for (String token : options.split("[,\\s]+")) {
         String normalized = token.trim();
         if (normalized.startsWith("tool=")) {
            toolLowThreshold = Math.max(0, parseInt(normalized.substring("tool=".length()), toolLowThreshold));
         } else if (normalized.startsWith("low=")) {
            toolLowThreshold = Math.max(0, parseInt(normalized.substring("low=".length()), toolLowThreshold));
         } else if (normalized.startsWith("durability=")) {
            toolLowThreshold = Math.max(0, parseInt(normalized.substring("durability=".length()), toolLowThreshold));
         }
      }

      return toolLowThreshold;
   }

   private static boolean toolIsLow(Minecraft client, int threshold) {
      if (threshold <= 0) {
         return false;
      } else {
         ItemStack stack = client.player.getMainHandItem();
         return !stack.isEmpty() && stack.isDamageableItem() && stack.getMaxDamage() - stack.getDamageValue() <= threshold;
      }
   }

   private static boolean canReachBlock(Minecraft client, BlockPos pos) {
      return client.player.distanceToSqr(Vec3.atCenterOf(pos)) <= 25.0;
   }

   private boolean canReachFarmTarget(Minecraft client, MacroModel.Node node, MacroRunner.FarmOptions options, BlockPos target) {
      if (canReachBlock(client, target)) {
         this.releaseMovementKeys(client);
         return true;
      } else if (!options.move()) {
         this.fail(client, node, "Failed: Farm Area target is out of reach.");
         return false;
      } else {
         this.moveTowardBlock(client, target);
         if (this.activeTicks > 300) {
            this.releaseMovementKeys(client);
            this.fail(client, node, "Failed: Farm Area could not reach the crop.");
         }

         return false;
      }
   }

   private boolean moveToUnloadedArea(Minecraft client, MacroModel.Node node, MacroRunner.FarmOptions options) {
      if (options.radius() > 0 || this.areaHasLoadedPosition(client, options.min(), options.max())) {
         return false;
      } else if (!options.move()) {
         this.fail(client, node, "Failed: Farm Area is not loaded and move is false.");
         return true;
      } else {
         this.moveTowardBlock(client, areaCenter(options.min(), options.max()));
         if (this.activeTicks > 300) {
            this.releaseMovementKeys(client);
            this.fail(client, node, "Failed: Farm Area could not reach the area.");
         }

         return true;
      }
   }

   private boolean moveToUnloadedArea(Minecraft client, MacroModel.Node node, MacroRunner.MineAreaOptions options) {
      if (this.areaHasLoadedPosition(client, options.min(), options.max())) {
         return false;
      } else if (!options.move()) {
         this.fail(client, node, "Failed: Mine Area is not loaded and move is false.");
         return true;
      } else {
         this.moveTowardBlock(client, areaCenter(options.min(), options.max()));
         if (this.activeTicks > 300) {
            this.releaseMovementKeys(client);
            this.fail(client, node, "Failed: Mine Area could not reach the area.");
         }

         return true;
      }
   }

   private void moveTowardBlock(Minecraft client, BlockPos pos) {
      this.lookAt(client, Vec3.atCenterOf(pos));
      client.options.keyUp.setDown(true);
   }

   private boolean areaHasLoadedPosition(Minecraft client, BlockPos min, BlockPos max) {
      for (BlockPos mutable : BlockPos.betweenClosed(min, max)) {
         if (client.level.isLoaded(mutable)) {
            return true;
         }
      }

      return false;
   }

   private static BlockPos areaCenter(BlockPos min, BlockPos max) {
      return new BlockPos((min.getX() + max.getX()) / 2, (min.getY() + max.getY()) / 2, (min.getZ() + max.getZ()) / 2);
   }

   private static BlockPos minPos(BlockPos first, BlockPos second) {
      return new BlockPos(Math.min(first.getX(), second.getX()), Math.min(first.getY(), second.getY()), Math.min(first.getZ(), second.getZ()));
   }

   private static BlockPos maxPos(BlockPos first, BlockPos second) {
      return new BlockPos(Math.max(first.getX(), second.getX()), Math.max(first.getY(), second.getY()), Math.max(first.getZ(), second.getZ()));
   }

   private static boolean areaWithinLimit(BlockPos min, BlockPos max, int maxBlocksPerSide) {
      return max.getX() - min.getX() < maxBlocksPerSide && max.getY() - min.getY() < maxBlocksPerSide && max.getZ() - min.getZ() < maxBlocksPerSide;
   }

   private void openBlockAt(Minecraft client, BlockPos pos) {
      if (client.gameMode != null) {
         this.lookAt(client, Vec3.atCenterOf(pos));
         BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
         client.gameMode.useItemOn(client.player, InteractionHand.MAIN_HAND, hit);
         client.player.swing(InteractionHand.MAIN_HAND);
      }
   }

   private BlockPos nearestMatureCrop(Minecraft client, int radius) {
      return this.nearestMatureCrop(client, client.player.blockPosition().offset(-radius, -1, -radius), client.player.blockPosition().offset(radius, 1, radius));
   }

   private BlockPos nearestMatureCrop(Minecraft client, MacroRunner.FarmOptions options) {
      return options.radius() > 0 ? this.nearestMatureCrop(client, options.radius()) : this.nearestMatureCrop(client, options.min(), options.max());
   }

   private BlockPos nearestMatureCrop(Minecraft client, BlockPos min, BlockPos max) {
      BlockPos nearest = null;
      double nearestDistance = Double.MAX_VALUE;

      for (BlockPos mutable : BlockPos.betweenClosed(min, max)) {
         BlockPos pos = mutable.immutable();
         if (client.level.isLoaded(pos) && isMatureFarmCrop(client.level.getBlockState(pos))) {
            double distance = client.player.distanceToSqr(Vec3.atCenterOf(pos));
            if (distance < nearestDistance) {
               nearestDistance = distance;
               nearest = pos;
            }
         }
      }

      return nearest;
   }

   private static boolean isSupportedFarmCrop(BlockState state) {
      return !seedForFarmCrop(state).isBlank();
   }

   private static boolean isMatureFarmCrop(BlockState state) {
      String id = blockId(state);
      Integer age = ageValue(state);
      if (age == null) {
         return false;
      } else {
         return switch (id) {
            case "minecraft:wheat", "minecraft:carrots", "minecraft:potatoes" -> age >= 7;
            case "minecraft:beetroots", "minecraft:nether_wart" -> age >= 3;
            default -> false;
         };
      }
   }

   private static String seedForFarmCrop(BlockState state) {
      String var1 = blockId(state);

      return switch (var1) {
         case "minecraft:wheat" -> "minecraft:wheat_seeds";
         case "minecraft:carrots" -> "minecraft:carrot";
         case "minecraft:potatoes" -> "minecraft:potato";
         case "minecraft:beetroots" -> "minecraft:beetroot_seeds";
         case "minecraft:nether_wart" -> "minecraft:nether_wart";
         default -> "";
      };
   }

   private static Integer ageValue(BlockState state) {
      for (Property<?> property : state.getProperties()) {
         if (property instanceof IntegerProperty ageProperty && "age".equals(property.getName())) {
            return (Integer)state.getValue(ageProperty);
         }
      }

      return null;
   }

   private BlockPos nearestContainer(Minecraft client, int radius) {
      return this.nearestContainer(client, radius, "any");
   }

   private BlockPos nearestContainer(Minecraft client, int radius, String type) {
      BlockPos center = client.player.blockPosition();
      BlockPos nearest = null;
      double nearestDistance = Double.MAX_VALUE;

      for (BlockPos mutable : BlockPos.betweenClosed(center.offset(-radius, -2, -radius), center.offset(radius, 2, radius))) {
         BlockPos pos = mutable.immutable();
         if (client.level.isLoaded(pos) && isContainerBlock(client.level.getBlockState(pos), type)) {
            double distance = client.player.distanceToSqr(Vec3.atCenterOf(pos));
            if (distance < nearestDistance) {
               nearestDistance = distance;
               nearest = pos;
            }
         }
      }

      return nearest;
   }

   private static boolean isContainerBlock(BlockState state) {
      return isContainerBlock(state, "any");
   }

   private static boolean isContainerBlock(BlockState state, String type) {
      String id = blockId(state);
      String normalizedType = type == null ? "any" : type;
      boolean chest = id.equals("minecraft:chest") || id.equals("minecraft:trapped_chest");
      boolean barrel = id.equals("minecraft:barrel");
      boolean enderChest = id.equals("minecraft:ender_chest");
      boolean shulker = id.endsWith("_shulker_box");

      return switch (normalizedType) {
         case "chest" -> chest;
         case "barrel" -> barrel;
         case "ender_chest" -> enderChest;
         case "shulker" -> shulker;
         default -> chest || barrel || enderChest || shulker;
      };
   }

   private static String blockId(BlockState state) {
      return BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
   }

   private MacroRunner.DepositResult depositAnyInventoryStack(Minecraft client) {
      AbstractContainerMenu handler = activeContainerHandler(client);
      if (handler != null && client.gameMode != null) {
         int firstPlayerSlot = firstPlayerInventorySlot(handler);
         boolean hadStack = false;

         for (int index = firstPlayerSlot; index < handler.slots.size(); index++) {
            Slot slot = (Slot)handler.slots.get(index);
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty()) {
               hadStack = true;
               if (this.containerCanAccept(handler, firstPlayerSlot, stack)) {
                  client.gameMode.handleInventoryMouseClick(handler.containerId, slot.index, 0, ClickType.QUICK_MOVE, client.player);
                  return MacroRunner.DepositResult.MOVED;
               }
            }
         }

         return hadStack ? MacroRunner.DepositResult.NO_SPACE : MacroRunner.DepositResult.DONE;
      } else {
         return MacroRunner.DepositResult.NO_CONTAINER;
      }
   }

   private MacroRunner.DepositResult depositNextInventoryStack(Minecraft client, MacroModel.Node node) {
      AbstractContainerMenu handler = activeContainerHandler(client);
      if (handler != null && client.gameMode != null) {
         int firstPlayerSlot = firstPlayerInventorySlot(handler);
         boolean depositAll = !"specific".equalsIgnoreCase(node.value == null ? "" : node.value.trim());
         String specificItem = MacroModel.normalizeItemId(node.value2);
         boolean shiftClick = depositWithdrawShiftClick(node);
         boolean fastDeposit = fastDepositEnabled(node);
         Set<Integer> excludedSlots = parseInventorySlotSet(node.value5);
         if (!depositAll && !isKnownItemId(specificItem)) {
            return MacroRunner.DepositResult.INVALID_ITEM;
         } else if (excludedSlots == null) {
            return MacroRunner.DepositResult.INVALID_SLOTS;
         } else {
            boolean moved = false;
            boolean blocked = false;

            for (int index = firstPlayerSlot; index < handler.slots.size(); index++) {
               int playerSlotNumber = playerInventorySlotNumber(handler, index);
               if (excludedSlots.contains(playerSlotNumber)) {
                  continue;
               }

               Slot slot = (Slot)handler.slots.get(index);
               ItemStack stack = slot.getItem();
               if (!stack.isEmpty() && (depositAll || stackMatches(stack, specificItem))) {
                  if (!this.containerCanAccept(handler, firstPlayerSlot, stack)) {
                     blocked = true;
                     continue;
                  }

                  if (shiftClick) {
                     client.gameMode.handleInventoryMouseClick(handler.containerId, slot.index, 0, ClickType.QUICK_MOVE, client.player);
                  } else if (!this.pickupMoveStack(client, handler, index, 0, firstPlayerSlot, stack)) {
                     blocked = true;
                     continue;
                  }

                  moved = true;
                  if (!fastDeposit) {
                     return MacroRunner.DepositResult.MOVED;
                  }
               }
            }

            if (moved) {
               return MacroRunner.DepositResult.MOVED;
            } else {
               return blocked ? MacroRunner.DepositResult.NO_SPACE : MacroRunner.DepositResult.DONE;
            }
         }
      } else {
         return MacroRunner.DepositResult.NO_CONTAINER;
      }
   }

   private MacroRunner.DepositResult withdrawNextContainerStack(Minecraft client, MacroModel.Node node) {
      AbstractContainerMenu handler = activeContainerHandler(client);
      if (handler != null && client.gameMode != null) {
         int firstPlayerSlot = firstPlayerInventorySlot(handler);
         boolean withdrawAll = !"specific".equalsIgnoreCase(node.value == null ? "" : node.value.trim());
         String specificItem = MacroModel.normalizeItemId(node.value2);
         boolean shiftClick = depositWithdrawShiftClick(node);
         if (!withdrawAll && !isKnownItemId(specificItem)) {
            return MacroRunner.DepositResult.INVALID_ITEM;
         } else {
            for (int index = 0; index < firstPlayerSlot; index++) {
               Slot slot = (Slot)handler.slots.get(index);
               ItemStack stack = slot.getItem();
               if (!stack.isEmpty() && (withdrawAll || stackMatches(stack, specificItem)) && this.playerInventoryCanAccept(handler, firstPlayerSlot, stack)) {
                  if (shiftClick) {
                     client.gameMode.handleInventoryMouseClick(handler.containerId, slot.index, 0, ClickType.QUICK_MOVE, client.player);
                  } else if (!this.pickupMoveStack(client, handler, index, firstPlayerSlot, handler.slots.size(), stack)) {
                     return MacroRunner.DepositResult.NO_SPACE;
                  }

                  return MacroRunner.DepositResult.MOVED;
               }
            }

            return MacroRunner.DepositResult.DONE;
         }
      } else {
         return MacroRunner.DepositResult.NO_CONTAINER;
      }
   }

   private MacroRunner.DepositResult withdrawSpecificFromOpenContainer(Minecraft client, String itemId) {
      AbstractContainerMenu handler = activeContainerHandler(client);
      if (handler != null && client.gameMode != null) {
         int firstPlayerSlot = firstPlayerInventorySlot(handler);

         for (int index = 0; index < firstPlayerSlot; index++) {
            Slot slot = (Slot)handler.slots.get(index);
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty() && stackMatches(stack, itemId)) {
               if (!this.playerInventoryCanAccept(handler, firstPlayerSlot, stack)) {
                  return MacroRunner.DepositResult.NO_SPACE;
               }

               client.gameMode.handleInventoryMouseClick(handler.containerId, slot.index, 0, ClickType.QUICK_MOVE, client.player);
               return MacroRunner.DepositResult.MOVED;
            }
         }

         return MacroRunner.DepositResult.DONE;
      } else {
         return MacroRunner.DepositResult.NO_CONTAINER;
      }
   }

   private static boolean depositWithdrawShiftClick(MacroModel.Node node) {
      return node == null || !"false".equalsIgnoreCase(node.value3 == null ? "" : node.value3.trim());
   }

   private static boolean fastDepositEnabled(MacroModel.Node node) {
      return Boolean.parseBoolean(node == null || node.value4 == null ? "" : node.value4.trim());
   }

   private boolean pickupMoveStack(Minecraft client, AbstractContainerMenu handler, int sourceSlotIndex, int targetStart, int targetEnd, ItemStack stack) {
      int targetSlotIndex = this.firstAcceptingSlotIndex(handler, targetStart, targetEnd, stack);
      if (targetSlotIndex < 0) {
         return false;
      } else {
         client.gameMode.handleInventoryMouseClick(handler.containerId, sourceSlotIndex, 0, ClickType.PICKUP, client.player);
         client.gameMode.handleInventoryMouseClick(handler.containerId, targetSlotIndex, 0, ClickType.PICKUP, client.player);
         if (!handler.getCarried().isEmpty()) {
            client.gameMode.handleInventoryMouseClick(handler.containerId, sourceSlotIndex, 0, ClickType.PICKUP, client.player);
         }

         return true;
      }
   }

   private int firstAcceptingSlotIndex(AbstractContainerMenu handler, int start, int end, ItemStack stack) {
      for (int index = Math.max(0, start); index < Math.min(end, handler.slots.size()); index++) {
         Slot slot = (Slot)handler.slots.get(index);
         if (slot.mayPlace(stack)) {
            ItemStack existing = slot.getItem();
            if (existing.isEmpty()) {
               return index;
            }

            int maxCount = Math.min(existing.getMaxStackSize(), slot.getMaxStackSize(existing));
            if (ItemStack.isSameItemSameComponents(existing, stack) && existing.getCount() < maxCount) {
               return index;
            }
         }
      }

      return -1;
   }

   private boolean containerCanAccept(AbstractContainerMenu handler, int firstPlayerSlot, ItemStack stack) {
      for (int index = 0; index < firstPlayerSlot; index++) {
         Slot slot = (Slot)handler.slots.get(index);
         if (slot.mayPlace(stack)) {
            ItemStack existing = slot.getItem();
            if (existing.isEmpty()) {
               return true;
            }

            int maxCount = Math.min(existing.getMaxStackSize(), slot.getMaxStackSize(existing));
            if (ItemStack.isSameItemSameComponents(existing, stack) && existing.getCount() < maxCount) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean playerInventoryCanAccept(AbstractContainerMenu handler, int firstPlayerSlot, ItemStack stack) {
      for (int index = firstPlayerSlot; index < handler.slots.size(); index++) {
         Slot slot = (Slot)handler.slots.get(index);
         if (slot.mayPlace(stack)) {
            ItemStack existing = slot.getItem();
            if (existing.isEmpty()) {
               return true;
            }

            int maxCount = Math.min(existing.getMaxStackSize(), slot.getMaxStackSize(existing));
            if (ItemStack.isSameItemSameComponents(existing, stack) && existing.getCount() < maxCount) {
               return true;
            }
         }
      }

      return false;
   }

   private static int firstPlayerInventorySlot(AbstractContainerMenu handler) {
      return Math.max(0, handler.slots.size() - 36);
   }

   private static int playerInventorySlotNumber(AbstractContainerMenu handler, int menuIndex) {
      int firstPlayerSlot = firstPlayerInventorySlot(handler);
      int offset = menuIndex - firstPlayerSlot;
      if (offset < 0 || offset >= PLAYER_INVENTORY_SLOT_COUNT) {
         return -1;
      } else {
         return offset >= 27 ? offset - 27 : offset + 9;
      }
   }

   private static int playerInventoryMenuSlot(int inventorySlot) {
      return inventorySlot >= 0 && inventorySlot < 9 ? inventorySlot + 36 : inventorySlot;
   }

   private boolean dropPlayerInventorySlot(Minecraft client, int inventorySlot, boolean dropStack) {
      if (client.gameMode == null || client.player == null || client.player.inventoryMenu == null) {
         return false;
      } else {
         client.gameMode
            .handleInventoryMouseClick(client.player.inventoryMenu.containerId, playerInventoryMenuSlot(inventorySlot), dropStack ? 1 : 0, ClickType.THROW, client.player);
         return true;
      }
   }

   private Integer firstMatchingPlayerInventorySlot(Minecraft client, String itemId, Set<Integer> excludedSlots) {
      int size = Math.min(PLAYER_INVENTORY_SLOT_COUNT, client.player.getInventory().getContainerSize());

      for (int slot = 0; slot < size; slot++) {
         if (!excludedSlots.contains(slot) && stackMatches(client.player.getInventory().getItem(slot), itemId)) {
            return slot;
         }
      }

      return null;
   }

   private int dropMatchingPlayerInventorySlots(Minecraft client, String itemId, boolean dropStack, Set<Integer> excludedSlots) {
      int dropped = 0;
      int size = Math.min(PLAYER_INVENTORY_SLOT_COUNT, client.player.getInventory().getContainerSize());

      for (int slot = 0; slot < size; slot++) {
         if (!excludedSlots.contains(slot) && stackMatches(client.player.getInventory().getItem(slot), itemId) && this.dropPlayerInventorySlot(client, slot, dropStack)) {
            dropped++;
         }
      }

      return dropped;
   }

   private static boolean dropAllMatchingDropItems(MacroModel.Node node) {
      return Boolean.parseBoolean(node == null || node.value4 == null ? "" : node.value4.trim());
   }

   private static Set<Integer> parseInventorySlotSet(String slotText) {
      Set<Integer> slots = new HashSet<>();
      String normalized = slotText == null ? "" : slotText.trim().toLowerCase(Locale.ROOT);
      if (normalized.isBlank()) {
         return slots;
      } else {
         for (String rawToken : normalized.split("[,;\\s]+")) {
            String token = cleanInventorySlotToken(rawToken);
            if (token.isBlank()) {
               continue;
            }

            int dash = token.indexOf('-');
            if (dash >= 0) {
               Integer start = parseIntOrNull(token.substring(0, dash).trim());
               Integer end = parseIntOrNull(token.substring(dash + 1).trim());
               if (start == null || end == null || !addInventorySlotRange(slots, start, end)) {
                  return null;
               }
            } else {
               Integer slot = parseIntOrNull(token);
               if (slot == null || !addInventorySlot(slots, slot)) {
                  return null;
               }
            }
         }

         return slots;
      }
   }

   private static String cleanInventorySlotToken(String token) {
      String normalized = token == null ? "" : token.trim().toLowerCase(Locale.ROOT);
      if (normalized.equals("exclude") || normalized.equals("skip") || normalized.equals("slot") || normalized.equals("slots") || normalized.equals("inv") || normalized.equals("inventory")) {
         return "";
      }

      for (String prefix : List.of("exclude=", "skip=", "slots=", "slot=", "inv=", "inventory=", "exclude:", "skip:", "slots:", "slot:", "inv:", "inventory:")) {
         if (normalized.startsWith(prefix)) {
            return normalized.substring(prefix.length()).trim();
         }
      }

      return normalized;
   }

   private static boolean addInventorySlotRange(Set<Integer> slots, int start, int end) {
      int min = Math.min(start, end);
      int max = Math.max(start, end);
      if (min < 0 || max >= PLAYER_INVENTORY_SLOT_COUNT) {
         return false;
      }

      for (int slot = min; slot <= max; slot++) {
         slots.add(slot);
      }

      return true;
   }

   private static boolean addInventorySlot(Set<Integer> slots, int slot) {
      if (slot < 0 || slot >= PLAYER_INVENTORY_SLOT_COUNT) {
         return false;
      } else {
         slots.add(slot);
         return true;
      }
   }

   private static Integer inventorySlotNumber(String slotText) {
      String normalized = cleanInventorySlotToken(slotText);
      if (normalized.isBlank()) {
         return null;
      } else {
         for (String prefix : List.of("slots ", "slot ", "inventory ", "inv ", "hotbar ")) {
            if (normalized.startsWith(prefix)) {
               normalized = normalized.substring(prefix.length()).trim();
               break;
            }
         }
      }

      Integer slot = parseIntOrNull(normalized);
      return slot != null && slot >= 0 && slot < PLAYER_INVENTORY_SLOT_COUNT ? slot : null;
   }

   private static Slot openContainerSlot(AbstractContainerMenu handler, String slotText) {
      Integer index = openContainerSlotIndex(handler, slotText);
      return index != null ? (Slot)handler.slots.get(index) : null;
   }

   private static ItemStack openContainerStack(Minecraft client, String slotText) {
      AbstractContainerMenu handler = activeContainerHandler(client);
      Slot slot = handler == null ? null : openContainerSlot(handler, slotText);
      return slot == null ? null : slot.getItem();
   }

   private static List<ItemStack> openContainerStacks(Minecraft client, SlotRange range) {
      AbstractContainerMenu handler = activeContainerHandler(client);
      if (handler == null) {
         return null;
      } else {
         List<ItemStack> stacks = new ArrayList<>();
         int start = Math.max(0, Math.min(range.start(), range.end()));
         int end = Math.min(handler.slots.size() - 1, Math.max(range.start(), range.end()));

         for (int index = start; index <= end; index++) {
            stacks.add(((Slot)handler.slots.get(index)).getItem());
         }

         return stacks;
      }
   }

   private static Integer openContainerSlotIndex(AbstractContainerMenu handler, String slotText) {
      Integer slot = openContainerSlotNumber(slotText);
      if (slot == null) {
         return null;
      } else {
         int firstPlayerSlot = firstPlayerInventorySlot(handler);
         if (slot >= 1 && slot <= firstPlayerSlot) {
            return slot - 1;
         } else {
            return slot >= 0 && slot < firstPlayerSlot ? slot : null;
         }
      }
   }

   private static boolean looksLikeOpenContainerSlotTarget(String slotText) {
      return openContainerSlotNumber(slotText) != null;
   }

   private static boolean isExplicitOpenContainerSlotTarget(String slotText) {
      String normalized = slotText == null ? "" : slotText.trim().toLowerCase(Locale.ROOT);
      return normalized.startsWith("gui") || normalized.startsWith("open") || normalized.startsWith("container") || normalized.startsWith("screen");
   }

   private static Integer openContainerSlotNumber(String slotText) {
      String normalized = slotText == null ? "" : slotText.trim().toLowerCase(Locale.ROOT);
      if (normalized.isBlank()) {
         return null;
      } else {
         normalized = normalized.replace('=', ' ');
         for (String prefix : List.of("gui", "open", "container", "screen")) {
            if (normalized.startsWith(prefix)) {
               normalized = normalized.substring(prefix.length()).trim();
               break;
            }
         }

         if (normalized.startsWith("slot")) {
            normalized = normalized.substring("slot".length()).trim();
         }

         return parseIntOrNull(normalized);
      }
   }

   private static AbstractContainerMenu activeContainerHandler(Minecraft client) {
      AbstractContainerScreen<?> screen = handledScreen(client);
      return screen != null ? screen.getMenu() : openContainerHandler(client);
   }

   private static AbstractContainerMenu openContainerHandler(Minecraft client) {
      return client != null && client.player != null && client.player.containerMenu != null && client.player.containerMenu != client.player.inventoryMenu
         ? client.player.containerMenu
         : null;
   }

   private static boolean hasOpenContainer(Minecraft client) {
      return openContainerHandler(client) != null;
   }

   private static AbstractContainerScreen<?> handledScreen(Minecraft client) {
      return client.screen instanceof AbstractContainerScreen<?> handledScreen ? handledScreen : null;
   }

   private static ItemStack playerInventoryStack(Minecraft client, String slotText) {
      Integer slot = playerInventorySlotIndex(slotText);
      return slot != null && slot < client.player.getInventory().getContainerSize() ? client.player.getInventory().getItem(slot) : null;
   }

   private static List<ItemStack> playerInventoryStacks(Minecraft client, SlotRange range) {
      int start = Math.min(range.start(), range.end());
      int end = Math.max(range.start(), range.end());
      if (start >= 1 && end <= 36) {
         start--;
         end--;
      }

      if (start < 0 || end >= 36) {
         return null;
      } else {
         List<ItemStack> stacks = new ArrayList<>();
         int maxSlot = Math.min(35, client.player.getInventory().getContainerSize() - 1);

         for (int index = start; index <= end && index <= maxSlot; index++) {
            stacks.add(client.player.getInventory().getItem(index));
         }

         return stacks;
      }
   }

   private static Integer playerInventorySlotIndex(String slotText) {
      String normalized = slotText == null ? "" : slotText.trim().toLowerCase(Locale.ROOT);
      if (normalized.isBlank()) {
         return null;
      } else {
         boolean hotbarOnly = false;
         if (normalized.startsWith("hotbar")) {
            hotbarOnly = true;
            normalized = normalized.substring("hotbar".length()).trim();
         }

         if (normalized.startsWith("slot")) {
            normalized = normalized.substring("slot".length()).trim();
         }

         Integer slot = parseIntOrNull(normalized);
         if (slot == null) {
            return null;
         } else if (!hotbarOnly) {
            if (slot >= 1 && slot <= 36) {
               return slot - 1;
            } else {
               return slot >= 0 && slot < 36 ? slot : null;
            }
         } else {
            return slot >= 1 && slot <= 9 ? slot - 1 : null;
         }
      }
   }

   private static ItemStack stackFromItemOrSlot(Minecraft client, String sourceText) {
      String source = sourceText == null ? "" : sourceText.trim();
      if (!source.isBlank() && !source.equalsIgnoreCase("held") && !source.equalsIgnoreCase("mainhand") && !source.equalsIgnoreCase("main_hand")) {
         if (isExplicitOpenContainerSlotTarget(source)) {
            return openContainerStack(client, source);
         }

         ItemStack slotStack = playerInventoryStack(client, source);
         if (slotStack != null) {
            return slotStack;
         } else {
            Item item = itemById(MacroModel.normalizeItemId(source));
            return item == null ? null : new ItemStack(item);
         }
      } else {
         return client.player.getMainHandItem();
      }
   }

   private static List<ItemStack> stacksFromItemOrSlot(Minecraft client, String sourceText) {
      String source = sourceText == null ? "" : sourceText.trim();
      SlotRange range = slotRange(source);
      if (range != null) {
         return range.openContainer() ? openContainerStacks(client, range) : playerInventoryStacks(client, range);
      } else {
         ItemStack stack = stackFromItemOrSlot(client, source);
         return stack == null ? null : List.of(stack);
      }
   }

   private void rememberOpenContainerTagMatch(MacroModel.Node node, String tagText) {
      String next = node.next("true");
      if (next == null || next.isBlank()) {
         this.clearOpenContainerTagMatch();
      } else {
         this.lastOpenContainerTagFilter = tagText;
         this.lastOpenContainerTagNextNodeId = next;
      }
   }

   private void clearOpenContainerTagMatch() {
      this.lastOpenContainerTagFilter = "";
      this.lastOpenContainerTagNextNodeId = "";
   }

   private static boolean isOpenContainerTagSource(String sourceText) {
      SlotRange range = slotRange(sourceText);
      return range != null && range.openContainer() || isExplicitOpenContainerSlotTarget(sourceText);
   }

   private Boolean openContainerSourceHasTag(Minecraft client, MacroModel.Node node, String tagText) {
      AbstractContainerMenu handler = activeContainerHandler(client);
      if (handler == null) {
         return null;
      } else {
         SlotRange range = slotRange(node.value);
         if (range != null && range.openContainer()) {
            int firstPlayerSlot = firstPlayerInventorySlot(handler);
            int start = Math.max(0, Math.min(range.start(), range.end()));
            int end = Math.min(firstPlayerSlot - 1, Math.max(range.start(), range.end()));
            return start > end ? false : this.openContainerSlotsHaveTag(client, node, handler, start, end, tagText);
         } else {
            Integer slotIndex = isExplicitOpenContainerSlotTarget(node.value) ? openContainerSlotIndex(handler, node.value) : null;
            return slotIndex == null ? null : this.openContainerSlotsHaveTag(client, node, handler, slotIndex, slotIndex, tagText);
         }
      }
   }

   private Boolean openContainerSlotsHaveTag(Minecraft client, MacroModel.Node node, AbstractContainerMenu handler, int start, int end, String tagText) {
      for (int index = start; index <= end && index < handler.slots.size(); index++) {
         Slot slot = (Slot)handler.slots.get(index);
         if (stackHasConfiguredTag(slot.getItem(), tagText)) {
            this.rememberOpenContainerTagMatch(node, tagText);
            if (clickMatchedTagEnabled(node)) {
               if (client.gameMode == null) {
                  return null;
               }

               client.gameMode.handleInventoryMouseClick(handler.containerId, slot.index, 0, ClickType.PICKUP, client.player);
            }

            return true;
         }
      }

      return false;
   }

   private static boolean clickMatchedTagEnabled(MacroModel.Node node) {
      return Boolean.parseBoolean(node.value3 == null ? "" : node.value3.trim());
   }

   private static boolean stackHasConfiguredTag(ItemStack stack, String tagText) {
      if (stack.isEmpty()) {
         return false;
      } else {
         PriceRange priceRange = priceRange(tagText);
         String remainingTagText = priceRange == null ? tagText : tagText.replace(priceRange.source(), " ").trim();
         if (priceRange != null && !stackHasPriceInRange(stack, priceRange)) {
            return false;
         }

         if (remainingTagText.isBlank()) {
            return priceRange != null;
         }

         TagKey<Item> itemTag = itemTagKey(remainingTagText);
         if (itemTag != null && stack.is(itemTag)) {
            return true;
         }

         String needle = tagSearchText(remainingTagText);
         return !needle.isBlank() && stackSearchText(stack).contains(needle);
      }
   }

   private static String stackSearchText(ItemStack stack) {
      StringBuilder text = new StringBuilder();
      text.append(BuiltInRegistries.ITEM.getKey(stack.getItem())).append(' ');
      text.append(stack.getHoverName().getString()).append(' ');
      try {
         for (Component line : stack.getTooltipLines(Item.TooltipContext.EMPTY, Minecraft.getInstance().player, TooltipFlag.NORMAL)) {
            text.append(line.getString()).append(' ');
         }
      } catch (RuntimeException ignored) {
      }

      text.append(stack.getComponents());
      return text.toString().toLowerCase(Locale.ROOT);
   }

   private static String tagSearchText(String tagText) {
      String normalized = tagText == null ? "" : tagText.trim().toLowerCase(Locale.ROOT);
      if (normalized.startsWith("#")) {
         normalized = normalized.substring(1);
      }

      return normalized;
   }

   private static boolean stackHasPriceInRange(ItemStack stack, PriceRange range) {
      Matcher matcher = PRICE_PATTERN.matcher(stackSearchText(stack));
      while (matcher.find()) {
         double price = parsePriceValue(matcher.group(1), matcher.group(2));
         if (!Double.isNaN(price) && price <= range.max()) {
            return true;
         }
      }

      return false;
   }

   private static boolean stackMatchesClickTarget(ItemStack stack, String itemId, String tagText, PriceRange priceRange) {
      if (stack.isEmpty()) {
         return false;
      } else if (!itemId.isBlank() && !stackMatches(stack, itemId)) {
         return false;
      } else if (priceRange != null && !stackHasPriceInRange(stack, priceRange)) {
         return false;
      } else {
         return tagText.isBlank() || stackHasConfiguredTag(stack, tagText);
      }
   }

   private static boolean clickTargetHasValidFilter(String target, String itemId) {
      return !itemId.isBlank() && isKnownItemId(itemId) || priceRange(target) != null || !clickTargetTagText(target).isBlank();
   }

   private static String clickTargetItemText(String target) {
      String text = removePriceRange(target);
      String[] parts = text.trim().split("\\s+", 2);
      return parts.length == 0 ? "" : parts[0];
   }

   private static String clickTargetItemId(String target) {
      String itemText = clickTargetItemText(target);
      return itemText.isBlank() ? "" : MacroModel.normalizeItemId(itemText);
   }

   private static String clickTargetTagText(String target) {
      String text = removePriceRange(target).trim();
      int firstSpace = text.indexOf(' ');
      return firstSpace < 0 ? "" : text.substring(firstSpace + 1).trim();
   }

   private static String removePriceRange(String text) {
      Matcher matcher = PRICE_RANGE_PATTERN.matcher(text == null ? "" : text);
      return matcher.find() ? matcher.replaceFirst(" ").trim() : (text == null ? "" : text.trim());
   }

   private static PriceRange priceRange(String text) {
      Matcher matcher = PRICE_RANGE_PATTERN.matcher(text == null ? "" : text);
      if (!matcher.find()) {
         return null;
      } else {
         double first = parsePriceValue(matcher.group(1), matcher.group(2));
         double second = parsePriceValue(matcher.group(3), matcher.group(4));
         return Double.isNaN(first) || Double.isNaN(second)
            ? null
            : new PriceRange(matcher.group(), Math.min(first, second), Math.max(first, second));
      }
   }

   private static double parsePriceValue(String numberText, String suffixText) {
      try {
         double value = Double.parseDouble(numberText.replace(",", ""));
         return value * switch ((suffixText == null ? "" : suffixText).toLowerCase(Locale.ROOT)) {
            case "k" -> 1_000.0;
            case "m" -> 1_000_000.0;
            case "b" -> 1_000_000_000.0;
            case "t" -> 1_000_000_000_000.0;
            default -> 1.0;
         };
      } catch (NumberFormatException var3) {
         return Double.NaN;
      }
   }

   private static SlotRange slotRange(String sourceText) {
      String normalized = sourceText == null ? "" : sourceText.trim().toLowerCase(Locale.ROOT).replace('=', ' ');
      if (normalized.isBlank()) {
         return null;
      } else {
         boolean openContainer = false;
         for (String prefix : List.of("gui", "open", "container", "screen")) {
            if (normalized.startsWith(prefix)) {
               openContainer = true;
               normalized = normalized.substring(prefix.length()).trim();
               break;
            }
         }

         if (normalized.startsWith("slots")) {
            normalized = normalized.substring("slots".length()).trim();
         } else if (normalized.startsWith("slot")) {
            normalized = normalized.substring("slot".length()).trim();
         }

         int dash = normalized.indexOf('-');
         if (dash < 0) {
            return null;
         } else {
            Integer start = parseIntOrNull(normalized.substring(0, dash).trim());
            Integer end = parseIntOrNull(normalized.substring(dash + 1).trim());
            if (start == null || end == null || start < 0 || end < 0) {
               return null;
            } else {
               return new SlotRange(openContainer || start > 35 || end > 35, start, end);
            }
         }
      }
   }

   private static boolean stackMatches(ItemStack stack, String itemId) {
      if (stack.isEmpty()) {
         return false;
      } else {
         ResourceLocation stackId = BuiltInRegistries.ITEM.getKey(stack.getItem());
         return stackId.toString().equals(MacroModel.normalizeItemId(itemId));
      }
   }

   private static boolean isKnownItemId(String itemId) {
      return itemById(itemId) != null;
   }

   private static Item itemById(String itemId) {
      String normalized = MacroModel.normalizeItemId(itemId);
      return BuiltInRegistries.ITEM.stream().filter(item -> BuiltInRegistries.ITEM.getKey(item).toString().equals(normalized)).findFirst().orElse(null);
   }

   private static boolean isKnownBlockId(String blockId) {
      String normalized = MacroModel.normalizeItemId(blockId);
      return BuiltInRegistries.BLOCK.stream().anyMatch(block -> BuiltInRegistries.BLOCK.getKey(block).toString().equals(normalized));
   }

   private static TagKey<Item> itemTagKey(String tagText) {
      ResourceLocation id = identifierFromText(tagText);
      return id == null ? null : TagKey.create(Registries.ITEM, id);
   }

   private static ResourceLocation identifierFromText(String value) {
      String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
      if (normalized.startsWith("#")) {
         normalized = normalized.substring(1);
      }

      if (normalized.isBlank()) {
         return null;
      } else {
         String namespace = "minecraft";
         String path = normalized;
         int separator = normalized.indexOf(58);
         if (separator >= 0) {
            namespace = normalized.substring(0, separator);
            path = normalized.substring(separator + 1);
         }

         if (!namespace.isBlank() && !path.isBlank() && !path.contains(" ")) {
            try {
               return ResourceLocation.fromNamespaceAndPath(namespace, path);
            } catch (RuntimeException var6) {
               return null;
            }
         } else {
            return null;
         }
      }
   }

   private record PriceRange(String source, double min, double max) {
   }

   private record DurabilityCheck(boolean lower, double percent) {
   }

   private record SlotRange(boolean openContainer, int start, int end) {
   }

   private static DurabilityCheck durabilityCheck(String value) {
      String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
      if (normalized.isBlank()) {
         return null;
      }

      boolean higher = normalized.contains("higher")
         || normalized.contains("above")
         || normalized.contains("over")
         || normalized.contains(">=")
         || normalized.contains(">");
      boolean lower = !higher;
      String numberText = normalized.replaceAll("[^0-9.]+", " ").trim();
      if (numberText.isBlank()) {
         return null;
      }

      Double percent = parseDoubleOrNull(numberText.split("\\s+")[0]);
      if (percent == null || percent < 0.0 || percent > 100.0) {
         return null;
      }

      return new DurabilityCheck(lower, percent);
   }

   private static Vec3 parseVec3(String value) {
      if (value != null && !value.isBlank()) {
         String[] parts = value.trim().split("[,\\s]+");
         if (parts.length < 3) {
            return null;
         } else {
            try {
               return new Vec3(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
            } catch (NumberFormatException var3) {
               return null;
            }
         }
      } else {
         return null;
      }
   }

   private static BlockPos parseBlockPos(String value) {
      Vec3 vec = parseVec3(value);
      return vec == null ? null : BlockPos.containing(vec);
   }

   private static BlockPos parseFirstBlockPos(String value) {
      if (value != null && !value.isBlank()) {
         String[] parts = value.trim().split("[,\\s]+");
         if (parts.length < 3) {
            return null;
         } else {
            try {
               return BlockPos.containing(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
            } catch (NumberFormatException var3) {
               return null;
            }
         }
      } else {
         return null;
      }
   }

   private static double[] parseTwoDoubles(String value) {
      if (value != null && !value.isBlank()) {
         String[] parts = value.trim().split("[,\\s]+");
         if (parts.length < 2) {
            return null;
         } else {
            try {
               return new double[]{Double.parseDouble(parts[0]), Double.parseDouble(parts[1])};
            } catch (NumberFormatException var3) {
               return null;
            }
         }
      } else {
         return null;
      }
   }

   private static boolean truthy(String value) {
      String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
      return normalized.equals("1")
         || normalized.equals("true")
         || normalized.equals("on")
         || normalized.equals("yes")
         || normalized.equals("press")
         || normalized.equals("pressed")
         || normalized.equals("down");
   }

   private static boolean truthyWithDefault(String value, boolean fallback) {
      if (value != null && !value.isBlank()) {
         String normalized = value.trim().toLowerCase(Locale.ROOT);
         return !normalized.equals("0") && !normalized.equals("false") && !normalized.equals("off") && !normalized.equals("no") ? truthy(normalized) : false;
      } else {
         return fallback;
      }
   }

   private static Boolean parseBooleanOrDefault(String value, boolean fallback) {
      if (value != null && !value.isBlank()) {
         String normalized = value.trim().toLowerCase(Locale.ROOT);
         if (normalized.equals("1")
            || normalized.equals("true")
            || normalized.equals("on")
            || normalized.equals("yes")
            || normalized.equals("press")
            || normalized.equals("pressed")
            || normalized.equals("down")) {
            return true;
         } else {
            return !normalized.equals("0")
                  && !normalized.equals("false")
                  && !normalized.equals("off")
                  && !normalized.equals("no")
                  && !normalized.equals("release")
                  && !normalized.equals("released")
                  && !normalized.equals("up")
               ? null
               : false;
         }
      } else {
         return fallback;
      }
   }

   private static Boolean parseBooleanOrAction(String value) {
      return parseBooleanOrDefault(value, false);
   }

   private static Integer parseMouseButton(String value) {
      String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
      if (normalized.isBlank() || "0".equals(normalized) || "left".equals(normalized) || "attack".equals(normalized) || "left click".equals(normalized)) {
         return 0;
      } else {
         return "1".equals(normalized) || "right".equals(normalized) || "use".equals(normalized) || "right click".equals(normalized) ? 1 : null;
      }
   }

   private void setMouseButtonDown(Minecraft client, int button, boolean down) {
      if (button == 1) {
         client.options.keyUse.setDown(down);
         KeyMapping.set(client.options.keyUse.getDefaultKey(), down);
      } else {
         client.options.keyAttack.setDown(down);
         KeyMapping.set(client.options.keyAttack.getDefaultKey(), down);
      }
   }

   private void clickMouseButton(Minecraft client, int button) {
      if (this.performMouseButtonClick(client, button)) {
         return;
      }

      InputConstants.Key key = button == 1 ? client.options.keyUse.getDefaultKey() : client.options.keyAttack.getDefaultKey();
      KeyMapping.click(key);
      this.setMouseButtonDown(client, button, true);
      this.setMouseButtonDown(client, button, false);
   }

   private boolean performMouseButtonClick(Minecraft client, int button) {
      if (client.player == null || client.gameMode == null) {
         return false;
      } else {
         return button == 1 ? this.performUseClick(client) : this.performAttackClick(client);
      }
   }

   private boolean performAttackClick(Minecraft client) {
      if (client.hitResult instanceof EntityHitResult hit && hit.getType() == Type.ENTITY) {
         client.gameMode.attack(client.player, hit.getEntity());
         client.player.swing(InteractionHand.MAIN_HAND);
         return true;
      } else if (client.hitResult instanceof BlockHitResult hit && hit.getType() == Type.BLOCK) {
         client.gameMode.startDestroyBlock(hit.getBlockPos(), hit.getDirection());
         client.player.swing(InteractionHand.MAIN_HAND);
         return true;
      } else {
         return false;
      }
   }

   private boolean performUseClick(Minecraft client) {
      if (client.hitResult instanceof EntityHitResult hit && hit.getType() == Type.ENTITY) {
         InteractionResult result = client.gameMode.interact(client.player, hit.getEntity(), InteractionHand.MAIN_HAND);
         if (result.consumesAction()) {
            client.player.swing(InteractionHand.MAIN_HAND);
         }

         return true;
      } else if (client.hitResult instanceof BlockHitResult hit && hit.getType() == Type.BLOCK) {
         InteractionResult result = client.gameMode.useItemOn(client.player, InteractionHand.MAIN_HAND, hit);
         if (result.consumesAction()) {
            client.player.swing(InteractionHand.MAIN_HAND);
         }

         return true;
      } else {
         InteractionResult result = client.gameMode.useItem(client.player, InteractionHand.MAIN_HAND);
         if (result.consumesAction()) {
            client.player.swing(InteractionHand.MAIN_HAND);
         }

         return true;
      }
   }

   private static MacroRunner.AutoClickOptions autoClickOptions(String value) {
      String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
      if (normalized.isBlank()) {
         return new MacroRunner.AutoClickOptions(false, 1, 125, 50);
      }

      String[] tokens = normalized.split("[,\\s]+");
      boolean hold = false;
      int count = 1;
      int durationMs = 50;
      int intervalMs = 125;
      Double cps = null;

      for (int index = 0; index < tokens.length; index++) {
         String token = tokens[index];
         if (token.isBlank()) {
            continue;
         }

         String key = token;
         String parsedValue = "";
         int equals = token.indexOf('=');
         if (equals > 0) {
            key = token.substring(0, equals);
            parsedValue = token.substring(equals + 1);
         } else if (index + 1 < tokens.length && isAutoClickOptionName(token)) {
            parsedValue = tokens[++index];
         }

         switch (key) {
            case "hold", "held", "down" -> {
               hold = true;
               Integer parsed = parseDurationMs(parsedValue);
               if (parsed != null) {
                  durationMs = parsed;
               }
            }
            case "duration", "time", "ms", "holdms" -> {
               Integer parsed = parseDurationMs(parsedValue);
               if (parsed != null) {
                  durationMs = parsed;
               }
            }
            case "count", "clicks", "times" -> {
               Integer parsed = parseIntOrNull(parsedValue);
               if (parsed != null) {
                  count = parsed;
               }
            }
            case "cps" -> {
               Double parsed = parseDoubleOrNull(parsedValue);
               if (parsed != null) {
                  cps = parsed;
               }
            }
            case "interval", "delay" -> {
               Integer parsed = parseDurationMs(parsedValue);
               if (parsed != null) {
                  intervalMs = parsed;
               }
            }
            default -> {
               Integer parsedDuration = parseDurationMs(token);
               Integer parsedInt = parseIntOrNull(token);
               if (hold && parsedDuration != null) {
                  durationMs = parsedDuration;
               } else if (parsedInt != null) {
                  count = parsedInt;
               }
            }
         }
      }

      if (cps != null && cps > 0.0) {
         intervalMs = Math.max(1, (int)Math.round(1000.0 / cps));
      }

      return hold
         ? new MacroRunner.AutoClickOptions(true, 1, Math.max(1, durationMs), Math.max(1, intervalMs))
         : new MacroRunner.AutoClickOptions(false, Math.max(1, count), Math.max(1, durationMs), Math.max(1, intervalMs));
   }

   private static boolean isAutoClickOptionName(String value) {
      return switch (value) {
         case "hold", "held", "down", "duration", "time", "ms", "holdms", "count", "clicks", "times", "cps", "interval", "delay" -> true;
         default -> false;
      };
   }

   private static MacroRunner.PressKeyOptions pressKeyOptions(String value) {
      String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
      if (normalized.isBlank()) {
         return new MacroRunner.PressKeyOptions(false, 50);
      }

      String[] tokens = normalized.split("[,\\s]+");
      boolean hold = false;
      int durationMs = 50;

      for (int index = 0; index < tokens.length; index++) {
         String token = tokens[index];
         if (token.isBlank()) {
            continue;
         }

         String key = token;
         String parsedValue = "";
         int equals = token.indexOf('=');
         if (equals > 0) {
            key = token.substring(0, equals);
            parsedValue = token.substring(equals + 1);
         } else if (index + 1 < tokens.length && isPressKeyOptionName(token)) {
            parsedValue = tokens[++index];
         }

         switch (key) {
            case "hold", "held", "down" -> {
               hold = true;
               Integer parsed = parseDurationMs(parsedValue);
               if (parsed != null) {
                  durationMs = parsed;
               }
            }
            case "tap", "press", "click" -> {
               hold = false;
               Integer parsed = parseDurationMs(parsedValue);
               if (parsed != null) {
                  durationMs = parsed;
               }
            }
            case "duration", "time", "ms" -> {
               Integer parsed = parseDurationMs(parsedValue);
               if (parsed != null) {
                  durationMs = parsed;
               }
            }
            default -> {
               Integer parsed = parseDurationMs(token);
               if (parsed != null) {
                  durationMs = parsed;
               }
            }
         }
      }

      return new MacroRunner.PressKeyOptions(hold, Math.max(1, durationMs));
   }

   private static boolean isPressKeyOptionName(String value) {
      return switch (value) {
         case "hold", "held", "down", "tap", "press", "click", "duration", "time", "ms" -> true;
         default -> false;
      };
   }

   private static int durationTicks(int durationMs) {
      return Math.max(1, (int)Math.ceil(durationMs / 50.0));
   }

   private static Integer parseDurationMs(String value) {
      String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
      if (normalized.isBlank()) {
         return null;
      }

      try {
         if (normalized.endsWith("ms")) {
            return Math.max(1, (int)Math.round(Double.parseDouble(normalized.substring(0, normalized.length() - 2).trim())));
         } else if (normalized.endsWith("s")) {
            return Math.max(1, (int)Math.round(Double.parseDouble(normalized.substring(0, normalized.length() - 1).trim()) * 1000.0));
         } else {
            return Math.max(1, (int)Math.round(Double.parseDouble(normalized)));
         }
      } catch (NumberFormatException var2) {
         return null;
      }
   }

   private static InputConstants.Key keyFromText(String value) {
      String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
      if (normalized.isBlank()) {
         return null;
      }

      Integer keyCode = keyCodeFromName(normalized);
      InputConstants.Key key = keyCode == null
         ? InputConstants.getKey(normalized.startsWith("key.") ? normalized : "key.keyboard." + normalized.replace(' ', '.'))
         : InputConstants.getKey(keyCode, 0);
      return key == InputConstants.UNKNOWN ? null : key;
   }

   private static Integer keyCodeFromName(String value) {
      if (value.length() == 1) {
         char character = value.charAt(0);
         if (character >= 'a' && character <= 'z') {
            return InputConstants.KEY_A + character - 'a';
         }

         if (character >= '0' && character <= '9') {
            return InputConstants.KEY_0 + character - '0';
         }
      }

      if (value.startsWith("f")) {
         Integer number = parseIntOrNull(value.substring(1));
         if (number != null && number >= 1 && number <= 25) {
            return InputConstants.KEY_F1 + number - 1;
         }
      }

      Integer numeric = parseIntOrNull(value);
      if (numeric != null) {
         return numeric;
      }

      return switch (value.replace("_", "").replace("-", "").replace(" ", "")) {
         case "space" -> InputConstants.KEY_SPACE;
         case "tab" -> InputConstants.KEY_TAB;
         case "enter", "return" -> InputConstants.KEY_RETURN;
         case "esc", "escape" -> InputConstants.KEY_ESCAPE;
         case "backspace" -> InputConstants.KEY_BACKSPACE;
         case "delete", "del" -> InputConstants.KEY_DELETE;
         case "insert", "ins" -> InputConstants.KEY_INSERT;
         case "home" -> InputConstants.KEY_HOME;
         case "end" -> InputConstants.KEY_END;
         case "pageup", "pgup" -> InputConstants.KEY_PAGEUP;
         case "pagedown", "pgdn" -> InputConstants.KEY_PAGEDOWN;
         case "up", "arrowup" -> InputConstants.KEY_UP;
         case "down", "arrowdown" -> InputConstants.KEY_DOWN;
         case "left", "arrowleft" -> InputConstants.KEY_LEFT;
         case "right", "arrowright" -> InputConstants.KEY_RIGHT;
         case "shift", "leftshift", "lshift" -> InputConstants.KEY_LSHIFT;
         case "rightshift", "rshift" -> InputConstants.KEY_RSHIFT;
         case "ctrl", "control", "leftctrl", "leftcontrol", "lctrl", "lcontrol" -> InputConstants.KEY_LCONTROL;
         case "rightctrl", "rightcontrol", "rctrl", "rcontrol" -> InputConstants.KEY_RCONTROL;
         case "alt", "leftalt", "lalt" -> InputConstants.KEY_LALT;
         case "rightalt", "ralt" -> InputConstants.KEY_RALT;
         case "minus" -> InputConstants.KEY_MINUS;
         case "equals", "equal" -> InputConstants.KEY_EQUALS;
         case "comma" -> InputConstants.KEY_COMMA;
         case "period", "dot" -> InputConstants.KEY_PERIOD;
         case "slash" -> InputConstants.KEY_SLASH;
         case "backslash" -> InputConstants.KEY_BACKSLASH;
         case "semicolon" -> InputConstants.KEY_SEMICOLON;
         case "apostrophe", "quote" -> InputConstants.KEY_APOSTROPHE;
         case "grave", "backtick" -> InputConstants.KEY_GRAVE;
         case "leftbracket", "lbracket" -> InputConstants.KEY_LBRACKET;
         case "rightbracket", "rbracket" -> InputConstants.KEY_RBRACKET;
         default -> null;
      };
   }

   private static String[] signLines(String value) {
      String[] split = (value == null ? "" : value).split("\\|", -1);
      String[] lines = new String[]{"", "", "", ""};

      for (int index = 0; index < lines.length && index < split.length; index++) {
         lines[index] = split[index].trim();
      }

      return lines;
   }

   private static boolean eventTextMatches(String text, String filterText, String modeText) {
      String normalizedText = text == null ? "" : text;
      String filter = filterText == null ? "" : filterText.trim();
      String mode = modeText == null ? "any" : modeText.trim().toLowerCase(Locale.ROOT);
      boolean listed = filter.isBlank()
         || normalizedText.toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT))
         || nameMatchesList(normalizedText, filter)
         || nameMatchesRegex(normalizedText, filter);
      if ("allow".equals(mode)) {
         return listed;
      } else {
         return "block".equals(mode) ? !listed : listed;
      }
   }

   private static int parseScheduleDelayTicks(String value) {
      String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
      if (normalized.isBlank()) {
         return -1;
      } else {
         try {
            if (normalized.endsWith("ms")) {
               return Math.max(1, (int)Math.ceil(Integer.parseInt(normalized.substring(0, normalized.length() - 2).trim()) / 50.0));
            } else if (normalized.endsWith("s")) {
               return Math.max(1, Integer.parseInt(normalized.substring(0, normalized.length() - 1).trim()) * 20);
            } else {
               return normalized.endsWith("m")
                  ? Math.max(1, Integer.parseInt(normalized.substring(0, normalized.length() - 1).trim()) * 20 * 60)
                  : Math.max(1, Integer.parseInt(normalized) * 20);
            }
         } catch (NumberFormatException var3) {
            return -1;
         }
      }
   }

   private static LocalTime parseTimeOfDay(String value) {
      if (value != null && !value.isBlank() && value.contains(":")) {
         String[] parts = value.trim().split(":");
         if (parts.length < 2) {
            return null;
         } else {
            try {
               int hour = Math.max(0, Math.min(23, Integer.parseInt(parts[0])));
               int minute = Math.max(0, Math.min(59, Integer.parseInt(parts[1])));
               return LocalTime.of(hour, minute);
            } catch (NumberFormatException var4) {
               return null;
            }
         }
      } else {
         return null;
      }
   }

   private static int parseInt(String value, int fallback) {
      try {
         return Integer.parseInt(value == null ? "" : value.trim());
      } catch (NumberFormatException var3) {
         return fallback;
      }
   }

   private static int optionInt(String options, String prefix, int fallback) {
      if (options != null && !options.isBlank()) {
         for (String token : options.split("[,\\s]+")) {
            String normalized = token.trim().toLowerCase(Locale.ROOT);
            if (normalized.startsWith(prefix)) {
               return parseInt(normalized.substring(prefix.length()), fallback);
            }
         }

         return fallback;
      } else {
         return fallback;
      }
   }

   private static boolean optionBoolean(String options, String prefix, boolean fallback) {
      if (options != null && !options.isBlank()) {
         for (String token : options.split("[,\\s]+")) {
            String normalized = token.trim().toLowerCase(Locale.ROOT);
            if (normalized.startsWith(prefix)) {
               Boolean parsed = parseBooleanOrDefault(normalized.substring(prefix.length()), fallback);
               return parsed == null ? fallback : parsed;
            }
         }

         return fallback;
      } else {
         return fallback;
      }
   }

   private static boolean moveOption(String options, boolean fallback) {
      boolean prefixed = optionBoolean(options, "move=", fallback);
      if (prefixed == fallback && options != null && !options.isBlank()) {
         for (String token : options.split("[,\\s]+")) {
            String normalized = token.trim().toLowerCase(Locale.ROOT);
            if (normalized.equals("true") || normalized.equals("false")) {
               Boolean parsed = parseBooleanOrDefault(normalized, fallback);
               return parsed == null ? fallback : parsed;
            }
         }

         return fallback;
      } else {
         return prefixed;
      }
   }

   private static Integer parseIntOrNull(String value) {
      try {
         return Integer.parseInt(value == null ? "" : value.trim());
      } catch (NumberFormatException var2) {
         return null;
      }
   }

   private static double parseDouble(String value, double fallback) {
      try {
         return Double.parseDouble(value == null ? "" : value.trim());
      } catch (NumberFormatException var4) {
         return fallback;
      }
   }

   private static Double parseDoubleOrNull(String value) {
      try {
         return Double.parseDouble(value == null ? "" : value.trim());
      } catch (NumberFormatException var2) {
         return null;
      }
   }

   private static Boolean doubleAtOrBelow(double actual, String value) {
      Double threshold = parseDoubleOrNull(value);
      return threshold == null ? null : actual <= threshold;
   }

   private static Boolean intAtOrBelow(int actual, String value) {
      Integer threshold = parseIntOrNull(value);
      return threshold == null ? null : actual <= threshold;
   }

   private static Boolean intAtLeast(int actual, String value) {
      Integer threshold = parseIntOrNull(value);
      return threshold == null ? null : actual >= threshold;
   }

   private static boolean nameMatchesList(String playerName, String filterText) {
      for (String token : filterText.split("[,\\s]+")) {
         if (!token.isBlank() && playerName.equalsIgnoreCase(token.trim())) {
            return true;
         }
      }

      return false;
   }

   private static boolean nameMatchesRegex(String playerName, String regexText) {
      String regex = regexText.trim();
      if (regex.isEmpty()) {
         return false;
      } else {
         try {
            return playerName.matches(regex);
         } catch (PatternSyntaxException var4) {
            return false;
         }
      }
   }

   private void send(Minecraft client, String message) {
      if (this.settings.chatNotifications()) {
         if (client != null && client.player != null) {
            client.player.displayClientMessage(Component.literal("[Litemacro] " + message), false);
         }
      }
   }

   private void saveSettings() {
      try {
         this.settings.save(this.settingsPath);
      } catch (IOException var2) {
         MacroBuilderClient.LOGGER.error("Failed to save Litemacro settings", var2);
      }
   }

   private static String keyName(int keyCode) {
      return keyCode != -1 && keyCode >= 0 ? InputConstants.getKey(keyCode, 0).getDisplayName().getString() : "Unbound";
   }

   record OverlaySnapshot(int macroNumber, String macroName, String componentName, String status, int ticks, int activeCount) {
   }

   private record AutoClickOptions(boolean hold, int count, int durationMs, int intervalMs) {
   }

   private record BoneMealOptions(boolean refill, int radius, BlockPos target) {
   }

   private record ContainerSearchOptions(int radius, boolean move, String type) {
   }

   private static enum DepositResult {
      MOVED,
      DONE,
      NO_CONTAINER,
      INVALID_ITEM,
      INVALID_SLOTS,
      NO_SPACE;
   }

   private record DiscordResult(boolean success, String message) {
   }

   private record FarmOptions(int radius, BlockPos min, BlockPos max, boolean replant, boolean depositWhenFull, boolean move) {
   }

   private record MineAreaOptions(BlockPos min, BlockPos max, int toolLowThreshold, boolean move) {
   }

   private record PressKeyOptions(boolean hold, int durationMs) {
   }
}
