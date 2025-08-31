package dev.infernity.rollplayer.rollplayerlib3;

public interface Token {
    Span span();

    record NumberExpression(double value, Span span) implements Token {

    }

    record EndOfString(Span span) implements Token {

    }
}
