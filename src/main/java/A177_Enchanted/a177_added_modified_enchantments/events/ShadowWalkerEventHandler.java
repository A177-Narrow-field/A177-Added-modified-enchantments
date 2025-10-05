package A177_Enchanted.a177_added_modified_enchantments.events;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class ShadowWalkerEventHandler {
    
    // 缓存玩家的附魔等级
    private static final Map<UUID, Integer> PLAYER_ENCHANTMENT_CACHE = new HashMap<>();
    private static final Map<UUID, Integer> PLAYER_LIGHTBURN_ENCHANTMENT_CACHE = new HashMap<>();
    
    // 缓存玩家当前的亮度等级
    private static final Map<UUID, Integer> PLAYER_LIGHT_LEVEL_CACHE = new HashMap<>();
    
    // 缓存玩家的下次更新时间
    private static final Map<UUID, Long> PLAYER_NEXT_UPDATE_TIME = new HashMap<>();
    // 缓存玩家的下次燃烧时间
    private static final Map<UUID, Long> PLAYER_NEXT_BURN_TIME = new HashMap<>();
    
    // 更新间隔（ticks）
    private static final int UPDATE_INTERVAL = 5; // 0.25秒 (5 ticks = 0.25秒)
    // 燃烧间隔（ticks）
    private static final int BURN_INTERVAL = 20; // 1秒 (20 ticks = 1秒)
    
    // 最大速度加成百分比（每级附魔的加成）
    private static final double SHADOW_SPEED_BOOST_PER_LEVEL = 0.6; // 每级60%速度加成
    private static final double LIGHTBURN_SPEED_BOOST_PER_LEVEL = 0.3; // 每级30%速度加成
    
    // 速度修饰符的UUID
    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        
        Player player = event.player;
        
        // 只在服务端执行逻辑，避免客户端和服务端不一致的问题
        if (player.level().isClientSide()) {
            return;
        }
        
        UUID playerId = player.getUUID();
        long currentTime = player.level().getGameTime();
        
        // 获取缓存的暗影行者附魔等级，如果不存在则计算并缓存
        int shadowWalkerLevel = PLAYER_ENCHANTMENT_CACHE.getOrDefault(playerId, -1);
        if (shadowWalkerLevel == -1) {
            shadowWalkerLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.SHADOW_WALKER.get(), player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET));
            PLAYER_ENCHANTMENT_CACHE.put(playerId, shadowWalkerLevel);
        }
        
        // 获取缓存的光灼行者附魔等级，如果不存在则计算并缓存
        int lightburnWalkerLevel = PLAYER_LIGHTBURN_ENCHANTMENT_CACHE.getOrDefault(playerId, -1);
        if (lightburnWalkerLevel == -1) {
            lightburnWalkerLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.LIGHTBURN_WALKER.get(), player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET));
            PLAYER_LIGHTBURN_ENCHANTMENT_CACHE.put(playerId, lightburnWalkerLevel);
        }
        
        // 只有在有附魔时才处理效果
        if (shadowWalkerLevel > 0) {
            // 检查是否需要更新效果
            Long nextUpdateTime = PLAYER_NEXT_UPDATE_TIME.get(playerId);
            if (nextUpdateTime == null || currentTime >= nextUpdateTime) {
                applyShadowWalkerEffect(player, shadowWalkerLevel);
                PLAYER_NEXT_UPDATE_TIME.put(playerId, currentTime + UPDATE_INTERVAL);
            }
        } else if (lightburnWalkerLevel > 0) {
            // 检查是否需要更新效果
            Long nextUpdateTime = PLAYER_NEXT_UPDATE_TIME.get(playerId);
            if (nextUpdateTime == null || currentTime >= nextUpdateTime) {
                applyLightburnWalkerEffect(player, lightburnWalkerLevel);
                PLAYER_NEXT_UPDATE_TIME.put(playerId, currentTime + UPDATE_INTERVAL);
            }
            
            // 检查是否需要燃烧敌对生物
            Long nextBurnTime = PLAYER_NEXT_BURN_TIME.get(playerId);
            if (nextBurnTime == null || currentTime >= nextBurnTime) {
                applyLightburnWalkerBurnEffect(player, lightburnWalkerLevel);
                PLAYER_NEXT_BURN_TIME.put(playerId, currentTime + BURN_INTERVAL);
            }
        } else {
            removeWalkerEffect(player);
        }
    }
    
    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        // 当玩家装备的靴子发生变化时更新缓存
        if (event.getEntity() instanceof Player player && event.getSlot() == net.minecraft.world.entity.EquipmentSlot.FEET) {
            // 只在服务端更新缓存
            if (player.level().isClientSide()) {
                return;
            }
            
            UUID playerId = player.getUUID();
            // 从新的装备中获取附魔等级
            int shadowWalkerLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.SHADOW_WALKER.get(), event.getTo());
            int lightburnWalkerLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.LIGHTBURN_WALKER.get(), event.getTo());
            PLAYER_ENCHANTMENT_CACHE.put(playerId, shadowWalkerLevel);
            PLAYER_LIGHTBURN_ENCHANTMENT_CACHE.put(playerId, lightburnWalkerLevel);
            
            // 移除之前的效果，确保在更换或脱下鞋子时能正确清除效果
            removeWalkerEffect(player);
            
            // 如果新的附魔等级为0，确保效果被移除
            if (shadowWalkerLevel <= 0 && lightburnWalkerLevel <= 0) {
                removeWalkerEffect(player);
            }
        }
    }
    
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        // 玩家登出时清理缓存和效果
        Player player = event.getEntity();
        removeWalkerEffect(player);
        UUID playerId = player.getUUID();
        PLAYER_ENCHANTMENT_CACHE.remove(playerId);
        PLAYER_LIGHTBURN_ENCHANTMENT_CACHE.remove(playerId);
        PLAYER_LIGHT_LEVEL_CACHE.remove(playerId);
        PLAYER_NEXT_UPDATE_TIME.remove(playerId);
        PLAYER_NEXT_BURN_TIME.remove(playerId);
    }
    
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        // 玩家切换维度时清理缓存
        Player player = event.getEntity();
        removeWalkerEffect(player);
        UUID playerId = player.getUUID();
        PLAYER_ENCHANTMENT_CACHE.remove(playerId);
        PLAYER_LIGHTBURN_ENCHANTMENT_CACHE.remove(playerId);
        PLAYER_LIGHT_LEVEL_CACHE.remove(playerId);
        PLAYER_NEXT_UPDATE_TIME.remove(playerId);
        PLAYER_NEXT_BURN_TIME.remove(playerId);
    }
    
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        // 玩家重生时清理缓存
        Player player = event.getEntity();
        removeWalkerEffect(player);
        UUID playerId = player.getUUID();
        PLAYER_ENCHANTMENT_CACHE.remove(playerId);
        PLAYER_LIGHTBURN_ENCHANTMENT_CACHE.remove(playerId);
        PLAYER_LIGHT_LEVEL_CACHE.remove(playerId);
        PLAYER_NEXT_UPDATE_TIME.remove(playerId);
        PLAYER_NEXT_BURN_TIME.remove(playerId);
    }
    
    /**
     * 应用暗影行者效果
     * @param player 玩家实体
     * @param enchantmentLevel 附魔等级
     */
    private static void applyShadowWalkerEffect(Player player, int enchantmentLevel) {
        UUID playerId = player.getUUID();
        
        // 获取玩家所在位置的亮度
        int lightLevel = player.level().getMaxLocalRawBrightness(player.blockPosition());
        
        // 检查亮度是否发生变化，只有变化时才更新效果
        Integer cachedLightLevel = PLAYER_LIGHT_LEVEL_CACHE.get(playerId);
        if (cachedLightLevel == null || cachedLightLevel != lightLevel) {
            PLAYER_LIGHT_LEVEL_CACHE.put(playerId, lightLevel);
            
            // 根据亮度和附魔等级计算速度加成
            // 亮度为0时获得最大加成，亮度越高加成越少
            // 附魔等级越高，最大加成越多（1级60%，2级120%，3级180%）
            double maxSpeedBoost = SHADOW_SPEED_BOOST_PER_LEVEL * enchantmentLevel;
            double speedBoost = maxSpeedBoost * Math.max(0, (15 - lightLevel) / 15.0);
            
            AttributeInstance attribute = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
            
            // 移除旧的修饰符（如果存在）
            AttributeModifier existingModifier = attribute.getModifier(SPEED_MODIFIER_UUID);
            if (existingModifier != null) {
                attribute.removeModifier(existingModifier);
            }
            
            // 只有在速度加成大于0时才添加修饰符
            if (speedBoost > 0) {
                // 应用速度加成
                AttributeModifier modifier = new AttributeModifier(
                    SPEED_MODIFIER_UUID,
                    "Shadow Walker Speed Boost",
                    speedBoost,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_BASE
                );
                attribute.addTransientModifier(modifier);
            }
        }
    }
    
    /**
     * 应用光灼行者效果
     * @param player 玩家实体
     * @param enchantmentLevel 附魔等级
     */
    private static void applyLightburnWalkerEffect(Player player, int enchantmentLevel) {
        UUID playerId = player.getUUID();
        
        // 获取玩家所在位置的亮度
        int lightLevel = player.level().getMaxLocalRawBrightness(player.blockPosition());
        
        // 检查亮度是否发生变化，只有变化时才更新效果
        Integer cachedLightLevel = PLAYER_LIGHT_LEVEL_CACHE.get(playerId);
        if (cachedLightLevel == null || cachedLightLevel != lightLevel) {
            PLAYER_LIGHT_LEVEL_CACHE.put(playerId, lightLevel);
            
            // 根据亮度和附魔等级计算速度加成
            // 亮度为15时获得最大加成，亮度越低加成越少
            // 附魔等级越高，最大加成越多（1级30%，2级60%，3级90%）
            double maxSpeedBoost = LIGHTBURN_SPEED_BOOST_PER_LEVEL * enchantmentLevel;
            double speedBoost = maxSpeedBoost * Math.max(0, lightLevel / 15.0);
            
            AttributeInstance attribute = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
            
            // 移除旧的修饰符（如果存在）
            AttributeModifier existingModifier = attribute.getModifier(SPEED_MODIFIER_UUID);
            if (existingModifier != null) {
                attribute.removeModifier(existingModifier);
            }
            
            // 只有在速度加成大于0时才添加修饰符
            if (speedBoost > 0) {
                // 应用速度加成
                AttributeModifier modifier = new AttributeModifier(
                    SPEED_MODIFIER_UUID,
                    "Lightburn Walker Speed Boost",
                    speedBoost,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_BASE
                );
                attribute.addTransientModifier(modifier);
            }
        }
    }
    
    /**
     * 应用光灼行者燃烧效果
     * @param player 玩家实体
     * @param enchantmentLevel 附魔等级
     */
    private static void applyLightburnWalkerBurnEffect(Player player, int enchantmentLevel) {
        // 获取玩家所在位置的亮度
        int lightLevel = player.level().getMaxLocalRawBrightness(player.blockPosition());
        
        // 如果亮度大于10，则点燃周围的敌对生物
        if (lightLevel > 10) {
            int duration = 3 * enchantmentLevel; // 每级增加3秒，基础3秒
            
            // 获取周围的敌对生物
            AABB boundingBox = player.getBoundingBox().inflate(6.0D);
            List<LivingEntity> entities = player.level().getEntitiesOfClass(LivingEntity.class, boundingBox, 
                entity -> entity.isAlive() && entity instanceof net.minecraft.world.entity.monster.Enemy && !entity.equals(player));
            
            // 点燃敌对生物
            for (LivingEntity entity : entities) {
                entity.setSecondsOnFire(duration);
            }
        }
    }
    
    /**
     * 移除行者效果
     * @param player 玩家实体
     */
    private static void removeWalkerEffect(Player player) {
        AttributeInstance attribute = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
        AttributeModifier modifier = attribute.getModifier(SPEED_MODIFIER_UUID);
        
        if (modifier != null) {
            attribute.removeModifier(modifier);
        }
        
        // 清理缓存
        UUID playerId = player.getUUID();
        PLAYER_LIGHT_LEVEL_CACHE.remove(playerId);
        PLAYER_NEXT_UPDATE_TIME.remove(playerId);
        PLAYER_NEXT_BURN_TIME.remove(playerId);
    }
}