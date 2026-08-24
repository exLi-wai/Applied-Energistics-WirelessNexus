package com.lw.ae_wireless_nexus.network;

import com.lw.ae_wireless_nexus.integration.mmce.IMMCEWirelessHost;
import com.lw.ae_wireless_nexus.integration.mmce.MMCEWirelessEndpoint;
import com.lw.ae_wireless_nexus.misc.Mods;
import com.lw.ae_wireless_nexus.tile.TileWirelessConnector;
import net.minecraft.tileentity.TileEntity;

public final class WirelessEndpointLookup {

    public static WirelessRuntimeEndpoint find(TileEntity tile) {
        if (tile instanceof TileWirelessConnector) return (TileWirelessConnector) tile;
        if (Mods.MMCE.isLoaded() && tile instanceof IMMCEWirelessHost
            && MMCEWirelessEndpoint.isSupported(tile)) {
            return ((IMMCEWirelessHost) tile).aeWirelessNexus$getWirelessEndpoint();
        }
        return null;
    }
}
