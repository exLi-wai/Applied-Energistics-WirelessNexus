package com.lw.ae_wireless_nexus.api;

import com.lw.ae_wireless_nexus.common.network.NetworkHandler;
import com.lw.ae_wireless_nexus.common.network.PacketOpenWirelessEndpoint;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class WirelessNexusClientApi {
    private WirelessNexusClientApi() {}

    public static void requestOpenEndpointSelector(BlockPos position) {
        if (position != null) {
            NetworkHandler.CHANNEL.sendToServer(new PacketOpenWirelessEndpoint(position));
        }
    }
}
