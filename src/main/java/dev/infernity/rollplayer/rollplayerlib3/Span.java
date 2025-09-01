package dev.infernity.rollplayer.rollplayerlib3;

import org.jetbrains.annotations.NotNull;

public record Span(int cursorStart, int cursorEnd, String originalString) {
    @NotNull
    @Override
    public String toString() {
        return "at " + cursorStart + ".." + cursorEnd;
    }

    public static Span merge(Span from, Span to) {
        return new Span(from.cursorStart(), to.cursorEnd(), to.originalString());
    }

    public DebugInfo debugInfo() {
        var sr = new StringReader(originalString);
        int column = 1;
        int row = 1;
        for(int i = 0; i < cursorStart; i++) {
            if (sr.read() == '\n') {
                column = 1;
                row += 1;
            } else {
                column += 1;
            }
        }

        var lines = this.originalString.split("\n");

        return new DebugInfo(lines[row - 1], column - 1, column + (cursorEnd - cursorStart), row);
    }

    public record DebugInfo(
            String linePreview,
            int from,
            int to,
            int line
    ) {
        @Override
        public @NotNull String toString() {
            var startingWs = 0;
            for(var ch : linePreview.toCharArray()) {
                if(ch == ' ') {
                    startingWs += 1;
                } else if (ch == '\t') {
                    startingWs += 4;
                } else {
                    break;
                }
            }
            return linePreview.trim() +
                    "\n" +
                    " ".repeat(from - startingWs) +
                    "^".repeat(to - from) + ", line " + line;
        }
    }
}
