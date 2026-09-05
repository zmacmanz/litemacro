package com.alfre.macrobuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

final class MacroModel {
   static final String START = "official:start";
   static final String WAIT = "official:misc.wait";
   static final String CHAT = "official:misc.chat";
   static final String PLAYER_RESPAWN = "official:player.respawn";
   static final String PLAYER_MOVE = "official:player.move";
   static final String PLAYER_LOOK = "official:player.look";
   static final String PLAYER_SET_MOUSE_BUTTON = "official:player.setMouseButton";
   static final String PLAYER_SHOOT_BOW = "official:player.shootBow";
   static final String PLAYER_IS_ACCOUNT = "official:player.isAccount";
   static final String PLAYER_IN_ACTION = "official:player.inAction";
   static final String PLAYER_IS_ALIVE = "official:player.isAlive";
   static final String INVENTORY_FULL = "official:inventory.isFull";
   static final String ITEM_IN_INVENTORY = "official:inventory.hasItem";
   static final String LEGACY_ITEM_IN_INVENTORY = "builder:inventory.hasItem";
   static final String HEALTH_BELOW = "builder:player.healthBelow";
   static final String XP_LEVEL_AT_LEAST = "builder:player.xpLevelAtLeast";
   static final String PLAYER_AT_LOCATION = "official:player.isAtLocation";
   static final String PLAYER_SET_CROUCH = "official:player.setCrouch";
   static final String PLAYER_JUMP = "official:player.jump";
   static final String PLAYER_SET_SPRINT = "builder:player.setSprint";
   static final String PLAYER_AUTO_CLICK = "builder:player.autoClick";
   static final String PLAYER_PRESS_KEY = "builder:player.pressKey";
   static final String FOOD_BELOW = "builder:player.foodBelow";
   static final String PLAYER_ON_GROUND = "builder:player.onGround";
   static final String PLAYER_IN_WATER = "builder:player.inWater";
   static final String PLAYER_NEARBY = "official:entity.player_nearby";
   static final String HELD_ITEM_IS = "builder:inventory.heldItemIs";
   static final String SLOT_HAS_ITEM = "builder:inventory.slotHasItem";
   static final String ITEM_OR_SLOT_HAS_TAG = "builder:inventory.itemOrSlotHasTag";
   static final String ITEM_DURABILITY = "builder:inventory.itemDurability";
   static final String EMPTY_SLOTS_AT_LEAST = "builder:inventory.emptySlotsAtLeast";
   static final String OPEN_CONTAINER_HAS_ITEM = "builder:inventory.openContainerHasItem";
   static final String CLICK_OPEN_CONTAINER_SLOT = "official:inventory.clickOpenContainerSlot";
   static final String HOTBAR_SELECT = "official:inventory.hotbarSelect";
   static final String HOTBAR_USE = "official:inventory.hotbarUse";
   static final String DROP_ITEMS = "official:inventory.dropItems";
   static final String CHEST_WITHDRAW_ITEMS = "official:inventory.chestWithdrawItems";
   static final String CHEST_DEPOSIT_ITEMS = "official:inventory.chestDepositItems";
   static final String HAS_OPEN_CONTAINER = "official:inventory.hasOpenContainer";
   static final String IS_OPEN_CONTAINER_FULL = "official:inventory.isOpenContainerFull";
   static final String OPEN_PLAYER_INVENTORY = "official:inventory.openInventory";
   static final String CLOSE_OPEN_CONTAINER = "official:inventory.closeOpenContainer";
   static final String SELECT_HOTBAR_SLOT = "builder:inventory.selectHotbarSlot";
   static final String DROP_SELECTED_ITEM = "builder:inventory.dropSelectedItem";
   static final String WORLD_INTERACT_WITH_BLOCK = "official:world.interactWithBlock";
   static final String WORLD_MINE_BLOCK = "official:world.mineBlock";
   static final String WORLD_MINE_AREA = "builder:world.mineArea";
   static final String WORLD_PLACE_BLOCK = "official:world.placeBlock";
   static final String WORLD_JUMP_AND_PLACE_BLOCK = "official:world.jumpAndPlaceBlock";
   static final String WORLD_FARM_AREA = "builder:world.farmArea";
   static final String WORLD_OPEN_NEAREST_CONTAINER = "builder:world.openNearestContainer";
   static final String WORLD_AUTO_BONE_MEAL = "builder:world.autoBoneMeal";
   static final String WORLD_BLOCK_IS = "builder:world.blockIs";
   static final String LOOKING_AT_BLOCK = "builder:world.lookingAtBlock";
   static final String ENTITY_ATTACK = "official:entity.attack";
   static final String ENTITY_INTERACT = "official:entity.interact";
   static final String ENTITY_NEARBY = "builder:entity.nearby";
   static final String SCOREBOARD_CONTAINS = "builder:logic.scoreboardContains";
   static final String LOCAL_MESSAGE = "builder:localMessage";
   static final String STOP_MACRO = "builder:stop";
   static final String END_CONNECTION = "builder:flow.endConnection";
   static final String BUILDER_NOTE = "builder:flow.note";
   static final String REPEAT_MACRO = "builder:misc.repeatMacro";
   static final String REPEAT_SECTION = "builder:misc.repeatSection";
   static final String MISC_IDLE_UNTIL = "builder:misc.idleUntil";
   static final String MISC_DISCONNECT = "official:misc.disconnect";
   static final String MISC_REJOIN_SERVER = "official:misc.rejoinServer";
   static final String MISC_JOIN_SERVER = "official:misc.joinServer";
   static final String MISC_MACRO_STOP = "official:misc.macroStop";
   static final String MISC_MACRO_START = "official:misc.macroStart";
   static final String MISC_RANDOM = "official:misc.random";
   static final String MISC_RANDOM_OUTPUT_3 = "builder:misc.randomOutput3";
   static final String MISC_UPDATE_SIGN = "official:misc.updateSign";
   static final String MISC_IS_IN_LOBBY = "official:misc.isInLobby";
   static final String NOTIFICATION_DISCORD = "official:notification.discord";
   static final String LOGIN_REPEAT = "official:login.repeat";
   static final String EVENT_IF_CHAT_SAME = "builder:event.ifChatSame";
   static final String EVENT_CHAT = "official:event.chat";
   static final String EVENT_KICKED = "builder:event.kicked";
   static final String EVENT_DEATH = "official:event.death";
   static final String EVENT_DAMAGE_RECEIVED = "official:event.damageReceived";
   static final String EVENT_TELEPORT = "official:event.teleport";
   static final String EVENT_SCHEDULE = "official:event.schedule";
   static final String EVENT_PLAYER_SPAWNED = "official:event.player_spawned";
   static final String EVENT_PLAYER_DESPAWNED = "official:event.player_despawned";
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final List<String> COMPLETED_OR_FAILED = List.of("completed", "failed");
   private static final List<String> TRUE_FALSE_OR_FAILED = List.of("true", "false", "failed");
   private static final List<String> COMPLETED_TOOL_LOW_OR_FAILED = List.of("completed", "tool_low", "failed");
   private static final List<String> RANDOM_OUTPUT_3 = List.of("one", "two", "three");
   private static final List<String> REPEAT_COMPLETED_OR_FAILED = List.of("repeat", "completed", "failed");
   private static final List<MacroModel.Descriptor> DESCRIPTORS = List.of(
      new MacroModel.Descriptor("official:start", "Macro Entry Point", "Entry", -11961857, List.of("started"), true, "", "", ""),
      new MacroModel.Descriptor("official:player.respawn", "Respawn", "Player", -6765703, COMPLETED_OR_FAILED, true, "", "", ""),
      new MacroModel.Descriptor("official:player.move", "Move", "Player", -6765703, COMPLETED_OR_FAILED, true, "Direction or target", "Distance", "forward"),
      new MacroModel.Descriptor("official:player.look", "Look", "Player", -6765703, COMPLETED_OR_FAILED, true, "Yaw Pitch or target", "Target type", "0 0"),
      new MacroModel.Descriptor(
         "official:player.setMouseButton", "Mouse Button", "Player", -6765703, COMPLETED_OR_FAILED, true, "State: press/release", "Button: left/right", "press"
      ),
      new MacroModel.Descriptor("official:player.shootBow", "Shoot Bow", "Player", -6765703, COMPLETED_OR_FAILED, true, "Target", "Equip bow true/false", ""),
      new MacroModel.Descriptor("official:player.isAccount", "Is Account", "Player", -1726398, TRUE_FALSE_OR_FAILED, true, "Names", "", ""),
      new MacroModel.Descriptor("official:player.inAction", "Player In Action", "Player", -1726398, TRUE_FALSE_OR_FAILED, true, "State", "", "sneaking"),
      new MacroModel.Descriptor("official:player.isAlive", "Player Is Alive", "Player", -1726398, TRUE_FALSE_OR_FAILED, true, "", "", ""),
      new MacroModel.Descriptor("official:player.setCrouch", "Set Crouch", "Player", -6765703, COMPLETED_OR_FAILED, true, "Crouch true/false", "", "true"),
      new MacroModel.Descriptor("builder:player.setSprint", "Set Sprint", "Player", -6765703, COMPLETED_OR_FAILED, true, "Sprint true/false", "", "true"),
      new MacroModel.Descriptor("official:player.jump", "Jump", "Player", -6765703, COMPLETED_OR_FAILED, true, "", "", ""),
      new MacroModel.Descriptor(
         "builder:player.autoClick", "Auto Click", "Player", -6765703, COMPLETED_OR_FAILED, true, "Button: left/right", "Count/cps or hold ms", "left"
      ),
      new MacroModel.Descriptor(
         "builder:player.pressKey", "Press Key", "Player", -6765703, COMPLETED_OR_FAILED, true, "Key name/code", "Press/hold duration", "space"
      ),
      new MacroModel.Descriptor("official:inventory.isFull", "Inventory Is Full", "Logic", -1726398, TRUE_FALSE_OR_FAILED, true, "", "", ""),
      new MacroModel.Descriptor(
         "official:inventory.hasItem", "Inventory Has Item", "Logic", -1726398, TRUE_FALSE_OR_FAILED, true, "Item id", "Minimum count", "minecraft:bone_block"
      ),
      new MacroModel.Descriptor(
         "builder:inventory.hasItem", "Inventory Has Item", "Logic", -1726398, TRUE_FALSE_OR_FAILED, false, "Item id", "Minimum count", "minecraft:bone_block"
      ),
      new MacroModel.Descriptor("builder:player.healthBelow", "Health Below", "Logic", -2069387, TRUE_FALSE_OR_FAILED, true, "Health value", "", "10"),
      new MacroModel.Descriptor("builder:player.foodBelow", "Food Below", "Logic", -2069387, TRUE_FALSE_OR_FAILED, true, "Food value 0-20", "", "6"),
      new MacroModel.Descriptor("builder:player.xpLevelAtLeast", "XP Level At Least", "Logic", -4794025, TRUE_FALSE_OR_FAILED, true, "Level", "", "1"),
      new MacroModel.Descriptor("builder:player.onGround", "Player On Ground", "Logic", -1726398, TRUE_FALSE_OR_FAILED, true, "", "", ""),
      new MacroModel.Descriptor("builder:player.inWater", "Player In Water", "Logic", -1726398, TRUE_FALSE_OR_FAILED, true, "", "", ""),
      new MacroModel.Descriptor(
         "official:player.isAtLocation", "Player At Location", "Logic", -10375185, TRUE_FALSE_OR_FAILED, true, "X Y Z", "Max distance", "0 64 0"
      ),
      new MacroModel.Descriptor(
         "official:entity.player_nearby", "Player Nearby", "Logic", -10375185, TRUE_FALSE_OR_FAILED, true, "Names or regex", "Mode: any/allow/block", ""
      ),
      new MacroModel.Descriptor(
         "builder:logic.scoreboardContains", "Scoreboard Contains", "Logic", -1726398, TRUE_FALSE_OR_FAILED, true, "Text or regex", "Mode: contains/exact/regex", ""
      ),
      new MacroModel.Descriptor("official:misc.wait", "Wait", "Flow", -11094334, COMPLETED_OR_FAILED, true, "Duration ms", "", "10000"),
      new MacroModel.Descriptor("official:misc.chat", "Chat / Command", "Action", -6765703, COMPLETED_OR_FAILED, true, "Message", "", "/say Macro running"),
      new MacroModel.Descriptor(
         "official:inventory.clickOpenContainerSlot",
         "Click GUI Item",
         "Inventory",
         -3770147,
         COMPLETED_OR_FAILED,
         true,
         "Item id, tag, price, or GUI slot",
         "Shift click: true/false",
         "minecraft:bone_block"
      ),
      new MacroModel.Descriptor(
         "official:inventory.hotbarSelect", "Hotbar Select", "Inventory", -3040666, COMPLETED_OR_FAILED, true, "Item id or blank", "Slot 1-9", ""
      ),
      new MacroModel.Descriptor(
         "official:inventory.hotbarUse", "Hotbar Use", "Inventory", -3040666, COMPLETED_OR_FAILED, true, "Item id or blank", "Slot 1-9", ""
      ),
      new MacroModel.Descriptor(
         "official:inventory.dropItems", "Drop Items", "Inventory", -3040666, COMPLETED_OR_FAILED, true, "Item id, slot, or blank", "Drop stack true/false", ""
      ),
      new MacroModel.Descriptor(
         "official:inventory.chestWithdrawItems",
         "Withdraw Items",
         "Inventory",
         -3040666,
         COMPLETED_OR_FAILED,
         true,
         "Mode: all/specific",
         "Specific item",
         "all"
      ),
      new MacroModel.Descriptor(
         "official:inventory.chestDepositItems",
         "Deposit Items",
         "Inventory",
         -3040666,
         COMPLETED_OR_FAILED,
         true,
         "Mode: all/specific",
         "Specific item",
         "all"
      ),
      new MacroModel.Descriptor(
         "builder:inventory.heldItemIs", "Held Item Is", "Inventory", -1726398, TRUE_FALSE_OR_FAILED, true, "Item id", "", "minecraft:bone_block"
      ),
      new MacroModel.Descriptor(
         "builder:inventory.slotHasItem", "Slot Has Item", "Inventory", -1726398, TRUE_FALSE_OR_FAILED, true, "Slot 1-36", "Item id", "1"
      ),
      new MacroModel.Descriptor(
         "builder:inventory.itemOrSlotHasTag",
         "Item/Slot Has Tag",
         "Inventory",
         -1726398,
         TRUE_FALSE_OR_FAILED,
         true,
         "Item id, held, slot, or GUI slots",
         "Tag/text or $price range",
         "held"
      ),
      new MacroModel.Descriptor(
         "builder:inventory.itemDurability",
         "Item Durability",
         "Inventory",
         -1726398,
         TRUE_FALSE_OR_FAILED,
         true,
         "Item, held, slot, or GUI slots",
         "Lower/Higher than %",
         "held"
      ),
      new MacroModel.Descriptor(
         "builder:inventory.emptySlotsAtLeast", "Empty Slots At Least", "Inventory", -1726398, TRUE_FALSE_OR_FAILED, true, "Minimum empty slots", "", "1"
      ),
      new MacroModel.Descriptor("official:inventory.hasOpenContainer", "Has Open GUI", "Inventory", -1726398, TRUE_FALSE_OR_FAILED, true, "", "", ""),
      new MacroModel.Descriptor("official:inventory.isOpenContainerFull", "Open GUI Is Full", "Inventory", -1726398, TRUE_FALSE_OR_FAILED, true, "", "", ""),
      new MacroModel.Descriptor(
         "builder:inventory.openContainerHasItem",
         "Open GUI Has Item",
         "Inventory",
         -1726398,
         TRUE_FALSE_OR_FAILED,
         true,
         "Item id",
         "Minimum count",
         "minecraft:bone_block"
      ),
      new MacroModel.Descriptor("official:inventory.openInventory", "Open Inventory", "Inventory", -3040666, COMPLETED_OR_FAILED, true, "", "", ""),
      new MacroModel.Descriptor("official:inventory.closeOpenContainer", "Close GUI", "Inventory", -2069387, COMPLETED_OR_FAILED, true, "", "", ""),
      new MacroModel.Descriptor(
         "builder:inventory.selectHotbarSlot", "Select Hotbar Slot", "Inventory", -3040666, COMPLETED_OR_FAILED, true, "Slot 1-9", "", "1"
      ),
      new MacroModel.Descriptor(
         "builder:inventory.dropSelectedItem", "Drop Selected Item", "Inventory", -3040666, COMPLETED_OR_FAILED, true, "Drop stack: true/false", "", "false"
      ),
      new MacroModel.Descriptor(
         "official:world.interactWithBlock", "Interact With Block", "World", -11094334, COMPLETED_OR_FAILED, true, "Target X Y Z", "Button 0/1", ""
      ),
      new MacroModel.Descriptor(
         "official:world.mineBlock", "Mine Block", "World", -11094334, COMPLETED_OR_FAILED, true, "Target X Y Z", "Select tool true/false", ""
      ),
      new MacroModel.Descriptor(
         "builder:world.mineArea", "Mine Area", "World", -11094334, COMPLETED_TOOL_LOW_OR_FAILED, true, "From X Y Z", "To X Y Z / tool low / move", "0 64 0"
      ),
      new MacroModel.Descriptor("official:world.placeBlock", "Place Block", "World", -11094334, COMPLETED_OR_FAILED, true, "Target X Y Z", "Select block", ""),
      new MacroModel.Descriptor(
         "official:world.jumpAndPlaceBlock", "Jump And Place Block", "World", -11094334, COMPLETED_OR_FAILED, true, "Select block", "", ""
      ),
      new MacroModel.Descriptor(
         "builder:world.farmArea", "Farm Area", "World", -11094334, COMPLETED_OR_FAILED, true, "From X Y Z or radius", "To X Y Z / options", "0 64 0"
      ),
      new MacroModel.Descriptor(
         "builder:world.openNearestContainer", "Open Nearest Container", "World", -11094334, COMPLETED_OR_FAILED, true, "Radius 1-16", "Move/type", "8"
      ),
      new MacroModel.Descriptor(
         "builder:world.autoBoneMeal", "Auto Bone Meal", "World", -11094334, COMPLETED_OR_FAILED, true, "Item id", "Options", "minecraft:bone_meal"
      ),
      new MacroModel.Descriptor("builder:world.blockIs", "Block At Location Is", "World", -1726398, TRUE_FALSE_OR_FAILED, true, "X Y Z", "Block id", "0 64 0"),
      new MacroModel.Descriptor("builder:world.lookingAtBlock", "Looking At Block", "World", -1726398, TRUE_FALSE_OR_FAILED, true, "Block id or blank", "", ""),
      new MacroModel.Descriptor(
         "official:entity.attack", "Attack Entity", "Entity", -3770147, COMPLETED_OR_FAILED, true, "Target/filter", "Move true/false", "minecraft:zombie"
      ),
      new MacroModel.Descriptor(
         "official:entity.interact", "Interact Entity", "Entity", -3770147, COMPLETED_OR_FAILED, true, "Target/filter", "Move true/false", "minecraft:villager"
      ),
      new MacroModel.Descriptor(
         "builder:entity.nearby", "Entity Nearby", "Entity", -1726398, TRUE_FALSE_OR_FAILED, true, "Target/filter", "Max distance", "minecraft:zombie"
      ),
      new MacroModel.Descriptor("builder:localMessage", "Local Message", "Action", -10375185, COMPLETED_OR_FAILED, true, "Message", "", "Macro message"),
      new MacroModel.Descriptor("builder:misc.repeatMacro", "Repeat Macro", "Flow", -7366491, List.of(), true, "Repeat count", "", "3"),
      new MacroModel.Descriptor("builder:misc.repeatSection", "Repeat", "Flow", -7366491, REPEAT_COMPLETED_OR_FAILED, true, "Repeat count", "", "3"),
      new MacroModel.Descriptor(
         "builder:misc.idleUntil", "Idle Until", "Flow", -7366491, List.of("triggered", "failed"), true, "Condition: chat/player/full/kicked", "Filter", "chat"
      ),
      new MacroModel.Descriptor("builder:flow.endConnection", "End Connection", "Flow", -7366491, List.of(), true, "", "", ""),
      new MacroModel.Descriptor("builder:flow.note", "Note", "Flow", -7366491, List.of(), true, "Text", "", "Write notes here"),
      new MacroModel.Descriptor("builder:stop", "Stop Macro", "Flow", -7366491, List.of(), true, "", "", ""),
      new MacroModel.Descriptor("official:misc.disconnect", "Disconnect", "Misc", -2069387, COMPLETED_OR_FAILED, true, "Permanent true/false", "", "true"),
      new MacroModel.Descriptor("official:misc.rejoinServer", "Rejoin Server", "Misc", -2069387, COMPLETED_OR_FAILED, true, "Delay ms", "Server IP blank=last", "3000"),
      new MacroModel.Descriptor("official:misc.joinServer", "Join Server", "Misc", -2069387, COMPLETED_OR_FAILED, true, "Server IP", "Delay ms", ""),
      new MacroModel.Descriptor("official:misc.macroStop", "Stop Macro Slot", "Misc", -7366491, COMPLETED_OR_FAILED, true, "Macro number", "", ""),
      new MacroModel.Descriptor("official:misc.macroStart", "Start Macro Slot", "Misc", -7366491, COMPLETED_OR_FAILED, true, "Macro number", "", ""),
      new MacroModel.Descriptor("official:misc.random", "Random True/False", "Misc", -11094334, TRUE_FALSE_OR_FAILED, true, "True percent", "", "50"),
      new MacroModel.Descriptor("builder:misc.randomOutput3", "Random Output 3", "Misc", -11094334, RANDOM_OUTPUT_3, true, "", "", ""),
      new MacroModel.Descriptor(
         "official:misc.updateSign", "Update Sign", "Misc", -11094334, COMPLETED_OR_FAILED, true, "Line 1 | Line 2 | Line 3 | Line 4", "", ""
      ),
      new MacroModel.Descriptor("official:misc.isInLobby", "Is In Lobby", "Misc", -1726398, TRUE_FALSE_OR_FAILED, true, "Player", "", ""),
      new MacroModel.Descriptor(
         "official:notification.discord", "Discord Notification", "Notification", -10983950, COMPLETED_OR_FAILED, true, "Webhook URL", "Message", ""
      ),
      new MacroModel.Descriptor("official:login.repeat", "Login Repeat", "Login", -4794025, COMPLETED_OR_FAILED, true, "Repeat count", "", "2"),
      new MacroModel.Descriptor(
         "builder:event.ifChatSame", "If Chat Same", "Event", -1726398, TRUE_FALSE_OR_FAILED, true, "Chat text", "Mode: same/contains/regex", ""
      ),
      new MacroModel.Descriptor(
         "official:event.chat", "Event: Chat", "Event", -10375185, List.of("triggered"), true, "Filter text/regex", "Mode: any/allow/block", ""
      ),
      new MacroModel.Descriptor(
         "builder:event.kicked", "If Kicked", "Event", -10375185, List.of("triggered"), true, "Reason text/regex", "Mode: any/allow/block", ""
      ),
      new MacroModel.Descriptor("official:event.death", "Event: Death", "Event", -10375185, List.of("triggered"), true, "", "", ""),
      new MacroModel.Descriptor("official:event.damageReceived", "Event: Damage", "Event", -10375185, List.of("triggered"), true, "", "", ""),
      new MacroModel.Descriptor("official:event.teleport", "Event: Teleport", "Event", -10375185, List.of("triggered"), true, "", "", ""),
      new MacroModel.Descriptor(
         "official:event.schedule", "Event: Schedule", "Event", -10375185, List.of("triggered"), true, "Time or cron", "Target type", "* * * * *"
      ),
      new MacroModel.Descriptor(
         "official:event.player_spawned",
         "Event: Player Spawned",
         "Event",
         -10375185,
         List.of("triggered"),
         true,
         "Filter text/regex",
         "Mode: any/allow/block",
         ""
      ),
      new MacroModel.Descriptor(
         "official:event.player_despawned",
         "Event: Player Despawned",
         "Event",
         -10375185,
         List.of("triggered"),
         true,
         "Filter text/regex",
         "Mode: any/allow/block",
         ""
      )
   );
   private final List<MacroModel.Node> nodes = new ArrayList<>();
   private int nextIndex = 1;
   private String name = "Macro 1";
   private int stepDelayMs;
   private boolean alwaysOn;
   private String autoStartMode = "off";
   private String autoStartFilter = "";
   private boolean randomDelayEnabled;
   private int randomDelayMinMs = 250;
   private int randomDelayMaxMs = 1250;

