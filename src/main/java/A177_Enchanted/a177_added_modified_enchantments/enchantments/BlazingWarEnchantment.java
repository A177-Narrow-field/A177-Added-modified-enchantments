package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;

import java.util.UUID;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber
public class BlazingWarEnchantment extends Enchantment {
    // 移动速度增加的UUID
    public static final UUID SPEED_MODIFIER_UUID = UUID.fromString("C1D2E3F4-A5B6-7890-ABCD-EF1234567893");
    
    // 攻击速度增加的UUID
    public static final UUID ATTACK_SPEED_MODIFIER_UUID = UUID.fromString("D1E2F3A4-B5C6-7890-ABCD-EF1234567894");
    
    // 缓存玩家的附魔等级
    private static final WeakHashMap<Player, Integer> PLAYER_ENCHANTMENT_CACHE = new WeakHashMap<>();
    
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("blazing_war");
    }

    public BlazingWarEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[]{EquipmentSlot.CHEST});
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }// 附魔等级

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }// 可在附魔台发现

    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }// 是否为宝藏附魔

    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }// 是否可交易

    @Override
    public int getMinCost(int level) {
        return 10 + (level - 1) * 10;
    }// 附魔最小成本
    
    @Override
    public int getMaxCost(int level) {
        return super.getMinCost(level) + 50;
    }// 附魔最大成本
    
    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem && 
               ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.CHEST;
    }// 是否能装备

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();}//可以正确的出现在附魔台

    // 当玩家装备变化时检查是否需要应用效果
    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof Player player && event.getSlot() == EquipmentSlot.CHEST) {
            // 延迟一段时间再检查，确保装备已经正确更新
            player.level().scheduleTick(player.blockPosition(), net.minecraft.world.level.block.Blocks.AIR, 1);
        }
    }
    
    // 当玩家tick时检查燃烧状态并应用效果
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        
        // 检查玩家是否在燃烧
        if (player.isOnFire()) {
            // 检查胸甲上的附魔等级
            ItemStack chestArmor = player.getItemBySlot(EquipmentSlot.CHEST);
            int level = chestArmor.getEnchantmentLevel(ModEnchantments.BLAZING_WAR.get());
            
            if (level > 0) {
                // 应用效果：每级增加的移动速度和攻击速度
                applyEffects(player, level);
                PLAYER_ENCHANTMENT_CACHE.put(player, level);
            } else {
                // 移除效果
                removeEffects(player);
                PLAYER_ENCHANTMENT_CACHE.remove(player);
            }
        } else {
            // 移除效果
            removeEffects(player);
            PLAYER_ENCHANTMENT_CACHE.remove(player);
        }
    }
    
    // 应用效果
    private static void applyEffects(Player player, int level) {
        // 增加移动速度 (每级20%)
        double speedBonus = level * 0.2;
        AttributeModifier speedModifier = new AttributeModifier(
            SPEED_MODIFIER_UUID, 
            "Blazing war speed bonus", 
            speedBonus, 
            AttributeModifier.Operation.MULTIPLY_BASE
        );
        
        // 增加攻击速度 (每级20%)
        double attackSpeedBonus = level * 0.2;
        AttributeModifier attackSpeedModifier = new AttributeModifier(
            ATTACK_SPEED_MODIFIER_UUID, 
            "Blazing war attack speed bonus", 
            attackSpeedBonus, 
            AttributeModifier.Operation.MULTIPLY_BASE
        );
        
        // 应用移动速度修饰符
        if (player.getAttributes().hasAttribute(Attributes.MOVEMENT_SPEED)) {
            var attributeInstance = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (attributeInstance != null) {
                // 移除旧的修饰符（如果存在）
                if (attributeInstance.getModifier(SPEED_MODIFIER_UUID) != null) {
                    attributeInstance.removeModifier(SPEED_MODIFIER_UUID);
                }
                // 添加新的修饰符
                attributeInstance.addTransientModifier(speedModifier);
            }
        }
        
        // 应用攻击速度修饰符
        if (player.getAttributes().hasAttribute(Attributes.ATTACK_SPEED)) {
            var attributeInstance = player.getAttribute(Attributes.ATTACK_SPEED);
            if (attributeInstance != null) {
                // 移除旧的修饰符（如果存在）
                if (attributeInstance.getModifier(ATTACK_SPEED_MODIFIER_UUID) != null) {
                    attributeInstance.removeModifier(ATTACK_SPEED_MODIFIER_UUID);
                }
                // 添加新的修饰符
                attributeInstance.addTransientModifier(attackSpeedModifier);
            }
        }
    }
    
    // 移除效果
    private static void removeEffects(Player player) {
        // 移除移动速度修饰符
        if (player.getAttributes().hasAttribute(Attributes.MOVEMENT_SPEED)) {
            var attributeInstance = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (attributeInstance != null && attributeInstance.getModifier(SPEED_MODIFIER_UUID) != null) {
                attributeInstance.removeModifier(SPEED_MODIFIER_UUID);
            }
        }
        
        // 移除攻击速度修饰符
        if (player.getAttributes().hasAttribute(Attributes.ATTACK_SPEED)) {
            var attributeInstance = player.getAttribute(Attributes.ATTACK_SPEED);
            if (attributeInstance != null && attributeInstance.getModifier(ATTACK_SPEED_MODIFIER_UUID) != null) {
                attributeInstance.removeModifier(ATTACK_SPEED_MODIFIER_UUID);
            }
        }
    }
}