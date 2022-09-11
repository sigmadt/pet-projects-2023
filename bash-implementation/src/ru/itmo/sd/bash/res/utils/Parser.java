package ru.itmo.sd.bash.res.utils;

import ru.itmo.sd.bash.res.utils.exceptions.WrongSyntaxException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Parser {
    private static final HashMap<String, Character> symbols = new HashMap<>() {
        {
            put("space", ' ');
            put("single_q", '\'');
            put("double_q", '"');
            put("pipe", '|');
            put("assign", '=');
            put("subst", '$');
        }
    };

    List<Character> splitSymbols = Arrays.asList(
            symbols.get("space"),
            symbols.get("pipe"),
            symbols.get(""),
            symbols.get("single_q"),
            symbols.get("double_q")
    );


    public List<Token> run(String content) throws WrongSyntaxException {
        List<Token> tokens = new ArrayList<>();
        var currIndex = 0;
        var currToken = "";
        var substOrAssignment = "";

        while (currIndex < content.length()) {
            var currSymbol = content.charAt(currIndex);

            if (splitSymbols.contains(currSymbol)) {
                if (!currToken.isEmpty()) {
                    var tok = getSpecificToken(currToken, substOrAssignment);
                    tokens.add(tok);
                    currToken = "";
                }
                substOrAssignment = "";
            }

            switch (currSymbol) {
                case ' ':
                    currIndex++;
                    continue;
                case '\'':
                case '"':
                    var findIndex = 0;
                    var findQFlag = false;
                    for (int innerInd = currIndex + 1; innerInd < content.length(); innerInd++) {
                        if (content.charAt(innerInd) == currSymbol) {
                            findQFlag = true;
                            findIndex = innerInd;
                            break;
                        }
                    }
                    if (!findQFlag) {
                        var message = String.format("There is no pair for this symbol %c ", currSymbol);
                        throw new WrongSyntaxException(message);
                    }

                    var rightBorder = findIndex + 1;
                    var stringToPass = content.substring(currIndex, rightBorder);
                    currIndex = rightBorder;

                    var tokenToPass = new Token(stringToPass, Token.getType(currSymbol));
                    tokens.add(tokenToPass);
                    continue;
                case '|':
                    tokenToPass = new Token("|", Token.Type.PIPE_SYMBOL);
                    tokens.add(tokenToPass);

                    currIndex++;
                    continue;
                case '=':
                    substOrAssignment = "assign";
                    break;
                case '$':
                    substOrAssignment = "subst";
                    break;
            }

            currIndex++;
            currToken = currToken.concat(String.valueOf(currSymbol));

        }
        if (!currToken.isEmpty()) {
            var tok = getSpecificToken(currToken, substOrAssignment);
            tokens.add(tok);
        }
        return tokens;

    }

    private Token getSpecificToken(String content, String flag) {
        if (flag.equals("subst")) {
            return new Token(content, Token.Type.SUBST);
        }
        if (flag.equals("assign")) {
            return new Token(content, Token.Type.ASSIGN);
        }
        return new Token(content, Token.Type.JUST_TEXT);
    }
}
