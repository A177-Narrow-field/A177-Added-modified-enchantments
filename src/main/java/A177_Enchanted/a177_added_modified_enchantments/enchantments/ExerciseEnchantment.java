package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class ExerciseEnchantment extends Enchantment {

    // 每级增加的最大生命值上限
    private static final double MAX_HEALTH_BONUS_PER_LEVEL = 20.0;
    // 疾跑时间达到30秒增加的生命值
    private static final double HEALTH_GAIN_PER_SPRINT = 2.0;
    // 每3分钟不疾跑减少的生命值
    private static final double HEALTH_LOSS_PER_INACTIVITY = 2.0;

    // 所需疾跑时间（秒）
    private static final int SPRINT_TIME_REQUIRED = 30;
    // 不活跃时间阈值（秒）
    private static final int INACTIVITY_TIME_THRESHOLD = 180;

    private static final UUID EXERCISE_MODIFIER_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5E0");
    
    // NBT标签键
    private static final String EXERCISE_DATA_TAG = "ExerciseEnchantmentData";
    private static final String SPRINT_TIME_KEY = "SprintTime";
    private static final String INACTIVITY_TIME_KEY = "InactivityTime";
    private static final String CURRENT_BONUS_KEY = "CurrentBonus";

    // 存储玩家数据
    private static final Map<Player, PlayerExerciseData> playerDataMap = new HashMap<>();

    public ExerciseEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[]{EquipmentSlot.CHEST});
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinCost(int level) {
        return 15 + (level - 1) * 10;
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
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem());
    }

    @Override
    public boolean isTradeable() {
        return AllEnchantmentsConfig.EXERCISE.isTradeable.get();
    }

    @Override
    public boolean isDiscoverable() {
        return AllEnchantmentsConfig.EXERCISE.isDiscoverable.get();
    }

    @Override
    public boolean isTreasureOnly() {
        return AllEnchantmentsConfig.EXERCISE.isTreasureOnly.get();
    }

    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        // 只处理胸甲装备的变化
        if (event.getSlot() == EquipmentSlot.CHEST && event.getEntity() instanceof Player player) {
            // 只在服务端执行逻辑，避免客户端和服务端同时修改导致闪烁
            if (player.level().isClientSide()) {
                return;
            }
            
            // 获取变化前后的物品
            ItemStack fromItem = event.getFrom(); // 之前穿戴的装备
            ItemStack toItem = event.getTo();   // 现在要穿戴的装备

            // 检查之前穿戴的装备是否有锻炼附魔
            boolean hadExerciseEnchant = !fromItem.isEmpty() &&
                    fromItem.getEnchantmentLevel(ModEnchantments.EXERCISE.get()) > 0;

            // 检查现在穿戴的装备是否有锻炼附魔
            boolean hasExerciseEnchant = !toItem.isEmpty() &&
                    toItem.getEnchantmentLevel(ModEnchantments.EXERCISE.get()) > 0;

            // 如果之前有附魔而现在没有（脱下或损坏），或者装备发生了变化（切换）
            if (hadExerciseEnchant && !hasExerciseEnchant) {
                PlayerExerciseData data = playerDataMap.get(player);
                if (data != null) {
                    data.reset(); // 完全重置进度和加成
                }
                // 确保移除属性修饰符
                removeExerciseModifier(player);
                // 保存重置后的数据
                savePlayerData(player);
            }

            // 如果现在有附魔（穿上或切换为有附魔的装备）
            if (hasExerciseEnchant) {
                PlayerExerciseData data = playerDataMap.computeIfAbsent(player, k -> loadPlayerData(player));
                // 新穿上装备，重置计时器但保持currentBonus为0（从头开始）
                data.sprintTime = 0;
                data.inactivityTime = 0;
                // 保存数据
                savePlayerData(player);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player != null) {
            Player player = event.player;

            // 只在服务端执行逻辑，避免客户端和服务端同时修改导致闪烁
            if (player.level().isClientSide()) {
                return;
            }

            // 检查玩家是否穿着有锻炼附魔的胸甲
            ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
            int exerciseLevel = chestplate.getEnchantmentLevel(ModEnchantments.EXERCISE.get());

            if (exerciseLevel > 0) {
                // 确保玩家有数据记录
                PlayerExerciseData data = playerDataMap.computeIfAbsent(player, k -> loadPlayerData(player));

                // 更新疾跑时间
                if (player.isSprinting()) {
                    data.sprintTime++;
                    data.inactivityTime = 0;
                } else {
                    data.inactivityTime++;
                    data.sprintTime = 0;
                }

                // 检查是否达到疾跑奖励条件
                if (data.sprintTime >= SPRINT_TIME_REQUIRED * 20) { // 20 ticks = 1 second
                    awardSprintBonus(player, exerciseLevel);
                    data.sprintTime = 0;
                    // 保存更新后的数据
                    savePlayerData(player);
                }

                // 检查是否达到不活跃惩罚条件
                if (data.inactivityTime >= INACTIVITY_TIME_THRESHOLD * 20) {
                    applyInactivityPenalty(player, exerciseLevel);
                    data.inactivityTime = 0;
                    // 保存更新后的数据
                    savePlayerData(player);
                }

                // 更新属性修饰符（只在有附魔时）
                updateExerciseModifier(player);
            } else {
                // 如果没有附魔，移除数据（但保留在map中以便装备变化时处理）
                PlayerExerciseData data = playerDataMap.get(player);
                if (data != null) {
                    data.sprintTime = 0;
                    data.inactivityTime = 0;
                }
            }

            // 清理不再需要的玩家数据
            cleanupPlayerData();
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        // 玩家死亡或维度传送时保持数据
        if (event.isWasDeath()) {
            // 从原玩家复制数据到新玩家
            Player originalPlayer = event.getOriginal();
            Player newPlayer = event.getEntity();
            
            // 只在服务端执行
            if (newPlayer.level().isClientSide()) {
                return;
            }
            
            // 加载原玩家的数据
            PlayerExerciseData originalData = loadPlayerData(originalPlayer);
            
            // 保存到新玩家
            savePlayerData(newPlayer, originalData);
            
            // 更新新玩家的属性修饰符
            updateExerciseModifier(newPlayer);
        }
    }
    
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        // 玩家登录时加载数据
        Player player = event.getEntity();
        
        // 只在服务端执行
        if (player.level().isClientSide()) {
            return;
        }
        
        PlayerExerciseData data = loadPlayerData(player);
        playerDataMap.put(player, data);
        // 更新属性修饰符
        updateExerciseModifier(player);
    }
    
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        // 玩家登出时保存数据
        Player player = event.getEntity();
        
        // 只在服务端执行
        if (player.level().isClientSide()) {
            return;
        }
        
        savePlayerData(player);
        playerDataMap.remove(player);
    }

    private static void awardSprintBonus(Player player, int exerciseLevel) {
        PlayerExerciseData data = playerDataMap.get(player);
        if (data != null && data.currentBonus < MAX_HEALTH_BONUS_PER_LEVEL * exerciseLevel) {
            data.currentBonus += HEALTH_GAIN_PER_SPRINT;
            data.currentBonus = Math.min(data.currentBonus, MAX_HEALTH_BONUS_PER_LEVEL * exerciseLevel);
            // 可以在这里添加粒子效果或声音反馈
        }
    }

    private static void applyInactivityPenalty(Player player, int exerciseLevel) {
        PlayerExerciseData data = playerDataMap.get(player);
        if (data != null && data.currentBonus > 0) {
            data.currentBonus -= HEALTH_LOSS_PER_INACTIVITY;
            data.currentBonus = Math.max(data.currentBonus, 0);
        }
    }

    private static void updateExerciseModifier(Player player) {
        var healthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttribute == null) return;

        // 移除旧的修饰符
        healthAttribute.removeModifier(EXERCISE_MODIFIER_UUID);

        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        int exerciseLevel = chestplate.getEnchantmentLevel(ModEnchantments.EXERCISE.get());

        if (exerciseLevel > 0) {
            PlayerExerciseData data = playerDataMap.get(player);
            if (data != null && data.currentBonus > 0) {
                // 添加新的修饰符
                AttributeModifier modifier = new AttributeModifier(
                        EXERCISE_MODIFIER_UUID,
                        "Exercise bonus",
                        data.currentBonus,
                        AttributeModifier.Operation.ADDITION
                );
                healthAttribute.addTransientModifier(modifier);
            }
        }

        // 确保生命值不超过上限
        adjustHealthIfNeeded(player);
    }

    // 移除修饰符
    private static void removeExerciseModifier(Player player) {
        var healthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttribute != null) {
            healthAttribute.removeModifier(EXERCISE_MODIFIER_UUID);
            adjustHealthIfNeeded(player);
        }
    }

    private static void adjustHealthIfNeeded(Player player) {
        float currentHealth = player.getHealth();
        float maxHealth = player.getMaxHealth();
        if (currentHealth > maxHealth) {
            player.setHealth(maxHealth);
        }
    }

    private static void cleanupPlayerData() {
        // 定期清理不再需要的玩家数据（例如玩家退出游戏后）
        playerDataMap.entrySet().removeIf(entry -> !entry.getKey().isAlive() || entry.getKey().level().isClientSide());
    }
    
    // 保存玩家数据到NBT
    private static void savePlayerData(Player player) {
        PlayerExerciseData data = playerDataMap.get(player);
        if (data != null) {
            savePlayerData(player, data);
        }
    }
    
    // 保存指定的玩家数据到NBT
    private static void savePlayerData(Player player, PlayerExerciseData data) {
        CompoundTag persistentData = player.getPersistentData();
        CompoundTag exerciseData = new CompoundTag();
        
        exerciseData.putInt(SPRINT_TIME_KEY, data.sprintTime);
        exerciseData.putInt(INACTIVITY_TIME_KEY, data.inactivityTime);
        exerciseData.putDouble(CURRENT_BONUS_KEY, data.currentBonus);
        
        persistentData.put(EXERCISE_DATA_TAG, exerciseData);
    }
    
    // 从NBT加载玩家数据
    private static PlayerExerciseData loadPlayerData(Player player) {
        CompoundTag persistentData = player.getPersistentData();
        PlayerExerciseData data = new PlayerExerciseData();
        
        if (persistentData.contains(EXERCISE_DATA_TAG, CompoundTag.TAG_COMPOUND)) {
            CompoundTag exerciseData = persistentData.getCompound(EXERCISE_DATA_TAG);
            
            data.sprintTime = exerciseData.getInt(SPRINT_TIME_KEY);
            data.inactivityTime = exerciseData.getInt(INACTIVITY_TIME_KEY);
            data.currentBonus = exerciseData.getDouble(CURRENT_BONUS_KEY);
        }
        
        return data;
    }

    // 玩家数据存储类
    private static class PlayerExerciseData {
        public int sprintTime = 0; // 持续疾跑时间（tick）
        public int inactivityTime = 0; // 不活跃时间（tick）
        public double currentBonus = 0; // 当前获得的额外生命值

        public void reset() {
            sprintTime = 0;
            inactivityTime = 0;
            currentBonus = 0; // 重置生命值加成
        }
    }
}