package com.kanhe.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kanhe.Kanhe;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

/** 客户端记住的密码，按服务器地址各存一条。 */
public final class ClientPasswordStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("kanhe-client.json");

    private static Store store;

    public static final class Store {
        public boolean promptAlways = true;
        public boolean rememberByDefault = true;
        public Map<String, String> passwords = new LinkedHashMap<>();
    }

    public static synchronized Store store() {
        if (store == null) {
            store = new Store();
            try {
                if (Files.exists(PATH)) {
                    Store loaded = GSON.fromJson(Files.readString(PATH, StandardCharsets.UTF_8), Store.class);
                    if (loaded != null) {
                        store = loaded;
                    }
                }
            } catch (Exception e) {
                Kanhe.LOGGER.warn("Could not read {}", PATH, e);
            }
            if (store.passwords == null) {
                store.passwords = new LinkedHashMap<>();
            }
        }
        return store;
    }

    public static synchronized String get(String key) {
        return store().passwords.get(key);
    }

    public static synchronized void put(String key, String code) {
        if (code == null || code.isEmpty()) {
            store().passwords.remove(key);
        } else {
            store().passwords.put(key, code);
        }
        save();
    }

    private static void save() {
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, GSON.toJson(store()), StandardCharsets.UTF_8);
        } catch (Exception e) {
            Kanhe.LOGGER.error("Could not write {}", PATH, e);
        }
    }

    /** 记住密码时使用的键：优先用服务器列表里的地址，其次用原始地址。 */
    public static String keyOf(ServerData data, ServerAddress address) {
        String raw = data != null && data.ip != null && !data.ip.isBlank()
            ? data.ip
            : address.getHost() + ":" + address.getPort();
        int query = raw.indexOf('?');
        if (query >= 0) {
            raw = raw.substring(0, query);
        }
        int user = raw.indexOf('@');
        if (user >= 0) {
            raw = raw.substring(user + 1);
        }
        return raw.trim().toLowerCase(Locale.ROOT);
    }

    private ClientPasswordStore() {
    }
}
