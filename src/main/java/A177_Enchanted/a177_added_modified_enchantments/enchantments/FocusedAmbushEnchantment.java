package A177_Enchanted.a177_added_modified_enchantments.enchantments;

import A177_Enchanted.a177_added_modified_enchantments.config.AllEnchantmentsConfig;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import A177_Enchanted.a177_added_modified_enchantments.init.ModEnchantments;

@Mod.EventBusSubscriber
public class FocusedAmbushEnchantment extends Enchantment {
    // 每级伤害增加百分比 (15%)
    private static final double DAMAGE_BONUS_PER_LEVEL = 0.15;
    
    private static AllEnchantmentsConfig.EnchantConfig getConfig() {
        return AllEnchantmentsConfig.ENCHANTMENTS.get("focused_ambush");
    }

    public FocusedAmbushEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
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
        return this.getMinCost(level) + 15;
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
        // 只能附在武器上
        return stack.getItem() instanceof SwordItem;
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 检查伤害来源是否为玩家
        if (event.getSource().getEntity() instanceof Player player) {
            // 检查玩家是否蹲下
            if (player.isCrouching()) {
                ItemStack weapon = player.getMainHandItem();
                
                // 检查武器是否带有专注伏击附魔
                if (!weapon.isEmpty() && weapon.isEnchanted() && weapon.getEnchantmentLevel(ModEnchantments.FOCUSED_AMBUSH.get()) > 0) {
                    int level = weapon.getEnchantmentLevel(ModEnchantments.FOCUSED_AMBUSH.get());
                    
                    // 计算伤害加成：每级15%
                    float additionalDamage = event.getAmount() * (float) (level * DAMAGE_BONUS_PER_LEVEL);
                    event.setAmount(event.getAmount() + additionalDamage);
                }
            }
        }
    }
}