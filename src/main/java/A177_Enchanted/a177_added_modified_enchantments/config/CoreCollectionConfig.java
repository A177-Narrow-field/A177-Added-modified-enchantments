package A177_Enchanted.a177_added_modified_enchantments.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class CoreCollectionConfig {
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> allowedBlocks;
    public static ForgeConfigSpec SPEC;
    
    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        init(builder);
        SPEC = builder.build();
    }

    public static void init(ForgeConfigSpec.Builder builder) {
        builder.comment("核心采集附魔配置").push("core_collection");

        allowedBlocks = builder
                .comment("核心采集附魔可以采集的方块列表 (格式: 方块注册名，例如: minecraft:bedrock)",
                        "对于其他模组的方块，添加其注册名即可",
                        "系统会自动识别并允许采集这些方块")
                .defineList("allowedBlocks", getDefaultAllowedBlocks(), 
                        obj -> obj instanceof String);

        builder.pop();
    }

    private static List<String> getDefaultAllowedBlocks() {
        List<String> list = new ArrayList<>();
        list.add("minecraft:end_portal_frame");
        list.add("minecraft:budding_amethyst");
        list.add("minecraft:command_block");
        list.add("minecraft:spawner");
        list.add("minecraft:barrier");
        list.add("minecraft:bedrock");
        return list;
    }
}