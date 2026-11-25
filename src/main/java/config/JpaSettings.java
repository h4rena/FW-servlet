package main.java.config;

import jakarta.servlet.ServletContext;

public final class JpaSettings {

    private final String driver;
    private final String url;
    private final String user;
    private final String password;
    private final boolean showSql;

    public JpaSettings(String driver, String url, String user, String password, boolean showSql) {
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.password = password;
        this.showSql = showSql;
    }

    public static JpaSettings from(ServletContext ctx) {
        String driver = param(ctx, "org.postgresql.Driver", "db.driver");
        String url = param(ctx, "", "db.url");
        String user = param(ctx, "", "db.user");
        String password = param(ctx, "", "db.password");
        boolean showSql = Boolean.parseBoolean(param(ctx, "false", "jpa.show-sql", "hibernate.show_sql"));
        return new JpaSettings(driver, url, user, password, showSql);
    }

    private static String param(ServletContext ctx, String defaultValue, String... names) {
        for (String name : names) {
            String value = ctx.getInitParameter(name);
            if (value != null && !value.isBlank()) return value.trim();
        }
        return defaultValue;
    }

    public String driver() { return driver; }

    public String url() { return url; }

    public String user() { return user; }

    public String password() { return password; }

    public boolean showSql() { return showSql; }
}
