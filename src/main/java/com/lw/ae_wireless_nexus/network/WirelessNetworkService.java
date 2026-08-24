package com.lw.ae_wireless_nexus.network;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

import com.lw.ae_wireless_nexus.api.WirelessEndpointState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGridHost;
import appeng.api.networking.pathing.ControllerState;
import appeng.api.networking.security.ISecurityGrid;
import appeng.me.GridAccessException;
import appeng.tile.networking.TileController;
import appeng.core.worlddata.WorldData;
import com.lw.ae_wireless_nexus.tile.TileWirelessController;
import com.lw.ae_wireless_nexus.tile.TileWirelessConnector;
import com.lw.ae_wireless_nexus.config.WirelessConfig;
import com.lw.ae_wireless_nexus.ae_wireless_nexus;
import net.minecraftforge.common.DimensionManager;

public final class WirelessNetworkService {
    public static final int CHANNELS_PER_EXPOSED_FACE = 32;
    private WirelessNetworkService() {}
    private static final Map<String, WirelessRuntimeEndpoint> ENDPOINTS = new HashMap<String, WirelessRuntimeEndpoint>();

    public static boolean hasMultipleWirelessControllers(Iterable<? extends TileController> controllers) {
        int found = 0;
        if (controllers != null) for (TileController controller : controllers) {
            if (controller instanceof TileWirelessController && ++found > 1) return true;
        }
        return false;
    }

    public static void onControllerChanged(TileWirelessController controller) {
        if (!isServer(controller)) return;
        List<TileController> group = collectControllers(controller);
        if (group.isEmpty()) return;
        WirelessNetworkSavedData data = WirelessNetworkSavedData.get(controller.getWorld());
        TileWirelessController wireless = null;
        int count = 0;
        for (TileController tile : group) if (tile instanceof TileWirelessController) { wireless = (TileWirelessController) tile; count++; }
        if (wireless == null) return;
        WirelessNetworkRecord record = data.getOrCreate(wireless.getNetworkId());
        record.setAnchor(
            controller.getWorld().provider.getDimension(),
            wireless.getPos().getX(),
            wireless.getPos().getY(),
            wireless.getPos().getZ());
        boolean conflict = count != 1 || hasControllerConflict(controller);
        record.setTotalChannels(conflict ? 0 : scanCapacity(group));
        record.setOnline(!conflict);
        data.markDirty();
        allocate(controller.getWorld());
    }

    public static void onControllerRemoved(TileWirelessController controller) {
        if (!isServer(controller)) return;
        Set<TileWirelessController> neighbours = new HashSet<TileWirelessController>();
        for (EnumFacing side : EnumFacing.values()) {
            TileEntity tile = controller.getWorld().getTileEntity(controller.getPos().offset(side));
            if (tile instanceof TileController) {
                for (TileController member : collectControllersFrom((TileController) tile)) {
                    if (member instanceof TileWirelessController) neighbours.add((TileWirelessController) member);
                }
            }
        }
        WirelessNetworkSavedData data = WirelessNetworkSavedData.get(controller.getWorld());
        data.remove(controller.getNetworkId());
        data.markDirty();
        for (TileWirelessController neighbour : neighbours) onControllerChanged(neighbour);
        allocate(controller.getWorld());
    }

    public static void onControllerUnloaded(TileWirelessController controller) {
        if (!isServer(controller)) return;
        WirelessNetworkRecord record = WirelessNetworkSavedData.get(controller.getWorld()).get(controller.getNetworkId());
        if (record != null) { record.setOnline(false); record.setTotalChannels(0); }
        allocate(controller.getWorld());
    }

    public static String getNetworkName(TileWirelessController controller) {
        WirelessNetworkSavedData data = WirelessNetworkSavedData.get(controller.getWorld());
        WirelessNetworkRecord record = data.getOrCreate(controller.getNetworkId());
        return record.getName();
    }

