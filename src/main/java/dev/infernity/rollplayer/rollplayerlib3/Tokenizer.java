package dev.infernity.rollplayer.rollplayerlib3;

import dev.infernity.rollplayer.rollplayerlib3.exceptions.SyntaxException;
import dev.infernity.rollplayer.rollplayerlib3.exceptions.TokenizationException;

import java.util.ArrayList;
import java.util.List;

public class Tokenizer {
    private final StringReader reader;

    public Tokenizer(String str){
        this.reader = new StringReader(str);
    }

    public Result<List<Token>> tokenize() {
        var list = new ArrayList<Token>();
        while(true) {
            reader.skipWhitespace();
            Result<Token> token = tokenizeOnce();
            if(token.isError()) {
                return Result.error(token.error().orElseThrow());
            }
            if(token.getOrThrow() instanceof Token.EndOfString) {
                return Result.success(list);
            }
            list.add(token.getOrThrow());
        }
    }

    public Result<Token> tokenizeOnce(){
        try {
            var start = this.reader.getCursor();
            reader.skipWhitespace();
            return switch (reader.peek()) {
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.' ->
                    Result.success(new Token.NumberExpression(reader.readDouble(), this.createSpan(start)));
                default -> throw new TokenizationException("Unexpected character '" + reader.peek() + "'", this.createSpan(start));
            };
        }
        catch (SyntaxException exception) {
            return Result.error(exception.getMessage());
        }
        catch (StringIndexOutOfBoundsException ignoredException) {
            return Result.success(new Token.EndOfString(createSpan()));
        }
    }

    public Span createSpan() {
        return new Span(this.reader.getCursor(), this.reader.getCursor(), this.reader.getString());
    }

    public Span createSpan(int start) {
        return new Span(start, this.reader.getCursor(), this.reader.getString());
    }

    public Span createSpan(int start, int end) {
        return new Span(start, end, this.reader.getString());
    }
}
