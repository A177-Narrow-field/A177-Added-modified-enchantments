package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.core.BlockPos;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;

import java.util.Random;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

@Mod.EventBusSubscriber
public class BodySnatchEnchantment extends Enchantment {
    private static final Random RANDOM = new Random();

    public BodySnatchEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.create("HOE", item -> item instanceof HoeItem), new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 10;
    }

    @Override
    public int getMinCost(int level) {
        return 15 + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 20;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在锄头上
        return stack.getItem() instanceof HoeItem;
    }

    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("body_snatch");
    }

    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.canEnchant(stack);
    }

    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }
    
    // 与祭魂镰斩附魔冲突
    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return super.checkCompatibility(other) && other != ModEnchantments.SOUL_REAPING_SICKLE.get();
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        // 检查死亡实体是否为生物实体且伤害来源是否为玩家
        if (event.getEntity() instanceof LivingEntity && event.getSource().getEntity() instanceof Player player) {
            // 检查玩家主手装备是否有夺体附魔
            ItemStack mainHandItem = player.getMainHandItem();
            int level = mainHandItem.getEnchantmentLevel(ModEnchantments.BODY_SNATCH.get());

            // 如果有附魔且等级大于0
            if (level > 0) {
                // 计算触发概率（每级1%）
                double chance = level * 0.01;

                // 判断是否触发效果
                if (RANDOM.nextDouble() < chance) {
                    // 获取生物类型
                    EntityType<?> entityType = event.getEntity().getType();
                    
                    // 构建生成蛋的注册表名称
                    ResourceLocation entityKey = EntityType.getKey(entityType);
                    ResourceLocation spawnEggKey = ResourceLocation.tryParse(
                        entityKey.getNamespace() + ":" + entityKey.getPath() + "_spawn_egg"
                    );
                    
                    // 从注册表获取生成蛋物品
                    if (spawnEggKey != null) {
                        ItemStack spawnEgg = new ItemStack(ForgeRegistries.ITEMS.getValue(spawnEggKey));
                        
                        // 如果获取到有效的生成蛋物品，则添加到掉落物中
                        if (!spawnEgg.isEmpty()) {
                            // 在实体位置创建物品实体
                            BlockPos entityPos = event.getEntity().blockPosition();
                            ItemEntity itemEntity = new ItemEntity(
                                event.getEntity().level(),
                                entityPos.getX() + 0.5,
                                entityPos.getY() + 0.5,
                                entityPos.getZ() + 0.5,
                                spawnEgg
                            );
                            
                            // 添加物品实体到世界
                            event.getEntity().level().addFreshEntity(itemEntity);
                        }
                    }
                }
            }
        }
    }
}