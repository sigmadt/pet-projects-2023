package ru.itmo.sd.bash.res.utils;

import java.util.ArrayList;
import java.util.List;

public class Replacer {

    public String applyHelper(String input, EnvManager envManager) {
        var currIndex = 0;
        var content = "";


        while (currIndex < input.length()) {
            var currSymbol = input.charAt(currIndex);

            if (currSymbol != '$') {
                content = content.concat(String.valueOf(currSymbol));
                currIndex++;
            } else {
                int varIndex = currIndex + 1;
                StringBuilder variable = new StringBuilder();

                while (varIndex < input.length() && Character.isLetterOrDigit(input.charAt(varIndex))) {
                    variable.append(input.charAt(varIndex));
                    varIndex++;
                }

                if (variable.length() > 0) {
                    content = content.concat(envManager.get(variable.toString()));
                } else {
                    content = content.concat(String.valueOf(currSymbol));
                }
                currIndex = varIndex;
            }


        }

        return content;
    }



    public List<Token> apply(List<Token> givenTokens, EnvManager envManager) {
        List<Token> resultTokens = new ArrayList<>();

        for (var token : givenTokens) {
            var tokenType = token.getType();

            switch (tokenType) {
                case SUBST:
                    var inside = token.getInside();
                    var replacedStr = applyHelper(inside, envManager);

                    var currToken = new Token(replacedStr, Token.Type.JUST_TEXT);
                    resultTokens.add(currToken);
                    continue;
                case DOUBLE_Q_TEXT:
                    inside = token.getInside();
                    replacedStr = applyHelper(inside.substring(1, inside.length() - 1), envManager);

                    currToken = new Token(replacedStr, Token.Type.JUST_TEXT);
                    resultTokens.add(currToken);
                    continue;
                case SINGLE_Q_TEXT:
                    inside = token.getInside();

                    currToken = new Token(inside.substring(1, inside.length() - 1), Token.Type.JUST_TEXT);
                    resultTokens.add(currToken);
                    continue;
                default:
                    resultTokens.add(token);
            }
        }

        return resultTokens;
    }

}