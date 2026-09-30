package com.kanhe.client.mixin;

import com.kanhe.client.PasswordPrompt;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.network.DisconnectionDetails;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientHandshakePacketListenerImpl.class)
public class ClientHandshakePacketListenerImplMixin {
    @Inject(method = "onDisconnect", at = @At("HEAD"), cancellable = true)
    private void kanhe$retryPassword(DisconnectionDetails details, CallbackInfo ci) {
        if (PasswordPrompt.onConnectFailed(details)) {
            ci.cancel();
        }
    }
}
