// $Id: Base64.java 13160 2010-04-13 12:49:33Z kianusch $

package com.agfa.db.tools;

class Base64 {
   private static final String base64encode = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "abcdefghijklmnopqrstuvwxyz" + "0123456789"
   + "+/";

   private static final byte[] base64decode = new byte[128];
   static {
       for (int i = 0; i < base64decode.length; i++)
           base64decode[i] = -1;
       for (int i = 0; i < 64; i++)
           base64decode[base64encode.charAt(i)] = (byte) i;
   }

   public static byte[] Decode(String s) {
       return Decode(s.toCharArray());
   }

   public static byte[] Decode(char[] in) {
       return Decode(in, 0, in.length);
   }

   public static byte[] Decode(char[] in, int iOff, int iLen) {
       if (iLen % 4 != 0)
           throw new IllegalArgumentException("Length of Base64 encoded input string is not a multiple of 4.");
       while (iLen > 0 && in[iOff + iLen - 1] == '=')
           iLen--;
       int oLen = (iLen * 3) / 4;
       byte[] out = new byte[oLen];
       int ip = iOff;
       int iEnd = iOff + iLen;
       int op = 0;
       while (ip < iEnd) {
           int i0 = in[ip++];
           int i1 = in[ip++];
           int i2 = ip < iEnd ? in[ip++] : 'A';
           int i3 = ip < iEnd ? in[ip++] : 'A';
           if (i0 > 127 || i1 > 127 || i2 > 127 || i3 > 127)
               throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
           int b0 = base64decode[i0];
           int b1 = base64decode[i1];
           int b2 = base64decode[i2];
           int b3 = base64decode[i3];
           if (b0 < 0 || b1 < 0 || b2 < 0 || b3 < 0)
               throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
           int o0 = (b0 << 2) | (b1 >>> 4);
           int o1 = ((b1 & 0xf) << 4) | (b2 >>> 2);
           int o2 = ((b2 & 3) << 6) | b3;
           out[op++] = (byte) o0;
           if (op < oLen)
               out[op++] = (byte) o1;
           if (op < oLen)
               out[op++] = (byte) o2;
       }
       return out;
   }

   public static String Encode(String string) {
       String encoded = "";
       int paddingCount = (3 - (string.length() % 3)) % 3;
       string += "\0\0".substring(0, paddingCount);

       for (int i = 0; i < string.length(); i += 3) {
           int j = (string.charAt(i) << 16) + (string.charAt(i + 1) << 8) + string.charAt(i + 2);

           encoded = encoded + base64encode.charAt((j >> 18) & 0x3f) + base64encode.charAt((j >> 12) & 0x3f)
           + base64encode.charAt((j >> 6) & 0x3f) + base64encode.charAt(j & 0x3f);
       }

       return encoded.substring(0, encoded.length() - paddingCount) + "==".substring(0, paddingCount);
   }
}