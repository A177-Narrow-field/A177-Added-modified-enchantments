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
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Mod.EventBusSubscriber
public class GluttonousPouchEnchantment extends Enchantment {
    // 缓存玩家的附魔等级
    private static final Map<UUID, Integer> PLAYER_ENCHANTMENT_CACHE = new HashMap<>();
    
    // 基础格挡概率 (20%)
    private static final double BASE_BLOCK_CHANCE = 0.2;
    
    // 每级格挡概率提升 (15%)
    private static final double BLOCK_CHANCE_PER_LEVEL = 0.15;
    
    // 基础饥饿消耗 (3点)
    private static final int BASE_HUNGER_COST = 2;
    
    // 每级饥饿消耗增加 (2点)
    private static final int HUNGER_COST_PER_LEVEL = 2;
    
    // 缓存玩家的下次更新时间
    private static final Map<UUID, Long> PLAYER_NEXT_UPDATE_TIME = new HashMap<>();
    
    // 更新间隔（ticks）
    private static final int UPDATE_INTERVAL = 20; // 1秒 (20 ticks = 1秒)
    
    // 减速效果持续时间（ticks）
    private static final int SLOWNESS_DURATION = 40; // 2秒 (40 ticks = 2秒)
    
    // 重力效果持续时间（ticks）
    private static final int GRAVITY_DURATION = 40; // 2秒 (40 ticks = 2秒)
    
    // 随机数生成器
    private static final Random RANDOM = new Random();
    
    
    // 减速修饰符的UUID
    private static final UUID SLOWNESS_MODIFIER_UUID = UUID.fromString("D4F6E1B7-8A2D-4C1A-B9E5-0F2A8C5D9E3F");
    
    // 重力修饰符的UUID
    private static final UUID GRAVITY_MODIFIER_UUID = UUID.fromString("E5A7F2C8-9B3E-5D2B-C0F6-1A3B9D6E0F4A");
    
    // 玩家减速效果的到期时间缓存
    private static final Map<UUID, Long> PLAYER_SLOWNESS_END_TIME = new HashMap<>();
    
    // 玩家重力效果的到期时间缓存
    private static final Map<UUID, Long> PLAYER_GRAVITY_END_TIME = new HashMap<>();

