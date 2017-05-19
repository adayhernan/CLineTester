//Created by Dagger -- https://github.com/DaggerES
//With the help of ArSi -- https://github.com/arsi-apli
using System;

namespace ConsoleApplication
{
    public class CcamMsg
    {
        public byte[] Raw { get; set; }
        public byte[] FixedData { get; set; }
        public byte[] CustomData { get; set; }
        public MsgTypes CommandTag { get; set; }
        public int DataLength { get; set; }

        public CcamMsg(byte[] raw)
        {
            Raw = raw;
            CommandTag = (MsgTypes)(raw[1] & 0xff);

            // Header
            FixedData = new byte[4];
            Array.Copy(raw, 0, FixedData, 0, 4);

            // Data
            DataLength = (raw[2] & 0x0F) * 256 + (raw[3] & 0xFF);
            CustomData = new byte[DataLength];

            Array.Copy(raw, 4, CustomData, 0, DataLength);
        }
    }
}
