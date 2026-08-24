package com.lw.ae_wireless_nexus.item;

import java.util.List;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;

import com.lw.ae_wireless_nexus.Tags;
import com.lw.ae_wireless_nexus.network.WirelessNetworkToolBinding;

@Optional.Interface(iface = "baubles.api.IBauble", modid = "baubles")
public class ItemWirelessConnectorTool extends Item implements IBauble {

    public ItemWirelessConnectorTool(CreativeTabs creativeTab) {
        setRegistryName(Tags.MOD_ID, "wireless_connector_tool");
        setTranslationKey("ae_wireless_nexus.wireless_connector_tool");
        setCreativeTab(creativeTab);
        setMaxStackSize(1);
    }

    @Override
    @Optional.Method(modid = "baubles")
    public BaubleType getBaubleType(ItemStack stack) {
        return BaubleType.TRINKET;
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> tooltip,
        ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        if (WirelessNetworkToolBinding.hasBinding(stack)) {
            tooltip.add(TextFormatting.AQUA + I18n.format(
                "tooltip.ae_wireless_nexus.wireless_tool.bound_network",
                WirelessNetworkToolBinding.getNetworkDisplayName(stack)));
            tooltip.add(TextFormatting.GRAY + I18n.format(
                "tooltip.ae_wireless_nexus.wireless_tool.auto_bind"));
        } else {
            tooltip.add(TextFormatting.GRAY + I18n.format(
                "tooltip.ae_wireless_nexus.wireless_tool.unbound"));
        }
    }
}