   static MacroModel createDefault() {
      MacroModel model = new MacroModel();
      MacroModel.Node start = model.addNode("official:start", 220, 120);
      start.outputs.clear();
      return model;
   }

   static MacroModel fromMineBotExport(InputStream input, String name) throws IOException {
      MacroModel var3;
      try (Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
         var3 = fromMineBotExport(reader, name);
      }

      return var3;
   }

   static MacroModel fromMineBotExport(Reader reader, String name) {
      JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
      JsonArray componentArray = root.getAsJsonArray("components");
      if (componentArray != null && !componentArray.isEmpty()) {
         MacroModel model = new MacroModel();
         model.setName(name);

         for (JsonElement componentElement : componentArray) {
            JsonObject componentObject = componentElement.getAsJsonObject();
            String type = componentType(componentObject);
            MacroModel.Descriptor descriptor = descriptor(type);
            if (descriptor.type().equals(type) || "official:misc.wait".equals(type)) {
               JsonObject data = componentObject.getAsJsonObject("data");
               String id = string(data, "instanceReferenceId", model.nextId());
               JsonObject position = componentObject.getAsJsonObject("render").getAsJsonObject("position");
               MacroModel.Node node = new MacroModel.Node(id, type, integer(position, "x", 240), integer(position, "y", 140));
               applyMineBotVariables(node, data);
               readMineBotOutputs(node, data);
               model.nodes.add(node);
            }
         }

         if (model.startNode() == null) {
            model.addNode("official:start", 220, 120);
         }

         model.repairStartOutputKey();
         model.repairOutputKeys();
         model.bumpNextIndexPastExistingIds();
         return model;
      } else {
         throw new IllegalArgumentException("MineBot export has no components.");
      }
   }

