package com.kanhe;

import net.fabricmc.api.ModInitializer;

public class KanheMod implements ModInitializer {
    @Override
    public void onInitialize() {
        // 首次启动时生成 config/kanhe.json（以及一个随机的 6 位密码）。
        KanheGate.config();
    }
}
