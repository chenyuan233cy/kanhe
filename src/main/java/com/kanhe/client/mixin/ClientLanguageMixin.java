package com.kanhe.client.mixin;

import com.kanhe.client.LanguageInjector;
import net.minecraft.client.resources.language.ClientLanguage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 普通的 HEAD 注入：只回答本 mod 自己的键，其余查询全部交还原版，
 * 因此任意多个 mod 都可以挂同样的方法而互不冲突。
 */
@Mixin(ClientLanguage.class)
public class ClientLanguageMixin {
    @Inject(method = "getOrDefault", at = @At("HEAD"), cancellable = true)
    private void kanhe$translate(String key, String defaultValue, CallbackInfoReturnable<String> cir) {
        String translation = LanguageInjector.translate(key);
        if (translation != null) {
            cir.setReturnValue(translation);
        }
    }

    @Inject(method = "has", at = @At("HEAD"), cancellable = true)
    private void kanhe$hasTranslation(String key, CallbackInfoReturnable<Boolean> cir) {
        if (LanguageInjector.translate(key) != null) {
            cir.setReturnValue(true);
        }
    }
}
