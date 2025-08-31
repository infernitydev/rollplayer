package dev.infernity.rollplayer.rollplayerlib3.exceptions;

import dev.infernity.rollplayer.rollplayerlib3.Span;

public class SpannedException extends RuntimeException {
    public SpannedException(String message, Span span) {
        super(message);
        this.span = span;
    }

    Span span;

    public Span span() {
        return this.span;
    }

    @Override
    public String getMessage() {
        if(this.span == null) {
            return super.getMessage();
        }
        return super.getMessage() +
                "\n" +
                span.debugInfo();
    }
}
