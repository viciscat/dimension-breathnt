package net.viciscat.dimensionbreathnt;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.viciscat.dimensionbreathnt.commands.CommandNobreath;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import techguns.TGItems;
import techguns.capabilities.TGExtendedPlayer;

import java.util.*;
import java.util.stream.Collectors;

@Mod(
        modid = DimensionBreathnt.MOD_ID,
        name = DimensionBreathnt.MOD_NAME,
        version = DimensionBreathnt.VERSION,
        acceptableRemoteVersions = "*"
)
public class DimensionBreathnt {

    public static final String MOD_ID = "dimension-breathnt";
    public static final String MOD_NAME = "DimensionBreathnt";
    public static final String VERSION = "1.0-SNAPSHOT";
    public Logger logger = LogManager.getLogger(MOD_ID);

    public static Configuration config;
    public static List<Integer> Dimensions_no_breath;
    public static Property Dimensions_no_breath_prop;

    public static Property Biomes_no_breath_prop;
    public static List<Integer> Biomes_no_breath;

    /**
     * This is the instance of your mod as created by Forge. It will never be null.
     */
    @Mod.Instance(MOD_ID)
    public static DimensionBreathnt INSTANCE;

    /**
     * This is the first initialization event. Register tile entities here.
     * The registry events below will have fired prior to entry to this method.
     */
    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event) {
        config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();

        config.addCustomCategoryComment("general", "The mod's config, pretty cool");

        Dimensions_no_breath_prop = config.get("general",
                "dimension_list",
                new int[]{},
                "Dimensions where the player won't be able to breath");
        Dimensions_no_breath = Arrays.stream(Dimensions_no_breath_prop.getIntList()).boxed().collect(Collectors.toList());

        Biomes_no_breath_prop = config.get("general",
                "biome_list",
                new int[]{},
                "Biomes where the player won't be able to breath");
        Biomes_no_breath = Arrays.stream(Biomes_no_breath_prop.getIntList()).boxed().collect(Collectors.toList());

        config.save();

    }

    /**
     * This is the second initialization event. Register custom recipes
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {


    }

    @Mod.EventHandler
    public void serverInit(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandNobreath());
    }

    /**
     * This is the final initialization event. Register actions from other mods here
     */
    @Mod.EventHandler
    public void postinit(FMLPostInitializationEvent event) {
        logger.warn("potatoes");
        logger.warn(DimensionManager.getStaticDimensionIDs());

    }

    /**
     * Forge will automatically look up and bind blocks to the fields in this class
     * based on their registry name.
     */
    @GameRegistry.ObjectHolder(MOD_ID)
    public static class Blocks {
      /*
          public static final MySpecialBlock mySpecialBlock = null; // placeholder for special block below
      */
    }

    /**
     * Forge will automatically look up and bind items to the fields in this class
     * based on their registry name.
     */
    @GameRegistry.ObjectHolder(MOD_ID)
    public static class Items {
      /*
          public static final ItemBlock mySpecialBlock = null; // itemblock for the block above
          public static final MySpecialItem mySpecialItem = null; // placeholder for special item below
      */
    }

    /**
     * This is a special class that listens to registry events, to allow creation of mod blocks and items at the proper time.
     */
    @Mod.EventBusSubscriber
    public static class ObjectRegistryHandler {
        /**
         * Listen for the register event for creating custom items
         */
        @SubscribeEvent
        public static void addItems(RegistryEvent.Register<Item> event) {
           /*
             event.getRegistry().register(new ItemBlock(Blocks.myBlock).setRegistryName(MOD_ID, "myBlock"));
             event.getRegistry().register(new MySpecialItem().setRegistryName(MOD_ID, "mySpecialItem"));
            */
        }

        /**
         * Listen for the register event for creating custom blocks
         */
        @SubscribeEvent
        public static void addBlocks(RegistryEvent.Register<Block> event) {
           /*
             event.getRegistry().register(new MySpecialBlock().setRegistryName(MOD_ID, "mySpecialBlock"));
            */
        }

        @SubscribeEvent
        public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
            EntityPlayer p = event.player;
            p.sendMessage(new TextComponentString(Integer.toString(p.dimension)));
            p.sendMessage(p.getDataManager().get(TGExtendedPlayer.DATA_BACK_SLOT).getTextComponent());
        }

        @SubscribeEvent
        public static void playerTick(TickEvent.PlayerTickEvent event) {
            if (event.phase == TickEvent.Phase.START) return;

            EntityPlayer p = event.player;


            Integer biome_id = Biome.getIdForBiome(p.world.getBiome(p.getPosition()));

            if (Dimensions_no_breath.contains(p.dimension) || Biomes_no_breath.contains(biome_id)) {

                ItemStack back_item = p.getDataManager().get(TGExtendedPlayer.DATA_BACK_SLOT);
                ItemStack face_item = p.getDataManager().get(TGExtendedPlayer.DATA_FACE_SLOT);
                NBTTagCompound pData = p.getEntityData();


                int pAir = pData.getInteger("DimAir");
                int i = EnchantmentHelper.getRespirationModifier(p);
                pData.setInteger("DimAir", i > 0 && new Random().nextInt(i + 1) > 0 ? pAir : pAir - 1);

                // If no air
                if (pAir == 0) {

                    if (back_item.getItem() == TGItems.SCUBA_TANKS &&
                            face_item.getItem() == TGItems.OXYGEN_MASK.getItem() &&
                            back_item.getItemDamage() < back_item.getMaxDamage()) {

                        back_item.setItemDamage(back_item.getItemDamage() + 3);

                    } else {
                        p.attackEntityFrom(DamageSource.MAGIC, 4.0f);
                    }

                    pData.setInteger("DimAir", 20);
                }
            } else {
                p.getEntityData().setInteger("DimAir", 20);
            }


        }
    }
}


