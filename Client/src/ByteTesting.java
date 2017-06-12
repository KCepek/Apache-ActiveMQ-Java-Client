/**
 * This class demonstrates the structure of byte arrays used via Apache ActiveMQ
 * and how to decode ActiveMQ's byte arrays. ActiveMQ's byte arrays are encoded
 * in a big-endian byte order, so the most significant byte is stored first.
 * There is also a number in front of each byte sequence to represent the type
 * of values sent.
 * 
 * When sending characters in Java, UTF-16 is used, so a char in a byte array
 * will be represented by two bytes. In C++, characters use UTF-8, so a char in
 * a byte array will be represented by one byte. Wide characters (wchar_t) in
 * C++ will either be kept as a single byte if possible or throw an error C++'s
 * writeChar for StreamMessage, so if a program is intended to communicate with
 * interoperability, then send a character by another means, such as using
 * writeString and converting the character to a String.
 * 
 * boolean -> 1 followed by 1-byte representation
 * byte    -> 2 followed by 1-byte representation
 * char    -> 3 followed by 2-byte representation for Java and 1-byte representation for C++
 * short   -> 4 followed by 2-byte representation
 * int     -> 5 followed by 4-byte representation
 * long    -> 6 followed by 8-byte representation
 * double  -> 7 followed by 8-byte representation
 * float   -> 8 followed by 4-byte representation
 * 
 * String  -> 9, 0 followed by 1-byte representation for each char in the String
 * 
 * @author Kyle Cepek
 * @version 06-08-2017
 */
public class ByteTesting {
	public static void main(String[] args) {
		// BYTE
		// i.e. {9, 1, 3} -> {2, 9, 2, 1, 2, 3}

		// SHORT
		// i.e. {1, -32768, 32767} -> {4, 0, 1, 4, -128, 0, 4, 127, -1}
		byte[] shortBytes = new byte[] { -128, 0 };
		System.out.println("These two bytes have a short value of:     " + bytesToShort2(shortBytes, 0));

		// INT
		// i.e. {9, 1, 2147483647} -> {5, 0, 0, 0, 9, 5, 0, 0, 0, 1, 5, 127, -1, -1, -1}
		byte[] intBytes = new byte[] { 127, -1, -1, -1 };
		System.out.println("These four bytes have an int value of:     " + bytesToInt4(intBytes, 0));

		// LONG
		// i.e. {1, 9223372036854775807L} -> {6, 0, 0, 0, 0, 0, 0, 0, 1, 6, 127, -1, -1, -1, -1, -1, -1, -1}
		byte[] longBytes = new byte[] { 127, -1, -1, -1, -1, -1, -1, -1 };
		System.out.println("These eight bytes have a long value of:    " + bytesToLong8(longBytes, 0));

		// CHAR
		// i.e. {'a', 'z', '5', 'a'} -> {3, 0, 97, 3, 0, 122, 3, 0, 53, 3, 0, 97}
		byte[] charBytes = new byte[] { 0, 97 };
		System.out.println("These two bytes have a char value of:      " + bytesToChar2(charBytes, 0));
		
		// FLOAT
		// i.e. {3.2, 4.5, 3.2} -> {8, 64, 76, -52, -51, 8, 64, -112, 0, 0, 8, 64, 76, -52, -51}
		byte[] floatBytes = new byte[] {64, 76, -52, -51};
		System.out.println("These four bytes have a float value of:    " + bytesToFloat4(floatBytes, 0));
		
		// DOUBLE
		// i.e. {123.456, 383} -> {7, 64, 94, -35, 47, 26, -97, -66, 119, 7, 64, 119, -16, 0, 0, 0, 0, 0}
		byte[] doubleBytes = new byte[] {64, 94, -35, 47, 26, -97, -66, 119};
		System.out.println("These eight bytes have a double value of:  " + bytesToDouble8(doubleBytes, 0));
		
		// BOOLEAN
		// i.e. {true, false, false} -> {1, 1, 1, 0, 1, 0}
		byte[] booleanBytes = new byte[] {1};
		System.out.println("This one byte has a boolean value of:      " + bytesToBoolean1(booleanBytes, 0));
		
	}

