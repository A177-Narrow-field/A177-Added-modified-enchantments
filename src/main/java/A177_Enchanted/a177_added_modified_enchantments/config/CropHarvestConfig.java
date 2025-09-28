package A177_Enchanted.a177_added_modified_enchantments.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import A177_Enchanted.a177_added_modified_enchantments.A177_added_modified_enchantments;

import java.util.List;
import java.util.Arrays;

@Mod.EventBusSubscriber(modid = A177_added_modified_enchantments.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CropHarvestConfig {
    public static class Common {
        public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
        
        public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CROP_HARVEST_CROPS = BUILDER
                .comment("List of crops that the Crop Harvest enchantment will work on. Use block registry names (e.g. 'minecraft:wheat')")
                .defineList("cropHarvestCrops", 
                        Arrays.asList(
                                "minecraft:wheat", 
                                "minecraft:carrots", 
                                "minecraft:potatoes", 
                                "minecraft:beetroots", 
                                "minecraft:nether_wart"),
                        obj -> obj instanceof String);
        
        public static final ForgeConfigSpec SPEC = BUILDER.build();
        public static List<String> cropHarvestCrops;
    }
    
    public static void registerConfigs() {
        Common.cropHarvestCrops = (List<String>) Common.CROP_HARVEST_CROPS.get();
    }
}