    public static WirelessNetworkRecord getRecord(TileWirelessController controller) {
        if (controller == null || controller.getWorld() == null) return null;
        return WirelessNetworkSavedData.get(controller.getWorld()).get(controller.getNetworkId());
    }

    public static void setNetworkName(TileWirelessController controller, String name) {
        if (!isServer(controller)) return;
        WirelessNetworkSavedData.get(controller.getWorld()).getOrCreate(controller.getNetworkId()).setName(name);
        WirelessNetworkSavedData.get(controller.getWorld()).markDirty();
    }
    public static boolean setNetworkName(TileWirelessController controller, String name, EntityPlayer player) {
        if (!isServer(controller) || player == null || !hasPermission(controller, player)) return false;
        setNetworkName(controller, name);
        return true;
    }

    public static boolean hasPermission(TileWirelessController controller, EntityPlayer player) {
        if (controller == null || player == null) return false;
        try {
            ISecurityGrid security = controller.getProxy().getSecurity();
            return security.hasPermission(player, SecurityPermissions.BUILD);
        } catch (GridAccessException ex) {
            return true;
        }
    }

    public static boolean bindConnector(TileWirelessConnector connector,
        UUID networkId, EntityPlayer player) {
        return bindEndpoint(connector, networkId, player);
    }

    public static boolean bindEndpoint(WirelessRuntimeEndpoint endpoint,
        UUID networkId, EntityPlayer player) {
        if (endpoint == null || networkId == null || player == null) return false;
        if (endpoint.getWirelessNetworkId() != null && !canModifyEndpoint(endpoint, player)) return false;
        World world = endpoint.getWirelessEndpointWorld();
        if (world == null || world.isRemote) return false;
        WirelessNetworkRecord record = WirelessNetworkSavedData.get(world).get(networkId);
        TileWirelessController controller = findController(record);
        if (record == null || !record.isOnline() || controller == null
            || !hasPermission(controller, player)) return false;
        endpoint.bindWirelessNetwork(networkId, player.getUniqueID());
        registerEndpoint(endpoint);
        return true;
    }


    public static boolean bindEndpointPersisted(WirelessRuntimeEndpoint endpoint,
        UUID networkId, EntityPlayer player) {
        if (endpoint == null || networkId == null || player == null) {
            ae_wireless_nexus.LOGGER.info("Persisted wireless bind rejected: missing endpoint/network/player");
            return false;
        }
        World world = endpoint.getWirelessEndpointWorld();
        if (world == null || world.isRemote) {
            ae_wireless_nexus.LOGGER.info("Persisted wireless bind rejected: invalid world (remote={})",
                world != null && world.isRemote);
            return false;
        }
        if (endpoint.getWirelessNetworkId() != null && !canModifyEndpoint(endpoint, player)) {
            ae_wireless_nexus.LOGGER.info("Persisted wireless bind rejected: player {} cannot modify endpoint {}",
                player.getName(), endpoint.getWirelessEndpointKey());
            return false;
        }
        WirelessNetworkRecord record = WirelessNetworkSavedData.get(world).get(networkId);
        if (record == null) {
            ae_wireless_nexus.LOGGER.info("Persisted wireless bind rejected: network {} not found in dimension {}",
                networkId, world.provider.getDimension());
            return false;
        }
        TileWirelessController controller = findController(record);
        if (controller != null && !hasPermission(controller, player)) {
            ae_wireless_nexus.LOGGER.info("Persisted wireless bind rejected: player {} lacks permission for {}",
                player.getName(), networkId);
            return false;
        }
        endpoint.bindWirelessNetwork(networkId, player.getUniqueID());
        registerEndpoint(endpoint);
        return true;
    }

