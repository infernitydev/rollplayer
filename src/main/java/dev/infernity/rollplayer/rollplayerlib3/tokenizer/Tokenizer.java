package dev.infernity.rollplayer.rollplayerlib3.tokenizer;

import dev.infernity.rollplayer.rollplayerlib3.Span;
import dev.infernity.rollplayer.rollplayerlib3.exceptions.SyntaxException;

import java.util.ArrayList;
import java.util.function.Supplier;

public class Tokenizer {
    private final StringReader reader;
    private Token lastToken;

    public Tokenizer(String str){
        this.reader = new StringReader(str);
        this.lastToken = new Token.NoToken(createSpan());
    }

    public TokenizationResult tokenize() {
        var list = new ArrayList<Token>();
        while(true) {
            reader.skipWhitespace();
            TokenResult token = tokenizeOnce();
            if(token.isError()) {
                return TokenizationResult.error(token.error().orElseThrow(), token.getSpan());
            }
            lastToken = token.getOrThrow();
            if(lastToken instanceof Token.EndOfString) {
                return TokenizationResult.success(list);
            }
            list.add(lastToken);
        }
    }

    public TokenResult tokenizeOnce() throws SyntaxException {
        reader.skipWhitespace();
        return switch (lastToken) {
            case Token.DiceStart token -> tokenizeSides();
            default -> tokenizeBase();
        };
    }

    private TokenResult tokenizeSides() {
        var start = this.reader.getCursor();
        int sides = this.reader.readPositiveInt();
        if (reader.peek() == ':') {
            reader.expect(':');
            int max = this.reader.readPositiveInt();
            if (sides < max) {
                int temp = max;
                max = sides;
                sides = temp;
            }
            return new TokenResult.Success(new Token.Sides(sides, max, createSpan(start)));
        }
        return TokenResult.success(new Token.Sides(1, sides, createSpan(start)));
    }

    private TokenResult tokenizeBase(){
        try {
            var start = this.reader.getCursor();
            return switch (reader.peek()) {
                case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
                     'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                     'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
                     'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                     '_' -> {
                    String string = this.readIdentifier();
                    if (string.equals("d")) {
                        yield TokenResult.success(new Token.DiceStart(createSpan(start)));
                    } else {
                        yield TokenResult.error("I don't know what '" + string + "' is.", this.createSpan(start));
                    }
                }
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.' ->
                        TokenResult.success(new Token.Number(reader.readDouble(), this.createSpan(start)));
                case '+' -> token('+', () -> new Token.Plus(this.createSpan(start)));
                case '-' -> token('-', () -> new Token.Minus(this.createSpan(start)));
                case '/' -> token('/', () -> new Token.Divide(this.createSpan(start)));
                case '*' -> token('*', () -> new Token.Multiply(this.createSpan(start)));
                default -> TokenResult.error("What does '" + reader.peek() + "' mean?", this.createSpan(start));
            };
        }
        catch (StringIndexOutOfBoundsException ignoredException) {
            return TokenResult.success(new Token.EndOfString(createSpan()));
        }
    }

    public Span createSpan() {
        return new Span(this.reader.getCursor(), this.reader.getCursor(), this.reader.getString());
    }

    public Span createSpan(int start) {
        return new Span(start, this.reader.getCursor(), this.reader.getString());
    }

    public TokenResult token(char symbol, Supplier<Token> token) throws SyntaxException {
        reader.expect(symbol);
        return TokenResult.success(token.get());
    }

    public static boolean isAllowedInIdentifier(final char c) {
        return c >= 'A' && c <= 'Z'
                || c >= 'a' && c <= 'z'
                || c == '_';
    }

    public String readIdentifier() {
        final int start = this.reader.getCursor();
        while (this.reader.canRead() && isAllowedInIdentifier(this.reader.peek())) {
            this.reader.skip();
        }
        return this.reader.getString().substring(start, this.reader.getCursor());
    }
}
