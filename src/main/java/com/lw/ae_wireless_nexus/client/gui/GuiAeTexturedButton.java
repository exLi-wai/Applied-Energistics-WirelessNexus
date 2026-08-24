package com.lw.ae_wireless_nexus.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public final class GuiAeTexturedButton extends GuiButton {
    private static final ResourceLocation NORMAL = texture("button.png");
    private static final ResourceLocation HOVERED = texture("button_highlighted.png");
    private static final ResourceLocation DISABLED = texture("button_disabled.png");

    public GuiAeTexturedButton(int id, int x, int y, String text) {
        this(id, x, y, 200, text);
    }

    public GuiAeTexturedButton(int id, int x, int y, int width, String text) {
        super(id, x, y, width, 20, text);
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        if (!visible) return;
        hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
        minecraft.getTextureManager().bindTexture(!enabled ? DISABLED : hovered ? HOVERED : NORMAL);
        GlStateManager.color(1F, 1F, 1F, 1F);
        drawModalRectWithCustomSizedTexture(x, y, 0, 0, 3, height, 200, 20);
        drawModalRectWithCustomSizedTexture(x + 3, y, 3, 0, width - 6, height, 200, 20);
        drawModalRectWithCustomSizedTexture(x + width - 3, y, 197, 0, 3, height, 200, 20);
        mouseDragged(minecraft, mouseX, mouseY);
        int color = !enabled ? 0xA0A0A0 : hovered ? 0xFFFFA0 : 0xE0E0E0;
        drawCenteredString(minecraft.fontRenderer, displayString, x + width / 2, y + 6, color);
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation("ae_wireless_nexus", "textures/gui/sprites/" + name);
    }
}
