import java.net.URI;

public class TestURI {
    public static void main(String[] args) throws Exception {
        String workingUrl = "postgresql://postgres.gjsxhlpqsribyilidwzc:Po9amvIVps5LI675@aws-0-eu-central-1.pooler.supabase.com:6543/postgres";
        URI dbUri = new URI(workingUrl);
        String userInfo = dbUri.getUserInfo();
        System.out.println("UserInfo: " + userInfo);
        String[] split = userInfo.split(":");
        System.out.println("Username: " + split[0]);
        System.out.println("Password: " + split[1]);
    }
}
