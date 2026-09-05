package com.alfre.macrobuilder;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;

final class StickyNoteTextLayout {
   private StickyNoteTextLayout() {
   }

   static List<String> wrapLines(String source, Font font, int maxWidth, int maxLines) {
      List<String> lines = new ArrayList<>();
      String[] rawLines = (source == null ? "" : source).split("\\n", -1);
      for (String rawLine : rawLines) {
         appendWrappedLine(lines, rawLine == null ? "" : rawLine, font, Math.max(1, maxWidth));
         if (lines.size() >= maxLines) {
            return new ArrayList<>(lines.subList(0, maxLines));
         }
      }

      if (lines.isEmpty()) {
         lines.add("");
      }

      return lines;
   }

   private static void appendWrappedLine(List<String> lines, String rawLine, Font font, int maxWidth) {
      if (rawLine.isEmpty()) {
         lines.add("");
         return;
      }

      StringBuilder current = new StringBuilder();
      for (String word : rawLine.split(" ", -1)) {
         if (word.isEmpty()) {
            appendWhitespace(lines, current, font, maxWidth);
            continue;
         }

         String candidate = current.length() == 0 ? word : current + " " + word;
         if (font.width(candidate) <= maxWidth) {
            current.setLength(0);
            current.append(candidate);
            continue;
         }

         if (current.length() > 0) {
            lines.add(current.toString());
            current.setLength(0);
         }

         appendWrappedWord(lines, current, word, font, maxWidth);
      }

      lines.add(current.toString());
   }

   private static void appendWhitespace(List<String> lines, StringBuilder current, Font font, int maxWidth) {
      if (current.length() == 0 || font.width(current + " ") <= maxWidth) {
         current.append(' ');
         return;
      }

      lines.add(current.toString());
      current.setLength(0);
   }

   private static void appendWrappedWord(List<String> lines, StringBuilder current, String word, Font font, int maxWidth) {
      for (int index = 0; index < word.length(); index++) {
         String candidate = current.toString() + word.charAt(index);
         if (current.length() > 0 && font.width(candidate) > maxWidth) {
            lines.add(current.toString());
            current.setLength(0);
         }

         current.append(word.charAt(index));
      }
   }
}
