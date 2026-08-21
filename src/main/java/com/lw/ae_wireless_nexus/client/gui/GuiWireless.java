package com.lw.ae_wireless_nexus.client.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.input.Mouse;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import com.lw.ae_wireless_nexus.common.gui.ContainerWireless;
import com.lw.ae_wireless_nexus.common.gui.GuiHandler;
import com.lw.ae_wireless_nexus.common.network.NetworkHandler;
import com.lw.ae_wireless_nexus.common.network.PacketWirelessBind;
import com.lw.ae_wireless_nexus.common.network.PacketWirelessNetworks;
import com.lw.ae_wireless_nexus.common.network.PacketWirelessPriority;
import com.lw.ae_wireless_nexus.common.network.PacketWirelessRename;
import com.lw.ae_wireless_nexus.common.network.PacketWirelessState;
import com.lw.ae_wireless_nexus.common.network.PacketWirelessUnbind;

public final class GuiWireless extends GuiContainer {
    private static final ResourceLocation BACKGROUND =
        new ResourceLocation("ae_wireless_nexus", "textures/guis/background.png");
    private static final ResourceLocation SCROLLER =
        new ResourceLocation("ae_wireless_nexus", "textures/gui/sprites/small_scroller.png");
    private static final ResourceLocation SCROLLER_DISABLED =
        new ResourceLocation("ae_wireless_nexus", "textures/gui/sprites/small_scroller_disabled.png");
    private static final int BACKGROUND_TEXTURE_SIZE = 256;
    private static final int BACKGROUND_BORDER_SIZE = 2;
    private static final int PANEL_COLOR = 0xFF30383A;
    private static final int PANEL_BORDER = 0xFF768083;
    private static final int TEXT_COLOR = 0xFF404040;
    private static final int MUTED_COLOR = 0xFF686868;
    private static final int LIST_TOP = 57;
    private static final int LIST_BOTTOM = 177;
    private static final int LIST_ROW_HEIGHT = 30;
    private static final int VISIBLE_LIST_ROWS = 4;
    private static final int SCROLLBAR_WIDTH = 7;
    private static final int SCROLLBAR_THUMB_HEIGHT = 15;
    private static final int SCROLLBAR_TOP = 61;
    private static final int SCROLLBAR_BOTTOM = 173;
    private final int guiId;
    private final BlockPos position;
    private GuiTextField nameField;
    private GuiTextField priorityField;
    private boolean nameStateApplied;
    private boolean priorityStateApplied;
    private int networkScrollOffset;
    private boolean draggingNetworkScrollbar;
    private int scrollbarDragOffset;

    public GuiWireless(ContainerWireless container, int guiId, BlockPos position) {
        super(container);
        this.guiId = guiId;
        this.position = position;
        xSize = guiId == GuiHandler.WIRELESS_CONTROLLER ? 220 : 236;
        ySize = guiId == GuiHandler.WIRELESS_CONTROLLER ? 160 : 220;
    }

    @Override
    public void initGui() {
        super.initGui();
        if (guiId == GuiHandler.WIRELESS_CONTROLLER) {
            nameField = new GuiTextField(1, fontRenderer, guiLeft + 10, guiTop + 44, xSize - 20, 18);
            nameField.setMaxStringLength(com.lw.ae_wireless_nexus.config.WirelessConfig.maxNetworkNameLength);
            nameField.setTextColor(0xFFFFFF);
            nameField.setText("Wireless Network");
        } else {
            priorityField = new GuiTextField(2, fontRenderer, guiLeft + xSize - 54, guiTop + 27, 44, 18);
            priorityField.setMaxStringLength(4);
            priorityField.setTextColor(0xFFFFFF);
            priorityField.setText("0");
            buttonList.add(new GuiAeTexturedButton(1, guiLeft + xSize - 76, guiTop + 190, 66,
                I18n.format("gui.ae_wireless_nexus.disconnect")));
        }
        applyServerState();
    }

