package com.kanhe.mixin;

import com.kanhe.Kanhe;
import com.kanhe.KanheGate;
import net.minecraft.network.Connection;
import net.minecraft.network.ServerConnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.handshake.ClientIntent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.login.LoginProtocols;
import net.minecraft.server.network.ServerHandshakePacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 拒绝所有没带当前密码的连接，状态查询（ping）和登录一视同仁
 * （原版对 {@code allowed-connection-ids} 就是这么处理的）。单机存档的主机不受影响，
 * 因为它走的是内存握手监听器。
 */
@Mixin(ServerHandshakePacketListenerImpl.class)
public class ServerHandshakePacketListenerImplMixin {
    @Shadow
    @Final
    private Connection connection;

    @Inject(method = "handleIntention", at = @At("HEAD"), cancellable = true)
    private void kanhe$requireCode(ClientIntentionPacket packet, CallbackInfo ci) {
        ServerConnectionDetails details = ServerConnectionDetails.fromIntentPacket(packet);
        if (KanheGate.accepts(details.properties())) {
            return;
        }

        boolean codeSupplied = details.properties().get(Kanhe.PROPERTY_ID) != null;
        if (packet.intention() == ClientIntent.LOGIN && codeSupplied) {
            // 客户端本来就知道这个服务器，所以可以告诉它为什么被拒绝。
            Component reason = Component.translatableWithFallback(Kanhe.KEY_WRONG_CODE,
                "Wrong server password - ask the admin for the current 6 digit code");
            this.connection.setupOutboundProtocol(LoginProtocols.CLIENTBOUND);
            this.connection.send(new ClientboundLoginDisconnectPacket(reason));
            this.connection.disconnect(reason);
        } else {
            // 完全没带密码：保持沉默，让服务器看起来像一个不存在的地址。
            this.connection.disconnect(Component.translatable("multiplayer.disconnect.rejected"));
        }
        ci.cancel();
    }
}
