package dev.infernity.rollplayer.rollplayerlib3;

import java.util.ArrayList;

public class Parser {
    /**
     * @param input Raw user command input
     * @return Arraylist of expression results as strings. All numbers should return as doubles so if you want to pretty it up you have to fix that
     * <br>If an inputted expression had no math, it will return int the form "r (num) (num) (num)..." from the rolls, also as doubles
     */
    public static ArrayList<String> evaluate(String input) throws IllegalArgumentException{
        ArrayList<ArrayList<String>> inputExpressions = new ArrayList<>();
        for(String exp : removeWhitespace(input)) {
            ArrayList<String> tester;
            try {
                tester = stringTokenizer(exp);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(String.format("Exception with expression %s caught\n%s", exp, e));
            }

            try {
                tester = evaluateDice(tester);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(String.format("Exception with diceroll caught in expression %s \n%s", exp, e));
            }

            inputExpressions.add(tester);
        }

        ArrayList<String> output = new ArrayList<>();
        for(ArrayList<String> expression : inputExpressions) {
            //if the input expression was just a dice roll, preface with "r " and have the numbers separated by spaces
            if (expression.getFirst().equals("Dice Roll Expression")) {
                StringBuilder rolls = new StringBuilder("r");
                expression.removeFirst();
                for(String s : expression)
                    rolls.append(" ").append(s);

                output.add(rolls.toString());
            }
            //if the input expression had math in it
            else try {
                output.add("" + new MathSolver(expression).evaluate());
            } catch (IllegalArgumentException e) {
                StringBuilder reconstructor = new StringBuilder();
                for(String s : expression) reconstructor.append(s);
                throw new IllegalArgumentException(String.format("Exception caught while evaluating math expression %s\n%s", reconstructor, e));
            }
        }

        return output;
    }

    /**
     * Removes whitespace from a string input and breaks it into multiple strings for use in stringTokenizer
     * @param input Raw command string input
     * @return ArrayList of strings without whitespace
     */
    public static ArrayList<String> removeWhitespace (String input) {
        ArrayList<String> output = new ArrayList<>();
        int unparsedIndex;

        // dummy return
        if(input.contains(" ")) {

            // turn all repeat spaces into one single space to make the next parts easier
            while (input.contains("  ")) {
                int doubleIndex = input.indexOf("  ");
                input = input.substring(0, doubleIndex) + input.substring(doubleIndex + 1);
            }

            // separate the expressions and load them into output
            // if i were smarter i'd do this using enums
            while (input.contains(" ")) {
                unparsedIndex = input.indexOf(" ");
                String before = "other", after = "other";  //other, roll, int, parenthesis ) (

                if (unparsedIndex == 0 || unparsedIndex == input.length() - 1)
                    continue;
                // check the char before
                if (input.substring(unparsedIndex - 1, unparsedIndex).matches("\\d")) {
                    // ints section
                    int checker = 0;
                    String checkerSub = input.substring(unparsedIndex - 1 - checker, unparsedIndex - checker);
                    before = "int";

                    // keep going backwards until it hits a non-number to see if this is actually part of a roll
                    while (checkerSub.matches("\\d")) {
                        //dummy break
                        if (unparsedIndex - checker - 2 < 0)
                            break;

                        switch (input.charAt(unparsedIndex - checker - 2)) {
                            case 'd':
                            case ':':
                            case 'R':
                                before = "roll";
                                break;
                            case 'l':
                            case 'h':
                                if (input.charAt(unparsedIndex - checker - 3) == 'k')
                                    before = "roll";
                                break;
                            case 'r':
                                if (input.charAt(unparsedIndex - checker - 3) == 'r')
                                    before = "roll";
                                break;
                        }
                        checker++;
                    }
                } else {
                    switch (input.charAt(unparsedIndex - 1)) {
                        case 'l':
                        case 'h':
                            if (input.charAt(unparsedIndex - 2) == 'k')
                                before = "roll";
                            break;
                        case '}':
                            before = "roll";
                            break;
                        case ')':
                            before = "parenthesis";
                            break;
                    }
                }

                // check the char after
                if (input.substring(unparsedIndex + 1, unparsedIndex + 2).matches("\\d")) {
                    // ints section
                    int checker = 0;
                    String checkerSub = input.substring(unparsedIndex + 1 + checker, unparsedIndex + 2 + checker);
                    after = "int";

                    // keep going forwards until it hits a d to see if its a roll
                    while (checkerSub.matches("\\d") && unparsedIndex + 2 + checker < input.length()) {
                        if (input.charAt(unparsedIndex + 2 + checker) == 'd') {
                            after = "roll";
                            break;
                        }
                        checker++;
                        checkerSub = input.substring(unparsedIndex + 1 + checker, unparsedIndex + 2 + checker);
                    }
                } else if (input.charAt(unparsedIndex + 1) == 'd')
                    after = "roll";
                else if (input.charAt(unparsedIndex + 1) == '(')
                    after = "parenthesis";

                // now determine how the space gets removed
                switch (before) {
                    case "roll":
                    case "int":
                    case "parenthesis":
                        switch (after) {
                            case "roll":
                            case "int":
                            case "parenthesis":
                                output.add(input.substring(0, unparsedIndex));
                                input = input.substring(unparsedIndex + 1);
                                break;
                            case "other":
                                input = input.substring(0, unparsedIndex) + input.substring(unparsedIndex + 1);
                                break;
                        }
                        break;
                    case "other":
                        input = input.substring(0, unparsedIndex) + input.substring(unparsedIndex + 1);
                        break;
                }
            }
        }

        output.add(input);
        return output;
    }

