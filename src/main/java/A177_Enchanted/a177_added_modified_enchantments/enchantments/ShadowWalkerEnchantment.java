package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

public class ShadowWalkerEnchantment extends Enchantment {
    public ShadowWalkerEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.ARMOR_FEET, new EquipmentSlot[]{EquipmentSlot.FEET});
    }
    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinCost(int level) {
        return 20 + (level - 1) * 20;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 20;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem && 
               ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.FEET;//确保只有脚装备可以添加此附魔
    }

    @Override
    public boolean isTreasureOnly() {
        return AllEnchantmentsConfig.SHADOW_WALKER.isTreasureOnly.get();
    }

    @Override
    public boolean isDiscoverable() {
        return AllEnchantmentsConfig.SHADOW_WALKER.isDiscoverable.get();
    }// 可在附魔台发现

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && 
               AllEnchantmentsConfig.SHADOW_WALKER.isDiscoverable.get();}//确保在附魔台中可以正确应用

    @Override
    public boolean isTradeable() {
        return AllEnchantmentsConfig.SHADOW_WALKER.isTradeable.get();
    }// 不可通过交易获得

}