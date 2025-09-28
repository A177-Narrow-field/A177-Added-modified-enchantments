package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

@Mod.EventBusSubscriber
public class EnemyTauntEnchantment extends Enchantment {

    public EnemyTauntEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.BOW, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMinCost(int level) {
        return 20;
    }

    @Override
    public int getMaxCost(int level) {
        return 50;
    }

    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("enemy_taunt");
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
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在弓或弩上
        return stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem;
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        // 检查是否为箭的撞击事件
        if (event.getProjectile() instanceof AbstractArrow arrow && !arrow.level().isClientSide) {
            // 检查箭是否由玩家射出
            if (arrow.getOwner() instanceof Player player) {
                // 获取玩家使用的武器（弓或弩）
                ItemStack weapon = player.getMainHandItem();
                if (weapon.isEmpty() || !(weapon.getItem() instanceof BowItem || weapon.getItem() instanceof CrossbowItem)) {
                    weapon = player.getOffhandItem();
                }

                // 检查武器是否有敌引附魔
                int level = weapon.getEnchantmentLevel(ModEnchantments.ENEMY_TAUNT.get());
                if (level > 0) {
                    // 检查撞击结果是否为实体
                    if (event.getRayTraceResult() instanceof net.minecraft.world.phys.EntityHitResult entityHitResult) {
                        // 检查被击中的实体是否为生物实体
                        if (entityHitResult.getEntity() instanceof LivingEntity target) {
                            // 吸引目标周围6格范围内的敌人
                            attractNearbyEnemies(target);
                        }
                    }
                }
            }
        }
    }

    /**
     * 吸引目标周围6格范围内的敌人攻击目标
     * @param target 被击中的目标
     */
    private static void attractNearbyEnemies(LivingEntity target) {
        // 创建一个包围盒，覆盖目标周围的6格区域
        AABB boundingBox = target.getBoundingBox().inflate(6.0);
        
        // 获取范围内的所有生物实体
        for (Entity entity : target.level().getEntities(target, boundingBox)) {
            // 检查实体是否为怪物且不是玩家
            if (entity instanceof Mob mob && !(entity instanceof Player)) {
                // 检查怪物是否存活
                if (mob.isAlive()) {
                    // 设置怪物的目标为被击中的目标
                    mob.setTarget(target);
                }
            }
        }
    }
}