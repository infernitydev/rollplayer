package dev.infernity.rollplayer.rollplayerlib3;

import java.util.ArrayList;
import java.util.function.DoubleBinaryOperator;

interface Node{
    enum BinaryOp implements DoubleBinaryOperator {
        PLUS     ("+", Double::sum),
        MINUS    ("-", (l, r) -> l - r),
        TIMES    ("*", (l, r) -> l * r),
        DIVIDE   ("/", (l, r) -> l / r),
        EXPONENT ("^", Math::pow);

        private final String symbol;
        private final DoubleBinaryOperator operation;

        BinaryOp (final String symbol, final DoubleBinaryOperator operation) {
            this.symbol = symbol;
            this.operation = operation;
        }
        public String getSymbol() { return symbol; }

        @Override
        public double applyAsDouble(final double left, final double right) {
            return operation.applyAsDouble(left, right);
        }
    }
    double evaluate();
}

public class MathSolver extends Expression {
    Node rootNode;

    public MathSolver(ArrayList<String> tokens) throws IllegalArgumentException{
        super(tokens);
        rootNode = parseAdd(); // despite the confusing name this generates the entire tree
        if (!peek("EOF")) throw new RuntimeException("Reached end of token stream while parsing: " + tokenStream.subList(pointer, tokenStream.size()));
    }

    public double evaluate() {
        return rootNode.evaluate();
    }
    //for more information on ASTs i used this lecture: https://andrewcmyers.github.io/oodds/lecture.html?id=parsing

    // operations by tier
    // +- */ ^ number ()

    // Add > Mult (+- Mult)*
    private Node parseAdd() throws IllegalArgumentException{
        Node e = parseMult();
        while (peek(Node.BinaryOp.PLUS.getSymbol()) || peek(Node.BinaryOp.MINUS.getSymbol())) {
            if(peek(Node.BinaryOp.PLUS.getSymbol())) {
                consume();
                e = new Binary(Node.BinaryOp.PLUS, e, parseMult());
            } else {
                consume();
                e = new Binary(Node.BinaryOp.MINUS, e, parseMult());
            }
        }
        return e;
    }

    // Mult > Pow (*/ Pow)*
    private Node parseMult() throws IllegalArgumentException{
        Node e = parsePow();
        while (peek(Node.BinaryOp.TIMES.getSymbol()) || peek(Node.BinaryOp.DIVIDE.getSymbol())) {
            if(peek(Node.BinaryOp.TIMES.getSymbol())) {
                consume();
                e = new Binary(Node.BinaryOp.TIMES, e, parsePow());
            } else {
                consume();
                e = new Binary(Node.BinaryOp.DIVIDE, e, parsePow());
            }
        }
        return e;
    }

    // Pow > Num (^ Num)*
    private Node parsePow() throws IllegalArgumentException{
        Node e = parseNum();
        while (peek(Node.BinaryOp.EXPONENT.getSymbol())) {
            consume();
            e = new Binary(Node.BinaryOp.EXPONENT, e, parseNum());
        }
        return e;
    }

    // Num > double | (Add)
    private Node parseNum() throws IllegalArgumentException{
        if (isNumber(peek()))
            return new Number(Double.parseDouble(consume()));
        else {
            try { consume("("); }
            catch (IllegalArgumentException e) {
                if (peek("EOF")) throw new IllegalArgumentException("Reached end of token stream while parsing: " + tokenStream);
                else throw new IllegalArgumentException("Neither number nor parenthesis found in number node parse: " + peek() + " in " + tokenStream);
            }
            Node e = parseAdd();
            consume(")");
            return e;
        }
    }
}

record Binary(BinaryOp operator, Node left, Node right) implements Node {
    public double evaluate() {
        return operator.applyAsDouble(left.evaluate(), right.evaluate());
    }
}

record Number(double value) implements Node {
    public double evaluate() {
        return value;
    }
}