package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class PrimitiveLightweightEnchantment extends Enchantment {
    // 重力修饰符的UUID
    private static final UUID GRAVITY_MODIFIER_UUID = UUID.fromString("A1B2C3D4-E5F6-7890-ABCD-EF1234567890");
    
    // 缓存玩家的附魔等级
    private static final Map<Integer, Integer> PLAYER_ENCHANTMENT_CACHE = new HashMap<>();
    
    // 缓存玩家的效果到期时间
    private static final Map<Integer, Long> PLAYER_EFFECT_END_TIME = new HashMap<>();
    
    // 效果持续时间（ticks）
    private static final int EFFECT_DURATION = 40; // 2秒 (40 ticks = 2秒)
    
    // 每级附魔的饥饿消耗速度增加百分比 (1%)
    private static final double HUNGER_CONSUMPTION_PER_LEVEL = 0.01;
    
    // 跟踪玩家是否处于蹲下重置状态
    private static final Map<Integer, Boolean> PLAYER_CROUCH_RESET_STATE = new HashMap<>();
    
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("primitive_lightweight");
    }

    public PrimitiveLightweightEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentCategory.ARMOR_FEET, new EquipmentSlot[]{EquipmentSlot.FEET});
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public int getMinCost(int level) {
        return 10 + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 10;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem armorItem && 
               armorItem.getEquipmentSlot() == EquipmentSlot.FEET;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();
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
    protected boolean checkCompatibility(Enchantment other) {
        // 不允许与轻盈附魔共存
        return super.checkCompatibility(other) && other != ModEnchantments.LIGHTWEIGHT.get();
    }
    
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Player player) {
            int playerId = player.getId();
            long currentTime = player.level().getGameTime();
            
            // 获取玩家身上的附魔等级
            int enchantmentLevel = getTotalEnchantmentLevel(player);
            
            // 更新缓存
            PLAYER_ENCHANTMENT_CACHE.put(playerId, enchantmentLevel);
            
            // 检查效果是否应该结束
            Long effectEndTime = PLAYER_EFFECT_END_TIME.get(playerId);
            if (effectEndTime != null && currentTime >= effectEndTime) {
                removePrimitiveLightweightEffects(player);
                PLAYER_EFFECT_END_TIME.remove(playerId);
            }
            
            // 只有在玩家拥有附魔时才处理效果
            if (enchantmentLevel > 0) {
                // 应用初级轻盈效果
                applyPrimitiveLightweightEffects(player, enchantmentLevel, currentTime);
                PLAYER_EFFECT_END_TIME.put(playerId, currentTime + EFFECT_DURATION);
            } else {
                // 移除效果
                removePrimitiveLightweightEffects(player);
                PLAYER_EFFECT_END_TIME.remove(playerId);
            }
        }
    }
    
    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            int playerId = player.getId();
            
            // 清理缓存以强制更新效果
            PLAYER_ENCHANTMENT_CACHE.remove(playerId);
            PLAYER_EFFECT_END_TIME.remove(playerId);
            PLAYER_CROUCH_RESET_STATE.remove(playerId);
            
            // 获取玩家身上的附魔等级
            int enchantmentLevel = getTotalEnchantmentLevel(player);
            
            // 当装备被移除时，移除效果
            if (enchantmentLevel <= 0) {
                removePrimitiveLightweightEffects(player);
            }
        }
    }
    
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        // 玩家登出时清理缓存
        Player player = event.getEntity();
        removePrimitiveLightweightEffects(player);
        int playerId = player.getId();
        PLAYER_ENCHANTMENT_CACHE.remove(playerId);
        PLAYER_EFFECT_END_TIME.remove(playerId);
        PLAYER_CROUCH_RESET_STATE.remove(playerId);
    }
    
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        // 玩家切换维度时清理缓存
        Player player = event.getEntity();
        removePrimitiveLightweightEffects(player);
        int playerId = player.getId();
        PLAYER_ENCHANTMENT_CACHE.remove(playerId);
        PLAYER_EFFECT_END_TIME.remove(playerId);
        PLAYER_CROUCH_RESET_STATE.remove(playerId);
    }
    
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        // 玩家重生时清理缓存
        Player player = event.getEntity();
        removePrimitiveLightweightEffects(player);
        int playerId = player.getId();
        PLAYER_ENCHANTMENT_CACHE.remove(playerId);
        PLAYER_EFFECT_END_TIME.remove(playerId);
        PLAYER_CROUCH_RESET_STATE.remove(playerId);
    }
    
    /**
     * 计算玩家身上所有相关装备的附魔等级总和
     * @param player 玩家实体
     * @return 附魔等级总和
     */
    private static int getTotalEnchantmentLevel(Player player) {
        int totalLevel = 0;
        
        // 检查足部装备
        ItemStack feetArmor = player.getItemBySlot(EquipmentSlot.FEET);
        if (feetArmor.getItem() instanceof ArmorItem) {
            totalLevel += feetArmor.getEnchantmentLevel(ModEnchantments.PRIMITIVE_LIGHTWEIGHT.get());
        }
        
        return totalLevel;
    }

    /**
     * 应用初级轻盈附魔效果
     * @param player 玩家实体
     * @param enchantmentLevel 附魔等级
     * @param currentTime 当前游戏时间
     */
    private static void applyPrimitiveLightweightEffects(Player player, int enchantmentLevel, long currentTime) {
        // 使用AttributeModifier实现重力减少效果
        // 计算重力系数，每级减少10%
        double gravityFactor = -(enchantmentLevel * 0.15);  // 每级减少15%重力
        
        AttributeInstance gravityAttribute = player.getAttribute(ForgeMod.ENTITY_GRAVITY.get());
        
        // 移除旧的修饰符（如果存在）
        AttributeModifier existingGravityModifier = gravityAttribute.getModifier(GRAVITY_MODIFIER_UUID);
        if (existingGravityModifier != null) {
            gravityAttribute.removeModifier(existingGravityModifier);
        }
        
        // 添加重力修饰符
        AttributeModifier gravityModifier = new AttributeModifier(
            GRAVITY_MODIFIER_UUID,
            "Primitive Lightweight Gravity",
            gravityFactor,
            AttributeModifier.Operation.MULTIPLY_BASE
        );
        gravityAttribute.addTransientModifier(gravityModifier);
        
        // 增加饥饿消耗速度 (每级10%)
        float exhaustion = player.getFoodData().getExhaustionLevel();
        if (exhaustion > 0) {
            float increasedExhaustion = exhaustion * (1.0f + (float)(enchantmentLevel * HUNGER_CONSUMPTION_PER_LEVEL));
            player.getFoodData().setExhaustion(increasedExhaustion);
        }
        
        // 设置效果的到期时间
        PLAYER_EFFECT_END_TIME.put(player.getId(), currentTime + EFFECT_DURATION);
    }
    
    /**
     * 移除初级轻盈附魔效果
     * @param player 玩家实体
     */
    private static void removePrimitiveLightweightEffects(Player player) {
        // 移除重力效果
        AttributeInstance gravityAttribute = player.getAttribute(ForgeMod.ENTITY_GRAVITY.get());
        AttributeModifier gravityModifier = gravityAttribute.getModifier(GRAVITY_MODIFIER_UUID);
        
        if (gravityModifier != null) {
            gravityAttribute.removeModifier(gravityModifier);
        }
        
        // 饥饿度消耗速度效果通过属性修饰符无法直接实现
        // 因为原版Minecraft没有直接控制饥饿度消耗速度的属性
    }
    
    /**
     * 重置重力到正常值
     * @param player 玩家实体
     */
    private static void resetGravity(Player player) {
        // 重置重力相关状态
        // 注意：由于轻盈附魔使用的是属性修饰符，重置操作主要通过移除修饰符完成
        // 这里可以添加额外的重力相关重置逻辑
    }
}