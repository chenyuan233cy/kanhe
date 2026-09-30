package com.kanhe.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.TransferState;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

public class PasswordScreen extends Screen {
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int HINT_COLOR = 0xFFA0A0A0;
    private static final int ERROR_COLOR = 0xFFFF5555;

    private final Screen parent;
    private final ServerAddress address;
    private final ServerData data;
    private final boolean quickPlay;
    private final TransferState transfer;
    private final String initialCode;
    private final Component error;

    private EditBox codeBox;
    private Checkbox rememberBox;

    public PasswordScreen(Screen parent, ServerAddress address, ServerData data, boolean quickPlay, TransferState transfer, String initialCode, Component error) {
        super(Component.translatable("kanhe.screen.title"));
        this.parent = parent;
        this.address = address;
        this.data = data;
        this.quickPlay = quickPlay;
        this.transfer = transfer;
        this.initialCode = initialCode == null ? "" : initialCode;
        this.error = error;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2 - 10;

        // 玩家输入的内容一律渲染成 '*'，真实值不会显示出来。
        this.codeBox = new EditBox(this.font, centerX - 100, centerY, 200, 20, Component.translatable("kanhe.screen.hint"));
        this.codeBox.setMaxLength(64);
        this.codeBox.addFormatter((text, offset) -> FormattedCharSequence.forward("*".repeat(text.length()), Style.EMPTY));
        this.codeBox.setValue(this.initialCode);
        this.addRenderableWidget(this.codeBox);
        this.setInitialFocus(this.codeBox);

        this.rememberBox = Checkbox.builder(Component.translatable("kanhe.screen.remember"), this.font)
            .pos(centerX - 100, centerY + 28)
            .selected(ClientPasswordStore.store().rememberByDefault)
            .build();
        this.addRenderableWidget(this.rememberBox);

        this.addRenderableWidget(Button.builder(Component.translatable("kanhe.screen.join"), button -> this.submit())
            .bounds(centerX - 102, centerY + 54, 100, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.minecraft.gui.setScreen(this.parent))
            .bounds(centerX + 2, centerY + 54, 100, 20).build());
    }

    private void submit() {
        PasswordPrompt.submit(this.parent, this.minecraft, this.address, this.data, this.quickPlay, this.transfer,
            this.codeBox.getValue().trim(), this.rememberBox.selected());
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.isConfirmation()) {
            this.submit();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        int centerX = this.width / 2;
        int top = this.height / 2 - 66;
        graphics.centeredText(this.font, this.title, centerX, top, TEXT_COLOR);
        graphics.centeredText(this.font, Component.translatable("kanhe.screen.server", this.serverLabel()), centerX, top + 14, HINT_COLOR);
        graphics.centeredText(this.font, Component.translatable("kanhe.screen.help"), centerX, top + 30, HINT_COLOR);
        if (this.error != null) {
            graphics.centeredText(this.font, this.error, centerX, top + 46, ERROR_COLOR);
        }
    }

    private String serverLabel() {
        if (this.data != null && this.data.name != null && !this.data.name.isBlank()) {
            return this.data.name;
        }
        return this.address.getHost() + ":" + this.address.getPort();
    }
}
