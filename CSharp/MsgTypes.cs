//Created by Dagger -- https://github.com/DaggerES
//With the help of ArSi -- https://github.com/arsi-apli

namespace ConsoleApplication
{
    public enum MsgTypes
    {
        MsgCliInfo = 0,
        MsgEcmCw = 1,
        MsgEmmAck = 2,
        MsgCardDel = 4,
        MsgCmd05 = 5,
        MsgKeepalive = 6,
        MsgCardAdd = 7,
        MsgSrvInfo = 8,
        MsgCmd_0A = 10,
        MsgCmd_0B = 11,
        MsgCmd_0C = 12,
        MsgCmd_0D = 13,
        MsgCmd_0E = 14,
        MsgNewCardSidinfo = 15,
        MsgEcmNok1 = 254,
        MsgEcmNok2 = 255,
        MsgSleepsend = 128,
        MsgCachePush = 129,
        MsgNoHeader = 65535
    }
}
