package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

public class AdvancedTimeAxisMendingEnchantment extends Enchantment {

    public AdvancedTimeAxisMendingEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.BREAKABLE, EquipmentSlot.values());
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMinCost(int level) {
        return level * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 15;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 可以附在任何可损坏的物品上
        return stack.isDamageableItem();
    }


    @Override
    public boolean isTreasureOnly() {
        return AllEnchantmentsConfig.ADVANCED_TIME_AXIS_MENDING.isTreasureOnly.get();
    }

    @Override
    public boolean isTradeable() {
        return AllEnchantmentsConfig.ADVANCED_TIME_AXIS_MENDING.isTradeable.get();
    }

    @Override
    public boolean isDiscoverable() {
        return AllEnchantmentsConfig.ADVANCED_TIME_AXIS_MENDING.isDiscoverable.get();
    }
}