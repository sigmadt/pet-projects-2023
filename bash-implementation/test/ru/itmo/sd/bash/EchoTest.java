package ru.itmo.sd.bash;


import org.junit.jupiter.api.Test;
import ru.itmo.sd.bash.res.utils.Executor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EchoTest {


    @Test
    void simpleEchoTest() {
        var bash = new BashTranslateHelper();

        assertEquals("55\n", bash.run("echo 55"));
        assertEquals("word\n", bash.run("echo word"));
        assertEquals("\n", bash.run("echo"));
    }

    @Test
    void echoAndFriendCommandsTest() {
        var bash = new BashTranslateHelper();
        var executor = new Executor();

        List<String> cmds = executor.getCommandNames();

        for (var cmdName : cmds) {
            assertEquals(String.format("%s\n", cmdName), bash.run(String.format("echo %s", cmdName)));
        }
    }

    @Test
    void pipedEchoTest() {
        var bash = new BashTranslateHelper();

        assertEquals("\n", bash.run("echo 21 | echo"));
        assertEquals("21\n", bash.run("echo | echo 21"));
        assertEquals("last\n", bash.run("echo first " +
                "| echo second " +
                "| echo third " +
                "| echo fourth " +
                "| echo last"));


    }

}