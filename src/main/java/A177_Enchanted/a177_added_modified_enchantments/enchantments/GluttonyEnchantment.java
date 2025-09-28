package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
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
public class GluttonyEnchantment extends Enchantment {

    // 每级增加的最大生命值上限
    private static final double MAX_HEALTH_BONUS_PER_LEVEL = 20.0;
    // 高饥饿度持续1分钟增加的生命值
    private static final double HEALTH_GAIN_PER_FULL = 2.0;
    // 低饥饿度持续2分钟减少的生命值
    private static final double HEALTH_LOSS_PER_HUNGER = 2.0;

    // 所需高饥饿度时间（秒）
    private static final int FULL_HUNGER_TIME_REQUIRED = 60;
    // 低饥饿度惩罚时间阈值（秒）
    private static final int LOW_HUNGER_TIME_THRESHOLD = 120;
    // 饥饿度阈值（16格）
    private static final int HUNGER_THRESHOLD = 16;
    // 饥饿度减少间隔（tick）
    private static final int HUNGER_DECREASE_INTERVAL = 1200; // 1分钟 = 60秒 * 20 tick/秒 = 1200 tick

    private static final UUID GLUTTONY_MODIFIER_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5F0");
    
    // NBT标签键
    private static final String GLUTTONY_DATA_TAG = "GluttonyEnchantmentData";
    private static final String FULL_HUNGER_TIME_KEY = "FullHungerTime";
    private static final String LOW_HUNGER_TIME_KEY = "LowHungerTime";
    private static final String HUNGER_DECREASE_TIME_KEY = "HungerDecreaseTime";
    private static final String CURRENT_BONUS_KEY = "CurrentBonus";

    // 存储玩家数据
    private static final Map<Player, PlayerGluttonyData> playerDataMap = new HashMap<>();

