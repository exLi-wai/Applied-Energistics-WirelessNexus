package com.lw.ae_wireless_nexus.registry;

import com.lw.ae_wireless_nexus.Tags;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import com.lw.ae_wireless_nexus.ae_wireless_nexus;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class ClientRegistration {

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        register(Item.getItemFromBlock(ModBlocks.WIRELESS_CONTROLLER), "wireless_controller");
        register(Item.getItemFromBlock(ModBlocks.WIRELESS_CONNECTOR), "wireless_connector");
        register(ModItems.WIRELESS_CONNECTOR_TOOL, "wireless_connector_tool");
    }

    private static void register(Item item, String name) {
        ModelLoader.setCustomModelResourceLocation(item, 0,
            new ModelResourceLocation(ae_wireless_nexus.MOD_ID + ":" + name, "inventory"));
    }
}
