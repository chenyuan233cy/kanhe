package com.kanhe;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class KanheCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(tree("kanhe"));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> tree(String name) {
        return Commands.literal(name)
            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .executes(context -> show(context.getSource()))
            .then(Commands.literal("show")
                .executes(context -> show(context.getSource())))
            .then(Commands.literal("reset")
                .executes(context -> reset(context.getSource())))
            .then(Commands.literal("set")
                .then(Commands.argument("code", StringArgumentType.word())
                    .executes(context -> set(context.getSource(), StringArgumentType.getString(context, "code")))))
            .then(Commands.literal("length")
                .then(Commands.argument("digits", IntegerArgumentType.integer(PasswordCodes.MIN_LENGTH, PasswordCodes.MAX_LENGTH))
                    .executes(context -> setLength(context.getSource(), IntegerArgumentType.getInteger(context, "digits")))))
            .then(Commands.literal("on")
                .executes(context -> toggle(context.getSource(), true)))
            .then(Commands.literal("off")
                .executes(context -> toggle(context.getSource(), false)));
    }

    private static int show(CommandSourceStack source) {
        source.sendSuccess(() -> Component.translatableWithFallback("commands.kanhe.show",
            "Server password: %s (%s, %s digits)", KanheGate.code(),
            Component.translatableWithFallback(
                KanheGate.isEnabled() ? "commands.kanhe.on" : "commands.kanhe.off",
                KanheGate.isEnabled() ? "enabled" : "disabled"),
            KanheGate.length()), false);
        return 1;
    }

    private static int reset(CommandSourceStack source) {
        String code = KanheGate.regenerate();
        source.sendSuccess(() -> Component.translatableWithFallback("commands.kanhe.reset",
            "New server password: %s", code), true);
        return 1;
    }

    private static int set(CommandSourceStack source, String code) {
        if (!PasswordCodes.isValid(code)) {
            source.sendFailure(Component.translatableWithFallback("commands.kanhe.invalid",
                "The password must be %s to %s digits", PasswordCodes.MIN_LENGTH, PasswordCodes.MAX_LENGTH));
            return 0;
        }
        KanheGate.setCode(code);
        source.sendSuccess(() -> Component.translatableWithFallback("commands.kanhe.set",
            "Server password set to %s (%s digits)", code, code.length()), true);
        return 1;
    }

    private static int setLength(CommandSourceStack source, int digits) {
        int length = KanheGate.setLength(digits);
        source.sendSuccess(() -> Component.translatableWithFallback("commands.kanhe.length",
            "Password length is now %s, new password: %s", length, KanheGate.code()), true);
        return 1;
    }

    private static int toggle(CommandSourceStack source, boolean enabled) {
        KanheGate.setEnabled(enabled);
        source.sendSuccess(() -> Component.translatableWithFallback(
            enabled ? "commands.kanhe.enabled" : "commands.kanhe.disabled",
            enabled ? "Server password protection enabled" : "Server password protection disabled"), true);
        return 1;
    }

    private KanheCommand() {
    }
}
