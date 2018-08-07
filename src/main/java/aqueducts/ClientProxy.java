package aqueducts;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientProxy implements IProxy {
    public static ModelResourceLocation aqueductWaterLocation = new ModelResourceLocation(new ResourceLocation(Aqueducts.MODID, "aqueduct_water"), "normal");

    @Override
    public void preInit() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void init() {

    }

    @SubscribeEvent
    public void registerModels(ModelRegistryEvent event)
    {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Aqueducts.AQUEDUCT),0,new ModelResourceLocation(Aqueducts.AQUEDUCT.getRegistryName(),"inventory"));
        ModelLoader.setCustomStateMapper(Aqueducts.AQUEDUCT_WATER, new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                return aqueductWaterLocation;
            }
        });
    }
}
