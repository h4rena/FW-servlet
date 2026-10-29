package main.java.model;

import java.util.HashMap;
import java.util.Map;

public class Session {

    private final Map<String, Object> data = new HashMap<>();

    public void set(String cle, Object valeur) {
        data.put(cle, valeur);
    }

    public Object get(String cle) {
        return data.get(cle);
    }

    public boolean has(String cle) {
        return data.containsKey(cle);
    }

    public boolean estConnecte() {
        return data.containsKey("user");
    }

    public boolean aRole(String role) {
        return role != null && role.equals(data.get("role"));
    }

    public void remove(String cle) {
        data.remove(cle);
    }

    public void clear() {
        data.clear();
    }

    public Map<String, Object> getData() {
        return data;
    }

    public String toString() {
        return "Session" + data;
    }
}