    public GluttonyEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[]{EquipmentSlot.CHEST});
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinCost(int level) {
        return 20 + (level - 1) * 15;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 25;
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
        return AllEnchantmentsConfig.GLUTTONY.isTradeable.get();
    }

    @Override
    public boolean isDiscoverable() {
        return AllEnchantmentsConfig.GLUTTONY.isDiscoverable.get();
    }

    @Override
    public boolean isTreasureOnly() {
        return AllEnchantmentsConfig.GLUTTONY.isTreasureOnly.get();
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

            // 检查之前穿戴的装备是否有饕餮附魔
            boolean hadGluttonyEnchant = !fromItem.isEmpty() &&
                    fromItem.getEnchantmentLevel(ModEnchantments.GLUTTONY.get()) > 0;

            // 检查现在穿戴的装备是否有饕餮附魔
            boolean hasGluttonyEnchant = !toItem.isEmpty() &&
                    toItem.getEnchantmentLevel(ModEnchantments.GLUTTONY.get()) > 0;

            // 如果之前有附魔而现在没有（脱下或损坏）
            if (hadGluttonyEnchant && !hasGluttonyEnchant) {
                PlayerGluttonyData data = playerDataMap.get(player);
                if (data != null) {
                    data.reset(); // 完全重置进度和加成
                }
                // 确保移除属性修饰符
                removeGluttonyModifier(player);
                // 保存重置后的数据
                savePlayerData(player);
            }

            // 如果现在有附魔（穿上或切换为有附魔的装备）
            if (hasGluttonyEnchant) {
                PlayerGluttonyData data = playerDataMap.computeIfAbsent(player, k -> loadPlayerData(player));
                // 新穿上装备，重置计时器
                data.fullHungerTime = 0;
                data.lowHungerTime = 0;
                data.hungerDecreaseTime = 0; // 重置饥饿度减少计时器
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

            // 检查玩家是否穿着有饕餮附魔的胸甲
            ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
            int gluttonyLevel = chestplate.getEnchantmentLevel(ModEnchantments.GLUTTONY.get());

            if (gluttonyLevel > 0) {
                // 确保玩家有数据记录
                PlayerGluttonyData data = playerDataMap.computeIfAbsent(player, k -> loadPlayerData(player));

                // 处理饥饿度减少逻辑
                data.hungerDecreaseTime++;
                if (data.hungerDecreaseTime >= HUNGER_DECREASE_INTERVAL) {
                    decreaseHunger(player);
                    data.hungerDecreaseTime = 0;
                }

                // 获取玩家饥饿度
                FoodData foodData = player.getFoodData();
                int hungerLevel = foodData.getFoodLevel();

                // 检查饥饿度状态
                if (hungerLevel >= HUNGER_THRESHOLD) {
                    // 高饥饿度状态
                    data.fullHungerTime++;
                    data.lowHungerTime = 0;

                    // 检查是否达到高饥饿度奖励条件
                    if (data.fullHungerTime >= FULL_HUNGER_TIME_REQUIRED * 20) {
                        awardHungerBonus(player, gluttonyLevel);
                        data.fullHungerTime = 0;
                        // 保存更新后的数据
                        savePlayerData(player);
                    }
                } else {
                    // 低饥饿度状态
                    data.lowHungerTime++;
                    data.fullHungerTime = 0;

                    // 检查是否达到低饥饿度惩罚条件
                    if (data.lowHungerTime >= LOW_HUNGER_TIME_THRESHOLD * 20) {
                        applyHungerPenalty(player, gluttonyLevel);
                        data.lowHungerTime = 0;
                        // 保存更新后的数据
                        savePlayerData(player);
                    }
                }

                // 更新属性修饰符
                updateGluttonyModifier(player);
            } else {
                // 如果没有附魔，移除数据
                PlayerGluttonyData data = playerDataMap.get(player);
                if (data != null) {
                    data.fullHungerTime = 0;
                    data.lowHungerTime = 0;
                    data.hungerDecreaseTime = 0; // 重置饥饿度减少计时器
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
            PlayerGluttonyData originalData = loadPlayerData(originalPlayer);
            
            // 保存到新玩家
            savePlayerData(newPlayer, originalData);
            
            // 更新新玩家的属性修饰符
            updateGluttonyModifier(newPlayer);
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
        
        PlayerGluttonyData data = loadPlayerData(player);
        playerDataMap.put(player, data);
        // 更新属性修饰符
        updateGluttonyModifier(player);
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

    private static void decreaseHunger(Player player) {
        FoodData foodData = player.getFoodData();
        int currentHunger = foodData.getFoodLevel();
        
        // 减少1点饥饿度，但不低于0
        if (currentHunger > 0) {
            foodData.setFoodLevel(Math.max(0, currentHunger - 1));
        }
    }

    private static void awardHungerBonus(Player player, int gluttonyLevel) {
        PlayerGluttonyData data = playerDataMap.get(player);
        if (data != null && data.currentBonus < MAX_HEALTH_BONUS_PER_LEVEL * gluttonyLevel) {
            data.currentBonus += HEALTH_GAIN_PER_FULL;
            data.currentBonus = Math.min(data.currentBonus, MAX_HEALTH_BONUS_PER_LEVEL * gluttonyLevel);
            // 可以在这里添加粒子效果或声音反馈
        }
    }

    private static void applyHungerPenalty(Player player, int gluttonyLevel) {
        PlayerGluttonyData data = playerDataMap.get(player);
        if (data != null && data.currentBonus > 0) {
            data.currentBonus -= HEALTH_LOSS_PER_HUNGER;
            data.currentBonus = Math.max(data.currentBonus, 0);
        }
    }

    private static void updateGluttonyModifier(Player player) {
        var healthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttribute == null) return;

        // 移除旧的修饰符
        healthAttribute.removeModifier(GLUTTONY_MODIFIER_UUID);

        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        int gluttonyLevel = chestplate.getEnchantmentLevel(ModEnchantments.GLUTTONY.get());

        if (gluttonyLevel > 0) {
            PlayerGluttonyData data = playerDataMap.get(player);
            if (data != null && data.currentBonus > 0) {
                // 添加新的修饰符
                AttributeModifier modifier = new AttributeModifier(
                        GLUTTONY_MODIFIER_UUID,
                        "Gluttony bonus",
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
    private static void removeGluttonyModifier(Player player) {
        var healthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttribute != null) {
            healthAttribute.removeModifier(GLUTTONY_MODIFIER_UUID);
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
        PlayerGluttonyData data = playerDataMap.get(player);
        if (data != null) {
            savePlayerData(player, data);
        }
    }
    
    // 保存指定的玩家数据到NBT
    private static void savePlayerData(Player player, PlayerGluttonyData data) {
        CompoundTag persistentData = player.getPersistentData();
        CompoundTag gluttonyData = new CompoundTag();
        
        gluttonyData.putInt(FULL_HUNGER_TIME_KEY, data.fullHungerTime);
        gluttonyData.putInt(LOW_HUNGER_TIME_KEY, data.lowHungerTime);
        gluttonyData.putInt(HUNGER_DECREASE_TIME_KEY, data.hungerDecreaseTime);
        gluttonyData.putDouble(CURRENT_BONUS_KEY, data.currentBonus);
        
        persistentData.put(GLUTTONY_DATA_TAG, gluttonyData);
    }
    
    // 从NBT加载玩家数据
    private static PlayerGluttonyData loadPlayerData(Player player) {
        CompoundTag persistentData = player.getPersistentData();
        PlayerGluttonyData data = new PlayerGluttonyData();
        
        if (persistentData.contains(GLUTTONY_DATA_TAG, CompoundTag.TAG_COMPOUND)) {
            CompoundTag gluttonyData = persistentData.getCompound(GLUTTONY_DATA_TAG);
            
            data.fullHungerTime = gluttonyData.getInt(FULL_HUNGER_TIME_KEY);
            data.lowHungerTime = gluttonyData.getInt(LOW_HUNGER_TIME_KEY);
            data.hungerDecreaseTime = gluttonyData.getInt(HUNGER_DECREASE_TIME_KEY);
            data.currentBonus = gluttonyData.getDouble(CURRENT_BONUS_KEY);
        }
        
        return data;
    }

    // 玩家数据存储类
    private static class PlayerGluttonyData {
        public int fullHungerTime = 0; // 高饥饿度持续时间（tick）
        public int lowHungerTime = 0;  // 低饥饿度持续时间（tick）
        public int hungerDecreaseTime = 0; // 饥饿度减少计时器（tick）
        public double currentBonus = 0; // 当前获得的额外生命值

        public void reset() {
            fullHungerTime = 0;
            lowHungerTime = 0;
            hungerDecreaseTime = 0; // 重置饥饿度减少计时器
            currentBonus = 0; // 重置生命值加成
        }
    }
}