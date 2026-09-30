package com.kanhe;

import java.security.SecureRandom;
import java.util.random.RandomGenerator;

public final class PasswordCodes {
    public static final int DEFAULT_LENGTH = 6;
    public static final int MIN_LENGTH = 4;
    public static final int MAX_LENGTH = 32;

    private static final RandomGenerator RANDOM = new SecureRandom();

    /** 把长度夹进允许范围。 */
    public static int clampLength(int length) {
        return Math.max(MIN_LENGTH, Math.min(MAX_LENGTH, length));
    }

    /** 生成长度为 length 的随机数字密码（长度会被夹进允许范围）。 */
    public static String generate(int length) {
        int size = clampLength(length);
        StringBuilder code = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            code.append((char) ('0' + RANDOM.nextInt(10)));
        }
        return code.toString();
    }

    /** 长度是否在允许范围内。 */
    public static boolean isValidLength(int length) {
        return length >= MIN_LENGTH && length <= MAX_LENGTH;
    }

    /** 密码是否合法：长度在允许范围内，且全是数字。 */
    public static boolean isValid(String code) {
        if (code == null) {
            return false;
        }
        int length = code.length();
        if (!isValidLength(length)) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            char c = code.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    private PasswordCodes() {
    }
}
