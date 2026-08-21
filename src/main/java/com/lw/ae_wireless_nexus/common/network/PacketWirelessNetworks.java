package com.lw.ae_wireless_nexus.common.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import com.lw.ae_wireless_nexus.network.WirelessNetworkRecord;
import com.lw.ae_wireless_nexus.network.WirelessNetworkService;

public final class PacketWirelessNetworks implements IMessage {
    public static final List<Entry> CLIENT_ENTRIES = new ArrayList<Entry>();
    private List<Entry> entries = new ArrayList<Entry>();
    public PacketWirelessNetworks() {}
    public PacketWirelessNetworks(World world, EntityPlayer player) {
        for (WirelessNetworkRecord record : WirelessNetworkService.getVisibleNetworks(world, player)) {
            entries.add(new Entry(record.getId(), record.getName(), record.getTotalChannels(), record.getAllocatedChannels()));
        }
    }
    @Override
    public void fromBytes(ByteBuf buffer) {
        PacketBuffer packet = new PacketBuffer(buffer);
        int size = Math.min(64, packet.readVarInt());
        entries = new ArrayList<Entry>(size);
        for (int i = 0; i < size; i++) {
            entries.add(new Entry(new UUID(packet.readLong(), packet.readLong()), packet.readString(32), packet.readVarInt(), packet.readVarInt()));
        }
    }
    @Override
    public void toBytes(ByteBuf buffer) {
        PacketBuffer packet = new PacketBuffer(buffer);
        packet.writeVarInt(Math.min(64, entries.size()));
        for (int i = 0; i < Math.min(64, entries.size()); i++) {
            Entry entry = entries.get(i);
            packet.writeLong(entry.id.getMostSignificantBits()); packet.writeLong(entry.id.getLeastSignificantBits());
            packet.writeString(entry.name); packet.writeVarInt(entry.total); packet.writeVarInt(entry.allocated);
        }
    }

    public static void send(EntityPlayerMP player) {
        NetworkHandler.CHANNEL.sendTo(new PacketWirelessNetworks(player.world, player), player);
    }

    public static final class Handler implements IMessageHandler<PacketWirelessNetworks, IMessage> {
        @Override
        public IMessage onMessage(final PacketWirelessNetworks message, MessageContext context) {
            Minecraft.getMinecraft().addScheduledTask(() -> { synchronized (CLIENT_ENTRIES) { CLIENT_ENTRIES.clear(); CLIENT_ENTRIES.addAll(message.entries); } });
            return null;
        }
    }
    public static final class Entry {
        public final UUID id; public final String name; public final int total; public final int allocated;
        public Entry(UUID id, String name, int total, int allocated) { this.id = id; this.name = name; this.total = total; this.allocated = allocated; }
    }
}
