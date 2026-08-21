package com.lw.ae_wireless_nexus.config;

import net.minecraftforge.common.config.Config;
import com.lw.ae_wireless_nexus.Tags;

@Config(modid = Tags.MOD_ID, name = "ae_wireless_nexus")
public final class WirelessConfig {

    @Config.Comment("Wireless channels provided by each exposed controller face")
    @Config.RangeInt(min = 1, max = 128)
    public static int channelsPerExposedFace = 32;

    @Config.Comment("Maximum accepted wireless network name length")
    @Config.RangeInt(min = 1, max = 64)
    public static int maxNetworkNameLength = 32;

    @Config.Comment("Maximum connector priority")
    @Config.RangeInt(min = 0, max = 1000)
    public static int maxEndpointPriority = 100;
}
