package com.ezenit.utils.iab;

public class KeyHelper {

    /**
     * Encrypt a string
     *
     * @param s
     *            The string to encrypt
     * @param key
     *            The key to seed the encryption
     * @return The encrypted string
     */
    public static String encode(String s, String key) {
        return base64Encode(xorWithKey(s.getBytes(), key.getBytes()));
    }

    /**
     * Decrypt a string
     *
     * @param s
     *            The string to decrypt
     * @param key
     *            The key used to encrypt the string
     * @return The unencrypted string
     */
    public static String decode(String s, String key) {
        return new String(xorWithKey(base64Decode(s), key.getBytes()));
    }

    private static byte[] xorWithKey(byte[] a, byte[] key) {
        byte[] out = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            out[i] = (byte) (a[i] ^ key[i % key.length]);
        }
        return out;
    }

    private static byte[] base64Decode(String s) {
        try {
            return Base64.decode(s);
        } catch (Base64DecoderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;
    }

    private static String base64Encode(byte[] bytes) {
    	return Base64.encode(bytes).replaceAll("\\s", "");
    }
}