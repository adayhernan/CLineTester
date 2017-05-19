//Created by Dagger -- https://github.com/DaggerES
//With the help of ArSi -- https://github.com/arsi-apli

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.*;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CCcamConnector {

    public static final int MSG_CLI_INFO = 0;
    public static final int MSG_ECM_CW = 1;
    public static final int MSG_EMM_ACK = 2;
    public static final int MSG_CARD_DEL = 4;
    public static final int MSG_CMD_05 = 5;
    public static final int MSG_KEEPALIVE = 6;
    public static final int MSG_CARD_ADD = 7;
    public static final int MSG_SRV_INFO = 8;

    public static final int MSG_CMD_0A = 10;
    public static final int MSG_CMD_0B = 11;
    public static final int MSG_CMD_0C = 12;
    public static final int MSG_CMD_0D = 13;
    public static final int MSG_CMD_0E = 14;
    public static final int MSG_NEW_CARD_SIDINFO = 15;

    public static final int MSG_ECM_NOK1 = 254;
    public static final int MSG_ECM_NOK2 = 255;

    public static final int MSG_SLEEPSEND = 128;
    public static final int MSG_CACHE_PUSH = 129;

    public static final int MSG_NO_HEADER = 65535;

    private String host, user, password;
    private int port;
    private CryptoBlock recvblock;
    private CryptoBlock sendblock;
    private Socket socket;
    private DataInputStream is;
    private BufferedOutputStream os;
    private Map cardMap = new HashMap();

     public CCcamConnector(String host, int port, String username, String psw)
            throws UnknownHostException, IOException {

        this.host = host;
        this.port = port;
        this.user = username;
        this.password = psw;


        socket = new Socket();
        socket.connect(new InetSocketAddress(this.host, this.port), 2000);
        socket.setSoTimeout(3000);
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
        } catch (IOException e) {
            socket.close();
        }
        return -1;
    }

    public synchronized int cc_msg_send(int cmd, int len, byte[] buf) {
        byte[] netbuf;

        if (cmd == MSG_NO_HEADER) {
            netbuf = new byte[len];
            System.arraycopy(buf, 0, netbuf, 0, len);
        } else {
            netbuf = new byte[4 + len];
            // build command message
            netbuf[0] = 0;   // flags??
            netbuf[1] = (byte) (cmd & 0xff);
            netbuf[2] = (byte) (len >> 8);
            netbuf[3] = (byte) (len & 0xff);
            if (len > 0) {
                System.arraycopy(buf, 0, netbuf, 4, len);
            }
            len += 4;
        }
        sendblock.cc_encrypt(netbuf, len);

        try {
            os.write(netbuf);
            os.flush();
            return len;
        } catch (IOException e) {
        }
        return -1;
    }

    public void cc_send_client_info() {
        byte[] buf = new byte[20 + 8 + 1 + 64];
        System.arraycopy(user.getBytes(), 0, buf, 0, user.length()); // 20
        System.arraycopy(CcData.nodeid, 0, buf, 20, 8);
        buf[28] = 0;
        System.arraycopy(CcData.version.getBytes(), 0, buf, 29, CcData.version.length()); // 32
        System.arraycopy(CcData.build.getBytes(), 0, buf, 61, CcData.build.length()); // 32
        cc_msg_send(MSG_CLI_INFO, 20 + 8 + 1 + 64, buf); // send 'CCcam' xor w/ pwd
    }

    public byte[] readMessage() throws IOException {
        if (is == null) {
            throw new IOException();
        }

        int len = 4;
        byte[] header = new byte[4];
        byte[] data = null;
        is.read(header, 0, 4);
        recvblock.cc_decrypt(header, len);
        int datalen = (((header[2] & 0xe7) * 256) + header[3] & 0xff);
        if (datalen != 0) {  // check if any data is expected in msg
            if (datalen > (1024 - 2)) {
                return null;
            }
            data = new byte[datalen];
            is.read(data, 0, datalen);
            recvblock.cc_decrypt(data, datalen);
        }
        byte[] netbuf = new byte[4 + datalen];
        System.arraycopy(header, 0, netbuf, 0, 4);
        if (datalen > 0) {
            System.arraycopy(data, 0, netbuf, 4, datalen);
        }
        return netbuf;
    }

    private byte[] nodeid = new byte[8];
    private String version = new String();
    private String build = new String();

    private void decodeMesg(CCCamMsg msg) throws IOException {
        byte[] data = msg.getCustomData();
        switch (msg.getCommandTag()) {
            case MSG_CLI_INFO:
                break;

            case MSG_SRV_INFO:
                System.arraycopy(msg.getCustomData(), 0, nodeid, 0, 8);
                version = new String(msg.getCustomData(), 8, 31);
                build = new String(msg.getCustomData(), 40, 31);
                break;

            case MSG_ECM_CW:
                socket.close();
                throw new IOException();

            case MSG_ECM_NOK1:
            case MSG_ECM_NOK2:
                socket.close();

            case MSG_KEEPALIVE:
                socket.close();
                throw new IOException();

            case MSG_CARD_DEL:
                break;

            case MSG_CARD_ADD:
                if ((data[20] & 0xff) > 0) {
                    CcCardData card = new CcCardData();
                    card.setShareId(Arrays.copyOf(data, 4));
                    card.setUpHops((data[10] & 0xff) + 1);
                    card.setNodeId(Arrays.copyOfRange(data, 22 + data[20] * 7, 8 + 22 + data[20] * 7));
                    card.setCaId(((data[8] & 0xff) << 8) | (data[9]) & 0xff);
                    card.setNumberOfProviders(data[20] & 0xff);
                    card.setSerial(Arrays.copyOfRange(data, 12, 12 + 8));

                    int[] providers = new int[data[20]];
                    for (int i = 0; i < data[20]; i++) {
                        int provid = ((data[21 + i * 7] & 0xff) << 16) | ((data[22 + i * 7] & 0xff) << 8) | (data[23 + i * 7] & 0xff);
                        providers[i] = provid;
                    }
                    card.setProviders(providers);
                    cardMap.put(card.getShareId(), card);
                }
                break;
            default:
                socket.close();
                throw new IOException();
        }
    }

    public class CCCamMsg {

        byte[] raw;
        byte[] fixedData;
        byte[] customData;
        int commandTag;
        int dataLength;

        public CCCamMsg(byte[] raw) {
            this.raw = raw;
        }

        public byte[] getRaw() {
            return raw;
        }

        public byte[] getFixedData() {
            return fixedData;
        }

        public byte[] getCustomData() {
            return customData;
        }

        public int getCommandTag() {
            return commandTag;
        }

        public int getDataLength() {
            return dataLength;
        }

    }

    public CCCamMsg parseCCcam(byte[] raw) {
        try {
            CCCamMsg msg = new CCCamMsg(raw);
            msg.commandTag = raw[1] & 0xff;
            // Header
            msg.fixedData = new byte[4];
            System.arraycopy(raw, 0, msg.fixedData, 0, 4);
            // Data
            msg.dataLength = (raw[2] & 0x0F) * 256 + (raw[3] & 0xFF);
            msg.customData = new byte[msg.dataLength];

            System.arraycopy(raw, 4, msg.customData, 0, msg.dataLength);
            return msg;
        } catch (Exception e) {
            return null;
        }

    }

    public List<CcCardData> TestCline() throws Exception {
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

            SendMsg(20, sha1hash);//send crypted hash to server

            byte[] userBuf = new byte[20];
            System.arraycopy(user.getBytes(), 0, userBuf, 0, user.length());
            SendMsg(20, userBuf);//send username to server

            byte[] pwd = new byte[password.length()];
            System.arraycopy(password.getBytes(), 0, pwd, 0, password.length());
            sendblock.cc_encrypt(pwd, password.length()); //encript the password

            byte[] CCcam = {'C', 'C', 'c', 'a', 'm', 0};
            SendMsg(6, CCcam); //But send CCcam\0

            byte[] rcvBuf = new byte[20];
            is.read(rcvBuf);
            recvblock.cc_decrypt(rcvBuf, 20);
            //check if received string after decription equals "CCcam"
            if (Arrays.equals(CCcam, Arrays.copyOf(rcvBuf, 6))) {
                //CCLine is correct!!!!
                cc_send_client_info();
                while (true) {
                    try {
                        byte[] readMessage = readMessage();
                        if (readMessage != null) {
                            CCCamMsg parseCCcam = parseCCcam(readMessage);
                            if (parseCCcam != null) {
                                decodeMesg(parseCCcam);
                            }
                        }
                    } catch (IOException iOException) {
                        return new ArrayList<>(cardMap.values());
                    }
                }

            }
        } catch (Exception e) {
        }
        socket.close();
        is.close();
        os.close();
        return new ArrayList<>();
    }
}