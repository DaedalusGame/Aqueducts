package aqueducts;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class BlockAqueduct extends Block {
    public BlockAqueduct() {
        super(Material.ROCK);

        this.setUnlocalizedName("aqueduct");
        this.setCreativeTab(CreativeTabs.TRANSPORTATION);
        this.setHardness(2.1F).setResistance(8.0F);
        this.setSoundType(SoundType.STONE);
        this.setHarvestLevel("pickaxe", 0);
        this.setTickRandomly(true);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if(fromPos.equals(pos.up()))
        {
            worldIn.scheduleUpdate(pos, this, 25);
        }
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        super.onBlockAdded(worldIn, pos, state);
        worldIn.scheduleUpdate(pos, this, 25);
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        IBlockState waterState = worldIn.getBlockState(pos.up());
        Block block = waterState.getBlock();

        if(waterState.getMaterial() == Material.WATER && waterState.getValue(BlockLiquid.LEVEL) != 0) //Specifically almost full water... maybe needs to check falling water too...
        {
            int dist = TileEntityAqueductWater.getMinDistance(worldIn,pos.up())+1;
            if(dist <= Aqueducts.AQUEDUCT_MAX_LENGTH) {
                worldIn.setBlockState(pos.up(), Aqueducts.AQUEDUCT_WATER.getDefaultState().withProperty(BlockLiquid.LEVEL,8));
                TileEntity te = worldIn.getTileEntity(pos.up());
                if(te instanceof TileEntityAqueductWater)
                    ((TileEntityAqueductWater) te).setDistanceFromSource(dist);
            }
        }
        else if(waterState.getBlock() instanceof BlockAqueductWater)
        {
            ((BlockAqueductWater) waterState.getBlock()).checkAndDry(worldIn,pos.up(),waterState);
        }
    }
}