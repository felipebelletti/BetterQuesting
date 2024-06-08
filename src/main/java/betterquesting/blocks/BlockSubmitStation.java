package betterquesting.blocks;

import betterquesting.core.BetterQuesting;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class BlockSubmitStation extends BlockContainer {
    public BlockSubmitStation() {
        super(Material.WOOD);
        this.setHardness(1);
        this.setTranslationKey("betterquesting.submit_station");
        this.setCreativeTab(BetterQuesting.tabQuesting);
    }

    @Override
    public TileEntity createNewTileEntity(Level world, int meta) {
        return new TileSubmitStation();
    }

    /**
     * The type of render function called. 3 for standard block models, 2 for TESR's, 1 for liquids, -1 is no render
     */
    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    /**
     * Called upon block activation (right click on the block.)
     */
    @Override
    public boolean onBlockActivated(Level world, BlockPos pos, IBlockState state, Player player, EnumHand hand, EnumFacing heldItem, float side, float hitX, float hitY) {
        if (!world.isRemote) {
            player.openGui(BetterQuesting.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    public void breakBlock(Level world, BlockPos pos, IBlockState state) {
        TileSubmitStation tileStation = (TileSubmitStation) world.getTileEntity(pos);

        if (tileStation != null) {
            InventoryHelper.dropInventoryItems(world, pos, tileStation);
        }

        super.breakBlock(world, pos, state);
    }
}
