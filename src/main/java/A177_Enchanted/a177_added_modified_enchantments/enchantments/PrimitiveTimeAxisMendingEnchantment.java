package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

public class PrimitiveTimeAxisMendingEnchantment extends Enchantment {

    public PrimitiveTimeAxisMendingEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.BREAKABLE, EquipmentSlot.values());
    }

    @Override
    public int getMaxLevel() {
        return 1; // 设置最大等级为1
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 15;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.isDamageableItem(); // 可以在物品上使用
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return stack.isDamageableItem(); // 可以在附魔台使用
    }

    @Override
    public boolean isTreasureOnly() {
        return AllEnchantmentsConfig.PRIMITIVE_TIME_AXIS_MENDING.isTreasureOnly.get();
    }

    @Override
    public boolean isTradeable() {
        return AllEnchantmentsConfig.PRIMITIVE_TIME_AXIS_MENDING.isTradeable.get();
    }

    @Override
    public boolean isDiscoverable() {
        return AllEnchantmentsConfig.PRIMITIVE_TIME_AXIS_MENDING.isDiscoverable.get();
    }

    @Override
    public boolean checkCompatibility(Enchantment other) {
        // 与其他修复类附魔冲突（如经验修补、初级经验修补）
        return super.checkCompatibility(other) &&
                !other.getDescriptionId().equals("minecraft:mending") &&
                !other.getDescriptionId().equals("your_mod_id:primitive_mending");
    }

    /**
     * 计算应该恢复的耐久度
     * @param level 附魔等级
     * @return 应该恢复的耐久度
     */
    public static int getDurabilityRestored(int level, ItemStack stack) {
        if (level <= 0 || stack.isEmpty() || !stack.isDamaged()) {
            return 0;
        }

        // 每级回复+1%耐久，最少回复1点
        int maxDamage = stack.getMaxDamage();
        int restoreAmount = Math.max(1, (int) (maxDamage * level * 0.01));

        return restoreAmount;
    }
}