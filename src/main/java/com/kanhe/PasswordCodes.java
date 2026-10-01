package com.kanhe;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.random.RandomGenerator;

public final class PasswordCodes {
    /** 随机密码的形态。 */
    public enum Mode {
        DIGITS("0123456789"),
        LETTERS("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"),
        MIXED("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");

        private final String alphabet;

        Mode(String alphabet) {
            this.alphabet = alphabet;
        }

        public String alphabet() {
            return alphabet;
        }

        /** 配置 / 指令里使用的名字：digits / letters / mixed。 */
        public String serializedName() {
            return name().toLowerCase(Locale.ROOT);
        }

        /** @return 对应的形态，名字不认识时返回 null。 */
        public static Mode byName(String name) {
            if (name == null) {
                return null;
            }
            for (Mode mode : values()) {
                if (mode.serializedName().equalsIgnoreCase(name.trim())) {
                    return mode;
                }
            }
            return null;
        }
    }

    public static final Mode DEFAULT_MODE = Mode.DIGITS;
    public static final int DEFAULT_LENGTH = 6;
    public static final int MIN_LENGTH = 4;
    public static final int MAX_LENGTH = 32;
    /** 手动指定密码时的长度上限（可见 ASCII 字符）。 */
    public static final int MAX_SET_LENGTH = 64;

    private static final RandomGenerator RANDOM = new SecureRandom();

    /** 把随机密码长度夹进允许范围。 */
    public static int clampLength(int length) {
        return Math.max(MIN_LENGTH, Math.min(MAX_LENGTH, length));
    }

    /** 随机密码长度是否在允许范围内。 */
    public static boolean isValidLength(int length) {
        return length >= MIN_LENGTH && length <= MAX_LENGTH;
    }

    /** 按形态和长度生成随机密码（长度会被夹进允许范围）。 */
    public static String generate(Mode mode, int length) {
        String alphabet = (mode == null ? DEFAULT_MODE : mode).alphabet();
        int size = clampLength(length);
        StringBuilder code = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            code.append(alphabet.charAt(RANDOM.nextInt(alphabet.length())));
        }
        return code.toString();
    }

    /** 手动指定的密码是否合法：1~64 个可见 ASCII 字符（不含空格与控制字符）。 */
    public static boolean isValidPassword(String code) {
        if (code == null) {
            return false;
        }
        int length = code.length();
        if (length < 1 || length > MAX_SET_LENGTH) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            char c = code.charAt(i);
            if (c < '!' || c > '~') {
                return false;
            }
        }
        return true;
    }

    private PasswordCodes() {
    }
}
