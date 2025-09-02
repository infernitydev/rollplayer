package dev.infernity.rollplayer.rollplayerlib3.tokenizer;

import dev.infernity.rollplayer.rollplayerlib3.Span;

public interface Token {
    Span span();
    record NoToken(Span span) implements Token {}

    record Number(double value, Span span) implements Token {}

    record DiceStart(Span span) implements Token {}

    record Sides(int min, int max, Span span) implements Token {}

    record Plus(Span span) implements Token {}
    record Minus(Span span) implements Token {}
    record Divide(Span span) implements Token {}
    record Multiply(Span span) implements Token {}

    record EndOfString(Span span) implements Token {}
}
