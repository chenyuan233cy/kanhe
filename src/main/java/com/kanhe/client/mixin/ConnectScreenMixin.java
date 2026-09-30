package com.kanhe.client.mixin;

import com.kanhe.client.PasswordPrompt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.TransferState;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConnectScreen.class)
public class ConnectScreenMixin {
    @Inject(method = "startConnecting", at = @At("HEAD"), cancellable = true)
    private static void kanhe$askForPassword(Screen parent, Minecraft minecraft, ServerAddress address, ServerData data,
                                                     boolean quickPlay, TransferState transfer, CallbackInfo ci) {
        if (PasswordPrompt.intercept(parent, minecraft, address, data, quickPlay, transfer)) {
            ci.cancel();
        }
    }
}
