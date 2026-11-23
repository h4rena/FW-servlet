package main.java.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import jakarta.servlet.ServletContext;

public class Database {

    private final String driver;
    private final String url;
    private final String user;
    private final String password;
    private final String erreurPilote;

    public Database(ServletContext ctx) {
        this.driver = ctx.getInitParameter("db.driver");
        this.url = ctx.getInitParameter("db.url");
        this.user = ctx.getInitParameter("db.user");
        this.password = ctx.getInitParameter("db.password");

        String err = null;
        if (driver != null && !driver.isBlank()) {
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException e) {
                err = "Pilote JDBC introuvable : " + driver;
            }
        }
        this.erreurPilote = err;
    }

    public Connection getConnection() throws SQLException {
        if (erreurPilote != null) throw new SQLException(erreurPilote);
        return DriverManager.getConnection(url, user, password);
    }

    public String tester() {
        if (url == null || url.isBlank()) return "DB non configuree (db.url absent)";
        try (Connection c = getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT 1")) {
            return rs.next()
                ? "DB OK (SELECT 1 = " + rs.getInt(1) + ") via " + url
                : "DB OK (aucune ligne) via " + url;
        } catch (SQLException e) {
            return "DB ERROR : " + e.getMessage();
        }
    }
}