	/**
	 * Converts 2 bytes from a byte array to a short.
	 * 
	 * @param bytes
	 *            - a byte array.
	 * @param offset
	 *            - how much the first significant byte is offset in the byte
	 *            array if the length of the array is greater than 2.
	 * @return
	 */
	public static short bytesToShort2(byte[] bytes, int offset) {
		short ret = 0;
		int max = offset + 2;
		for (int i = offset; i < max && i < bytes.length; ++i) {
			ret <<= 8;
			ret |= (short) bytes[i] & 0xFF;
		}
		return ret;
	}

	/**
	 * Converts 4 bytes from a byte array to an int.
	 * 
	 * @param bytes
	 *            - a byte array.
	 * @param offset
	 *            - how much the first significant byte is offset in the byte
	 *            array if the length of the array is greater than 4.
	 * @return
	 */
	public static int bytesToInt4(byte[] bytes, int offset) {
		int ret = 0;
		int max = offset + 4;
		for (int i = offset; i < max && i < bytes.length; ++i) {
			ret <<= 8;
			ret |= (int) bytes[i] & 0xFF;
		}
		return ret;
	}

	/**
	 * Converts 4 bytes from a byte array to a long.
	 * 
	 * @param bytes
	 *            - a byte array.
	 * @param offset
	 *            - how much the first significant byte is offset in the byte
	 *            array if the length of the array is greater than 8.
	 * @return
	 */
	public static long bytesToLong8(byte[] bytes, int offset) {
		long ret = 0;
		int max = offset + 8;
		for (int i = offset; i < max && i < bytes.length; ++i) {
			ret <<= 8;
			ret |= (long) bytes[i] & 0xFF;
		}
		return ret;
	}

	/**
	 * Converts 2 bytes from a byte array to a char.
	 * 
	 * @param bytes
	 *            - a byte array.
	 * @param offset
	 *            - how much the first significant byte is offset in the byte
	 *            array if the length of the array is greater than 2.
	 * @return
	 */
	public static char bytesToChar2(byte[] bytes, int offset) {
		char ret = 0;
		int max = offset + 2;
		for (int i = offset; i < max && i < bytes.length; ++i) {
			ret <<= 8;
			ret |= (char) bytes[i] & 0xFF;
		}
		return ret;
	}
	
	/**
	 * Converts 4 bytes from a byte array to a float.
	 * 
	 * @param bytes
	 *            - a byte array.
	 * @param offset
	 *            - how much the first significant byte is offset in the byte
	 *            array if the length of the array is greater than 4.
	 * @return
	 */
	public static float bytesToFloat4(byte[] bytes, int offset) {
		int ret = 0;
		int max = offset + 4;
		for (int i = offset; i < max && i < bytes.length; ++i) {
			ret <<= 8;
			ret |= (int) bytes[i] & 0xFF;
		}
		return Float.intBitsToFloat(ret);
	}
	
	/**
	 * Converts 8 bytes from a byte array to a double.
	 * 
	 * @param bytes
	 *            - a byte array.
	 * @param offset
	 *            - how much the first significant byte is offset in the byte
	 *            array if the length of the array is greater than 8.
	 * @return
	 */
	public static double bytesToDouble8(byte[] bytes, int offset) {
		long ret = 0;
		int max = offset + 8;
		for (int i = offset; i < max && i < bytes.length; ++i) {
			ret <<= 8;
			ret |= (long) bytes[i] & 0xFF;
		}
		return Double.longBitsToDouble(ret);
	}
	
	/**
	 * Converts 1 byte from a byte array to a double.
	 * 
	 * @param bytes
	 *            - a byte array.
	 * @param offset
	 *            - how much the first significant byte is offset in the byte
	 *            array if the length of the array is greater than 1.
	 * @return
	 */
	public static boolean bytesToBoolean1(byte[] bytes, int offset) {
		boolean ret = false;
		int max = offset + 1;
		for (int i = offset; i < max && i < bytes.length; ++i) {
			if (bytes[i] == 1) {
				ret = true;
			}
		}
		return ret;
	}
}
