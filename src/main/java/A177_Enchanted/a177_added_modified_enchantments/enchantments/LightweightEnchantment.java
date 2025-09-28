package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
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
public class LightweightEnchantment extends Enchantment {
    // 重力修饰符的UUID
    private static final UUID GRAVITY_MODIFIER_UUID = UUID.fromString("C1D2E3F4-A5B6-C7D8-E9F0-1234567890AB");
    
    // 缓存玩家的附魔等级
    private static final Map<Integer, Integer> PLAYER_ENCHANTMENT_CACHE = new HashMap<>();
    
    // 缓存玩家的效果到期时间
    private static final Map<Integer, Long> PLAYER_EFFECT_END_TIME = new HashMap<>();
    
    // 效果持续时间（ticks）
    private static final int EFFECT_DURATION = 40; // 2秒 (40 ticks = 2秒)
    
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("lightweight");
    }

    public LightweightEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.ARMOR_FEET, new EquipmentSlot[]{EquipmentSlot.FEET});
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public int getMinCost(int level) {
        return 15 + (level - 1) * 15;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 15;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem armorItem && 
               armorItem.getEquipmentSlot() == EquipmentSlot.FEET;
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
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();
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
                removeLightweightEffects(player);
                PLAYER_EFFECT_END_TIME.remove(playerId);
            }
            
            // 只有在玩家拥有附魔时才处理效果
            if (enchantmentLevel > 0) {
                // 应用轻盈效果
                applyLightweightEffects(player, enchantmentLevel, currentTime);
                PLAYER_EFFECT_END_TIME.put(playerId, currentTime + EFFECT_DURATION);
            } else {
                // 移除效果
                removeLightweightEffects(player);
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
            
            // 获取玩家身上的附魔等级
            int enchantmentLevel = getTotalEnchantmentLevel(player);
            
            // 当装备被移除时，移除效果
            if (enchantmentLevel <= 0) {
                removeLightweightEffects(player);
            }
        }
    }
    
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        // 玩家登出时清理缓存
        Player player = event.getEntity();
        removeLightweightEffects(player);
        int playerId = player.getId();
        PLAYER_ENCHANTMENT_CACHE.remove(playerId);
        PLAYER_EFFECT_END_TIME.remove(playerId);
    }
    
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        // 玩家切换维度时清理缓存
        Player player = event.getEntity();
        removeLightweightEffects(player);
        int playerId = player.getId();
        PLAYER_ENCHANTMENT_CACHE.remove(playerId);
        PLAYER_EFFECT_END_TIME.remove(playerId);
    }
    
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        // 玩家重生时清理缓存
        Player player = event.getEntity();
        removeLightweightEffects(player);
        int playerId = player.getId();
        PLAYER_ENCHANTMENT_CACHE.remove(playerId);
        PLAYER_EFFECT_END_TIME.remove(playerId);
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
            totalLevel += feetArmor.getEnchantmentLevel(ModEnchantments.LIGHTWEIGHT.get());
        }
        
        return totalLevel;
    }

    /**
     * 应用轻盈附魔效果
     * @param player 玩家实体
     * @param enchantmentLevel 附魔等级
     * @param currentTime 当前游戏时间
     */
    private static void applyLightweightEffects(Player player, int enchantmentLevel, long currentTime) {
        // 使用AttributeModifier实现重力减少效果
        // 计算重力系数，每级减少20%，最多减少90%
        double gravityFactor = -(enchantmentLevel * 0.2);  // 每级减少20%重力
        gravityFactor = Math.max(-0.9, gravityFactor); // 最多减少90%重力
        
        AttributeInstance gravityAttribute = player.getAttribute(ForgeMod.ENTITY_GRAVITY.get());
        
        // 移除旧的修饰符（如果存在）
        AttributeModifier existingGravityModifier = gravityAttribute.getModifier(GRAVITY_MODIFIER_UUID);
        if (existingGravityModifier != null) {
            gravityAttribute.removeModifier(existingGravityModifier);
        }
        
        // 添加重力修饰符
        AttributeModifier gravityModifier = new AttributeModifier(
            GRAVITY_MODIFIER_UUID,
            "Lightweight Gravity",
            gravityFactor,
            AttributeModifier.Operation.MULTIPLY_BASE
        );
        gravityAttribute.addTransientModifier(gravityModifier);
        
        // 设置效果的到期时间
        PLAYER_EFFECT_END_TIME.put(player.getId(), currentTime + EFFECT_DURATION);
    }
    
    /**
     * 移除轻盈附魔效果
     * @param player 玩家实体
     */
    private static void removeLightweightEffects(Player player) {
        // 移除重力效果
        AttributeInstance gravityAttribute = player.getAttribute(ForgeMod.ENTITY_GRAVITY.get());
        AttributeModifier gravityModifier = gravityAttribute.getModifier(GRAVITY_MODIFIER_UUID);
        
        if (gravityModifier != null) {
            gravityAttribute.removeModifier(gravityModifier);
        }
    }
}