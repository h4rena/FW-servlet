package main.java.utils;

import java.io.InputStream;
import java.util.List;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import main.java.model.Session;

public class AppListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        System.out.println("Application démarrée");

        try {
            ServletContext ctx = sce.getServletContext();

            //pour application.properties 
            InputStream in = ctx.getResourceAsStream("/WEB-INF/application.properties");
            AppConfig.load(in);
            System.out.println("upload.dir = " + AppConfig.get("upload.dir"));
            //

            //utilisateurs -> roles (web.xml)
            RoleConfig.load(ctx.getInitParameter("user-roles"));
            System.out.println("user-roles = " + ctx.getInitParameter("user-roles"));

            String pkg = ctx.getInitParameter("controllers-package");

            if (pkg == null) pkg = "main.java.controllers";

            List<String> controllers = new scanController().scanControllers(pkg);

            ctx.setAttribute("controllers", controllers);

            //pour la session
            ctx.setAttribute("session", new Session());

            System.out.println("Controllers scannés : " + controllers);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
