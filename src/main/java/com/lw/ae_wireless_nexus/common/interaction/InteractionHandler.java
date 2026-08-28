package com.lw.ae_wireless_nexus.common.interaction;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import com.lw.ae_wireless_nexus.ae_wireless_nexus;
import com.lw.ae_wireless_nexus.common.gui.GuiHandler;
import com.lw.ae_wireless_nexus.tile.TileWirelessConnector;

@Mod.EventBusSubscriber(modid = ae_wireless_nexus.MOD_ID)
public final class InteractionHandler {

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != EnumHand.MAIN_HAND) return;

        TileEntity tile = event.getWorld().getTileEntity(event.getPos());
        EntityPlayer player = event.getEntityPlayer();
        ItemStack held = player.getHeldItemMainhand();

        if (held.getItem() instanceof ItemBlock) return;
        if (event.getWorld().isRemote || player.isSneaking()) return;

        int id = tile instanceof TileWirelessConnector ? GuiHandler.WIRELESS_CONNECTOR : -1;
        if (id < 0) return;
        player.openGui(ae_wireless_nexus.instance, id, event.getWorld(),
            event.getPos().getX(), event.getPos().getY(), event.getPos().getZ());
        event.setCancellationResult(EnumActionResult.SUCCESS);
        event.setCanceled(true);
    }
}
