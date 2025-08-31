package dev.infernity.rollplayer.rollplayerlib3.exceptions;

import dev.infernity.rollplayer.rollplayerlib3.Span;

public class TokenizationException extends SpannedException {
    public TokenizationException(String message, Span span) {
        super(message, span);
    }
}
