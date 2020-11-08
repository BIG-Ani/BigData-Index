package com.neu.info7255.bigdata_proj.util;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MessageDigestGenerator {

    private static final char[] hexArr = "0123456789abcdef".toCharArray();

    public static String MD5_ALGORITHM = "MD5";
    private static MessageDigest messageDigest = null;

    public static String md5Gen(String sources) {
        String md5Hex = DigestUtils.md5Hex(sources).toUpperCase();

        return md5Hex;
    }

    public static String getSequence(String type, String source) {
        String encodeSeq = "";

        try {
            messageDigest = MessageDigest.getInstance(type);
            byte[] input = source.getBytes("UTF-8");
            encodeSeq = buffer2Hex(input);
            return encodeSeq;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return encodeSeq;
    }

    public static String buffer2Hex(byte[] data) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int d = data[i];
            if (d < 0) {
                d += 256;
            }
            int d1 = d / 16;
            int d2 = d % 16;
            sb.append(hexArr[d1] + hexArr[d2]);
        }
        return sb.toString();
    }
}
