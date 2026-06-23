package main.java.utils;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.java.annotation.FWController;
import main.java.annotation.UrlMapping;

public class scanController {

   private static List<String> controllerNames = new ArrayList<>();

   public static List<String> getControllerNames() {
       return controllerNames;
   }

   public List<String> scanControllers(String packageName) throws Exception {

    List<String> found = new ArrayList<>();

    String path = packageName.replace('.', '/');

    URL url = Thread.currentThread().getContextClassLoader().getResource(path);

    if (url == null) return found;

    File dir = new File(url.toURI());

    for (File file : dir.listFiles()) {
        
        if (file.getName().endsWith(".class")) {
            String className = packageName + "." + file.getName().replace(".class", "");
            Class<?> clazz = Class.forName(className);
            if (clazz.isAnnotationPresent(FWController.class)) {
                found.add(clazz.getName());
                System.out.println("Controller trouvé : " + clazz.getName());
            }
        }
    }
    controllerNames = found;
    return found;
   }

   public static Map<String, Method> getUrlMappings(Class<?> controllerClass) {
    Map<String, Method> mappings = new HashMap<>();
    for (Method method : controllerClass.getDeclaredMethods()) {
        UrlMapping mapping = method.getAnnotation(UrlMapping.class);
        if (mapping != null) {
            mappings.put(mapping.value(), method);
                }
            }
    return mappings;
    }
}