import java.io.IOException;
import java.net.UnknownHostException;

public class Main {
    public static void main(String[] args) throws UnknownHostException, IOException {
    	
        String host = "ccamline.server.com";
        int port = 9999;
        String user = "username";
        String password = "password";

    	new CCcamConnector(host, port, user, password).run();
    }
}
