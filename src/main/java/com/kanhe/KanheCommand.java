package com.kanhe;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

public final class KanheCommand {
    /** 每条帮助：翻译键|英文兜底文本。 */
    private static final String[] HELP_LINES = {
        "commands.kanhe.help.show|/kanhe show - show the current password, random mode, random length and state",
        "commands.kanhe.help.reset|/kanhe reset - generate a new random password using the current mode and length",
        "commands.kanhe.help.set|/kanhe set <password> - set a password manually (does not change mode or length)",
        "commands.kanhe.help.length|/kanhe length <4-32> - change the random password length and generate a new one",
        "commands.kanhe.help.mode|/kanhe mode <digits|letters|mixed> - change the random password mode and generate a new one",
        "commands.kanhe.help.toggle|/kanhe on | off - enable or disable the protection"
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(tree("kanhe"));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> tree(String name) {
        return Commands.literal(name)
            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .executes(context -> help(context.getSource()))
            .then(Commands.literal("help")
                .executes(context -> help(context.getSource())))
            .then(Commands.literal("show")
                .executes(context -> show(context.getSource())))
            .then(Commands.literal("reset")
                .executes(context -> reset(context.getSource())))
            .then(Commands.literal("set")
                .then(Commands.argument("password", StringArgumentType.string())
                    .executes(context -> set(context.getSource(), StringArgumentType.getString(context, "password")))))
            .then(Commands.literal("length")
                .then(Commands.argument("digits", IntegerArgumentType.integer(PasswordCodes.MIN_LENGTH, PasswordCodes.MAX_LENGTH))
                    .executes(context -> setLength(context.getSource(), IntegerArgumentType.getInteger(context, "digits")))))
            .then(Commands.literal("mode")
                .then(Commands.argument("mode", StringArgumentType.word())
                    .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                        new String[] {"digits", "letters", "mixed"}, builder))
                    .executes(context -> setMode(context.getSource(), StringArgumentType.getString(context, "mode")))))
            .then(Commands.literal("on")
                .executes(context -> toggle(context.getSource(), true)))
            .then(Commands.literal("off")
                .executes(context -> toggle(context.getSource(), false)));
    }

    private static int help(CommandSourceStack source) {
        source.sendSuccess(() -> Component.translatableWithFallback("commands.kanhe.help.title",
            "Kanhe - join password commands:"), false);
        for (String line : HELP_LINES) {
            int split = line.indexOf('|');
            String key = line.substring(0, split);
            String fallback = line.substring(split + 1);
            source.sendSuccess(() -> Component.translatableWithFallback(key, fallback), false);
        }
        return 1;
    }

    private static Component modeName(PasswordCodes.Mode mode) {
        return Component.translatableWithFallback("commands.kanhe.mode." + mode.serializedName(), mode.serializedName());
    }

    private static int show(CommandSourceStack source) {
        source.sendSuccess(() -> Component.translatableWithFallback("commands.kanhe.show",
            "Server password: %s (%s, random: %s, %s chars)", KanheGate.code(),
            Component.translatableWithFallback(
                KanheGate.isEnabled() ? "commands.kanhe.on" : "commands.kanhe.off",
                KanheGate.isEnabled() ? "enabled" : "disabled"),
            modeName(KanheGate.mode()), KanheGate.length()), false);
        return 1;
    }

    private static int reset(CommandSourceStack source) {
        String code = KanheGate.regenerate();
        source.sendSuccess(() -> Component.translatableWithFallback("commands.kanhe.reset",
            "New random password: %s", code), true);
        return 1;
    }

    private static int set(CommandSourceStack source, String code) {
        if (!PasswordCodes.isValidPassword(code)) {
            source.sendFailure(Component.translatableWithFallback("commands.kanhe.invalid",
                "The password must be 1 to %s visible characters (no spaces)", PasswordCodes.MAX_SET_LENGTH));
            return 0;
        }
        KanheGate.setCode(code);
        source.sendSuccess(() -> Component.translatableWithFallback("commands.kanhe.set",
            "Server password set to %s (%s chars)", code, code.length()), true);
        return 1;
    }

    private static int setLength(CommandSourceStack source, int digits) {
        int length = KanheGate.setLength(digits);
        source.sendSuccess(() -> Component.translatableWithFallback("commands.kanhe.length",
            "Random password length is now %s, new password: %s", length, KanheGate.code()), true);
        return 1;
    }

    private static int setMode(CommandSourceStack source, String name) {
        PasswordCodes.Mode mode = PasswordCodes.Mode.byName(name);
        if (mode == null) {
            source.sendFailure(Component.translatableWithFallback("commands.kanhe.mode.invalid",
                "Unknown mode '%s' - use digits, letters or mixed", name));
            return 0;
        }
        KanheGate.setMode(mode);
        source.sendSuccess(() -> Component.translatableWithFallback("commands.kanhe.mode",
            "Random password mode is now %s, new password: %s", modeName(mode), KanheGate.code()), true);
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
