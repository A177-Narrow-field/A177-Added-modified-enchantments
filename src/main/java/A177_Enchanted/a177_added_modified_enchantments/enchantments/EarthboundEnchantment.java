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
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class EarthboundEnchantment extends Enchantment {
    // 定义属性修饰符的UUID
    private static final UUID ARMOR_UUID = UUID.fromString("12C445B4-7A6D-4E88-B3C3-8F9E1E7A2D4F");
    private static final UUID ARMOR_TOUGHNESS_UUID = UUID.fromString("23D556C5-8B7E-4F99-C4D4-9A0F2F8B3E5A");
    private static final UUID KNOCKBACK_RESISTANCE_UUID = UUID.fromString("34E667D6-9C8F-4A00-D5E5-0B1A3A9C4F6B");
    private static final UUID GRAVITY_UUID = UUID.fromString("56F778D7-0C90-4B00-E6F6-1C2D4E7F9A0B");
    
    // 缓存玩家的下次更新时间
    private static final Map<UUID, Long> PLAYER_NEXT_UPDATE_TIME = new HashMap<>();
    
    // 重力效果持续时间（ticks）
    private static final int GRAVITY_DURATION = 20; // 1秒 (20 ticks = 1秒)
    
    // 玩家重力效果的到期时间缓存
    private static final Map<UUID, Long> PLAYER_GRAVITY_END_TIME = new HashMap<>();
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("earthbound");
    }

    public EarthboundEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentCategory.ARMOR, new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET});
    }

    @Override
    public int getMaxLevel() {
        return 1;
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
        return stack.getItem() instanceof ArmorItem;
    }


    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        // 只有当配置允许且物品是盔甲时才能在附魔台中应用
        return isDiscoverable() && canEnchant(stack);
    }//可以正确的出现在附魔台

    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }// 可通过交易获得

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }// 可在附魔台发现
    
    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }// 宝藏附魔

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START && !event.player.level().isClientSide) {
            Player player = event.player;
            UUID playerId = player.getUUID();
            long currentTime = player.level().getGameTime();
            
            // 检查玩家是否装备了地缚附魔的盔甲并计算总等级
            int earthboundLevel = 0;
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                    ItemStack stack = player.getItemBySlot(slot);
                    if (stack.isEnchanted() && stack.getEnchantmentLevel(ModEnchantments.EARTHBOUND.get()) > 0) {
                        earthboundLevel += stack.getEnchantmentLevel(ModEnchantments.EARTHBOUND.get());
                    }
                }
            }
            
            // 检查重力效果是否应该结束
            Long gravityEndTime = PLAYER_GRAVITY_END_TIME.get(playerId);
            if (gravityEndTime != null && currentTime >= gravityEndTime) {
                removeEarthboundEffects(player);
                PLAYER_GRAVITY_END_TIME.remove(playerId);
            }
            
            // 如果装备了地缚附魔
            if (earthboundLevel > 0) {
                // 检查是否需要更新效果
                Long nextUpdateTime = PLAYER_NEXT_UPDATE_TIME.get(playerId);
                if (nextUpdateTime == null || currentTime >= nextUpdateTime) {
                    applyAttributeModifiers(player, earthboundLevel, currentTime);
                    PLAYER_NEXT_UPDATE_TIME.put(playerId, currentTime + 20); // 每秒更新一次
                }
            } else {
                // 移除属性修饰符
                removeAttributeModifiers(player);
            }
        }
    }
    
    private static void applyAttributeModifiers(Player player, int level, long currentTime) {
        // 添加护甲值
        if (player.getAttribute(Attributes.ARMOR) != null) {
            AttributeModifier armorModifier = new AttributeModifier(
                ARMOR_UUID,
                "Earthbound armor",
                3 * level,
                AttributeModifier.Operation.ADDITION
            );
            if (player.getAttribute(Attributes.ARMOR).getModifier(ARMOR_UUID) == null) {
                player.getAttribute(Attributes.ARMOR).addTransientModifier(armorModifier);
            }
        }
        
        // 添加韧性
        if (player.getAttribute(Attributes.ARMOR_TOUGHNESS) != null) {
            AttributeModifier toughnessModifier = new AttributeModifier(
                ARMOR_TOUGHNESS_UUID,
                "Earthbound armor toughness",
                5 * level,
                AttributeModifier.Operation.ADDITION
            );
            if (player.getAttribute(Attributes.ARMOR_TOUGHNESS).getModifier(ARMOR_TOUGHNESS_UUID) == null) {
                player.getAttribute(Attributes.ARMOR_TOUGHNESS).addTransientModifier(toughnessModifier);
            }
        }
        
        // 添加击退抗性
        if (player.getAttribute(Attributes.KNOCKBACK_RESISTANCE) != null) {
            AttributeModifier knockbackModifier = new AttributeModifier(
                KNOCKBACK_RESISTANCE_UUID,
                "Earthbound knockback resistance",
                1 * level,
                AttributeModifier.Operation.ADDITION
            );
            if (player.getAttribute(Attributes.KNOCKBACK_RESISTANCE).getModifier(KNOCKBACK_RESISTANCE_UUID) == null) {
                player.getAttribute(Attributes.KNOCKBACK_RESISTANCE).addTransientModifier(knockbackModifier);
            }
        }
        
        // 使用AttributeModifier实现重力效果
        // 计算重力系数，基于附魔等级
        double gravityFactor = level * 0.15; // 每级增加15%重力
        
        AttributeInstance gravityAttribute = player.getAttribute(ForgeMod.ENTITY_GRAVITY.get());
        
        // 移除旧的修饰符（如果存在）
        AttributeModifier existingGravityModifier = gravityAttribute.getModifier(GRAVITY_UUID);
        if (existingGravityModifier != null) {
            gravityAttribute.removeModifier(existingGravityModifier);
        }
        
        // 添加重力修饰符
        AttributeModifier gravityModifier = new AttributeModifier(
            GRAVITY_UUID,
            "Earthbound Gravity",
            gravityFactor,
            AttributeModifier.Operation.MULTIPLY_BASE
        );
        gravityAttribute.addTransientModifier(gravityModifier);
        
        // 设置重力效果的到期时间
        PLAYER_GRAVITY_END_TIME.put(player.getUUID(), currentTime + GRAVITY_DURATION);
    }
    
    private static void removeAttributeModifiers(Player player) {
        // 移除护甲值修饰符
        if (player.getAttribute(Attributes.ARMOR) != null) {
            AttributeModifier armorModifier = player.getAttribute(Attributes.ARMOR).getModifier(ARMOR_UUID);
            if (armorModifier != null) {
                player.getAttribute(Attributes.ARMOR).removeModifier(armorModifier);
            }
        }
        
        // 移除韧性修饰符
        if (player.getAttribute(Attributes.ARMOR_TOUGHNESS) != null) {
            AttributeModifier toughnessModifier = player.getAttribute(Attributes.ARMOR_TOUGHNESS).getModifier(ARMOR_TOUGHNESS_UUID);
            if (toughnessModifier != null) {
                player.getAttribute(Attributes.ARMOR_TOUGHNESS).removeModifier(toughnessModifier);
            }
        }
        
        // 移除击退抗性修饰符
        if (player.getAttribute(Attributes.KNOCKBACK_RESISTANCE) != null) {
            AttributeModifier knockbackModifier = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE).getModifier(KNOCKBACK_RESISTANCE_UUID);
            if (knockbackModifier != null) {
                player.getAttribute(Attributes.KNOCKBACK_RESISTANCE).removeModifier(knockbackModifier);
            }
        }
        
        // 移除重力效果
        removeEarthboundEffects(player);
    }
    
    /**
     * 移除地缚附魔效果
     * @param player 玩家实体
     */
    private static void removeEarthboundEffects(Player player) {
        AttributeInstance gravityAttribute = player.getAttribute(ForgeMod.ENTITY_GRAVITY.get());
        AttributeModifier gravityModifier = gravityAttribute.getModifier(GRAVITY_UUID);
        
        if (gravityModifier != null) {
            gravityAttribute.removeModifier(gravityModifier);
        }
    }
}