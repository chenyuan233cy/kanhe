package com.kanhe.client;

import net.fabricmc.api.ClientModInitializer;

public class KanheClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPasswordStore.store();
    }
}
