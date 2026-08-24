package com.lw.ae_wireless_nexus.network;

import com.lw.ae_wireless_nexus.ae_wireless_nexus;
import com.lw.ae_wireless_nexus.api.WirelessLocation;
import com.lw.ae_wireless_nexus.common.gui.GuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class WirelessEndpointGuiService {
    public static final double MAX_DISTANCE_SQ = 64.0D;

    private WirelessEndpointGuiService() {}

    public static boolean open(EntityPlayerMP player, World world, BlockPos position) {
        WirelessRuntimeEndpoint endpoint = findAccessible(player, world, position);
        if (endpoint == null) return false;
        player.openGui(ae_wireless_nexus.instance, GuiHandler.WIRELESS_ENDPOINT,
            world, position.getX(), position.getY(), position.getZ());
        return true;
    }

    public static WirelessRuntimeEndpoint findAccessible(EntityPlayer player, World world,
        BlockPos position) {
        if (player == null || world == null || position == null || world.isRemote
            || player.world != world || player.getDistanceSq(position) > MAX_DISTANCE_SQ) return null;
        WirelessRuntimeEndpoint endpoint = WirelessEndpointLookup.find(world.getTileEntity(position));
        if (!matchesLocation(endpoint, world, position) || !endpoint.isWirelessEndpointValid()) return null;
        return endpoint.getWirelessNetworkId() == null
            || WirelessNetworkService.canModifyEndpoint(endpoint, player) ? endpoint : null;
    }

    public static boolean isEndpointAt(World world, BlockPos position) {
        if (world == null || position == null) return false;
        WirelessRuntimeEndpoint endpoint = WirelessEndpointLookup.find(world.getTileEntity(position));
        return matchesLocation(endpoint, world, position);
    }

    private static boolean matchesLocation(WirelessRuntimeEndpoint endpoint, World world,
        BlockPos position) {
        if (endpoint == null || endpoint.getWirelessEndpointWorld() != world) return false;
        WirelessLocation location = endpoint.getWirelessEndpointLocation();
        return location == null || (location.getDimension() == world.provider.getDimension()
            && location.getBlockPos().equals(position));
    }
}
