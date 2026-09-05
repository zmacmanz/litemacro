package com.alfre.macrobuilder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;

final class LitemacroMarketplaceService {
   private static final String CONFIG_FILE_NAME = "litemacro-marketplace.properties";
   private static final String DEFAULT_PROJECT_URL = "https://klhkfpdtpoipafarugop.supabase.co";
   private static final String DEFAULT_PUBLISHABLE_KEY = "sb_publishable_moPnPKqQ7DRbQIRuInf3yA_v14sPiJ7";
   static final String PROJECT_URL = cleanProjectUrl(marketplaceSetting("project_url", "projectUrl", "LITEMACRO_MARKETPLACE_PROJECT_URL", "litemacro.marketplace.project_url"));
   static final String PUBLISHABLE_KEY = marketplaceSetting(
      "publishable_key",
      "publishableKey",
      "LITEMACRO_MARKETPLACE_PUBLISHABLE_KEY",
      "litemacro.marketplace.publishable_key"
   );
   static final String PUBLIC_BUCKET_NAME = "graphs";
   static final int ANONYMOUS_UPLOAD_LIMIT = 5;
   private static final String UPLOADER_ID_FILE_NAME = "litemacro-marketplace-uploader.properties";
   private static final String UPLOADER_ID_KEY = "uploader_id";
   private static final String SUBMIT_PRESET_RPC = "submit_litemacro_marketplace_preset";
   private static final String DOWNLOAD_PRESET_RPC = "download_litemacro_marketplace_preset";
   private static final String INCREMENT_DOWNLOAD_RPC = "increment_preset_downloads";
   private static final String LIKE_PRESET_RPC = "like_litemacro_marketplace_preset";
   private static final long MAX_PRESET_FILE_SIZE_BYTES = 5L * 1024L * 1024L;
   private static final int FETCH_LIMIT = 200;
   private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

   private LitemacroMarketplaceService() {
   }

   static boolean isConfigured() {
      return !PROJECT_URL.isBlank() && !PUBLISHABLE_KEY.isBlank();
   }

   static String configurationHelp() {
      return "Marketplace backend not configured. Add config/" + CONFIG_FILE_NAME + " with project_url=https://your-project.supabase.co and publishable_key=sb_publishable_...";
   }

   static void requireConfigured() throws IOException {
      if (!isConfigured()) {
         throw new IOException(configurationHelp());
      }
   }

