package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.Entity;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber
public class BounceEnchantment extends Enchantment {
    // 用于跟踪已弹射的目标，防止重复弹射
    private static final Set<UUID> BOUNCED_ENTITIES = new HashSet<>();
    
    // 用于跟踪已处理的箭矢，防止重复处理
    private static final Set<UUID> PROCESSED_ARROWS = new HashSet<>();
    
    // 用于标记箭矢为弹射箭矢，避免无限循环
    private static final String BOUNCE_ARROW_TAG = "BounceArrow";

    public BounceEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.BOW, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
    }

    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("bounce");
    }

    @Override
    public int getMaxLevel() {
        return 5;
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
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }

    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }

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

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        // 检查是否为箭的撞击事件
        if (event.getProjectile() instanceof AbstractArrow arrow && !arrow.level().isClientSide) {
            // 检查是否为弹射箭矢，避免无限循环
            if (arrow.getPersistentData().getBoolean(BOUNCE_ARROW_TAG)) {
                return;
            }
            
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

                        // 检查武器是否有弹射附魔
                        int level = weapon.getEnchantmentLevel(ModEnchantments.BOUNCE.get());
                        if (level > 0 && !BOUNCED_ENTITIES.contains(target.getUUID()) && !PROCESSED_ARROWS.contains(arrow.getUUID())) {
                            // 标记箭矢已被处理
                            PROCESSED_ARROWS.add(arrow.getUUID());
                            
                            // 标记当前目标已被弹射过
                            BOUNCED_ENTITIES.add(target.getUUID());
                            
                            // 先让箭矢正常命中第一个目标
                            // 然后创建新的箭矢弹射到其他目标
                            bounceToNearbyTargets(arrow, target, level);
                            
                            // 清理标记列表
                            BOUNCED_ENTITIES.clear();
                            PROCESSED_ARROWS.remove(arrow.getUUID());
                        }
                    }
                }
            }
        }
    }

    /**
     * 寻找附近的目标并弹射箭矢
     * @param originalArrow 原始箭矢
     * @param target 当前目标
     * @param level 附魔等级
     */
    private static void bounceToNearbyTargets(AbstractArrow originalArrow, LivingEntity target, int level) {
        LivingEntity currentTarget = target;
        Set<UUID> alreadyBouncedInThisChain = new HashSet<>(); // 记录本次弹射链中已处理的目标
        alreadyBouncedInThisChain.add(currentTarget.getUUID()); // 添加初始目标
        
        for (int i = 0; i < level; i++) {
            LivingEntity nextTarget = findNearestEntity(originalArrow, currentTarget);
            if (nextTarget != null && !alreadyBouncedInThisChain.contains(nextTarget.getUUID())) {
                // 标记目标已在本次弹射链中处理过
                alreadyBouncedInThisChain.add(nextTarget.getUUID());
                
                // 计算从当前目标到下一个目标的方向向量
                Vec3 targetPos = nextTarget.position().add(0, nextTarget.getBbHeight() / 2.0, 0);
                Vec3 currentPos = currentTarget.position().add(0, currentTarget.getBbHeight() / 2.0, 0);
                Vec3 direction = targetPos.subtract(currentPos).normalize().scale(1.5);
                
                // 创建新的箭矢用于弹射
                AbstractArrow bouncedArrow = (AbstractArrow) originalArrow.getType().create(originalArrow.level());
                if (bouncedArrow != null) {
                    // 标记为弹射箭矢，避免无限循环
                    bouncedArrow.getPersistentData().putBoolean(BOUNCE_ARROW_TAG, true);
                    
                    // 设置箭矢属性
                    bouncedArrow.setOwner(originalArrow.getOwner());
                    bouncedArrow.setPierceLevel(originalArrow.getPierceLevel());
                    bouncedArrow.setBaseDamage(originalArrow.getBaseDamage());
                    bouncedArrow.setCritArrow(originalArrow.isCritArrow());
                    
                    // 设置箭矢的位置和速度
                    bouncedArrow.setPos(currentPos);
                    bouncedArrow.setDeltaMovement(direction);
                    
                    // 将新箭矢添加到世界中
                    originalArrow.level().addFreshEntity(bouncedArrow);
                }
                
                // 更新当前目标
                currentTarget = nextTarget;
            } else {
                // 没有找到更多目标，跳出循环
                break;
            }
        }
    }

    /**
     * 查找附近最近的生物实体
     * @param arrow 箭矢
     * @param target 当前目标
     * @return 最近的生物实体，如果找不到则返回null
     */
    private static LivingEntity findNearestEntity(AbstractArrow arrow, LivingEntity target) {
        LivingEntity nearestEntity = null;
        double nearestDistance = Double.MAX_VALUE;

        // 获取附近的所有生物实体
        for (Entity entity : arrow.level().getEntities(target, target.getBoundingBox().inflate(6.0))) {
            // 检查实体是否为生物实体，且不是玩家，不是当前目标，存活状态，且未被弹射过
            if (entity instanceof LivingEntity livingEntity && 
                !(livingEntity instanceof Player) &&  // 排除玩家
                !livingEntity.is(target) && 
                livingEntity.isAlive() && 
                !BOUNCED_ENTITIES.contains(livingEntity.getUUID())) {
                
                double distance = target.distanceTo(livingEntity);
                if (distance <= 6.0 && distance < nearestDistance) {
                    nearestDistance = distance;
                    nearestEntity = livingEntity;
                }
            }
        }

        return nearestEntity;
    }
}