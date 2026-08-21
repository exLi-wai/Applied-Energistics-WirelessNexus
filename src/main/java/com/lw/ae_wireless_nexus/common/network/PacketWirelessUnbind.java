package com.lw.ae_wireless_nexus.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import com.lw.ae_wireless_nexus.tile.TileWirelessConnector;

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
                if (message.position == null || player.getDistanceSq(message.position) > 64.0D) return;
                TileEntity tile = player.world.getTileEntity(message.position);
                if (tile instanceof TileWirelessConnector) {
                    TileWirelessConnector connector = (TileWirelessConnector) tile;
                    if (connector.getBindingPlayer() == null || connector.getBindingPlayer().equals(player.getUniqueID())) {
                        connector.unbindFromNetwork();
                        com.lw.ae_wireless_nexus.network.WirelessNetworkService.registerConnector(connector);
                    }
                    PacketWirelessState.send(player, message.position, com.lw.ae_wireless_nexus.common.gui.GuiHandler.WIRELESS_CONNECTOR);
                    PacketWirelessNetworks.send(player);
                }
            });
            return null;
        }
    }
}
