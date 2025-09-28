package A177_Enchanted.a177_added_modified_enchantments.enchantments;

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

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;

import java.lang.reflect.Field;
import java.util.UUID;

@Mod.EventBusSubscriber
public class ObesityEnchantment extends Enchantment {
    // 每高于基础生命值1点增加的属性
    private static final double KNOCKBACK_RESISTANCE_PER_HEALTH_ABOVE_BASE = 1.0;
    private static final double ARMOR_PER_HEALTH_ABOVE_BASE = 1.0;
    private static final double MOVEMENT_SLOWDOWN_PER_HEALTH_ABOVE_BASE = 0.01; // 1%移动速度
    private static final double GRAVITY_PER_HEALTH_ABOVE_BASE = 0.01; // 1%重力
    // 基础生命值（10颗心）
    private static final double BASE_HEALTH = 20.0;

    // 属性修饰符UUID，用于唯一标识我们的修饰符
    private static final UUID KNOCKBACK_RESISTANCE_MODIFIER_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5E3");
    private static final UUID ARMOR_MODIFIER_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5E4");
    private static final UUID MOVEMENT_SLOWDOWN_MODIFIER_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5E5");
    private static final UUID GRAVITY_MODIFIER_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5E6");

    public ObesityEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[]{EquipmentSlot.CHEST});
    }

    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("obesity");
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
            // 移除旧的属性修饰符
            event.getEntity().getAttributes().getInstance(Attributes.KNOCKBACK_RESISTANCE).removeModifier(KNOCKBACK_RESISTANCE_MODIFIER_UUID);
            event.getEntity().getAttributes().getInstance(Attributes.ARMOR).removeModifier(ARMOR_MODIFIER_UUID);
            event.getEntity().getAttributes().getInstance(Attributes.MOVEMENT_SPEED).removeModifier(MOVEMENT_SLOWDOWN_MODIFIER_UUID);
            
            // 尝试移除重力修饰符
            try {
                Field gravityField = Attributes.class.getDeclaredField("GRAVITY");
                gravityField.setAccessible(true);
                Object gravityAttribute = gravityField.get(null);
                event.getEntity().getAttributes().getInstance((net.minecraft.world.entity.ai.attributes.Attribute) gravityAttribute).removeModifier(GRAVITY_MODIFIER_UUID);
            } catch (Exception e) {
                // 如果无法访问重力属性，则忽略
            }

            // 如果有新的装备且有肥硕附魔，则添加属性修饰符
            ItemStack newItem = event.getTo();
            if (!newItem.isEmpty()) {
                int obesityLevel = newItem.getEnchantmentLevel(ModEnchantments.OBESITY.get());
                if (obesityLevel > 0 && event.getEntity() instanceof Player) {
                    updateModifiers((Player) event.getEntity());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player != null) {
            // 检查玩家是否穿着有肥硕附魔的胸甲
            ItemStack chestplate = event.player.getItemBySlot(EquipmentSlot.CHEST);
            int obesityLevel = chestplate.getEnchantmentLevel(ModEnchantments.OBESITY.get());

            if (obesityLevel > 0) {
                updateModifiers(event.player);
            } else {
                // 如果没有附魔，移除修饰符
                event.player.getAttributes().getInstance(Attributes.KNOCKBACK_RESISTANCE).removeModifier(KNOCKBACK_RESISTANCE_MODIFIER_UUID);
                event.player.getAttributes().getInstance(Attributes.ARMOR).removeModifier(ARMOR_MODIFIER_UUID);
                event.player.getAttributes().getInstance(Attributes.MOVEMENT_SPEED).removeModifier(MOVEMENT_SLOWDOWN_MODIFIER_UUID);
                
                // 尝试移除重力修饰符
                try {
                    Field gravityField = Attributes.class.getDeclaredField("GRAVITY");
                    gravityField.setAccessible(true);
                    Object gravityAttribute = gravityField.get(null);
                    event.player.getAttributes().getInstance((net.minecraft.world.entity.ai.attributes.Attribute) gravityAttribute).removeModifier(GRAVITY_MODIFIER_UUID);
                } catch (Exception e) {
                    // 如果无法访问重力属性，则忽略
                }
            }
        }
    }

    private static void updateModifiers(Player player) {
        var knockbackResistanceAttribute = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        var armorAttribute = player.getAttribute(Attributes.ARMOR);
        var movementSpeedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        
        if (knockbackResistanceAttribute == null || armorAttribute == null || movementSpeedAttribute == null) return;

        // 移除旧的修饰符
        knockbackResistanceAttribute.removeModifier(KNOCKBACK_RESISTANCE_MODIFIER_UUID);
        armorAttribute.removeModifier(ARMOR_MODIFIER_UUID);
        movementSpeedAttribute.removeModifier(MOVEMENT_SLOWDOWN_MODIFIER_UUID);

        // 计算玩家超出基础生命值的部分
        double healthAboveBase = Math.max(0, player.getMaxHealth() - BASE_HEALTH);
        
        // 计算各属性加成
        double knockbackResistanceBonus = healthAboveBase * KNOCKBACK_RESISTANCE_PER_HEALTH_ABOVE_BASE;
        double armorBonus = healthAboveBase * ARMOR_PER_HEALTH_ABOVE_BASE;
        double movementSlowdown = -healthAboveBase * MOVEMENT_SLOWDOWN_PER_HEALTH_ABOVE_BASE; // 负值表示减速

        // 添加击退抗性修饰符
        AttributeModifier knockbackResistanceModifier = new AttributeModifier(
                KNOCKBACK_RESISTANCE_MODIFIER_UUID,
                "Obesity knockback resistance",
                knockbackResistanceBonus,
                AttributeModifier.Operation.ADDITION
        );
        knockbackResistanceAttribute.addTransientModifier(knockbackResistanceModifier);

        // 添加护甲值修饰符
        AttributeModifier armorModifier = new AttributeModifier(
                ARMOR_MODIFIER_UUID,
                "Obesity armor",
                armorBonus,
                AttributeModifier.Operation.ADDITION
        );
        armorAttribute.addTransientModifier(armorModifier);

        // 添加移动速度修饰符（减速）
        AttributeModifier movementSlowdownModifier = new AttributeModifier(
                MOVEMENT_SLOWDOWN_MODIFIER_UUID,
                "Obesity movement slowdown",
                movementSlowdown,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
        movementSpeedAttribute.addTransientModifier(movementSlowdownModifier);
        
        // 尝试添加重力修饰符
        try {
            Field gravityField = Attributes.class.getDeclaredField("GRAVITY");
            gravityField.setAccessible(true);
            Object gravityAttribute = gravityField.get(null);
            
            var gravityAttr = player.getAttribute((net.minecraft.world.entity.ai.attributes.Attribute) gravityAttribute);
            if (gravityAttr != null) {
                gravityAttr.removeModifier(GRAVITY_MODIFIER_UUID);
                
                double gravityBonus = healthAboveBase * GRAVITY_PER_HEALTH_ABOVE_BASE;
                AttributeModifier gravityModifier = new AttributeModifier(
                        GRAVITY_MODIFIER_UUID,
                        "Obesity gravity",
                        gravityBonus,
                        AttributeModifier.Operation.MULTIPLY_TOTAL
                );
                gravityAttr.addTransientModifier(gravityModifier);
            }
        } catch (Exception e) {
            // 如果无法访问重力属性，则忽略
        }
    }
}