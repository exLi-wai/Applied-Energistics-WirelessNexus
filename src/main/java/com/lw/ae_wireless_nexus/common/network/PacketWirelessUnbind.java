package com.lw.ae_wireless_nexus.common.network;

import com.lw.ae_wireless_nexus.common.gui.GuiHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import com.lw.ae_wireless_nexus.tile.TileWirelessConnector;
import com.lw.ae_wireless_nexus.network.WirelessEndpointLookup;
import com.lw.ae_wireless_nexus.network.WirelessNetworkService;
import com.lw.ae_wireless_nexus.network.WirelessRuntimeEndpoint;
import com.lw.ae_wireless_nexus.network.WirelessEndpointGuiService;

public final class PacketWirelessUnbind implements IMessage {
    private BlockPos position;

    public PacketWirelessUnbind() {}

    public PacketWirelessUnbind(BlockPos position) {
        this.position = position;
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        position = new BlockPos(buffer.readInt(),
                buffer.readInt(),
                buffer.readInt());
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(position.getX());
        buffer.writeInt(position.getY());
        buffer.writeInt(position.getZ());
    }

    public static final class Handler implements IMessageHandler<PacketWirelessUnbind, IMessage> {
        @Override
        public IMessage onMessage(final PacketWirelessUnbind message, final MessageContext context) {
            final EntityPlayerMP player = context.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (message.position == null) return;
                TileEntity tile = player.world.getTileEntity(message.position);
                WirelessRuntimeEndpoint connector = WirelessEndpointGuiService.findAccessible(
                    player, player.world, message.position);
                if (connector != null) {
                    if (WirelessNetworkService.canModifyEndpoint(connector, player)) {
                        connector.unbindWirelessNetwork();
                        WirelessNetworkService.registerEndpoint(connector);
                    }
                    int gui = tile instanceof TileWirelessConnector
                        ? GuiHandler.WIRELESS_CONNECTOR
                        : GuiHandler.WIRELESS_ENDPOINT;
                    PacketWirelessState.send(player, message.position, gui);
                    PacketWirelessNetworks.send(player);
                }
            });
            return null;
        }
    }
}
