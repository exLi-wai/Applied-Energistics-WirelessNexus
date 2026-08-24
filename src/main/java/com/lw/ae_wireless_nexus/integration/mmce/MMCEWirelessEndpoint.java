package com.lw.ae_wireless_nexus.integration.mmce;

import java.util.UUID;

import appeng.api.AEApi;
import appeng.api.exceptions.FailedConnectionException;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.core.worlddata.WorldData;
import com.lw.ae_wireless_nexus.api.WirelessEndpointState;
import com.lw.ae_wireless_nexus.api.WirelessLocation;
import com.lw.ae_wireless_nexus.config.WirelessConfig;
import com.lw.ae_wireless_nexus.network.WirelessLeaseStatus;
import com.lw.ae_wireless_nexus.network.WirelessNetworkService;
import com.lw.ae_wireless_nexus.network.WirelessRuntimeEndpoint;
import com.lw.ae_wireless_nexus.tile.TileWirelessController;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public final class MMCEWirelessEndpoint implements WirelessRuntimeEndpoint {
    private final TileEntity tile;
    private UUID targetNetwork;
    private UUID bindingPlayer;
    private int bindingPlayerId = -1;
    private int priority;
    private int requestedChannels;
    private IGridConnection remoteConnection;
    private WirelessLeaseStatus leaseStatus = WirelessLeaseStatus.UNBOUND;

    public MMCEWirelessEndpoint(TileEntity tile) {
        this.tile = tile;
    }

    public static boolean isSupported(TileEntity tile) {
        if (tile == null || !(tile instanceof IGridProxyable)) return false;
        for (Class<?> type = tile.getClass(); type != null; type = type.getSuperclass()) {
            String name = type.getName();
            if (name.equals("github.kasuminova.mmce.common.tile.MEFluidInputBus")
                || name.equals("github.kasuminova.mmce.common.tile.MEFluidOutputBus")
                || name.equals("github.kasuminova.mmce.common.tile.MEItemInputBus")
                || name.equals("github.kasuminova.mmce.common.tile.MEItemOutputBus")
                || name.equals("github.kasuminova.mmce.common.tile.MEGasInputBus")
                || name.equals("github.kasuminova.mmce.common.tile.MEGasOutputBus")
                || name.equals("github.kasuminova.mmce.common.tile.MEPatternProvider")) return true;
        }
        return false;
    }

    @Override
    public UUID getWirelessNetworkId() { return targetNetwork; }

    @Override
    public int getWirelessPriority() { return priority; }

    @Override
    public void setWirelessPriority(int value) {
        priority = Math.max(0, Math.min(
            WirelessConfig.maxEndpointPriority, value));
        tile.markDirty();
        WirelessNetworkService.registerEndpoint(this);
    }

    @Override
    public int getRequestedWirelessChannels() {
        IGridNode node = getWirelessGridNode();
        int minimum = getChannelUsage(null, node);
        requestedChannels = remoteConnection == null
            ? Math.max(requestedChannels, minimum)
            : getChannelUsage(remoteConnection, node);
        return requestedChannels;
    }

    @Override
    public String getWirelessEndpointKey() {
        World world = getWirelessEndpointWorld();
        return world == null ? "mmce:unloaded:" + System.identityHashCode(tile)
            : "mmce:" + world.provider.getDimension() + ":" + tile.getPos().toLong();
    }

    @Override
    public World getWirelessEndpointWorld() { return tile.getWorld(); }

    @Override
    public WirelessLocation getWirelessEndpointLocation() {
        World world = getWirelessEndpointWorld();
        return world == null ? null : new WirelessLocation(world.provider.getDimension(), tile.getPos());
    }

    @Override
    public ITextComponent getWirelessEndpointDisplayName() {
        return new TextComponentTranslation(tile.getBlockType().getTranslationKey() + ".name");
    }

    @Override
    public int getWirelessBindingPlayerId() { return bindingPlayerId; }

    @Override
    public IGridNode getWirelessGridNode() {
        if (!isSupported(tile)) return null;
        AENetworkProxy proxy = ((IGridProxyable) tile).getProxy();
        return proxy == null ? null : proxy.getNode();
    }

    @Override
    public boolean isWirelessEndpointValid() {
        return isSupported(tile) && getWirelessEndpointWorld() != null
            && !getWirelessEndpointWorld().isRemote && !tile.isInvalid();
    }

    @Override
    public void bindWirelessNetwork(UUID networkId, UUID playerId) {
        destroyRemoteConnection();
        targetNetwork = networkId;
        bindingPlayer = playerId;
        bindingPlayerId = -1;
        World world = getWirelessEndpointWorld();
        if (world != null && world.getMinecraftServer() != null && playerId != null) {
            EntityPlayer online = world.getMinecraftServer().getPlayerList().getPlayerByUUID(playerId);
            if (online != null) bindingPlayerId = WorldData.instance().playerData()
                .getPlayerID(online.getGameProfile());
        }
        IGridNode node = getWirelessGridNode();
        if (node != null && bindingPlayerId >= 0) node.setPlayerID(bindingPlayerId);
        requestedChannels = getChannelUsage(null, node);
        leaseStatus = WirelessLeaseStatus.CONNECTING;
        tile.markDirty();
    }

    @Override
    public void unbindWirelessNetwork() {
        WirelessNetworkService.unregisterEndpoint(this);
        destroyRemoteConnection();
        targetNetwork = null;
        bindingPlayer = null;
        bindingPlayerId = -1;
        requestedChannels = 0;
        leaseStatus = WirelessLeaseStatus.UNBOUND;
        tile.markDirty();
    }

    @Override
    public WirelessEndpointState getWirelessEndpointState() {
        return WirelessEndpointState.valueOf(leaseStatus.name());
    }

    @Override
    public void setWirelessLease(WirelessLeaseStatus status, TileWirelessController controller) {
        leaseStatus = status;
        if (status != WirelessLeaseStatus.CONNECTING || controller == null) {
            destroyRemoteConnection();
            return;
        }
        if (remoteConnection != null) {
            leaseStatus = WirelessLeaseStatus.CONNECTED;
            return;
        }
        try {
            IGridNode source = getWirelessGridNode();
            IGridNode target = controller.getProxy().getNode();
            if (source != null && target != null) {
                if (bindingPlayerId >= 0) source.setPlayerID(bindingPlayerId);
                remoteConnection = AEApi.instance().grid().createGridConnection(source, target);
                leaseStatus = WirelessLeaseStatus.CONNECTED;
            }
        } catch (FailedConnectionException ignored) {
            leaseStatus = WirelessLeaseStatus.CONNECTING;
        }
    }

    public void validate() {
        WirelessNetworkService.registerEndpoint(this);
    }

    public void unload() {
        WirelessNetworkService.unregisterEndpoint(this);
        destroyRemoteConnection();
    }

    public void readFromNBT(NBTTagCompound tag) {
        if (tag.hasKey("AEWirelessNexusTarget")) {
            try { targetNetwork = UUID.fromString(tag.getString("AEWirelessNexusTarget")); }
            catch (IllegalArgumentException ignored) { targetNetwork = null; }
        }
        if (tag.hasKey("AEWirelessNexusPlayer")) {
            try { bindingPlayer = UUID.fromString(tag.getString("AEWirelessNexusPlayer")); }
            catch (IllegalArgumentException ignored) { bindingPlayer = null; }
        }
        bindingPlayerId = tag.hasKey("AEWirelessNexusPlayerId")
            ? tag.getInteger("AEWirelessNexusPlayerId") : -1;
        priority = Math.max(0, Math.min(
            WirelessConfig.maxEndpointPriority,
            tag.getInteger("AEWirelessNexusPriority")));
        requestedChannels = Math.max(0, Math.min(32,
            tag.getInteger("AEWirelessNexusRequestedChannels")));
    }

    public void writeToNBT(NBTTagCompound tag) {
        if (targetNetwork != null) tag.setString("AEWirelessNexusTarget", targetNetwork.toString());
        if (bindingPlayer != null) tag.setString("AEWirelessNexusPlayer", bindingPlayer.toString());
        tag.setInteger("AEWirelessNexusPlayerId", bindingPlayerId);
        tag.setInteger("AEWirelessNexusPriority", priority);
        tag.setInteger("AEWirelessNexusRequestedChannels", requestedChannels);
    }

    private void destroyRemoteConnection() {
        if (remoteConnection != null) {
            remoteConnection.destroy();
            remoteConnection = null;
        }
    }

    private static int getChannelUsage(IGridConnection connection, IGridNode node) {
        if (node == null) return 0;
        int minimum = node.hasFlag(GridFlags.REQUIRE_CHANNEL) ? 1 : 0;
        int maximum;
        if (node.hasFlag(GridFlags.CANNOT_CARRY)) maximum = minimum;
        else maximum = node.hasFlag(GridFlags.DENSE_CAPACITY) ? 32 : 8;
        int used = connection == null
            ? minimum
            : Math.max(minimum, connection.getUsedChannels());
        return Math.max(0, Math.min(maximum, used));
    }
}
