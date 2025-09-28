package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;

@Mod.EventBusSubscriber
public class CriticalOverclockEnchantment extends Enchantment {
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("critical_overclock");
    }

    public CriticalOverclockEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 10;
    }

    @Override
    public int getMinCost(int level) {
        return 15 + (level - 1) * 15;
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + 20;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return EnchantmentCategory.WEAPON.canEnchant(stack.getItem()) || EnchantmentCategory.DIGGER.canEnchant(stack.getItem());
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
    public boolean isAllowedOnBooks() {
        // 允许在附魔书上显示，这样创造模式物品栏会显示附魔书
        return true;
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        // 与伤害暴击附魔冲突
        return super.checkCompatibility(other) && other != ModEnchantments.DAMAGE_CRITICAL.get();
    }

    // 超频暴击的事件处理已移至各个暴击触发附魔类中
}