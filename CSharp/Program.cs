//Created by Dagger -- https://github.com/DaggerES

using System;
using System.IO;
using System.Net.Sockets;
using System.Security.Cryptography;
using System.Text;

namespace ConsoleApplication
{
    public class Program
    {
        private static readonly CryptoBlock ReceiveBlock = new CryptoBlock();
        private static readonly CryptoBlock SendBlock = new CryptoBlock();
        private static readonly Socket Socket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);

        static void Main(string[] args)
        {
            var server = "srv.server.com";
            var port = 9999;
            var username = "user";
            var password = "pass";
            try
            {
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

                SendMsg(20, sha1Hash); //Handshake

                byte[] userName = new byte[20];
                Array.Copy(GetBytes(username), userName, GetBytes(username).Length);
                SendMsg(20, userName); //Send username in a padded array of 20 bytes

                byte[] pwd = new byte[password.Length];
                Array.Copy(GetBytes(password), pwd, GetBytes(password).Length);
                SendBlock.cc_encrypt(pwd, pwd.Length); //encript psw in cripto block

                byte[] cCcam = { Convert.ToByte('C'), Convert.ToByte('C'), Convert.ToByte('c'),
                    Convert.ToByte('a'), Convert.ToByte('m'), 0 };
                SendMsg(6, cCcam); //Send "CCcam" with password encripted block

                try
                {
                    byte[] receiveBytes = new byte[20];
                    var recvCount = Socket.Receive(receiveBytes);

                    if (recvCount > 0)
                    {
                        ReceiveBlock.cc_decrypt(receiveBytes, 20);

                        Console.WriteLine(Encoding.Default.GetString(receiveBytes).Replace("\0", "") == "CCcam"
                            ? "SUCCESS!"
                            : "Wrong ACK received!");
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
        
        private static bool CheckConnectionChecksum(byte[] buf, int len)
        {
            if (len == 16)
            {
                byte sum1 = (byte)(buf[0] + buf[4] +buf[8]);
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

        private static void SendMsg(int len, byte[] data)
        {
            SendBlock.cc_encrypt(data, len);

            try
            {
                Socket.Send(data);
            }
            catch (IOException e)
            {
                Console.WriteLine("Connection closed while sending");
                Socket.Close();
            }
        }
    }
}
