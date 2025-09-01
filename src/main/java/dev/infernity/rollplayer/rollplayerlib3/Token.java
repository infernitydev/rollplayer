package dev.infernity.rollplayer.rollplayerlib3;

public interface Token {
    Span span();
    record NoToken(Span span) implements Token {}

    record Number(double value, Span span) implements Token {}

    record DiceStart(Span span) implements Token {}

    record Sides(int min, int max, Span span) implements Token {}

    record EndOfString(Span span) implements Token {}
}
