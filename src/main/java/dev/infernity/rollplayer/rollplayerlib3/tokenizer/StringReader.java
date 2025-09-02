package dev.infernity.rollplayer.rollplayerlib3.tokenizer;

import dev.infernity.rollplayer.rollplayerlib3.exceptions.SyntaxException;

/*

This code is taken from the Brigadier codebase and is thusly licensed under the MIT License:

Copyright (c) Microsoft Corporation. All rights reserved.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

*/

public class StringReader {
    private final String string;
    private int cursor;

    public StringReader(StringReader other) {
        this.string = other.string;
        this.cursor = other.cursor;
    }

    public StringReader(String string) {
        this.string = string;
    }

    public String getString() {
        return this.string;
    }

    public void setCursor(int cursor) {
        this.cursor = cursor;
    }

    public int getRemainingLength() {
        return this.string.length() - this.cursor;
    }

    public int getTotalLength() {
        return this.string.length();
    }

    public int getCursor() {
        return this.cursor;
    }

    public String getRead() {
        return this.string.substring(0, this.cursor);
    }

    public String getRemaining() {
        return this.string.substring(this.cursor);
    }

    public boolean canRead(int length) {
        return this.cursor + length <= this.string.length();
    }

    public boolean canRead() {
        return this.canRead(1);
    }

    public char peek() {
        return this.string.charAt(this.cursor);
    }

    public String peekMultiple(int length) {
        return this.string.substring(this.cursor, this.cursor+length);
    }

    public char peek(int offset) {
        return this.string.charAt(this.cursor + offset);
    }

    public char read() {
        return this.string.charAt(this.cursor++);
    }

    public void skip() {
        ++this.cursor;
    }

    public static boolean isAllowedNumber(char c) {
        return c >= '0' && c <= '9' || c == '.' || c == '-';
    }

    public static boolean isQuotedStringStart(char c) {
        return c == '"' || c == '\'';
    }

    public void skipWhitespace() {
        while(this.canRead() && Character.isWhitespace(this.peek())) {
            this.skip();
        }

    }

    public int readInt() throws SyntaxException {
        int start = this.cursor;

        while(this.canRead() && isAllowedNumber(this.peek())) {
            this.skip();
        }

        String number = this.string.substring(start, this.cursor);
        if (number.isEmpty()) {
            throw new SyntaxException("Expected an integer");
        } else {
            try {
                return Integer.parseInt(number);
            } catch (NumberFormatException var4) {
                this.cursor = start;
                throw new SyntaxException("Expected an integer, got '" + number + "'");
            }
        }
    }

    public int readPositiveInt() throws SyntaxException {
        int start = this.cursor;

        while(this.canRead() && isAllowedNumber(this.peek())) {
            this.skip();
        }

        String number = this.string.substring(start, this.cursor);
        if (number.isEmpty()) {
            throw new SyntaxException("Expected a positive integer");
        } else {
            try {
                var val = Integer.parseInt(number);
                if (val <= 0) {throw new SyntaxException("Expected a positive integer, not a negative one or zero");}
                return val;
            } catch (NumberFormatException var4) {
                this.cursor = start;
                throw new SyntaxException("Expected a positive integer, got '" + number + "'");
            }
        }
    }

    public double readDouble() throws SyntaxException {
        int start = this.cursor;

        while(this.canRead() && isAllowedNumber(this.peek())) {
            this.skip();
        }

        String number = this.string.substring(start, this.cursor);
        if (number.isEmpty()) {
            throw new SyntaxException("Expected a number");
        } else {
            try {
                return Double.parseDouble(number);
            } catch (NumberFormatException var4) {
                this.cursor = start;
                throw new SyntaxException("Expected a number, got '" + number + "'");
            }
        }
    }

    public static boolean isAllowedInUnquotedString(char c) {
        return c >= '0' && c <= '9' || c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z' || c == '_' || c == '-' || c == '.' || c == '+';
    }

    public String readUnquotedString() {
        int start = this.cursor;

        while(this.canRead() && isAllowedInUnquotedString(this.peek())) {
            this.skip();
        }

        return this.string.substring(start, this.cursor);
    }

    public String readQuotedString() throws SyntaxException {
        if (!this.canRead()) {
            return "";
        } else {
            char next = this.peek();
            if (!isQuotedStringStart(next)) {
                throw new SyntaxException("Expected a quote [to start a string]");
            } else {
                this.skip();
                return this.readStringUntil(next);
            }
        }
    }

    public String readStringUntil(char terminator) throws SyntaxException {
        StringBuilder result = new StringBuilder();
        boolean escaped = false;

        while(this.canRead()) {
            char c = this.read();
            if (escaped) {
                if (c != terminator && c != '\\') {
                    this.setCursor(this.getCursor() - 1);
                    throw new SyntaxException("Expected a valid escape sequence, but got '" + c + "'");
                }

                result.append(c);
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else {
                if (c == terminator) {
                    return result.toString();
                }

                result.append(c);
            }
        }
        throw new SyntaxException("Expected a quote [to end a string]");
    }

    public String readString() throws SyntaxException {
        if (!this.canRead()) {
            return "";
        } else {
            char next = this.peek();
            if (isQuotedStringStart(next)) {
                this.skip();
                return this.readStringUntil(next);
            } else {
                return this.readUnquotedString();
            }
        }
    }

    public boolean readBoolean() throws SyntaxException {
        int start = this.cursor;
        String value = this.readString();
        if (value.isEmpty()) {
            throw new SyntaxException("Expected a boolean");
        } else if (value.equals("true")) {
            return true;
        } else if (value.equals("false")) {
            return false;
        } else {
            this.cursor = start;
            throw new SyntaxException("Expected a boolean, got '" + value + "'");
        }
    }

    public void expect(char c) throws SyntaxException {
        if (this.canRead() && this.peek() == c) {
            this.skip();
        } else {
            throw new SyntaxException("Expected '" + c + "', got '" + this.peek() + "'");
        }
    }
}
