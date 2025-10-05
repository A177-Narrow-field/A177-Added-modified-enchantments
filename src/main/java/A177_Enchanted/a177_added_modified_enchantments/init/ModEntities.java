package A177_Enchanted.a177_added_modified_enchantments.init;

import A177_Enchanted.a177_added_modified_enchantments.A177_added_modified_enchantments;
import A177_Enchanted.a177_added_modified_enchantments.entity.OreHighlightEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = 
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, A177_added_modified_enchantments.MODID);
    
    // 矿石高亮实体注册
    public static final RegistryObject<EntityType<OreHighlightEntity>> ORE_HIGHLIGHT = ENTITIES.register("ore_highlight", 
            () -> EntityType.Builder.of((EntityType.EntityFactory<OreHighlightEntity>) OreHighlightEntity::new, MobCategory.MISC)
                    .sized(0.9f, 0.f)
                    .clientTrackingRange(10)
                    .updateInterval(Integer.MAX_VALUE)
                    .build("ore_highlight"));
    
    // 在这里添加实体注册
    // 示例:
    // public static final RegistryObject<EntityType<CustomEntity>> CUSTOM_ENTITY = ENTITIES.register("custom_entity", 
    //         () -> EntityType.Builder.of(CustomEntity::new, MobCategory.CREATURE)
    //                 .sized(0.6f, 1.8f)
    //                 .build(new ResourceLocation(A177_added_modified_enchantments.MODID, "custom_entity").toString()));
}