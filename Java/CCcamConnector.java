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

  
  public static int[] unsignedToBytes(byte[] b) {
	  int[] unsigned = new int[b.length];
	  for (int i = 0; i< b.length; i++)		  
		  unsigned[i] =  b[i] & 0xFF;
  		return unsigned;
  }
  
  public synchronized int SendMsg(int len, byte[] buf) throws IOException {
	  	byte[] netbuf;
		netbuf = new byte[len];
		System.arraycopy(buf, 0, netbuf, 0, len);
		sendblock.cc_encrypt(netbuf, len);
		
		try {
			int[] t = unsignedToBytes(netbuf);
		  os.write(netbuf);
		  os.flush();        
		  return len;
		} catch(IOException e) {
		  socket.close();
		}
		return -1;
  }

  public boolean TestCline() throws Exception {
    try {
    	
	  	byte[] helloBytes = new byte[16];
		is.readFully(helloBytes);
						
	  	CryptoBlock.cc_crypt_xor(helloBytes);  // XOR init bytes with 'CCcam'

	    MessageDigest md;
	    md = MessageDigest.getInstance("SHA-1");
	    byte[] sha1hash = new byte[20];
		sha1hash = md.digest(helloBytes);		

		recvblock = new CryptoBlock();
		recvblock.cc_crypt_init(sha1hash, 20);
		recvblock.cc_decrypt(helloBytes, 16);
		
		sendblock = new CryptoBlock();
		sendblock.cc_crypt_init(helloBytes, 16);
		sendblock.cc_decrypt(sha1hash, 20);
		
		SendMsg(20, sha1hash );//send crypted hash to server
		
	    byte[] userBuf = new byte[20];
		System.arraycopy(user.getBytes(), 0, userBuf, 0, user.length());
		SendMsg(20, userBuf);//send username to server	
		
		byte[] pwd = new byte[63];
		System.arraycopy(password.getBytes(), 0, pwd, 0, password.length());
		sendblock.cc_encrypt(pwd, password.length()); //encript the password
		
		byte[] CCcam = {'C','C','c','a','m',0 };
		SendMsg(6, CCcam); //But send CCcam\0

		byte[] rcvBuf = new byte[20];
	    is.read(rcvBuf);
		recvblock.cc_decrypt(rcvBuf, 20); 
		//received string after decription equals "CCcam"
		if ( Arrays.equals(CCcam, Arrays.copyOf(rcvBuf,6) ) ) {
			//CCLine is correct!!!!
			socket.close();
			is.close();
			os.close();
			return true;
		}
    } catch(Exception e) {
    	System.out.println(e.toString());
    	e.printStackTrace();
    }
	socket.close();
	is.close();
	os.close();
    return false;
  }
}