   static MacroModel load(Path path) throws IOException {
      if (Files.notExists(path)) {
         MacroModel model = createDefault();
         model.save(path);
         return model;
      } else {
         MacroModel var15;
         try (Reader reader = Files.newBufferedReader(path)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            MacroModel model = new MacroModel();
            model.name = string(root, "name", "Macro 1");
            model.nextIndex = Math.max(1, integer(root, "nextIndex", 1));
            model.stepDelayMs = Math.max(0, integer(root, "stepDelayMs", 0));
            model.alwaysOn = booleanValue(root.get("alwaysOn"), false);
            model.autoStartMode = normalizeAutoStartMode(string(root, "autoStartMode", "off"));
            model.autoStartFilter = string(root, "autoStartFilter", "");
            model.randomDelayEnabled = booleanValue(root.get("randomDelayEnabled"), false);
            model.randomDelayMinMs = Math.max(0, integer(root, "randomDelayMinMs", 250));
            model.randomDelayMaxMs = Math.max(model.randomDelayMinMs, integer(root, "randomDelayMaxMs", 1250));
            JsonArray nodeArray = root.getAsJsonArray("nodes");
            if (nodeArray != null) {
               for (JsonElement nodeElement : nodeArray) {
                  JsonObject nodeObject = nodeElement.getAsJsonObject();
                  MacroModel.Node node = new MacroModel.Node(
                     string(nodeObject, "id", model.nextId()),
                     string(nodeObject, "type", "official:misc.wait"),
                     integer(nodeObject, "x", 240),
                     integer(nodeObject, "y", 140)
                  );
                  node.value = string(nodeObject, "value", descriptor(node.type).defaultValue());
                  node.value2 = string(nodeObject, "value2", descriptor(node.type).defaultValue2());
                  node.value3 = string(nodeObject, "value3", defaultValue3(node.type));
                  node.value4 = string(nodeObject, "value4", defaultValue4(node.type));
                  node.value5 = string(nodeObject, "value5", defaultValue5(node.type));
                  node.delayMs = Math.max(-1, integer(nodeObject, "delayMs", -1));
                  node.enabled = booleanValue(nodeObject.get("enabled"), true);
                  if (BUILDER_NOTE.equals(node.type)) {
                     node.value = string(nodeObject, "stickyNoteText", node.value);
                     node.noteWidth = Math.max(0, integer(nodeObject, "stickyNoteWidth", 0));
                     node.noteHeight = Math.max(0, integer(nodeObject, "stickyNoteHeight", 0));
                  }

                  JsonObject outputsObject = nodeObject.getAsJsonObject("outputs");
                  if (outputsObject != null) {
                     for (String key : outputsObject.keySet()) {
                        node.outputs.put(key, outputString(outputsObject.get(key)));
                     }
                  }

                  JsonObject routesObject = nodeObject.getAsJsonObject("routes");
                  if (routesObject != null) {
                     for (String key : routesObject.keySet()) {
                        String route = string(routesObject, key, "");
                        if (!route.isBlank()) {
                           node.connectionRoutes.put(key, route);
                        }
                     }
                  }

                  JsonObject colorsObject = nodeObject.getAsJsonObject("linkColors");
                  if (colorsObject != null) {
                     for (String key : colorsObject.keySet()) {
                        String color = string(colorsObject, key, "");
                        if (!color.isBlank()) {
                           node.connectionColors.put(key, color);
                        }
                     }
                  }

                  model.nodes.add(node);
               }
            }

            if (model.startNode() == null) {
               model.addNode("official:start", 220, 120);
            }

            model.repairStartOutputKey();
            model.repairOutputKeys();
            model.bumpNextIndexPastExistingIds();
            var15 = model;
         }

         return var15;
      }
   }

