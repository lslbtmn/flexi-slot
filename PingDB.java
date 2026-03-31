import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PingDB {
    public static void main(String[] args) {
        String jdbcUrl = "jdbc:postgresql://aws-1-eu-west-1.pooler.supabase.com:5432/postgres?sslmode=require";
        String user = "postgres.gjsxhlpqsribyilidwzc";
        String pass = "oYSspcapvfOvhRKa";
        try {
            System.out.println("Connecting...");
            Connection conn = DriverManager.getConnection(jdbcUrl, user, pass);
            System.out.println("SUCCESSFULLY CONNECTED TO SUPABASE!");
            conn.close();
        } catch (SQLException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }
}
