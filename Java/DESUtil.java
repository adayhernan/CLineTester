//Created by ArSi -- https://github.com/arsi-apli

package org.cline;

public class DESUtil {

    public static byte[] getRandomBytes(int len) {
        byte[] random = new byte[len];
        for (int i = 0; i < len; i++) {
            random[i] = (byte) (Math.random() * 256.0);
        }
        return random;
    }

    public static String bytesToString(byte[] bytes, int offs, int len) {
        StringBuffer sb = new StringBuffer();
        String bt;
        if (len > bytes.length) {
            len = bytes.length;
        }
        for (int i = 0; i < len && (i + offs < bytes.length); i++) {
            bt = Integer.toHexString(bytes[offs + i] & 0xFF);
            if (bt.length() == 1) {
                sb.append('0');
            }
            sb.append(bt);
            sb.append(' ');
        }
        return sb.toString().trim().toUpperCase();
    }

    public static String intToHexString(int i, int digits) {
        String s = Integer.toHexString(i);
        while (s.length() < digits) {
            s = "0" + s;
        }
        return s;
    }
}
