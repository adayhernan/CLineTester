//Created by Dagger -- https://github.com/gavazquez
//With the help of ArSi -- https://github.com/arsi-apli

using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net.Sockets;
using System.Security.Cryptography;
using System.Text;

namespace ConsoleApplication
{
    public class Program
    {
        public static List<CcCardData> Cards { get; } = new List<CcCardData>();

        private static bool retrieveCardInfo = true;
        private static string server = "server.com";
        private static int port = 6666;
        private static string username = "user";
        private static string password = "pass";

        private static readonly CryptoBlock ReceiveBlock = new CryptoBlock();
        private static readonly CryptoBlock SendBlock = new CryptoBlock();
        private static readonly Socket Socket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);

        static void Main(string[] args)
        {
            try
            {
                Socket.SendTimeout = 500;
                Socket.ReceiveTimeout = 500;

                Socket.Connect(server, port);

                var helloBytes = new byte[16];
                Socket.Receive(helloBytes);
                Console.WriteLine("Received hello: " + Encoding.UTF8.GetString(helloBytes));

                if (!CheckConnectionChecksum(helloBytes, 16))
                {
                    throw new Exception("Wrong connection Checksum");
                }

                CryptoBlock.cc_crypt_xor(helloBytes);

                SHA1 sha = new SHA1Managed();
                var sha1Hash = sha.ComputeHash(helloBytes);

                //Init receive cripto block
                ReceiveBlock.cc_crypt_init(sha1Hash, 20);
                ReceiveBlock.cc_decrypt(helloBytes, 16);

                //Init send cripto block
                SendBlock.cc_crypt_init(helloBytes, 16);
                SendBlock.cc_decrypt(sha1Hash, 20);

                SendMessage(MsgTypes.MsgNoHeader, 20, sha1Hash); //Handshake

                byte[] userName = new byte[20];
                Array.Copy(GetBytes(username), userName, GetBytes(username).Length);
                SendMessage(MsgTypes.MsgNoHeader, 20, userName); //Send username in a padded array of 20 bytes

                byte[] pwd = new byte[password.Length];
                Array.Copy(GetBytes(password), pwd, GetBytes(password).Length);
                SendBlock.cc_encrypt(pwd, pwd.Length); //encript psw in cripto block

                byte[] cCcam = { Convert.ToByte('C'), Convert.ToByte('C'), Convert.ToByte('c'),
                    Convert.ToByte('a'), Convert.ToByte('m'), 0 };
                SendMessage(MsgTypes.MsgNoHeader, 6, cCcam); //Send "CCcam" with password encripted block

                try
                {
                    byte[] receiveBytes = new byte[20];
                    var recvCount = Socket.Receive(receiveBytes);

                    if (recvCount > 0)
                    {
                        ReceiveBlock.cc_decrypt(receiveBytes, 20);

                        var valid = Encoding.Default.GetString(receiveBytes).Replace("\0", "") == "CCcam";
                        Console.WriteLine(valid ? "Connection SUCCESS!" : "Wrong ACK received!");
                        if (valid && retrieveCardInfo)
                        {
                            SendClientInfo();

                            if (Socket.Connected)
                                ReadCardsInformation();
                        }
                    }
                    else
                    {
                        Console.WriteLine("Wrong username/password");
                    }
                }
                catch (Exception)
                {
                    Console.WriteLine("Wrong username/password");
                }
            }
            catch (Exception)
            {
                Console.WriteLine("Failed to connect");
            }
            Socket.Close();
        }

        private static void ReadCardsInformation()
        {
            while (true)
            {
                try
                {
                    var msgBytes = ReadMessage();
                    if (msgBytes != null)
                    {
                        var msg = new CcamMsg(msgBytes);
                        DecodeMessage(msg);
                    }
                }
                catch (Exception)
                {
                    return;
                }
            }
        }

        private static bool CheckConnectionChecksum(byte[] buf, int len)
        {
            if (len == 16)
            {
                byte sum1 = (byte)(buf[0] + buf[4] + buf[8]);
                byte sum2 = (byte)(buf[1] + buf[5] + buf[9]);
                byte sum3 = (byte)(buf[2] + buf[6] + buf[10]);
                byte sum4 = (byte)(buf[3] + buf[7] + buf[11]);

                var valid = (sum1 == buf[12]) && (sum2 == buf[13]) && (sum3 == buf[14]) && (sum4 == buf[15]);
                return valid;
            }
            return false;
        }
        private static byte[] GetBytes(string str)
        {
            byte[] bytes = new byte[str.Length * sizeof(char)];
            Buffer.BlockCopy(str.ToCharArray(), 0, bytes, 0, bytes.Length);
            return Encoding.ASCII.GetBytes(str);
        }
        
        #region Message sending

