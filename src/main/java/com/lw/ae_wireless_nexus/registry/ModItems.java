package com.lw.ae_wireless_nexus.registry;

import com.lw.ae_wireless_nexus.Tags;
import com.lw.ae_wireless_nexus.ae_wireless_nexus;
import com.lw.ae_wireless_nexus.item.ItemWirelessConnectorTool;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class ModItems {

    public static ItemWirelessConnectorTool WIRELESS_CONNECTOR_TOOL;

    public static void init() {
        WIRELESS_CONNECTOR_TOOL = new ItemWirelessConnectorTool(ae_wireless_nexus.CREATIVE_TAB);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                WIRELESS_CONNECTOR_TOOL
        );
    }
}
