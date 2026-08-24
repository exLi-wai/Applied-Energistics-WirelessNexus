package com.lw.ae_wireless_nexus.common.network;

import com.lw.ae_wireless_nexus.common.gui.GuiHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import com.lw.ae_wireless_nexus.network.WirelessNetworkService;
import com.lw.ae_wireless_nexus.tile.TileWirelessController;

public final class PacketWirelessRename implements IMessage {

    private BlockPos position;
    private String name;
    public PacketWirelessRename() {}

    public PacketWirelessRename(BlockPos position, String name) {
        this.position = position;
        this.name = name;
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        position = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
        name = new PacketBuffer(buffer).readString(64);
    }
    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(position.getX()); buffer.writeInt(position.getY()); buffer.writeInt(position.getZ());
        new PacketBuffer(buffer).writeString(name == null ? "" : name.substring(0, Math.min(64, name.length())));
    }
    public static final class Handler implements IMessageHandler<PacketWirelessRename, IMessage> {

        @Override
        public IMessage onMessage(final PacketWirelessRename message, final MessageContext context) {
            final EntityPlayerMP player = context.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (message.position == null || player.getDistanceSq(message.position) > 64.0D) return;
                TileEntity tile = player.world.getTileEntity(message.position);
                if (tile instanceof TileWirelessController) {
                    WirelessNetworkService.setNetworkName((TileWirelessController) tile, message.name, player);
                    PacketWirelessState.send(player, message.position, GuiHandler.WIRELESS_CONTROLLER);
                    PacketWirelessNetworks.send(player);
                }
            });
            return null;
        }
    }
}
