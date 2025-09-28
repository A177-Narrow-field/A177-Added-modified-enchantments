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

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class VeteranEnchantment extends Enchantment {

    // 每经验等级增加的生命值
    private static final double HEALTH_PER_LEVEL = 1.0;
    // 最大附魔等级
    private static final int MAX_ENCHANTMENT_LEVEL = 1;

    // 属性修饰符UUID
    private static final UUID VETERAN_MODIFIER_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5E2");
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("veteran");
    }

    // NBT标签键
    private static final String VETERAN_DATA_TAG = "VeteranEnchantmentData";
    private static final String LAST_EXPERIENCE_LEVEL_KEY = "LastExperienceLevel";
    private static final String CURRENT_BONUS_KEY = "CurrentBonus";

    // 存储玩家数据
    private static final Map<Player, PlayerVeteranData> playerDataMap = new HashMap<>();

    public VeteranEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[]{EquipmentSlot.CHEST});
    }

    @Override
    public int getMaxLevel() {
        return MAX_ENCHANTMENT_LEVEL;
    }

    @Override
    public int getMinCost(int level) {
        return 25;
    }

    @Override
    public int getMaxCost(int level) {
        return 50;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem &&
                ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.CHEST;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();
    }

    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }
    
    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }

    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        // 只处理胸甲装备的变化
        if (event.getSlot() == EquipmentSlot.CHEST && event.getEntity() instanceof Player player) {
            // 只在服务端执行逻辑
            if (player.level().isClientSide()) {
                return;
            }

            ItemStack fromItem = event.getFrom(); // 之前穿戴的装备
            ItemStack toItem = event.getTo();   // 现在要穿戴的装备

            // 检查之前穿戴的装备是否有身经百战附魔
            boolean hadVeteranEnchant = !fromItem.isEmpty() &&
                    fromItem.getEnchantmentLevel(ModEnchantments.VETERAN.get()) > 0;

            // 检查现在穿戴的装备是否有身经百战附魔
            boolean hasVeteranEnchant = !toItem.isEmpty() &&
                    toItem.getEnchantmentLevel(ModEnchantments.VETERAN.get()) > 0;

            // 如果之前有附魔而现在没有（脱下或损坏）
            if (hadVeteranEnchant && !hasVeteranEnchant) {
                // 移除属性修饰符
                removeVeteranModifier(player);
                // 清理玩家数据
                playerDataMap.remove(player);
            }

            // 如果现在有附魔（穿上或切换为有附魔的装备）
            if (hasVeteranEnchant) {
                // 立即更新属性修饰符
                updateVeteranModifier(player);
                // 保存当前经验等级作为参考
                PlayerVeteranData data = playerDataMap.computeIfAbsent(player, k -> new PlayerVeteranData());
                data.lastExperienceLevel = player.experienceLevel;
                data.currentBonus = calculateHealthBonus(player);
                savePlayerData(player);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player != null) {
            Player player = event.player;

            // 只在服务端执行逻辑
            if (player.level().isClientSide()) {
                return;
            }

            // 检查玩家是否穿着有身经百战附魔的胸甲
            ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
            int veteranLevel = chestplate.getEnchantmentLevel(ModEnchantments.VETERAN.get());

            if (veteranLevel > 0) {
                // 确保玩家有数据记录
                PlayerVeteranData data = playerDataMap.computeIfAbsent(player, k -> loadPlayerData(player));

                // 检查经验等级是否发生变化
                if (data.lastExperienceLevel != player.experienceLevel) {
                    // 经验等级发生变化，更新生命值加成
                    data.currentBonus = calculateHealthBonus(player);
                    data.lastExperienceLevel = player.experienceLevel;

                    // 更新属性修饰符
                    updateVeteranModifier(player);
                    // 保存数据
                    savePlayerData(player);
                }
            } else {
                // 如果没有附魔，移除数据
                playerDataMap.remove(player);
            }

            // 定期清理不再需要的玩家数据
            cleanupPlayerData();
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        // 玩家死亡或维度传送时保持数据
        if (event.isWasDeath()) {
            Player originalPlayer = event.getOriginal();
            Player newPlayer = event.getEntity();

            // 只在服务端执行
            if (newPlayer.level().isClientSide()) {
                return;
            }

            // 加载原玩家的数据
            PlayerVeteranData originalData = loadPlayerData(originalPlayer);

            // 保存到新玩家
            savePlayerData(newPlayer, originalData);

            // 更新新玩家的属性修饰符
            updateVeteranModifier(newPlayer);
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

        PlayerVeteranData data = loadPlayerData(player);
        playerDataMap.put(player, data);
        // 更新属性修饰符
        updateVeteranModifier(player);
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

    // 计算生命值加成
    private static double calculateHealthBonus(Player player) {
        return player.experienceLevel * HEALTH_PER_LEVEL;
    }

    // 更新属性修饰符
    private static void updateVeteranModifier(Player player) {
        var healthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttribute == null) return;

        // 移除旧的修饰符
        healthAttribute.removeModifier(VETERAN_MODIFIER_UUID);

        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        int veteranLevel = chestplate.getEnchantmentLevel(ModEnchantments.VETERAN.get());

        if (veteranLevel > 0) {
            PlayerVeteranData data = playerDataMap.get(player);
            if (data == null) {
                data = loadPlayerData(player);
                playerDataMap.put(player, data);
            }

            // 计算当前加成
            double currentBonus = calculateHealthBonus(player);
            data.currentBonus = currentBonus;
            data.lastExperienceLevel = player.experienceLevel;

            if (currentBonus > 0) {
                // 添加新的修饰符
                AttributeModifier modifier = new AttributeModifier(
                        VETERAN_MODIFIER_UUID,
                        "Veteran experience bonus",
                        currentBonus,
                        AttributeModifier.Operation.ADDITION
                );
                healthAttribute.addTransientModifier(modifier);
            }
        }

        // 确保生命值不超过上限
        adjustHealthIfNeeded(player);
    }

    // 移除修饰符
    private static void removeVeteranModifier(Player player) {
        var healthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttribute != null) {
            healthAttribute.removeModifier(VETERAN_MODIFIER_UUID);
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
        // 清理不再需要的玩家数据
        playerDataMap.entrySet().removeIf(entry -> !entry.getKey().isAlive() || entry.getKey().level().isClientSide());
    }

    // 保存玩家数据到NBT
    private static void savePlayerData(Player player) {
        PlayerVeteranData data = playerDataMap.get(player);
        if (data != null) {
            savePlayerData(player, data);
        }
    }

    // 保存指定的玩家数据到NBT
    private static void savePlayerData(Player player, PlayerVeteranData data) {
        CompoundTag persistentData = player.getPersistentData();
        CompoundTag veteranData = new CompoundTag();

        veteranData.putInt(LAST_EXPERIENCE_LEVEL_KEY, data.lastExperienceLevel);
        veteranData.putDouble(CURRENT_BONUS_KEY, data.currentBonus);

        persistentData.put(VETERAN_DATA_TAG, veteranData);
    }

    // 从NBT加载玩家数据
    private static PlayerVeteranData loadPlayerData(Player player) {
        CompoundTag persistentData = player.getPersistentData();
        PlayerVeteranData data = new PlayerVeteranData();

        if (persistentData.contains(VETERAN_DATA_TAG, CompoundTag.TAG_COMPOUND)) {
            CompoundTag veteranData = persistentData.getCompound(VETERAN_DATA_TAG);

            data.lastExperienceLevel = veteranData.getInt(LAST_EXPERIENCE_LEVEL_KEY);
            data.currentBonus = veteranData.getDouble(CURRENT_BONUS_KEY);
        } else {
            // 如果没有保存的数据，使用当前值初始化
            data.lastExperienceLevel = player.experienceLevel;
            data.currentBonus = calculateHealthBonus(player);
        }

        return data;
    }

    // 玩家数据存储类
    private static class PlayerVeteranData {
        public int lastExperienceLevel = 0; // 上次记录的经验等级
        public double currentBonus = 0;     // 当前获得的额外生命值

        public void reset() {
            lastExperienceLevel = 0;
            currentBonus = 0;
        }
    }
}