package com.lw.ae_wireless_nexus.api;

import java.util.UUID;

public interface IWirelessEndpoint {
    UUID getWirelessNetworkId();
    int getWirelessPriority();
    void setWirelessPriority(int priority);
    int getRequestedWirelessChannels();
    String getWirelessEndpointKey();
}
