package com.alfre.macrobuilder;

import java.util.List;

final class LitemacroMarketplacePreset {
   private final String id;
   private final String slug;
   private final String authorUserId;
   private final String name;
   private final String authorName;
   private final String authorAvatarUrl;
   private final String description;
   private final List<String> tags;
   private final String gameVersion;
   private final String litemacroVersion;
   private final int likesCount;
   private final int downloadsCount;
   private final String storageBucket;
   private final String filePath;
   private final boolean published;
   private final String createdAt;
   private final String updatedAt;

   LitemacroMarketplacePreset(
      String id,
      String slug,
      String authorUserId,
      String name,
      String authorName,
      String authorAvatarUrl,
      String description,
      List<String> tags,
      String gameVersion,
      String litemacroVersion,
      int likesCount,
      int downloadsCount,
      String storageBucket,
      String filePath,
      boolean published,
      String createdAt,
      String updatedAt
   ) {
      this.id = id;
      this.slug = slug;
      this.authorUserId = authorUserId;
      this.name = name;
      this.authorName = authorName;
      this.authorAvatarUrl = authorAvatarUrl;
      this.description = description;
      this.tags = tags == null ? List.of() : List.copyOf(tags);
      this.gameVersion = gameVersion;
      this.litemacroVersion = litemacroVersion;
      this.likesCount = likesCount;
      this.downloadsCount = downloadsCount;
      this.storageBucket = storageBucket;
      this.filePath = filePath;
      this.published = published;
      this.createdAt = createdAt;
      this.updatedAt = updatedAt;
   }

   String id() {
      return this.id;
   }

   String slug() {
      return this.slug;
   }

   String authorUserId() {
      return this.authorUserId;
   }

   String name() {
      return this.name;
   }

   String authorName() {
      return this.authorName;
   }

   String authorAvatarUrl() {
      return this.authorAvatarUrl;
   }

   String description() {
      return this.description;
   }

   List<String> tags() {
      return this.tags;
   }

   String gameVersion() {
      return this.gameVersion;
   }

   String litemacroVersion() {
      return this.litemacroVersion;
   }

   int likesCount() {
      return this.likesCount;
   }

   int downloadsCount() {
      return this.downloadsCount;
   }

   String storageBucket() {
      return this.storageBucket;
   }

   String filePath() {
      return this.filePath;
   }

   boolean published() {
      return this.published;
   }

   String createdAt() {
      return this.createdAt;
   }

   String updatedAt() {
      return this.updatedAt;
   }

   boolean belongsToLitemacro() {
      String version = this.litemacroVersion == null ? "" : this.litemacroVersion.toLowerCase(java.util.Locale.ROOT);
      if (version.contains("litemacro")) {
         return true;
      }

      for (String tag : this.tags) {
         if ("litemacro".equalsIgnoreCase(tag) || "macro_builder".equalsIgnoreCase(tag)) {
            return true;
         }
      }

      return false;
   }
}
