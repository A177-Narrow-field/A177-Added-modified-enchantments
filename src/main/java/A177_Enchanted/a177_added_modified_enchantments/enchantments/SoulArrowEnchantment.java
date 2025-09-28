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
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class SoulArrowEnchantment extends Enchantment {
    // 存储被标记的目标和对应的玩家
    private static final Map<UUID, MarkedTarget> MARKED_TARGETS = new HashMap<>();
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("soul_arrow");
    }
    
    // 标记目标信息类
    private static class MarkedTarget {
        final UUID playerUUID;
        final long markedTime;
        
        MarkedTarget(UUID playerUUID, long markedTime) {
            this.playerUUID = playerUUID;
            this.markedTime = markedTime;
        }
    }

    public SoulArrowEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.BOW, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
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
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMinCost(int level) {
        return 30;
    }

    @Override
    public int getMaxCost(int level) {
        return 50;
    }



    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        // 可以应用于弓和弩
        return EnchantmentCategory.BOW.canEnchant(stack.getItem()) || stack.getItem() instanceof CrossbowItem;
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
            if (arrow.getOwner() instanceof Player player) {
                // 获取玩家使用的武器（弓）
                ItemStack weapon = player.getMainHandItem();
                if (weapon.isEmpty() || !(weapon.getItem() instanceof BowItem)) {
                    weapon = player.getOffhandItem();
                }

                // 检查武器是否有游魂矢附魔
                int level = weapon.getEnchantmentLevel(ModEnchantments.SOUL_ARROW.get());
                if (level > 0) {
                    // 消耗5%耐久度
                    if (!player.isCreative()) {
                        int currentDamage = weapon.getDamageValue();
                        int maxDamage = weapon.getMaxDamage();
                        if (maxDamage > 0) {
                            int damageToApply = Math.max(1, (int) (maxDamage * 0.1)); // 至少消耗1点耐久
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

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        // 检查是否为箭的撞击事件
        if (event.getProjectile() instanceof AbstractArrow arrow && !arrow.level().isClientSide) {
            // 检查撞击结果是否为实体
            if (event.getRayTraceResult() instanceof EntityHitResult entityHitResult) {
                // 检查被击中的实体是否为生物实体
                Entity targetEntity = entityHitResult.getEntity();
                if (targetEntity instanceof LivingEntity target && !(target instanceof Player)) {
                    // 检查箭是否由玩家射出
                    if (arrow.getOwner() instanceof Player player) {
                        // 获取玩家使用的武器（弓）
                        ItemStack weapon = player.getMainHandItem();
                        if (weapon.isEmpty() || !(weapon.getItem() instanceof BowItem)) {
                            weapon = player.getOffhandItem();
                        }

                        // 检查武器是否有游魂矢附魔
                        int level = weapon.getEnchantmentLevel(ModEnchantments.SOUL_ARROW.get());
                        if (level > 0) {
                            // 造成目标最大生命值5%的真实伤害
                            float maxHealth = target.getMaxHealth();
                            float damage = maxHealth * 0.05f;
                            target.hurt(target.damageSources().magic(), damage);
                            
                            // 标记目标
                            if (!MARKED_TARGETS.containsKey(target.getUUID())) {
                                MARKED_TARGETS.put(target.getUUID(), new MarkedTarget(player.getUUID(), System.currentTimeMillis()));
                                
                                // 启动检查任务
                                checkTargetAfterDelay(target, player);
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 延迟检查目标状态
     */
    private static void checkTargetAfterDelay(LivingEntity target, Player player) {
        // 使用新的线程来处理3秒延迟检查
        new Thread(() -> {
            try {
                // 等待3秒
                Thread.sleep(3000);
                
                // 在主线程中执行检查和处理
                target.level().getServer().execute(() -> {
                    // 检查目标是否仍然存在且被标记
                    MarkedTarget markedTarget = MARKED_TARGETS.get(target.getUUID());
                    if (markedTarget != null) {
                        // 移除标记
                        MARKED_TARGETS.remove(target.getUUID());
                        
                        // 检查目标是否还活着
                        if (target.isAlive()) {
                            // 目标未死亡，扣除玩家10%血量
                            deductPlayerHealth(player);
                        } else {
                            // 目标已死亡，恢复玩家15%血量
                            restorePlayerHealth(player);
                        }
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    /**
     * 恢复玩家10%血量
     */
    private static void restorePlayerHealth(Player player) {
        if (!player.level().isClientSide && player.isAlive()) {
            float maxHealth = player.getMaxHealth();
            float healAmount = maxHealth * 0.1f;
            player.heal(healAmount);
        }
    }
    
    /**
     * 扣除玩家20%血量
     */
    private static void deductPlayerHealth(Player player) {
        if (!player.level().isClientSide && player.isAlive()) {
            float maxHealth = player.getMaxHealth();
            float damage = maxHealth * 0.2f;
            player.hurt(player.damageSources().magic(), damage);
        }
    }
}