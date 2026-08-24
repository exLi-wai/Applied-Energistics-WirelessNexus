package com.lw.ae_wireless_nexus.network;

import appeng.api.networking.IGridNode;
import com.lw.ae_wireless_nexus.api.IWirelessBindableEndpoint;
import com.lw.ae_wireless_nexus.tile.TileWirelessController;
import net.minecraft.world.World;

public interface WirelessRuntimeEndpoint extends IWirelessBindableEndpoint {
    World getWirelessEndpointWorld();
    int getWirelessBindingPlayerId();
    IGridNode getWirelessGridNode();
    boolean isWirelessEndpointValid();
    void setWirelessLease(WirelessLeaseStatus status, TileWirelessController controller);
}