    /**
     * Returns a list of string tokens ready to be constructed into an abstract syntax tree for Rollplayer
     * <br>Valid tokens are: ()+*-/^!, d, drop, kh, kl, and doubles as strings
     * @param input String input for the tokenizer with all spaces removed
     * @return ArrayList of string tokens
     * @throws IllegalArgumentException Unrecognized operator exception
     */
    public static ArrayList<String> stringTokenizer(String input) throws IllegalArgumentException{
        int stringIterator = 0;
        String substringIteration;
        ArrayList<String> outputList = new ArrayList<>();

        // remove all underscores before processing
        if(input.contains("_")){
            StringBuilder inputWithoutUnderscores = new StringBuilder();
            int underscoreIndex;
            do {
                underscoreIndex = input.indexOf("_");
                inputWithoutUnderscores.append(input, 0, underscoreIndex);
                input = input.substring(underscoreIndex+1);
            }while (input.contains("_"));
            input = inputWithoutUnderscores + input;
        }

        // walk along the input string and parse as the iterator goes
        while(stringIterator < input.length()){
            substringIteration = "" +input.charAt(stringIterator);

            // this differentiates "d" as in "2d4" and "d" as in "drop" or "dr"
            if (substringIteration.equals("d")) {
                if (stringIterator+4 < input.length() && input.startsWith("drop", stringIterator)) {
                    outputList.add("drop");
                    stringIterator += 4;
                }
                else if (stringIterator+2 < input.length() && input.startsWith("dr", stringIterator)) {
                    outputList.add("drop");
                    stringIterator += 2;
                }
                else {
                    outputList.add("d");
                    stringIterator++;
                }
                continue;
            }

            // block for two-char tokens that goes before the single-char tokens
            if(stringIterator+1 < input.length()) {
                switch (input.substring(stringIterator, stringIterator + 2)) {
                    case "kh", "kl", ">=", "<=", "rr":
                        outputList.add(input.substring(stringIterator, stringIterator + 2));
                        stringIterator += 2;
                        continue;
                }
            }

            // big block for single-char tokens
            // this covers: ()+*-/^><=,i
            switch (substringIteration) {
                case "(", ")", "+", "*", "-", "/", "^", "{", "}", "!", ":", ">", "<", "=", ",", "i":
                    outputList.add(substringIteration);
                    stringIterator++;
                    continue;
            }

            // integer detection
            if (substringIteration.matches("\\.|\\d")) {
                int intLength = 0;
                // big condition but it just means "keep checking characters until it hits a non-digit"
                while(stringIterator+intLength < input.length() && input.substring(stringIterator+intLength,stringIterator+intLength+1).matches("\\.|\\d")){
                    intLength++;
                }
                substringIteration = input.substring(stringIterator, stringIterator+intLength);
                outputList.add("" + Double.parseDouble(substringIteration));

                stringIterator += intLength;
                continue;
            }
            // throw an error if no token was processed from the substring iteration
            throw new IllegalArgumentException("Unrecognized argument in token parser: " + substringIteration);
        }
        return outputList;
    }

