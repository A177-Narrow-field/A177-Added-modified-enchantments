package A177_Enchanted.a177_added_modified_enchantments.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class RangeFootBlockConfig {
    public static ForgeConfigSpec.ConfigValue<Integer> range;
    public static ForgeConfigSpec SPEC;
    
    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        init(builder);
        SPEC = builder.build();
    }

    public static void init(ForgeConfigSpec.Builder builder) {
        builder.comment("范围方块附魔配置").push("range_foot_block");

        range = builder
                .comment("范围方块附魔的工作范围（半径），例如：1表示3x3区域，2表示5x5区域")
                .define("range", 2);

        builder.pop();
    }
}