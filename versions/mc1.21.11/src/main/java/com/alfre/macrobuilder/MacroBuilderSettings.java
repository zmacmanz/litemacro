package com.alfre.macrobuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

final class MacroBuilderSettings {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private final int[] macroKeys = new int[100];
   private boolean chatNotifications = true;

   MacroBuilderSettings() {
      for (int index = 0; index < this.macroKeys.length; index++) {
         this.macroKeys[index] = -1;
      }
   }

   static MacroBuilderSettings load(Path path) throws IOException {
      MacroBuilderSettings settings = new MacroBuilderSettings();
      if (Files.notExists(path)) {
         settings.save(path);
         return settings;
      } else {
         try (Reader reader = Files.newBufferedReader(path)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonElement notifications = root.get("chatNotifications");
            if (notifications != null && !notifications.isJsonNull()) {
               settings.chatNotifications = notifications.getAsBoolean();
            }

            JsonArray keys = root.getAsJsonArray("macroKeys");
            if (keys != null) {
               for (int index = 0; index < settings.macroKeys.length && index < keys.size(); index++) {
                  JsonElement key = keys.get(index);
                  if (key != null && !key.isJsonNull()) {
                     settings.macroKeys[index] = key.getAsInt();
                  }
               }
            }
         }

         return settings;
      }
   }

   void save(Path path) throws IOException {
      Files.createDirectories(path.getParent());
      JsonObject root = new JsonObject();
      root.addProperty("chatNotifications", this.chatNotifications);
      JsonArray keys = new JsonArray();

      for (int key : this.macroKeys) {
         keys.add(key);
      }

      root.add("macroKeys", keys);
      Files.writeString(path, GSON.toJson(root));
   }

   boolean chatNotifications() {
      return this.chatNotifications;
   }

   void setChatNotifications(boolean chatNotifications) {
      this.chatNotifications = chatNotifications;
   }

   int macroKey(int macroNumber) {
      return this.macroKeys[Math.max(1, Math.min(100, macroNumber)) - 1];
   }

   void setMacroKey(int macroNumber, int keyCode) {
      this.macroKeys[Math.max(1, Math.min(100, macroNumber)) - 1] = keyCode;
   }
}