    @Override
    public void onGuiClosed() {
        synchronized (PacketWirelessNetworks.CLIENT_ENTRIES) {
            PacketWirelessNetworks.CLIENT_ENTRIES.clear();
        }
        PacketWirelessState.CLIENT_STATE = null;
        super.onGuiClosed();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 28 || keyCode == 156) {
            if (nameField != null && nameField.isFocused()) {
                NetworkHandler.CHANNEL.sendToServer(new PacketWirelessRename(position, nameField.getText()));
                nameField.setFocused(false);
                return;
            }
            if (priorityField != null && priorityField.isFocused()) {
                sendPriority();
                priorityField.setFocused(false);
                return;
            }
        }
        if (nameField != null && nameField.textboxKeyTyped(typedChar, keyCode)) return;
        if (priorityField != null && priorityField.textboxKeyTyped(typedChar, keyCode)) return;
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (nameField != null) nameField.mouseClicked(mouseX, mouseY, mouseButton);
        if (priorityField != null) priorityField.mouseClicked(mouseX, mouseY, mouseButton);
        if (guiId != GuiHandler.WIRELESS_CONNECTOR || mouseButton != 0) return;

        List<PacketWirelessNetworks.Entry> entries = snapshotEntries();
        clampNetworkScroll(entries.size());
        int scrollbarLeft = guiLeft + xSize - 18;
        if (entries.size() > VISIBLE_LIST_ROWS
            && mouseX >= scrollbarLeft
            && mouseX < scrollbarLeft + SCROLLBAR_WIDTH
            && mouseY >= guiTop + SCROLLBAR_TOP
            && mouseY < guiTop + SCROLLBAR_BOTTOM) {
            int thumbTop = guiTop + getScrollbarThumbTop(entries.size());
            int thumbHeight = getScrollbarThumbHeight(entries.size());
            scrollbarDragOffset = mouseY >= thumbTop && mouseY < thumbTop + thumbHeight
                ? mouseY - thumbTop
                : thumbHeight / 2;
            draggingNetworkScrollbar = true;
            updateNetworkScrollFromMouse(mouseY, entries.size());
            return;
        }

