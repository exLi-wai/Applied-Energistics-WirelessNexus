package com.lw.ae_wireless_nexus.api;

import java.util.UUID;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public interface IWirelessEndpoint {
    UUID getWirelessNetworkId();
    int getWirelessPriority();
    void setWirelessPriority(int priority);
    int getRequestedWirelessChannels();
    String getWirelessEndpointKey();

    default WirelessLocation getWirelessEndpointLocation() {
        return null;
    }

    /** Name shown by integration GUIs. */
    default ITextComponent getWirelessEndpointDisplayName() {
        return new TextComponentString(getWirelessEndpointKey());
    }
}
