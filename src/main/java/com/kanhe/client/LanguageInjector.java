package com.kanhe.client;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kanhe.Kanhe;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;

/**
 * Fabric Loader 本身不会把 mod 的 {@code assets/} 目录交给客户端资源管理器（那是 Fabric API 的活），
 * 所以本 mod 的文本直接从 mod jar 里读出来，由
 * {@link com.kanhe.client.mixin.ClientLanguageMixin} 提供。
 */
public final class LanguageInjector {
    private static final Gson GSON = new Gson();
    private static final String[] OWNED_PREFIXES = {
        "kanhe.",
        "disconnect.kanhe.",
        "commands.kanhe."
    };

    private static final Map<String, String> ENGLISH = load("en_us");
    private static final Map<String, String> CHINESE = load("zh_cn");

    private static String selectedCode = "";
    private static Map<String, String> selected = ENGLISH;

    /** @return 属于本 mod 的键对应的文本；属于原版或其他 mod 的键返回 null。 */
    public static String translate(String key) {
        if (!isOwned(key)) {
            return null;
        }
        String value = current().get(key);
        return value != null ? value : ENGLISH.get(key);
    }

    private static boolean isOwned(String key) {
        for (String prefix : OWNED_PREFIXES) {
            if (key.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private static Map<String, String> current() {
        Minecraft minecraft = Minecraft.getInstance();
        String code = minecraft == null ? "en_us" : minecraft.getLanguageManager().getSelected();
        if (!code.equals(selectedCode)) {
            selectedCode = code;
            selected = code.startsWith("zh") ? CHINESE : ENGLISH;
        }
        return selected;
    }

    private static Map<String, String> load(String code) {
        Map<String, String> translations = new HashMap<>();
        String path = "/assets/" + Kanhe.MOD_ID + "/lang/" + code + ".json";
        try (InputStream stream = LanguageInjector.class.getResourceAsStream(path)) {
            if (stream == null) {
                return translations;
            }
            JsonObject json = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);
            if (json == null) {
                return translations;
            }
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                translations.put(entry.getKey(), entry.getValue().getAsString());
            }
        } catch (Exception e) {
            Kanhe.LOGGER.warn("Could not load {}", path, e);
        }
        return translations;
    }

    private LanguageInjector() {
    }
}
