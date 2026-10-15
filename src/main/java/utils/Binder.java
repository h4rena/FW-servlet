package main.java.utils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

import main.java.model.MapFile;

public class Binder {

    public static Object[] bind(HttpServletRequest req, Method method) {
        Parameter[] params = method.getParameters();
        Object[] args = new Object[params.length];

        for (int i = 0; i < params.length; i++) {
            String nom = nomParametre(params[i], method);
            Class<?> type = params[i].getType();

            if (type == String.class || type == int.class || type == boolean.class) {
                args[i] = convertirNonObjet(req.getParameter(nom), type, nom, method.getName());
            } else if (type == MapFile.class) {
                args[i] = construireFichier(req, nom, method.getName());
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

        if (type.isInterface()) {
            throw new RuntimeException(
                "Type " + type.getName() + " : interface, impossible a construire"
                + " (methode " + methode + ", parametre " + chemin + ")");
        }

        Object instance = instancier(type, chemin, methode);
        remplirSetters(instance, type, req, chemin, methode);
        return instance;
    }

    private static Object instancier(Class<?> type, String chemin, String methode) {
        Constructor<?> constructeur;
        try {
            constructeur = type.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(
                "Type " + type.getName() + " sans constructeur sans argument"
                + " (methode " + methode + ", parametre " + chemin + ")");
        }

        try {
            constructeur.setAccessible(true);
            return constructeur.newInstance();
        } catch (ReflectiveOperationException | RuntimeException e) {
            throw new RuntimeException(
                "Impossible d'instancier " + type.getName() + " : " + e, e);
        }
    }

    private static void remplirSetters(Object instance, Class<?> type,
                                     HttpServletRequest req, String chemin, String methode) {
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

    private static MapFile construireFichier(HttpServletRequest req, String nom, String methode) {
    Part part;
    try {
        part = req.getPart(nom);
    } catch (IOException | ServletException e) {
        throw new RuntimeException("Lecture du fichier '" + nom + "' impossible"
            + " (methode " + methode + ") : " + e.getMessage(), e);
    }
    if (part == null) return null;               // champ absent

    String nomFichier = part.getSubmittedFileName();
    if (nomFichier == null) nomFichier = "";
    byte[] contenu;
    try (InputStream in = part.getInputStream()) {
        contenu = in.readAllBytes();
    } catch (IOException e) {
        throw new RuntimeException("Lecture du contenu de '" + nomFichier
            + "' impossible : " + e.getMessage(), e);
    }

    //pour upload avec application.properties
    MapFile fichier = new MapFile(nomFichier, extensionDe(nomFichier), contenu);

    String dir = AppConfig.get("upload.dir");
    if (dir != null && !dir.isBlank() && !nomFichier.isBlank()) {
        try {
            String sur = Paths.get(nomFichier).getFileName().toString();  // anti "../"
            Path dossier = Paths.get(dir);
            Files.createDirectories(dossier);                             // crée si absent
            Path dest = dossier.resolve(sur);
            Files.write(dest, contenu);                                   // écrire les octets
            fichier.setChemin(dest.toString());
        } catch (IOException e) {
            throw new RuntimeException("Ecriture de '" + nomFichier + "' impossible : "
                + e.getMessage(), e);
        }
    }
    return fichier;
    }

    private static String extensionDe(String nomFichier) {
        int point = nomFichier.lastIndexOf('.');
        if (point <= 0 || point == nomFichier.length() - 1) return "";
        return nomFichier.substring(point + 1);
    }

}
