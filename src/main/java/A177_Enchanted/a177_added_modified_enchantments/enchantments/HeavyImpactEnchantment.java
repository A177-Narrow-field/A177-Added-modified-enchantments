package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
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
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

@Mod.EventBusSubscriber
public class HeavyImpactEnchantment extends Enchantment {
    public HeavyImpactEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.BOW, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("heavy_impact");
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinCost(int level) {
        return 20;
    }

    @Override
    public int getMaxCost(int level) {
        return 50;
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
        // 确保弓和弩都能在附魔台附魔
        return EnchantmentCategory.BOW.canEnchant(stack.getItem()) || 
               EnchantmentCategory.CROSSBOW.canEnchant(stack.getItem());
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 可以附在弓和弩上
        return stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem;
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        // 检查是否为箭的事件
        if (event.getEntity() instanceof AbstractArrow arrow && !arrow.level().isClientSide) {
            // 检查箭是否由玩家射出
            if (arrow.getOwner() instanceof net.minecraft.world.entity.player.Player player) {
                // 获取玩家使用的武器（弓或弩）
                ItemStack weapon = player.getMainHandItem();
                if (weapon.isEmpty() || !(weapon.getItem() instanceof BowItem || weapon.getItem() instanceof CrossbowItem)) {
                    weapon = player.getOffhandItem();
                }

                // 检查武器是否有沉击附魔
                int level = weapon.getEnchantmentLevel(ModEnchantments.HEAVY_IMPACT.get());
                if (level > 0) {
                    // 每级减少20%飞行速度
                    double velocityMultiplier = 1.0 - (level * 0.2);
                    
                    // 获取当前运动向量
                    var motion = arrow.getDeltaMovement();
                    
                    // 减少飞行速度
                    arrow.setDeltaMovement(motion.x * velocityMultiplier, motion.y * velocityMultiplier, motion.z * velocityMultiplier);
                    
                    // 每级增加80%伤害
                    arrow.setBaseDamage(arrow.getBaseDamage() * (1.0 + level * 0.8));
                }
            }
        }
    }
}