        public static void SendClientInfo()
        {
            var cliInfo = new byte[20 + 8 + 1 + 64];

            Array.Copy(GetBytes(username), cliInfo, GetBytes(username).Length); //20
            Array.Copy(CcamClientInfo.Nodeid, 0, cliInfo, 20, CcamClientInfo.Nodeid.Length); //8
            cliInfo[28] = 0;

            Array.Copy(GetBytes(CcamClientInfo.Version), 0, cliInfo, 29, GetBytes(CcamClientInfo.Version).Length); // 32
            Array.Copy(GetBytes(CcamClientInfo.Build), 0, cliInfo, 61, GetBytes(CcamClientInfo.Build).Length); // 32

            SendMessage(MsgTypes.MsgCliInfo, cliInfo);
        }

        public static int SendMessage(MsgTypes cmd, byte[] buf)
        {
            return SendMessage(cmd, buf.Length, buf);
        }

        public static int SendMessage(MsgTypes cmd, int len, byte[] data)
        {
            byte[] sendData;
            if (cmd == MsgTypes.MsgNoHeader)
            {
                sendData = new byte[len];
                Array.Copy(data, sendData, len);
            }
            else
            {
                // build command message
                sendData = new byte[4 + len];
                sendData[0] = 0;   // flags??
                sendData[1] = (byte)((int)cmd & 0xff);
                sendData[2] = (byte)(len >> 8);
                sendData[3] = (byte)(len & 0xff);
                if (len > 0)
                {
                    Array.Copy(data, 0, sendData, 4, len);
                }
            }

            SendBlock.cc_encrypt(sendData, len);

            try
            {
                return Socket.Send(sendData);
            }
            catch (Exception)
            {
                Console.WriteLine("Connection closed while sending");
                Socket.Close();
            }

            return -1;
        }

        #endregion

        #region Message receiveing

        public static byte[] ReadMessage()
        {
            if (Socket == null)
            {
                throw new IOException();
            }
            
            var header = new byte[4];
            Socket.Receive(header);
            ReceiveBlock.cc_decrypt(header, 4);
            var dataLength = (((header[2] & 0xe7) * 256) + header[3] & 0xff);
            if (dataLength == 0 || dataLength > (1024 - 2))
                return null;

            var data = new byte[dataLength];
            Socket.Receive(data);
            ReceiveBlock.cc_decrypt(data, dataLength);

            var fullData = new byte[4 + dataLength];
            Array.Copy(header, 0, fullData, 0, 4);
            Array.Copy(data, 0, fullData, 4, dataLength);

            return fullData;
        }

        public static void DecodeMessage(CcamMsg msg)
        {
            switch (msg.CommandTag)
            {
                case MsgTypes.MsgCardDel:
                case MsgTypes.MsgCliInfo:
                    break;
                case MsgTypes.MsgCardAdd:
                    if ((msg.CustomData[20] & 0xff) > 0)
                    {
                        var card = new CcCardData
                        {
                            RemoteId = string.Join("", msg.CustomData.Take(4).Select(s => s.ToString("X"))),
                            Uphops = (msg.CustomData[10] & 0xff) + 1,
                            NodeId = msg.CustomData.Skip(22 + msg.CustomData[20] * 7).Take(8 + 22 + msg.CustomData[20] * 7).ToArray(),
                            CaId = (((msg.CustomData[8]) << 8) | (msg.CustomData[9])).ToString("X"),
                            ProviderCount = msg.CustomData[20] & 0xff,
                            Serial = msg.CustomData.Skip(12).Take(12 + 8).ToArray()
                        };

                        var providers = new int[msg.CustomData[20]];
                        for (var i = 0; i < msg.CustomData[20]; i++)
                        {
                            var provid = ((msg.CustomData[21 + i * 7] & 0xff) << 16) | 
                                ((msg.CustomData[22 + i * 7] & 0xff) << 8) | 
                                (msg.CustomData[23 + i * 7] & 0xff);

                            providers[i] = provid;
                        }
                        card.Providers = providers.Select(p => p.ToString("X")).ToArray();
                        Console.WriteLine($"Card: {card}");
                        Cards.Add(card);
                    }
                    break;
                case MsgTypes.MsgSrvInfo:

                    CcamServerInfo.NodeId = string.Join("", msg.CustomData.Take(8).Select(n => n.ToString("X")));
                    CcamServerInfo.Version = Encoding.Default.GetString(msg.CustomData.Skip(8).Take(31).ToArray());
                    CcamServerInfo.Build = Encoding.Default.GetString(msg.CustomData.Skip(40).Take(31).ToArray());

                    Console.WriteLine($"Server information: NodeId: {CcamServerInfo.NodeId} " +
                                      $"Version:{CcamServerInfo.Version.Replace("\0", "").Trim()} " +
                                      $"Build: {CcamServerInfo.Build.Trim()}");
                    break;
                case MsgTypes.MsgEcmNok1:
                case MsgTypes.MsgEcmNok2:
                    Socket.Close();
                    break;
                default:
                    Socket.Close();
                    throw new IOException();
            }
        }

        #endregion
    }
}
