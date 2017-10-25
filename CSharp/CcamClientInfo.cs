//Created by Dagger -- https://github.com/gavazquez
//With the help of ArSi -- https://github.com/arsi-apli
using System;

namespace ConsoleApplication
{
    public class CcamClientInfo
    {
        public static byte[] Nodeid = new byte[8];
        public static string Version = "2.0.11";
        public static string Build = "2892";

        static CcamClientInfo()
        {
            new Random().NextBytes(Nodeid);
        }
    }
}
