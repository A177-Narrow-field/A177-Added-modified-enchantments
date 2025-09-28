package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
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
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class OxygenDepletingSprintEnchantment extends Enchantment {
    // 移动速度修饰符UUID
    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("A1B2C3D4-E5F6-7890-ABCD-EF1234567890");
    
    // 存储玩家的附魔等级
    private static final Map<UUID, Integer> PLAYER_ENCHANTMENT_LEVELS = new HashMap<>();
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("oxygen_depleting_sprint");
    }

    public OxygenDepletingSprintEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.ARMOR_FEET, new EquipmentSlot[]{EquipmentSlot.FEET});
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinCost(int level) {
        return 10 + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 20;
    }

    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
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
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem &&
                ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.FEET;
    }

    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player) || event.getSlot() != EquipmentSlot.FEET) {
            return;
        }

        // 获取玩家的移动速度属性
        var speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute == null) return;

        // 移除旧的修饰符
        if (speedAttribute.getModifier(SPEED_MODIFIER_UUID) != null) {
            speedAttribute.removeModifier(SPEED_MODIFIER_UUID);
        }

        // 检查新的装备是否具有耗氧冲刺附魔
        ItemStack newItem = event.getTo();
        int level = newItem.getEnchantmentLevel(ModEnchantments.OXYGEN_DEPLETING_SPRINT.get());
        
        // 更新玩家的附魔等级缓存
        UUID playerId = player.getUUID();
        if (level > 0) {
            PLAYER_ENCHANTMENT_LEVELS.put(playerId, level);
        } else {
            PLAYER_ENCHANTMENT_LEVELS.remove(playerId);
        }

        // 如果有附魔且玩家正在疾跑，应用速度增加效果
        if (level > 0 && player.isSprinting()) {
            // 每级增加40%移动速度
            AttributeModifier speedModifier = new AttributeModifier(
                    SPEED_MODIFIER_UUID,
                    "Oxygen depleting sprint speed boost",
                    (double) level * 0.4,
                    AttributeModifier.Operation.MULTIPLY_TOTAL
            );
            speedAttribute.addTransientModifier(speedModifier);
        }
    }
    
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        Player player = event.player;
        if (player.level().isClientSide()) {
            return;
        }

        UUID playerId = player.getUUID();
        // 检查玩家是否装备了耗氧冲刺附魔
        Integer level = PLAYER_ENCHANTMENT_LEVELS.get(playerId);
        
        if (level != null && level > 0) {
            // 获取玩家的移动速度属性
            var speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttribute == null) return;
            
            boolean isSprinting = player.isSprinting();
            boolean hasModifier = speedAttribute.getModifier(SPEED_MODIFIER_UUID) != null;
            
            // 如果正在疾跑但没有速度加成，则添加
            if (isSprinting && !hasModifier) {
                AttributeModifier speedModifier = new AttributeModifier(
                        SPEED_MODIFIER_UUID,
                        "Oxygen depleting sprint speed boost",
                        (double) level * 0.4,
                        AttributeModifier.Operation.MULTIPLY_TOTAL
                );
                speedAttribute.addTransientModifier(speedModifier);
            } 
            // 如果没有疾跑但有速度加成，则移除
            else if (!isSprinting && hasModifier) {
                speedAttribute.removeModifier(SPEED_MODIFIER_UUID);
            }
        }
    }
}