    public static boolean canModifyEndpoint(WirelessRuntimeEndpoint endpoint,
        EntityPlayer player) {
        if (endpoint == null || player == null) return false;
        int playerId = WorldData.instance().playerData().getPlayerID(player.getGameProfile());
        if (playerId >= 0 && playerId == endpoint.getWirelessBindingPlayerId()) return true;
        World world = endpoint.getWirelessEndpointWorld();
        UUID networkId = endpoint.getWirelessNetworkId();
        if (world == null || networkId == null) return false;
        WirelessNetworkRecord record = WirelessNetworkSavedData.get(world).get(networkId);
        TileWirelessController controller = findController(record);
        return controller != null && hasPermission(controller, player);
    }

    public static void registerConnector(TileWirelessConnector connector) {
        registerEndpoint(connector);
    }

    public static void unregisterConnector(TileWirelessConnector connector) {
        unregisterEndpoint(connector);
    }

    public static void registerEndpoint(WirelessRuntimeEndpoint endpoint) {
        if (endpoint == null || endpoint.getWirelessEndpointWorld() == null
            || endpoint.getWirelessEndpointWorld().isRemote || !endpoint.isWirelessEndpointValid()) return;
        ENDPOINTS.put(endpoint.getWirelessEndpointKey(), endpoint);
        allocate(endpoint.getWirelessEndpointWorld());
    }

    public static void unregisterEndpoint(WirelessRuntimeEndpoint endpoint) {
        if (endpoint == null) return;
        ENDPOINTS.remove(endpoint.getWirelessEndpointKey());
        endpoint.setWirelessLease(WirelessLeaseStatus.UNBOUND, null);
    }

    private static void allocate(World world) {
        if (world == null || world.isRemote) return;
        WirelessNetworkSavedData data = WirelessNetworkSavedData.get(world);
        for (WirelessNetworkRecord record : data.records()) record.setAllocatedChannels(0);
        List<WirelessRuntimeEndpoint> connectors = new ArrayList<WirelessRuntimeEndpoint>(ENDPOINTS.values());
        Collections.sort(connectors, new Comparator<WirelessRuntimeEndpoint>() {
            @Override
            public int compare(WirelessRuntimeEndpoint a, WirelessRuntimeEndpoint b) {
                int priority = Integer.compare(b.getWirelessPriority(), a.getWirelessPriority());
                return priority != 0 ? priority : a.getWirelessEndpointKey().compareTo(b.getWirelessEndpointKey());
            }
        });
        for (WirelessRuntimeEndpoint connector : connectors) {
            UUID id = connector.getWirelessNetworkId();
            if (id == null) { connector.setWirelessLease(WirelessLeaseStatus.UNBOUND, null); continue; }
            WirelessNetworkRecord record = data.get(id);
            TileWirelessController target = findController(record);
            if (record == null || target == null) { connector.setWirelessLease(WirelessLeaseStatus.TARGET_OFFLINE, null); continue; }
            if (!hasPermission(target, connector.getWirelessBindingPlayerId())) { connector.setWirelessLease(WirelessLeaseStatus.NO_PERMISSION, null); continue; }
            int requested = Math.min(Math.max(1, WirelessConfig.channelsPerExposedFace),
                Math.max(0, connector.getRequestedWirelessChannels()));
            if (record.getAllocatedChannels() + requested <= record.getTotalChannels()) {
                connector.setWirelessLease(WirelessLeaseStatus.CONNECTING, target);
                if (connector.getWirelessEndpointState() == WirelessEndpointState.CONNECTED) {
                    record.setAllocatedChannels(record.getAllocatedChannels() + requested);
                }
            } else connector.setWirelessLease(WirelessLeaseStatus.CAPACITY_EXHAUSTED, null);
        }
        data.markDirty();
    }

    public static void clearRuntime() {
        for (WirelessRuntimeEndpoint connector : new ArrayList<WirelessRuntimeEndpoint>(ENDPOINTS.values())) {
            connector.setWirelessLease(WirelessLeaseStatus.UNBOUND, null);
        }
        ENDPOINTS.clear();
    }

