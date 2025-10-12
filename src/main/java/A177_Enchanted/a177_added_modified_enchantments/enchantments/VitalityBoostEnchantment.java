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
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;

import java.util.UUID;

@Mod.EventBusSubscriber
public class VitalityBoostEnchantment extends Enchantment {
    // 生命值和护甲值的增加量
    private static final double HEALTH_PER_LEVEL = 4.0;
    private static final double ARMOR_PER_LEVEL = 1.0;

    // 属性修饰符UUID，用于唯一标识我们的修饰符
    private static final UUID HEALTH_MODIFIER_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    private static final UUID ARMOR_MODIFIER_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5D0");
    
    // NBT标签键
    private static final String VITALITY_BOOST_DATA_TAG = "VitalityBoostEnchantmentData";
    private static final String SAVED_HEALTH_KEY = "SavedHealth";
    
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("vitality_boost");
    }

    public VitalityBoostEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[]{EquipmentSlot.CHEST});
    }

    @Override
    public int getMaxLevel() {
        return 10;
    }

    @Override
    public int getMinCost(int level) {
        return 10 + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 15;
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
        // 当胸甲装备发生变化时处理属性变更
        if (event.getSlot() == EquipmentSlot.CHEST) {
            // 获取当前生命值
            float currentHealth = event.getEntity().getHealth();
            float maxHealth = event.getEntity().getMaxHealth();
            
            // 移除旧的属性修饰符
            event.getEntity().getAttributes().getInstance(Attributes.MAX_HEALTH).removeModifier(HEALTH_MODIFIER_UUID);
            event.getEntity().getAttributes().getInstance(Attributes.ARMOR).removeModifier(ARMOR_MODIFIER_UUID);

            // 如果有新的装备且有壮护附魔，则添加属性修饰符
            ItemStack newItem = event.getTo();
            if (!newItem.isEmpty()) {
                int vitalityLevel = newItem.getEnchantmentLevel(ModEnchantments.VITALITY_BOOST.get());
                if (vitalityLevel > 0) {
                    // 添加生命值修饰符
                    AttributeModifier healthModifier = new AttributeModifier(
                            HEALTH_MODIFIER_UUID,
                            "Vitality boost health",
                            HEALTH_PER_LEVEL * vitalityLevel,
                            AttributeModifier.Operation.ADDITION
                    );
                    event.getEntity().getAttributes().getInstance(Attributes.MAX_HEALTH).addTransientModifier(healthModifier);

                    // 添加护甲值修饰符
                    AttributeModifier armorModifier = new AttributeModifier(
                            ARMOR_MODIFIER_UUID,
                            "Vitality boost armor",
                            ARMOR_PER_LEVEL * vitalityLevel,
                            AttributeModifier.Operation.ADDITION
                    );
                    event.getEntity().getAttributes().getInstance(Attributes.ARMOR).addTransientModifier(armorModifier);
                }
            }
            
            // 调整生命值，确保不会超过新的最大生命值
            float newMaxHealth = event.getEntity().getMaxHealth();
            if (currentHealth > newMaxHealth) {
                event.getEntity().setHealth(newMaxHealth);
            }
        }
    }
    
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        // 玩家登录时更新属性修饰符并恢复生命值
        Player player = event.getEntity();
        updateVitalityBoostModifier(player);
        restorePlayerHealth(player);
    }
    
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        // 玩家登出时保存当前生命值
        Player player = event.getEntity();
        savePlayerHealth(player);
    }
    
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        // 玩家死亡或维度传送时保持属性修饰符和生命值
        Player newPlayer = event.getEntity();
        updateVitalityBoostModifier(newPlayer);
        
        if (event.isWasDeath()) {
            // 玩家死亡时恢复保存的生命值
            restorePlayerHealth(newPlayer);
        }
    }
    
    private static void updateVitalityBoostModifier(Player player) {
        // 移除旧的属性修饰符
        player.getAttributes().getInstance(Attributes.MAX_HEALTH).removeModifier(HEALTH_MODIFIER_UUID);
        player.getAttributes().getInstance(Attributes.ARMOR).removeModifier(ARMOR_MODIFIER_UUID);
        
        // 检查玩家是否穿着有壮护附魔的胸甲
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        int vitalityLevel = chestplate.getEnchantmentLevel(ModEnchantments.VITALITY_BOOST.get());
        
        if (vitalityLevel > 0) {
            // 添加生命值修饰符
            AttributeModifier healthModifier = new AttributeModifier(
                    HEALTH_MODIFIER_UUID,
                    "Vitality boost health",
                    HEALTH_PER_LEVEL * vitalityLevel,
                    AttributeModifier.Operation.ADDITION
            );
            player.getAttributes().getInstance(Attributes.MAX_HEALTH).addTransientModifier(healthModifier);

            // 添加护甲值修饰符
            AttributeModifier armorModifier = new AttributeModifier(
                    ARMOR_MODIFIER_UUID,
                    "Vitality boost armor",
                    ARMOR_PER_LEVEL * vitalityLevel,
                    AttributeModifier.Operation.ADDITION
            );
            player.getAttributes().getInstance(Attributes.ARMOR).addTransientModifier(armorModifier);
            
            // 调整生命值，确保不会超过新的最大生命值
            float currentHealth = player.getHealth();
            float newMaxHealth = player.getMaxHealth();
            if (currentHealth > newMaxHealth) {
                player.setHealth(newMaxHealth);
            }
        }
    }
    
    // 保存玩家当前生命值
    private static void savePlayerHealth(Player player) {
        CompoundTag persistentData = player.getPersistentData();
        CompoundTag vitalityData = persistentData.getCompound(VITALITY_BOOST_DATA_TAG);
        vitalityData.putFloat(SAVED_HEALTH_KEY, player.getHealth());
        persistentData.put(VITALITY_BOOST_DATA_TAG, vitalityData);
    }
    
    // 恢复玩家生命值
    private static void restorePlayerHealth(Player player) {
        CompoundTag persistentData = player.getPersistentData();
        if (persistentData.contains(VITALITY_BOOST_DATA_TAG, CompoundTag.TAG_COMPOUND)) {
            CompoundTag vitalityData = persistentData.getCompound(VITALITY_BOOST_DATA_TAG);
            if (vitalityData.contains(SAVED_HEALTH_KEY)) {
                float savedHealth = vitalityData.getFloat(SAVED_HEALTH_KEY);
                // 确保恢复的生命值不超过新的最大生命值
                float maxHealth = player.getMaxHealth();
                player.setHealth(Math.min(savedHealth, maxHealth));
            }
        }
    }
}