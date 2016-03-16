//Created by Dagger -- https://github.com/DaggerES

namespace ConsoleApplication
{
    public class CryptoBlock
    {
        private readonly int[] _keytable;
        private int _state;
        private int _counter;
        private int _sum;

        public CryptoBlock()
        {
            _keytable = new int[256];
            _state = 0;
            _counter = 0;
            _sum = 0;
        }

        public void cc_crypt_init(byte[] key, int len)
        {
            int i;
            for (i = 0; i < 256; i++) _keytable[i] = i;
            int j = 0;
            for (i = 0; i < 256; i++)
            {
                j = 0xff & (j + key[i % len] + _keytable[i]);
                // Swap
                int k = _keytable[i];
                _keytable[i] = _keytable[j];
                _keytable[j] = k;
            }

            _state = key[0];
            _counter = 0;
            _sum = 0;
        }

        public static void cc_crypt_xor(byte[] data)
        {
            string cccam = "CCcam";
            for (sbyte i = 0; i < 8; i++)
            {
                data[8 + i] = (byte)(i * data[i]);
                if (i < 5) data[i] ^= (byte)cccam[i];
            }
        }

        public void cc_encrypt(byte[] data, int len)
        {
            for (int i = 0; i < len; i++)
            {
                _counter = 0xff & (_counter + 1);
                _sum += _keytable[_counter];

                sbyte k = (sbyte)_keytable[_counter];
                _keytable[_counter] = _keytable[_sum & 0xFF];
                _keytable[_sum & 0xFF] = k;

                sbyte z = (sbyte)data[i];
                data[i] = (byte)(z ^ _keytable[_keytable[_counter & 0xFF] + _keytable[_sum & 0xFF] & 0xFF] ^ _state);
                _state = 0xff & (_state ^ z);
            }
        }

        public void cc_decrypt(byte[] data, int len)
        {
            for (int i = 0; i < len; i++)
            {
                _counter = 0xff & (_counter + 1);
                _sum += _keytable[_counter];

                sbyte k = (sbyte)_keytable[_counter];
                _keytable[_counter] = _keytable[_sum & 0xFF];
                _keytable[_sum & 0xFF] = k;

                sbyte z = (sbyte)data[i];
                data[i] = (byte)(z ^ _keytable[_keytable[_counter] + _keytable[_sum & 0xFF] & 0xFF] ^ _state);
                z = (sbyte)data[i];
                _state = 0xff & (_state ^ z);
            }
        }
    }
}