    public static void refreshRuntime(World world) {
        allocate(world);
    }

    private static boolean hasPermission(TileWirelessController controller, int playerId) {
        if (controller == null || playerId < 0) return false;
        try {
            ISecurityGrid security = controller.getProxy().getSecurity();
            return security.hasPermission(playerId, SecurityPermissions.BUILD);
        } catch (GridAccessException ex) {
            return true;
        }
    }

    private static TileWirelessController findController(WirelessNetworkRecord record) {
        if (record == null || !record.isOnline()) return null;
        World world = DimensionManager.getWorld(record.getDimension());
        if (world == null) return null;
        TileEntity tile = world.getTileEntity(new BlockPos(record.getX(), record.getY(), record.getZ()));
        return tile instanceof TileWirelessController ? (TileWirelessController) tile : null;
    }

    public static List<WirelessNetworkRecord> getVisibleNetworks(World world, EntityPlayer player) {
        WirelessNetworkSavedData data = WirelessNetworkSavedData.get(world);
        List<WirelessNetworkRecord> result = new ArrayList<WirelessNetworkRecord>();
        for (WirelessNetworkRecord record : data.records()) {
            TileWirelessController controller = findController(record);
            if (controller != null && hasPermission(controller, player)) result.add(record);
        }
        Collections.sort(result, new Comparator<WirelessNetworkRecord>() {
            @Override public int compare(WirelessNetworkRecord a, WirelessNetworkRecord b) {
                int result = a.getName().compareTo(b.getName());
                return result == 0 ? a.getId().toString().compareTo(b.getId().toString()) : result;
            }
        });
        return result;
    }

    public static int scanCapacity(TileWirelessController controller) {
        return scanCapacity(collectControllers(controller));
    }

    private static int scanCapacity(List<TileController> group) {
        Set<BlockPos> positions = new HashSet<BlockPos>();
        for (TileController tile : group) positions.add(tile.getPos());
        int exposedFaces = 0;
        for (TileController tile : group) for (EnumFacing side : EnumFacing.values()) {
            if (side == EnumFacing.DOWN || side == EnumFacing.UP || side.getAxis().isHorizontal()) {
                BlockPos adjacent = tile.getPos().offset(side);
                if (!positions.contains(adjacent) && !(tile.getWorld().getTileEntity(adjacent) instanceof IGridHost)) exposedFaces++;
            }
        }
        return exposedFaces * Math.max(1, WirelessConfig.channelsPerExposedFace);
    }

    public static List<TileController> collectControllers(TileWirelessController origin) {
        if (origin == null || origin.getWorld() == null) return Collections.emptyList();
        return collectControllersFrom(origin);
    }

    private static List<TileController> collectControllersFrom(TileController origin) {
        if (origin == null || origin.getWorld() == null) return Collections.emptyList();
        List<TileController> result = new ArrayList<TileController>();
        Queue<BlockPos> queue = new ArrayDeque<BlockPos>();
        Set<BlockPos> visited = new HashSet<BlockPos>();
        queue.add(origin.getPos());
        while (!queue.isEmpty() && result.size() < 343) {
            BlockPos pos = queue.remove();
            if (!visited.add(pos)) continue;
            TileEntity tile = origin.getWorld().getTileEntity(pos);
            if (!(tile instanceof TileController)) continue;
            result.add((TileController) tile);
            for (EnumFacing side : EnumFacing.values()) queue.add(pos.offset(side));
        }
        return result;
    }

    private static boolean hasControllerConflict(TileWirelessController controller) {
        try { return controller.getProxy().getPath().getControllerState() == ControllerState.CONTROLLER_CONFLICT; }
        catch (GridAccessException ex) { return false; }
    }

    private static boolean isServer(TileController tile) {
        return tile != null && tile.getWorld() != null && !tile.getWorld().isRemote;
    }

}
