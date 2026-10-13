package main.java.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public class JsonSerializer {

    public static String serialize(Object object) {
        StringBuilder sb = new StringBuilder();
        Set<Object> visites = Collections.newSetFromMap(new IdentityHashMap<Object, Boolean>());
        ecrire(object, sb, visites);
        return sb.toString();
    }

    private static void ecrire(Object objet, StringBuilder sb, Set<Object> visites) {
        if (objet == null) {
            sb.append("null");
            return;
        }

        Class<?> type = objet.getClass();

        if (objet instanceof Number || objet instanceof Boolean) {
            sb.append(objet);
            return;
        }

        if (objet instanceof Character || objet instanceof CharSequence || type.isEnum()) {
            citer(sb, objet.toString());
            return;
        }

        if (objet instanceof Map) {
            ecrireMap((Map<?, ?>) objet, sb, visites);
            return;
        }

        if (objet instanceof Collection) {
            ecrireCollection((Collection<?>) objet, sb, visites);
            return;
        }

        if (type.isArray()) {
            ecrireTableau(objet, sb, visites);
            return;
        }

        ecrireObjet(objet, sb, visites);
    }

    private static void ecrireMap(Map<?, ?> map, StringBuilder sb, Set<Object> visites) {
        verifierCycle(map, visites);
        visites.add(map);
        sb.append("{");
        boolean premier = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!premier) sb.append(",");
            premier = false;
            citer(sb, String.valueOf(entry.getKey()));
            sb.append(":");
            ecrire(entry.getValue(), sb, visites);
        }
        visites.remove(map);
        sb.append("}");
    }

    private static void ecrireCollection(Collection<?> collection, StringBuilder sb, Set<Object> visites) {
        verifierCycle(collection, visites);
        visites.add(collection);
        sb.append("[");
        boolean premier = true;
        for (Object element : collection) {
            if (!premier) sb.append(",");
            premier = false;
            ecrire(element, sb, visites);
        }
        visites.remove(collection);
        sb.append("]");
    }

    private static void ecrireTableau(Object tableau, StringBuilder sb, Set<Object> visites) {
        verifierCycle(tableau, visites);
        visites.add(tableau);
        sb.append("[");
        int longueur = Array.getLength(tableau);
        for (int i = 0; i < longueur; i++) {
            if (i > 0) sb.append(",");
            ecrire(Array.get(tableau, i), sb, visites);
        }
        visites.remove(tableau);
        sb.append("]");
    }

    private static void ecrireObjet(Object objet, StringBuilder sb, Set<Object> visites) {
        verifierCycle(objet, visites);
        visites.add(objet);
        sb.append("{");
        boolean premier = true;
        for (Method getter : objet.getClass().getMethods()) {
            String nom = nomPropriete(getter);
            if (nom == null) continue;

            Object valeur;
            try {
                getter.setAccessible(true);
                valeur = getter.invoke(objet);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(
                    "Getter " + objet.getClass().getSimpleName() + "." + getter.getName()
                    + " a echoue : " + e.getCause(), e);
            }

            if (!premier) sb.append(",");
            premier = false;
            citer(sb, nom);
            sb.append(":");
            ecrire(valeur, sb, visites);
        }
        visites.remove(objet);
        sb.append("}");
    }

    private static String nomPropriete(Method getter) {
        if (getter.getParameterCount() != 0 || getter.getReturnType().equals(Void.TYPE)) {
            return null;
        }

        String nom = getter.getName();
        if (nom.equals("getClass")) {
            return null;
        }
        if (nom.startsWith("get") && nom.length() > 3) {
            return Character.toLowerCase(nom.charAt(3)) + nom.substring(4);
        }
        if (nom.startsWith("is") && nom.length() > 2
                && (getter.getReturnType().equals(boolean.class)
                    || getter.getReturnType().equals(Boolean.class))) {
            return Character.toLowerCase(nom.charAt(2)) + nom.substring(3);
        }
        return null;
    }

    private static void verifierCycle(Object objet, Set<Object> visites) {
        if (visites.contains(objet)) {
            throw new RuntimeException(
                "Cycle detecte dans la serialisation JSON pour : " + objet.getClass().getName());
        }
    }

    private static void citer(StringBuilder sb, String valeur) {
        sb.append("\"");
        for (int i = 0; i < valeur.length(); i++) {
            char c = valeur.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append("\"");
    }
}
