package aqueducts;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.wrappers.FluidBlockWrapper;

import java.util.ArrayList;
import java.util.HashSet;

public class TileEntityAqueductWater extends TileEntity {
    private int distanceFromSource;
    private static final HashSet<ResourceLocation> WATER_SOURCES = new HashSet<>();
    private static final HashSet<Biome> BIOMES = new HashSet<>();

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && Aqueducts.AQUEDUCT_IS_TANK)
            return true;

        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && Aqueducts.AQUEDUCT_IS_TANK)
            return (T)new FluidBlockWrapper(Aqueducts.AQUEDUCT_WATER, world, pos);

        return super.getCapability(capability, facing);
    }

    public static void addWaterSource(ResourceLocation block)
    {
        WATER_SOURCES.add(block);
    }

    public int getDistanceFromSource() {
        return distanceFromSource;
    }

    public void setDistanceFromSource(int distanceFromSource) {
        this.distanceFromSource = distanceFromSource;
        this.markDirty();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        compound.setInteger("Distance", this.distanceFromSource);

        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        this.distanceFromSource = compound.getInteger("Distance");
    }

    public static int getMinDistance(World world, BlockPos checkpos)
    {
        int minDistance = 1000000;

        HashSet<BlockPos> corners = new HashSet<>();

        for (EnumFacing facing: EnumFacing.HORIZONTALS) {
            BlockPos neighborpos = checkpos.offset(facing);
            IBlockState state = world.getBlockState(neighborpos);
            boolean isWater = false;
            int dist = getBlockDistanceFromSource(world,neighborpos,state,false);

            if(dist > -1)
            {
                minDistance = Math.min(minDistance,dist);
                isWater = true;
            }

            if(isWater)
            {
                corners.add(neighborpos.offset(facing.rotateY(),1));
                corners.add(neighborpos.offset(facing.rotateY(),-1));
            }
        }

        for(BlockPos neighborpos : corners)
        {
            IBlockState state = world.getBlockState(neighborpos);

            int dist = getBlockDistanceFromSource(world,neighborpos,state,false);
            if(dist > -1)
                minDistance = Math.min(minDistance,dist);
        }

        return minDistance;
    }

    public static int getBlockDistanceFromSource(World world, BlockPos pos, IBlockState state, boolean recursed) {
        boolean isValidBiome = true;

        if(!BIOMES.isEmpty())
            isValidBiome = BIOMES.contains(world.getBiome(pos)) == Aqueducts.AQUEDUCT_BIOMES_IS_WHITELIST;

        if(isValidBiome && isRealWaterSource(state))
        {
            return (Aqueducts.AQUEDUCT_SOURCES_MINIMUM <= 0 || isEnoughWater(world, pos)) ? 0 : -1;
        }
        else if(state.getBlock() == Aqueducts.AQUEDUCT && !recursed)
        {
            return getBlockDistanceFromSource(world,pos.up(),world.getBlockState(pos.up()),true);
        }
        else
        {
            TileEntity te = world.getTileEntity(pos);

            if (te instanceof TileEntityAqueductWater) {
                return ((TileEntityAqueductWater) te).getDistanceFromSource();
            }
        }

        return -1;
    }

    public static boolean isRealWaterSource(IBlockState state) {
        return (state.getMaterial() == Material.WATER && state.getValue(BlockLiquid.LEVEL) == 0) || WATER_SOURCES.contains(state.getBlock().getRegistryName());
    }

    public static boolean isEnoughWater(World world, BlockPos pos) {
        HashSet<BlockPos> visited = Sets.newHashSet();
        ArrayList<BlockPos> toVisit = Lists.newArrayList(pos);
        int foundWater = 0;
        for(int i = 0; i < Aqueducts.AQUEDUCT_SOURCES_SEARCH; i++) {
            BlockPos visit = toVisit.remove(0);
            if(visited.contains(visit))
                continue;
            visited.add(visit);
            if(!isRealWaterSource(world.getBlockState(visit)))
                continue;
            foundWater++;
            if(foundWater >= Aqueducts.AQUEDUCT_SOURCES_MINIMUM)
                return true;
            for (EnumFacing facing : EnumFacing.VALUES)
                toVisit.add(visit.offset(facing));
        }
        return false;
    }

    public static void reloadBiomeList()
    {
        BIOMES.clear();
        for (String str : Aqueducts.AQUEDUCT_BIOME_STRINGS) {
            ResourceLocation location = new ResourceLocation(str);
            Biome biome = Biome.REGISTRY.getObject(location);
            if(biome != null && !BIOMES.contains(biome))
                BIOMES.add(biome);
        }
    }
}
