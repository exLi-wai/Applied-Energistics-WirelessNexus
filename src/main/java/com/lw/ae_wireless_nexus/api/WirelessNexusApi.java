package com.lw.ae_wireless_nexus.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import com.lw.ae_wireless_nexus.network.WirelessNetworkRecord;
import com.lw.ae_wireless_nexus.network.WirelessNetworkService;

public final class WirelessNexusApi {
    public static final int API_VERSION = 1;

    private WirelessNexusApi() { }

    public static List<WirelessNetworkView> getVisibleNetworks(World world, EntityPlayer player) {
        if (world == null || player == null || world.isRemote) return Collections.emptyList();
        List<WirelessNetworkView> result = new ArrayList<WirelessNetworkView>();
        for (WirelessNetworkRecord record : WirelessNetworkService.getVisibleNetworks(world, player)) {
            result.add(new WirelessNetworkView(record.getId(), record.getName(), record.getTotalChannels(), record.getAllocatedChannels(), record.isOnline()));
        }
        return Collections.unmodifiableList(result);
    }
}
