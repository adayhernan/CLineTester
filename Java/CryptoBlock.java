//Created by Dagger -- https://github.com/gavazquez
//With the help of ArSi -- https://github.com/arsi-apli

package org.cline;

public class CryptoBlock {

	int[] keytable;
	int state;
	int counter;
	int sum;

	public CryptoBlock() {
		keytable = new int[256];
		state = 0;
		counter = 0;
		sum = 0;
	}

	public void cc_crypt_init( byte[] key, int len )
	{
		int i;
		for (i=0; i<256; i++) keytable[i] = i;
		int j = 0;
		for (i=0; i<256; i++) {
			j = 0xff & ( j + key[i % len] + keytable[i] );
			// Swap
			int k = keytable[i];
			keytable[i] = keytable[j];
			keytable[j] = k;
		}

		state = key[0];
		counter = 0;
		sum = 0;
	}

	static void cc_crypt_xor(byte[] data)
	{
		String cccam = "CCcam";
		for ( byte i=0; i < 8; i++ ) {
			data[8 + i] = (byte) (i * data[i]);
			if ( i < 5 ) data[i] ^= cccam.charAt(i);
		}
	}

	public void cc_encrypt( byte[] data, int len ) {
		for (int i=0; i<len; i++) {
			counter = 0xff & (counter+1);
			sum += keytable[counter];

			byte k = (byte) keytable[counter];
			keytable[counter] = keytable[sum & 0xFF];
			keytable[sum & 0xFF] = k;

			byte z = data[i];
			data[i] = (byte) (z ^ keytable[ keytable[counter & 0xFF] + keytable[sum & 0xFF] & 0xFF ] ^ state);
			state = 0xff & (state ^ z);
		}
	}

	public void cc_decrypt( byte[] data, int len ) {
		for (int i=0; i<len; i++) {
			counter = 0xff & (counter+1);
			sum += keytable[counter];

			byte k = (byte) keytable[counter];
			keytable[counter] = keytable[sum & 0xFF];
			keytable[sum & 0xFF] = k;

			byte z = data[i];
			data[i] = (byte) (z ^ keytable[ keytable[counter] + keytable[sum & 0xFF] & 0xFF ] ^ state);
			z = data[i];
			state = 0xff & (state ^ z);
		}
	}
}
