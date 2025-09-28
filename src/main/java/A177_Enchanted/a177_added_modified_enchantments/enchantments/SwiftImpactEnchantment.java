package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.ArmorItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class SwiftImpactEnchantment extends Enchantment {

    private static final Map<UUID, SprintData> PLAYER_SPRINT_DATA = new HashMap<>();

    private static final int SPEED_INCREASE_INTERVAL = 10; // 0.5秒 (10 ticks)
    private static final float SPEED_INCREASE_PER_INTERVAL = 0.1f; // 10%每0.5秒
    private static final float BASE_IMPACT_DAMAGE = 1.0f;
    private static final int IMPACT_COOLDOWN = 20; // 1秒冷却 (20 ticks)
    private static final float MAX_SPEED_MULTIPLIER_PER_LEVEL = 1.0f; // 每级增加100%上限
    private static final float IMPACT_THRESHOLD = 2.0f; // 需要超过100%移速才能造成伤害

    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("swift_impact");
    }

    public SwiftImpactEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[]{EquipmentSlot.CHEST});
    }

    @Override
    public int getMinCost(int level) {
        return 10 + (level - 1) * 15;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 25;
    }

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }// 可在附魔台发现

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();}//确保在附魔台中可以正确应用

    @Override
    public int getMaxLevel() {
        return 5;
    }// 可附魔

    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }
    
    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附魔到胸甲上
        return stack.getItem() instanceof ArmorItem &&
                ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.CHEST;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Player player = event.player;
        UUID playerId = player.getUUID();

        // 检查胸甲是否有急速冲击附魔
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        int enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.SWIFT_IMPACT.get(), chestplate);

        // 如果没有附魔，确保移除所有效果并清理数据
        if (enchantmentLevel <= 0) {
            removeSpeedEffect(player);
            PLAYER_SPRINT_DATA.remove(playerId);
            return;
        }

        SprintData data = PLAYER_SPRINT_DATA.getOrDefault(playerId, new SprintData());
        boolean isSprinting = player.isSprinting();
        boolean isCrouching = player.isCrouching();

        // 每次tick都先移除速度效果，然后根据需要重新应用
        removeSpeedEffect(player);
        data.hasSpeedEffect = false;

        // 蹲下时中断疾跑和加速效果
        if (isCrouching) {
            if (data.wasSprinting) {
                // 从疾跑状态切换到蹲下，立即重置所有效果
                data.currentSpeedMultiplier = 1.0f;
                data.impactCooldowns.clear();
                data.lastSpeedIncreaseTick = 0;
            }
            // 蹲下时不处理任何加速效果
            data.wasSprinting = false;
            PLAYER_SPRINT_DATA.put(playerId, data);
            return;
        }

        if (isSprinting) {
            long currentTick = player.level().getGameTime();

            // 疾跑时增加速度
            if (currentTick - data.lastSpeedIncreaseTick >= SPEED_INCREASE_INTERVAL) {
                float maxSpeedMultiplier = 1.0f + (enchantmentLevel * MAX_SPEED_MULTIPLIER_PER_LEVEL);

                if (data.currentSpeedMultiplier < maxSpeedMultiplier) {
                    data.currentSpeedMultiplier = Math.min(maxSpeedMultiplier,
                            data.currentSpeedMultiplier + SPEED_INCREASE_PER_INTERVAL);
                    data.lastSpeedIncreaseTick = currentTick;
                }
            }

            // 应用速度效果（只在疾跑时）
            applySpeedEffect(player, data.currentSpeedMultiplier);
            data.hasSpeedEffect = true;

            // 检查碰撞（速度超过100%阈值）
            if (!player.level().isClientSide && data.currentSpeedMultiplier >= IMPACT_THRESHOLD) {
                checkForCollisions(player, data, chestplate);
            }
        } else {
            // 没有疾跑时重置速度倍数
            data.currentSpeedMultiplier = 1.0f;
            data.impactCooldowns.clear();
        }

        data.wasSprinting = isSprinting;
        PLAYER_SPRINT_DATA.put(playerId, data);
    }

    private static void applySpeedEffect(Player player, float speedMultiplier) {
        if (player.level().isClientSide) {
            return;
        }

        AttributeInstance speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute == null) {
            return;
        }

        // 计算速度加成
        float speedBonus = speedMultiplier - 1.0f;

        // 添加瞬态速度修饰符（每tick都会重新应用）
        AttributeModifier speedModifier = new AttributeModifier(
                SPEED_MODIFIER_UUID,
                "Swift Impact Speed Bonus",
                speedBonus,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );

        speedAttribute.addTransientModifier(speedModifier);
    }

    private static void removeSpeedEffect(Player player) {
        if (player.level().isClientSide) {
            return;
        }

        AttributeInstance speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute != null) {
            // 强制移除所有同UUID的修饰符
            try {
                speedAttribute.removeModifier(SPEED_MODIFIER_UUID);
            } catch (Exception e) {
                // 忽略异常，确保继续执行
            }
        }
    }

    private static void checkForCollisions(Player player, SprintData data, ItemStack chestplate) {
        long currentTick = player.level().getGameTime();

        // 使用固定的碰撞检测范围
        AABB collisionArea = player.getBoundingBox()
                .expandTowards(player.getLookAngle().scale(2.0))
                .inflate(0.1, 0.1, 0.1);

        List<LivingEntity> entities = player.level().getEntitiesOfClass(
                LivingEntity.class, collisionArea, e ->
                        e != player &&
                                e.isAlive() &&
                                !e.isInvulnerable() &&
                                (!(e instanceof Player) || !((Player)e).isCreative()) &&
                                (!(e instanceof Player) || !((Player)e).isSpectator())
        );

        boolean hitTarget = false;
        for (LivingEntity target : entities) {
            // 检查冷却时间
            Long lastImpactTime = data.impactCooldowns.get(target.getUUID());
            if (lastImpactTime != null && currentTick - lastImpactTime < IMPACT_COOLDOWN) {
                continue;
            }

            // 计算伤害：基础伤害1 + (速度倍数 - 阈值)的伤害
            float damage = BASE_IMPACT_DAMAGE + (data.currentSpeedMultiplier - IMPACT_THRESHOLD);
            target.hurt(player.damageSources().playerAttack(player), damage);

            // 添加击退效果
            // 计算击退方向（从玩家指向目标）
            double dx = target.getX() - player.getX();
            double dz = target.getZ() - player.getZ();
            
            // 标准化向量并乘以击退力度
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (distance > 0.0D) {
                double horizontalKnockbackStrength = 0.1 * (data.currentSpeedMultiplier - IMPACT_THRESHOLD);
                double verticalKnockbackStrength = 0.2 * (data.currentSpeedMultiplier - IMPACT_THRESHOLD); // 垂直击退力度
                target.push(dx / distance * horizontalKnockbackStrength, verticalKnockbackStrength, dz / distance * horizontalKnockbackStrength);
            }

            // 设置冷却时间
            data.impactCooldowns.put(target.getUUID(), currentTick);
            
            // 标记已击中目标
            hitTarget = true;

            // 只对第一个目标造成伤害
            break;
        }
        
        // 如果击中了目标，则消耗胸甲耐久度
        if (hitTarget && !player.isCreative() && !player.level().isClientSide) {
            // 计算需要消耗的耐久度（1%耐久度，最少1点）
            int maxDamage = chestplate.getMaxDamage();
            if (maxDamage > 0) {
                // 计算消耗值：1%的耐久度，最少为1点
                int consumeAmount = Math.max(1, maxDamage / 100);
                // 消耗耐久度
                if (chestplate.getDamageValue() + consumeAmount >= maxDamage) {
                    // 如果耐久度不足以支撑消耗，则破坏物品
                    chestplate.shrink(1);
                } else {
                    // 否则减少耐久度
                    chestplate.setDamageValue(chestplate.getDamageValue() + consumeAmount);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            long currentTick = event.getServer().overworld().getGameTime();

            // 清理过期的冷却时间
            PLAYER_SPRINT_DATA.values().forEach(data -> {
                data.impactCooldowns.entrySet().removeIf(entry ->
                        currentTick - entry.getValue() > IMPACT_COOLDOWN * 2
                );
            });
        }
    }

    private static class SprintData {
        public float currentSpeedMultiplier = 1.0f;
        public long lastSpeedIncreaseTick = 0;
        public boolean wasSprinting = false;
        public boolean hasSpeedEffect = false;
        public Map<UUID, Long> impactCooldowns = new HashMap<>();
    }

    @SubscribeEvent
    public static void onPlayerLogout(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        removeSpeedEffect(event.getEntity());
        PLAYER_SPRINT_DATA.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onPlayerLogin(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
        removeSpeedEffect(event.getEntity());
        PLAYER_SPRINT_DATA.remove(event.getEntity().getUUID());
    }
}