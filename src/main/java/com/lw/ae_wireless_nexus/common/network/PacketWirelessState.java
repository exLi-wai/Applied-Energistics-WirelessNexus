package com.lw.ae_wireless_nexus.common.network;

import java.util.UUID;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import com.lw.ae_wireless_nexus.network.WirelessNetworkRecord;
import com.lw.ae_wireless_nexus.network.WirelessNetworkService;
import com.lw.ae_wireless_nexus.tile.TileWirelessConnector;
import com.lw.ae_wireless_nexus.tile.TileWirelessController;

public final class PacketWirelessState implements IMessage {
    public static volatile State CLIENT_STATE;
    private State state;

    public PacketWirelessState() {
    }

    public PacketWirelessState(State state) { this.state = state; }

    public static void send(EntityPlayerMP player, BlockPos pos, int guiId) {
        TileEntity tile = player.world.getTileEntity(pos);
        State state = new State(guiId, pos, null, "", 0, 0, 0, false);
        if (tile instanceof TileWirelessController) {
            TileWirelessController controller = (TileWirelessController) tile;
            WirelessNetworkRecord record = WirelessNetworkService.getRecord(controller);
            if (record != null) state = new State(guiId, pos, record.getId(), record.getName(), record.getTotalChannels(), record.getAllocatedChannels(), 0, record.isOnline());
        } else if (tile instanceof TileWirelessConnector) {
            TileWirelessConnector connector = (TileWirelessConnector) tile;
            state = new State(guiId, pos, connector.getWirelessNetworkId(), "", 0, 0, connector.getWirelessPriority(), connector.getLeaseStatus() == com.lw.ae_wireless_nexus.network.WirelessLeaseStatus.CONNECTED);
        }
        NetworkHandler.CHANNEL.sendTo(new PacketWirelessState(state), player);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        PacketBuffer p = new PacketBuffer(buffer);
        int gui = p.readVarInt(); BlockPos pos = new BlockPos(p.readInt(), p.readInt(), p.readInt());
        boolean has = p.readBoolean(); UUID id = has ? new UUID(p.readLong(), p.readLong()) : null;
        state = new State(gui, pos, id, p.readString(64), p.readVarInt(), p.readVarInt(), p.readVarInt(), p.readBoolean());
    }
    @Override
    public void toBytes(ByteBuf buffer) {
        PacketBuffer p = new PacketBuffer(buffer); p.writeVarInt(state.guiId);
        p.writeInt(state.pos.getX()); p.writeInt(state.pos.getY()); p.writeInt(state.pos.getZ());
        p.writeBoolean(state.id != null); if (state.id != null) { p.writeLong(state.id.getMostSignificantBits()); p.writeLong(state.id.getLeastSignificantBits()); }
        p.writeString(state.name == null ? "" : state.name); p.writeVarInt(state.total); p.writeVarInt(state.allocated); p.writeVarInt(state.priority); p.writeBoolean(state.online);
    }
    public static final class Handler implements IMessageHandler<PacketWirelessState, IMessage> {
        @Override
        public IMessage onMessage(final PacketWirelessState message, MessageContext context) {
            Minecraft.getMinecraft().addScheduledTask(() -> { CLIENT_STATE = message.state; });
            return null;
        }
    }
    public static final class State {
        public final int guiId, total, allocated, priority;
        public final BlockPos pos;
        public final UUID id;
        public final String name;
        public final boolean online;

        State(int guiId, BlockPos pos, UUID id, String name, int total, int allocated, int priority, boolean online) {
            this.guiId=guiId;
            this.pos=pos;
            this.id=id;
            this.name=name;
            this.total=total;
            this.allocated=allocated;
            this.priority=priority;
            this.online=online;
        }
        public boolean matches(BlockPos other) {
            return pos.equals(other);
        }
    }
}
