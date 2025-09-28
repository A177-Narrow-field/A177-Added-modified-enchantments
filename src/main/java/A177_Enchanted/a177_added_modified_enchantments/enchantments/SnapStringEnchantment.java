package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;

import java.util.Random;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

@Mod.EventBusSubscriber
public class SnapStringEnchantment extends Enchantment {
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("snap_string");
    }
    
    public SnapStringEnchantment() {
        super(Rarity.COMMON, EnchantmentCategory.BOW, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinCost(int level) {
        return 1 + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return 6 + (level - 1) * 10;
    }

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }
    
    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }
    
    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        // 确保弩也能在附魔台附魔
        return EnchantmentCategory.BOW.canEnchant(stack.getItem()) || 
               EnchantmentCategory.CROSSBOW.canEnchant(stack.getItem());
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在弓或弩上
        return stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem;
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        // 检查是否为箭的事件
        if (event.getEntity() instanceof AbstractArrow arrow && !arrow.level().isClientSide) {
            // 检查箭是否由玩家射出
            if (arrow.getOwner() instanceof Player player) {
                // 获取玩家使用的武器（弓或弩）
                ItemStack weapon = player.getMainHandItem();
                if (weapon.isEmpty() || !(weapon.getItem() instanceof BowItem || weapon.getItem() instanceof CrossbowItem)) {
                    weapon = player.getOffhandItem();
                }

                // 检查武器是否有断弦附魔
                int level = weapon.getEnchantmentLevel(ModEnchantments.SNAP_STRING.get());
                if (level > 0 && !player.isCreative()) {
                    // 每级增加20%概率
                    double probability = level * 0.2;
                    Random random = new Random();
                    
                    // 检查是否触发断弦效果
                    if (random.nextDouble() < probability) {
                        // 扣除30%耐久度
                        int currentDamage = weapon.getDamageValue();
                        int maxDamage = weapon.getMaxDamage();
                        if (maxDamage > 0) {
                            int damageToApply = Math.max(1, (int) (maxDamage * 0.3)); // 至少消耗1点耐久
                            weapon.setDamageValue(currentDamage + damageToApply);
                            
                            // 如果物品损坏，则移除它
                            if (currentDamage + damageToApply >= maxDamage) {
                                player.setItemInHand(player.getUsedItemHand(), ItemStack.EMPTY);
                            }
                        }
                    }
                }
            }
        }
    }
}