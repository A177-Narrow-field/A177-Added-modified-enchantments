package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
// 添加缺失的导入
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraftforge.event.TickEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber
public class ArmySummonEnchantment extends Enchantment {
    // 冷却时间（tick）- 21秒
    private static final int COOLDOWN_TIME = 430;
    // 召唤生物存活时间（tick）- 20秒
    private static final int DESPAWN_TIME = 400;
    // 每级召唤的生物数量
    private static final int MOBS_PER_LEVEL = 2;
    
    // 存储玩家冷却时间
    private static final Map<UUID, Long> PLAYER_COOLDOWN = new HashMap<>();
    // 存储生物消失时间
    private static final Map<UUID, Long> ENTITY_DESPAWN_TIME = new HashMap<>();
    // 存储玩家召唤的生物列表
    private static final Map<UUID, List<UUID>> PLAYER_SUMMONED_ENTITIES = new HashMap<>();
    // 存储玩家最近攻击的目标
    private static final Map<UUID, UUID> PLAYER_LAST_TARGET = new HashMap<>();
    // 直接存储实体引用以提高性能，使用WeakHashMap避免内存泄漏
    private static final Map<UUID, Mob> SUMMONED_ENTITY_REFERENCES = new WeakHashMap<>();

    public ArmySummonEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 3;
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
    public boolean canEnchant(ItemStack stack) {
        // 只能附在剑上
        return stack.getItem() instanceof SwordItem;
    }

    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("army_summon");
    }

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.canEnchant(stack);
    }

    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }

    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }

    @Override
    protected boolean checkCompatibility(Enchantment ench) {
        return super.checkCompatibility(ench) && ench != ModEnchantments.STEALTH_HUNTER.get();
    }

    /**
     * 右键使用附魔武器召唤军队
     */
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        Player player = event.getEntity();
        ItemStack weapon = player.getItemInHand(event.getHand());
        
        // 检查武器是否有召军附魔
        if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ARMY_SUMMON.get(), weapon) <= 0) {
            return;
        }

        UUID playerUUID = player.getUUID();
        long currentTime = event.getLevel().getGameTime();

        // 检查是否仍在冷却中
        Long cooldownEndTime = PLAYER_COOLDOWN.get(playerUUID);
        if (cooldownEndTime != null && currentTime < cooldownEndTime) {
            // 仍在冷却中
            return;
        }

        // 创造模式玩家不需要消耗经验值
        if (!player.isCreative()) {
            // 检查玩家是否有足够的经验值（15点经验）
            if (player.totalExperience < 15) {
                return;
            }

            // 消耗30点经验值
            player.giveExperiencePoints(-30);
        }

        // 设置冷却时间
        PLAYER_COOLDOWN.put(playerUUID, currentTime + COOLDOWN_TIME);
        // 设置武器冷却显示
        player.getCooldowns().addCooldown(weapon.getItem(), COOLDOWN_TIME);

        // 召唤军队
        summonArmy(event.getLevel(), player);

        // 播放召唤音效
        event.getLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
    
    /**
     * 每tick检查生物消失
     */
    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            checkAndDespawnEntities(event.level);
        }
    }
    
    /**
     * 每tick检查玩家冷却是否结束
     */
    @SubscribeEvent
    public static void onPlayerTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
            return; // 只在服务端运行
        }

        UUID playerUUID = player.getUUID();
        long currentTime = player.level().getGameTime();

        // 检查冷却是否结束（无论是否持有附魔武器都要检查）
        Long cooldownEndTime = PLAYER_COOLDOWN.get(playerUUID);
        if (cooldownEndTime != null && currentTime >= cooldownEndTime) {
            PLAYER_COOLDOWN.remove(playerUUID);
            // 播放冷却完成提示音
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        
        // 检查玩家是否在蹲下，如果是，则让召唤的生物跟随着玩家
        if (player.isCrouching()) {
            followPlayer(player);
        }
    }
    
    /**
     * 让召唤的生物跟随着玩家
     */
    private static void followPlayer(Player player) {
        UUID playerUUID = player.getUUID();
        List<UUID> summonedEntities = PLAYER_SUMMONED_ENTITIES.get(playerUUID);
        
        if (summonedEntities != null && !summonedEntities.isEmpty()) {
            for (UUID entityUUID : summonedEntities) {
                // 直接从映射中获取实体引用，避免遍历所有实体
                Mob entity = SUMMONED_ENTITY_REFERENCES.get(entityUUID);
                if (entity != null && entity.isAlive()) {
                    // 检查生物是否距离玩家太远
                    double distance = entity.distanceTo(player);
                    if (distance > 5.0) { // 如果距离大于5格
                        // 让生物朝玩家方向移动
                        double dx = player.getX() - entity.getX();
                        double dy = player.getY() - entity.getY();
                        double dz = player.getZ() - entity.getZ();
                        
                        // 计算方向向量并标准化
                        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
                        if (length > 0) {
                            dx /= length;
                            dy /= length;
                            dz /= length;
                            
                            // 设置生物的移动方向
                            double speed = 0.3; // 移动速度
                            entity.setDeltaMovement(dx * speed, dy * speed, dz * speed);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 召唤军队
     */
    private static void summonArmy(Level level, Player player) {
        // 检查玩家头盔附魔类型
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        
        // 检查武器上的召军附魔等级
        ItemStack weapon = player.getMainHandItem();
        int enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ARMY_SUMMON.get(), weapon);
        // 计算召唤生物数量（每级3个）
        int count = enchantmentLevel * MOBS_PER_LEVEL;
        
        // 获取玩家的召唤列表
        UUID playerUUID = player.getUUID();
        List<UUID> summonedEntities = PLAYER_SUMMONED_ENTITIES.computeIfAbsent(playerUUID, k -> new ArrayList<>());
        
        // 召唤生物
        for (int i = 0; i < count; i++) {
            // 默认召唤生物类型
            EntityType<? extends Mob> entityType = EntityType.WOLF;
            
            // 检查头盔附魔类型并确定召唤生物（每个生物独立计算概率）
            if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.COMMANDER.get(), helmet) > 0) {
                // 统领附魔 - 60%概率狼，40%概率铁傀儡
                if (Math.random() < 0.6) {
                    entityType = EntityType.WOLF;
                } else {
                    entityType = EntityType.IRON_GOLEM;
                }
            } else if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ILLAGER_COMMANDER.get(), helmet) > 0) {
                // 灾厄统领附魔 - 70%概率掠夺者，20%概率卫道士，10%概率劫掠兽
                double rand = Math.random();
                if (rand < 0.7) {
                    entityType = EntityType.PILLAGER;
                } else if (rand < 0.9) {
                    entityType = EntityType.VINDICATOR;
                } else {
                    entityType = EntityType.EVOKER;
                }
            } else if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.UNDEAD_COMMANDER.get(), helmet) > 0) {
                // 亡灵统帅附魔 - 40%概率尸壳，60%概率凋零骷髅
                if (Math.random() < 0.4) {
                    entityType = EntityType.HUSK;
                } else {
                    entityType = EntityType.WITHER_SKELETON;
                }
            }
            
            // 在玩家周围随机位置生成生物
            double angle = Math.random() * 2 * Math.PI;
            double distance = 2.0 + Math.random() * 3.0;
            double x = player.getX() + Math.cos(angle) * distance;
            double z = player.getZ() + Math.sin(angle) * distance;
            double y = player.getY();
            
            // 寻找合适的生成位置
            BlockPos spawnPos = new BlockPos((int)x, (int)y, (int)z);
            if (!level.isEmptyBlock(spawnPos)) {
                // 如果位置被阻挡，尝试向上查找
                for (int j = 0; j < 5; j++) {
                    spawnPos = spawnPos.above();
                    if (level.isEmptyBlock(spawnPos)) {
                        break;
                    }
                }
            }
            
            // 生成生物
            Mob entity = entityType.create(level);
            if (entity != null) {
                entity.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
                entity.setTarget(null);
                entity.setAggressive(false);
                
                // 为生物装备合适的武器
                equipMob(entity, entityType);
                
                // 设置生物无法掉落任何物品
                entity.setDropChance(EquipmentSlot.MAINHAND, 0.0f);
                entity.setDropChance(EquipmentSlot.OFFHAND, 0.0f);
                entity.setDropChance(EquipmentSlot.HEAD, 0.0f);
                entity.setDropChance(EquipmentSlot.CHEST, 0.0f);
                entity.setDropChance(EquipmentSlot.LEGS, 0.0f);
                entity.setDropChance(EquipmentSlot.FEET, 0.0f);
                
                // 添加到消失时间映射中
                ENTITY_DESPAWN_TIME.put(entity.getUUID(), level.getGameTime() + DESPAWN_TIME);
                
                // 添加到玩家的召唤列表中
                summonedEntities.add(entity.getUUID());
                
                // 添加到实体引用映射中以提高性能
                SUMMONED_ENTITY_REFERENCES.put(entity.getUUID(), entity);
                
                level.addFreshEntity(entity);
            }
        }
    }
    
    /**
     * 为生物装备合适的武器
     */
    private static void equipMob(Mob entity, EntityType<? extends Mob> entityType) {
        if (entityType == EntityType.PILLAGER) {
            // 掠夺者装备弩
            entity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.CROSSBOW));
        } else if (entityType == EntityType.VINDICATOR) {
            // 卫道士装备铁斧
            entity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
        } else if (entityType == EntityType.EVOKER) {
            // 唤魔者不装备武器（使用法术攻击）
        } else if (entityType == EntityType.HUSK) {
            // 尸壳不装备武器（空手）
        } else if (entityType == EntityType.WITHER_SKELETON) {
            // 凋零骷髅装备石剑
            entity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
        } else if (entityType == EntityType.WOLF) {
            // 狼不装备武器（使用撕咬攻击）
        } else if (entityType == EntityType.IRON_GOLEM) {
            // 铁傀儡不装备武器（使用拳头攻击）
        } else if (entityType == EntityType.LLAMA) {
            // 羊驼不装备武器（使用吐口水攻击）
        }
    }
    
    /**
     * 防止召唤的生物掉落物品
     */
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        Entity entity = event.getEntity();
        // 检查是否是我们召唤的生物
        if (SUMMONED_ENTITY_REFERENCES.containsKey(entity.getUUID())) {
            // 清空掉落物列表
            event.getDrops().clear();
        }
    }
    
    /**
     * 检查并移除超时的生物
     */
    public static void checkAndDespawnEntities(Level level) {
        if (level.isClientSide()) {
            return;
        }
        
        long currentTime = level.getGameTime();
        
        // 检查是否有需要消失的生物
        ENTITY_DESPAWN_TIME.entrySet().removeIf(entry -> {
            if (currentTime >= entry.getValue()) {
                // 时间到了，移除生物
                UUID entityUUID = entry.getKey();
                // 从玩家召唤列表中移除
                PLAYER_SUMMONED_ENTITIES.values().forEach(list -> list.removeIf(uuid -> uuid.equals(entityUUID)));
                // 从实体引用映射中移除
                SUMMONED_ENTITY_REFERENCES.remove(entityUUID);
                
                // 使用一个较大的AABB来搜索整个世界
                AABB searchBounds = new AABB(-30000000, 0, -30000000, 30000000, 256, 30000000);
                List<Mob> entities = level.getEntitiesOfClass(Mob.class, searchBounds);
                for (Mob entity : entities) {
                    if (entity.getUUID().equals(entityUUID)) {
                        entity.discard();
                        break;
                    }
                }
                return true;
            }
            return false;
        });
    }
    
    /**
     * 玩家攻击实体事件
     */
    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        UUID playerUUID = player.getUUID();
        
        // 检查玩家是否有召军附魔
        ItemStack weapon = player.getMainHandItem();
        if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ARMY_SUMMON.get(), weapon) <= 0) {
            return;
        }
        
        // 记录玩家攻击的目标
        Entity target = event.getTarget();
        if (target instanceof Mob) {
            PLAYER_LAST_TARGET.put(playerUUID, target.getUUID());
            
            // 让玩家召唤的生物攻击这个目标（使用优化的方法）
            List<UUID> summonedEntities = PLAYER_SUMMONED_ENTITIES.get(playerUUID);
            if (summonedEntities != null && !summonedEntities.isEmpty()) {
                for (UUID entityUUID : summonedEntities) {
                    // 直接从映射中获取实体引用，避免遍历所有实体
                    Mob entity = SUMMONED_ENTITY_REFERENCES.get(entityUUID);
                    if (entity != null && entity.isAlive()) {
                        entity.setTarget((Mob) target);
                    }
                }
            }
        }
    }
    
    /**
     * 玩家离开世界事件
     */
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        UUID playerUUID = player.getUUID();
        
        // 移除玩家召唤的所有生物
        List<UUID> summonedEntities = PLAYER_SUMMONED_ENTITIES.get(playerUUID);
        if (summonedEntities != null && !summonedEntities.isEmpty()) {
            Level level = player.level();
            if (!level.isClientSide()) {
                // 移除所有召唤的生物
                for (UUID entityUUID : summonedEntities) {
                    // 从实体引用映射中获取实体
                    Mob entity = SUMMONED_ENTITY_REFERENCES.get(entityUUID);
                    if (entity != null && entity.isAlive()) {
                        // 移除生物
                        entity.discard();
                    }
                    // 从实体引用映射中移除
                    SUMMONED_ENTITY_REFERENCES.remove(entityUUID);
                }
            }
        }
        
        // 清理玩家数据
        clearPlayerData(playerUUID);
    }
    
    /**
     * 玩家切换维度事件
     */
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        // 使用与登出相同的逻辑
        onPlayerLoggedOut(new PlayerEvent.PlayerLoggedOutEvent(event.getEntity()));
    }
    
    /**
     * 玩家登录事件 - 处理玩家重新进入游戏的情况
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        // 使用与登出相同的逻辑，确保玩家重新进入游戏时清理掉之前召唤的生物
        onPlayerLoggedOut(new PlayerEvent.PlayerLoggedOutEvent(event.getEntity()));
    }
    
    /**
     * 清理玩家数据
     */
    public static void clearPlayerData(UUID playerUUID) {
        PLAYER_COOLDOWN.remove(playerUUID);
        PLAYER_LAST_TARGET.remove(playerUUID);
        List<UUID> summonedEntities = PLAYER_SUMMONED_ENTITIES.remove(playerUUID);
        
        // 从实体引用映射中移除该玩家的所有实体
        if (summonedEntities != null) {
            for (UUID entityUUID : summonedEntities) {
                SUMMONED_ENTITY_REFERENCES.remove(entityUUID);
            }
        }
    }
    
    /**
     * 检查玩家是否在冷却中
     */
    public static boolean isPlayerOnCooldown(Player player) {
        UUID playerUUID = player.getUUID();
        Long cooldownEnd = PLAYER_COOLDOWN.get(playerUUID);
        
        if (cooldownEnd == null) {
            return false;
        }
        
        long currentTime = player.level().getGameTime();
        if (currentTime >= cooldownEnd) {
            PLAYER_COOLDOWN.remove(playerUUID);
            return false;
        }
        
        return true;
    }
    
    /**
     * 获取玩家冷却进度 (0.0-1.0)
     */
    public static float getPlayerCooldownProgress(Player player) {
        UUID playerUUID = player.getUUID();
        Long cooldownEnd = PLAYER_COOLDOWN.get(playerUUID);
        
        if (cooldownEnd == null) {
            return 0.0f;
        }
        
        long currentTime = player.level().getGameTime();
        if (currentTime >= cooldownEnd) {
            PLAYER_COOLDOWN.remove(playerUUID);
            return 0.0f;
        }
        
        long remaining = cooldownEnd - currentTime;
        return Math.min(1.0f, (float) remaining / COOLDOWN_TIME);
    }
}