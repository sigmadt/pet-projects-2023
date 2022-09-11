package ru.itmo.sd.bash;


import org.junit.jupiter.api.Test;
import ru.itmo.sd.bash.res.utils.Utils;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WcTest {
    private static final LinkedHashMap<String, String> rightStatsToPath = new LinkedHashMap<>() {{
        this.put("zmej", "160\t30\t10");
        this.put("forth", "308\t75\t18");
        this.put("test", "49\t10\t5");
    }};

    public static final Map<String, String> namesToPath = Utils.getTestFilesPaths();

    @Test
    void wcOneFileTest() {
        var bash = new BashTranslateHelper();

        for (var fileName : namesToPath.keySet()) {
            assertEquals(
                    String.format("%s %s\n", rightStatsToPath.get(fileName), namesToPath.get(fileName)),
                    bash.run(String.format("wc %s", namesToPath.get(fileName)))
            );
        }


    }

    @Test
    void wcManyFileTest() {
        var bash = new BashTranslateHelper();
        var totalStats = new StringBuilder();


        for (var fileName : rightStatsToPath.keySet()) {
            totalStats.append(rightStatsToPath.get(fileName)).append(" ");
            totalStats.append(namesToPath.get(fileName)).append("\n");
        }

        totalStats.append("517\t115\t33 total\n");

        var allPaths = String.join(" ", namesToPath.values());
        assertEquals(
                totalStats.toString(),
                bash.run(String.format("wc %s", allPaths)));

    }
}