   void save(Path path) throws IOException {
      Files.createDirectories(path.getParent());
      Files.writeString(path, this.toJson());
   }

   String toJson() {
      JsonObject root = new JsonObject();
      root.addProperty("format", "litemacro-v1");
      root.addProperty("name", this.name);
      root.addProperty("stepDelayMs", this.stepDelayMs);
      root.addProperty("alwaysOn", this.alwaysOn);
      root.addProperty("autoStartMode", this.autoStartMode);
      root.addProperty("autoStartFilter", this.autoStartFilter);
      root.addProperty("randomDelayEnabled", this.randomDelayEnabled);
      root.addProperty("randomDelayMinMs", this.randomDelayMinMs);
      root.addProperty("randomDelayMaxMs", this.randomDelayMaxMs);
      root.addProperty("nextIndex", this.nextIndex);
      JsonArray nodeArray = new JsonArray();

      for (MacroModel.Node node : this.nodes) {
         JsonObject nodeObject = new JsonObject();
         nodeObject.addProperty("id", node.id);
         nodeObject.addProperty("type", node.type);
         nodeObject.addProperty("x", node.x);
         nodeObject.addProperty("y", node.y);
         nodeObject.addProperty("value", node.value);
         nodeObject.addProperty("value2", node.value2);
         nodeObject.addProperty("value3", node.value3);
         nodeObject.addProperty("value4", node.value4);
         nodeObject.addProperty("value5", node.value5);
         nodeObject.addProperty("delayMs", node.delayMs);
         nodeObject.addProperty("enabled", node.enabled);
         if (BUILDER_NOTE.equals(node.type)) {
            nodeObject.addProperty("stickyNoteText", node.value == null ? "" : node.value);
            nodeObject.addProperty("stickyNoteWidth", node.noteWidth);
            nodeObject.addProperty("stickyNoteHeight", node.noteHeight);
         }

         JsonObject outputsObject = new JsonObject();

         for (Entry<String, String> output : node.outputs.entrySet()) {
            outputsObject.addProperty(output.getKey(), output.getValue());
         }

         nodeObject.add("outputs", outputsObject);
         if (!node.connectionRoutes.isEmpty()) {
            JsonObject routesObject = new JsonObject();

            for (Entry<String, String> route : node.connectionRoutes.entrySet()) {
               routesObject.addProperty(route.getKey(), route.getValue());
            }

            nodeObject.add("routes", routesObject);
         }

         if (!node.connectionColors.isEmpty()) {
            JsonObject colorsObject = new JsonObject();

            for (Entry<String, String> color : node.connectionColors.entrySet()) {
               colorsObject.addProperty(color.getKey(), color.getValue());
            }

            nodeObject.add("linkColors", colorsObject);
         }

         nodeArray.add(nodeObject);
      }

      root.add("nodes", nodeArray);
      return GSON.toJson(root);
   }

   MacroModel.Node addNode(String type, int x, int y) {
      MacroModel.Descriptor descriptor = descriptor(type);
      MacroModel.Node node = new MacroModel.Node(this.nextId(), descriptor.type(), x, y);
      node.value = descriptor.defaultValue();
      node.value2 = descriptor.defaultValue2();
      node.value3 = defaultValue3(descriptor.type());
      node.value4 = defaultValue4(descriptor.type());
      node.value5 = defaultValue5(descriptor.type());
      node.delayMs = -1;
      this.nodes.add(node);
      return node;
   }

   boolean removeNode(MacroModel.Node node) {
      if (node != null && !"official:start".equals(node.type)) {
         boolean removed = this.nodes.remove(node);
         if (removed) {
            for (MacroModel.Node existing : this.nodes) {
               Iterator<Entry<String, String>> iterator = existing.outputs.entrySet().iterator();

               while (iterator.hasNext()) {
                  Entry<String, String> output = iterator.next();
                  String remainingTargets = removeOutputTarget(output.getValue(), node.id);
                     if (remainingTargets.isBlank()) {
                        iterator.remove();
                        existing.clearConnectionRoutesForOutput(output.getKey());
                        existing.clearConnectionColorsForOutput(output.getKey());
                     } else {
                        if (!remainingTargets.equals(output.getValue())) {
                           existing.removeConnectionRoute(output.getKey(), node.id);
                           existing.removeConnectionColor(output.getKey(), node.id);
                        }

                        output.setValue(remainingTargets);
                  }
               }
            }
         }

         return removed;
      } else {
         return false;
      }
   }

   MacroModel.Node startNode() {
      for (MacroModel.Node node : this.nodes) {
         if ("official:start".equals(node.type)) {
            return node;
         }
      }

      return null;
   }

   List<MacroModel.Node> startNodes() {
      List<MacroModel.Node> starts = new ArrayList<>();

      for (MacroModel.Node node : this.nodes) {
         if ("official:start".equals(node.type)) {
            starts.add(node);
         }
      }

      return starts;
   }

   MacroModel.Node node(String id) {
      for (MacroModel.Node node : this.nodes) {
         if (node.id.equals(id)) {
            return node;
         }
      }

      return null;
   }

   List<MacroModel.Node> nodes() {
      return this.nodes;
   }

