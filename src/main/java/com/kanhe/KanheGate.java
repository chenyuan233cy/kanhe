package com.kanhe;

import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.QueryProperties;

/**
 * 服务端状态：每个进服玩家都必须通过服务器地址里的 {@code _id} 参数带上这个数字密码，
 * 外加它背后持久化的配置文件。
 */
public final class KanheGate {
    private static final Object LOCK = new Object();
    private static final String CONFIG_NAME = "kanhe.json";

    private static KanheConfig config;
    private static Path configPath;

    public static KanheConfig config() {
        synchronized (LOCK) {
            if (config == null) {
                configPath = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_NAME);
                config = KanheConfig.load(configPath);
                logCode("Server password is");
            }
            return config;
        }
    }

    /** @return 带着这些地址参数的连接是否允许继续。 */
    public static boolean accepts(QueryProperties properties) {
        KanheConfig current = config();
        if (!current.enabled || !PasswordCodes.isValid(current.code)) {
            return true;
        }
        return current.code.equals(properties.get(Kanhe.PROPERTY_ID));
    }

    public static boolean isEnabled() {
        return config().enabled;
    }

    public static String code() {
        return config().code;
    }

    public static int length() {
        return config().length;
    }

    /** 按当前位数重新生成一个随机密码。 */
    public static String regenerate() {
        synchronized (LOCK) {
            KanheConfig current = config();
            current.code = PasswordCodes.generate(current.length);
            current.enabled = true;
            current.save(configPath);
            logCode("Server password reset to");
            return current.code;
        }
    }

    /** 手动指定密码，位数随之改成该密码的位数。 */
    public static void setCode(String code) {
        synchronized (LOCK) {
            KanheConfig current = config();
            current.code = code;
            current.length = code.length();
            current.save(configPath);
            logCode("Server password set to");
        }
    }

    /** 修改密码位数，并立刻生成一个该位数的新密码。 */
    public static int setLength(int length) {
        synchronized (LOCK) {
            KanheConfig current = config();
            current.length = PasswordCodes.clampLength(length);
            current.code = PasswordCodes.generate(current.length);
            current.enabled = true;
            current.save(configPath);
            logCode("Server password length is now " + current.length + ", new password");
            return current.length;
        }
    }

    public static void setEnabled(boolean enabled) {
        synchronized (LOCK) {
            config().enabled = enabled;
            config().save(configPath);
            Kanhe.LOGGER.info("Server password protection is now {}", enabled ? "enabled" : "disabled");
        }
    }

    private static void logCode(String prefix) {
        if (config.logCode) {
            Kanhe.LOGGER.info("{} {} - players enter it in the join password prompt", prefix, config.code);
        }
    }
}
