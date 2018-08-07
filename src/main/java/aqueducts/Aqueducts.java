package aqueducts;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = Aqueducts.MODID, acceptedMinecraftVersions = "[1.12, 1.13)")
@Mod.EventBusSubscriber
public class Aqueducts
{
    @SidedProxy(clientSide = "aqueducts.ClientProxy",serverSide = "aqueducts.ServerProxy")
    public static IProxy proxy;

    static Configuration configuration;

    public static int AQUEDUCT_MAX_LENGTH = 128;
    public static String[] AQUEDUCT_BIOME_STRINGS = new String[0];
    public static boolean AQUEDUCT_BIOMES_IS_WHITELIST = true;
    public static String[] AQUEDUCT_SOURCE_WHITELIST = new String[] {
            "streams:river"
    };
    public static boolean AQUEDUCT_IS_TANK = false;
    public static int AQUEDUCT_SOURCES_MINIMUM = 0;
    public static int AQUEDUCT_SOURCES_SEARCH = 0;

    public static final String MODID = "aqueducts";
    public static final String NAME = "Aqueducts";

    @GameRegistry.ObjectHolder("aqueducts:aqueduct_water")
    public static BlockAqueductWater AQUEDUCT_WATER;
    @GameRegistry.ObjectHolder("aqueducts:aqueduct")
    public static BlockAqueduct AQUEDUCT;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        configuration = new Configuration(event.getSuggestedConfigurationFile(),true);
        configuration.load();
        proxy.preInit();

        AQUEDUCT_BIOME_STRINGS = loadPropStringList("Biomes", "Aqueducts can only draw water from sources in specific biomes.", AQUEDUCT_BIOME_STRINGS);
        AQUEDUCT_BIOMES_IS_WHITELIST = loadPropBool("BiomesIsWhitelist", "Whether aqueduct biomes should be whitelisted or blacklisted. If the biome list is empty, it's automatically a blacklist.", AQUEDUCT_BIOMES_IS_WHITELIST);
        AQUEDUCT_SOURCE_WHITELIST = loadPropStringList("Sources", "Sources Aqueducts can pull from other than real source blocks.", AQUEDUCT_SOURCE_WHITELIST);
        AQUEDUCT_MAX_LENGTH = loadPropInt("MaxLength", "How long aqueducts can be.", AQUEDUCT_MAX_LENGTH);
        AQUEDUCT_SOURCES_MINIMUM = loadPropInt("SourcesRequired", "How many connected water sources are required for an aqueduct to take from it.", AQUEDUCT_SOURCES_MINIMUM);
        AQUEDUCT_SOURCES_SEARCH = loadPropInt("SourcesSearch", "How many blocks will be checked for water sources. This should be a bit larger than the minimum amount of sources.", AQUEDUCT_SOURCES_SEARCH);
        AQUEDUCT_IS_TANK = loadPropBool("IsTank", "Aqueduct water counts as a fluid tank for modded pipes. Happy birthday Vyraal1", AQUEDUCT_IS_TANK);

        for(String s : AQUEDUCT_SOURCE_WHITELIST)
            TileEntityAqueductWater.addWaterSource(new ResourceLocation(s));

        MinecraftForge.EVENT_BUS.register(this);

        GameRegistry.registerTileEntity(TileEntityAqueductWater.class,"aqueducts:aqueduct_water");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init();

        TileEntityAqueductWater.reloadBiomeList();
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(AQUEDUCT = (BlockAqueduct) new BlockAqueduct().setRegistryName(new ResourceLocation(Aqueducts.MODID, "aqueduct")));
        event.getRegistry().register(AQUEDUCT_WATER = (BlockAqueductWater) new BlockAqueductWater().setRegistryName(new ResourceLocation(Aqueducts.MODID, "aqueduct_water")));
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new ItemBlock(AQUEDUCT).setRegistryName(AQUEDUCT.getRegistryName()));
    }

    public static int loadPropInt(String propName, String desc, int default_) {
        Property prop = configuration.get("config", propName, default_);
        prop.setComment(desc);

        return prop.getInt(default_);
    }

    public static boolean loadPropBool(String propName, String desc, boolean default_) {
        Property prop = configuration.get("config", propName, default_);
        prop.setComment(desc);

        return prop.getBoolean(default_);
    }

    public static String[] loadPropStringList(String propName, String desc, String[] default_) {
        Property prop = configuration.get("config", propName, default_);
        prop.setComment(desc);

        return prop.getStringList();
    }
}
