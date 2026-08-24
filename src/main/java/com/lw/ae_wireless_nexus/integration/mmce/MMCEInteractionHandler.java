package com.lw.ae_wireless_nexus.integration.mmce;

import java.util.UUID;

import com.lw.ae_wireless_nexus.ae_wireless_nexus;
import com.lw.ae_wireless_nexus.misc.Mods;
import com.lw.ae_wireless_nexus.network.WirelessNetworkToolBinding;
import com.lw.ae_wireless_nexus.network.WirelessNetworkService;
import com.lw.ae_wireless_nexus.network.WirelessNetworkRecord;
import com.lw.ae_wireless_nexus.network.WirelessNetworkSavedData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public final class MMCEInteractionHandler {
    public MMCEInteractionHandler() {}

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (!Mods.MMCE.isLoaded() || event.getHand() != EnumHand.MAIN_HAND) return;
        EntityPlayer player = event.getEntityPlayer();
        ItemStack held = player.getHeldItemMainhand();

        TileEntity tile = event.getWorld().getTileEntity(event.getPos());
        MMCEWirelessEndpoint endpoint = getEndpoint(tile);
        if (endpoint == null) return;
        if (!WirelessNetworkToolBinding.isWirelessConnectorTool(held)) return;
        if (!event.getWorld().isRemote) {
            WirelessNetworkToolBinding.bindEndpoint(held, endpoint, player);
        }
        event.setCancellationResult(EnumActionResult.SUCCESS);
        event.setCanceled(true);
    }

    public static void bindPlacedComponent(World world, BlockPos position, EntityPlayer player) {
        if (world == null || world.isRemote || player == null) return;
        ItemStack tool = WirelessNetworkToolBinding.findAutomaticBindingTool(player);
        UUID networkId = WirelessNetworkToolBinding.getNetworkId(tool);
        if (networkId == null) return;
        MMCEWirelessEndpoint endpoint = getEndpoint(world.getTileEntity(position));
        if (endpoint != null && WirelessNetworkService.bindEndpointPersisted(endpoint, networkId, player)) {
            ae_wireless_nexus.LOGGER.info("Automatically bound MMCE wireless endpoint {} to {} immediately",
                position, networkId);
            notifyBindingSuccess(player, world, networkId);
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (!Mods.MMCE.isLoaded() || event.phase != TickEvent.Phase.END
            || event.world.isRemote) return;
        if (event.world.getTotalWorldTime() % 20L != 0L) return;
        WirelessNetworkService.refreshRuntime(event.world);
    }

    private static void notifyBindingSuccess(EntityPlayer player, World world, UUID networkId) {
        WirelessNetworkRecord record = WirelessNetworkSavedData.get(world).get(networkId);
        String name = record == null ? networkId.toString() : record.getName();
        player.sendMessage(new TextComponentTranslation(
            "message.ae_wireless_nexus.wireless_tool.mmce_connected", name));
    }

    private static MMCEWirelessEndpoint getEndpoint(TileEntity tile) {
        if (!MMCEWirelessEndpoint.isSupported(tile) || !(tile instanceof IMMCEWirelessHost)) return null;
        return ((IMMCEWirelessHost) tile).aeWirelessNexus$getWirelessEndpoint();
    }
}
