package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class StomachPouchEnchantment extends Enchantment {
    // 缓存玩家的附魔等级
    private static final Map<UUID, Integer> PLAYER_ENCHANTMENT_CACHE = new HashMap<>();
    
    // 每级附魔的伤害减免百分比 (15%)
    private static final double DAMAGE_REDUCTION_PER_LEVEL = 0.15;
    
    // 每级附魔的饥饿消耗速度增加百分比 (20%)
    private static final double HUNGER_CONSUMPTION_PER_LEVEL = 0.2;

    // 更新间隔（ticks）
    private static final int UPDATE_INTERVAL = 20; // 1秒 (20 ticks = 1秒)
    
    // 减速效果持续时间（ticks）
    private static final int SLOWNESS_DURATION = 40; // 2秒 (40 ticks = 2秒)
    
    // 重力效果持续时间（ticks）
    private static final int GRAVITY_DURATION = 40; // 2秒 (40 ticks = 2秒)
    
    // 缓存玩家的下次更新时间
    private static final Map<UUID, Long> PLAYER_NEXT_UPDATE_TIME = new HashMap<>();
    
    // 减速修饰符的UUID
    private static final UUID SLOWNESS_MODIFIER_UUID = UUID.fromString("E1F2A3B4-C5D6-7890-ABCD-EF1234567891");
    
    // 重力修饰符的UUID
    private static final UUID GRAVITY_MODIFIER_UUID = UUID.fromString("F2A3B4C5-D6E7-8901-BCDE-F01234567892");
    
    // 玩家减速效果的到期时间缓存
    private static final Map<UUID, Long> PLAYER_SLOWNESS_END_TIME = new HashMap<>();
    
    // 玩家重力效果的到期时间缓存
    private static final Map<UUID, Long> PLAYER_GRAVITY_END_TIME = new HashMap<>();


    public StomachPouchEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[]{EquipmentSlot.CHEST});
    }
    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public int getMinCost(int level) {
        return 20 + (level - 1) * 20;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 30;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem && 
               ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.CHEST;
    }

    @Override
    public boolean isTreasureOnly() {
        return AllEnchantmentsConfig.STOMACH_POUCH.isTreasureOnly.get();
    }

    @Override
    public boolean isDiscoverable() {
        return AllEnchantmentsConfig.STOMACH_POUCH.isDiscoverable.get();
    }// 可在附魔台发现

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem());}//确保在附魔台中可以正确应用

    @Override
    public boolean isTradeable() {
        return AllEnchantmentsConfig.STOMACH_POUCH.isTradeable.get();
    }//可通过交易获得
    
    // 与生命格挡和简易格挡附魔冲突
    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return super.checkCompatibility(other) 
                && other != ModEnchantments.LIFE_BLOCK.get()
                && other != ModEnchantments.SIMPLE_BLOCK.get();
    }

    
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 处理伤害减免效果
        if (event.getEntity() instanceof Player player) {
            // 只在服务端执行逻辑
            if (player.level().isClientSide()) {
                return;
            }
            
            UUID playerId = player.getUUID();
            
            // 获取缓存的附魔等级，如果不存在则计算并缓存
            int enchantmentLevel = PLAYER_ENCHANTMENT_CACHE.getOrDefault(playerId, -1);
            if (enchantmentLevel == -1) {
                enchantmentLevel = player.getItemBySlot(EquipmentSlot.CHEST).getEnchantmentLevel(ModEnchantments.STOMACH_POUCH.get());
                PLAYER_ENCHANTMENT_CACHE.put(playerId, enchantmentLevel);
            }
            
            if (enchantmentLevel > 0) {
                // 计算伤害减免 (每级10%)
                double damageReduction = Math.min(1.0, enchantmentLevel * DAMAGE_REDUCTION_PER_LEVEL);
                float reducedDamage = event.getAmount() * (1.0f - (float) damageReduction);
                event.setAmount(reducedDamage);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingTickEvent event) {
        // 处理饥饿消耗速度增加和减速效果
        if (event.getEntity() instanceof Player player) {
            // 只在服务端应用效果，避免客户端和服务端同步问题
            if (player.level().isClientSide()) {
                return;
            }
            
            UUID playerId = player.getUUID();
            long currentTime = player.level().getGameTime();
            
            // 获取缓存的附魔等级，如果不存在则计算并缓存
            int enchantmentLevel = PLAYER_ENCHANTMENT_CACHE.getOrDefault(playerId, -1);
            if (enchantmentLevel == -1) {
                enchantmentLevel = player.getItemBySlot(EquipmentSlot.CHEST).getEnchantmentLevel(ModEnchantments.STOMACH_POUCH.get());
                PLAYER_ENCHANTMENT_CACHE.put(playerId, enchantmentLevel);
            }
            
            // 检查减速效果是否应该结束
            Long slownessEndTime = PLAYER_SLOWNESS_END_TIME.get(playerId);
            if (slownessEndTime != null && currentTime >= slownessEndTime) {
                removeStomachPouchEffects(player);
                PLAYER_SLOWNESS_END_TIME.remove(playerId);
            }
            
            // 只有在玩家拥有附魔时才处理效果
            if (enchantmentLevel > 0) {
                // 检查是否需要更新效果
                Long nextUpdateTime = PLAYER_NEXT_UPDATE_TIME.get(playerId);
                if (nextUpdateTime == null || currentTime >= nextUpdateTime) {
                    applyStomachPouchEffects(player, enchantmentLevel, currentTime);
                    PLAYER_NEXT_UPDATE_TIME.put(playerId, currentTime + UPDATE_INTERVAL);
                }
            } else {
                // 移除减速效果
                removeStomachPouchEffects(player);
                PLAYER_SLOWNESS_END_TIME.remove(playerId);
            }
        }
    }
    
    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        // 当玩家装备发生变化时更新缓存
        if (event.getEntity() instanceof Player player && event.getSlot() == EquipmentSlot.CHEST) {
            // 只在服务端执行清理
            if (player.level().isClientSide()) {
                return;
            }
            
            UUID playerId = player.getUUID();
            int enchantmentLevel = player.getItemBySlot(EquipmentSlot.CHEST).getEnchantmentLevel(ModEnchantments.STOMACH_POUCH.get());
            PLAYER_ENCHANTMENT_CACHE.put(playerId, enchantmentLevel);
            
            // 立即清理下次更新时间缓存以强制更新效果
            PLAYER_NEXT_UPDATE_TIME.remove(playerId);
            PLAYER_SLOWNESS_END_TIME.remove(playerId);
            PLAYER_GRAVITY_END_TIME.remove(playerId);

            // 当装备被移除时，移除减速效果
            if (enchantmentLevel <= 0) {
                removeStomachPouchEffects(player);
            }
        }
    }
    
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        // 玩家登出时清理缓存
        Player player = event.getEntity();
        // 只在服务端执行清理
        if (!player.level().isClientSide()) {
            removeStomachPouchEffects(player);
            UUID playerId = player.getUUID();
            PLAYER_ENCHANTMENT_CACHE.remove(playerId);
            PLAYER_NEXT_UPDATE_TIME.remove(playerId);
            PLAYER_SLOWNESS_END_TIME.remove(playerId);
            PLAYER_GRAVITY_END_TIME.remove(playerId);
        }
    }
    
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        // 玩家切换维度时清理缓存
        Player player = event.getEntity();
        // 只在服务端执行清理
        if (!player.level().isClientSide()) {
            removeStomachPouchEffects(player);
            UUID playerId = player.getUUID();
            PLAYER_ENCHANTMENT_CACHE.remove(playerId);
            PLAYER_NEXT_UPDATE_TIME.remove(playerId);
            PLAYER_SLOWNESS_END_TIME.remove(playerId);
            PLAYER_GRAVITY_END_TIME.remove(playerId);
        }
    }
    
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        // 玩家重生时清理缓存
        Player player = event.getEntity();
        // 只在服务端执行清理
        if (!player.level().isClientSide()) {
            removeStomachPouchEffects(player);
            UUID playerId = player.getUUID();
            PLAYER_ENCHANTMENT_CACHE.remove(playerId);
            PLAYER_NEXT_UPDATE_TIME.remove(playerId);
            PLAYER_SLOWNESS_END_TIME.remove(playerId);
            PLAYER_GRAVITY_END_TIME.remove(playerId);
        }
    }
    
    /**
     * 应用胃袋附魔效果
     * @param player 玩家实体
     * @param enchantmentLevel 附魔等级
     * @param currentTime 当前游戏时间
     */
    private static void applyStomachPouchEffects(Player player, int enchantmentLevel, long currentTime) {
        // 增加饥饿消耗速度 (每级20%)
        float exhaustion = player.getFoodData().getExhaustionLevel();
        if (exhaustion > 0) {
            float increasedExhaustion = exhaustion * (1.0f + (float)(enchantmentLevel * HUNGER_CONSUMPTION_PER_LEVEL));
            player.getFoodData().setExhaustion(increasedExhaustion);
        }
        
        // 使用AttributeModifier实现减速效果
        // 计算减速系数，基于附魔等级
        double slowFactor = -(enchantmentLevel * 0.1);  // 每级减少10%速度
        slowFactor = Math.max(-0.5, slowFactor); // 最多减少50%速度
        
        AttributeInstance speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        
        // 移除旧的修饰符（如果存在）
        AttributeModifier existingSpeedModifier = speedAttribute.getModifier(SLOWNESS_MODIFIER_UUID);
        if (existingSpeedModifier != null) {
            speedAttribute.removeModifier(existingSpeedModifier);
        }
        
        // 添加减速修饰符
        AttributeModifier speedModifier = new AttributeModifier(
            SLOWNESS_MODIFIER_UUID,
            "Stomach Pouch Slowdown",
            slowFactor,
            AttributeModifier.Operation.MULTIPLY_BASE
        );
        speedAttribute.addTransientModifier(speedModifier);
        
        // 设置减速效果的到期时间（2秒后）
        PLAYER_SLOWNESS_END_TIME.put(player.getUUID(), currentTime + SLOWNESS_DURATION);
        
        // 使用AttributeModifier实现重力效果
        // 计算重力系数，基于附魔等级
        double gravityFactor = enchantmentLevel * 0.1;  // 每级增加10%重力
        
        AttributeInstance gravityAttribute = player.getAttribute(ForgeMod.ENTITY_GRAVITY.get());
        
        // 移除旧的修饰符（如果存在）
        AttributeModifier existingGravityModifier = gravityAttribute.getModifier(GRAVITY_MODIFIER_UUID);
        if (existingGravityModifier != null) {
            gravityAttribute.removeModifier(existingGravityModifier);
        }
        
        // 添加重力修饰符
        AttributeModifier gravityModifier = new AttributeModifier(
            GRAVITY_MODIFIER_UUID,
            "Stomach Pouch Gravity",
            gravityFactor,
            AttributeModifier.Operation.MULTIPLY_BASE
        );
        gravityAttribute.addTransientModifier(gravityModifier);
        
        // 设置重力效果的到期时间（2秒后）
        PLAYER_GRAVITY_END_TIME.put(player.getUUID(), currentTime + GRAVITY_DURATION);
    }
    
    /**
     * 移除胃袋附魔效果
     * @param player 玩家实体
     */
    private static void removeStomachPouchEffects(Player player) {
        // 移除减速效果
        AttributeInstance speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeModifier speedModifier = speedAttribute.getModifier(SLOWNESS_MODIFIER_UUID);
        
        if (speedModifier != null) {
            speedAttribute.removeModifier(speedModifier);
        }
        
        // 移除重力效果
        AttributeInstance gravityAttribute = player.getAttribute(ForgeMod.ENTITY_GRAVITY.get());
        AttributeModifier gravityModifier = gravityAttribute.getModifier(GRAVITY_MODIFIER_UUID);
        
        if (gravityModifier != null) {
            gravityAttribute.removeModifier(gravityModifier);
        }
    }
}