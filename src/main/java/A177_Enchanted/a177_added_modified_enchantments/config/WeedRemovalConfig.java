package A177_Enchanted.a177_added_modified_enchantments.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class WeedRemovalConfig {
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> weedBlockList;
    public static ForgeConfigSpec SPEC;
    
    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        init(builder);
        SPEC = builder.build();
    }

    public static void init(ForgeConfigSpec.Builder builder) {
        builder.comment("杂草清除附魔配置").push("weed_removal");

        weedBlockList = builder
                .comment("杂草清除附魔需要清除的方块列表 (格式: 方块注册名，例如: minecraft:grass)",
                        "对于其他模组的方块，添加其注册名即可",
                        "系统会自动识别并清除这些方块")
                .defineList("weedBlockList", getDefaultWeedBlockList(), 
                        obj -> obj instanceof String);

        builder.pop();
    }

    private static List<String> getDefaultWeedBlockList() {
        List<String> list = new ArrayList<>();
        // 草类方块
        list.add("minecraft:grass");
        list.add("minecraft:tall_grass");
        list.add("minecraft:fern");
        list.add("minecraft:large_fern");
        list.add("minecraft:dead_bush");
        
        // 花朵类方块
        list.add("minecraft:dandelion");
        list.add("minecraft:poppy");
        list.add("minecraft:blue_orchid");
        list.add("minecraft:allium");
        list.add("minecraft:azure_bluet");
        list.add("minecraft:red_tulip");
        list.add("minecraft:orange_tulip");
        list.add("minecraft:white_tulip");
        list.add("minecraft:pink_tulip");
        list.add("minecraft:oxeye_daisy");
        list.add("minecraft:cornflower");
        list.add("minecraft:lily_of_the_valley");
        list.add("minecraft:wither_rose");
        list.add("minecraft:sunflower");
        list.add("minecraft:lilac");
        list.add("minecraft:rose_bush");
        list.add("minecraft:peony");
        
        // 树叶类方块
        list.add("minecraft:oak_leaves");
        list.add("minecraft:spruce_leaves");
        list.add("minecraft:birch_leaves");
        list.add("minecraft:jungle_leaves");
        list.add("minecraft:acacia_leaves");
        list.add("minecraft:dark_oak_leaves");
        list.add("minecraft:mangrove_leaves");
        list.add("minecraft:azalea_leaves");
        list.add("minecraft:flowering_azalea_leaves");
        list.add("minecraft:cherry_leaves");
        
        // 花簇类方块
        list.add("minecraft:flowering_azalea");
        list.add("minecraft:azalea");
        list.add("minecraft:pink_petals");
        list.add("minecraft:cherry_sapling");
        
        // 藤蔓类方块
        list.add("minecraft:vine");
        list.add("minecraft:twisting_vines");
        list.add("minecraft:twisting_vines_plant");
        list.add("minecraft:weeping_vines");
        list.add("minecraft:weeping_vines_plant");
        list.add("minecraft:cave_vines");
        list.add("minecraft:cave_vines_plant");
        list.add("minecraft:glow_lichen");
        
        // 海草类方块
        list.add("minecraft:seagrass");
        list.add("minecraft:tall_seagrass");
        
        // 海带类方块
        list.add("minecraft:kelp");
        list.add("minecraft:kelp_plant");
        
        return list;
    }
}