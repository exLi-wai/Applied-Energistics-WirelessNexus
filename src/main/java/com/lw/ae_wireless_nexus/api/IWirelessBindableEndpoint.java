package com.lw.ae_wireless_nexus.api;

import java.util.UUID;

/** Optional endpoint contract for adapters that persist their own binding state. */
public interface IWirelessBindableEndpoint extends IWirelessEndpoint {
    void bindWirelessNetwork(UUID networkId, UUID playerId);
    void unbindWirelessNetwork();
    WirelessEndpointState getWirelessEndpointState();
}
