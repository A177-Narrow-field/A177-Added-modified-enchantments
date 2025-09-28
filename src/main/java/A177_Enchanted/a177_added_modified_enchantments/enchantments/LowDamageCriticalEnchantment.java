package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

public class LowDamageCriticalEnchantment extends Enchantment {
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("low_damage_critical");
    }

    public LowDamageCriticalEnchantment() {
        super(Rarity.COMMON, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public int getMinCost(int level) {
        return 5 + (level - 1) * 5;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 10;
    }

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
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
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 可以附在工具和武器上
        return EnchantmentCategory.WEAPON.canEnchant(stack.getItem()) ||
                EnchantmentCategory.DIGGER.canEnchant(stack.getItem());
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        // 与超频暴击和伤害暴击附魔冲突
        return super.checkCompatibility(other) && 
               other != ModEnchantments.CRITICAL_OVERCLOCK.get() && 
               other != ModEnchantments.DAMAGE_CRITICAL.get();
    }
}