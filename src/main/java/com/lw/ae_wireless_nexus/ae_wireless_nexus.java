package com.lw.ae_wireless_nexus;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lw.ae_wireless_nexus.registry.ModBlocks;
import com.lw.ae_wireless_nexus.registry.ModItems;
import com.lw.ae_wireless_nexus.common.network.NetworkHandler;
import com.lw.ae_wireless_nexus.common.gui.GuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import com.lw.ae_wireless_nexus.network.WirelessNetworkService;
import com.lw.ae_wireless_nexus.misc.Mods;
import com.lw.ae_wireless_nexus.integration.mmce.MMCEInteractionHandler;
import net.minecraftforge.common.MinecraftForge;

@Mod(
        modid =
        Tags.MOD_ID,
        name = Tags.MOD_NAME,
        version = Tags.VERSION,
    dependencies = "required-after:appliedenergistics2"
)
public class ae_wireless_nexus {

    @Mod.Instance
    public static ae_wireless_nexus instance;

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    public static final String MOD_ID = Tags.MOD_ID;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ModBlocks.init();
        ModItems.init();
        ModBlocks.registerTileEntities();
        NetworkHandler.registerPackets();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        if (Mods.MMCE.isLoaded()) MinecraftForge.EVENT_BUS.register(new MMCEInteractionHandler());
    }

    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
        WirelessNetworkService.clearRuntime();
    }

    public static final CreativeTabs CREATIVE_TAB = new CreativeTabs(MOD_ID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ModBlocks.WIRELESS_CONTROLLER);
        }
    };
}