   static CompletableFuture<List<LitemacroMarketplacePreset>> fetchPublishedPresets(ListingMode mode) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            requireConfigured();
            return filterLitemacro(fetchPresets(buildPublishedListingsUrl(mode), PUBLISHABLE_KEY));
         } catch (Exception var2) {
            throw new RuntimeException("Failed to fetch marketplace presets", var2);
         }
      });
   }

   static CompletableFuture<Void> incrementDownload(String presetId) {
      return CompletableFuture.runAsync(() -> {
         try {
            requireConfigured();
            if (presetId == null || presetId.isBlank()) {
               return;
            }

            JsonObject payload = new JsonObject();
            payload.addProperty("target_preset_id", presetId);
            postRpcJson(INCREMENT_DOWNLOAD_RPC, payload, PUBLISHABLE_KEY, "Failed to update download count");
         } catch (Exception var3) {
            MacroBuilderClient.LOGGER.debug("Marketplace download count failed", var3);
         }
      });
   }

   static CompletableFuture<LitemacroMarketplacePreset> likePreset(String presetId) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            requireConfigured();
            if (presetId == null || presetId.isBlank()) {
               throw new IOException("Marketplace macro is missing an id.");
            }

            JsonObject payload = new JsonObject();
            payload.addProperty("target_preset_id", presetId);
            payload.addProperty("liker_install_id", uploaderInstallId());
            JsonObject result = postRpcJson(LIKE_PRESET_RPC, payload, PUBLISHABLE_KEY, "Failed to like marketplace macro");
            JsonObject row = result.has("preset") && result.get("preset").isJsonObject() ? result.getAsJsonObject("preset") : result;
            return parsePreset(row);
         } catch (Exception error) {
            throw new CompletionException(error);
         }
      });
   }

   static CompletableFuture<Path> downloadPresetToTempFile(LitemacroMarketplacePreset preset) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            requireConfigured();
            if (preset == null || preset.filePath() == null || preset.filePath().isBlank()) {
               throw new IOException("Marketplace macro has no file path.");
            }

            String slug = preset.slug() == null || preset.slug().isBlank() ? "litemacro-marketplace" : preset.slug();
            Path tempFile = Files.createTempFile("litemacro-" + sanitizeSlug(slug) + "-", ".json");
            if (isDatabasePreset(preset)) {
               JsonObject payload = new JsonObject();
               payload.addProperty("target_preset_id", preset.id());
               JsonObject macroJson = postRpcJson(DOWNLOAD_PRESET_RPC, payload, PUBLISHABLE_KEY, "Marketplace macro is unavailable");
               Files.writeString(tempFile, macroJson.toString(), StandardCharsets.UTF_8);
               return tempFile;
            }

            HttpRequest.Builder builder = HttpRequest.newBuilder()
               .timeout(Duration.ofSeconds(20))
               .uri(URI.create(buildPublicObjectUrl(preset.storageBucket(), preset.filePath())))
               .header("Accept", "application/json");

            HttpResponse<byte[]> response = HTTP_CLIENT.send(builder.GET().build(), HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
               throw new IOException("Marketplace download failed with HTTP " + response.statusCode());
            }

            Files.write(tempFile, response.body());
            return tempFile;
         } catch (Exception var5) {
            throw new RuntimeException("Failed to download marketplace macro", var5);
         }
      });
   }

   static CompletableFuture<SubmissionResult> submitPreset(PublishRequest request) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            requireConfigured();
            if (request == null || request.localPresetPath() == null || !Files.exists(request.localPresetPath())) {
               throw new IOException("Macro file is missing.");
            }

            requireWithinSizeLimit(request.localPresetPath());
            String slug = sanitizeSlug(request.slug());
            if (slug.isBlank()) {
               throw new IOException("Macro name is required.");
            }

            JsonElement macroJson = JsonParser.parseString(Files.readString(request.localPresetPath()));
            if (macroJson == null || !macroJson.isJsonObject()) {
               throw new IOException("Macro file is not valid JSON.");
            }

            JsonObject payload = new JsonObject();
            payload.addProperty("uploader_install_id", uploaderInstallId());
            payload.addProperty("preset_slug", slug);
            payload.addProperty("preset_name", blankToEmpty(request.name()));
            payload.addProperty("preset_author_name", blankToEmpty(request.authorName()));
            payload.addProperty("preset_description", blankToEmpty(request.description()));
            payload.add("preset_tags", toJsonArray(withLitemacroTag(request.tags())));
            payload.addProperty("preset_game_version", blankToEmpty(request.gameVersion()));
            payload.addProperty("preset_litemacro_version", blankToEmpty(request.litemacroVersion()));
            payload.add("macro_json", macroJson);
            JsonObject result = postRpcJson(SUBMIT_PRESET_RPC, payload, PUBLISHABLE_KEY, "Failed to submit marketplace macro");
            boolean accepted = !result.has("accepted") || result.get("accepted").getAsBoolean();
            int uploadCount = intValue(result, "upload_count");
            int remaining = intValue(result, "remaining");
            String message = firstNonBlank(nullableString(result, "message"), "Submitted for marketplace review.");
            if (!accepted) {
               throw new IOException(message);
            }

            return new SubmissionResult(uploadCount, remaining, message);
         } catch (Exception var7) {
            throw new CompletionException(var7);
         }
      });
   }

   static String uploaderInstallId() {
      try {
         Path path = FabricLoader.getInstance().getConfigDir().resolve(UPLOADER_ID_FILE_NAME);
         Properties properties = new Properties();
         if (Files.exists(path)) {
            try (var input = Files.newInputStream(path)) {
               properties.load(input);
            }
         }

         String uploaderId = firstNonBlank(properties.getProperty(UPLOADER_ID_KEY));
         if (uploaderId != null && !uploaderId.isBlank()) {
            return uploaderId.trim();
         }

         uploaderId = UUID.randomUUID().toString();
         properties.setProperty(UPLOADER_ID_KEY, uploaderId);
         Files.createDirectories(path.getParent());
         try (var output = Files.newOutputStream(path)) {
            properties.store(output, "Litemacro marketplace anonymous uploader id");
         }
         return uploaderId;
      } catch (Exception error) {
         MacroBuilderClient.LOGGER.warn("Failed to load marketplace uploader id", error);
         return UUID.nameUUIDFromBytes(("litemacro:" + FabricLoader.getInstance().getGameDir()).getBytes(StandardCharsets.UTF_8)).toString();
      }
   }

   static String litemacroVersionLabel() {
      String version = FabricLoader.getInstance()
         .getModContainer(MacroBuilderClient.MOD_ID)
         .map(container -> container.getMetadata().getVersion().getFriendlyString())
         .orElse("1.0.0");
      return "Litemacro " + version;
   }

   static String minecraftVersionLabel() {
      try {
         Object version = SharedConstants.getCurrentVersion();
         for (String methodName : List.of("getName", "name", "id")) {
            try {
               Object value = version.getClass().getMethod(methodName).invoke(version);
               if (value != null && !value.toString().isBlank()) {
                  return value.toString();
               }
            } catch (ReflectiveOperationException ignored) {
            }
         }
         return version.toString();
      } catch (Throwable var1) {
         return "Minecraft";
      }
   }

   private static String buildPublishedListingsUrl(ListingMode mode) {
      ListingMode selected = mode == null ? ListingMode.TRENDING : mode;
      return PROJECT_URL
         + "/rest/v1/marketplace_presets?select=id,slug,author_user_id,name,author_name,author_avatar_url,description,tags,game_version,litemacro_version,likes_count,downloads_count,storage_bucket,file_path,published,created_at,updated_at&published=eq.true"
         + "&storage_bucket=eq.database"
         + selected.orderClause
         + "&limit="
         + FETCH_LIMIT;
   }

   private static List<LitemacroMarketplacePreset> fetchPresets(String url, String bearerToken) throws IOException, InterruptedException {
      HttpRequest request = HttpRequest.newBuilder()
         .uri(URI.create(url))
         .timeout(Duration.ofSeconds(15))
         .header("apikey", PUBLISHABLE_KEY)
         .header("Authorization", "Bearer " + firstNonBlank(bearerToken, PUBLISHABLE_KEY))
         .header("Accept", "application/json")
         .header("Cache-Control", "no-cache")
         .header("Pragma", "no-cache")
         .GET()
         .build();
      HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() < 200 || response.statusCode() >= 300) {
         throw httpException("Marketplace request failed", response);
      }

      return parsePresets(response.body());
   }

   private static JsonObject postRpcJson(String functionName, JsonObject payload, String bearerToken, String errorMessage)
      throws IOException, InterruptedException {
      HttpResponse<String> response = sendRpcRequest(functionName, payload, bearerToken, Duration.ofSeconds(15));
      if (response.statusCode() < 200 || response.statusCode() >= 300) {
         throw httpException(errorMessage, response);
      }

      JsonElement root = JsonParser.parseString(response.body());
      return root != null && root.isJsonObject() ? root.getAsJsonObject() : new JsonObject();
   }

   private static HttpResponse<String> sendRpcRequest(String functionName, JsonObject payload, String bearerToken, Duration timeout)
      throws IOException, InterruptedException {
      return HTTP_CLIENT.send(
         HttpRequest.newBuilder()
            .uri(URI.create(PROJECT_URL + "/rest/v1/rpc/" + functionName))
            .timeout(timeout)
            .header("apikey", PUBLISHABLE_KEY)
            .header("Authorization", "Bearer " + firstNonBlank(bearerToken, PUBLISHABLE_KEY))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
            .build(),
         HttpResponse.BodyHandlers.ofString()
      );
   }

   private static List<LitemacroMarketplacePreset> parsePresets(String json) {
      JsonElement root = JsonParser.parseString(json);
      if (root == null || !root.isJsonArray()) {
         return List.of();
      }

      List<LitemacroMarketplacePreset> presets = new ArrayList<>();
      for (JsonElement element : root.getAsJsonArray()) {
         if (element.isJsonObject()) {
            presets.add(parsePreset(element.getAsJsonObject()));
         }
      }

      return presets;
   }

   private static LitemacroMarketplacePreset parsePreset(JsonObject row) {
      return new LitemacroMarketplacePreset(
         string(row, "id"),
         string(row, "slug"),
         nullableString(row, "author_user_id"),
         string(row, "name"),
         string(row, "author_name"),
         nullableString(row, "author_avatar_url"),
         string(row, "description"),
         stringArray(row, "tags"),
         nullableString(row, "game_version"),
         nullableString(row, "litemacro_version"),
         intValue(row, "likes_count"),
         intValue(row, "downloads_count"),
         nullableString(row, "storage_bucket"),
         string(row, "file_path"),
         booleanValue(row, "published"),
         nullableString(row, "created_at"),
         nullableString(row, "updated_at")
      );
   }

   private static List<LitemacroMarketplacePreset> filterLitemacro(List<LitemacroMarketplacePreset> presets) {
      if (presets == null || presets.isEmpty()) {
         return List.of();
      }

      List<LitemacroMarketplacePreset> filtered = new ArrayList<>();
      for (LitemacroMarketplacePreset preset : presets) {
         if (preset != null && preset.belongsToLitemacro() && "database".equalsIgnoreCase(preset.storageBucket())) {
            filtered.add(preset);
         }
      }

      return filtered;
   }

   private static List<String> withLitemacroTag(List<String> tags) {
      List<String> values = new ArrayList<>();
      values.add("litemacro");
      if (tags != null) {
         for (String tag : tags) {
            if (tag != null && !tag.isBlank() && values.stream().noneMatch(existing -> existing.equalsIgnoreCase(tag.trim()))) {
               values.add(tag.trim());
            }
         }
      }

      return values;
   }

   private static JsonArray toJsonArray(List<String> values) {
      JsonArray array = new JsonArray();
      if (values != null) {
         for (String value : values) {
            if (value != null && !value.isBlank()) {
               array.add(value.trim());
            }
         }
      }

      return array;
   }

   private static void requireWithinSizeLimit(Path path) throws IOException {
      long size = Files.size(path);
      if (size > MAX_PRESET_FILE_SIZE_BYTES) {
         throw new IOException("Macro file is too large. Marketplace uploads must be under 5 MB.");
      }
   }

   private static String buildPublicObjectUrl(String bucketName, String filePath) {
      return PROJECT_URL + "/storage/v1/object/public/" + normalizeBucketName(bucketName) + "/" + encodeStoragePath(filePath);
   }

   private static boolean isDatabasePreset(LitemacroMarketplacePreset preset) {
      String bucket = preset.storageBucket() == null ? "" : preset.storageBucket().trim();
      String path = preset.filePath() == null ? "" : preset.filePath().trim();
      return bucket.equalsIgnoreCase("database") || path.startsWith("database/");
   }

   private static String encodeStoragePath(String path) {
      String normalized = path == null ? "" : path.replace("\\", "/");
      String[] segments = normalized.split("/");
      StringBuilder encoded = new StringBuilder();
      for (String segment : segments) {
         if (segment != null && !segment.isBlank()) {
            if (!encoded.isEmpty()) {
               encoded.append('/');
            }

            encoded.append(url(segment));
         }
      }

      return encoded.toString();
   }

   private static String sanitizeSlug(String value) {
      if (value == null) {
         return "";
      }

      return value.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-+|-+$)", "");
   }

   private static String normalizeBucketName(String bucketName) {
      return bucketName == null || bucketName.isBlank() ? PUBLIC_BUCKET_NAME : bucketName.trim();
   }

   private static String marketplaceSetting(String fileKey, String fileAlias, String envKey, String propertyKey) {
      String value = System.getProperty(propertyKey);
      if (value == null || value.isBlank()) {
         value = System.getenv(envKey);
      }

      if (value == null || value.isBlank()) {
         Properties properties = marketplaceProperties();
         value = firstNonBlank(properties.getProperty(fileKey), properties.getProperty(fileAlias));
      }

      String fallback = switch (fileKey) {
         case "project_url" -> DEFAULT_PROJECT_URL;
         case "publishable_key" -> DEFAULT_PUBLISHABLE_KEY;
         default -> "";
      };
      return value == null || value.isBlank() ? fallback : value.trim();
   }

   private static Properties marketplaceProperties() {
      Properties properties = new Properties();
      try {
         Path path = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME);
         if (Files.exists(path)) {
            try (var input = Files.newInputStream(path)) {
               properties.load(input);
            }
         }
      } catch (Exception ignored) {
      }

      return properties;
   }

   private static String cleanProjectUrl(String value) {
      String cleaned = value == null ? "" : value.trim();
      while (cleaned.endsWith("/")) {
         cleaned = cleaned.substring(0, cleaned.length() - 1);
      }

      return cleaned;
   }

   private static String url(String value) {
      return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
   }

   private static String blankToEmpty(String value) {
      return value == null ? "" : value.trim();
   }

   private static String firstNonBlank(String... values) {
      if (values != null) {
         for (String value : values) {
            if (value != null && !value.isBlank()) {
               return value;
            }
         }
      }

      return null;
   }

   private static IOException httpException(String message, HttpResponse<String> response) {
      String body = response.body() == null ? "" : response.body().trim();
      String parsed = extractJsonErrorMessage(body);
      if (isMissingMarketplaceRpc(body, parsed)) {
         if (containsIgnoreCase(body, LIKE_PRESET_RPC) || containsIgnoreCase(parsed, LIKE_PRESET_RPC)) {
            return new IOException(message + ": Supabase is missing the Like setup. Run the latest litemacro-supabase-marketplace.sql once.");
         }

         return new IOException(
            message
               + ": Supabase marketplace setup is missing. Run the latest litemacro-supabase-marketplace.sql once."
         );
      }

      if (parsed != null && !parsed.isBlank()) {
         return new IOException(message + ": " + parsed);
      }

      return new IOException(body.isBlank() ? message + " (HTTP " + response.statusCode() + ")" : message + " (HTTP " + response.statusCode() + "): " + body);
   }

   private static boolean isMissingMarketplaceRpc(String body, String parsed) {
      String combined = (String.valueOf(body) + " " + String.valueOf(parsed)).toLowerCase(Locale.ROOT);
      return combined.contains("could not find the function public.submit_litemacro_marketplace_preset")
         || combined.contains("could not find the function public.download_litemacro_marketplace_preset")
         || combined.contains("could not find the function public.increment_preset_downloads")
         || combined.contains("could not find the function public.like_litemacro_marketplace_preset");
   }

   private static boolean containsIgnoreCase(String value, String needle) {
      return value != null && needle != null && value.toLowerCase(Locale.ROOT).contains(needle.toLowerCase(Locale.ROOT));
   }

   private static String extractJsonErrorMessage(String body) {
      if (body == null || body.isBlank()) {
         return null;
      }

      try {
         JsonElement root = JsonParser.parseString(body);
         if (!root.isJsonObject()) {
            return null;
         }

         JsonObject object = root.getAsJsonObject();
         return firstNonBlank(nullableString(object, "message"), nullableString(object, "error"), nullableString(object, "details"), nullableString(object, "hint"));
      } catch (Exception var3) {
         return null;
      }
   }

   private static String string(JsonObject object, String key) {
      String value = nullableString(object, key);
      return value == null ? "" : value;
   }

   private static String nullableString(JsonObject object, String key) {
      JsonElement element = object.get(key);
      return element == null || element.isJsonNull() ? null : element.getAsString();
   }

   private static int intValue(JsonObject object, String key) {
      JsonElement element = object.get(key);
      return element == null || element.isJsonNull() ? 0 : element.getAsInt();
   }

   private static boolean booleanValue(JsonObject object, String key) {
      JsonElement element = object.get(key);
      return element != null && !element.isJsonNull() && element.getAsBoolean();
   }

   private static List<String> stringArray(JsonObject object, String key) {
      JsonElement element = object.get(key);
      if (element == null || element.isJsonNull() || !element.isJsonArray()) {
         return List.of();
      }

      List<String> values = new ArrayList<>();
      for (JsonElement item : element.getAsJsonArray()) {
         if (item != null && !item.isJsonNull()) {
            String value = item.getAsString();
            if (!value.isBlank()) {
               values.add(value);
            }
         }
      }

      return values;
   }

   record PublishRequest(
      Path localPresetPath,
      String existingStorageBucket,
      String existingFilePath,
      String slug,
      String name,
      String authorName,
      String description,
      List<String> tags,
      String gameVersion,
      String litemacroVersion,
      boolean published
   ) {
   }

   record SubmissionResult(int uploadCount, int remaining, String message) {
   }

   enum ListingMode {
      NEWEST("&order=created_at.desc"),
      UPDATED("&order=updated_at.desc"),
      DOWNLOADS("&order=downloads_count.desc&order=updated_at.desc"),
      LIKES("&order=likes_count.desc&order=updated_at.desc"),
      TRENDING("&order=downloads_count.desc&order=likes_count.desc&order=updated_at.desc");

      private final String orderClause;

      ListingMode(String orderClause) {
         this.orderClause = orderClause;
      }
   }
}
