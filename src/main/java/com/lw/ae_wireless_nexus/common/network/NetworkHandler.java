package com.lw.ae_wireless_nexus.common.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class NetworkHandler {
    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("ae_wireless_nexus");
    private static int packetId = 0;

    public static void registerPackets() {
        CHANNEL.registerMessage(PacketWirelessBind.Handler.class, PacketWirelessBind.class, packetId++, Side.SERVER);
        CHANNEL.registerMessage(PacketWirelessUnbind.Handler.class, PacketWirelessUnbind.class, packetId++, Side.SERVER);
        CHANNEL.registerMessage(PacketWirelessRename.Handler.class, PacketWirelessRename.class, packetId++, Side.SERVER);
        CHANNEL.registerMessage(PacketWirelessPriority.Handler.class, PacketWirelessPriority.class, packetId++, Side.SERVER);
        CHANNEL.registerMessage(PacketWirelessNetworks.Handler.class, PacketWirelessNetworks.class, packetId++, Side.CLIENT);
        CHANNEL.registerMessage(PacketWirelessState.Handler.class, PacketWirelessState.class, packetId++, Side.CLIENT);
    }
}
