package main.java.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class Binder {

    public static Object[] bind(HttpServletRequest req, Method method) {
        Parameter[] params = method.getParameters();
        Object[] args = new Object[params.length];

        for (int i = 0; i < params.length; i++) {
            String nom = nomParametre(params[i], method);
            Class<?> type = params[i].getType();

            if (type == String.class || type == int.class || type == boolean.class) {
                args[i] = convertirNonObjet(req.getParameter(nom), type, nom, method.getName());
            } else {
                args[i] = construireObjet(req, type, nom, method.getName());
            }
        }

        return args;
    }

    private static Object convertirNonObjet(String brut, Class<?> type,
                                    String chemin, String methode) {

        if (type == String.class) {
            return brut;
        }

        if (type == int.class) {
            if (brut == null || brut.isBlank()) {
                return Integer.valueOf(0);
            }
            try {
                return Integer.valueOf(brut);
            } catch (NumberFormatException nfe) {
                throw new RuntimeException(
                    "Parametre '" + chemin + "' de la methode " + methode
                    + " : '" + brut + "' n'est pas un entier");
            }
        }

        if (type == boolean.class) {
            return "true".equalsIgnoreCase(brut)
                || "on".equalsIgnoreCase(brut)
                || "1".equals(brut);
        }

        throw new RuntimeException(
            "Type de parametre non supporte : " + type.getName()
            + " (methode " + methode + ", parametre " + chemin + ")");
    }

    private static Object construireObjet(HttpServletRequest req, Class<?> type,
                                     String chemin, String methode) {

        if (type == HttpServletRequest.class
                || type == HttpServletResponse.class
                || type == HttpSession.class) {
            throw new RuntimeException(
                "Injection de " + type.getName() + " non supportee"
                + " (methode " + methode + ", parametre " + chemin + ")");
        }

        if (type.isInterface()) {
            throw new RuntimeException(
                "Type " + type.getName() + " : interface, impossible a construire"
                + " (methode " + methode + ", parametre " + chemin + ")");
        }

        Constructor<?> constructeur;
        try {
            constructeur = type.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(
                "Type " + type.getName() + " sans constructeur sans argument"
                + " (methode " + methode + ", parametre " + chemin + ")");
        }

        Object instance;
        try {
            constructeur.setAccessible(true);
            instance = constructeur.newInstance();
        } catch (ReflectiveOperationException | RuntimeException e) {
            throw new RuntimeException(
                "Impossible d'instancier " + type.getName() + " : " + e, e);
        }

        for (Method setter : type.getMethods()) {
            if (!setter.getName().startsWith("set")) continue;
            if (setter.getName().length() <= 3) continue;
            if (setter.getParameterCount() != 1) continue;

            String prop = nomPropriete(setter.getName());
            String brut = req.getParameter(prop);
            if (brut == null) continue;

            Object valeur = convertirNonObjet(brut, setter.getParameterTypes()[0],
                                      type.getSimpleName() + "." + prop, methode);

            try {
                setter.invoke(instance, valeur);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(
                    "Setter " + type.getSimpleName() + "." + setter.getName()
                    + " a echoue : " + e.getCause(), e);
            }
        }

        return instance;
    }

    private static String nomPropriete(String setter) {
        String brut = setter.substring(3);
        return Character.toLowerCase(brut.charAt(0)) + brut.substring(1);
    }

    private static String nomParametre(Parameter p, Method method) {
        if (!p.isNamePresent())
            throw new RuntimeException(
                "Nom du parametre indisponible pour " + method.getName()
                + " : recompiler avec -parameters");
        return p.getName();
    }
}
