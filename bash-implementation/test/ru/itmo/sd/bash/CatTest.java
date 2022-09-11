package ru.itmo.sd.bash;


import org.junit.jupiter.api.Test;
import ru.itmo.sd.bash.res.utils.Utils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CatTest {
    public static final Map<String, String> namesToPath = Utils.getTestFilesPaths();


    @Test
    void simpleCatTest() {
        var bash = new BashTranslateHelper();

        var content =
                "some content here\n" +
                        "as well\n" +
                        "as lines\n" +
                        "that many\n" +
                        "yeah";

        assertEquals(content, bash.run(String.format("cat %s", namesToPath.get("test"))));

    }

    @Test
    void pipedCatTest() {
        var bash = new BashTranslateHelper();
        assertEquals("some content from echo\n", bash.run("echo some content from echo | cat"));

    }

}