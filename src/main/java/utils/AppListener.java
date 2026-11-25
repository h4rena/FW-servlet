package main.java.utils;

import java.io.InputStream;
import java.util.List;

import jakarta.persistence.EntityManagerFactory;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import main.java.config.FrameworkJpaConfig;
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

            //acces base via context-param (web.xml)
            ctx.setAttribute("database", new Database(ctx));
            System.out.println("db.url = " + ctx.getInitParameter("db.url"));

            initialiserJpa(ctx);

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

    private void initialiserJpa(ServletContext ctx) {
        String url = ctx.getInitParameter("db.url");
        if (url == null || url.isBlank()) {
            System.out.println("JPA non configuré (db.url absent)");
            return;
        }

        try {
            AnnotationConfigWebApplicationContext spring = new AnnotationConfigWebApplicationContext();
            spring.setServletContext(ctx);
            spring.register(FrameworkJpaConfig.class);
            spring.refresh();

            ctx.setAttribute("springContext", spring);
            ctx.setAttribute("entityManagerFactory", spring.getBean(EntityManagerFactory.class));
            ctx.setAttribute("transactionManager", spring.getBean("transactionManager", PlatformTransactionManager.class));

            System.out.println("JPA OK : contexte Spring chargé");
        } catch (Exception e) {
            System.out.println("JPA ERROR : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        Object spring = sce.getServletContext().getAttribute("springContext");
        if (spring instanceof ConfigurableApplicationContext context) {
            context.close();
        }
    }
}
