package ru.itmo.sd.bash;


import org.junit.jupiter.api.Test;
import ru.itmo.sd.bash.res.utils.Parser;
import ru.itmo.sd.bash.res.utils.Token;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParserTest {
    @Test
    void allTokenizerTest() {
        var parser = new Parser();
        var resListToken = parser.run(
                "\" double q text \""
                        .concat(" 'single q text'")
                        .concat("$subst ")
                        .concat("| pipe ")
                        .concat("justtext ")
                        .concat("assign=12 "));


        var correctListToken = Arrays.asList(
                new Token("\" double q text \"", Token.Type.DOUBLE_Q_TEXT),
                new Token("'single q text'", Token.Type.SINGLE_Q_TEXT),
                new Token("$subst", Token.Type.SUBST),
                new Token("|", Token.Type.PIPE_SYMBOL),
                new Token("pipe", Token.Type.JUST_TEXT),
                new Token("justtext", Token.Type.JUST_TEXT),
                new Token("assign=12", Token.Type.ASSIGN)

        );

        assertTrue(
                resListToken.size() == correctListToken.size() &&
                        resListToken.containsAll(correctListToken) &&
                        correctListToken.containsAll(resListToken));

    }

    @Test
    void assignAndSubstTest() {
        var parser = new Parser();
        var resListToken = parser.run(
                "x=17 "
                        .concat("| ")
                        .concat("$x ")
                        .concat("| var=1234 ")
                        .concat("| y=word ")
                        .concat("| $y"));


        var correctListToken = Arrays.asList(
                new Token("x=17", Token.Type.ASSIGN),
                new Token("|", Token.Type.PIPE_SYMBOL),
                new Token("$x", Token.Type.SUBST),
                new Token("|", Token.Type.PIPE_SYMBOL),
                new Token("var=1234", Token.Type.ASSIGN),
                new Token("|", Token.Type.PIPE_SYMBOL),
                new Token("y=word", Token.Type.ASSIGN),
                new Token("|", Token.Type.PIPE_SYMBOL),
                new Token("$y", Token.Type.SUBST)
        );

        assertTrue(
                resListToken.size() == correctListToken.size() &&
                        resListToken.containsAll(correctListToken) &&
                        correctListToken.containsAll(resListToken));


    }
}