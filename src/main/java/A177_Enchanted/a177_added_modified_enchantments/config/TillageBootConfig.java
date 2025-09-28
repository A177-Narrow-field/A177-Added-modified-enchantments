package A177_Enchanted.a177_added_modified_enchantments.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class TillageBootConfig {
    public static ForgeConfigSpec.BooleanValue enabled;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> cropMappings;
    public static ForgeConfigSpec.ConfigValue<Integer> range;
    public static ForgeConfigSpec SPEC;

    public static void init(ForgeConfigSpec.Builder builder) {
        builder.comment("耕耘锄附魔配置").push("tillage_boot");

        enabled = builder
                .comment("是否启用耕耘锄附魔功能")
                .define("enabled", true);

        range = builder
                .comment("耕耘靴附魔的工作范围（半径），例如：1表示3x3区域，2表示5x5区域")
                .define("range", 2);

        cropMappings = builder
                .comment("农作物种子ID列表 (格式: 种子物品ID，例如: minecraft:wheat_seeds)",
                        "对于其他模组的农作物，只需添加其种子的注册名即可",
                        "系统会自动尝试推断对应的作物方块ID")
                .defineList("crop_mappings", getDefaultCropMappings(), 
                        obj -> obj instanceof String);

        builder.pop();
    }

    private static List<String> getDefaultCropMappings() {
        List<String> list = new ArrayList<>();
        list.add("minecraft:wheat_seeds");
        list.add("minecraft:carrot");
        list.add("minecraft:potato");
        list.add("minecraft:beetroot_seeds");
        list.add("minecraft:melon_seeds");
        list.add("minecraft:pumpkin_seeds");
        return list;
    }
}