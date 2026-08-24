package com.lw.ae_wireless_nexus.block;

import com.lw.ae_wireless_nexus.Tags;
import net.minecraft.block.material.Material;
import appeng.block.AEBaseTileBlock;
import com.lw.ae_wireless_nexus.tile.TileWirelessConnector;
import com.lw.ae_wireless_nexus.ae_wireless_nexus;
import com.lw.ae_wireless_nexus.network.WirelessNetworkToolBinding;
import com.lw.ae_wireless_nexus.network.WirelessNetworkService;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.UUID;

public class BlockWirelessConnector extends AEBaseTileBlock {
    public BlockWirelessConnector() {
        super(Material.IRON);
        setRegistryName(Tags.MOD_ID, "wireless_connector");
        setTranslationKey("ae_wireless_nexus.wireless_connector");
        setCreativeTab(ae_wireless_nexus.CREATIVE_TAB);
        setHardness(3.0F);
        setTileEntity(TileWirelessConnector.class);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state,
        EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        if (world.isRemote || !(placer instanceof EntityPlayer)) return;

        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileWirelessConnector) {
            EntityPlayer player = (EntityPlayer) placer;
            ItemStack tool = WirelessNetworkToolBinding.findAutomaticBindingTool(player);
            UUID networkId = WirelessNetworkToolBinding.getNetworkId(tool);
            if (networkId != null) {
                if (WirelessNetworkService.bindEndpointPersisted((TileWirelessConnector) tile,
                    networkId, player)) {
                    player.sendMessage(new TextComponentTranslation(
                        "message.ae_wireless_nexus.wireless_tool.connected",
                        WirelessNetworkToolBinding.getNetworkDisplayName(tool)));
                }
            }
        }
    }
}
