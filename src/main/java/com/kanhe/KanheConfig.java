package com.kanhe;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class KanheConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean enabled = true;
    public String code = "";
    /** 随机密码的形态：digits（数字）/ letters（字母）/ mixed（字母+数字）。 */
    public String mode = PasswordCodes.DEFAULT_MODE.serializedName();
    /** 随机密码的位数，默认 6，范围 4~32。手动 set 的密码不受它限制。 */
    public int length = PasswordCodes.DEFAULT_LENGTH;
    /** 加载 / 重置 / 设置密码时，是否把当前密码打印到服务端控制台。 */
    public boolean logCode = true;

    public static KanheConfig load(Path path) {
        KanheConfig config = new KanheConfig();
        try {
            if (Files.exists(path)) {
                KanheConfig stored = GSON.fromJson(Files.readString(path, StandardCharsets.UTF_8), KanheConfig.class);
                if (stored != null) {
                    config = stored;
                }
            }
        } catch (Exception e) {
            Kanhe.LOGGER.warn("Could not read {} - regenerating it", path, e);
        }
        PasswordCodes.Mode mode = PasswordCodes.Mode.byName(config.mode);
        config.mode = (mode == null ? PasswordCodes.DEFAULT_MODE : mode).serializedName();
        config.length = PasswordCodes.clampLength(config.length <= 0 ? PasswordCodes.DEFAULT_LENGTH : config.length);
        if (!PasswordCodes.isValidPassword(config.code)) {
            config.code = PasswordCodes.generate(PasswordCodes.Mode.byName(config.mode), config.length);
        }
        config.save(path);
        return config;
    }

    public PasswordCodes.Mode randomMode() {
        PasswordCodes.Mode mode = PasswordCodes.Mode.byName(this.mode);
        return mode == null ? PasswordCodes.DEFAULT_MODE : mode;
    }

    public void save(Path path) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(this), StandardCharsets.UTF_8);
        } catch (Exception e) {
            Kanhe.LOGGER.error("Could not write {}", path, e);
        }
    }
}
