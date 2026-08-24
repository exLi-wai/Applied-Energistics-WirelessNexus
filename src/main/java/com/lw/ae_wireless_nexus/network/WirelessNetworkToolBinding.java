package com.lw.ae_wireless_nexus.network;

import java.util.UUID;

import com.lw.ae_wireless_nexus.Tags;
import com.lw.ae_wireless_nexus.registry.ModItems;
import com.lw.ae_wireless_nexus.integration.baubles.BaublesWirelessToolSupport;
import com.lw.ae_wireless_nexus.tile.TileWirelessConnector;
import com.lw.ae_wireless_nexus.tile.TileWirelessController;
import com.lw.ae_wireless_nexus.ae_wireless_nexus;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.Loader;

public final class WirelessNetworkToolBinding {

    private static final String BINDING_TAG = Tags.MOD_ID;
    private static final String NETWORK_ID_TAG = "NetworkId";
    private static final String NETWORK_NAME_TAG = "NetworkName";

    private WirelessNetworkToolBinding() {}

    public static boolean isWirelessConnectorTool(ItemStack stack) {
        return !stack.isEmpty()
            && ModItems.WIRELESS_CONNECTOR_TOOL != null
            && stack.getItem() == ModItems.WIRELESS_CONNECTOR_TOOL;
    }

    public static boolean selectNetwork(ItemStack tool, TileWirelessController controller,
        EntityPlayer player) {
        if (!isWirelessConnectorTool(tool) || controller == null || player == null) {
            return false;
        }

        String networkName = controller.getNetworkName();
        if (!WirelessNetworkService.hasPermission(controller, player)) {
            notifyPlayer(player, "message.ae_wireless_nexus.wireless_tool.no_permission",
                networkName);
            return false;
        }

        NBTTagCompound root = tool.hasTagCompound()
            ? tool.getTagCompound()
            : new NBTTagCompound();
        NBTTagCompound binding = new NBTTagCompound();
        binding.setString(NETWORK_ID_TAG, controller.getNetworkId().toString());
        binding.setString(NETWORK_NAME_TAG, networkName);
        if (root != null) {
            root.setTag(BINDING_TAG, binding);
        }
        tool.setTagCompound(root);
        player.inventory.markDirty();
        ae_wireless_nexus.LOGGER.info("Wireless connector tool selected network {} ({}) for player {}",
            networkName, controller.getNetworkId(), player.getName());
        notifyPlayer(player, "message.ae_wireless_nexus.wireless_tool.selected", networkName);
        return true;
    }

    public static boolean bindConnector(ItemStack tool, TileWirelessConnector connector,
        EntityPlayer player) {
        return bindEndpoint(tool, connector, player);
    }

    public static boolean bindEndpoint(ItemStack tool, WirelessRuntimeEndpoint endpoint,
        EntityPlayer player) {
        UUID networkId = getNetworkId(tool);
        if (networkId == null || endpoint == null || player == null) {
            return false;
        }

        String networkName = getNetworkDisplayName(tool);
        if (!WirelessNetworkService.bindEndpoint(endpoint, networkId, player)) {
            notifyPlayer(player, "message.ae_wireless_nexus.wireless_tool.bind_failed",
                networkName);
            return false;
        }

        notifyPlayer(player, "message.ae_wireless_nexus.wireless_tool.connected", networkName);
        return true;
    }

    public static boolean hasBinding(ItemStack tool) {
        return getNetworkId(tool) != null;
    }

    public static ItemStack findAutomaticBindingTool(EntityPlayer player) {
        if (player == null) return ItemStack.EMPTY;

        ItemStack offhand = player.getHeldItemOffhand();
        if (hasBinding(offhand)) return offhand;

        if (Loader.isModLoaded("baubles")) {
            ItemStack bauble = BaublesWirelessToolSupport.findBoundWirelessTool(player);
            if (hasBinding(bauble)) return bauble;
        }

        for (ItemStack carried : player.inventory.mainInventory) {
            if (hasBinding(carried)) return carried;
        }

        return ItemStack.EMPTY;
    }

    public static UUID getNetworkId(ItemStack tool) {
        if (!isWirelessConnectorTool(tool)) {
            return null;
        }
        String value = getBinding(tool).getString(NETWORK_ID_TAG);
        if (value.isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public static String getNetworkDisplayName(ItemStack tool) {
        UUID networkId = getNetworkId(tool);
        if (networkId == null) {
            return "";
        }
        String networkName = getBinding(tool).getString(NETWORK_NAME_TAG);
        return networkName.isEmpty() ? networkId.toString() : networkName;
    }

    private static NBTTagCompound getBinding(ItemStack tool) {
        if (tool.isEmpty() || !tool.hasTagCompound()) {
            return new NBTTagCompound();
        }
        return tool.getTagCompound().getCompoundTag(BINDING_TAG);
    }

    private static void notifyPlayer(EntityPlayer player, String key, Object... arguments) {
        player.sendMessage(new TextComponentTranslation(key, arguments));
    }
}