    /**
     * Evaluates all dice rolls in given input token list to prepare for final math pass
     * @param input Tokenized expression to evaluate rolls within
     * @return Token expression able to be passed into the math solver with all "d" dice rolls resolved
     * <br>If the expression only contained a dice roll expression and no math, it returns all of the rolls as tokens in a list headed by a "Dice Roll Expression" metatoken
     */
    public static ArrayList<String> evaluateDice(ArrayList<String> input) throws IllegalArgumentException{
        //dummy return
        if(!input.contains("d")) return input;

        input.add("EOF"); // necessary to prevent errors
        ArrayList<String> output = new ArrayList<>();
        ArrayList<String> toEvaluate = new ArrayList<>();
        int expressionStart;
        int expressionEnd = 0;
        boolean firstPass = true;

        while (input.subList(expressionEnd,input.size()).contains("d")) {
            expressionStart = expressionEnd;
            expressionEnd = input.subList(expressionStart, input.size()).indexOf("d") + expressionStart;

            //load pre-dice tokens into output
            if (input.get(expressionEnd).equals("d") && input.get(expressionEnd - 1).matches("\\d+.\\d+"))
                expressionEnd--;
            output.addAll(input.subList(expressionStart, expressionEnd));

            //load dice tokens into toEvaluate
            boolean validToken = true;
            boolean hasDiceRoll = false;
            if (input.get(expressionEnd).matches("\\d+.\\d+")) {
                toEvaluate.add(input.get(expressionEnd));
                expressionEnd++;
            }

            while (validToken && expressionEnd < input.size()) {
                //expressionEnd should always be on the operator token (aka after the last number/operator)
                switch (input.get(expressionEnd)) {
                    case "d":
                        if(hasDiceRoll) throw new IllegalArgumentException("More than one dice roll detected in expression");
                        hasDiceRoll = true;
                        toEvaluate.addAll(input.subList(expressionEnd, expressionEnd+2));
                        expressionEnd += 2;
                        if(input.get(expressionEnd).equals(":")) {
                            toEvaluate.addAll(input.subList(expressionEnd, expressionEnd+2));
                            expressionEnd += 2;
                        }
                        break;
                    case "kh", "kl":
                        toEvaluate.add(input.get(expressionEnd));
                        if (input.get(expressionEnd + 1).matches("\\d+.\\d+")) {
                            toEvaluate.add(input.get(expressionEnd + 1));
                            expressionEnd += 2;
                            break;
                        }
                        expressionEnd++;
                        break;
                    case "rr":
                        while (!input.get(expressionEnd).equals("}")) {
                            toEvaluate.add(input.get(expressionEnd));
                            expressionEnd++;
                        }
                        toEvaluate.add(input.get(expressionEnd));
                        expressionEnd++;
                        if (input.get(expressionEnd).equals(":")) {
                            toEvaluate.addAll(input.subList(expressionEnd, expressionEnd+2));
                            expressionEnd += 2;
                        }
                        break;
                    case "drop":
                        while (!input.get(expressionEnd).equals("}")) {
                            toEvaluate.add(input.get(expressionEnd));
                            expressionEnd++;
                        }
                        toEvaluate.add(input.get(expressionEnd));
                        expressionEnd++;
                        break;
                    case "!":
                        if (input.get(expressionEnd+1).equals("{")) {
                            while (!input.get(expressionEnd).equals("}")) {
                                toEvaluate.add(input.get(expressionEnd));
                                expressionEnd++;
                            }
                        }
                        toEvaluate.add(input.get(expressionEnd));
                        expressionEnd++;
                        if (input.get(expressionEnd).equals(":")) {
                            toEvaluate.addAll(input.subList(expressionEnd, expressionEnd+2));
                            expressionEnd += 2;
                        }
                        break;
                    case "i":
                        // just yeet the math into the handler and don't bother validating it
                        // don't need to check for i or : because if it reaches those it should keep grabbing math anyway
                        int endIndex = input.size();
                        String[] checks = {"d", "kh", "kl", "rr", "drop", "!"};
                        for(String check : checks)
                            if(input.subList(expressionEnd, input.size()).contains(check))
                                if(input.subList(expressionEnd, input.size()).indexOf(check) < endIndex)
                                    endIndex = input.subList(expressionEnd, input.size()).indexOf(check) + expressionEnd;
                        toEvaluate.addAll(input.subList(expressionEnd, endIndex));
                        expressionEnd = endIndex;
                        break;
                    default:
                        validToken = false;
                        break;
                }
            }

            // evaluate roll
            DiceRoller evaluator = new DiceRoller(toEvaluate);
            Rolls evaluation = evaluator.evaluateExpression();

            // if the expression was just a dice roll with no math, return it in a special way and head it with a metatoken
            if (firstPass && expressionEnd >= input.size()-1) {
                output.clear();
                output.add("Dice Roll Expression");
                for(double roll : evaluation.getRolls())
                    output.add("" + roll);
                return output;
            } else {
                output.add("" +evaluation.getSum());
            }
            firstPass = false;
            toEvaluate.clear();

            if(output.size() > 99) throw new IllegalArgumentException("Rollplayer will not parse more than 100 expressions");
        }

        // one last pass to catch any math after the last dice roll expression
        output.addAll(input.subList(expressionEnd, input.size()));

        return output;
    }
}