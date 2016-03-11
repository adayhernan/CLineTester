import java.io.IOException;
import java.net.UnknownHostException;

public class Main {
    public static void main(String[] args) throws UnknownHostException, IOException {
    	
        String host = "server.clineserver.com";
        int port = 99999;
        String user = "user";
        String password = "pass";

    	boolean valid = false;
    	
    	try {
			valid = new CCcamConnector(host, port, user, password).TestCline();
		} catch (Exception e) {
			e.printStackTrace();
		}    	
    	
    	if (valid)
    		System.out.println("CCLine is valid");
    	else
    		System.out.println("CCLine is NOT valid");
    }
}
