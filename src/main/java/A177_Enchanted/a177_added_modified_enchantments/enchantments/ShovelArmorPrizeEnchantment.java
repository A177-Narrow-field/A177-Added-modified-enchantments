package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber
public class ShovelArmorPrizeEnchantment extends Enchantment {
    private static final Random RANDOM = new Random();
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("shovel_armor_prize");
    }

    public ShovelArmorPrizeEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.DIGGER, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }// 可在附魔台发现
    
    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }// 可交易
    
    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }// 是否为宝藏附魔

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        // 限制只能在附魔台对铲子进行附魔
        return stack.getItem() instanceof ShovelItem && isDiscoverable();
    }

    @Override
    public int getMinCost(int level) {
        return 1 + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 20;
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在铲上
        return stack.getItem() instanceof ShovelItem;
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 检查受伤实体是否为生物实体且伤害来源是否为玩家
        if (event.getEntity() instanceof LivingEntity && event.getSource().getEntity() instanceof Player player) {
            LivingEntity target = (LivingEntity) event.getEntity();
            // 检查玩家主手装备是否有翘甲附魔
            ItemStack mainHandItem = player.getMainHandItem();
            int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.SHOVEL_ARMOR_PRIZE.get(), mainHandItem);

            // 如果有附魔且等级大于0
            if (level > 0) {
                // 计算触发概率（每级10%）
                double chance = level * 0.1;
                
                // 判断是否触发效果
                if (RANDOM.nextDouble() < chance) {
                    // 只在服务端执行
                    if (!target.level().isClientSide) {
                        // 遍历目标的所有装备槽位
                        for (EquipmentSlot slot : EquipmentSlot.values()) {
                            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                                ItemStack armor = target.getItemBySlot(slot);
                                // 如果槽位有装备，则将其添加到掉落物中
                                if (!armor.isEmpty()) {
                                    // 创建掉落物实体并添加到事件的掉落物列表中
                                    target.spawnAtLocation(armor);
                                    // 移除目标身上的装备
                                    target.setItemSlot(slot, ItemStack.EMPTY);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}