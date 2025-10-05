package A177_Enchanted.a177_added_modified_enchantments.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class OreDetectorConfig {
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> lowTierOres;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> mediumTierOres;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> highTierOres;
    public static ForgeConfigSpec.ConfigValue<Integer> detectionRadius;
    public static ForgeConfigSpec.ConfigValue<Integer> sneakingDetectionRadius; // 潜行时的探测半径
    public static ForgeConfigSpec SPEC;
    
    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        init(builder);
        SPEC = builder.build();
    }

    public static void init(ForgeConfigSpec.Builder builder) {
        builder.comment("矿探附魔配置").push("ore_detector");

        lowTierOres = builder
                .comment("低级矿物列表 (格式: 方块注册名)",
                        "包括煤炭、铜矿等基础矿物")
                .defineList("lowTierOres", getDefaultLowTierOres(), 
                        obj -> obj instanceof String);

        mediumTierOres = builder
                .comment("中级矿物列表 (格式: 方块注册名)",
                        "包括铁矿、金矿、青金石、红石、下届石英、下届金矿等常见矿物")
                .defineList("mediumTierOres", getDefaultMediumTierOres(), 
                        obj -> obj instanceof String);

        highTierOres = builder
                .comment("高级矿物列表 (格式: 方块注册名)",
                        "包括钻石矿、下届合金矿等稀有矿物")
                .defineList("highTierOres", getDefaultHighTierOres(), 
                        obj -> obj instanceof String);
        
        detectionRadius = builder
                .comment("探测半径 (默认值: 18)")
                .define("detectionRadius", 18);
        
        sneakingDetectionRadius = builder
                .comment("潜行时探测半径 (默认值: 5)")
                .define("sneakingDetectionRadius", 5);

        builder.pop();
    }

    private static List<String> getDefaultLowTierOres() {
        List<String> list = new ArrayList<>();
        list.add("minecraft:coal_ore");
        list.add("minecraft:deepslate_coal_ore");
        list.add("minecraft:copper_ore");
        list.add("minecraft:deepslate_copper_ore");
        return list;
    }

    private static List<String> getDefaultMediumTierOres() {
        List<String> list = new ArrayList<>();
        list.add("minecraft:iron_ore");
        list.add("minecraft:deepslate_iron_ore");
        list.add("minecraft:gold_ore");
        list.add("minecraft:deepslate_gold_ore");
        list.add("minecraft:lapis_ore");
        list.add("minecraft:deepslate_lapis_ore");
        list.add("minecraft:redstone_ore");
        list.add("minecraft:deepslate_redstone_ore");
        list.add("minecraft:nether_gold_ore");
        list.add("minecraft:nether_quartz_ore");
        return list;
    }

    private static List<String> getDefaultHighTierOres() {
        List<String> list = new ArrayList<>();
        list.add("minecraft:diamond_ore");
        list.add("minecraft:deepslate_diamond_ore");
        list.add("minecraft:ancient_debris");
        list.add("minecraft:emerald_ore");
        list.add("minecraft:deepslate_emerald_ore");
        return list;
    }
}