   boolean hasAnyConnection(MacroModel.Node node) {
      return node != null && (!node.outputs.isEmpty() || this.hasIncomingConnection(node));
   }

   boolean clearConnections(MacroModel.Node node) {
      if (node == null) {
         return false;
      }

      boolean changed = !node.outputs.isEmpty() || !node.connectionRoutes.isEmpty() || !node.connectionColors.isEmpty();
      node.outputs.clear();
      node.connectionRoutes.clear();
      node.connectionColors.clear();

      for (MacroModel.Node existing : this.nodes) {
         if (existing == node) {
            continue;
         }

         Iterator<Entry<String, String>> iterator = existing.outputs.entrySet().iterator();
         while (iterator.hasNext()) {
            Entry<String, String> output = iterator.next();
            String before = output.getValue();
            String remainingTargets = removeOutputTarget(before, node.id);
            if (!remainingTargets.equals(before)) {
               changed = true;
               existing.removeConnectionRoute(output.getKey(), node.id);
               existing.removeConnectionColor(output.getKey(), node.id);
               if (remainingTargets.isBlank()) {
                  iterator.remove();
                  existing.clearConnectionRoutesForOutput(output.getKey());
                  existing.clearConnectionColorsForOutput(output.getKey());
               } else {
                  output.setValue(remainingTargets);
               }
            }
         }
      }

      return changed;
   }

   boolean removeConnection(MacroModel.Node source, String outputKey, String targetId) {
      if (source == null || outputKey == null || targetId == null) {
         return false;
      }

      String before = source.outputs.get(outputKey);
      if (before == null || before.isBlank()) {
         source.removeConnectionRoute(outputKey, targetId);
         source.removeConnectionColor(outputKey, targetId);
         return false;
      }

      String remainingTargets = removeOutputTarget(before, targetId);
      boolean changed = !remainingTargets.equals(before);
      source.removeConnectionRoute(outputKey, targetId);
      source.removeConnectionColor(outputKey, targetId);
      if (remainingTargets.isBlank()) {
         source.outputs.remove(outputKey);
         source.clearConnectionRoutesForOutput(outputKey);
         source.clearConnectionColorsForOutput(outputKey);
      } else {
         source.outputs.put(outputKey, remainingTargets);
      }

      return changed;
   }

