package urbantrees.spaklingscience.at.urbantrees.util;

import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Provides simple static utility methods for use
 * bytes and byte arrays.
 * @author Laurenz Fiala
 * @since 2018/05/13
 */
public class ByteUtils {

    /**
     * Converts given octal bytes into a single decimal value.
     * @param bytes array if bytes
     * @return The decimal-long representation of given byte array.
     */
    public static final long octalToDecimal(byte[] bytes)
    {

        long l = 0;
        for (int i = 0; i < bytes.length; i++) {
            l |= bytes[i] & 0xFF;
            if (i != bytes.length - 1) {
                l <<= 8;
            }
        }

        return l;

    }

    /**
     * Cuts off all unused bytes from the given array
     * and returns a new array without treiling zeros.
     * @param rawBytes any byte array
     * @return new byte array without trailing zeros
     */
    public static final byte[] trim(byte[] rawBytes) {

        int index = rawBytes.length;
        while (index > 0) {
            if (rawBytes[index-1] != 0) {
                break;
            }
            index--;
        }

        return Arrays.copyOfRange(rawBytes, 0, index);

    }

    /**
     * Converts the given byte array to a string.
     * A wrapper method for "new String(byteArray, charset)" in case this
     * is inconsistent etc.
     * @param bytes byte array
     * @param charset {@link Charset} to use for conversion
     * @return converted string
     */
    public static final String toString(byte[] bytes, Charset charset) {
        return new String(bytes, charset);
    }

}
