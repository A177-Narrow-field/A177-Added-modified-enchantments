package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

@Mod.EventBusSubscriber
public class WhisperEnchantment extends Enchantment {
    public WhisperEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.BOW, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMinCost(int level) {
        return 25;
    }

    @Override
    public int getMaxCost(int level) {
        return 50;
    }

    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("whisper");
    }

    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }// 是否为宝藏附魔

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }// 可在附魔台发现

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        // 参考StaggeringBlowEnchantment的实现方式，确保弩也能在附魔台附魔
        return EnchantmentCategory.BOW.canEnchant(stack.getItem()) || 
               EnchantmentCategory.CROSSBOW.canEnchant(stack.getItem());
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在弓或弩上
        return stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem;
    }

    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }// 是否可交易

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        // 检查是否为箭的撞击事件
        if (event.getProjectile() instanceof AbstractArrow arrow && !arrow.level().isClientSide) {
            // 检查撞击结果是否为实体
            if (event.getRayTraceResult() instanceof EntityHitResult entityHitResult) {
                // 检查被击中的实体是否为生物实体
                if (entityHitResult.getEntity() instanceof LivingEntity target) {
                    // 检查箭是否由玩家射出
                    if (arrow.getOwner() instanceof Player player) {
                        // 获取玩家使用的武器（弓或弩）
                        ItemStack weapon = player.getMainHandItem();
                        if (weapon.isEmpty() || !(weapon.getItem() instanceof BowItem || weapon.getItem() instanceof CrossbowItem)) {
                            weapon = player.getOffhandItem();
                        }

                        // 检查武器是否有轻语附魔
                        int level = weapon.getEnchantmentLevel(ModEnchantments.WHISPER.get());
                        if (level > 0) {
                            // 计算额外伤害：目标每1点护甲值增加2点伤害
                            float armorValue = target.getArmorValue();
                            float extraDamage = armorValue * 2.0f;
                            
                            // 增加伤害
                            target.hurt(player.damageSources().arrow(arrow, player), extraDamage);
                            
                            // 设置箭矢不穿透，防止回弹问题
                            arrow.setPierceLevel((byte) 0);
                            
                            // 设置箭矢在击中目标后不再存在，彻底解决回弹问题
                            arrow.discard();
                        }
                    }
                }
            }
        }
    }
}