    public GluttonousPouchEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[]{EquipmentSlot.CHEST});
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public int getMinCost(int level) {
        return 20 + (level - 1) * 15;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 20;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem && 
               ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.CHEST;
    }

    @Override
    public boolean isTreasureOnly() {
        return AllEnchantmentsConfig.GLUTTONOUS_POUCH.isTreasureOnly.get();
    }

    @Override
    public boolean isDiscoverable() {
        return AllEnchantmentsConfig.GLUTTONOUS_POUCH.isDiscoverable.get();
    }// 可在附魔台发现

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem());}//确保在附魔台中可以正确应用

    @Override
    public boolean isTradeable() {
        return AllEnchantmentsConfig.GLUTTONOUS_POUCH.isTradeable.get();//是交易
    }
    
    // 与胃袋附魔冲突
    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return super.checkCompatibility(other) && 
               other != ModEnchantments.STOMACH_POUCH.get() &&
               !(other instanceof net.minecraft.world.item.enchantment.ProtectionEnchantment) &&
               !(other instanceof net.minecraft.world.item.enchantment.ThornsEnchantment);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 处理格挡效果，但不处理摔落伤害
        if (event.getEntity() instanceof Player player) {
            UUID playerId = player.getUUID();
            
            // 获取缓存的附魔等级，如果不存在则计算并缓存
            int enchantmentLevel = PLAYER_ENCHANTMENT_CACHE.getOrDefault(playerId, -1);
            if (enchantmentLevel == -1) {
                enchantmentLevel = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.GLUTTONOUS_POUCH.get(), player);
                PLAYER_ENCHANTMENT_CACHE.put(playerId, enchantmentLevel);
            }
            
            // 只在玩家拥有附魔时处理效果
            if (enchantmentLevel > 0) {
                // 检查玩家是否有有效的胸甲
                ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
                if (chestplate.isEmpty() || !(chestplate.getItem() instanceof ArmorItem) || chestplate.getDamageValue() >= chestplate.getMaxDamage()) {
                    return; // 如果没有胸甲或胸甲已损坏，则不进行格挡
                }
                
                // 计算格挡概率 (基础20% + 每级15%，最高5级=基础20%+4*15%=80%)
                double blockChance = Math.min(0.8, BASE_BLOCK_CHANCE + (enchantmentLevel - 1) * BLOCK_CHANCE_PER_LEVEL);
                
                // 判定是否格挡成功
                if (RANDOM.nextDouble() < blockChance) {
                    // 检查玩家是否有足够的饥饿值
                    int hungerCost = Math.min(20, BASE_HUNGER_COST + (enchantmentLevel - 1) * HUNGER_COST_PER_LEVEL);
                    if (player.getFoodData().getFoodLevel() >= hungerCost) {
                        // 消耗饥饿值
                        player.getFoodData().setFoodLevel(player.getFoodData().getFoodLevel() - hungerCost);
                        
                        // 播放格挡音效
                        playBlockSound(player);
                        
                        // 完全格挡伤害（但不格挡摔落伤害）
                        if (!event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FALL)) {
                            event.setCanceled(true);
                        }
                    }
                }
            }
        }
    }
    
    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player) {
            // 获取玩家的Gluttonous Pouch附魔等级
            int enchantmentLevel = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.GLUTTONOUS_POUCH.get(), player);
            
            // 只在玩家拥有附魔时处理效果
            if (enchantmentLevel > 0) {
                // 增加5倍摔落伤害（总共造成6倍原始伤害）
                event.setDistance(event.getDistance() * 6.0f);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Player player) {
            // 只在服务端应用效果，避免客户端和服务端同步问题
            if (player.level().isClientSide) {
                return;
            }
            
            UUID playerId = player.getUUID();
            long currentTime = player.level().getGameTime();
            
            // 获取缓存的附魔等级，如果不存在则计算并缓存
            int enchantmentLevel = PLAYER_ENCHANTMENT_CACHE.getOrDefault(playerId, -1);
            if (enchantmentLevel == -1) {
                enchantmentLevel = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.GLUTTONOUS_POUCH.get(), player);
                PLAYER_ENCHANTMENT_CACHE.put(playerId, enchantmentLevel);
            }
            
            // 检查减速效果是否应该结束
            Long slownessEndTime = PLAYER_SLOWNESS_END_TIME.get(playerId);
            if (slownessEndTime != null && currentTime >= slownessEndTime) {
                removeGluttonousPouchEffects(player);
                PLAYER_SLOWNESS_END_TIME.remove(playerId);
            }
            
            // 只有在玩家拥有附魔时才处理效果
            if (enchantmentLevel > 0) {
                // 检查是否需要更新效果
                Long nextUpdateTime = PLAYER_NEXT_UPDATE_TIME.get(playerId);
                if (nextUpdateTime == null || currentTime >= nextUpdateTime) {
                    applyGluttonousPouchEffects(player, enchantmentLevel, currentTime);
                    PLAYER_NEXT_UPDATE_TIME.put(playerId, currentTime + UPDATE_INTERVAL);
                }
            } else {
                // 移除减速效果
                removeGluttonousPouchEffects(player);
                PLAYER_SLOWNESS_END_TIME.remove(playerId);
            }
        }
    }
    
    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        // 当玩家装备发生变化时更新缓存
        if (event.getEntity() instanceof Player player && event.getSlot() == EquipmentSlot.CHEST) {
            UUID playerId = player.getUUID();
            int enchantmentLevel = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.GLUTTONOUS_POUCH.get(), player);
            PLAYER_ENCHANTMENT_CACHE.put(playerId, enchantmentLevel);
            
            // 立即清理下次更新时间缓存以强制更新效果
            PLAYER_NEXT_UPDATE_TIME.remove(playerId);
            PLAYER_SLOWNESS_END_TIME.remove(playerId);
            PLAYER_GRAVITY_END_TIME.remove(playerId);

            // 当装备被移除时，移除减速效果
            if (enchantmentLevel <= 0) {
                removeGluttonousPouchEffects(player);
            }
        }
    }
    
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        // 玩家登出时清理缓存
        Player player = event.getEntity();
        removeGluttonousPouchEffects(player);
        UUID playerId = player.getUUID();
        PLAYER_ENCHANTMENT_CACHE.remove(playerId);
        PLAYER_NEXT_UPDATE_TIME.remove(playerId);
        PLAYER_SLOWNESS_END_TIME.remove(playerId);
        PLAYER_GRAVITY_END_TIME.remove(playerId);
    }
    
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        // 玩家切换维度时清理缓存
        Player player = event.getEntity();
        removeGluttonousPouchEffects(player);
        UUID playerId = player.getUUID();
        PLAYER_ENCHANTMENT_CACHE.remove(playerId);
        PLAYER_NEXT_UPDATE_TIME.remove(playerId);
        PLAYER_SLOWNESS_END_TIME.remove(playerId);
        PLAYER_GRAVITY_END_TIME.remove(playerId);
    }
    
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        // 玩家重生时清理缓存
        Player player = event.getEntity();
        removeGluttonousPouchEffects(player);
        UUID playerId = player.getUUID();
        PLAYER_ENCHANTMENT_CACHE.remove(playerId);
        PLAYER_NEXT_UPDATE_TIME.remove(playerId);
        PLAYER_SLOWNESS_END_TIME.remove(playerId);
        PLAYER_GRAVITY_END_TIME.remove(playerId);
    }

    /**
     * 应用大胃袋附魔效果
     * @param player 玩家实体
     * @param enchantmentLevel 附魔等级
     * @param currentTime 当前游戏时间
     */
    private static void applyGluttonousPouchEffects(Player player, int enchantmentLevel, long currentTime) {
        // 使用AttributeModifier实现减速效果
        // 计算减速系数，基于附魔等级
        double slowFactor = -(enchantmentLevel * 0.2);  // 每级减少20%速度
        slowFactor = Math.max(-0.8, slowFactor); // 最多减少80%速度
        
        AttributeInstance attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        
        // 移除旧的修饰符（如果存在）
        AttributeModifier existingModifier = attribute.getModifier(SLOWNESS_MODIFIER_UUID);
        if (existingModifier != null) {
            attribute.removeModifier(existingModifier);
        }
        
        // 添加减速修饰符
        AttributeModifier modifier = new AttributeModifier(
            SLOWNESS_MODIFIER_UUID,
            "Gluttonous Pouch Slowdown",
            slowFactor,
            AttributeModifier.Operation.MULTIPLY_BASE
        );
        attribute.addTransientModifier(modifier);
        
        // 设置减速效果的到期时间（2秒后）
        PLAYER_SLOWNESS_END_TIME.put(player.getUUID(), currentTime + SLOWNESS_DURATION);
        
        // 使用AttributeModifier实现重力效果
        // 计算重力系数，基于附魔等级
        double gravityFactor = enchantmentLevel * 0.2;  // 每级增加20%重力
        
        AttributeInstance gravityAttribute = player.getAttribute(ForgeMod.ENTITY_GRAVITY.get());
        
        // 移除旧的修饰符（如果存在）
        AttributeModifier existingGravityModifier = gravityAttribute.getModifier(GRAVITY_MODIFIER_UUID);
        if (existingGravityModifier != null) {
            gravityAttribute.removeModifier(existingGravityModifier);
        }
        
        // 添加重力修饰符
        AttributeModifier gravityModifier = new AttributeModifier(
            GRAVITY_MODIFIER_UUID,
            "Gluttonous Pouch Gravity",
            gravityFactor,
            AttributeModifier.Operation.MULTIPLY_BASE
        );
        gravityAttribute.addTransientModifier(gravityModifier);
        
        // 设置重力效果的到期时间（2秒后）
        PLAYER_GRAVITY_END_TIME.put(player.getUUID(), currentTime + GRAVITY_DURATION);
    }
    
    /**
     * 移除大胃袋附魔效果
     * @param player 玩家实体
     */
    private static void removeGluttonousPouchEffects(Player player) {
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

    /**
     * 播放格挡音效
     * @param player 玩家实体
     */
    private static void playBlockSound(Player player) {
        if (!player.level().isClientSide) {
            // 播放原版盾牌格挡音效
            player.level().playSound(
                null, // 不指定特定玩家
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.SHIELD_BLOCK, // 使用原版盾牌格挡音效
                SoundSource.PLAYERS,
                1.0f, // 音量
                1.0f + (RANDOM.nextFloat() * 0.4f - 0.2f) // 音调 (0.8 - 1.2)
            );
        }
    }
}