package database;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;

public final class Database {

    private static volatile Connection c;

    public static Connection getConnection() {

        Connection result = c;
        if (result != null) {
            return result;
        }
        synchronized(Database.class) {
            if (c == null) {
                try {
                    URI dbUri = new URI(System.getenv("DATABASE_URL"));

                    String username = dbUri.getUserInfo().split(":")[0];
                    String password = dbUri.getUserInfo().split(":")[1];
                    String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();

                    c = DriverManager.getConnection(dbUrl, username, password);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println(e.getClass().getName() + ": " + e.getMessage());
                    c = null;
                }
            }

            return c;
        }
    }
}
