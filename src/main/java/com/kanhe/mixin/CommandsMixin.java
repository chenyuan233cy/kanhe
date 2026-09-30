package com.kanhe.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.kanhe.KanheCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Commands.class)
public class CommandsMixin {
    @Shadow
    @Final
    private CommandDispatcher<CommandSourceStack> dispatcher;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void kanhe$registerCommand(Commands.CommandSelection selection, CommandBuildContext context, CallbackInfo ci) {
        if (selection == Commands.CommandSelection.ALL) {
            // 只有那个用于校验的裸分发器会用 ALL，它没有绑定任何命令源。
            return;
        }
        KanheCommand.register(this.dispatcher);
    }
}