        int listTop = guiTop + LIST_TOP;
        int row = (mouseY - listTop) / LIST_ROW_HEIGHT;
        int entryIndex = networkScrollOffset + row;
        if (mouseX >= guiLeft + 10 && mouseX < scrollbarLeft && mouseY >= listTop
            && mouseY < guiTop + LIST_BOTTOM && row >= 0 && row < VISIBLE_LIST_ROWS
            && entryIndex < entries.size()) {
            NetworkHandler.CHANNEL.sendToServer(new PacketWirelessBind(
                position, entries.get(entryIndex).id));
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton,
        long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        if (!draggingNetworkScrollbar || clickedMouseButton != 0) return;
        List<PacketWirelessNetworks.Entry> entries = snapshotEntries();
        updateNetworkScrollFromMouse(mouseY, entries.size());
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        draggingNetworkScrollbar = false;
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if (guiId != GuiHandler.WIRELESS_CONNECTOR) return;

        int wheel = Mouse.getEventDWheel();
        if (wheel == 0) return;
        int mouseX = Mouse.getEventX() * width / mc.displayWidth;
        int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        if (mouseX < guiLeft + 10 || mouseX >= guiLeft + xSize - 10
            || mouseY < guiTop + LIST_TOP || mouseY >= guiTop + LIST_BOTTOM) return;

        List<PacketWirelessNetworks.Entry> entries = snapshotEntries();
        networkScrollOffset += wheel > 0 ? -1 : 1;
        clampNetworkScroll(entries.size());
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 1 && guiId == GuiHandler.WIRELESS_CONNECTOR) {
            NetworkHandler.CHANNEL.sendToServer(new PacketWirelessUnbind(position));
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1F, 1F, 1F, 1F);
        mc.getTextureManager().bindTexture(BACKGROUND);
        drawBackgroundPanel(guiLeft, guiTop, xSize, ySize);
        if (guiId == GuiHandler.WIRELESS_CONTROLLER) {
            drawPanel(guiLeft + 10, guiTop + 84, 62, 54);
            drawPanel(guiLeft + 79, guiTop + 84, 62, 54);
            drawPanel(guiLeft + 148, guiTop + 84, 62, 54);
        } else {
            drawPanel(guiLeft + 10, guiTop + 57, xSize - 20, 120);
        }
    }

    private void drawBackgroundPanel(int x, int y, int width, int height) {
        int border = BACKGROUND_BORDER_SIZE;
        int texture = BACKGROUND_TEXTURE_SIZE;
        int centerTextureSize = texture - border * 2;
        int centerWidth = width - border * 2;
        int centerHeight = height - border * 2;

        drawScaledCustomSizeModalRect(
            x, y, 0, 0, border, border, border, border, texture, texture);
        drawScaledCustomSizeModalRect(
            x + border, y, border, 0, centerTextureSize, border,
            centerWidth, border, texture, texture);
        drawScaledCustomSizeModalRect(
            x + width - border, y, texture - border, 0, border, border,
            border, border, texture, texture);

        drawScaledCustomSizeModalRect(
            x, y + border, 0, border, border, centerTextureSize,
            border, centerHeight, texture, texture);
        drawScaledCustomSizeModalRect(
            x + border, y + border, border, border, centerTextureSize, centerTextureSize,
            centerWidth, centerHeight, texture, texture);
        drawScaledCustomSizeModalRect(
            x + width - border, y + border, texture - border, border,
            border, centerTextureSize, border, centerHeight, texture, texture);

        drawScaledCustomSizeModalRect(
            x, y + height - border, 0, texture - border, border, border,
            border, border, texture, texture);
        drawScaledCustomSizeModalRect(
            x + border, y + height - border, border, texture - border,
            centerTextureSize, border, centerWidth, border, texture, texture);
        drawScaledCustomSizeModalRect(
            x + width - border, y + height - border, texture - border, texture - border,
            border, border, border, border, texture, texture);
    }

    private void drawPanel(int x, int y, int width, int height) {
        drawRect(x, y, x + width, y + height, PANEL_BORDER);
        drawRect(x + 2, y + 2, x + width - 2, y + height - 2, PANEL_COLOR);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        applyServerState();
        if (nameField != null) nameField.drawTextBox();
        if (priorityField != null) priorityField.drawTextBox();
        renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        if (guiId == GuiHandler.WIRELESS_CONTROLLER) drawController();
        else drawConnector();
    }

    private void drawController() {
        drawCentered(I18n.format("gui.ae_wireless_nexus.controller_title"), 10, TEXT_COLOR);
        fontRenderer.drawString(I18n.format("gui.ae_wireless_nexus.network_name"), 10, 30, MUTED_COLOR);
        fontRenderer.drawString(I18n.format("gui.ae_wireless_nexus.channel_capacity"), 10, 75, MUTED_COLOR);
        PacketWirelessState.State state = currentState();
        int total = state == null ? 0 : state.total;
        int allocated = state == null ? 0 : state.allocated;
        drawCapacityCard(14, I18n.format("gui.ae_wireless_nexus.total"), total, 0xFFFFFF);
        drawCapacityCard(83, I18n.format("gui.ae_wireless_nexus.allocated"), allocated, 0xFFFFFF);
        drawCapacityCard(152, I18n.format("gui.ae_wireless_nexus.available"), Math.max(0, total - allocated), 0x55FFFF);
    }

    private void drawCapacityCard(int x, String label, int value, int color) {
        fontRenderer.drawString(label, x, 91, 0xC0C0C0);
        fontRenderer.drawString(Integer.toString(value), x, 110, color);
    }

    private void drawConnector() {
        drawCentered(I18n.format("gui.ae_wireless_nexus.connector_title"), 10, TEXT_COLOR);
        PacketWirelessState.State state = currentState();
        String stateText = state == null || state.id == null
            ? I18n.format("gui.ae_wireless_nexus.unbound")
            : state.online ? I18n.format("gui.ae_wireless_nexus.connected") : I18n.format("gui.ae_wireless_nexus.disconnected");
        fontRenderer.drawString(I18n.format("gui.ae_wireless_nexus.current_state", stateText), 10, 31, MUTED_COLOR);
        fontRenderer.drawString(I18n.format("gui.ae_wireless_nexus.priority"), xSize - 108, 31, MUTED_COLOR);

        List<PacketWirelessNetworks.Entry> entries = snapshotEntries();
        clampNetworkScroll(entries.size());
        int y = 61;
        int end = Math.min(entries.size(), networkScrollOffset + VISIBLE_LIST_ROWS);
        for (int index = networkScrollOffset; index < end; index++) {
            PacketWirelessNetworks.Entry entry = entries.get(index);
            if (state != null && entry.id.equals(state.id)) {
                drawRect(12, y - 2, xSize - 22, y + 25, 0xFF465256);
            }
            fontRenderer.drawString(entry.name, 16, y, 0xFFFFFF);
            String usage = entry.allocated + " / " + entry.total;
            fontRenderer.drawString(usage,
                xSize - 26 - fontRenderer.getStringWidth(usage), y, 0xFFFFFF);
            fontRenderer.drawString(I18n.format("gui.ae_wireless_nexus.available_channels", Math.max(0, entry.total - entry.allocated)),
                16, y + 11, 0xB8C0C0);
            y += LIST_ROW_HEIGHT;
        }
        drawNetworkScrollbar(entries.size());
        if (entries.isEmpty()) drawCentered(I18n.format("gui.ae_wireless_nexus.no_networks"), 108, 0xB8C0C0);
        fontRenderer.drawString(I18n.format("gui.ae_wireless_nexus.available_networks", entries.size()), 10, 196, MUTED_COLOR);
    }

    private void drawNetworkScrollbar(int entryCount) {
        int left = xSize - 18;
        drawRect(left, SCROLLBAR_TOP, left + SCROLLBAR_WIDTH, SCROLLBAR_BOTTOM,
            0xFF202628);
        int thumbTop = getScrollbarThumbTop(entryCount);
        ResourceLocation texture = entryCount <= VISIBLE_LIST_ROWS
            ? SCROLLER_DISABLED
            : SCROLLER;
        GlStateManager.color(1F, 1F, 1F, 1F);
        mc.getTextureManager().bindTexture(texture);
        drawModalRectWithCustomSizedTexture(left, thumbTop, 0, 0,
            SCROLLBAR_WIDTH, SCROLLBAR_THUMB_HEIGHT,
            SCROLLBAR_WIDTH, SCROLLBAR_THUMB_HEIGHT);
    }

    private int getScrollbarThumbHeight(int entryCount) {
        return SCROLLBAR_THUMB_HEIGHT;
    }

    private int getScrollbarThumbTop(int entryCount) {
        int maximum = getMaximumNetworkScroll(entryCount);
        if (maximum == 0) return SCROLLBAR_TOP;
        int thumbHeight = getScrollbarThumbHeight(entryCount);
        int travel = SCROLLBAR_BOTTOM - SCROLLBAR_TOP - thumbHeight;
        return SCROLLBAR_TOP + travel * networkScrollOffset / maximum;
    }

    private void updateNetworkScrollFromMouse(int mouseY, int entryCount) {
        int maximum = getMaximumNetworkScroll(entryCount);
        if (maximum == 0) {
            networkScrollOffset = 0;
            return;
        }
        int thumbHeight = getScrollbarThumbHeight(entryCount);
        int travel = SCROLLBAR_BOTTOM - SCROLLBAR_TOP - thumbHeight;
        int relative = mouseY - guiTop - SCROLLBAR_TOP - scrollbarDragOffset;
        networkScrollOffset = Math.round((float) relative * maximum / Math.max(1, travel));
        clampNetworkScroll(entryCount);
    }

    private void clampNetworkScroll(int entryCount) {
        networkScrollOffset = Math.max(0,
            Math.min(getMaximumNetworkScroll(entryCount), networkScrollOffset));
    }

    private int getMaximumNetworkScroll(int entryCount) {
        return Math.max(0, entryCount - VISIBLE_LIST_ROWS);
    }

    private void drawCentered(String text, int y, int color) {
        fontRenderer.drawString(text, (xSize - fontRenderer.getStringWidth(text)) / 2, y, color);
    }

    private List<PacketWirelessNetworks.Entry> snapshotEntries() {
        synchronized (PacketWirelessNetworks.CLIENT_ENTRIES) {
            return new ArrayList<PacketWirelessNetworks.Entry>(PacketWirelessNetworks.CLIENT_ENTRIES);
        }
    }

    private PacketWirelessState.State currentState() {
        PacketWirelessState.State state = PacketWirelessState.CLIENT_STATE;
        return state != null && state.matches(position) ? state : null;
    }

    private void applyServerState() {
        PacketWirelessState.State state = currentState();
        if (state == null) return;
        if (!nameStateApplied && nameField != null && state.name != null) {
            nameField.setText(state.name);
            nameStateApplied = true;
        }
        if (!priorityStateApplied && priorityField != null) {
            priorityField.setText(Integer.toString(state.priority));
            priorityStateApplied = true;
        }
    }

    private void sendPriority() {
        try {
            NetworkHandler.CHANNEL.sendToServer(new PacketWirelessPriority(position, Integer.parseInt(priorityField.getText())));
        } catch (NumberFormatException ignored) {
            PacketWirelessState.State state = currentState();
            priorityField.setText(Integer.toString(state == null ? 0 : state.priority));
        }
    }
}
