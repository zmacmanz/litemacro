package com.alfre.macrobuilder;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;

final class StickyNoteTextLayout {
   private StickyNoteTextLayout() {
   }

   static List<String> wrapLines(String source, Font font, int maxWidth, int maxLines) {
      List<String> text = new ArrayList<>();

      for (StickyNoteTextLayout.Line line : layoutLines(source, font, maxWidth, maxLines)) {
         text.add(line.text());
      }

      return text;
   }

   static List<StickyNoteTextLayout.Line> layoutLines(String source, Font font, int maxWidth, int maxLines) {
      String text = source == null ? "" : source;
      List<StickyNoteTextLayout.Line> lines = new ArrayList<>();
      int width = Math.max(1, maxWidth);
      int lineStart = 0;
      StringBuilder current = new StringBuilder();

      for (int index = 0; index < text.length() && lines.size() < maxLines; index++) {
         char chr = text.charAt(index);
         if (chr == '\n') {
            lines.add(new StickyNoteTextLayout.Line(current.toString(), lineStart, index));
            lineStart = index + 1;
            current.setLength(0);
            continue;
         }

         String candidate = current.toString() + chr;
         if (current.length() > 0 && font.width(candidate) > width) {
            lines.add(new StickyNoteTextLayout.Line(current.toString(), lineStart, index));
            lineStart = index;
            current.setLength(0);
         }

         current.append(chr);
      }

      if (lines.size() < maxLines) {
         lines.add(new StickyNoteTextLayout.Line(current.toString(), lineStart, text.length()));
      }

      if (lines.isEmpty()) {
         lines.add(new StickyNoteTextLayout.Line("", 0, 0));
      }

      return lines;
   }

   record Line(String text, int start, int end) {
   }
}
