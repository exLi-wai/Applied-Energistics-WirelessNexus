package com.lw.ae_wireless_nexus.common.network;

import com.lw.ae_wireless_nexus.api.WirelessNexusApi;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class PacketOpenWirelessEndpoint implements IMessage {
    private BlockPos position;

    public PacketOpenWirelessEndpoint() {}

    public PacketOpenWirelessEndpoint(BlockPos position) { this.position = position; }

    @Override
    public void fromBytes(ByteBuf buffer) {
        position = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(position.getX());
        buffer.writeInt(position.getY());
        buffer.writeInt(position.getZ());
    }

    public static final class Handler implements IMessageHandler<PacketOpenWirelessEndpoint, IMessage> {
        @Override
        public IMessage onMessage(final PacketOpenWirelessEndpoint message,
            final MessageContext context) {
            final EntityPlayerMP player = context.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                WirelessNexusApi.openEndpointSelector(player, player.world, message.position);
            });
            return null;
        }
    }
}
