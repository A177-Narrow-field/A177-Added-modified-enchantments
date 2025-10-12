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

import java.util.UUID;

@Mod.EventBusSubscriber
public class SturdyBarbarianEnchantment extends Enchantment {
    // 每高于基础生命值1点增加的伤害
    private static final double DAMAGE_PER_HEALTH_ABOVE_BASE = 0.1;
    // 基础生命值（10颗心）
    private static final double BASE_HEALTH = 20.0;

    // 属性修饰符UUID，用于唯一标识我们的修饰符
    private static final UUID DAMAGE_MODIFIER_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5E1");

    public SturdyBarbarianEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[]{EquipmentSlot.CHEST});
    }

    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("sturdy_barbarian");
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
            // 修复空指针异常：检查AttributeInstance是否为null
            var attributes = event.getEntity().getAttributes();
            var attributeInstance = attributes.getInstance(Attributes.ATTACK_DAMAGE);
            if (attributeInstance != null) {
                attributeInstance.removeModifier(DAMAGE_MODIFIER_UUID);
            }

            // 如果有新的装备且有磐蛮附魔，则添加属性修饰符
            ItemStack newItem = event.getTo();
            if (!newItem.isEmpty()) {
                int sturdyBarbarianLevel = newItem.getEnchantmentLevel(ModEnchantments.STURDY_BARBARIAN.get());
                if (sturdyBarbarianLevel > 0 && event.getEntity() instanceof Player) {
                    updateDamageModifier((Player) event.getEntity());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player != null) {
            // 检查玩家是否穿着有磐蛮附魔的胸甲
            ItemStack chestplate = event.player.getItemBySlot(EquipmentSlot.CHEST);
            // 修复空指针异常：检查物品是否为空
            int sturdyBarbarianLevel = 0;
            if (!chestplate.isEmpty()) {
                sturdyBarbarianLevel = chestplate.getEnchantmentLevel(ModEnchantments.STURDY_BARBARIAN.get());
            }

            if (sturdyBarbarianLevel > 0) {
                updateDamageModifier(event.player);
            } else {
                // 如果没有附魔，移除修饰符
                var attributes = event.player.getAttributes();
                var damageAttribute = attributes.getInstance(Attributes.ATTACK_DAMAGE);
                if (damageAttribute != null) {
                    damageAttribute.removeModifier(DAMAGE_MODIFIER_UUID);
                }
            }
        }
    }

    private static void updateDamageModifier(Player player) {
        var damageAttribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damageAttribute == null) return;

        // 移除旧的修饰符
        damageAttribute.removeModifier(DAMAGE_MODIFIER_UUID);

        // 计算玩家超出基础生命值的部分
        double healthAboveBase = Math.max(0, player.getMaxHealth() - BASE_HEALTH);
        double damageBonus = healthAboveBase * DAMAGE_PER_HEALTH_ABOVE_BASE;

        // 添加伤害修饰符
        AttributeModifier damageModifier = new AttributeModifier(
                DAMAGE_MODIFIER_UUID,
                "Sturdy barbarian damage",
                damageBonus,
                AttributeModifier.Operation.ADDITION
        );
        damageAttribute.addTransientModifier(damageModifier);
    }
}