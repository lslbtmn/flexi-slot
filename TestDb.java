import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestDb {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://db.gjsxhlpqsribyilidwzc.supabase.co:5432/postgres?sslmode=require";
        String user = "postgres";
        String password = "Po9amvIVps5LI675";

        try {
            System.out.println("Connecting to database...");
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connection successful!");
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
