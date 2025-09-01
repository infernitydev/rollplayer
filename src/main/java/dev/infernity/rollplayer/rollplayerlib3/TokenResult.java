package dev.infernity.rollplayer.rollplayerlib3;

public interface TokenResult extends Result<Token> {

    static TokenResult success(Token object) {
        return new Success(object);
    }
    static TokenResult error(String message, Span span) {
        return new Error(message, span);
    }

    Span getSpan();

    class Success extends Result.Success<Token> implements TokenResult {
        public Success(Token value) {
            super(value);
        }

        @Override
        public Span getSpan() {
            return value.span();
        }
    }

    class Error extends Result.Error<Token> implements TokenResult {
        protected Span span;

        public Error(String msg, Span span) {
            super(msg);
            this.span = span;
        }

        @Override
        public Span getSpan() {
            return span;
        }
    }
}