   private boolean hasIncomingConnection(MacroModel.Node node) {
      for (MacroModel.Node existing : this.nodes) {
         if (existing != node) {
            for (String targetList : existing.outputs.values()) {
               if (outputTargets(targetList).contains(node.id)) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   String name() {
      return this.name;
   }

   void setName(String name) {
      String trimmed = name == null ? "" : name.trim();
      this.name = trimmed.isEmpty() ? "Macro 1" : trimmed;
   }

   int stepDelayMs() {
      return this.stepDelayMs;
   }

   void setStepDelayMs(int stepDelayMs) {
      this.stepDelayMs = Math.max(0, stepDelayMs);
   }

   boolean alwaysOn() {
      return this.alwaysOn;
   }

   void setAlwaysOn(boolean alwaysOn) {
      this.alwaysOn = alwaysOn;
   }

   String autoStartMode() {
      return this.autoStartMode;
   }

   void setAutoStartMode(String autoStartMode) {
      this.autoStartMode = normalizeAutoStartMode(autoStartMode);
   }

   String autoStartFilter() {
      return this.autoStartFilter;
   }

   void setAutoStartFilter(String autoStartFilter) {
      this.autoStartFilter = autoStartFilter == null ? "" : autoStartFilter.trim();
   }

   boolean randomDelayEnabled() {
      return this.randomDelayEnabled;
   }

   void setRandomDelayEnabled(boolean randomDelayEnabled) {
      this.randomDelayEnabled = randomDelayEnabled;
   }

   int randomDelayMinMs() {
      return this.randomDelayMinMs;
   }

   int randomDelayMaxMs() {
      return this.randomDelayMaxMs;
   }

   void setRandomDelayRangeMs(int minMs, int maxMs) {
      int safeMin = Math.max(0, minMs);
      int safeMax = Math.max(0, maxMs);
      this.randomDelayMinMs = Math.min(safeMin, safeMax);
      this.randomDelayMaxMs = Math.max(safeMin, safeMax);
   }

   static List<MacroModel.Descriptor> paletteDescriptors() {
      List<MacroModel.Descriptor> palette = new ArrayList<>();

      for (MacroModel.Descriptor descriptor : DESCRIPTORS) {
         if (descriptor.inPalette()) {
            palette.add(descriptor);
         }
      }

      return palette;
   }

   static MacroModel.Descriptor descriptor(String type) {
      for (MacroModel.Descriptor descriptor : DESCRIPTORS) {
         if (descriptor.type().equals(type)) {
            return descriptor;
         }
      }

      return DESCRIPTORS.stream().filter(descriptorx -> "official:misc.wait".equals(descriptorx.type())).findFirst().orElse(DESCRIPTORS.get(0));
   }

   static boolean runsInMainMenu(String type) {
      return switch (type) {
         case "official:start",
            "official:misc.wait",
            "builder:flow.endConnection",
            "builder:flow.note",
            "builder:localMessage",
            "builder:misc.repeatMacro",
            "builder:misc.repeatSection",
            "builder:misc.idleUntil",
            "builder:stop",
            "official:misc.rejoinServer",
            "official:misc.joinServer",
            "official:misc.macroStart",
            "official:misc.macroStop",
            "official:misc.random",
            "builder:misc.randomOutput3",
            "official:notification.discord",
            "builder:event.kicked" -> true;
         default -> false;
      };
   }

   private static String description(String type) {
      return switch (type) {
         case "official:start" -> "First block every macro starts from";
         case "official:player.respawn" -> "Clicks respawn when the player is dead";
         case "official:player.move" -> "Moves toward a direction, place, or entity";
         case "official:player.look" -> "Turns the player toward a target";
         case "official:player.setMouseButton" -> "Presses or releases left/right click";
         case "official:player.shootBow" -> "Aims and fires a bow";
         case "official:player.isAccount" -> "Checks if your username matches";
         case "official:player.inAction" -> "Checks sneaking, using, dead, or alive";
         case "official:player.isAlive" -> "True when the player is alive";
         case "official:player.setCrouch" -> "Turns crouch on or off";
         case "builder:player.setSprint" -> "Turns sprint on or off";
         case "official:player.jump" -> "Makes the player jump once";
         case "builder:player.autoClick" -> "Clicks or holds left/right click with a count and interval";
         case "builder:player.pressKey" -> "Presses or holds a keyboard key for a set duration";
         case "official:inventory.isFull" -> "True when inventory has no empty slots";
         case "official:inventory.hasItem", "builder:inventory.hasItem" -> "Checks for an item and count";
         case "builder:player.healthBelow" -> "True when health is at or below value";
         case "builder:player.foodBelow" -> "True when hunger is at or below value";
         case "builder:player.xpLevelAtLeast" -> "True when XP level is high enough";
         case "builder:player.onGround" -> "True when player is touching ground";
         case "builder:player.inWater" -> "True when player is in water";
         case "official:player.isAtLocation" -> "Checks if player is near coordinates";
         case "official:entity.player_nearby" -> "Checks for nearby players";
         case "official:misc.wait" -> "Pauses the macro for a set time";
         case "official:misc.chat" -> "Sends a chat message or command";
         case "official:inventory.clickOpenContainerSlot" -> "Clicks a matching item or numbered slot in open GUI";
         case "official:inventory.hotbarSelect" -> "Selects a hotbar item or slot";
         case "official:inventory.hotbarUse" -> "Uses the selected hotbar item";
         case "official:inventory.dropItems" -> "Drops matching inventory or hotbar items";
         case "official:inventory.chestWithdrawItems" -> "Moves items from open GUI to inventory";
         case "official:inventory.chestDepositItems" -> "Moves inventory items into open GUI";
         case "builder:inventory.heldItemIs" -> "Checks the item in your main hand";
         case "builder:inventory.slotHasItem" -> "Checks an inventory slot for an item";
         case "builder:inventory.itemOrSlotHasTag" -> "Checks item tags or stack data text and can click matching GUI slots";
         case "builder:inventory.itemDurability" -> "Checks whether item durability is lower or higher than a percent";
         case "builder:inventory.emptySlotsAtLeast" -> "Checks free inventory slot count";
         case "official:inventory.hasOpenContainer" -> "True when a GUI or chest is open";
         case "official:inventory.isOpenContainerFull" -> "True when open GUI has no empty slots";
         case "builder:inventory.openContainerHasItem" -> "Checks for an item in open GUI";
         case "official:inventory.openInventory" -> "Opens the player inventory";
         case "official:inventory.closeOpenContainer" -> "Closes the current GUI or chest";
         case "builder:inventory.selectHotbarSlot" -> "Selects a numbered hotbar slot";
         case "builder:inventory.dropSelectedItem" -> "Drops the held hotbar item";
         case "official:world.interactWithBlock" -> "Clicks or attacks a target block";
         case "official:world.mineBlock" -> "Mines a target block";
         case "builder:world.mineArea" -> "Mines between two XYZ points with a tool-low output";
         case "official:world.placeBlock" -> "Places a selected block on target";
         case "official:world.jumpAndPlaceBlock" -> "Jumps and places below player";
         case "builder:world.farmArea" -> "Harvests crops between two XYZ points and can move";
         case "builder:world.openNearestContainer" -> "Finds and opens a selected container type";
         case "builder:world.autoBoneMeal" -> "Uses bone meal and can refill from nearby containers";
         case "builder:world.blockIs" -> "Checks block id at coordinates";
         case "builder:world.lookingAtBlock" -> "Checks the block you are looking at";
         case "official:entity.attack" -> "Attacks matching mob or player";
         case "official:entity.interact" -> "Right-clicks matching mob or player";
         case "builder:entity.nearby" -> "Checks for matching nearby entities";
         case "builder:logic.scoreboardContains" -> "Checks sidebar, team, and objective scoreboard text";
         case "builder:localMessage" -> "Shows a local chat notification";
         case "builder:misc.repeatMacro" -> "Repeats the whole macro a set number of times, then stops";
         case "builder:misc.repeatSection" -> "Repeats a linked section a set number of times";
         case "builder:misc.idleUntil" -> "Waits here until chat, player, full inventory, or disconnect is detected";
         case "builder:flow.endConnection" -> "Ends this connection path without stopping other running branches";
         case "builder:flow.note" -> "Adds a saved note to explain part of the macro";
         case "builder:stop" -> "Stops the current macro run";
         case "official:misc.disconnect" -> "Disconnects from the server";
         case "official:misc.rejoinServer" -> "Reconnects to the last multiplayer server or a typed server IP";
         case "official:misc.joinServer" -> "Connects to the typed multiplayer server IP";
         case "official:misc.macroStop" -> "Stops another macro slot";
         case "official:misc.macroStart" -> "Starts another macro slot";
         case "official:misc.random" -> "Randomly chooses true or false";
         case "builder:misc.randomOutput3" -> "Randomly chooses one of three outputs";
         case "official:misc.updateSign" -> "Updates the sign you are looking at";
         case "official:misc.isInLobby" -> "Checks if named player is you";
         case "official:notification.discord" -> "Sends a Discord webhook message";
         case "official:login.repeat" -> "Pass-through login repeat marker";
         case "builder:event.ifChatSame" -> "Checks latest chat against text";
         case "official:event.chat" -> "Triggers when matching chat appears";
         case "builder:event.kicked" -> "Triggers after a kick or disconnect";
         case "official:event.death" -> "Triggers when you die";
         case "official:event.damageReceived" -> "Triggers when you take damage";
         case "official:event.teleport" -> "Triggers after a teleport or world change";
         case "official:event.schedule" -> "Triggers on a timer or clock time";
         case "official:event.player_spawned" -> "Triggers when matching player appears";
         case "official:event.player_despawned" -> "Triggers when matching player leaves";
         default -> "Macro component";
      };
   }

   private String nextId() {
      return "node-" + this.nextIndex++;
   }

   private void bumpNextIndexPastExistingIds() {
      int highest = 0;

      for (MacroModel.Node node : this.nodes) {
         if (node.id.startsWith("node-")) {
            try {
               highest = Math.max(highest, Integer.parseInt(node.id.substring("node-".length())));
            } catch (NumberFormatException var5) {
            }
         }
      }

      this.nextIndex = Math.max(this.nextIndex, highest + 1);
   }

   private void repairStartOutputKey() {
      MacroModel.Node start = this.startNode();
      if (start != null && !start.outputs.containsKey("started") && start.outputs.containsKey("completed")) {
         start.outputs.put("started", start.outputs.remove("completed"));
      }
   }

   private void repairOutputKeys() {
      for (MacroModel.Node node : this.nodes) {
         MacroModel.Descriptor descriptor = descriptor(node.type);
         if (descriptor.outputs().isEmpty()) {
            node.outputs.clear();
            node.connectionRoutes.clear();
            node.connectionColors.clear();
         } else {
            boolean hasExpectedOutput = false;
            Iterator target = descriptor.outputs().iterator();

            while (true) {
               if (target.hasNext()) {
                  String expected = (String)target.next();
                  if (!node.outputs.containsKey(expected)) {
                     continue;
                  }

                  hasExpectedOutput = true;
               }

               if (!hasExpectedOutput) {
                  String targetx = firstOutputTarget(node);
                  node.outputs.clear();
                  if (targetx != null && !targetx.isBlank()) {
                     node.outputs.put(primaryRepairOutput(descriptor), targetx);
                  }
               } else {
                  node.outputs.keySet().removeIf(key -> {
                     boolean remove = !descriptor.outputs().contains(key);
                     if (remove) {
                        node.clearConnectionRoutesForOutput(key);
                        node.clearConnectionColorsForOutput(key);
                     }

                     return remove;
                  });
               }
               break;
            }
         }
      }
   }

   private static String primaryRepairOutput(MacroModel.Descriptor descriptor) {
      if (descriptor.outputs().contains("completed")) {
         return "completed";
      } else {
         return descriptor.outputs().contains("started") ? "started" : descriptor.outputs().get(0);
      }
   }

   private static String firstOutputTarget(MacroModel.Node node) {
      for (String target : node.outputs.values()) {
         List<String> targets = outputTargets(target);
         if (!targets.isEmpty()) {
            return targets.get(0);
         }
      }

      return null;
   }

   static List<String> outputTargets(String value) {
      List<String> targets = new ArrayList<>();
      if (value != null && !value.isBlank()) {
         for (String part : value.split("\\R")) {
            String trimmed = part.trim();
            if (!trimmed.isBlank() && !targets.contains(trimmed)) {
               targets.add(trimmed);
            }
         }
      }

      return targets;
   }

   static String addOutputTarget(String value, String targetId) {
      List<String> targets = outputTargets(value);
      String trimmed = targetId == null ? "" : targetId.trim();
      if (!trimmed.isBlank() && !targets.contains(trimmed)) {
         targets.add(trimmed);
      }

      return String.join("\n", targets);
   }

   static String removeOutputTarget(String value, String targetId) {
      List<String> targets = outputTargets(value);
      targets.removeIf(target -> target.equals(targetId));
      return String.join("\n", targets);
   }

   private static String outputString(JsonElement value) {
      if (value == null || value.isJsonNull()) {
         return "";
      } else if (value.isJsonArray()) {
         List<String> targets = new ArrayList<>();

         for (JsonElement element : value.getAsJsonArray()) {
            if (element != null && !element.isJsonNull()) {
               String target = element.getAsString().trim();
               if (!target.isBlank() && !targets.contains(target)) {
                  targets.add(target);
               }
            }
         }

         return String.join("\n", targets);
      } else {
         return value.getAsString();
      }
   }

   private static void applyMineBotVariables(MacroModel.Node node, JsonObject data) {
      Map<String, JsonElement> variables = readMineBotVariables(data);
      if ("official:misc.wait".equals(node.type)) {
         node.value = stringValue(variables.get("duration"), "10000");
      } else if ("official:player.move".equals(node.type)) {
         node.value = firstNonBlank(
            stringValue(variables.get("direction"), ""), locationString(variables.get("location"), ""), stringValue(variables.get("playerTarget"), "")
         );
         node.value2 = stringValue(variables.get("distance"), "1");
      } else if ("builder:misc.idleUntil".equals(node.type)) {
         node.value = stringValue(variables.get("condition"), "chat");
         node.value2 = stringValue(variables.get("filter"), "");
      } else if (!"official:player.look".equals(node.type)) {
         if ("official:player.setMouseButton".equals(node.type)) {
            node.value = stringValue(variables.get("action"), "0");
            node.value2 = stringValue(variables.get("button"), "0");
         } else if ("official:player.shootBow".equals(node.type)) {
            node.value = firstNonBlank(
               stringValue(variables.get("playerTarget"), ""),
               locationString(variables.get("locationClosestTo"), ""),
               stringValue(variables.get("entityFilter"), "")
            );
            node.value2 = stringValue(variables.get("equipBow"), "true");
         } else if ("official:player.isAccount".equals(node.type)) {
            node.value = stringValue(variables.get("list"), "");
         } else if ("official:player.inAction".equals(node.type)) {
            node.value = stringValue(variables.get("state"), "sneaking");
         } else if ("official:player.setCrouch".equals(node.type)) {
            node.value = dropdownStateToBooleanText(variables.get("desiredState"), true);
         } else if ("official:misc.chat".equals(node.type)) {
            node.value = stringValue(variables.get("message"), "");
         } else if ("official:inventory.clickOpenContainerSlot".equals(node.type)) {
            int selector = intValue(variables.get("itemSelector"), 0);
            String slotLocation = stringValue(variables.get("slotLocation"), "");
            node.value = selector == 1 && !slotLocation.isBlank()
               ? "slot " + slotLocation
               : normalizeItemId(stringValue(variables.get("specificItem"), "minecraft:bone_block"));
            node.value2 = Boolean.toString(booleanValue(variables.get("shiftClick"), false));
            node.value3 = stringValue(variables.get("button"), "left");
         } else if ("official:inventory.hotbarSelect".equals(node.type) || "official:inventory.hotbarUse".equals(node.type)) {
            node.value = stringValue(variables.get("specificItem"), "");
            node.value2 = stringValue(variables.get("slotLocation"), "1");
         } else if ("official:inventory.dropItems".equals(node.type)) {
            node.value = stringValue(variables.get("specificItem"), "");
            node.value2 = "false";
            node.value3 = stringValue(variables.get("excludeSlots"), "");
         } else if ("official:inventory.chestWithdrawItems".equals(node.type)) {
            int selector = intValue(variables.get("itemSelector"), 1);
            node.value = selector == 1 ? "all" : "specific";
            node.value2 = normalizeItemId(stringValue(variables.get("specificItem"), "minecraft:bone_block"));
            node.value3 = Boolean.toString(booleanValue(variables.get("shiftClick"), true));
         } else if ("official:inventory.chestDepositItems".equals(node.type)) {
            int selector = intValue(variables.get("itemSelector"), 1);
            node.value = selector == 1 ? "all" : "specific";
            node.value2 = normalizeItemId(stringValue(variables.get("specificItem"), "minecraft:bone_block"));
            node.value3 = Boolean.toString(booleanValue(variables.get("shiftClick"), true));
            node.value5 = stringValue(variables.get("excludeSlots"), "");
         } else if ("official:inventory.hasItem".equals(node.type) || "builder:inventory.hasItem".equals(node.type)) {
            node.value = normalizeItemId(stringValue(variables.get("item"), stringValue(variables.get("specificItem"), "minecraft:bone_block")));
            node.value2 = "1";
         } else if ("official:player.isAtLocation".equals(node.type)) {
            JsonElement location = variables.get("location");
            if (location != null && location.isJsonObject()) {
               JsonObject object = location.getAsJsonObject();
               node.value = doubleString(object.get("x"), "0") + " " + doubleString(object.get("y"), "64") + " " + doubleString(object.get("z"), "0");
            }

            node.value2 = stringValue(variables.get("maxDistance"), "2");
         } else if ("official:entity.player_nearby".equals(node.type)) {
            int filterType = intValue(variables.get("filterType"), 0);
            String filterText = stringValue(variables.get("filterText"), "");
            String regexText = stringValue(variables.get("regexText"), "");
            node.value = filterText.isBlank() ? regexText : filterText;
            node.value2 = filterType == 1 ? "allow" : (filterType == 2 ? "block" : "any");
         } else if ("official:world.interactWithBlock".equals(node.type)
            || "official:world.mineBlock".equals(node.type)
            || "official:world.placeBlock".equals(node.type)) {
            node.value = locationString(variables.get("target"), "");
            node.value2 = firstNonBlank(
               stringValue(variables.get("button"), ""), stringValue(variables.get("selectTool"), ""), stringValue(variables.get("selectBuildingBlock"), "")
            );
         } else if ("official:world.jumpAndPlaceBlock".equals(node.type)) {
            node.value = stringValue(variables.get("selectBuildingBlock"), "");
         } else if ("official:entity.attack".equals(node.type) || "official:entity.interact".equals(node.type)) {
            node.value = firstNonBlank(
               stringValue(variables.get("playerTarget"), ""),
               locationString(variables.get("locationClosestTo"), ""),
               stringValue(variables.get("entityFilter"), "")
            );
            node.value2 = stringValue(variables.get("moveToTarget"), "false");
         } else if ("official:misc.disconnect".equals(node.type)) {
            node.value = stringValue(variables.get("permanent"), "true");
         } else if ("official:misc.macroStart".equals(node.type) || "official:misc.macroStop".equals(node.type)) {
            node.value = stringValue(variables.get("macroId"), "");
         } else if ("official:misc.updateSign".equals(node.type)) {
            node.value = stringValue(variables.get("line1"), "")
               + " | "
               + stringValue(variables.get("line2"), "")
               + " | "
               + stringValue(variables.get("line3"), "")
               + " | "
               + stringValue(variables.get("line4"), "");
         } else if ("official:misc.isInLobby".equals(node.type)) {
            node.value = stringValue(variables.get("player"), "");
         } else if ("official:notification.discord".equals(node.type)) {
            node.value = stringValue(variables.get("target"), "");
            node.value2 = stringValue(variables.get("message"), "");
         } else if ("official:login.repeat".equals(node.type)) {
            node.value = stringValue(variables.get("repeatCount"), "2");
         } else if (!"official:event.chat".equals(node.type)
            && !"official:event.player_spawned".equals(node.type)
            && !"official:event.player_despawned".equals(node.type)) {
            if ("official:event.schedule".equals(node.type)) {
               node.value = firstNonBlank(stringValue(variables.get("time"), ""), stringValue(variables.get("cron"), "* * * * *"));
               node.value2 = stringValue(variables.get("targetType"), "0");
            }
         } else {
            int filterType = intValue(variables.get("filterType"), 0);
            String filterText = stringValue(variables.get("filterText"), "");
            String regexText = stringValue(variables.get("regexText"), "");
            node.value = filterText.isBlank() ? regexText : filterText;
            node.value2 = filterType == 1 ? "allow" : (filterType == 2 ? "block" : "any");
         }
      } else {
         String yaw = stringValue(variables.get("yaw"), "");
         String pitch = stringValue(variables.get("pitch"), "");
         node.value = firstNonBlank(
            yaw.isBlank() && pitch.isBlank() ? "" : yaw + " " + pitch,
            locationString(variables.get("location"), ""),
            stringValue(variables.get("playerTarget"), "")
         );
         node.value2 = stringValue(variables.get("targetType"), "0");
      }
   }

   private static void readMineBotOutputs(MacroModel.Node node, JsonObject data) {
      JsonObject connections = data == null ? null : data.getAsJsonObject("connections");
      JsonArray outputs = connections == null ? null : connections.getAsJsonArray("outputs");
      if (outputs != null) {
         for (JsonElement outputElement : outputs) {
            JsonObject outputObject = outputElement.getAsJsonObject();
            String key = string(outputObject, "key", "");
            String target = string(outputObject, "target", "");
            if (!key.isBlank() && !target.isBlank()) {
               node.outputs.put(key, target);
            }
         }
      }
   }

   private static Map<String, JsonElement> readMineBotVariables(JsonObject data) {
      Map<String, JsonElement> variables = new LinkedHashMap<>();
      JsonObject variablesObject = data == null ? null : data.getAsJsonObject("variables");
      JsonArray values = variablesObject == null ? null : variablesObject.getAsJsonArray("values");
      if (values == null) {
         return variables;
      } else {
         for (JsonElement valueElement : values) {
            JsonObject variableObject = valueElement.getAsJsonObject();
            String id = string(variableObject, "id", "");
            JsonObject wrappedValue = variableObject.getAsJsonObject("value");
            JsonElement value = wrappedValue == null ? null : wrappedValue.get("value");
            if (!id.isBlank() && value != null) {
               variables.put(id, value);
            }
         }

         return variables;
      }
   }

   private static String componentType(JsonObject component) {
      JsonObject metadata = component.getAsJsonObject("metadata");
      return metadata == null ? "" : string(metadata, "type", "");
   }

   private static String stringValue(JsonElement value, String fallback) {
      if (value == null || value.isJsonNull()) {
         return fallback;
      } else {
         return value.isJsonObject() ? fallback : value.getAsString();
      }
   }

   private static String firstNonBlank(String... values) {
      for (String value : values) {
         if (value != null && !value.isBlank()) {
            return value;
         }
      }

      return "";
   }

   private static String dropdownStateToBooleanText(JsonElement value, boolean fallback) {
      int state = intValue(value, fallback ? 1 : 0);
      return Boolean.toString(state != 0);
   }

   private static String locationString(JsonElement value, String fallback) {
      if (value == null || value.isJsonNull()) {
         return fallback;
      } else if (value.isJsonObject()) {
         JsonObject object = value.getAsJsonObject();
         return doubleString(object.get("x"), "0") + " " + doubleString(object.get("y"), "64") + " " + doubleString(object.get("z"), "0");
      } else {
         String text = stringValue(value, fallback);
         return text.isBlank() ? fallback : text;
      }
   }

   private static int intValue(JsonElement value, int fallback) {
      if (value != null && !value.isJsonNull()) {
         try {
            return value.getAsInt();
         } catch (RuntimeException var3) {
            return fallback;
         }
      } else {
         return fallback;
      }
   }

   private static boolean booleanValue(JsonElement value, boolean fallback) {
      if (value != null && !value.isJsonNull()) {
         try {
            return value.getAsBoolean();
         } catch (RuntimeException var3) {
            return fallback;
         }
      } else {
         return fallback;
      }
   }

   private static String defaultValue3(String type) {
      if ("official:inventory.chestDepositItems".equals(type) || "official:inventory.chestWithdrawItems".equals(type)) {
         return "true";
      } else {
         return "official:inventory.clickOpenContainerSlot".equals(type)
            ? "left"
            : ("builder:inventory.itemOrSlotHasTag".equals(type) ? "false" : "");
      }
   }

   private static String normalizeAutoStartMode(String value) {
      String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
      return switch (normalized) {
         case "always", "run", "start" -> "always";
         case "chat", "message" -> "chat";
         case "player", "nearby", "player_nearby" -> "player";
         case "inventory", "inventory_full", "full" -> "inventory_full";
         case "kick", "kicked", "disconnect", "disconnected" -> "kicked";
         default -> "off";
      };
   }

   private static String defaultValue4(String type) {
      return "official:inventory.chestDepositItems".equals(type) || "official:inventory.dropItems".equals(type) ? "false" : "";
   }

   private static String defaultValue5(String type) {
      return "";
   }

   private static String doubleString(JsonElement value, String fallback) {
      if (value != null && !value.isJsonNull()) {
         try {
            double number = value.getAsDouble();
            return number == Math.rint(number) ? Integer.toString((int)number) : Double.toString(number);
         } catch (RuntimeException var4) {
            return fallback;
         }
      } else {
         return fallback;
      }
   }

   private static String string(JsonObject object, String key, String fallback) {
      JsonElement value = object.get(key);
      return value != null && !value.isJsonNull() ? value.getAsString() : fallback;
   }

   private static int integer(JsonObject object, String key, int fallback) {
      JsonElement value = object.get(key);
      if (value != null && !value.isJsonNull()) {
         try {
            return value.getAsInt();
         } catch (RuntimeException var5) {
            return fallback;
         }
      } else {
         return fallback;
      }
   }

   static String normalizeItemId(String itemId) {
      String normalized = itemId == null ? "" : itemId.toLowerCase(Locale.ROOT).trim();
      if (normalized.isEmpty()) {
         return "minecraft:air";
      } else {
         return normalized.contains(":") ? normalized : "minecraft:" + normalized;
      }
   }

   record Descriptor(
      String type,
      String label,
      String group,
      int color,
      List<String> outputs,
      boolean inPalette,
      String primaryLabel,
      String secondaryLabel,
      String defaultValue
   ) {
      String description() {
         return MacroModel.description(this.type);
      }

      String defaultValue2() {
         if ("official:inventory.clickOpenContainerSlot".equals(this.type)) {
            return "false";
         } else if ("official:inventory.chestDepositItems".equals(this.type)) {
            return "minecraft:bone_block";
         } else if ("official:inventory.chestWithdrawItems".equals(this.type)) {
            return "minecraft:bone_block";
         } else if ("official:inventory.hasItem".equals(this.type)) {
            return "1";
         } else if ("builder:inventory.hasItem".equals(this.type)) {
            return "1";
         } else if ("builder:inventory.openContainerHasItem".equals(this.type)) {
            return "1";
         } else if ("builder:inventory.slotHasItem".equals(this.type)) {
            return "minecraft:bone_block";
         } else if ("builder:inventory.itemOrSlotHasTag".equals(this.type)) {
            return "minecraft:logs";
         } else if ("builder:inventory.itemDurability".equals(this.type)) {
            return "lower 20";
         } else if ("official:inventory.hotbarSelect".equals(this.type) || "official:inventory.hotbarUse".equals(this.type)) {
            return "1";
         } else if ("official:inventory.dropItems".equals(this.type)) {
            return "false";
         } else if ("official:player.setMouseButton".equals(this.type)) {
            return "left";
         } else if ("builder:player.autoClick".equals(this.type)) {
            return "count=1 cps=8";
         } else if ("builder:player.pressKey".equals(this.type)) {
            return "tap 100";
         } else if ("official:player.shootBow".equals(this.type)) {
            return "true";
         } else if ("official:player.look".equals(this.type)) {
            return "0";
         } else if ("official:world.interactWithBlock".equals(this.type)) {
            return "1";
         } else if ("official:world.mineBlock".equals(this.type) || "official:world.placeBlock".equals(this.type)) {
            return "true";
         } else if ("builder:world.mineArea".equals(this.type)) {
            return "0 64 0 tool=10 move=true";
         } else if ("builder:world.farmArea".equals(this.type)) {
            return "0 64 0 replant deposit move=true";
         } else if ("builder:world.openNearestContainer".equals(this.type)) {
            return "move=true type=chest";
         } else if ("builder:world.autoBoneMeal".equals(this.type)) {
            return "refill radius=8";
         } else if ("builder:world.blockIs".equals(this.type)) {
            return "minecraft:air";
         } else if ("official:entity.attack".equals(this.type) || "official:entity.interact".equals(this.type)) {
            return "false";
         } else if ("builder:entity.nearby".equals(this.type)) {
            return "6";
         } else if ("official:player.isAtLocation".equals(this.type)) {
            return "2";
         } else if ("official:entity.player_nearby".equals(this.type)) {
            return "any";
         } else if ("builder:misc.idleUntil".equals(this.type)) {
            return "";
         } else if ("official:event.chat".equals(this.type)
            || "official:event.player_spawned".equals(this.type)
            || "official:event.player_despawned".equals(this.type)) {
            return "any";
         } else {
            return "builder:event.ifChatSame".equals(this.type) ? "same" : "";
         }
      }
   }

   static final class Node {
      final String id;
      final String type;
      final Map<String, String> outputs = new LinkedHashMap<>();
      final Map<String, String> connectionRoutes = new LinkedHashMap<>();
      final Map<String, String> connectionColors = new LinkedHashMap<>();
      int x;
      int y;
      String value = "";
      String value2 = "";
      String value3 = "";
      String value4 = "";
      String value5 = "";
      int delayMs = -1;
      boolean enabled = true;
      int noteWidth = 0;
      int noteHeight = 0;

      private Node(String id, String type, int x, int y) {
         this.id = id;
         this.type = type;
         this.x = x;
         this.y = y;
      }

      MacroModel.Descriptor descriptor() {
         return MacroModel.descriptor(this.type);
      }

      String next(String outputKey) {
         List<String> targets = this.nextTargets(outputKey);
         return targets.isEmpty() ? null : targets.get(0);
      }

      List<String> nextTargets(String outputKey) {
         return MacroModel.outputTargets(this.outputs.get(outputKey));
      }

      void addTarget(String outputKey, String targetId) {
         this.outputs.put(outputKey, MacroModel.addOutputTarget(this.outputs.get(outputKey), targetId));
      }

      void removeTarget(String outputKey, String targetId) {
         String remainingTargets = MacroModel.removeOutputTarget(this.outputs.get(outputKey), targetId);
         if (remainingTargets.isBlank()) {
            this.outputs.remove(outputKey);
            this.clearConnectionRoutesForOutput(outputKey);
            this.clearConnectionColorsForOutput(outputKey);
         } else {
            this.outputs.put(outputKey, remainingTargets);
            this.removeConnectionRoute(outputKey, targetId);
            this.removeConnectionColor(outputKey, targetId);
         }
      }

      String connectionRoute(String outputKey, String targetId) {
         return this.connectionRoutes.get(connectionRouteKey(outputKey, targetId));
      }

      void setConnectionRoute(String outputKey, String targetId, double bendWorldX) {
         this.connectionRoutes.put(connectionRouteKey(outputKey, targetId), Double.toString(bendWorldX));
      }

      void removeConnectionRoute(String outputKey, String targetId) {
         this.connectionRoutes.remove(connectionRouteKey(outputKey, targetId));
      }

      void clearConnectionRoutesForOutput(String outputKey) {
         this.connectionRoutes.keySet().removeIf(key -> key.startsWith(outputKey + "->"));
      }

      String connectionColor(String outputKey, String targetId) {
         return this.connectionColors.get(connectionRouteKey(outputKey, targetId));
      }

      void setConnectionColor(String outputKey, String targetId, String color) {
         String key = connectionRouteKey(outputKey, targetId);
         if (color == null || color.isBlank()) {
            this.connectionColors.remove(key);
         } else {
            this.connectionColors.put(key, color);
         }
      }

      void removeConnectionColor(String outputKey, String targetId) {
         this.connectionColors.remove(connectionRouteKey(outputKey, targetId));
      }

      void clearConnectionColorsForOutput(String outputKey) {
         this.connectionColors.keySet().removeIf(key -> key.startsWith(outputKey + "->"));
      }

      private static String connectionRouteKey(String outputKey, String targetId) {
         return outputKey + "->" + targetId;
      }
   }
}
