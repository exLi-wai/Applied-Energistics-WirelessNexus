package com.lw.ae_wireless_nexus.block;

import com.lw.ae_wireless_nexus.Tags;
import appeng.block.networking.BlockController;
import appeng.block.networking.BlockController.ControllerRenderType;
import appeng.tile.networking.TileController;
import com.lw.ae_wireless_nexus.tile.TileWirelessController;
import com.lw.ae_wireless_nexus.ae_wireless_nexus;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.IBlockAccess;
import com.lw.ae_wireless_nexus.common.gui.GuiHandler;
import com.lw.ae_wireless_nexus.network.WirelessNetworkToolBinding;

public class BlockWirelessController extends BlockController {
    public BlockWirelessController() {
        super();
        setRegistryName(Tags.MOD_ID, "wireless_controller");
        setTranslationKey("ae_wireless_nexus.wireless_controller");
        setCreativeTab(ae_wireless_nexus.CREATIVE_TAB);
        setTileEntity(TileWirelessController.class);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        boolean xx = isController(world, pos.west()) && isController(world, pos.east());
        boolean yy = isController(world, pos.down()) && isController(world, pos.up());
        boolean zz = isController(world, pos.north()) && isController(world, pos.south());

        ControllerRenderType type = ControllerRenderType.block;
        if (xx && !yy && !zz) {
            type = ControllerRenderType.column_x;
        } else if (!xx && yy && !zz) {
            type = ControllerRenderType.column_y;
        } else if (!xx && !yy && zz) {
            type = ControllerRenderType.column_z;
        } else if ((xx ? 1 : 0) + (yy ? 1 : 0) + (zz ? 1 : 0) >= 2) {
            int parity = Math.abs(pos.getX()) + Math.abs(pos.getY()) + Math.abs(pos.getZ());
            type = (parity & 1) == 0 ? ControllerRenderType.inside_a : ControllerRenderType.inside_b;
        }
        return state.withProperty(CONTROLLER_TYPE, type);
    }

    private static boolean isController(IBlockAccess world, BlockPos pos) {
        return world.getTileEntity(pos) instanceof TileController;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
        EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY,
        float hitZ) {
        if (hand != EnumHand.MAIN_HAND) return true;

        ItemStack held = player.getHeldItem(hand);
        if (player.isSneaking() || held.getItem() instanceof ItemBlock) {
            return super.onBlockActivated(world, pos, state, player, hand, side,
                hitX, hitY, hitZ);
        }

        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof TileWirelessController)) return false;

        if (WirelessNetworkToolBinding.isWirelessConnectorTool(held)) {
            if (!world.isRemote) {
                WirelessNetworkToolBinding.selectNetwork(
                    held, (TileWirelessController) tile, player);
            }
            return true;
        }

        if (!world.isRemote) {
            player.openGui(ae_wireless_nexus.instance, GuiHandler.WIRELESS_CONTROLLER,
                world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos,
        Block block, BlockPos fromPos) {
        super.neighborChanged(state, world, pos, block, fromPos);
        if (world.isRemote) {
            world.markBlockRangeForRenderUpdate(pos.add(-1, -1, -1), pos.add(1, 1, 1));
        }
    }
}
