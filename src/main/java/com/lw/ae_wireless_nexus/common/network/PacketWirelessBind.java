package com.lw.ae_wireless_nexus.common.network;

import java.util.UUID;

import com.lw.ae_wireless_nexus.common.gui.GuiHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import com.lw.ae_wireless_nexus.network.WirelessNetworkService;
import com.lw.ae_wireless_nexus.tile.TileWirelessConnector;
import com.lw.ae_wireless_nexus.network.WirelessEndpointLookup;
import com.lw.ae_wireless_nexus.network.WirelessRuntimeEndpoint;
import com.lw.ae_wireless_nexus.network.WirelessEndpointGuiService;

public final class PacketWirelessBind implements IMessage {
    private BlockPos connectorPos;
    private UUID networkId;

    public PacketWirelessBind() {}

    public PacketWirelessBind(BlockPos connectorPos, UUID networkId) {
        this.connectorPos = connectorPos;
        this.networkId = networkId;
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        connectorPos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
        networkId = new UUID(buffer.readLong(), buffer.readLong());
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(connectorPos.getX());
        buffer.writeInt(connectorPos.getY());
        buffer.writeInt(connectorPos.getZ());
        buffer.writeLong(networkId.getMostSignificantBits());
        buffer.writeLong(networkId.getLeastSignificantBits());
    }

    public static final class Handler implements IMessageHandler<PacketWirelessBind, IMessage> {
        @Override
        public IMessage onMessage(final PacketWirelessBind message, final MessageContext context) {
            final EntityPlayerMP player = context.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (message.connectorPos == null || message.networkId == null) return;
                TileEntity tile = player.world.getTileEntity(message.connectorPos);
                WirelessRuntimeEndpoint endpoint = WirelessEndpointGuiService.findAccessible(
                    player, player.world, message.connectorPos);
                if (endpoint != null) {
                    WirelessNetworkService.bindEndpoint(endpoint, message.networkId, player);
                    int gui = tile instanceof TileWirelessConnector
                        ? GuiHandler.WIRELESS_CONNECTOR
                        : GuiHandler.WIRELESS_ENDPOINT;
                    PacketWirelessState.send(player, message.connectorPos, gui);
                    PacketWirelessNetworks.send(player);
                }
            });
            return null;
        }
    }
}
