package ru.itmo.sd.bash;


import org.junit.jupiter.api.Test;
import ru.itmo.sd.bash.res.utils.Utils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GrepTest {
    public static final Map<String, String> namesToPath =
            new HashMap<>(Utils.getTestFilesPaths()) {
                {this.put("grep", "test/ru/itmo/sd/bash/temp/grep.cpp");}
            };


    @Test
    void grepSimplePatternTest() {
        var bash = new BashTranslateHelper();

        var correctOutput =
                "def f():\n" +
                "def yes():\n";

        assertEquals(correctOutput,
                bash.run(
                        String.format(
                                "grep def %s", namesToPath.get("zmej"))));


    }

    @Test
    void grepRegexPatternTest() {
        var bash = new BashTranslateHelper();

        var correctOutput =
                "3 4 + .\n" +
                "12 12 / .\n" +
                "1360 23 - .\n" +
                "17 4 mod .\n" +
                ": .less64 ( n -- n ) dup 64 > if .\" Greater than 64!\" else .\" Less than 64!\" then ;\n";

        assertEquals(correctOutput,
                bash.run(
                        String.format(
                                "grep \"\\d\" %s", namesToPath.get("forth"))));

        assertEquals("\n\n\n\n",
                bash.run(
                        String.format(
                                "grep \"^$\" %s", namesToPath.get("forth"))));
    }

    @Test
    void grepFlagCTest() {
        var bash = new BashTranslateHelper();


        assertEquals("2\n",
                bash.run(
                        String.format(
                                "grep -c def %s", namesToPath.get("zmej"))));

        assertEquals("2\n",
                bash.run(
                        String.format(
                                "grep -c as %s", namesToPath.get("test"))));

    }

    @Test
    void grepFlagCMultipleFilesTest() {
        var bash = new BashTranslateHelper();


        var correctOutput =
                "test/ru/itmo/sd/bash/temp/grep.cpp:2\n" +
                "test/ru/itmo/sd/bash/temp/zmej.py:4\n";

        assertEquals(correctOutput,
                bash.run(
                        String.format(
                                "grep -c yes %s %s",
                                namesToPath.get("grep"),
                                namesToPath.get("zmej"))));

    }

    @Test
    void grepFlagWTest() {
        var bash = new BashTranslateHelper();

        var correctOutput =
                "def yes():\n" +
                "doc string included zmej yes\n" +
                "ot not? - answer is yes\n";

        assertEquals(correctOutput,
                bash.run(
                        String.format(
                                "grep -w yes %s", namesToPath.get("zmej"))));

    }

    @Test
    void grepFlagLTest() {
        var bash = new BashTranslateHelper();


        var correctOutput =
                "test/ru/itmo/sd/bash/temp/grep.cpp\n" +
                        "test/ru/itmo/sd/bash/temp/zmej.py\n";

        assertEquals(correctOutput,
                bash.run(
                        String.format(
                                "grep -l yes %s %s",
                                namesToPath.get("grep"),
                                namesToPath.get("zmej"))));

    }

    @Test
    void grepFlagITest() {
        var bash = new BashTranslateHelper();

        var correctOutput =
                  "    \"SenSItive \" <<\n" +
                  "    \"or SENSITIVE \" <<\n" +
                  "    \"or even sensitive \" <<\n" +
                  "     \"or SENsensitive ?\" << endl;\n";

        assertEquals(correctOutput,
                bash.run(
                        String.format(
                                "grep -i sensitive %s",
                                namesToPath.get("grep"))));

    }


    @Test
    void grepFlagATest() {
        var bash = new BashTranslateHelper();


        var correctOutputOneFile =
                    "as well\n" +
                    "as lines\n" +
                    "that many\n" +
                    "yeah\n";

        var correctOutputTwoFiles =
                    "    cout << \"yes a lot of yes words\" << endl;\n" +
                    "\n" +
                    "\n" +
                    "    auto z = 45 * 43;\n" +
                    "\n" +
                    "    cout << z << \" yes is answer to all world's questions\";\n" +
                    "\n" +
                    "    cout <<\n" +
                    "    \"SenSItive \" <<\n" +
                    "    \"or SENSITIVE \" <<\n" +
                    "~~~~~~~~~~\n" +
                    "def yes():\n" +
                    "    return \"yesssss i will not be processed with -w flag :()\"\n" +
                    "\n" +
                    "'''\n" +
                    "doc string included zmej yes\n" +
                    "ot not? - answer is yes\n" +
                    "'''\n";


        assertEquals(correctOutputOneFile,
                bash.run(
                        String.format(
                                "grep -A 3 well %s",
                                namesToPath.get("test"))));


        assertEquals(correctOutputTwoFiles,
                bash.run(
                        String.format(
                                "grep -A 4 yes %s %s",
                                namesToPath.get("grep"),
                                namesToPath.get("zmej"))));
    }

    @Test
    void grepFlagANegativeValueTest() {
        var bash = new BashTranslateHelper();

        var shouldBePositiveMessage =
                "This command can not be processed.\n" +
                " Here is the message : Invalid flag for Grep command. \n" +
                " -A should be : positive \n" +
                " Given : negative \n";

        var shouldBePositiveIntegerValueMessage =
                "This command can not be processed.\n" +
                " Here is the message : Invalid flag for Grep command. \n" +
                " -A should be : positive integer \n" +
                " Given : notInt \n";

        assertEquals(shouldBePositiveMessage,
                bash.run(
                        String.format(
                                "grep -A -7 yes %s",
                                namesToPath.get("zmej"))));

        assertEquals(shouldBePositiveIntegerValueMessage,
                bash.run(
                        String.format(
                                "grep -A notInt oh %s %s",
                                namesToPath.get("grep"),
                                namesToPath.get("zmej"))));


    }



}