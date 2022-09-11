package ru.itmo.sd.bash.res.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EnvManager {
    private final Map<String, String> storage = new HashMap<>();


    public EnvManager() {
        set("PWD", Utils.getCurrentDir().toString());
    }

    public final void set(String variable, String elem) {
        storage.put(variable, elem);
    }

    public final String get(String variable) {
        if (storage.containsKey(variable)) {
            return storage.get(variable);
        }

        if (System.getenv().containsKey(variable)) {
            return System.getenv(variable);
        }

        return "";
    }

    public Set<Map.Entry<String, String>> items() {
        return storage.entrySet();
    }


}