package com.lw.ae_wireless_nexus.client.gui;

import java.lang.reflect.Method;

import com.lw.ae_wireless_nexus.ae_wireless_nexus;
import com.lw.ae_wireless_nexus.api.WirelessNexusClientApi;
import com.lw.ae_wireless_nexus.integration.mmce.MMCEWirelessEndpoint;
import com.lw.ae_wireless_nexus.misc.Mods;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = ae_wireless_nexus.MOD_ID, value = Side.CLIENT)
public final class MMCEWirelessGuiButtonHandler {
    private static final int BUTTON_ID = 0xA71E;

    private MMCEWirelessGuiButtonHandler() {}

    @SubscribeEvent
    public static void onInitGui(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!Mods.MMCE.isLoaded() || !(event.getGui() instanceof GuiContainer)) return;
        GuiContainer gui = (GuiContainer) event.getGui();
        TileEntity owner = findOwner(gui);
        if (!MMCEWirelessEndpoint.isSupported(owner)) return;
        int width = 76;
        int x = gui.getGuiLeft() + gui.getXSize() - width - 4;
        x = Math.max(2, Math.min(gui.width - width - 2, x));
        int y = Math.max(2, gui.getGuiTop() - 24);
        event.getButtonList().add(new GuiAeTexturedButton(BUTTON_ID, x, y, width,
            I18n.format("gui.ae_wireless_nexus.open_selector")));
    }

    @SubscribeEvent
    public static void onButton(GuiScreenEvent.ActionPerformedEvent.Pre event) {
        GuiButton button = event.getButton();
        if (!Mods.MMCE.isLoaded() || button == null || button.id != BUTTON_ID
            || !(event.getGui() instanceof GuiContainer)) return;
        TileEntity owner = findOwner((GuiContainer) event.getGui());
        if (!MMCEWirelessEndpoint.isSupported(owner)) return;
        WirelessNexusClientApi.requestOpenEndpointSelector(owner.getPos());
        event.setCanceled(true);
    }

    private static TileEntity findOwner(GuiContainer gui) {
        Object container = gui.inventorySlots;
        try {
            Method method = container.getClass().getMethod("getOwner");
            Object owner = method.invoke(container);
            return owner instanceof TileEntity ? (TileEntity) owner : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}
