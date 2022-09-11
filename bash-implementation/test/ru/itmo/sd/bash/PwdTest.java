package ru.itmo.sd.bash;


import org.junit.jupiter.api.Test;
import ru.itmo.sd.bash.res.utils.Utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PwdTest {

    @Test
    void simplePwdTest() {
        var bash = new BashTranslateHelper();

        assertEquals(Utils.getCurrentDir().toString().concat("\n"), bash.run("pwd"));
    }

}