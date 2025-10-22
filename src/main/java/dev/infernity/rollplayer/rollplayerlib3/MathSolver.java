package dev.infernity.rollplayer.rollplayerlib3;

import java.util.ArrayList;

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
        while (peek("+") || peek("-")) {
            if(peek("+")) {
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
        while (peek("*") || peek("/")) {
            if(peek("*")) {
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
        while (peek("^")) {
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

interface Node{
    enum BinaryOp { PLUS, MINUS, TIMES, DIVIDE, EXPONENT}
    double evaluate();
}

record Binary(BinaryOp operator, Node left, Node right) implements Node {
    public double evaluate() {
        return switch (operator) {
            case PLUS -> left.evaluate() + right.evaluate();
            case MINUS -> left.evaluate() - right.evaluate();
            case TIMES -> left.evaluate() * right.evaluate();
            case DIVIDE -> left.evaluate() / right.evaluate();
            case EXPONENT -> Math.pow(left.evaluate(), right.evaluate());
        };
    }
}

record Number(double value) implements Node {
    public double evaluate() {
        return value;
    }
}