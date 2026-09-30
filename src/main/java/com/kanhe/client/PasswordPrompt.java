package com.kanhe.client;

import com.kanhe.Kanhe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.TransferState;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/** 把密码界面接到原版的进服流程里。 */
public final class PasswordPrompt {
    private record Attempt(Screen parent, ServerAddress address, ServerData data, boolean quickPlay, TransferState transfer, String code) {
    }

    private static Attempt attempt;
    private static boolean reentrant;

    /** @return 是否已经接管这次进服（弹出了密码界面，或改写了连接地址）。 */
    public static boolean intercept(Screen parent, Minecraft minecraft, ServerAddress address, ServerData data, boolean quickPlay, TransferState transfer) {
        if (reentrant) {
            return false;
        }
        if (address.getProperties().get(Kanhe.PROPERTY_ID) != null) {
            // 玩家自己把密码写进地址里了，不需要再问。
            return false;
        }

        String key = ClientPasswordStore.keyOf(data, address);
        String remembered = ClientPasswordStore.get(key);
        if (remembered != null && !remembered.isEmpty() && !ClientPasswordStore.store().promptAlways) {
            connect(parent, minecraft, address, data, quickPlay, transfer, remembered);
            return true;
        }
        Kanhe.LOGGER.info("Asking for the join password of {}", key);
        minecraft.gui.setScreen(new PasswordScreen(parent, address, data, quickPlay, transfer,
            remembered == null ? "" : remembered, null));
        return true;
    }

    public static void submit(Screen parent, Minecraft minecraft, ServerAddress address, ServerData data, boolean quickPlay, TransferState transfer, String code, boolean remember) {
        if (remember) {
            ClientPasswordStore.put(ClientPasswordStore.keyOf(data, address), code);
        }
        connect(parent, minecraft, address, data, quickPlay, transfer, code);
    }

    private static void connect(Screen parent, Minecraft minecraft, ServerAddress address, ServerData data, boolean quickPlay, TransferState transfer, String code) {
        ServerAddress target = code == null || code.isEmpty()
            ? address
            : new ServerAddress(address.getHost(), address.getPort(), address.getProperties().with(Kanhe.PROPERTY_ID, code));
        attempt = new Attempt(parent, address, data, quickPlay, transfer, code == null ? "" : code);
        Kanhe.LOGGER.info("Connecting to {}:{} with a {} digit join password",
            address.getHost(), address.getPort(), code == null ? 0 : code.length());
        reentrant = true;
        try {
            ConnectScreen.startConnecting(parent, minecraft, target, data, quickPlay, transfer);
        } finally {
            reentrant = false;
        }
    }

    /** 玩家还没进去连接就断了：带着失败原因重新弹出密码界面。 */
    public static boolean onConnectFailed(DisconnectionDetails details) {
        Attempt failed = attempt;
        attempt = null;
        if (failed == null) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Component reason = details == null ? CommonComponents.EMPTY : details.reason();
        Component error = Component.translatable("kanhe.screen.failed", reason);
        minecraft.execute(() -> minecraft.gui.setScreen(new PasswordScreen(failed.parent(), failed.address(), failed.data(),
            failed.quickPlay(), failed.transfer(), failed.code(), error)));
        return true;
    }

    private PasswordPrompt() {
    }
}
