package com.lw.ae_wireless_nexus.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import com.lw.ae_wireless_nexus.network.WirelessNetworkRecord;
import com.lw.ae_wireless_nexus.network.WirelessNetworkService;
import com.lw.ae_wireless_nexus.network.WirelessEndpointGuiService;
import com.lw.ae_wireless_nexus.network.WirelessEndpointLookup;
import com.lw.ae_wireless_nexus.network.WirelessRuntimeEndpoint;

public final class WirelessNexusApi {
    public static final int API_VERSION = 2;

    private WirelessNexusApi() { }

    public static List<WirelessNetworkView> getVisibleNetworks(World world, EntityPlayer player) {
        if (world == null || player == null || world.isRemote) return Collections.emptyList();
        List<WirelessNetworkView> result = new ArrayList<WirelessNetworkView>();
        for (WirelessNetworkRecord record : WirelessNetworkService.getVisibleNetworks(world, player)) {
            result.add(new WirelessNetworkView(record.getId(), record.getName(),
                record.getTotalChannels(), record.getAllocatedChannels(), record.isOnline(),
                new WirelessLocation(record.getDimension(), record.getX(), record.getY(), record.getZ())));
        }
        return Collections.unmodifiableList(result);
    }

    /** Finds a built-in or optional-integration endpoint at a server-side position. */
    public static IWirelessBindableEndpoint findEndpoint(World world, BlockPos position) {
        if (world == null || position == null) return null;
        return WirelessEndpointLookup.find(world.getTileEntity(position));
    }

    /** Opens the shared selector after validating side, location, distance and endpoint ownership. */
    public static boolean openEndpointSelector(EntityPlayerMP player, World world, BlockPos position) {
        return WirelessEndpointGuiService.open(player, world, position);
    }

    public static boolean bindEndpoint(IWirelessBindableEndpoint endpoint, UUID networkId,
        EntityPlayer player) {
        return endpoint instanceof WirelessRuntimeEndpoint
            && WirelessNetworkService.bindEndpoint((WirelessRuntimeEndpoint) endpoint, networkId, player);
    }

    public static boolean unbindEndpoint(IWirelessBindableEndpoint endpoint, EntityPlayer player) {
        if (!(endpoint instanceof WirelessRuntimeEndpoint)
            || !WirelessNetworkService.canModifyEndpoint((WirelessRuntimeEndpoint) endpoint, player)) return false;
        endpoint.unbindWirelessNetwork();
        WirelessNetworkService.registerEndpoint((WirelessRuntimeEndpoint) endpoint);
        return true;
    }
}
