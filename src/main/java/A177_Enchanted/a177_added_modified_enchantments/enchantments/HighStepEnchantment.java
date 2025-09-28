package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

public class HighStepEnchantment extends Enchantment {
    public HighStepEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentCategory.ARMOR_FEET, new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS});
    }

    @Override
    public int getMaxLevel() {
        return 10;
    }

    @Override
    public int getMinCost(int level) {
        return level * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 1;
    }

    @Override
    public boolean isTreasureOnly() {
        return AllEnchantmentsConfig.HIGH_STEP.isTreasureOnly.get();
    }

    @Override
    public boolean isDiscoverable() {
        return AllEnchantmentsConfig.HIGH_STEP.isDiscoverable.get();
    }// 可在附魔台发现

    @Override
    public boolean isTradeable() {
        return AllEnchantmentsConfig.HIGH_STEP.isTradeable.get();
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && 
               AllEnchantmentsConfig.HIGH_STEP.isDiscoverable.get();}//确保在附魔台中可以正确应用

}