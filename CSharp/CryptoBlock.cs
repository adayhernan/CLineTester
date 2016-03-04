namespace ConsoleApplication
{
    public class CryptoBlock
    {
        byte[] keytable;
        byte state;
        byte counter;
        byte sum;

        public CryptoBlock()
        {
            keytable = new byte[256];
            counter = 0;
            sum = 0;
        }

        public void cc_crypt_init(byte[] key, int len)
        {
            for (var i = 0; i < 256; i++) keytable[i] = (byte)i;

            byte j = 0;
            for (var i = 0; i < 256; i++)
            {
                j += (byte)(key[i % len] + keytable[i]);

                // Swap
                var k = keytable[i];
                keytable[i] = keytable[j];
                keytable[j] = k;
            }

            state = key[0];
            counter = 0;
            sum = 0;
        }

        public static void cc_crypt_xor(byte[] data)
        {
            var cccam = "CCcam";
            for (byte i = 0; i < 8; i++)
            {
                data[8 + i] = (byte)(i * data[i]);
                if (i < 5) data[i] ^= (byte)cccam[i];
            }
        }

        public void cc_encrypt(byte[] data, int len)
        {
            for (int i = 0; i < len; i++)
            {
                counter++;
                sum += keytable[counter];

                //Swap
                var k = keytable[counter];
                keytable[counter] = keytable[sum];
                keytable[sum] = k;

                var z = data[i];
                data[i] = (byte)(z ^ keytable[keytable[counter] + keytable[sum] & 0xFF] ^ state);
                state = (byte) (state ^ z);
            }
        }

        public void cc_decrypt(byte[] data, int len)
        {
            for (int i = 0; i < len; i++)
            {
                counter++;
                sum += keytable[counter];

                //Swap
                var k = keytable[counter];
                keytable[counter] = keytable[sum];
                keytable[sum] = k;

                var z = data[i];
                data[i] = (byte)(z ^ keytable[keytable[counter] + keytable[sum] & 0xFF] ^ state);
                z = data[i];
                state = (byte) (state ^ z);
            }
        }
    }
}
