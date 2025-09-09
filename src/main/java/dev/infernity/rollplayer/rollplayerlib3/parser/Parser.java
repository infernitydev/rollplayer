package dev.infernity.rollplayer.rollplayerlib3.parser;

import org.rekex.spec.Regex;
import org.rekex.helper.datatype.SepBy1;

import java.util.List;

public interface Parser {

    record IntegerToken(
            @Regex("[0-9]+")String str,
            OptionalWhitespace trailingWs
    ){
        public int toInt() {
            return Integer.parseInt(str);
        }
        public long toLong() {
            return Long.parseLong(str);
        }
    }

    record NumberToken(
            @Regex("[0-9]+(\\.[0-9]+)?")String str,
            OptionalWhitespace trailingWs
    ){
        public int toInt() {
            return Integer.parseInt(str);
        }

        public long toLong() {
            return Long.parseLong(str);
        }

        public double toDouble() {
            return Double.parseDouble(str);
        }
    }

    enum AddOrSubtractKeyword implements BinaryNumberEvalutator {
        @Str("+") ADD{
            @Override
            public double evaluate(double left, double right) {
                return left + right;
            }
        },
        @Str("-") SUBTRACT{
            @Override
            public double evaluate(double left, double right) {
                return left - right;
            }
        }
    }

    enum MultiplyOrDivideKeyword implements BinaryNumberEvalutator {
        @Str("*") MULTIPLY{
            @Override
            public double evaluate(double left, double right) {
                return left * right;
            }
        },
        @Str("/") DIVIDE{
            @Override
            public double evaluate(double left, double right) {
                return left / right;
            }
        }
    }

    record Root(
            OptionalWhitespace leadingWs,
            List<Sum> sums
    ){}

    record Sum(
            SepBy1<Product, AddOrSubtractKeyword> products
    ){}

    record Product(
            SepBy1<Product, MultiplyOrDivideKeyword> products
    ){}

}
