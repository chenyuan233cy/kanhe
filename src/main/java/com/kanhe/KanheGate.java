package com.kanhe;

import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.QueryProperties;

/**
 * 服务端状态：每个进服玩家都必须通过服务器地址里的 {@code _id} 参数带上这个密码，
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
        if (!current.enabled || !PasswordCodes.isValidPassword(current.code)) {
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

    /** 当前随机密码的形态。 */
    public static PasswordCodes.Mode mode() {
        return config().randomMode();
    }

    /** 当前随机密码的位数。 */
    public static int length() {
        return config().length;
    }

    /** 按当前形态与位数重新生成一个随机密码。 */
    public static String regenerate() {
        synchronized (LOCK) {
            KanheConfig current = config();
            current.code = PasswordCodes.generate(current.randomMode(), current.length);
            current.enabled = true;
            current.save(configPath);
            logCode("Server password reset to");
            return current.code;
        }
    }

    /** 手动指定密码，不影响随机密码的形态与位数。 */
    public static void setCode(String code) {
        synchronized (LOCK) {
            config().code = code;
            config().save(configPath);
            logCode("Server password set to");
        }
    }

    /** 修改随机密码的位数，并立刻生成一个该位数的新密码。 */
    public static int setLength(int length) {
        synchronized (LOCK) {
            KanheConfig current = config();
            current.length = PasswordCodes.clampLength(length);
            current.code = PasswordCodes.generate(current.randomMode(), current.length);
            current.enabled = true;
            current.save(configPath);
            logCode("Random password length is now " + current.length + ", new password");
            return current.length;
        }
    }

    /** 修改随机密码的形态，并立刻生成一个新密码。 */
    public static PasswordCodes.Mode setMode(PasswordCodes.Mode mode) {
        synchronized (LOCK) {
            KanheConfig current = config();
            current.mode = mode.serializedName();
            current.code = PasswordCodes.generate(mode, current.length);
            current.enabled = true;
            current.save(configPath);
            logCode("Random password mode is now " + mode.serializedName() + ", new password");
            return mode;
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
