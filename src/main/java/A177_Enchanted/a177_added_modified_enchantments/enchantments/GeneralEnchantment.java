package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber
public class GeneralEnchantment extends Enchantment {
    // 为每个属性定义唯一的UUID，防止与其他修饰符冲突
    private static final UUID DAMAGE_MODIFIER_ID = UUID.fromString("a7bd8f9b-4f3b-4f31-8a6d-123456789abc");
    private static final UUID SPEED_MODIFIER_ID = UUID.fromString("b8ce9a0c-5a4c-4a42-9b7e-23456789bcde");
    
    // 缓存机制，记录每个玩家的上次更新时间
    private static final Map<UUID, Long> LAST_UPDATE_TIME = new HashMap<>();
    // 缓存机制，记录每个玩家当前受影响的实体
    private static final Map<UUID, Set<UUID>> AFFECTED_ENTITIES = new HashMap<>();
    // 缓存玩家是否拥有将军附魔的状态
    private static final Map<UUID, Boolean> PLAYER_HAS_GENERAL = new HashMap<>();
    
    // 效果持续时间（40 ticks = 2秒）
    private static final int EFFECT_DURATION = 40;
    // 更新间隔（20 ticks = 1秒）
    private static final int UPDATE_INTERVAL = 20;
    
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("general");
    }

    public GeneralEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.ARMOR_HEAD, new EquipmentSlot[]{EquipmentSlot.HEAD});
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
    protected boolean checkCompatibility(Enchantment enchantment) {
        return !(enchantment instanceof CommanderEnchantment) && !(enchantment instanceof KingEnchantment) && super.checkCompatibility(enchantment);
    }// 与统领和国王附魔冲突


    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }// 可在附魔台发现

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();}//可以正确的出现在附魔台

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
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        boolean hasGeneral = helmet.getEnchantmentLevel(ModEnchantments.GENERAL.get()) > 0;
        Boolean previousState = PLAYER_HAS_GENERAL.get(playerId);

        // 如果玩家刚刚获得或失去了将军附魔
        if (previousState == null || previousState != hasGeneral) {
            PLAYER_HAS_GENERAL.put(playerId, hasGeneral);
            
            if (hasGeneral) {
                // 玩家获得了将军附魔，应用效果
                applyGeneralEffect(player.level(), player);
            } else {
                // 玩家失去了将军附魔，移除效果
                removeGeneralEffect(player.level(), player);
            }
        } else if (hasGeneral) {
            // 玩家持续拥有将军附魔，更新效果（现在由缓存机制控制更新频率）
            applyGeneralEffect(player.level(), player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        // 玩家登出时清理缓存
        UUID playerId = event.getEntity().getUUID();
        PLAYER_HAS_GENERAL.remove(playerId);
        clearCache(playerId);
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        // 玩家切换维度时清理缓存
        UUID playerId = event.getEntity().getUUID();
        PLAYER_HAS_GENERAL.remove(playerId);
        clearCache(playerId);
    }

    /**
     * 应用将军附魔效果到附近的友方生物
     * @param level 世界
     * @param player 玩家
     */
    public static void applyGeneralEffect(Level level, Player player) {
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
        
        // 获取头盔上的将军附魔等级
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        int enchantmentLevel = helmet.getEnchantmentLevel(ModEnchantments.GENERAL.get());

        if (enchantmentLevel <= 0) {
            // 移除所有效果
            removeAllEffects(player);
            return;
        }

        // 计算伤害加成和速度加成
        // 每级增加30%伤害和20%速度
        double damageBonus = enchantmentLevel * 0.3;
        double speedBonus = enchantmentLevel * 0.2;

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
                    applyModifiers(targetPlayer, damageBonus, speedBonus);
                    currentAffectedEntities.add(targetPlayer.getUUID());
                }
            } else if (entity.isAlliedTo(player)) {
                applyModifiers(entity, damageBonus, speedBonus);
                currentAffectedEntities.add(entity.getUUID());
            }
        }
        
        // 获取之前受影响的实体
        Set<UUID> previousAffectedEntities = AFFECTED_ENTITIES.getOrDefault(playerUUID, new HashSet<>());
        
        // 移除不再受影响的实体的效果
        for (UUID entityUUID : previousAffectedEntities) {
            if (!currentAffectedEntities.contains(entityUUID)) {
                // 实体不再受影响，移除效果
                LivingEntity entity = level.getPlayerByUUID(entityUUID);
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
     * 移除将军附魔效果从附近的友方生物
     * @param level 世界
     * @param player 玩家
     */
    public static void removeGeneralEffect(Level level, Player player) {
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
            for (UUID ignored : new HashSet<>(affectedEntities)) {
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
                LivingEntity entity = level.getPlayerByUUID(entityUUID);
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
     * @param damageBonus 伤害加成
     * @param speedBonus 速度加成
     */
    private static void applyModifiers(LivingEntity entity, double damageBonus, double speedBonus) {
        // 应用伤害加成
        AttributeInstance damageAttribute = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damageAttribute != null) {
            // 移除现有的修饰符（如果有的话）
            if (damageAttribute.getModifier(DAMAGE_MODIFIER_ID) != null) {
                damageAttribute.removeModifier(DAMAGE_MODIFIER_ID);
            }
            // 添加新的修饰符
            damageAttribute.addTransientModifier(new AttributeModifier(
                    DAMAGE_MODIFIER_ID,
                    "General damage bonus",
                    damageBonus,
                    AttributeModifier.Operation.MULTIPLY_BASE
            ));
        }

        // 应用速度加成
        AttributeInstance speedAttribute = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute != null) {
            // 移除现有的修饰符（如果有的话）
            if (speedAttribute.getModifier(SPEED_MODIFIER_ID) != null) {
                speedAttribute.removeModifier(SPEED_MODIFIER_ID);
            }
            // 添加新的修饰符
            speedAttribute.addTransientModifier(new AttributeModifier(
                    SPEED_MODIFIER_ID,
                    "General speed bonus",
                    speedBonus,
                    AttributeModifier.Operation.MULTIPLY_BASE
            ));
        }
    }

    /**
     * 从生物身上移除属性修饰符
     * @param entity 生物
     */
    private static void removeModifiers(LivingEntity entity) {
        // 移除伤害加成
        AttributeInstance damageAttribute = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damageAttribute != null && damageAttribute.getModifier(DAMAGE_MODIFIER_ID) != null) {
            damageAttribute.removeModifier(DAMAGE_MODIFIER_ID);
        }

        // 移除速度加成
        AttributeInstance speedAttribute = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute != null && speedAttribute.getModifier(SPEED_MODIFIER_ID) != null) {
            speedAttribute.removeModifier(SPEED_MODIFIER_ID);
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