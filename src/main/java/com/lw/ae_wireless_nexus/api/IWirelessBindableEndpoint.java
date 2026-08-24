package com.lw.ae_wireless_nexus.api;

import java.util.UUID;

public interface IWirelessBindableEndpoint extends IWirelessEndpoint {
    void bindWirelessNetwork(UUID networkId, UUID playerId);
    void unbindWirelessNetwork();
    WirelessEndpointState getWirelessEndpointState();
}
