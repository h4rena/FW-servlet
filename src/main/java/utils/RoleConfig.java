package main.java.utils;

import java.util.HashMap;
import java.util.Map;

public class RoleConfig {

    private static Map<String, String> roles = new HashMap<>();

    public static void load(String config) {
        Map<String, String> m = new HashMap<>();
        if (config != null) {
            for (String paire : config.split(",")) {
                String[] p = paire.split(":", 2);
                if (p.length == 2) {
                    m.put(p[0].trim(), p[1].trim());
                }
            }
        }
        roles = m;
    }

    public static String rolePour(String user) {
        return roles.get(user);
    }
}
