package com.lw.ae_wireless_nexus.tile;

import java.util.UUID;

import appeng.api.AEApi;
import appeng.api.exceptions.FailedConnectionException;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.core.worlddata.WorldData;
import appeng.me.helpers.AENetworkProxy;
import appeng.tile.grid.AENetworkTile;
import com.lw.ae_wireless_nexus.config.WirelessConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;

import com.lw.ae_wireless_nexus.ae_wireless_nexus;
import com.lw.ae_wireless_nexus.api.IWirelessBindableEndpoint;
import com.lw.ae_wireless_nexus.api.WirelessEndpointState;
import com.lw.ae_wireless_nexus.api.WirelessLocation;
import com.lw.ae_wireless_nexus.common.gui.ContainerWireless;
import com.lw.ae_wireless_nexus.common.gui.GuiHandler;
import com.lw.ae_wireless_nexus.common.network.PacketWirelessNetworks;
import com.lw.ae_wireless_nexus.common.network.PacketWirelessState;
import com.lw.ae_wireless_nexus.network.WirelessLeaseStatus;
import com.lw.ae_wireless_nexus.network.WirelessRuntimeEndpoint;
import com.lw.ae_wireless_nexus.network.WirelessNetworkService;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class TileWirelessConnector extends AENetworkTile
    implements IWirelessBindableEndpoint, WirelessRuntimeEndpoint, ITickable {

    private UUID targetNetwork;
    private UUID bindingPlayer;
    private int priority;
    private int bindingPlayerId = -1;
    private int requestedChannels;
    private int reconnectTicks;
    private IGridConnection remoteConnection;
    private WirelessLeaseStatus leaseStatus = WirelessLeaseStatus.UNBOUND;

    @Override
    public void onReady() {
        super.onReady();
        WirelessNetworkService.registerConnector(this);
    }

    @Override
    protected AENetworkProxy createProxy() {
        AENetworkProxy proxy = super.createProxy();
        proxy.setFlags(GridFlags.DENSE_CAPACITY);
        return proxy;
    }

    @Override
    public AECableType getCableConnectionType(AEPartLocation side) {
        return AECableType.SMART;
    }

    @Override
    public UUID getWirelessNetworkId() {
        return targetNetwork;
    }

    public void bindToNetwork(UUID networkId) {
        targetNetwork = networkId;
        leaseStatus = WirelessLeaseStatus.CONNECTING;
        markDirty();
    }

    public void bindToNetwork(UUID networkId, UUID player) {
        destroyRemoteConnection();
        bindingPlayer = player;

        EntityPlayer online = world == null || world.getMinecraftServer() == null
            ? null
            : world.getMinecraftServer().getPlayerList().getPlayerByUUID(player);

        bindingPlayerId = online == null
            ? -1
            : WorldData.instance().playerData().getPlayerID(online.getGameProfile());

        IGridNode node = getWirelessGridNode();
        if (node != null && bindingPlayerId >= 0) {
            node.setPlayerID(bindingPlayerId);
        }

        requestedChannels = getMinimumRequestedChannels(node);
        bindToNetwork(networkId);
    }

    public void unbindFromNetwork() {
        destroyRemoteConnection();
        targetNetwork = null;
        bindingPlayer = null;
        bindingPlayerId = -1;
        requestedChannels = 0;
        leaseStatus = WirelessLeaseStatus.UNBOUND;
        markDirty();
    }

    public UUID getBindingPlayer() {
        return bindingPlayer;
    }

    @Override
    public void bindWirelessNetwork(UUID networkId, UUID playerId) {
        bindToNetwork(networkId, playerId);
    }

    @Override
    public void unbindWirelessNetwork() {
        unbindFromNetwork();
    }

    @Override
    public WirelessEndpointState getWirelessEndpointState() {
        return WirelessEndpointState.valueOf(leaseStatus.name());
    }

    public WirelessLeaseStatus getLeaseStatus() {
        return leaseStatus;
    }

    public void setLease(WirelessLeaseStatus status, TileWirelessController target) {
        leaseStatus = status;
        if (status != WirelessLeaseStatus.CONNECTING || target == null) {
            destroyRemoteConnection();
            return;
        }
        if (remoteConnection != null) {
            leaseStatus = WirelessLeaseStatus.CONNECTED;
            return;
        }

        try {
            IGridNode source = getWirelessGridNode();
            IGridNode destination = target.getProxy().getNode();
            if (source != null && destination != null) {
                if (bindingPlayerId >= 0) {
                    source.setPlayerID(bindingPlayerId);
                }

                remoteConnection = AEApi.instance().grid().createGridConnection(source, destination);
                leaseStatus = WirelessLeaseStatus.CONNECTED;
            } else {
                leaseStatus = WirelessLeaseStatus.CONNECTING;
            }
        } catch (FailedConnectionException ex) {
            leaseStatus = WirelessLeaseStatus.CONNECTING;
            ae_wireless_nexus.LOGGER.debug(
                "Wireless connector {} is waiting to reconnect",
                getWirelessEndpointKey(),
                ex);
        }
    }

    @Override
    public void setWirelessLease(WirelessLeaseStatus status, TileWirelessController target) {
        setLease(status, target);
    }

    private void destroyRemoteConnection() {
        if (remoteConnection != null) {
            remoteConnection.destroy();
            remoteConnection = null;
        }
    }

    @Override
    public int getWirelessPriority() {
        return priority;
    }

    @Override
    public void setWirelessPriority(int value) {
        priority = Math.max(
            0,
            Math.min(
                WirelessConfig.maxEndpointPriority,
                value));
        markDirty();
    }

    @Override
    public int getRequestedWirelessChannels() {
        IGridNode node = getWirelessGridNode();
        if (node == null) {
            requestedChannels = 0;
            return 0;
        }

        int minimum = getMinimumRequestedChannels(node);
        int maximum = node.hasFlag(GridFlags.CANNOT_CARRY)
            ? minimum
            : node.hasFlag(GridFlags.DENSE_CAPACITY) ? 32 : 8;

        requestedChannels = remoteConnection == null
            ? Math.max(requestedChannels, minimum)
            : Math.max(minimum, Math.min(maximum, remoteConnection.getUsedChannels()));
        return requestedChannels;
    }

    private int getMinimumRequestedChannels(IGridNode node) {
        return 0;
    }

    @Override
    public IGridNode getWirelessGridNode() {
        return getProxy() == null ? null : getProxy().getNode();
    }

    @Override
    public World getWirelessEndpointWorld() {
        return world;
    }

    @Override
    public int getWirelessBindingPlayerId() {
        return bindingPlayerId;
    }

    @Override
    public boolean isWirelessEndpointValid() {
        return world != null && !world.isRemote && !isInvalid();
    }

    @Override
    public String getWirelessEndpointKey() {
        return world == null
            ? "connector:unloaded"
            : "connector:" + world.provider.getDimension() + ":" + pos.toLong();
    }

    @Override
    public WirelessLocation getWirelessEndpointLocation() {
        return world == null ? null : new WirelessLocation(world.provider.getDimension(), pos);
    }

    @Override
    public ITextComponent getWirelessEndpointDisplayName() {
        return new TextComponentTranslation("tile.ae_wireless_nexus.wireless_connector.name");
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        if (tag.hasKey("WirelessTarget")) {
            try {
                targetNetwork = UUID.fromString(tag.getString("WirelessTarget"));
            } catch (IllegalArgumentException ignored) {
                // Ignore malformed saved UUID.
            }
        }

        priority = Math.max(
            0,
            Math.min(
                WirelessConfig.maxEndpointPriority,
                tag.getInteger("WirelessPriority")));
        bindingPlayerId = tag.hasKey("WirelessPlayerId")
            ? tag.getInteger("WirelessPlayerId")
            : -1;
        requestedChannels = Math.max(
            0,
            Math.min(32, tag.getInteger("WirelessRequestedChannels")));

        if (tag.hasKey("WirelessPlayer")) {
            try {
                bindingPlayer = UUID.fromString(tag.getString("WirelessPlayer"));
            } catch (IllegalArgumentException ignored) {
                // Ignore malformed saved UUID.
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        if (targetNetwork != null) {
            tag.setString("WirelessTarget", targetNetwork.toString());
        }
        tag.setInteger("WirelessPriority", priority);
        tag.setInteger("WirelessPlayerId", bindingPlayerId);
        tag.setInteger("WirelessRequestedChannels", requestedChannels);
        if (bindingPlayer != null) {
            tag.setString("WirelessPlayer", bindingPlayer.toString());
        }
        return tag;
    }

    @Override
    public void invalidate() {
        WirelessNetworkService.unregisterConnector(this);
        destroyRemoteConnection();
        super.invalidate();
    }

    @Override
    public void onChunkUnload() {
        WirelessNetworkService.unregisterConnector(this);
        destroyRemoteConnection();
        super.onChunkUnload();
    }

    @Override
    public void update() {
        if (world == null || world.isRemote) {
            return;
        }

        if (++reconnectTicks >= 20) {
            reconnectTicks = 0;
            WirelessNetworkService.registerConnector(this);
            syncOpenGui();
        }
    }

    private void syncOpenGui() {
        if (world.getMinecraftServer() == null) {
            return;
        }

        for (EntityPlayerMP player : world.getMinecraftServer().getPlayerList().getPlayers()) {
            Container open = player.openContainer;
            if (open instanceof ContainerWireless
                && ((ContainerWireless) open).getPosition().equals(pos)) {
                PacketWirelessState.send(player, pos, GuiHandler.WIRELESS_CONNECTOR);
                PacketWirelessNetworks.send(player);
            }
        }
    }
}
