//Created by ArSi -- https://github.com/arsi-apli

package org.cline;

public class CcData {
    // Local Info

    public static byte[] nodeid = {1, 2, 3, 4, 5, 6, 7, 8};
    public static String version = "2.0.11";
    public static String build = "2892";

    public byte[] getNodeId() {
        return nodeid;
    }

    public static void SetNodeId() {
        System.arraycopy(DESUtil.getRandomBytes(8), 0, nodeid, 0, 8);
    }

}
