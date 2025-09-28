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
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

@Mod.EventBusSubscriber
public class ToyBowEnchantment extends Enchantment {
    public ToyBowEnchantment() {
        super(Rarity.COMMON, EnchantmentCategory.BOW, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
    }

    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("toy_bow");
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMinCost(int level) {
        return 10;
    }

    @Override
    public int getMaxCost(int level) {
        return 30;
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
        // 可以应用于弓和弩
        return EnchantmentCategory.BOW.canEnchant(stack.getItem()) || stack.getItem() instanceof CrossbowItem;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在弓或弩上
        return stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem;
    }

    @SubscribeEvent
    public static void onArrowLoose(ArrowLooseEvent event) {
        // 检查使用的武器是否有玩具弓附魔
        Player player = event.getEntity();
        ItemStack weapon = event.getBow();
        
        if (weapon != null) {
            int level = weapon.getEnchantmentLevel(ModEnchantments.TOY_BOW.get());
            if (level > 0) {
            }
        }
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

                // 检查武器是否有玩具附魔
                int level = weapon.getEnchantmentLevel(ModEnchantments.TOY_BOW.get());
                if (level > 0) {
                    // 增加900%箭矢速度（即10倍速度）
                    double velocityMultiplier = 10.0;
                    arrow.setDeltaMovement(arrow.getDeltaMovement().scale(velocityMultiplier));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        // 检查箭是否由附魔武器射出
        if (event.getProjectile() instanceof AbstractArrow arrow) {
            if (arrow.getOwner() instanceof Player player) {
                // 获取玩家使用的武器（弓或弩）
                ItemStack weapon = player.getMainHandItem();
                if (weapon.isEmpty() || !(weapon.getItem() instanceof BowItem || weapon.getItem() instanceof CrossbowItem)) {
                    weapon = player.getOffhandItem();
                }

                // 检查武器是否有玩具弓附魔
                int level = weapon.getEnchantmentLevel(ModEnchantments.TOY_BOW.get());
                if (level > 0) {
                    // 减少99%的伤害
                    arrow.setBaseDamage(arrow.getBaseDamage() * 0.001);
                }
            }
        }
    }
}