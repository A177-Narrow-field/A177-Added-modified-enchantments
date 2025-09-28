package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.List;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber
public class CommanderEnchantment extends Enchantment {
    // 为每个属性定义唯一的UUID，防止与其他修饰符冲突
    private static final UUID DAMAGE_REDUCTION_MODIFIER_ID = UUID.fromString("c9deab1d-6f5c-4d53-9a7e-34567890abcd");
    private static final UUID ATTACK_SPEED_MODIFIER_ID = UUID.fromString("d0efbc2e-7a6d-4e64-0b8f-45678901bcde");
    
    // 缓存机制，记录每个玩家的上次更新时间
    private static final Map<UUID, Long> LAST_UPDATE_TIME = new HashMap<>();
    // 缓存机制，记录每个玩家当前受影响的实体
    private static final Map<UUID, Set<UUID>> AFFECTED_ENTITIES = new HashMap<>();
    // 缓存玩家是否拥有统领附魔的状态
    private static final Map<UUID, Boolean> PLAYER_HAS_COMMANDER = new HashMap<>();
    
    // 效果持续时间（40 ticks = 2秒）
    private static final int EFFECT_DURATION = 40;
    // 更新间隔（20 ticks = 1秒）
    private static final int UPDATE_INTERVAL = 20;
    
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("commander");
    }

    public CommanderEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.ARMOR_HEAD, new EquipmentSlot[]{EquipmentSlot.HEAD});
    }
    @Override
    public int getMaxLevel() {
        return 3;
    }


    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }// 是否为宝藏附魔

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();}//可以正确的出现在附魔台

    @Override
    protected boolean checkCompatibility(Enchantment enchantment) {
        return enchantment != ModEnchantments.KING.get() &&
               enchantment != ModEnchantments.GENERAL.get() &&
               enchantment != ModEnchantments.ILLAGER_COMMANDER.get() &&
               enchantment != ModEnchantments.UNDEAD_COMMANDER.get() &&
               super.checkCompatibility(enchantment);
    }


    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }// 可在附魔台发现

    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }// 是否可交易

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        Player player = event.player;
        if (player.level().isClientSide) {
            return;
        }

        UUID playerId = player.getUUID();
        boolean hasCommander = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.COMMANDER.get(), player) > 0;
        Boolean previousState = PLAYER_HAS_COMMANDER.get(playerId);

        // 如果玩家刚刚获得或失去了统领附魔
        if (previousState == null || previousState != hasCommander) {
            PLAYER_HAS_COMMANDER.put(playerId, hasCommander);
            
            if (hasCommander) {
                // 玩家获得了统领附魔，应用效果
                applyCommanderEffect(player.level(), player);
            } else {
                // 玩家失去了统领附魔，移除效果
                removeCommanderEffect(player.level(), player);
            }
        } else if (hasCommander) {
            // 玩家持续拥有统领附魔，更新效果（现在由缓存机制控制更新频率）
            applyCommanderEffect(player.level(), player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        // 玩家登出时清理缓存
        UUID playerId = event.getEntity().getUUID();
        PLAYER_HAS_COMMANDER.remove(playerId);
        clearCache(playerId);
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        // 玩家切换维度时清理缓存
        UUID playerId = event.getEntity().getUUID();
        PLAYER_HAS_COMMANDER.remove(playerId);
        clearCache(playerId);
    }
    
    /**
     * 阻止狼和铁傀儡攻击穿戴统领附魔的玩家
     */
    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        if (event.getNewTarget() instanceof Player player) {
            // 检查玩家是否穿戴了统领附魔
            if (EnchantmentHelper.getEnchantmentLevel(ModEnchantments.COMMANDER.get(), player) > 0) {
                // 如果攻击者是狼或铁傀儡，则取消攻击
                if (event.getEntity().getType() == EntityType.WOLF || 
                    event.getEntity().getType() == EntityType.IRON_GOLEM) {
                    event.setCanceled(true);
                }
            }
        }
    }

    /**
     * 应用统领附魔效果到附近的友方生物
     * @param level 世界
     * @param player 玩家
     */
    public static void applyCommanderEffect(Level level, Player player) {
        if (level.isClientSide) return;

        long currentTime = level.getGameTime();
        UUID playerUUID = player.getUUID();
        
        // 检查是否需要更新效果
        Long lastUpdateTime = LAST_UPDATE_TIME.get(playerUUID);
        if (lastUpdateTime != null && (currentTime - lastUpdateTime) < UPDATE_INTERVAL) {
            // 未到更新时间，保持现有效果
            maintainExistingEffects(player, currentTime);
            return;
        }
        
        // 更新时间戳
        LAST_UPDATE_TIME.put(playerUUID, currentTime);
        
        // 获取头盔上的统领附魔等级
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        int enchantmentLevel = helmet.getEnchantmentLevel(ModEnchantments.COMMANDER.get());

        if (enchantmentLevel <= 0) {
            // 移除所有效果
            removeAllEffects(player);
            return;
        }

        // 计算伤害减免和攻击速度加成
        // 每级增加20%免伤和20%攻速
        double damageReduction = enchantmentLevel * 0.2;
        double attackSpeedBonus = enchantmentLevel * 0.2;

        // 获取10格内的友方生物
        AABB boundingBox = new AABB(player.blockPosition()).inflate(10);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, boundingBox);
        
        // 记录当前受影响的实体
        Set<UUID> currentAffectedEntities = new HashSet<>();
        
        // 为友方生物应用效果
        for (LivingEntity entity : entities) {
            if (entity instanceof Player) {
                Player targetPlayer = (Player) entity;
                if (targetPlayer != player) { // 不包括穿戴者自己
                    applyModifiers(targetPlayer, damageReduction, attackSpeedBonus);
                    currentAffectedEntities.add(targetPlayer.getUUID());
                }
            } else if (entity.isAlliedTo(player)) {
                applyModifiers(entity, damageReduction, attackSpeedBonus);
                currentAffectedEntities.add(entity.getUUID());
            }
        }
        
        // 获取之前受影响的实体
        Set<UUID> previousAffectedEntities = AFFECTED_ENTITIES.getOrDefault(playerUUID, new HashSet<>());
        
        // 移除不再受影响的实体的效果
        for (UUID entityUUID : previousAffectedEntities) {
            if (!currentAffectedEntities.contains(entityUUID)) {
                // 实体不再受影响，移除效果
                LivingEntity entity = (LivingEntity) level.getPlayerByUUID(entityUUID);
                if (entity == null) {
                    // 尝试从实体列表中查找
                    for (LivingEntity livingEntity : entities) {
                        if (livingEntity.getUUID().equals(entityUUID)) {
                            entity = livingEntity;
                            break;
                        }
                    }
                }
                if (entity != null) {
                    removeModifiers(entity);
                }
            }
        }
        
        // 更新受影响的实体列表
        AFFECTED_ENTITIES.put(playerUUID, currentAffectedEntities);
    }

    /**
     * 移除统领附魔效果从附近的友方生物
     * @param level 世界
     * @param player 玩家
     */
    public static void removeCommanderEffect(Level level, Player player) {
        if (level.isClientSide) return;
        
        UUID playerUUID = player.getUUID();
        
        // 移除所有效果
        removeAllEffects(player);
        
        // 清除缓存
        LAST_UPDATE_TIME.remove(playerUUID);
        AFFECTED_ENTITIES.remove(playerUUID);
    }
    
    /**
     * 维持现有效果
     * @param player 玩家
     * @param currentTime 当前时间
     */
    private static void maintainExistingEffects(Player player, long currentTime) {
        UUID playerUUID = player.getUUID();
        Set<UUID> affectedEntities = AFFECTED_ENTITIES.get(playerUUID);
        
        if (affectedEntities != null) {
            // 检查是否需要移除过期效果
            for (UUID entityUUID : new HashSet<>(affectedEntities)) {
                // 效果持续时间检查可以在这里添加，如果需要的话
                // 目前我们保持效果直到下次更新
            }
        }
    }
    
    /**
     * 移除所有效果
     * @param player 玩家
     */
    private static void removeAllEffects(Player player) {
        Level level = player.level();
        UUID playerUUID = player.getUUID();
        Set<UUID> affectedEntities = AFFECTED_ENTITIES.get(playerUUID);
        
        if (affectedEntities != null) {
            for (UUID entityUUID : affectedEntities) {
                LivingEntity entity = (LivingEntity) level.getPlayerByUUID(entityUUID);
                if (entity != null) {
                    removeModifiers(entity);
                }
            }
        }
        
        // 清除受影响的实体列表
        AFFECTED_ENTITIES.remove(playerUUID);
    }

    /**
     * 为生物应用属性修饰符
     * @param entity 生物
     * @param damageReduction 伤害减免
     * @param attackSpeedBonus 攻击速度加成
     */
    private static void applyModifiers(LivingEntity entity, double damageReduction, double attackSpeedBonus) {
        // 应用伤害减免
        AttributeInstance knockbackAttribute = entity.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (knockbackAttribute != null) {
            // 移除现有的修饰符（如果有的话）
            if (knockbackAttribute.getModifier(DAMAGE_REDUCTION_MODIFIER_ID) != null) {
                knockbackAttribute.removeModifier(DAMAGE_REDUCTION_MODIFIER_ID);
            }
            // 添加新的修饰符（使用击退抗性来模拟伤害减免）
            knockbackAttribute.addTransientModifier(new AttributeModifier(
                    DAMAGE_REDUCTION_MODIFIER_ID,
                    "Commander damage reduction",
                    damageReduction,
                    AttributeModifier.Operation.ADDITION
            ));
        }

        // 应用攻击速度加成
        AttributeInstance attackSpeedAttribute = entity.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeedAttribute != null) {
            // 移除现有的修饰符（如果有的话）
            if (attackSpeedAttribute.getModifier(ATTACK_SPEED_MODIFIER_ID) != null) {
                attackSpeedAttribute.removeModifier(ATTACK_SPEED_MODIFIER_ID);
            }
            // 添加新的修饰符
            attackSpeedAttribute.addTransientModifier(new AttributeModifier(
                    ATTACK_SPEED_MODIFIER_ID,
                    "Commander attack speed bonus",
                    attackSpeedBonus,
                    AttributeModifier.Operation.MULTIPLY_BASE
            ));
        }
    }

    /**
     * 从生物身上移除属性修饰符
     * @param entity 生物
     */
    private static void removeModifiers(LivingEntity entity) {
        // 移除伤害减免
        AttributeInstance knockbackAttribute = entity.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (knockbackAttribute != null && knockbackAttribute.getModifier(DAMAGE_REDUCTION_MODIFIER_ID) != null) {
            knockbackAttribute.removeModifier(DAMAGE_REDUCTION_MODIFIER_ID);
        }

        // 移除攻击速度加成
        AttributeInstance attackSpeedAttribute = entity.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeedAttribute != null && attackSpeedAttribute.getModifier(ATTACK_SPEED_MODIFIER_ID) != null) {
            attackSpeedAttribute.removeModifier(ATTACK_SPEED_MODIFIER_ID);
        }
    }
    
    /**
     * 清理指定玩家的缓存
     * @param playerUUID 玩家UUID
     */
    public static void clearCache(UUID playerUUID) {
        LAST_UPDATE_TIME.remove(playerUUID);
        AFFECTED_ENTITIES.remove(playerUUID);
    }
}