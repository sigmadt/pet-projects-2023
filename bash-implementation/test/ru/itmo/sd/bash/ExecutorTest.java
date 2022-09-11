package ru.itmo.sd.bash;


import org.junit.jupiter.api.Test;
import ru.itmo.sd.bash.res.utils.EnvManager;
import ru.itmo.sd.bash.res.utils.Executor;
import ru.itmo.sd.bash.res.utils.Token;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExecutorTest {

    @Test
    void simplePipeAssignmentTest() {
        var executor = new Executor();
        var env = new EnvManager();

        var tokens = Arrays.asList(
                new Token("z=17", Token.Type.ASSIGN),
                new Token("|", Token.Type.PIPE_SYMBOL),
                new Token("x=1243", Token.Type.ASSIGN)
        );

        executor.run(tokens, env);

        assertEquals("17", env.get("z"));
        assertEquals("1243", env.get("x"));
    }

    @Test
    void pipeAssignmentTestWithOverride() {
        var executor = new Executor();
        var env = new EnvManager();

        var tokens = Arrays.asList(
                new Token("z=ec", Token.Type.ASSIGN),
                new Token("|", Token.Type.PIPE_SYMBOL),
                new Token("x=ho", Token.Type.ASSIGN),
                new Token("|", Token.Type.PIPE_SYMBOL),
                new Token("x=diff", Token.Type.ASSIGN),
                new Token("|", Token.Type.PIPE_SYMBOL),
                new Token("z=new", Token.Type.ASSIGN)
        );

        executor.run(tokens, env);


        assertEquals("new", env.get("z"));
        assertEquals("diff", env.get("x"));
    }


    @Test
    void pipeEchoWcTest() {
        var executor = new Executor();
        var env = new EnvManager();

        var tokens =
                Arrays.asList(
                        new Token("echo", Token.Type.JUST_TEXT),
                        new Token("smth smth smth", Token.Type.JUST_TEXT),
                        new Token("|", Token.Type.PIPE_SYMBOL),
                        new Token("wc", Token.Type.JUST_TEXT)
                );

        var input = executor.run(tokens, env);
        String res = "";
        try {
            res = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertEquals("15\t3\t1 ", res);
    }

}