package main.java.utils;

import java.util.List;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public class AppListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        System.out.println("Application démarrée");

        try {
            ServletContext ctx = sce.getServletContext();

            String pkg = ctx.getInitParameter("controllers-package");

            if (pkg == null) pkg = "main.java.controllers";

            List<String> controllers = new scanController().scanControllers(pkg);

            ctx.setAttribute("controllers", controllers);

            System.out.println("Controllers scannés : " + controllers);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
