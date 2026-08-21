package com.lw.ae_wireless_nexus.registry;

import com.lw.ae_wireless_nexus.Tags;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import com.lw.ae_wireless_nexus.tile.TileWirelessConnector;
import com.lw.ae_wireless_nexus.tile.TileWirelessController;
import com.lw.ae_wireless_nexus.block.BlockWirelessConnector;
import com.lw.ae_wireless_nexus.block.BlockWirelessController;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class ModBlocks {

    public static BlockWirelessController WIRELESS_CONTROLLER;
    public static BlockWirelessConnector WIRELESS_CONNECTOR;

    public static void init(){
        WIRELESS_CONNECTOR = new BlockWirelessConnector();
        WIRELESS_CONTROLLER = new BlockWirelessController();
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(
                WIRELESS_CONTROLLER,
                WIRELESS_CONNECTOR
        );
    }

    public static void registerTileEntities() {
        GameRegistry.registerTileEntity(TileWirelessController.class, Objects.requireNonNull(WIRELESS_CONTROLLER.getRegistryName()));
        GameRegistry.registerTileEntity(TileWirelessConnector.class, Objects.requireNonNull(WIRELESS_CONNECTOR.getRegistryName()));
    }

    @SubscribeEvent
    public static void registerItemBlock(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(registerItemBlocks(WIRELESS_CONTROLLER));
        event.getRegistry().register(registerItemBlocks(WIRELESS_CONNECTOR));
    }

    public static Item registerItemBlocks(Block block){
        return new ItemBlock(block).setRegistryName(Objects.requireNonNull(block.getRegistryName()));
    }
}
