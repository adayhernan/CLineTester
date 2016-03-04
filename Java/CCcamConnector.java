import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.*;

public class CCcamConnector {

  private String host, user, password;
  private int port;
  private CryptoBlock recvblock;
  private CryptoBlock sendblock;
  private Socket socket;
  private DataInputStream is;
  private BufferedOutputStream os;

  public CCcamConnector(String host, int port, String username, String psw) 
		  throws UnknownHostException, IOException {

    this.host = host;
    this.port = port;
    this.user = username;
    this.password = psw;
    
    socket = new Socket(this.host, this.port);
    is = new DataInputStream(socket.getInputStream());
    os = new BufferedOutputStream(socket.getOutputStream());
  }

  
  public synchronized int SendMsg(int len, byte[] buf) throws IOException {
	  	byte[] netbuf;
		netbuf = new byte[len];
		System.arraycopy(buf, 0, netbuf, 0, len);
		sendblock.cc_encrypt(netbuf, len);
		
		try {
		  os.write(netbuf);
		  os.flush();        
		  return len;
		} catch(IOException e) {
		  socket.close();
		}
		return -1;
  }

  public void serverHandshake(byte[] random16) throws IOException, NoSuchAlgorithmException {
	  	is.readFully(random16);
	  	CryptoBlock.cc_crypt_xor(random16);  // XOR init bytes with 'CCcam'

	    MessageDigest md;
	    md = MessageDigest.getInstance("SHA-1");
	    byte[] sha1hash = new byte[40];
	    md.update( random16 );
		sha1hash = md.digest();

		//init crypto states
		recvblock = new CryptoBlock();
		recvblock.cc_crypt_init(sha1hash, 20);
		recvblock.cc_decrypt(random16, 16);
		
		sendblock = new CryptoBlock();
		sendblock.cc_crypt_init(random16, 16);
		sendblock.cc_decrypt(sha1hash, 20);
		
		SendMsg(20, sha1hash );   // send crypted hash to server
	  }

  public void run() {
    try {
    	byte[] random16 = new byte[16];
    	serverHandshake(random16);    	

	    byte[] buf = new byte[20];
		System.arraycopy(user.getBytes(), 0, buf, 0, user.length() );
		SendMsg(20, buf );		
		
		byte[] pwd = new byte[63];
		System.arraycopy(password.getBytes(), 0, pwd, 0, password.length() );
		sendblock.cc_encrypt( pwd, password.length() );
		
		byte[] CCcam = {'C','C','c','a','m',0 };
		SendMsg(6, CCcam );

	    is.readFully( buf, 0, 20 );
		recvblock.cc_decrypt(buf,20);
		if ( Arrays.equals(CCcam, Arrays.copyOf(buf,6) ) ) {
			//WORKED!!!!
			socket.close();
		}

    } catch(SocketException e) {
    	System.out.println(e.toString());
    } catch(Exception e) {
    	System.out.println(e.toString());
      e.printStackTrace();
    }
  }
}
