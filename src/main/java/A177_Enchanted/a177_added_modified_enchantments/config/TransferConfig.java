package A177_Enchanted.a177_added_modified_enchantments.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class TransferConfig {
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> blockedContainers;
    public static ForgeConfigSpec SPEC;
    
    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        init(builder);
        SPEC = builder.build();
    }

    public static void init(ForgeConfigSpec.Builder builder) {
        builder.comment("传输附魔配置").push("transfer");

        blockedContainers = builder
                .comment("传输附魔无法传输物品到的容器方块列表 (格式: 方块实体注册名)",
                        "例如: minecraft:jukebox, minecraft:chiseled_bookshelf",
                        "系统会阻止向这些容器传输物品")
                .defineList("blockedContainers", getDefaultBlockedContainers(), 
                        obj -> obj instanceof String);

        builder.pop();
    }

    private static List<String> getDefaultBlockedContainers() {
        List<String> list = new ArrayList<>();
        list.add("minecraft:jukebox");
        list.add("minecraft:chiseled_bookshelf");
        return list;
    }
}