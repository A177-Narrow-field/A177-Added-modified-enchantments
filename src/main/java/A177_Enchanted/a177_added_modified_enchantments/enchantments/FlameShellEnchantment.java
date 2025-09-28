package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class FlameShellEnchantment extends Enchantment {
    // 获取配置
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("flame_shell");
    }
    
    public FlameShellEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[]{EquipmentSlot.CHEST});
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinCost(int level) {
        return 20 + (level - 1) * 15;
    }

    @Override
    public int getMaxCost(int level) {
        return super.getMinCost(level) + 50;
    }

    @Override
    public boolean isTreasureOnly() {
        return getConfig().isTreasureOnly.get();
    }

    @Override
    public boolean isDiscoverable() {
        return getConfig().isDiscoverable.get();
    }

    @Override
    public boolean isTradeable() {
        return getConfig().isTradeable.get();
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        // 只能附在胸甲上
        if (stack.getItem() instanceof ArmorItem) {
            return ((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.CHEST;
        }
        return false;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        // 根据配置决定是否可在附魔台获得
        return this.category.canEnchant(stack.getItem()) && isDiscoverable();
    }
    
    // 与绝境回韧和胃袋附魔冲突
    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return super.checkCompatibility(other) 
                && other != ModEnchantments.DESPERATE_RESILIENCE.get()
                && other != ModEnchantments.STOMACH_POUCH.get();
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 检查是否是玩家受到伤害
        if (!(event.getEntity() instanceof Player player)) return;
        
        // 检查玩家是否装备了炎壳附魔的胸甲
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.FLAME_SHELL.get(), chestplate);
        
        // 如果没有装备或者附魔等级为0，则不处理
        if (level <= 0) return;
        
        // 检查玩家是否正在燃烧
        if (player.getRemainingFireTicks() > 0) {
            // 减少伤害，每级减少25%伤害
            float damageReduction = 0.25f * level;
            float reducedDamage = event.getAmount() * (1.0f - damageReduction);
            event.setAmount(reducedDamage);
        }
    }
}