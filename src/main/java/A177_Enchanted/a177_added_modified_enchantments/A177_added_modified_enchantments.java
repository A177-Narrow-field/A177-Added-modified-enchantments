package A177_Enchanted.a177_added_modified_enchantments;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import A177_Enchanted.a177_added_modified_enchantments.init.ModBlocks;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.init.ModItems;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEntities;
import A177_Enchanted.a177_added_modified_enchantments.config.CropHarvestConfig;
import A177_Enchanted.a177_added_modified_enchantments.config.RangeFootBlockConfig;
import A177_Enchanted.a177_added_modified_enchantments.config.WeedRemovalConfig;
import A177_Enchanted.a177_added_modified_enchantments.config.CoreCollectionConfig;
import A177_Enchanted.a177_added_modified_enchantments.config.TransferConfig;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.config.OreDetectorConfig; // 添加OreDetectorConfig导入
import net.minecraftforge.event.entity.EntityAttributeCreationEvent; // 添加属性事件导入

// The value here should match an entry in the META-INF/mods.toml file
@Mod(A177_added_modified_enchantments.MODID)
public class A177_added_modified_enchantments {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "a177_added_modified_enchantments";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "a177_added_modified_enchantments" namespace
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "a177_added_modified_enchantments" namespace
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    // Creates a new Block with the id "a177_added_modified_enchantments:example_block", combining the namespace and path
    public static final RegistryObject<Block> EXAMPLE_BLOCK = BLOCKS.register("example_block", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)));
    // Creates a new BlockItem with the id "a177_added_modified_enchantments:example_block", combining the namespace and path
    public static final RegistryObject<Item> EXAMPLE_BLOCK_ITEM = ITEMS.register("example_block", () -> new BlockItem(EXAMPLE_BLOCK.get(), new Item.Properties()));

    // Very important note: you need to have a no-arg constructor in your mod class for Forge to be able to instantiate it.
    // This is the constructor that will be called by Forge when it loads your mod.
    public A177_added_modified_enchantments() {
        // 尽管FMLJavaModLoadingContext.get()被标记为弃用，但在当前版本中仍可使用
        this(FMLJavaModLoadingContext.get().getModEventBus());
    }
    public A177_added_modified_enchantments(IEventBus modEventBus) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so enchantments get registered
        ModEnchantments.ENCHANTMENTS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so custom blocks get registered
        ModBlocks.BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so custom items get registered
        ModItems.ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so entities get registered
        ModEntities.ENTITIES.register(modEventBus);

        // Register entity attributes
        modEventBus.addListener(this::addEntityAttributes);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // 尽管ModLoadingContext.get()被标记为弃用，但在当前版本中仍可使用
        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        // Register crop harvest enchantment config with a specific filename to avoid conflicts
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CropHarvestConfig.Common.SPEC, "crop_harvest_config.toml");
        // Register range foot block enchantment config with a specific filename to avoid conflicts
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, RangeFootBlockConfig.SPEC, "range_foot_block_config.toml");
        // Register weed removal enchantment config with a specific filename to avoid conflicts
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, WeedRemovalConfig.SPEC, "weed_removal_config.toml");
        // Register core collection enchantment config with a specific filename to avoid conflicts
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CoreCollectionConfig.SPEC, "core_collection_config.toml");
        // Register transfer enchantment config with a specific filename to avoid conflicts
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, TransferConfig.SPEC, "transfer_config.toml");
        // Register fist enchantment config with a specific filename to avoid conflicts
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, AllEnchantmentsConfig.SPEC, "all_enchantments_config.toml");
        // Register ore detector enchantment config with a specific filename to avoid conflicts
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, OreDetectorConfig.SPEC, "ore_detector_config.toml");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
        LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        if (Config.logDirtBlock) LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        // Register crop harvest config
        CropHarvestConfig.registerConfigs();

        // Reload weed removal config to ensure it's properly loaded
        A177_Enchanted.a177_added_modified_enchantments.enchantments.WeedRemovalBootEnchantment.reloadConfig();

        // Reload core collection config to ensure it's properly loaded
        A177_Enchanted.a177_added_modified_enchantments.enchantments.CoreCollectionEnchantment.reloadConfig();

        // Reload ore detector config to ensure it's properly loaded
        A177_Enchanted.a177_added_modified_enchantments.enchantments.OreDetectorEnchantment.reloadConfig();
    }

    // 添加实体属性
    private void addEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.ORE_HIGHLIGHT.get(), 
                  A177_Enchanted.a177_added_modified_enchantments.entity.OreHighlightEntity.createAttributes().build());
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}