package dev.infernity.rollplayer.rollplayerlib3.tokenizer;

import dev.infernity.rollplayer.rollplayerlib3.Result;
import dev.infernity.rollplayer.rollplayerlib3.Span;

import java.util.List;
import java.util.Optional;

public interface TokenizationResult extends Result<List<Token>> {

    static TokenizationResult success(List<Token> tokens) {
        return new Success(tokens);
    }
    static TokenizationResult error(String message, Span span) {
        return new Error(message, span);
    }

    Optional<Span> getErrorSpan();

    class Success extends Result.Success<List<Token>> implements TokenizationResult {
        public Success(List<Token> tokens) {
            super(tokens);
        }

        @Override
        public Optional<Span> getErrorSpan() {
            return Optional.empty();
        }
    }

    class Error extends Result.Error<List<Token>> implements TokenizationResult {
        protected Span span;

        public Error(String msg, Span span) {
            super(msg);
            this.span = span;
        }

        @Override
        public Optional<Span> getErrorSpan() {
            return Optional.of(span);
        }
    }
}
